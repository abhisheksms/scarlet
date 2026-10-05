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
first_launch_showed_tutorial = None  # set by setup(): what the wiped app opened on


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


def unlock():
    """Wake the emulator and leave its lock screen.

    One `wm dismiss-keyguard` can miss: after a PIN is cleared Android keeps its swipe lock
    up for a moment, and the app then starts behind it. So ask, look, and ask again.
    """
    for _ in range(8):
        shell("input keyevent KEYCODE_WAKEUP")
        shell("wm dismiss-keyguard")
        time.sleep(1.0)
        if "isKeyguardShowing=true" not in shell("dumpsys window"):
            return
        shell("input keyevent KEYCODE_MENU")
        time.sleep(1.0)
    raise RuntimeError("the emulator's lock screen would not go away")


def open_app():
    shell(f"am start -n {PACKAGE}/.MainActivity")
    time.sleep(2.5)


def scroll_down():
    """Drag the page up by about half a screen, to bring what is below the fold into view."""
    width, height = map(int, re.findall(r"(\d+)x(\d+)", shell("wm size"))[-1])
    shell(f"input swipe {width // 2} {int(height * 0.7)} {width // 2} {int(height * 0.2)} 300")
    time.sleep(1.0)


def scroll_to_top():
    width, height = map(int, re.findall(r"(\d+)x(\d+)", shell("wm size"))[-1])
    for _ in range(2):
        shell(f"input swipe {width // 2} {int(height * 0.3)} {width // 2} {int(height * 0.85)} 200")
    time.sleep(1.0)


def tap_below(tag):
    """Tap a control that may sit below the fold: scroll down once if its middle is not in reach, and tap.

    A row cut off by the bottom of the screen is still listed, with its full size. A tap at
    its middle would then land on the ad tray, or in Android's gesture area, not on the row.
    """
    _, height = map(int, re.findall(r"(\d+)x(\d+)", shell("wm size"))[-1])
    listed = nodes()
    tray = [n for n in listed if n.get("resource-id") == "banner-slot"]
    floor = int(re.findall(r"\d+", tray[0].get("bounds"))[1]) if tray else int(height * 0.94)
    found = [n for n in listed if n.get("resource-id") == tag]
    in_reach = bool(found) and centre(found[0])[1] < floor - 12
    if not in_reach:
        scroll_down()
    tap(tag)


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

def database_rows(query):
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
        rows = connection.execute(query).fetchall()
        connection.close()
        return rows


def handled_calls():
    return database_rows("SELECT number_raw, at_millis, action, rule_id FROM handled_calls ORDER BY at_millis DESC")


def allowed_numbers():
    """The allow list as stored: (number key, expiry millis or None)."""
    return database_rows("SELECT number_key, expires_at_millis FROM allowed_numbers")


def key_of(number):
    """The E.164 key the app gives a ten-digit number dialled on the emulator's US SIM."""
    return "+1" + number if len(number) == 10 else number


def action_label():
    """The one notification action's label, read from the app's own strings file, so the
    script never carries copy of its own. A notification action has no resource id to find
    it by; its text is the only handle Android's shade gives."""
    import xml.etree.ElementTree as ElementTree
    root = ElementTree.parse(os.path.join(REPO, "app", "src", "main", "res", "values", "strings.xml")).getroot()
    return next(e.text for e in root.iter("string") if e.get("name") == "notification_allow_hour")


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
    unlock()
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
    # The wiped app's first launch opens on How It Works; go_home() then closes it with Back.
    global first_launch_showed_tutorial
    open_app()
    first_launch_showed_tutorial = bool(find("how-screen"))
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


