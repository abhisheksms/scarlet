---
name: play-policy-guard
description: Google Play policy guard for Cyan Harbor's Android apps. Android-Studio-style Play Policy Insights for an Expo/React Native repo, run by one script against a per-app config, plus the Play and AdMob rules that apply to an ad-supported offline app with a one-time purchase. Use it BEFORE every Play upload, when setting up a new app's listing, and whenever a change touches anything Play reviews - ads or AdMob settings, the purchase or any payment wording, permissions or app.json android config, adding or upgrading a native SDK or dependency, analytics or crash reporting, data collection, the privacy policy or website, Data safety / content rating / target audience answers, store listing text or graphics, names and other real-world references, or the target API. Also use it when the founder asks "are we compliant", "will Play reject this" or "policy check", or forwards a Play Console rejection, warning or policy email.
---

# Play policy guard

Cyan Harbor publishes from an organization account that took weeks to verify. A
rejection costs days; a policy strike costs far more. The first app's pre-launch
audit (2026-09-24) found one blocker (a privacy policy that contradicted the Data
safety form, with no in-app link) and three launch risks (unused permissions,
uncapped ad content, no way to change EU ad consent). This skill exists so every
app ships without them, and so the next one is caught before Google catches it.

## 0. Setting up a new app (once)

1. Copy `policy-config.example.json` (this folder) to the app repo's root as
   `play-policy-config.json` and edit every value: app name, package, paths to
   `app.json`, the generated `android/` dir, source dirs, the listing and Data
   safety files, the site dir and privacy page, graphics, the permission
   allowlist, and the trademark, real-people and payment-steering lists for
   that app's domain. The example is Cricket Auction Simulator's real config.
2. Keep the app's Play Console answers and audit log in the app repo at
   `docs/launch/APP_RECORD.md`, started from `references/app-record.template.md`.
3. Keep `docs/launch/STORE_LISTING.md`, `docs/launch/DATA_SAFETY.md` and the
   graphics at the paths the config names. The `play-launch` skill has the
   templates.

## 1. Run the insights first

From anywhere inside the app repo (the checker finds the git root and reads
`play-policy-config.json` there; pass `--repo` and `--config` otherwise). The
script sits next to this file, at `scripts/play_policy_insights.py`; when the
skill is loaded from the plugin, that is
`${CLAUDE_PLUGIN_ROOT}/skills/play-policy-guard/scripts/play_policy_insights.py`.

```bash
# every change that touches the areas in the description (about 5 seconds)
python3 <skill>/scripts/play_policy_insights.py

# before ANY Play upload: point at the exact files you'll upload (2 to 5 minutes)
python3 <skill>/scripts/play_policy_insights.py \
  --apk ~/CyanHarbor/builds/<date>-build<N>/<app>-v<ver>-build<N>.apk \
  --aab ~/CyanHarbor/builds/<date>-build<N>/<app>-v<ver>-build<N>.aab \
  --online --gradle
```

It prints Android Studio-style findings: severity, check id, where, why, the fix
and the policy link. Exit 1 means at least one ERROR. Don't upload, and don't tell
the founder it's ready, until the errors are gone. A WARNING is a judgment call:
fix it, or write down why it's fine in the app's `APP_RECORD.md`.

What it covers:

| Area | Checks |
|---|---|
| Built app (APK/AAB) | package name, target API ≥ `min_target_sdk`, restricted or unexpected permissions against the allowlist, typed foreground services, debuggable flag, AD_ID declared, 16 KB alignment |
| Code | in-app privacy-policy link, ad content capped at PG, UMP privacy-options entry point, no payment steering (UPI, web…), no trademarked names, no names from the real-people list |
| Listing | title 30 / short 80 / full 4,000 characters, no emoji, caps or promo words in title or short, claims worth double-checking, trademarks |
| Graphics | 512 RGBA icon, 1024×500 no-alpha feature graphic, 2–8 screenshots within 2:1 |
| Data | Data safety lists what the Ads SDK collects; the privacy page covers data types, recipients, security, retention, deletion, and doesn't contradict Data safety |
| `--online` | the live privacy page equals the repo copy (catches "fixed but not deployed"); app-ads.txt lists the publisher |
| `--gradle` | **Google's own Play Policy Insights lint rules** (`com.google.play.policy.insights:insights-lint`; 13 rules: advertising ID, all-files access, background location, exact alarms, foreground services, full-screen intents, accessibility, VPN, SMS/call log, photo and video, package visibility, install packages, Health Connect) injected by `scripts/insights-lint.init.gradle` for one run, with no build-file edits, plus the resolved Billing Library and Mobile Ads SDK versions |

