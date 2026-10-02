# Verification on the emulator

What has been shown to work on a device, what has not, and how to repeat it. The
feature-by-feature view is `FEATURES.md`; this file is the evidence behind its
"emulator" entries.

Everything here was done on the emulator `scarlet_phone` (Pixel 4a profile, Android
16, API 36, Google Play image). Nothing was installed on, or changed on, the
founder's phone.

## The scripted checks

```bash
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export PATH="$ANDROID_HOME/platform-tools:$PATH"
$ANDROID_HOME/emulator/emulator -avd scarlet_phone -no-window -no-audio &
./gradlew assembleDebug
tools/verify_emulator.py          # about four minutes for the first six checks
```

The script refuses to run against anything that is not an emulator. It wipes the
app's data, simulates calls with `adb emu gsm call`, drives the app by test tags, and
writes `emulator-<date>.md` here. It reads the app's decision from the debug build's
log line (`decision=… rule=…`, never a number) and reads what Android then did from
Telecom's event log, the system call log and the notification manager. For expiry it
moves the emulator's clock forward and puts it back.

| # | Check | Result |
|---|---|---|
| 1 | A non-contact is rejected in Block mode | **passed** |
| 2 | A non-contact rings silently in Silence mode and appears in the system call log | **passed** |
| 3 | A contact rings normally | **passed** |
| 4 | The notification appears only when enabled | **passed** |
| 5 | History and number details show correct times under 12-hour and 24-hour settings | **passed** |
| 6 | A temporary allow lets the number ring until it expires (an expiring allow entry, and a pause) | **passed** |
| 7 | International-only scope lets a domestic non-contact ring and still blocks one from abroad | not yet run to completion |
| 8 | A repeat caller rings the second time | not yet run to completion |
| 9 | A milestone notification arrives at ten handled calls | not yet run to completion |
| 10 | The weekly report arrives once the week has ended | not yet run to completion |

### Which build the results are for

- **Checks 1 to 6 all passed** in the run recorded in
  [`emulator-2026-10-02.md`](emulator-2026-10-02.md) (2 Oct 2026, about 18:47). That
  run was on commit `00fe693` plus the test-tag edits that came in with this folder.
- Three edits were made after that run: the compact mode switch on the home screen,
  writing the report setting in one step, and a few more test tags. A re-run on that
  final build **passed checks 1 to 5** and was stopped, at the founder's request,
  while check 6 was running. So check 6 has passed once, on the earlier build; the
  code it exercises (pause, the allow list, the rule engine) was not among the edits.
- Checks 7 to 10 were added the same evening. The first attempt stalled in the
  script itself: check 6 had left a pause active once the clock was put back, so later
  calls rang through. The script now ends that pause and waits for each call to clear.
  The second attempt was the one stopped during check 6. **None of the four has
  finished a run**, so they prove nothing yet.

## Done by hand

- **The spike** (ADR-002): Android's own role prompt opened from the home screen and
  was accepted; block, silence and a contact's call behaved as documented.
- **Every screen**, at the default size and, for most, at 360 dp with 135% text in
  the dark theme (the founder's phone settings). The screenshots in
  [`screens/`](screens/) are from the final build, with made-up numbers.
  [`screens/contact-sheet.png`](screens/contact-sheet.png) shows them together.
- **The test banner** loads into its reserved slot on the home, options, history,
  statistics and settings screens. Nothing moves when it arrives.
- **The minified release build** (`./gradlew assembleRelease`, signed with the debug
  key for this test only): cold start in 461 ms (the debug build takes about 960 ms);
  a non-contact was rejected in Block (Telecom: `SCREENING_COMPLETED ([Reject, …])`,
  call log type "blocked") and rang silently in Silence (`SKIP_RINGING`); both calls
  appeared in History; no decision lines in logcat; no crash. So Room, DataStore and
  the phone-number library survive R8.
- **The playbook's policy checker**, APK checks only, on that release build: target
  API 36, all eleven permissions on the allowlist, no typed foreground service. Its
  one error, the advertising-ID declaration, is a launch task (the Data safety
  document does not exist yet).

## Not yet exercised on a device

Built, with their logic unit-tested, but not yet seen working end to end:

- the scope setting, repeat callers, the milestone notification and the weekly or
  monthly report (checks 7 to 10 above);
- deleting one history row, and Delete All with its confirmation;
- sharing the statistics; the licences screen; the contact, share-app and rate-app
  rows in Settings;
- a full-screen ad actually appearing on leaving History or Statistics;
- the notice on the home screen when the role has been lost to another app, and
  declining the role prompt;
- the handled-call notification as it looks on a locked screen;
- the consent form itself (the emulator is a US device, where none is required);
- typing a number into the allow list, and removing an entry;
- tapping a chart bar, switching the statistics period, and opening a number's
  details from the Statistics list;
- "yesterday" and dated headings in History, and the "change against the week
  before" lines in Statistics (every emulator call was made the same day).

## Raw captures

`raw/` is git-ignored: working screenshots and UI dumps from the emulator.