def check_india_series():
    # India's two commercial number series. The 160 rule is always on; the 140 rule is the
    # user's own switch in Options, off as installed. The emulator's SIM is a US one, so both
    # numbers are dialled with +91; on an Indian phone the same series also arrive as ten digits.
    go_home()
    tap("mode-BLOCK")
    service, promotional = "+911600123456", "+911401234567"
    clear_log()
    started = device_clock()
    ring(service)
    service_seen = decisions()
    rang = ringer_started_after(started)
    hang_up(service)
    # In Silence mode a 140 call shows whether the 140 rule is in force: blocked by it, or
    # silenced like any other unknown caller.
    tap("mode-SILENCE")
    clear_log()
    ring(promotional)
    before_switch = decisions()
    hang_up(promotional)
    tap("open-options")
    tap("india-140")
    back()
    clear_log()
    ring(promotional)
    after_switch = decisions()
    hang_up(promotional)
    time.sleep(1.0)
    logged = system_call_log(promotional)
    go_home()
    tap("open-options")
    tap("india-140")
    back()
    tap("mode-BLOCK")
    record(
        "11. India's 160 series rings; the 140 series is blocked only once the user asks",
        service_seen == [("ALLOW", "in-160-service")] and rang
        and before_switch == [("SILENCE", "unknown-caller")]
        and after_switch == [("BLOCK", "in-140-promotional")],
        f"a 1600 number (a bank, an insurer or a government body), lever at Block: decision={service_seen}, ringer started = {rang}",
        f"a 140 number (a registered telemarketer), lever at Silence, the switch off as installed: decision={before_switch}",
        f"the same number once the switch in Options is on: decision={after_switch}",
        f"system call log: {logged[-1] if logged else 'no entry'}  (type 6 = blocked, block_reason 1 = call screening service)",
    )


def check_quick_settings_tile():
    # The tile is added and clicked through the status bar's own shell command, as a user
    # would tap it: the first tap pauses filtering for an hour, the next resumes it.
    tile = f"{PACKAGE}/.tile.PauseTileService"
    go_home()
    tap("mode-BLOCK")
    if find("resume"):
        tap("resume")
    shell(f"cmd statusbar add-tile {tile}")
    time.sleep(1.5)
    shell(f"cmd statusbar click-tile {tile}")
    time.sleep(2.5)
    go_home()
    paused_shown = bool(find("resume"))
    clear_log()
    ring("5551110012")
    paused = decisions()
    hang_up("5551110012")
    shell(f"cmd statusbar click-tile {tile}")
    time.sleep(2.5)
    go_home()
    resumed_shown = not find("resume")
    clear_log()
    ring("5551110012")
    resumed = decisions()
    hang_up("5551110012")
    shell(f"cmd statusbar remove-tile {tile}")
    record(
        "12. The Quick Settings tile pauses filtering for an hour, and resumes it",
        paused_shown and paused == [("ALLOW", "paused")] and resumed_shown and resumed == [("BLOCK", "unknown-caller")],
        f"after one tap on the tile: Home offers Resume = {paused_shown}; a non-contact calls: decision={paused}",
        f"after the next tap: Home offers Resume = {not resumed_shown}; the same number: decision={resumed}",
    )


def returned_to_app():
    """Back out of whatever a link opened until the app is in front again."""
    for _ in range(6):
        if PACKAGE in focused_window():
            return
        shell("input keyevent KEYCODE_BACK")
        time.sleep(1.5)
    open_app()


def opens(tag, expected):
    """Tap a link and report the window that came to the front, then come back."""
    tap(tag)
    time.sleep(3.5)
    window = focused_window()
    returned_to_app()
    short = window.split("/")[0].split()[-1] if window else ""
    return short, any(e in window for e in expected)


def launch_switch(name):
    """One of the two switches in ui/Links.kt that say whether a page the app links to exists yet."""
    path = os.path.join(REPO, "app", "src", "main", "kotlin", "com", "cyanharborstudios", "callblock", "ui", "Links.kt")
    with open(path) as f:
        return re.search(rf"\bval {name} = (true|false)\b", f.read()).group(1) == "true"


