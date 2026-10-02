#!/usr/bin/env python3
"""Play Policy Insights: a pre-flight check of a Cyan Harbor Android app against
Google Play Developer Program Policies, modelled on Android Studio's
"Code > Inspect for Play Policy Insights" but covering what an Expo app needs:

  build     the release APK/AAB: target API, permissions, foreground-service
            types, debuggable flag, 16 KB native-library alignment
  code      in-app privacy-policy link, ad content rating cap, EU consent
            privacy-options entry point, payment steering, trademarks, real
            people's names
  listing   title / short / full description limits and claims
  graphics  Play icon, feature graphic, phone screenshots
  data      Data safety answers vs the SDKs and the privacy policy page
  online    live privacy page matches the repo, app-ads.txt (with --online)
  gradle    Google's own Play Policy Insights lint rules (insights-lint) and the
            resolved Billing Library / Mobile Ads SDK versions (with --gradle)

Usage (from anywhere inside the app repo):
  python3 <this skill>/scripts/play_policy_insights.py
  ... --apk ~/CyanHarbor/builds/<dated>/<name>.apk --aab <name>.aab
  ... --online --gradle            # the full pre-release run
  ... --json                       # machine-readable findings
  ... --repo <path> --config <file>   # when not run from inside the app repo

Exit code: 1 if any ERROR, else 0. Warnings and notes never fail the run.
The checker lives in the shared Cyan Harbor playbook plugin; the app it checks
is the git repo of the current directory. Per-app settings come from that repo:
play-policy-config.json at its root (copy policy-config.example.json from the
skill folder and edit it). Needs only the standard library, plus aapt2 from the
Android SDK for the build checks.
"""
from __future__ import annotations

import argparse
import glob
import html
import json
import os
import re
import shutil
import struct
import subprocess
import sys
import tempfile
import urllib.request
import zipfile
from pathlib import Path

SKILL_DIR = Path(__file__).resolve().parent.parent
REPO = Path.cwd()   # set by configure()
CONFIG: dict = {}   # set by configure()

CONFIG_CANDIDATES = ("play-policy-config.json", ".claude/play-policy-config.json", "docs/launch/play-policy-config.json")


def repo_root(start: Path) -> Path:
    try:
        out = subprocess.run(["git", "-C", str(start), "rev-parse", "--show-toplevel"],
                             capture_output=True, text=True, check=True).stdout.strip()
        if out:
            return Path(out)
    except (OSError, subprocess.CalledProcessError):
        pass
    return start


def configure(repo: Path | None, config: Path | None) -> None:
    """The checker is shared across apps, so the app under test is whichever
    repo it runs in, and that repo supplies the per-app settings."""
    global REPO, CONFIG
    REPO = (repo or repo_root(Path.cwd())).resolve()
    if config is None:
        for name in CONFIG_CANDIDATES:
            if (REPO / name).exists():
                config = REPO / name
                break
        else:
            legacy = SKILL_DIR / "policy-config.json"   # the skill folder copied into an app's .claude/skills
            if legacy.exists():
                config = legacy
    if config is None or not config.exists():
        sys.exit(f"No play-policy-config.json in {REPO}. Copy {SKILL_DIR / 'policy-config.example.json'} to "
                 f"{REPO / CONFIG_CANDIDATES[0]} and edit it for this app, or pass --config.")
    CONFIG = json.loads(config.expanduser().read_text(encoding="utf-8"))

URL = {
    "target_api": "https://developer.android.com/google/play/requirements/target-sdk",
    "billing": "https://developer.android.com/google/play/billing/deprecation-faq",
    "page_size": "https://developer.android.com/guide/practices/page-sizes",
    "permissions": "https://support.google.com/googleplay/android-developer/answer/9888170",
    "fgs": "https://support.google.com/googleplay/android-developer/answer/13392821",
    "user_data": "https://support.google.com/googleplay/android-developer/answer/10144311",
    "data_safety": "https://support.google.com/googleplay/android-developer/answer/10787469",
    "admob_disclosure": "https://developers.google.com/admob/android/privacy/play-data-disclosure",
    "admob_privacy": "https://support.google.com/admob/answer/2753860",
    "ad_id": "https://support.google.com/googleplay/android-developer/answer/6048248",
    "ads": "https://support.google.com/googleplay/android-developer/answer/9857753",
    "gambling": "https://support.google.com/googleplay/android-developer/answer/9877032",
    "ad_rating": "https://support.google.com/admob/answer/7562142",
    "ump": "https://developers.google.com/admob/android/privacy",
    "payments": "https://support.google.com/googleplay/android-developer/answer/9858738",
    "metadata": "https://support.google.com/googleplay/android-developer/answer/9898842",
    "misrepresentation": "https://support.google.com/googleplay/android-developer/answer/9888077",
    "ip": "https://support.google.com/googleplay/android-developer/answer/9888072",
    "graphics": "https://support.google.com/googleplay/android-developer/answer/9866151",
    "app_ads_txt": "https://support.google.com/admob/answer/9363762",
    "insights": "https://developer.android.com/studio/publish/insights",
}