How `--gradle` runs Google's rules: lint analyses the **app module only**. AGP
schedules analysis for every React Native library module even with
`checkDependencies` off, and lint crashes on react-native-worklets' Kotlin build
script, so the init script disables those library tasks for the run. Permissions
the libraries add are still covered: the checker reads the merged manifest from
the built APK. Google's `LoginCredentials` rule is disabled because it trips
lint's own API guard. The report counts only rules whose vendor is Play Policy
Insights, so if the rule set fails to load, that's a warning, not a pass.

`--gradle` uses Gradle, so never run it while a release build or the emulator is
running.

## 2. Judge what a script can't

The checks are heuristics. For anything new, read `references/policy-digest.md`
and ask the questions Play's reviewers ask:

- **New SDK or dependency:** what does it collect and send? Update
  `docs/launch/DATA_SAFETY.md`, the privacy page on the studio site and Play
  Console's Data safety **together**. They must say the same thing. Firebase or
  any analytics means new rows. New permissions it brings get blocked in
  `app.json` unless a shipped feature needs them.
- **New copy or offers:** no fake urgency, no countdowns, no steering to non-Play
  payments, and prices come from Google Play. Anything digital is Play Billing
  only.
- **New names, places or likenesses:** fictional only. The lists catch the famous
  ones, not the obscure ones; the first audit found a 1967 Test cricketer the
  list would have missed. Spot-check new names.
- **Ad changes:** opt-in rewarded only; state the reward before the ad; never
  during play; the reward always arrives; the PG cap stays. Any other format
  (interstitial, banner, app-open) means re-reading the Ads policy first.
- **Audience or content changes:** re-answer the IARC questionnaire. Keeping
  under-13s out of the target audience keeps the Families policy out, but
  child-appealing art or wording can still get the app reclassified.

## 3. Red lines

- Digital goods only through Google Play Billing. No UPI, web or "pay us directly"
  wording anywhere a user can see it.
- No permission without a shipped feature that needs it.
- Data safety and the privacy policy never disagree. Change them in the same PR.
- Never remove the in-app **Privacy Policy** link or the **Privacy Choices** entry
  point.
- No trademarked league, team, brand or product names, and no real people's names
  or likenesses, without documented rights.
- Listing claims must be literally true ("no internet" was not: ads and the
  purchase need a connection).
- Never tap a live ad on a real device.

## 4. Things only the founder can check

Their accounts, their clicks: give numbered steps and read their screenshots.
Never drive Play Console or AdMob yourself. The standing list is in the app's
`APP_RECORD.md` under "Founder account checks": Play Console developer
verification, developer website, EU trader details, App content complete,
pre-launch report; AdMob app linked, max ad content rating PG, blocked sensitive
categories, the GDPR message (publish only once a build with the Privacy Choices
entry point is in production), test device.

## 5. Keep it current

- Google changes dates every year: the target API on 31 August, and Billing Library
  deprecations. When one moves, update `references/policy-digest.md` here and
  every app's `play-policy-config.json` (`min_target_sdk`, `min_billing_major`).
  Check for a newer `insights_lint_version` at
  https://dl.google.com/dl/android/maven2/com/google/play/policy/insights/insights-lint/maven-metadata.xml.
- After any Play or AdMob rejection, warning or policy email: record it in the
  app's `APP_RECORD.md`, and if a script could have caught it, add a check here
  so every app gets it.
- For a full re-audit (a new app, a new SDK, a new monetization model), spawn a
  **read-only** agent with the digest, the app record and the structure in the
  template's "Re-audit" section. Relay its findings to the founder in plain
  English.

## Files

- `scripts/play_policy_insights.py`: the checker (standard library, plus aapt2 for build checks).
- `scripts/insights-lint.init.gradle`: injects Google's insights-lint for one Gradle run.
- `policy-config.example.json`: the per-app settings, filled in for Cricket Auction
  Simulator. Each app keeps its own copy as `play-policy-config.json` at its root.
- `references/policy-digest.md`: the rules that matter for this kind of app, with links.
- `references/app-record.template.md`: the per-app compliance record to start from.