def check_links():
    chooser = ("ChooserActivity", "intentresolver", "ResolverActivity")
    go_home()
    tap("open-statistics")
    share_window, share_ok = opens("share", chooser)

    # A row is shown only once the page behind it exists: the two store rows in Settings, the
    # privacy policy in About. Until then the check is that the row is not there.
    store_page = launch_switch("STORE_PAGE_LIVE")
    privacy_page = launch_switch("PRIVACY_PAGE_LIVE")
    wanted = [
        ("Settings", "share-app", chooser, store_page),
        ("Settings", "rate-app", ("com.android.vending",), store_page),
        ("About", "contact", chooser + ("com.google.android.gm",), True),
        ("About", "privacy-policy", chooser + ("chrome", "browser"), privacy_page),
    ]
    seen = []
    for screen, tag, expected, shown in wanted:
        go_home()
        tap("open-settings")
        if screen == "About":
            tap("about")
        if shown:
            window, ok = opens(tag, expected)
            seen.append((f"{screen}, {tag}: {window}", ok))
        else:
            absent = not find(tag)
            seen.append((f"{screen}, {tag}: its page does not exist yet; the row is not shown = {absent}", absent))
    go_home()
    record(
        "13. Share and the links open the system's own targets; a row whose page does not exist yet is not shown",
        share_ok and all(ok for _, ok in seen),
        f"Statistics, Share: {share_window}",
        *[line for line, _ in seen],
    )


def check_allow_removal():
    go_home()
    tap("open-history")
    rows = handled_calls()
    already = {k for k, _ in allowed_numbers()}
    position, number = next((i, r[0]) for i, r in enumerate(rows) if key_of(r[0]) not in already)
    key = key_of(number)
    # (a) allowed always from the sheet, then removed from the sheet
    tap("history-row", index=position)
    tap("allow-for-always")
    added = key in {k for k, _ in allowed_numbers()}
    tap("history-row", index=position)
    tap("remove-from-allow-list")
    time.sleep(1.0)
    if find("number-details"):
        back()
    removed_from_sheet = key not in {k for k, _ in allowed_numbers()}
    # (b) allowed for a day from the sheet, then removed with the row's button in Options
    tap("history-row", index=position)
    tap("allow-for-1440")
    go_home()
    tap("open-options")
    if not is_checked("allow-list"):
        tap("allow-list")
    before = len(allowed_numbers())
    tap("remove-allowed", index=0)
    time.sleep(1.0)
    after = len(allowed_numbers())
    back()
    record(
        "14. An allow entry can be removed from the number's sheet and from Options",
        added and removed_from_sheet and after == before - 1,
        f"allowed always from the sheet: on the list = {added}; Remove From Allow List: on the list = {not removed_from_sheet}",
        f"allowed for a day, then the row's remove button in Options: entries {before} -> {after}",
    )


def check_monthly_report():
    go_home()
    tap("open-settings")
    tap("report-MONTHLY")
    back()
    time.sleep(3)
    zone = ZoneInfo(shell("getprop persist.sys.timezone").strip())
    now = datetime.datetime.now(zone)
    month_start = now.replace(day=1, hour=0, minute=0, second=0, microsecond=0)
    rows = [r for r in handled_calls() if r[1] >= month_start.timestamp() * 1000]
    blocked = sum(1 for r in rows if r[2] == "BLOCK")
    silenced = sum(1 for r in rows if r[2] == "SILENCE")
    expected_text = f"{blocked} blocked, {silenced} silenced"
    early = expected_text in notification_dump()
    first_of_next = (month_start + datetime.timedelta(days=32)).replace(day=1, hour=10)
    if first_of_next - now < datetime.timedelta(hours=25):
        first_of_next += datetime.timedelta(days=1)
    shell("settings put global auto_time 0")
    shell(f"cmd alarm set-time {int(first_of_next.timestamp() * 1000)}")
    time.sleep(1)
    jobs = re.findall(
        r"JOB (?:#|(?P<ns>[\w.]+):)u0a\d+/(?P<id>\d+): \w+ (?:@[\w.]+@)?" + re.escape(PACKAGE) + r"/androidx\.work",
        shell("dumpsys jobscheduler"),
    )
    job_ids = sorted(set(jobs))
    for namespace, job_id in job_ids:
        shell(f"cmd jobscheduler run -f {'-n ' + namespace + ' ' if namespace else ''}{PACKAGE} {job_id}")
    time.sleep(6)
    text_shown = expected_text in notification_dump()
    shell("settings put global auto_time 1")
    shell(f"cmd alarm set-time {int(time.time() * 1000)}")
    record(
        "15. The monthly report arrives once the month has ended",
        not early and len(job_ids) >= 1 and text_shown,
        f"monthly switched on mid-month: a report with this month's counts = {early} (nothing is due yet)",
        f"clock moved to {first_of_next:%A %d %b, %H:%M}; the scheduled job was run: the notification reads \"{expected_text}\": {text_shown}",
    )