# Permissions that need a Play Console declaration, prominent disclosure, or are
# near-certain rejections for a game. Anything here in a build is an ERROR.
RESTRICTED = {
    "android.permission.READ_SMS", "android.permission.SEND_SMS", "android.permission.RECEIVE_SMS",
    "android.permission.RECEIVE_MMS", "android.permission.RECEIVE_WAP_PUSH", "android.permission.READ_CALL_LOG",
    "android.permission.WRITE_CALL_LOG", "android.permission.PROCESS_OUTGOING_CALLS",
    "android.permission.MANAGE_EXTERNAL_STORAGE", "android.permission.QUERY_ALL_PACKAGES",
    "android.permission.REQUEST_INSTALL_PACKAGES", "android.permission.ACCESS_BACKGROUND_LOCATION",
    "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION",
    "android.permission.READ_MEDIA_IMAGES", "android.permission.READ_MEDIA_VIDEO",
    "android.permission.SCHEDULE_EXACT_ALARM", "android.permission.USE_EXACT_ALARM",
    "android.permission.USE_FULL_SCREEN_INTENT", "android.permission.BIND_ACCESSIBILITY_SERVICE",
    "android.permission.BIND_VPN_SERVICE", "android.permission.READ_CONTACTS", "android.permission.CAMERA",
    "android.permission.RECORD_AUDIO", "android.permission.READ_PHONE_STATE", "android.permission.READ_PHONE_NUMBERS",
    "android.permission.BODY_SENSORS", "android.permission.SYSTEM_ALERT_WINDOW",
    "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE",
}

findings: list[dict] = []
passed: list[str] = []
skipped: list[str] = []


def add(sev: str, check: str, title: str, detail: str, fix: str, url_key: str, where: str = "") -> None:
    findings.append({"severity": sev, "check": check, "title": title, "detail": detail, "fix": fix,
                     "policy": URL[url_key], "where": where})


def ok(label: str) -> None:
    passed.append(label)


def rel(p: Path) -> str:
    try:
        return str(p.relative_to(REPO))
    except ValueError:
        return str(p)


# --------------------------------------------------------------------------- helpers

def strip_comments(src: str) -> str:
    """Remove // and /* */ comments while leaving string literals (and the URLs
    inside them) intact."""
    out, i, n, quote = [], 0, len(src), None
    while i < n:
        c = src[i]
        if quote:
            out.append(c)
            if c == "\\" and i + 1 < n:
                out.append(src[i + 1]); i += 2; continue
            if c == quote:
                quote = None
            i += 1; continue
        if c in "\"'`":
            quote = c; out.append(c); i += 1; continue
        if src.startswith("//", i):
            j = src.find("\n", i); i = n if j < 0 else j; continue
        if src.startswith("/*", i):
            j = src.find("*/", i + 2); i = n if j < 0 else j + 2; continue
        out.append(c); i += 1
    return "".join(out)


def source_files() -> list[Path]:
    files = []
    for d in CONFIG["source_dirs"]:
        for p in sorted((REPO / d).rglob("*")):
            if p.suffix not in (".ts", ".tsx", ".js", ".jsx") or not p.is_file():
                continue
            s = str(p)
            if ".test." in p.name or "__tests__" in s or "__fixtures__" in s or "/node_modules/" in s:
                continue
            files.append(p)
    return files


def html_text(markup: str) -> str:
    markup = re.sub(r"(?is)<(script|style)\b.*?</\1>", " ", markup)
    return re.sub(r"\s+", " ", html.unescape(re.sub(r"<[^>]+>", " ", markup))).strip()


def listing_blocks() -> dict[str, str]:
    src = (REPO / CONFIG["listing"]).read_text(encoding="utf-8")
    blocks = {}
    for key, heading in (("title", "App name"), ("short", "Short description"), ("full", "Full description")):
        m = re.search(r"^## " + re.escape(heading) + r".*?\n```\n(.*?)\n```", src, re.S | re.M)
        if m:
            blocks[key] = m.group(1)
    return blocks


def find_terms(text: str, terms: list[str], ignore_case: bool = False) -> list[str]:
    hits = []
    for t in terms:
        flags = re.I if (ignore_case and not t.isupper()) else 0
        if re.search(r"(?<![\w-])" + re.escape(t) + r"(?![\w-])", text, flags):
            hits.append(t)
    return hits


def aapt2() -> str | None:
    roots = [os.environ.get("ANDROID_HOME"), os.environ.get("ANDROID_SDK_ROOT"),
             "/opt/homebrew/share/android-commandlinetools", str(Path.home() / "Library/Android/sdk")]
    for r in roots:
        if r:
            cands = sorted(glob.glob(os.path.join(r, "build-tools", "*", "aapt2")),
                           key=lambda p: [int(x) if x.isdigit() else 0 for x in re.split(r"[.-]", Path(p).parent.name)])
            if cands:
                return cands[-1]
    return shutil.which("aapt2")


