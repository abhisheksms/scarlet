#!/usr/bin/env python3
"""Run the phase-one done-line checks on the emulator and write down what happened.

Usage:
    tools/verify_emulator.py [--serial emulator-5554] [--apk app/build/outputs/apk/debug/app-debug.apk]

It installs the debug build, wipes the app's data, simulates calls with `adb emu gsm call`
and checks, for each line of the done list:

  1. a non-contact is rejected in Block mode;
  2. a non-contact rings silently in Silence mode and appears in the system call log;
  3. a contact rings normally (and never reaches the app);
  4. the notification appears only when enabled;
  5. history and number details show correct times under 12-hour and 24-hour settings;
  6. a temporary allow lets the number ring until it expires (an expiring allow-list
     entry, and a pause).

Then four parity features beyond the done list:

  7. international-only scope lets a domestic non-contact ring and still blocks one from abroad;
  8. a repeat caller rings the second time;
  9. a milestone notification arrives at ten handled calls;
 10. the weekly report arrives once the week has ended.

The evidence goes to docs/verification/emulator-<date>.md. The exit code is 0 only if
every check passed.

Safety: it refuses to run against anything that is not an emulator. The founder's phone
is often on the same USB bus.

The app is driven by test tags (exposed as resource ids), never by its wording. Screening
decisions are read from the debug build's log line "decision=<ACTION> rule=<id>", which
never contains a phone number.
"""
import argparse
import datetime
import os
import re
import sqlite3
import subprocess
import sys
import tempfile
import time
import xml.etree.ElementTree as ET
from zoneinfo import ZoneInfo

PACKAGE = "com.cyanharborstudios.callblock"
ROLE = "android.app.role.CALL_SCREENING"
REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

CONTACT = "5559990001"
STRANGER_BLOCK = "5551110001"
STRANGER_SILENCE = "5551110002"
STRANGER_NOTIFY = "5551110003"
STRANGER_NO_NOTIFY = "5551110004"
STRANGER_PAUSE = "5551110005"

SERIAL = "emulator-5554"
results = []  # (name, passed, evidence lines)


# ---------- adb plumbing ----------

def adb(*args, timeout=60):
    out = subprocess.run(["adb", "-s", SERIAL, *args], capture_output=True, timeout=timeout)
    return out.stdout.decode("utf-8", "replace")


def shell(command):
    return adb("shell", command)


def require_emulator():
    if not SERIAL.startswith("emulator-") or shell("getprop ro.kernel.qemu").strip() != "1":
        sys.exit(f"refusing to run: {SERIAL} is not an emulator")


def focused_window():
    """The focused window and app, on one line: a system overlay can hold the focus while an ad is up."""
    lines = shell("dumpsys window").splitlines()
    return " ".join(line.strip() for line in lines if "mCurrentFocus" in line or "mFocusedApp" in line)


# ---------- the screen ----------

def nodes():
    for _ in range(4):
        shell("uiautomator dump /sdcard/verify.xml")
        xml = adb("exec-out", "cat", "/sdcard/verify.xml")
        if "<hierarchy" in xml:
            root = ET.fromstring(xml[xml.index("<"):])
            return list(root.iter("node"))
        time.sleep(1)
    return []


def centre(node):
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds")))
    return (x1 + x2) // 2, (y1 + y2) // 2


def find(tag):
    return [n for n in nodes() if n.get("resource-id") == tag]


def plain(text):
    """Screen text with its no-break spaces as plain spaces: a time is one word on screen."""
    return (text or "").replace("\u00a0", " ")


def texts():
    return [plain(n.get("text")) for n in nodes() if n.get("text")]


def spoken(tags):
    """What a screen reader says for each tagged control, by tag.

    Compose lists a tagged control as one node with the tag and one with the words, at the
    same bounds, so the words are taken from whichever node shares the tagged node's bounds.
    """
    all_nodes = nodes()
    words = {n.get("bounds"): plain(n.get("content-desc")) for n in all_nodes if n.get("content-desc")}
    return {n.get("resource-id"): words.get(n.get("bounds"), "") for n in all_nodes if n.get("resource-id") in tags}