def clear_shade():
    """Dismiss every notification with the shade's own Clear All control, so the one posted
    next stands alone: Android bundles several from one app and hides a child's actions."""
    shell("cmd statusbar expand-notifications")
    time.sleep(2.0)
    button = next((n for n in nodes() if n.get("resource-id") == "com.android.systemui:id/btn_clear_all"), None)
    if button is not None:
        x, y = centre(button)
        shell(f"input tap {x} {y}")
        time.sleep(2.0)
    shell("cmd statusbar collapse")
    time.sleep(1.0)


def our_notification_row(number):
    digits = re.sub(r"\D", "", number)
    for row in nodes():
        if row.get("resource-id") != "com.android.systemui:id/expandableNotificationRow":
            continue
        if any(digits in re.sub(r"\D", "", plain(n.get("text"))) for n in row.iter()):
            return row
    return None


def expand_notification_showing(number):
    """In the open shade, expand the notification whose text carries [number] with its own
    row's expand button: a system control with a resource id, not a word of ours."""
    row = our_notification_row(number)
    button = next((n for n in row.iter() if n.get("resource-id") == "android:id/expand_button"), None) if row is not None else None
    if button is not None:
        x, y = centre(button)
        shell(f"input tap {x} {y}")
        time.sleep(1.5)


def notification_action(number):
    """The expanded notification's first action, by Android's own id for it."""
    row = our_notification_row(number)
    return next((n for n in row.iter() if n.get("resource-id") == "android:id/action0"), None) if row is not None else None


def check_notification_action_and_lock_screen():
    go_home()
    tap("mode-BLOCK")
    if not is_checked("notifications"):
        tap("notifications")
    clear_shade()
    number = "5551110016"
    clear_log()
    ring(number)
    hang_up(number)
    posted_before = len(notifications_from_app())
    # the unlocked shade shows the number; the action appears once the notification is expanded
    shell("cmd statusbar expand-notifications")
    time.sleep(2.5)
    number_shown_unlocked = any(number in re.sub(r"\D", "", t) for t in texts())
    expand_notification_showing(number)
    button = notification_action(number)
    acted = button is not None
    labelled = acted and plain(button.get("text")).casefold() == action_label().casefold()
    if acted:
        x, y = centre(button)
        shell(f"input tap {x} {y}")
        time.sleep(2.0)
    shell("cmd statusbar collapse")
    time.sleep(1.0)
    entry = next((e for k, e in allowed_numbers() if k == key_of(number)), False)
    for_an_hour = entry not in (False, None) and abs(entry - (time.time() * 1000 + 3_600_000)) < 5 * 60_000
    notification_gone = len(notifications_from_app()) < posted_before
    clear_log()
    started = device_clock()
    ring(number)
    after = decisions()
    rang = ringer_started_after(started)
    hang_up(number)
    # the locked screen: behind a PIN the public version carries no number
    second = "5551110017"
    ring(second)
    hang_up(second)
    shell("locksettings set-pin 1234")
    shell("input keyevent KEYCODE_SLEEP")
    time.sleep(1.5)
    shell("input keyevent KEYCODE_WAKEUP")
    time.sleep(3.0)
    locked_texts = texts()
    number_hidden_locked = bool(locked_texts) and all(second not in re.sub(r"\D", "", t) for t in locked_texts)
    shell("locksettings clear --old 1234")
    unlock()
    go_home()
    if is_checked("notifications"):
        tap("notifications")
    record(
        "16. The notification's one action lets the number ring for an hour, and a locked screen shows no number",
        number_shown_unlocked and acted and for_an_hour and notification_gone and after == [("ALLOW", "allow-list")] and rang and number_hidden_locked,
        f"unlocked shade: the number is shown = {number_shown_unlocked}; the action is there = {acted}, with the label the strings file gives it = {labelled}",
        f"after the action: on the allow list for an hour = {for_an_hour}; notification cancelled = {notification_gone}; the number calls again: decision={after}, ringer started = {rang}",
        f"behind a PIN, after a new stopped call: the number appears on the locked screen = {not number_hidden_locked}",
    )