def png_info(path: Path) -> tuple[int, int, int] | None:
    """(width, height, colour type) from a PNG header; None if not a PNG."""
    with open(path, "rb") as f:
        head = f.read(26)
    if head[:8] != b"\x89PNG\r\n\x1a\n":
        return None
    w, h = struct.unpack(">II", head[16:24])
    return w, h, head[25]


def elf_load_alignments(data: bytes) -> list[int] | None:
    if data[:4] != b"\x7fELF" or data[4] != 2:  # 64-bit only; 32-bit ABIs are exempt
        return None
    end = "<" if data[5] == 1 else ">"
    phoff = struct.unpack_from(end + "Q", data, 0x20)[0]
    phentsize, phnum = struct.unpack_from(end + "HH", data, 0x36)
    aligns = []
    for k in range(phnum):
        off = phoff + k * phentsize
        if struct.unpack_from(end + "I", data, off)[0] == 1:  # PT_LOAD
            aligns.append(struct.unpack_from(end + "Q", data, off + 0x30)[0])
    return aligns


def fetch(url: str) -> str | None:
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "play-policy-insights/1.0"})
        with urllib.request.urlopen(req, timeout=20) as r:
            return r.read().decode("utf-8", "replace")
    except Exception:
        return None


def uses_admob() -> bool:
    pkg = REPO / Path(CONFIG["expo_app_json"]).parent / "package.json"
    return pkg.exists() and "react-native-google-mobile-ads" in pkg.read_text(encoding="utf-8")


# --------------------------------------------------------------------------- build checks

def check_build(apk: Path | None, aab: Path | None) -> set[str]:
    perms: set[str] = set()
    if not apk or not apk.exists():
        skipped.append("build checks: no release APK found (pass --apk)")
        return perms
    tool = aapt2()
    if not tool:
        skipped.append("build checks: aapt2 not found (install Android build-tools)")
        return perms
    run = lambda *a: subprocess.run([tool, *a, str(apk)], capture_output=True, text=True).stdout
    badging = run("dump", "badging")
    pkg = re.search(r"package: name='([^']+)' versionCode='(\d+)'", badging)
    tsdk = re.search(r"targetSdkVersion:'(\d+)'", badging)
    where = f"{apk.name} (versionCode {pkg.group(2) if pkg else '?'})"
    if pkg and pkg.group(1) != CONFIG["package"]:
        add("ERROR", "PP-PACKAGE", "Built package name is not the registered one",
            f"Build is {pkg.group(1)}; Play knows {CONFIG['package']}.", "Fix android.package in app.json.",
            "misrepresentation", where)
    if tsdk and int(tsdk.group(1)) < CONFIG["min_target_sdk"]:
        add("ERROR", "PP-TARGET-SDK", f"targetSdk {tsdk.group(1)} is below Play's minimum",
            f"New apps and updates must target API {CONFIG['min_target_sdk']}.",
            "Upgrade the Expo SDK / compileSdk so targetSdkVersion meets the requirement.", "target_api", where)
    elif tsdk:
        ok(f"target API {tsdk.group(1)}")

    for line in run("dump", "permissions").splitlines():
        m = re.match(r"uses-permission: name='([^']+)'", line.strip())
        if m:
            perms.add(m.group(1))
    own = f"{CONFIG['package']}.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
    allowed = set(CONFIG["allowed_permissions"]) | {own}
    restricted = sorted(p for p in perms if p in RESTRICTED or p.startswith("android.permission.FOREGROUND_SERVICE_"))
    unexpected = sorted(p for p in perms - allowed if p not in restricted)
    for p in restricted:
        add("ERROR", "PP-PERM-RESTRICTED", f"Sensitive permission in the build: {p.split('.')[-1]}",
            f"{p} needs a Play Console declaration or prominent disclosure, or should not be there at all.",
            "Remove it with android.blockedPermissions in app.json (if no feature uses it), then prebuild.",
            "fgs" if "FOREGROUND_SERVICE_" in p else "permissions", where)
    for p in unexpected:
        add("WARNING", "PP-PERM-UNEXPECTED", f"Permission not on this app's allowlist: {p}",
            "Play only allows permissions a shipped feature needs; a new one usually arrived with a new library.",
            "Block it in app.json, or add it to allowed_permissions in play-policy-config.json with the reason.",
            "permissions", where)
    if not restricted and not unexpected:
        ok(f"permissions ({len(perms)}, all on the allowlist)")

    tree = run("dump", "xmltree", "--file", "AndroidManifest.xml")
    if re.search(r"foregroundServiceType", tree):
        add("ERROR", "PP-FGS-TYPE", "A service declares a foreground-service type",
            "Typed foreground services need the Foreground service declaration in Play Console (Android 14+).",
            "Remove the typed service, or complete the declaration with a video of the feature.", "fgs", where)
    else:
        ok("no typed foreground services")
    if re.search(r"debuggable\(0x[0-9a-f]+\)=\(type 0x12\)0xffffffff", tree):
        add("ERROR", "PP-DEBUGGABLE", "Release build is debuggable",
            "Play rejects debuggable builds.", "Build the release variant.", "misrepresentation", where)
    if "com.google.android.gms.permission.AD_ID" in perms:
        ds = (REPO / CONFIG["data_safety"]).read_text(encoding="utf-8") if (REPO / CONFIG["data_safety"]).exists() else ""
        if "Advertising ID" not in ds or "Device or other IDs" not in ds:
            add("ERROR", "PP-AD-ID", "Build uses the advertising ID but the declarations don't say so",
                "AD_ID is in the manifest; Play Console needs the Advertising ID form and a Device or other IDs row in Data safety.",
                "Update DATA_SAFETY.md, then Play Console › App content.", "ad_id", rel(REPO / CONFIG["data_safety"]))
        else:
            ok("advertising ID declared")

    if aab and aab.exists():
        bad, count = [], 0
        with zipfile.ZipFile(aab) as z:
            for name in z.namelist():
                if name.endswith(".so") and "/lib/arm64-v8a/" in "/" + name:
                    aligns = elf_load_alignments(z.read(name))
                    if aligns is None:
                        continue
                    count += 1
                    if any(a < 16384 for a in aligns):
                        bad.append(Path(name).name)
        if bad:
            add("ERROR", "PP-16KB", f"{len(bad)} native libraries are not 16 KB aligned",
                ", ".join(bad[:6]), "Upgrade the libraries (or the Expo SDK) that ship them.", "page_size", aab.name)
        elif count:
            ok(f"16 KB alignment ({count} arm64 libraries)")
    else:
        skipped.append("16 KB check: no AAB found (pass --aab)")
    return perms