def dismiss_full_screen_ad():
    """Leaving History or Statistics may show the (test) full-screen ad. Close it; never tap it.

    The first full-screen ad on a fresh emulator brings up Android's own "viewing full screen"
    notice, which holds the focus until its OK button is tapped; that button is the system's,
    not the ad's. The ad itself closes on Back once its first seconds have passed.
    """
    for _ in range(12):
        if "AdActivity" not in focused_window():
            return
        if "ImmersiveModeConfirmation" in focused_window():
            for node in nodes():
                if node.get("resource-id") == "com.android.systemui:id/ok":
                    x, y = centre(node)
                    shell(f"input tap {x} {y}")
                    time.sleep(1)
                    break
        time.sleep(1)
        shell("input keyevent KEYCODE_BACK")
        time.sleep(1)


def tap(tag, index=0, wait=8.0):
    deadline = time.time() + wait
    while time.time() < deadline:
        dismiss_full_screen_ad()
        found = find(tag)
        if len(found) > index:
            x, y = centre(found[index])
            shell(f"input tap {x} {y}")
            time.sleep(1.2)
            return
        time.sleep(0.7)
    raise RuntimeError(f"control not found on screen: {tag}")


def is_checked(tag):
    def checked(node):
        return node.get("checked") == "true" or any(checked(child) for child in node)
    return any(checked(n) for n in find(tag))


def back():
    shell("input keyevent KEYCODE_BACK")
    time.sleep(1.5)
    dismiss_full_screen_ad()


def open_app():
    shell(f"am start -n {PACKAGE}/.MainActivity")
    time.sleep(2.5)


def go_home():
    open_app()
    for _ in range(5):
        if find("mode-OFF"):
            return
        back()
    raise RuntimeError("could not reach the home screen")


# ---------- calls ----------

def ring(number, seconds=4.0):
    adb("emu", "gsm", "call", number)
    time.sleep(seconds)


def hang_up(number):
    """The caller hangs up. Waits until Telecom has no ringing call left, so the next step starts clean."""
    adb("emu", "gsm", "cancel", number)
    for attempt in range(10):
        time.sleep(1.0)
        if not telecom_events("state=RINGING"):
            break
        if attempt == 4:
            adb("emu", "gsm", "cancel", number)
    else:
        shell("input keyevent KEYCODE_ENDCALL")
    time.sleep(1.0)


def clear_log():
    adb("logcat", "-c")


def decisions():
    out = adb("logcat", "-d", "-s", "ScreeningService:I")
    return re.findall(r"decision=(\w+) rule=([\w-]+)", out)


def telecom_events(marker):
    """Telecom's own event lines for the most recent call that mention [marker]."""
    return [line.strip() for line in shell("dumpsys telecom").splitlines() if marker in line]


def system_call_log(number):
    out = shell("content query --uri content://call_log/calls --projection number:type:block_reason")
    return [line.strip() for line in out.splitlines() if f"number={number}," in line]


def seconds_of_day(stamp):
    hours, minutes, seconds = map(int, stamp.split(":"))
    return hours * 3600 + minutes * 60 + seconds


def within_a_minute_after(line, stamp):
    """True if a Telecom event line is stamped in the minute starting at HH:MM:SS [stamp]."""
    match = re.search(r"(\d\d:\d\d:\d\d)\.\d+ - ", line)
    return bool(match) and 0 <= seconds_of_day(match.group(1)) - seconds_of_day(stamp) <= 60


def ringer_started_after(stamp):
    return any(within_a_minute_after(line, stamp) for line in telecom_events("START_RINGER"))


def device_clock():
    return shell("date +%H:%M:%S").strip()


# ---------- the app's own data (debug build, so run-as works) ----------

def handled_calls():
    with tempfile.TemporaryDirectory() as folder:
        path = os.path.join(folder, "callblock.db")
        for suffix in ("", "-wal", "-shm"):
            data = subprocess.run(
                ["adb", "-s", SERIAL, "exec-out", "run-as", PACKAGE, "cat", f"databases/callblock.db{suffix}"],
                capture_output=True,
            ).stdout
            if data:
                with open(path + suffix, "wb") as f:
                    f.write(data)
        connection = sqlite3.connect(path)
        rows = connection.execute(
            "SELECT number_raw, at_millis, action, rule_id FROM handled_calls ORDER BY at_millis DESC"
        ).fetchall()
        connection.close()
        return rows


def notifications_from_app(channel="handled_calls"):
    """The app's posted notifications on one channel, as the notification manager lists them.

    Android 16 adds a group summary of its own over an app's silent notifications
    (tag "…g:Aggregate_SilentSection"); that is not one of ours and is left out.
    """
    out = shell("dumpsys notification --noredact")
    records = re.findall(r"NotificationRecord\([^\n]*pkg=" + re.escape(PACKAGE) + r"[^\n]*", out)
    return [r for r in records if f"channel={channel}" in r and "Aggregate_" not in r]