def check_deletes():
    go_home()
    tap("open-history")
    before = len(handled_calls())
    tap("history-row", index=0)
    tap("delete-call")
    time.sleep(1.0)
    after_one = len(handled_calls())
    tap("delete-all")
    tap("delete-all-confirm")
    time.sleep(1.5)
    after_all = len(handled_calls())
    empty_shown = not find("history-row")
    go_home()
    record(
        "17. Deleting one call from its sheet, and Delete All, carry through",
        after_one == before - 1 and after_all == 0 and empty_shown,
        f"Delete This Call on the sheet: calls {before} -> {after_one}",
        f"Delete All, confirmed: calls {after_one} -> {after_all}; History shows no rows = {empty_shown}",
    )


def check_tutorial():
    # A first launch is a launch with nothing stored. Removing the settings file gives one and
    # leaves the history alone, so this check stands on its own. It leaves the lever at Off.
    shell(f"am force-stop {PACKAGE}")
    shell(f"run-as {PACKAGE} rm -f files/datastore/settings.preferences_pb")
    open_app()
    opened = bool(find("how-screen")) and not find("mode-OFF")
    back()
    closed = bool(find("mode-OFF")) and not find("how-screen")
    shell(f"am force-stop {PACKAGE}")
    open_app()
    stays_closed = bool(find("mode-OFF")) and not find("how-screen")
    tap("open-settings")
    tap("how-it-works")
    replayed = bool(find("how-screen"))
    scroll_down()
    tap("how-done")
    returned = bool(find("how-it-works")) and not find("how-screen")
    go_home()
    after_wipe = first_launch_showed_tutorial
    record(
        "18. How It Works opens by itself until it has been closed once, and again from Settings",
        opened and closed and stays_closed and replayed and returned and after_wipe is not False,
        f"at the start of this run the wiped app opened on How It Works = {after_wipe}" if after_wipe is not None else "",
        f"settings file removed, app opened: How It Works on screen, Home not yet = {opened}",
        f"one press of Back: Home = {closed}; the app stopped and opened again: Home, no How It Works = {stays_closed}",
        f"Settings, How It Works: on screen = {replayed}; its Done key: back in Settings = {returned}",
    )


# ---------- the plans, the timer and the schedule ----------

def set_plan(name):
    """A test build can try each plan without buying: Settings, Plans, the key for the plan."""
    go_home()
    scroll_to_top()  # the gear is at the top of Home, and an earlier step may have scrolled it away
    tap("open-settings")
    tap("plans")
    tap_below(f"debug-tier-{name}")
    go_home()