# --------------------------------------------------------------------------- code checks

def check_code() -> None:
    files = source_files()
    stripped = {p: strip_comments(p.read_text(encoding="utf-8", errors="replace")) for p in files}
    everything = "\n".join(stripped.values())

    url = CONFIG["privacy_policy_url"]
    const_names = [m.group(1) for s in stripped.values()
                   for m in re.finditer(r"export const (\w+)\s*=\s*[\"']" + re.escape(url) + r"[\"']", s)]
    opened = any(re.search(r"openURL\(\s*(?:[\"']" + re.escape(url) + r"[\"']|" + "|".join(map(re.escape, const_names or ["\0"])) + r")", s)
                 for s in stripped.values())
    if opened:
        ok("in-app privacy policy link")
    else:
        add("ERROR", "PP-PRIVACY-LINK", "No privacy policy link inside the app",
            "Play requires the policy in Play Console AND a link or text within the app.",
            f"Add a Settings link that calls Linking.openURL(\"{url}\").", "user_data", CONFIG["source_dirs"][0])

    if uses_admob():
        rating = re.search(r"maxAdContentRating\s*:\s*[\w.]*?\.?(G|PG|T|MA)\b", everything)
        if not rating:
            add("ERROR", "PP-AD-RATING", "Ad content rating is not capped in code",
                "Ads must suit the app's content rating, and gambling ads are banned when under-18s are in the audience. "
                "AdMob allows some sensitive categories by default.",
                "Call mobileAds().setRequestConfiguration({ maxAdContentRating: MaxAdContentRating.PG }) before initialize().",
                "ads", "src/ads")
        elif rating.group(1) in ("T", "MA"):
            add("ERROR", "PP-AD-RATING", f"Ad content cap is {rating.group(1)}, above an Everyone app",
                "Ads above the app's rating break the Ads policy.", "Use MaxAdContentRating.PG (or G).", "ads", "src/ads")
        else:
            ok(f"ad content capped at {rating.group(1)}")
        if re.search(r"gatherConsent|requestInfoUpdate", everything):
            if "showPrivacyOptionsForm" in everything:
                ok("EU consent privacy-options entry point")
            else:
                add("ERROR", "PP-UMP-OPTIONS", "No way to change ad-consent choices",
                    "When UMP reports privacyOptionsRequirementStatus REQUIRED, the app must offer a control that "
                    "calls showPrivacyOptionsForm().", "Add a Settings row shown only when it's REQUIRED.", "ump", "src/ads")
        else:
            add("WARNING", "PP-UMP", "AdMob without a consent flow",
                "EEA/UK/CH traffic needs a Google-certified CMP (UMP) for full ads.", "Gather consent before initialize().",
                "ump", "src/ads")

    steer = [(t, p) for p, s in stripped.items() for t in find_terms(s, CONFIG["payment_steering_terms"], True)]
    if steer:
        for t, p in steer:
            add("ERROR", "PP-PAYMENT-STEERING", f"Payment wording outside Google Play Billing: \"{t}\"",
                "Digital goods (including ad removal) must be sold through Play Billing only; no links, buttons or copy "
                "pointing to UPI, the web or other payment methods.", "Remove it from player-visible code.", "payments", rel(p))
    else:
        ok("no payment steering in code")

    tm = [(t, p) for p, s in stripped.items() for t in find_terms(s, CONFIG["trademark_terms"])]
    for t, p in tm:
        add("ERROR", "PP-TRADEMARK", f"Trademarked name in the app: \"{t}\"",
            "Unlicensed use of real league, team or brand marks breaks the IP policy.", "Replace it with the fictional name.",
            "ip", rel(p))
    if not tm:
        ok("no trademarked names in code")

    people = [(t, p) for p, s in stripped.items() for t in find_terms(s, CONFIG["real_people"])]
    for t, p in people:
        add("ERROR", "PP-REAL-PERSON", f"A real person's name appears: \"{t}\"",
            "Real people's names or likenesses need a licence.", "Rename the display name; keep the internal id.",
            "ip", rel(p))
    if not people:
        ok(f"no names from the {len(CONFIG['real_people'])}-name real-people list")