def notification_dump():
    return shell("dumpsys notification --noredact")


# ---------- recording ----------

def record(name, passed, *evidence):
    results.append((name, passed, [e for e in evidence if e]))
    print(("PASS  " if passed else "FAIL  ") + name)
    for line in evidence:
        if line:
            print("        " + line)


# ---------- setup ----------

def setup(apk):
    require_emulator()
    shell("svc power stayon true")
    shell("input keyevent KEYCODE_WAKEUP")
    shell("wm dismiss-keyguard")
    print(adb("install", "-r", apk).strip().splitlines()[-1])
    shell(f"pm clear {PACKAGE}")
    # The in-app role request was exercised by hand in the spike (ADR-002). Here the role is
    # set directly so the run is repeatable; re-adding it also re-grants notifications.
    shell(f"cmd role remove-role-holder {ROLE} {PACKAGE}")
    shell(f"cmd role add-role-holder {ROLE} {PACKAGE}")
    holder = shell(f"cmd role get-role-holders {ROLE}").strip()
    if holder != PACKAGE:
        sys.exit(f"could not make the app the call-screening app (holder: {holder!r})")
    if CONTACT not in shell("content query --uri content://com.android.contacts/data --projection data1"):
        shell("content insert --uri content://com.android.contacts/raw_contacts --bind account_type:n: --bind account_name:n:")
        raw_id = re.findall(r"_id=(\d+)", shell("content query --uri content://com.android.contacts/raw_contacts --projection _id"))[-1]
        shell(f"content insert --uri content://com.android.contacts/data --bind raw_contact_id:i:{raw_id} "
              f"--bind mimetype:s:vnd.android.cursor.item/phone_v2 --bind data1:s:{CONTACT} --bind data2:i:2")
    shell("settings put global auto_time 1")
    shell(f"cmd alarm set-time {int(time.time() * 1000)}")
    for number in (STRANGER_BLOCK, STRANGER_SILENCE, STRANGER_NOTIFY, STRANGER_NO_NOTIFY, STRANGER_PAUSE, CONTACT):
        adb("emu", "gsm", "cancel", number)
    go_home()


# ---------- the checks ----------

def check_block():
    tap("mode-BLOCK")
    clear_log()
    ring(STRANGER_BLOCK)
    seen = decisions()
    still_ringing = [line for line in telecom_events("state=RINGING")]
    log = system_call_log(STRANGER_BLOCK)
    passed = seen == [("BLOCK", "unknown-caller")] and not still_ringing and any("type=6" in row for row in log)
    record(
        "1. A non-contact is rejected in Block mode",
        passed,
        f"app: decision={seen}",
        f"telecom: calls still ringing = {len(still_ringing)}",
        f"system call log: {log[-1] if log else 'no row'}  (type 6 = blocked, block_reason 1 = call screening service)",
    )
    hang_up(STRANGER_BLOCK)


def check_silence():
    tap("mode-SILENCE")
    clear_log()
    started = device_clock()
    ring(STRANGER_SILENCE)
    seen = decisions()
    ringing = bool(telecom_events("state=RINGING"))
    skipped = [line for line in telecom_events("SKIP_RINGING") if within_a_minute_after(line, started)]
    rang = ringer_started_after(started)
    hang_up(STRANGER_SILENCE)
    log = system_call_log(STRANGER_SILENCE)
    passed = (
        seen == [("SILENCE", "unknown-caller")] and ringing and bool(skipped) and not rang
        and any("type=3" in row for row in log)
    )
    record(
        "2. A non-contact rings silently in Silence mode and appears in the system call log",
        passed,
        f"app: decision={seen}",
        f"telecom: call was in state RINGING = {ringing}; ringer started = {rang}",
        f"telecom: {skipped[-1][:90] if skipped else 'no SKIP_RINGING event'}",
        f"system call log after the caller hung up: {log[-1] if log else 'no row'}  (type 3 = missed)",
    )