def hour_cell(row_tag, hour):
    """The middle of one hour's cell in a row of the schedule's grid, in screen pixels."""
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", find(row_tag)[0].get("bounds")))
    inset = 3 * float(shell("wm density").split()[-1]) / 160  # the row's 3 dp inset
    cell = (x2 - x1 - 2 * inset) / 24
    return int(x1 + inset + (hour + 0.5) * cell), (y1 + y2) // 2


def drag_whole_week():
    """Drag across every hour of the top row, which stands for every day."""
    (xa, y), (xb, _) = hour_cell("schedule-row-ALL", 0), hour_cell("schedule-row-ALL", 23)
    shell(f"input swipe {xa} {y} {xb} {y} 600")
    time.sleep(1.2)


def set_this_hour_to_block():
    """Open the schedule, empty the week, and set the present hour to Block on every day."""
    go_home()
    tap_below("open-schedule")
    tap("brush-CLEAR")
    drag_whole_week()
    tap("brush-BLOCK")
    hour = int(shell("date +%H").strip())
    x, y = hour_cell("schedule-row-ALL", hour)
    shell(f"input tap {x} {y}")
    time.sleep(1.2)
    return hour


def clear_the_schedule():
    go_home()
    tap_below("open-schedule")
    tap("brush-CLEAR")
    drag_whole_week()
    if is_checked("schedule-on"):
        tap("schedule-on")
    go_home()


def call_decision(number):
    clear_log()
    ring(number)
    seen = decisions()
    hang_up(number)
    return seen


def check_timer():
    set_plan("PRO")
    clear_the_schedule()
    scroll_to_top()
    tap("mode-SILENCE")
    tap_below("open-timer")
    tap("timer-mode-BLOCK")
    tap("timer-for-15")
    scroll_to_top()
    key_shown = bool(find("end-timer"))
    timed = call_decision("5551110019")
    # Seventeen minutes on, the timer has ended by itself and the lever's Silence is back.
    later = datetime.datetime.now() + datetime.timedelta(minutes=17)
    shell("settings put global auto_time 0")
    shell(f"cmd alarm set-time {int(later.timestamp() * 1000)}")
    time.sleep(6)  # Home reads the clock every few seconds
    key_gone = not find("end-timer")
    after = call_decision("5551110019")
    shell("settings put global auto_time 1")
    shell(f"cmd alarm set-time {int(time.time() * 1000)}")
    tap("mode-OFF")
    set_plan("FREE")
    record(
        "19. A timer holds Block for a while, then the lever's own stop is back",
        key_shown and timed == [("BLOCK", "unknown-caller-on-timer")] and key_gone and after == [("SILENCE", "unknown-caller")],
        f"Pro, lever at Silence, timer at Block for 15 minutes: Home offers End Timer = {key_shown}; a non-contact calls: decision={timed}",
        f"clock moved on 17 minutes: End Timer gone = {key_gone}; the same number: decision={after}",
    )


def check_schedule():
    set_plan("PRO")
    scroll_to_top()
    tap("mode-OFF")
    hour = set_this_hour_to_block()
    switched_on = is_checked("schedule-on")
    go_home()
    scheduled = call_decision("5551110020")
    # Inside the scheduled hour a move of the lever holds until the hour ends, as a pause does.
    scroll_to_top()
    tap("mode-OFF")
    held = call_decision("5551110020")
    resume_shown = bool(find("resume"))
    tap("resume")
    resumed = call_decision("5551110020")
    # Five minutes into the next hour the schedule asks for nothing, and the lever is at Off.
    now = datetime.datetime.now()
    next_hour = now.replace(minute=5, second=0, microsecond=0) + datetime.timedelta(hours=1)
    shell("settings put global auto_time 0")
    shell(f"cmd alarm set-time {int(next_hour.timestamp() * 1000)}")
    time.sleep(2)
    outside = call_decision("5551110020")
    shell("settings put global auto_time 1")
    shell(f"cmd alarm set-time {int(time.time() * 1000)}")
    clear_the_schedule()
    set_plan("FREE")
    record(
        "20. The schedule blocks in the hours it was given, and a lever move inside them holds until they end",
        switched_on
        and scheduled == [("BLOCK", "unknown-caller-on-schedule")]
        and held == [("ALLOW", "paused")] and resume_shown
        and resumed == [("BLOCK", "unknown-caller-on-schedule")]
        and outside == [("ALLOW", "off")],
        f"Pro, lever at Off, the hour from {hour}:00 set to Block for every day: the schedule switched itself on = {switched_on}; a non-contact calls: decision={scheduled}",
        f"lever moved to Off inside that hour: decision={held}; Home offers Resume = {resume_shown}; after Resume: decision={resumed}",
        f"clock moved to {next_hour:%H:%M}, outside the schedule: decision={outside}",
    )