# --------------------------------------------------------------------------- listing + graphics

def check_listing() -> None:
    lp = REPO / CONFIG["listing"]
    if not lp.exists():
        skipped.append("listing: no STORE_LISTING.md")
        return
    b = listing_blocks()
    title, short, full = b.get("title", ""), b.get("short", ""), b.get("full", "")
    where = rel(lp)
    for key, text, limit in (("Title", title, 30), ("Short description", short, 80), ("Full description", full, 4000)):
        if not text:
            add("WARNING", "PP-LISTING", f"{key} not found", "Couldn't read it from the listing doc.", "Check the headings.",
                "metadata", where)
        elif len(text) > limit:
            add("ERROR", "PP-LISTING-LENGTH", f"{key} is {len(text)} characters (limit {limit})", text[:80],
                "Shorten it.", "metadata", where)
    emoji = re.compile("[\U0001F000-\U0001FAFF☀-➿]")
    promo = re.compile(r"(?i)(#\s?1\b|\bno\.?\s?1\b|\bbest\b|\btop\b|\bfree\b|\bsale\b|\bdiscount|\b% off\b|\bnew\b|\bhot\b|\bdownload now\b|\binstall now\b)")
    for key, text in (("Title", title), ("Short description", short)):
        if emoji.search(text) or re.search(r"\b[A-Z]{4,}\b", text):
            add("ERROR", "PP-LISTING-STYLE", f"{key} uses emoji or ALL CAPS", text,
                "Remove emoji, ALL CAPS and repeated symbols.", "metadata", where)
        m = promo.search(text)
        if m:
            add("ERROR", "PP-LISTING-PROMO", f"{key} contains a promotional or ranking claim: \"{m.group(0)}\"", text,
                "Title and short description can't carry price, ranking or promo claims.", "metadata", where)
    claims = re.compile(r"(?i)(no internet|100% free|number one|#1|guarantee|official|best .{0,20}game|top[- ]rated)")
    for key, text in (("Title", title), ("Short description", short), ("Full description", full)):
        for m in claims.finditer(text):
            add("WARNING", "PP-LISTING-CLAIM", f"{key} claim to double-check: \"{m.group(0)}\"",
                "Listing claims must be accurate and verifiable (ads and purchases need a connection, for example).",
                "Reword or confirm it's literally true.", "misrepresentation", where)
    copy = "\n".join((title, short, full))
    for t in find_terms(copy, CONFIG["trademark_terms"]) + find_terms(copy, CONFIG["real_people"]):
        add("ERROR", "PP-TRADEMARK", f"Listing uses a real name or mark: \"{t}\"", "Trademarks and real people need a licence.",
            "Remove it from the listing.", "ip", where)
    for t in find_terms(copy, CONFIG["payment_steering_terms"], True):
        add("ERROR", "PP-PAYMENT-STEERING", f"Listing mentions another payment method: \"{t}\"",
            "The listing can't steer players to non-Play payments.", "Remove it.", "payments", where)
    if not any(f["check"].startswith("PP-LISTING") or (f["where"] == where) for f in findings):
        ok(f"listing (title {len(title)}, short {len(short)}, full {len(full)} characters)")

    site = REPO / CONFIG["site_dir"]
    site_text = "\n".join(html_text(p.read_text(encoding="utf-8", errors="replace")) for p in sorted(site.rglob("*.html"))) if site.exists() else ""
    for t in find_terms(site_text, CONFIG["trademark_terms"]) + find_terms(site_text, CONFIG["real_people"]):
        add("WARNING", "PP-TRADEMARK", f"Website uses a real name or mark: \"{t}\"", "The site is linked from the listing.",
            "Remove it from site/.", "ip", CONFIG["site_dir"])