def check_contact():
    clear_log()
    started = device_clock()
    ring(CONTACT)
    seen = decisions()
    rang = ringer_started_after(started)
    contact_exists = [line for line in telecom_events("contact exists") if within_a_minute_after(line, started)]
    hang_up(CONTACT)
    passed = seen == [] and rang and bool(contact_exists)
    record(
        "3. A contact rings normally",
        passed,
        f"app: the screening service was not invoked (decisions logged = {seen})",
        f"telecom: ringer started = {rang}",
        f"telecom: {contact_exists[-1][:95] if contact_exists else 'no contact-exists event'}",
    )


def check_notifications():
    # Fresh install: the switch is off. Checks 1 and 2 handled two calls already.
    before = len(notifications_from_app())
    switch_off = not is_checked("notifications")

    tap("notifications")
    switch_on = is_checked("notifications")
    ring(STRANGER_NOTIFY)
    hang_up(STRANGER_NOTIFY)
    after_enabled = len(notifications_from_app())

    go_home()
    tap("notifications")
    switch_off_again = not is_checked("notifications")
    ring(STRANGER_NO_NOTIFY)
    hang_up(STRANGER_NO_NOTIFY)
    after_disabled = len(notifications_from_app())

    passed = (
        switch_off and before == 0 and switch_on and after_enabled == 1
        and switch_off_again and after_disabled == 1
    )
    record(
        "4. The notification appears only when enabled",
        passed,
        f"switch off (as installed): two calls handled, notifications posted = {before}",
        f"switch on: one more call handled, notifications posted = {after_enabled}",
        f"switch off again: one more call handled, notifications still = {after_disabled}",
    )
    go_home()


def expected_times(rows, zone, twenty_four_hour):
    out = []
    for _, at_millis, _, _ in rows:
        moment = datetime.datetime.fromtimestamp(at_millis / 1000, ZoneInfo(zone))
        if twenty_four_hour:
            out.append(moment.strftime("%H:%M"))
        else:
            out.append(moment.strftime("%I:%M %p").lstrip("0"))
    return out


def check_clock_formats():
    zone = shell("getprop persist.sys.timezone").strip()
    rows = handled_calls()
    evidence = [f"stored: {len(rows)} handled calls; time zone {zone}"]
    passed = len(rows) == 4

    for setting, twenty_four_hour in (("24", True), ("12", False)):
        shell(f"settings put system time_12_24 {setting}")
        shell("input keyevent KEYCODE_HOME")
        time.sleep(1)
        go_home()
        tap("open-history")
        shown = texts()
        want = expected_times(rows, zone, twenty_four_hour)
        history_ok = all(any(t.endswith(w) for t in shown) for w in want)
        wrong_form = [t for t in shown if re.search(r"\d:\d\d", t) and (bool(re.search(r"[AP]M", t)) == twenty_four_hour)]

        tap("history-row", index=0)
        # The sheet names this call with its day, its time and its outcome, and a number called
        # more than once also shows its first and last time. Every one must be in the phone's
        # form, and this call's must carry the time of the newest stored call, which is this row's.
        facts = spoken(("this-call", "first-handled", "last-handled"))
        details = [d for d in facts.values() if d and re.search(r"\d:\d\d", d)]
        right_form = all(bool(re.search(r"[AP]M", d)) != twenty_four_hour for d in details)
        this_call = facts.get("this-call", "")
        details_ok = bool(details) and right_form and want[0] in this_call
        back()   # closes the sheet
        back()   # leaves History

        ok = history_ok and not wrong_form and details_ok
        passed = passed and ok
        evidence.append(
            f"{setting}-hour: history shows {sorted(set(want))}: {history_ok}; "
            f"entries in the other form: {len(wrong_form)}; details sheet: {details}"
        )
        go_home()

    record("5. History and number details show correct times under 12-hour and 24-hour settings", passed, *evidence)