def check_plans():
    # What each plan holds: only Free shows the ad tray; only Pro runs a schedule, and a
    # schedule that is stored stays stored for when Pro comes back.
    set_plan("PRO")
    scroll_to_top()
    tap("mode-OFF")
    set_this_hour_to_block()
    go_home()
    tray_on_pro = bool(find("banner-slot"))
    on_pro = call_decision("5551110021")

    set_plan("NO_ADS")
    tray_on_no_ads = bool(find("banner-slot"))
    on_no_ads = call_decision("5551110021")
    tap_below("open-schedule")
    schedule_leads_to_plans = bool(find("plans-screen")) and not find("schedule-screen")

    set_plan("FREE")
    tray_on_free = bool(find("banner-slot"))
    on_free = call_decision("5551110021")
    tap_below("open-timer")
    timer_leads_to_plans = bool(find("plans-screen")) and not find("timer-sheet")

    set_plan("PRO")
    back_on_pro = call_decision("5551110021")
    clear_the_schedule()
    set_plan("FREE")
    record(
        "21. Each plan holds what it says: ads on Free only, the timer and the schedule on Pro only",
        tray_on_free and not tray_on_no_ads and not tray_on_pro
        and on_pro == [("BLOCK", "unknown-caller-on-schedule")]
        and on_no_ads == [("ALLOW", "off")] and on_free == [("ALLOW", "off")]
        and schedule_leads_to_plans and timer_leads_to_plans
        and back_on_pro == [("BLOCK", "unknown-caller-on-schedule")],
        f"the ad tray is on screen: Free = {tray_on_free}, No Ads = {tray_on_no_ads}, Pro = {tray_on_pro}",
        f"a schedule set on Pro (this hour: Block), lever at Off, a non-contact calls: Pro {on_pro}, No Ads {on_no_ads}, Free {on_free}",
        f"without Pro the Schedule row opens Plans = {schedule_leads_to_plans}, and so does the Timer row = {timer_leads_to_plans}",
        f"back on Pro the stored schedule runs again: decision={back_on_pro}",
    )


def dial(number, seconds=5.0):
    """The user calls [number]: the dialer places the call, and it is ended after a moment."""
    shell(f"am start -a android.intent.action.CALL -d tel:{number}")
    time.sleep(seconds)
    shell("input keyevent KEYCODE_ENDCALL")
    time.sleep(2.0)


def ring_until_ringer(number, started, limit=12.0):
    """Ring until Telecom starts the ringer, or [limit] seconds. Right after another call has ended the ringer can take a few seconds."""
    adb("emu", "gsm", "call", number)
    waited = 0.0
    while waited < limit:
        time.sleep(1.0)
        waited += 1.0
        if waited >= 3.0 and ringer_started_after(started):
            return True
    return ringer_started_after(started)


def outgoing_calls_seen():
    """How many outgoing calls the screening service says Android showed it. The line never holds a number."""
    return adb("logcat", "-d", "-s", "ScreeningService:I").count("outgoing call seen")