def check_graphics() -> None:
    g = CONFIG["graphics"]
    icon = REPO / g["icon"]
    info = png_info(icon) if icon.exists() else None
    if not info:
        add("ERROR", "PP-ICON", "Play icon missing or not a PNG", g["icon"], "Export a 512 x 512 32-bit PNG.", "graphics", g["icon"])
    elif info[:2] != (512, 512) or info[2] != 6 or icon.stat().st_size > 1024 * 1024:
        add("ERROR", "PP-ICON", "Play icon doesn't meet the spec",
            f"{info[0]}x{info[1]}, colour type {info[2]}, {icon.stat().st_size // 1024} KB; needs 512x512 32-bit PNG (RGBA) up to 1 MB.",
            "Re-export it (docs/launch/make-app-icon.py writes a compliant one).", "graphics", g["icon"])
    else:
        ok("Play icon 512x512 RGBA")
    fg = REPO / g["feature_graphic"]
    info = png_info(fg) if fg.exists() else None
    if fg.exists() and info and (info[:2] != (1024, 500) or info[2] in (4, 6)):
        add("ERROR", "PP-FEATURE-GRAPHIC", "Feature graphic doesn't meet the spec",
            f"{info[0]}x{info[1]}, colour type {info[2]}; needs 1024x500, JPEG or 24-bit PNG with no alpha.",
            "Re-export without transparency.", "graphics", g["feature_graphic"])
    elif fg.exists():
        ok("feature graphic 1024x500")
    else:
        add("ERROR", "PP-FEATURE-GRAPHIC", "Feature graphic missing", g["feature_graphic"], "Add one.", "graphics", g["feature_graphic"])
    shots = sorted(p for p in (REPO / g["screenshots_dir"]).glob("*") if p.suffix.lower() in (".png", ".jpg", ".jpeg"))
    if not 2 <= len(shots) <= 8:
        add("ERROR", "PP-SCREENSHOTS", f"{len(shots)} phone screenshots (needs 2 to 8)", g["screenshots_dir"],
            "Add or remove screenshots.", "graphics", g["screenshots_dir"])
    bad = []
    for p in shots:
        info = png_info(p)
        if info:
            lo, hi = sorted(info[:2])
            if lo < 320 or hi > 3840 or hi > 2 * lo:
                bad.append(f"{p.name} {info[0]}x{info[1]}")
    if bad:
        add("ERROR", "PP-SCREENSHOTS", "Screenshot sizes out of spec", "; ".join(bad),
            "Each side 320 to 3840 px, and the long side at most twice the short side.", "graphics", g["screenshots_dir"])
    elif 2 <= len(shots) <= 8:
        ok(f"{len(shots)} phone screenshots in spec")


# --------------------------------------------------------------------------- data safety + privacy policy

ADMOB_TYPES = ["Approximate location", "App interactions", "Diagnostics", "Device or other IDs"]