def check_temporary_allow():
    tap("mode-BLOCK")

    # (a) an allow-list entry that expires in one hour, made from the number's history row
    tap("open-history")
    rows = handled_calls()
    position = [r[0] for r in rows].index(STRANGER_BLOCK)
    tap("history-row", index=position)
    tap("allow-for-60")   # the sheet closes itself
    back()
    go_home()

    clear_log()
    started = device_clock()
    ring(STRANGER_BLOCK)
    during = decisions()
    rang = ringer_started_after(started)
    hang_up(STRANGER_BLOCK)

    shell("settings put global auto_time 0")
    shell(f"cmd alarm set-time {int((time.time() + 61 * 60) * 1000)}")
    time.sleep(1)
    clear_log()
    ring(STRANGER_BLOCK)
    after = decisions()
    hang_up(STRANGER_BLOCK)

    allow_ok = during == [("ALLOW", "allow-list")] and rang and after == [("BLOCK", "unknown-caller")]
    evidence = [
        f"allowed for 1 hour, call at once: decision={during}, ringer started = {rang}",
        f"clock moved forward 61 minutes, same number: decision={after}",
    ]

    # (b) a 15-minute pause, one tap from Home
    go_home()
    tap("pause-15")
    clear_log()
    ring(STRANGER_PAUSE)
    paused = decisions()
    hang_up(STRANGER_PAUSE)
    shell(f"cmd alarm set-time {int((time.time() + 61 * 60 + 16 * 60) * 1000)}")
    time.sleep(1)
    clear_log()
    ring(STRANGER_PAUSE)
    resumed = decisions()
    hang_up(STRANGER_PAUSE)

    pause_ok = paused == [("ALLOW", "paused")] and resumed == [("BLOCK", "unknown-caller")]
    evidence += [
        f"paused for 15 minutes, a non-contact calls: decision={paused}",
        f"clock moved forward 16 minutes more, same number: decision={resumed}",
    ]

    shell("settings put global auto_time 1")
    shell(f"cmd alarm set-time {int(time.time() * 1000)}")
    # With the real time back, the pause set "an hour ahead" would count again. End it.
    go_home()
    if find("resume"):
        tap("resume")
    record("6. A temporary allow lets the number ring until it expires", allow_ok and pause_ok, *evidence)


def check_scope():
    go_home()
    tap("mode-BLOCK")
    tap("open-options")
    tap("scope-international")
    back()
    # The emulator's SIM is a US one, so a US number is domestic and an Indian one is from abroad.
    clear_log()
    ring("6505550199")
    domestic = decisions()
    hang_up("6505550199")
    clear_log()
    ring("+919876543210")
    abroad = decisions()
    hang_up("+919876543210")
    go_home()
    tap("open-options")
    tap("scope-all")
    back()
    record(
        "7. International-only scope lets a domestic non-contact ring and still blocks one from abroad",
        domestic == [("ALLOW", "domestic-out-of-scope")] and abroad == [("BLOCK", "unknown-caller")],
        f"home country {shell('getprop gsm.sim.operator.iso-country').strip()}; a domestic number: decision={domestic}",
        f"a number from another country: decision={abroad}",
    )


def check_repeat_caller():
    go_home()
    tap("open-options")
    tap("repeat-15")
    back()
    number = "5551110006"
    clear_log()
    ring(number)
    first = decisions()
    hang_up(number)
    clear_log()
    started = device_clock()
    ring(number)
    second = decisions()
    rang = ringer_started_after(started)
    hang_up(number)
    go_home()
    tap("open-options")
    tap("repeat-0")
    back()
    record(
        "8. A repeat caller rings the second time",
        first == [("BLOCK", "unknown-caller")] and second == [("ALLOW", "repeat-call")] and rang,
        f"first call: decision={first}",
        f"same number again within the window: decision={second}, ringer started = {rang}",
    )


def check_milestone():
    go_home()
    tap("mode-BLOCK")
    before = len(notifications_from_app("milestones"))
    handled = len(handled_calls())
    for attempt in range(12):
        if handled >= 10:
            break
        number = f"555111{100 + attempt:04d}"
        ring(number, seconds=3.0)
        hang_up(number)
        handled = len(handled_calls())
    after = len(notifications_from_app("milestones"))
    record(
        "9. A milestone notification arrives at ten handled calls",
        before == 0 and handled == 10 and after == 1,
        f"milestone notifications before the tenth call: {before}",
        f"handled calls now: {handled}; milestone notifications: {after}",
    )