def check_call_back():
    go_home()
    scroll_to_top()
    tap("mode-BLOCK")
    number, other = "5551110022", "5551110023"
    stranger = call_decision(number)

    clear_log()
    dial(number)
    seen = outgoing_calls_seen()
    go_home()
    clear_log()
    started = device_clock()
    rang = ring_until_ringer(number, started)
    called_back = decisions()
    hang_up(number)

    # Twenty-five hours on, the number is a stranger again.
    later = datetime.datetime.now() + datetime.timedelta(hours=25)
    shell("settings put global auto_time 0")
    shell(f"cmd alarm set-time {int(later.timestamp() * 1000)}")
    time.sleep(1)
    next_day = call_decision(number)
    shell("settings put global auto_time 1")
    shell(f"cmd alarm set-time {int(time.time() * 1000)}")
    time.sleep(1)

    # Switched off in Options, the numbers kept are forgotten and a dialled number is not
    # remembered at all. That is also what leaves the next run clean.
    kept = lambda: database_rows("SELECT COUNT(*) FROM dialled_numbers")[0][0]
    go_home()
    tap("open-options")
    was_on = is_checked("call-backs")
    kept_while_on = kept()
    tap("call-backs")
    back()
    dial(other)
    go_home()
    kept_while_off = kept()
    while_off = call_decision(other)
    tap("open-options")
    tap("call-backs")
    back_on = is_checked("call-backs")
    back()
    # On again, and still inside the day: the first number rings only if it was kept.
    forgotten = call_decision(number)
    record(
        "22. A number the user called rings when it calls back, for a day",
        stranger == [("BLOCK", "unknown-caller")] and seen == 1
        and called_back == [("ALLOW", "you-called")] and rang
        and next_day == [("BLOCK", "unknown-caller")]
        and was_on and kept_while_on == 1 and kept_while_off == 0
        and while_off == [("BLOCK", "unknown-caller")] and back_on and forgotten == [("BLOCK", "unknown-caller")],
        f"lever at Block, a non-contact calls: decision={stranger}",
        f"the user calls that number (Android showed the app {seen} outgoing call), and it calls back: decision={called_back}, ringer started = {rang}",
        f"clock moved on 25 hours, the same number: decision={next_day}",
        f"the switch in Options is on as installed = {was_on}, with {kept_while_on} dialled number kept; switched off, another number is dialled: "
        f"numbers kept = {kept_while_off}, and when it calls back: decision={while_off}",
        f"switched back on = {back_on}; the first number, dialled a few minutes ago: decision={forgotten}",
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
    parser.add_argument("--only", help="run only these check numbers, e.g. 16 or 13,16; no report is written, and the app's data is not wiped (18 does remove its settings file)")
    args = parser.parse_args()
    SERIAL = args.serial

    checks = [
        check_block,
        check_silence,
        check_contact,
        check_notifications,
        check_clock_formats,
        check_temporary_allow,
        check_scope,
        check_repeat_caller,
        check_milestone,
        check_weekly_report,
        check_india_series,
        check_quick_settings_tile,
        check_links,
        check_allow_removal,
        check_monthly_report,
        check_notification_action_and_lock_screen,
        check_deletes,
        check_tutorial,
        check_timer,
        check_schedule,
        check_plans,
        check_call_back,
    ]
    if args.only:
        wanted = {int(n) for n in args.only.split(",")}
        checks = [c for i, c in enumerate(checks, start=1) if i in wanted]
        require_emulator()
        shell("svc power stayon true")
        unlock()
    else:
        setup(args.apk)
    try:
        for check in checks:
            check()
    finally:
        # Whatever happened, give the emulator its real clock back.
        shell("settings put global auto_time 1")
        shell(f"cmd alarm set-time {int(time.time() * 1000)}")
    if not args.only:
        write_report(args.apk)
    return 0 if all(ok for _, ok, _ in results) else 1


if __name__ == "__main__":
    sys.exit(main())