def check_data(online: bool) -> None:
    ds_path = REPO / CONFIG["data_safety"]
    ds = ds_path.read_text(encoding="utf-8") if ds_path.exists() else ""
    admob = uses_admob()
    if admob:
        missing = [t for t in ADMOB_TYPES if t not in ds]
        if missing:
            add("ERROR", "PP-DATA-SAFETY", "Data safety is missing what the Ads SDK collects", ", ".join(missing),
                "Declare each as collected and shared for advertising, analytics and fraud prevention.",
                "admob_disclosure", rel(ds_path))
        else:
            ok("Data safety lists the 4 AdMob data types")

    src_path = REPO / CONFIG["privacy_policy_source"]
    if not src_path.exists():
        add("ERROR", "PP-PRIVACY", "Privacy policy page source not found", CONFIG["privacy_policy_source"],
            "Point privacy_policy_source at the page you publish.", "user_data")
        return
    text = html_text(src_path.read_text(encoding="utf-8"))
    low = text.lower()
    need = {"a contact email": bool(re.search(r"[\w.+-]+@[\w-]+\.[\w.]+", text)),
            "security (encryption in transit)": "encrypt" in low,
            "retention": "retain" in low or "retention" in low,
            "deletion": "delet" in low,
            "an effective date": "effective date" in low}
    if admob:
        need.update({"Google / AdMob as the recipient": "google" in low and "admob" in low,
                     "IP address": "ip address" in low, "advertising ID": "advertising id" in low,
                     "location": "location" in low, "ad interactions": "interaction" in low,
                     "diagnostics": "diagnostic" in low})
    missing = [k for k, v in need.items() if not v]
    if missing:
        add("ERROR", "PP-PRIVACY-CONTENT", "Privacy policy is missing required content", ", ".join(missing),
            "Play requires data types and recipients, secure handling, and retention and deletion. AdMob requires IP and identifier disclosure.",
            "user_data", rel(src_path))
    else:
        ok("privacy policy covers data types, security, retention, deletion")
    for sentence in re.split(r"(?<=[.!?])\s+", text):
        if re.search(r"(?i)\b(do not|don't|never)\b[^.]*\bcollect\b[^.]*\blocation\b", sentence) and "precise" not in sentence.lower():
            if "Approximate location" in ds:
                add("ERROR", "PP-PRIVACY-CONTRADICTION", "Privacy policy denies collecting location; Data safety declares it",
                    sentence[:160], "Say \"precise location\" if that's what's never collected.", "user_data", rel(src_path))

    if not online:
        skipped.append("live privacy page and app-ads.txt (run with --online)")
        return
    live = fetch(CONFIG["privacy_policy_url"])
    if live is None:
        add("ERROR", "PP-PRIVACY-LIVE", "Privacy policy URL doesn't load", CONFIG["privacy_policy_url"],
            "Deploy the site.", "user_data", CONFIG["privacy_policy_url"])
    elif html_text(live) != text:
        add("WARNING", "PP-PRIVACY-LIVE", "Live privacy page differs from the repo copy",
            "The published page is not what's in site/. Play reviews the live one.",
            "Redeploy site/ to Cloudflare (LAUNCH_PLAN A3).", "user_data", CONFIG["privacy_policy_url"])
    else:
        ok("live privacy page matches the repo")
    if admob:
        app_json = json.loads((REPO / CONFIG["expo_app_json"]).read_text(encoding="utf-8"))
        app_id = json.dumps(app_json)
        m = re.search(r"ca-app-pub-(\d+)~", app_id)
        txt = fetch(CONFIG["developer_website"].rstrip("/") + "/app-ads.txt")
        if m and txt is not None and f"pub-{m.group(1)}" in txt and "f08c47fec0942fa0" in txt:
            ok("app-ads.txt lists this publisher")
        else:
            add("ERROR", "PP-APP-ADS", "app-ads.txt missing or without this publisher",
                f"Expected google.com, pub-{m.group(1) if m else '?'}, DIRECT, f08c47fec0942fa0",
                "Publish app-ads.txt at the developer website root.", "app_ads_txt", CONFIG["developer_website"])


# --------------------------------------------------------------------------- gradle: Google's lint rules + SDK versions

def check_gradle() -> None:
    android = REPO / CONFIG["android_dir"]
    gradlew = android / "gradlew"
    if not gradlew.exists():
        skipped.append("gradle checks: no generated android/ project (run expo prebuild)")
        return
    env = dict(os.environ)
    try:
        env["JAVA_HOME"] = subprocess.run(["/usr/libexec/java_home", "-v17"], capture_output=True, text=True).stdout.strip() or env.get("JAVA_HOME", "")
    except FileNotFoundError:
        pass
    deps = subprocess.run([str(gradlew), ":app:dependencies", "--configuration", "releaseRuntimeClasspath", "-q"],
                          cwd=android, capture_output=True, text=True, env=env).stdout
    bv = re.search(r"com\.android\.billingclient:billing:(?:[\d.]+ -> )?([\d.]+)", deps)
    if bv:
        major = int(bv.group(1).split(".")[0])
        if major < CONFIG["min_billing_major"]:
            add("ERROR", "PP-BILLING-LIB", f"Play Billing Library {bv.group(1)} is too old",
                f"Play requires version {CONFIG['min_billing_major']}+.", "Upgrade react-native-iap.", "billing", "releaseRuntimeClasspath")
        else:
            ok(f"Play Billing Library {bv.group(1)}")
    else:
        skipped.append("billing library version: not found in releaseRuntimeClasspath")
    av = re.search(r"com\.google\.android\.gms:play-services-ads(?:-lite)?:(?:[\d.]+ -> )?([\d.]+)", deps)
    if av:
        ok(f"Google Mobile Ads SDK {av.group(1)}")

    init = SKILL_DIR / "scripts" / "insights-lint.init.gradle"
    with tempfile.TemporaryDirectory() as tmp:
        log = Path(tmp) / "lint.log"
        with open(log, "w") as fh:
            subprocess.run([str(gradlew), ":app:lintRelease", "--init-script", str(init),
                            f"-PplayPolicyInsightsVersion={CONFIG['insights_lint_version']}", "--continue"],
                           cwd=android, stdout=fh, stderr=subprocess.STDOUT, env=env)
        report = android / "app" / "build" / "reports" / "lint-results-release.xml"
        page = report.with_suffix(".html")
        if not report.exists():
            add("WARNING", "PP-LINT", "Google's Play Policy Insights lint didn't produce a report",
                log.read_text(errors="replace")[-600:], "Run ./gradlew :app:lintRelease by hand to see why.", "insights", rel(android))
            return
        xml = report.read_text(encoding="utf-8", errors="replace")
    # Google's rules are the lint issues whose ids end in "Policy" (AdvertisingIdPolicy,
    # AllFilesAccessPolicy, ForegroundServicesPolicy, ...). Count them from the HTML report
    # so a run where the rule set silently failed to load can't pass as "no findings".
    rules = []
    if page.exists():
        text = html_text(page.read_text(encoding="utf-8", errors="replace"))
        for m in re.finditer(r"\b([A-Z]\w+Policy)\b", text):
            vendor = re.search(r"Vendor:\s*([^.]{0,40})", text[m.end():m.end() + 8000])
            if vendor and "play policy" in vendor.group(1).lower() and m.group(1) not in rules:
                rules.append(m.group(1))  # vendor "Play policy insights beta"; skips e.g. Compose's SnapshotMutationPolicy
    if not rules:
        add("WARNING", "PP-LINT", "Google's Play Policy Insights rules did not load",
            "The lint report lists none of the *Policy checks.", "Check insights_lint_version and the init script.", "insights", rel(android))
        return
    count = 0
    for m in re.finditer(r"<issue\b(.*?)>(.*?)</issue>", xml, re.S):
        attrs = dict(re.findall(r'(\w+)="([^"]*)"', m.group(1)))
        issue_id = attrs.get("id", "")
        if not (issue_id in rules or "play policy" in attrs.get("category", "").lower()):
            continue
        count += 1
        loc = re.search(r'<location\s+file="([^"]+)"(?:\s+line="(\d+)")?', m.group(2))
        where = f"{rel(Path(loc.group(1)))}:{loc.group(2) or ''}" if loc else ""
        sev = "ERROR" if attrs.get("severity") in ("Error", "Fatal") else "WARNING"
        add(sev, f"LINT:{attrs.get('id', '?')}", html.unescape(attrs.get("summary", attrs.get("id", ""))),
            html.unescape(attrs.get("message", "")), html.unescape(attrs.get("explanation", ""))[:300], "insights", where)
    if count == 0:
        ok(f"Google Play Policy Insights lint {CONFIG['insights_lint_version']}: {len(rules)} rules, no findings (app module; library permissions are covered by the APK check)")