def check_weekly_report():
    go_home()
    tap("open-settings")
    tap("report-WEEKLY")
    back()
    time.sleep(3)  # the job's first run, which must find nothing due
    early = len(notifications_from_app("reports"))

    rows = handled_calls()
    blocked = sum(1 for r in rows if r[2] == "BLOCK")
    silenced = sum(1 for r in rows if r[2] == "SILENCE")

    # Jump to 10:00 next Monday: this week has then ended, so its report is due. The daily
    # check is periodic work, and WorkManager runs it only once a full day has passed since it
    # was scheduled, even when forced; from a Sunday morning Monday 10:00 is too soon, so the
    # jump goes one day further, to the Tuesday, where the same week is still the one due.
    zone = ZoneInfo(shell("getprop persist.sys.timezone").strip())
    now = datetime.datetime.now(zone)
    monday = (now + datetime.timedelta(days=7 - now.weekday())).replace(hour=10, minute=0, second=0, microsecond=0)
    if monday - now < datetime.timedelta(hours=25):
        monday += datetime.timedelta(days=1)
    shell("settings put global auto_time 0")
    shell(f"cmd alarm set-time {int(monday.timestamp() * 1000)}")
    time.sleep(1)
    # "JOB #u0a123/4: … pkg/androidx.work…" on older Android; on Android 16 the job sits in
    # WorkManager's own namespace: "JOB androidx.work.systemjobscheduler:u0a123/0: … @ns@pkg/androidx.work…".
    jobs = re.findall(
        r"JOB (?:#|(?P<ns>[\w.]+):)u0a\d+/(?P<id>\d+): \w+ (?:@[\w.]+@)?" + re.escape(PACKAGE) + r"/androidx\.work",
        shell("dumpsys jobscheduler"),
    )
    job_ids = sorted(set(jobs))
    for namespace, job_id in job_ids:
        shell(f"cmd jobscheduler run -f {'-n ' + namespace + ' ' if namespace else ''}{PACKAGE} {job_id}")
    time.sleep(6)
    posted = notifications_from_app("reports")
    expected_text = f"{blocked} blocked, {silenced} silenced"
    text_shown = expected_text in notification_dump()

    shell("settings put global auto_time 1")
    shell(f"cmd alarm set-time {int(time.time() * 1000)}")
    record(
        "10. The weekly report arrives once the week has ended",
        early == 0 and len(job_ids) >= 1 and len(posted) == 1 and text_shown,
        f"weekly switched on mid-week: report notifications = {early} (nothing is due yet)",
        f"clock moved to {monday:%A %d %b, %H:%M}; the scheduled job was run: report notifications = {len(posted)}",
        f"the notification reads \"{expected_text}\": {text_shown}  (the log holds {blocked} blocked, {silenced} silenced this week)",
    )


# ---------- report ----------

def write_report(apk):
    today = datetime.date.today().isoformat()
    folder = os.path.join(REPO, "docs", "verification")
    os.makedirs(folder, exist_ok=True)
    path = os.path.join(folder, f"emulator-{today}.md")
    image = shell("getprop ro.build.fingerprint").strip()
    commit = subprocess.run(["git", "-C", REPO, "rev-parse", "--short", "HEAD"], capture_output=True).stdout.decode().strip()
    with open(path, "w") as f:
        f.write(f"# Emulator verification, {today}\n\n")
        f.write("Written by `tools/verify_emulator.py`. Calls are simulated with `adb emu gsm call`.\n")
        f.write("The app's decision is read from the debug build's log; what Android then did is read\n")
        f.write("from Telecom's own event log, the system call log and the notification manager.\n\n")
        f.write(f"- Device: `{SERIAL}`, `{image}`\n")
        f.write(f"- Build: `{os.path.relpath(apk, REPO)}` at commit `{commit}`\n")
        f.write(f"- Result: **{sum(1 for _, ok, _ in results if ok)} of {len(results)} checks passed**\n\n")
        for name, ok, evidence in results:
            f.write(f"## {name}\n\n**{'PASS' if ok else 'FAIL'}**\n\n")
            for line in evidence:
                f.write(f"- {line}\n")
            f.write("\n")
    print(f"\nwrote {os.path.relpath(path, REPO)}")


def main():
    global SERIAL
    parser = argparse.ArgumentParser()
    parser.add_argument("--serial", default="emulator-5554")
    parser.add_argument("--apk", default=os.path.join(REPO, "app", "build", "outputs", "apk", "debug", "app-debug.apk"))
    args = parser.parse_args()
    SERIAL = args.serial

    setup(args.apk)
    try:
        check_block()
        check_silence()
        check_contact()
        check_notifications()
        check_clock_formats()
        check_temporary_allow()
        check_scope()
        check_repeat_caller()
        check_milestone()
        check_weekly_report()
    finally:
        # Whatever happened, give the emulator its real clock back.
        shell("settings put global auto_time 1")
        shell(f"cmd alarm set-time {int(time.time() * 1000)}")
    write_report(args.apk)
    return 0 if all(ok for _, ok, _ in results) else 1


if __name__ == "__main__":
    sys.exit(main())