# --------------------------------------------------------------------------- report

def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    ap.add_argument("--repo", type=Path, help="the app repo to check (default: the git root of the current directory)")
    ap.add_argument("--config", type=Path, help="per-app settings (default: play-policy-config.json in the repo)")
    ap.add_argument("--apk", type=Path, help="release APK (default: the Gradle output under the configured android_dir)")
    ap.add_argument("--aab", type=Path, help="release AAB (default: the Gradle output under the configured android_dir)")
    ap.add_argument("--online", action="store_true", help="also check the live privacy page and app-ads.txt")
    ap.add_argument("--gradle", action="store_true", help="also run Google's insights-lint and read SDK versions (slow)")
    ap.add_argument("--json", action="store_true")
    a = ap.parse_args()
    configure(a.repo, a.config)
    outputs = REPO / CONFIG["android_dir"] / "app/build/outputs"
    apk = a.apk.expanduser() if a.apk else outputs / "apk/release/app-release.apk"
    aab = a.aab.expanduser() if a.aab else outputs / "bundle/release/app-release.aab"

    check_build(apk, aab)
    check_code()
    check_listing()
    check_graphics()
    check_data(a.online)
    if a.gradle:
        check_gradle()
    else:
        skipped.append("Google's insights-lint rules and SDK versions (run with --gradle)")

    errors = [f for f in findings if f["severity"] == "ERROR"]
    warnings = [f for f in findings if f["severity"] == "WARNING"]
    if a.json:
        print(json.dumps({"errors": len(errors), "warnings": len(warnings), "findings": findings,
                          "passed": passed, "skipped": skipped}, indent=2))
        return 1 if errors else 0

    tty = sys.stdout.isatty()
    col = (lambda c, s: f"\033[{c}m{s}\033[0m") if tty else (lambda c, s: s)
    print(col("1", f"Play Policy Insights: {CONFIG['app_name']} ({CONFIG['package']})"))
    print(f"{col('31', f'{len(errors)} error(s)')} · {col('33', f'{len(warnings)} warning(s)')} · {len(passed)} passed\n")
    for f in errors + warnings:
        mark = col("31", "ERROR  ") if f["severity"] == "ERROR" else col("33", "WARNING")
        print(f"{mark} {f['check']}  {f['title']}")
        if f["where"]:
            print(f"        at {f['where']}")
        if f["detail"]:
            print(f"        {f['detail']}")
        if f["fix"]:
            print(f"        fix: {f['fix']}")
        print(f"        policy: {f['policy']}\n")
    if passed:
        print(col("32", "Passed: ") + " · ".join(passed))
    if skipped:
        print("Not checked: " + " · ".join(skipped))
    return 1 if errors else 0


if __name__ == "__main__":
    sys.exit(main())
