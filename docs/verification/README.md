# Verification on the emulator

What has been shown to work on a device, what has not, and how to repeat it. The
feature-by-feature view is `FEATURES.md`; this file is the evidence behind its
"emulator" entries.

Everything here was done on the emulator `scarlet_phone` (Pixel 4a profile, Android
16, API 36, Google Play image). The screens were looked at with the emulator set to
360 dp wide (`adb -s emulator-5554 shell wm density 480`, 780 dp tall), the founder's
own phone width. Nothing was installed on, or changed on, the founder's phone by these
checks.

## The scripted checks

```bash
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools
export PATH="$ANDROID_HOME/platform-tools:$PATH"
$ANDROID_HOME/emulator/emulator -avd scarlet_phone -no-window -no-audio &
./gradlew assembleDebug
tools/verify_emulator.py          # about twenty-three minutes for all twenty-one checks
tools/verify_emulator.py --only 16   # one check again, on the app as it is; no report
                                     # (13 needs a stopped call in the history; 18 removes the settings file)
```

The script refuses to run against anything that is not an emulator. It wipes the
app's data, simulates calls with `adb emu gsm call`, drives the app by test tags, and
writes `emulator-<date>.md` here. It reads the app's decision from the debug build's
log line (`decision=… rule=…`, never a number) and reads what Android then did from
Telecom's event log, the system call log and the notification manager. For expiry and
for the weekly report it moves the emulator's clock forward and puts it back.

| # | Check | Result |
|---|---|---|
| 1 | A non-contact is rejected in Block mode | **passed** |
| 2 | A non-contact rings silently in Silence mode and appears in the system call log | **passed** |
| 3 | A contact rings normally | **passed** |
| 4 | The notification appears only when enabled | **passed** |
| 5 | History and number details show correct times under 12-hour and 24-hour settings | **passed** |
| 6 | A temporary allow lets the number ring until it expires (an expiring allow entry, and a pause) | **passed** |
| 7 | International-only scope lets a domestic non-contact ring and still blocks one from abroad | **passed** |
| 8 | A repeat caller rings the second time | **passed** |
| 9 | A milestone notification arrives at ten handled calls | **passed** |
| 10 | The weekly report arrives once the week has ended | **passed** |
| 11 | India's 160 series rings; the 140 series is blocked only once the user asks | **passed** |
| 12 | The Quick Settings tile pauses filtering for an hour, and resumes it | **passed** |
| 13 | Share and the links open the system's own targets (the chooser, Gmail); a row whose page does not exist yet (Privacy Policy, Share App, Rate App) is not on screen | **passed** |
| 14 | An allow entry can be removed from the number's sheet and from Options | **passed** |
| 15 | The monthly report arrives once the month has ended | **passed** |
| 16 | The notification's one action lets the number ring for an hour, and a locked screen shows no number | **passed** |
| 17 | Deleting one call from its sheet, and Delete All, carry through | **passed** |
| 18 | How It Works opens by itself until it has been closed once, and again from Settings | **passed** |
| 19 | A timer holds Block for a while, then the lever's own stop is back | **passed**, in the later run described below |
| 20 | The schedule blocks in the hours it was given, and a lever move inside them holds until they end | **passed**, in the later run |
| 21 | Each plan holds what it says: ads on Free only, the timer and the schedule on Pro only | **passed**, in the later run |
| 22 | A number the user called rings when it calls back, for a day | **passed**, run by itself (below) |
| 23 | After a call to an emergency number, every call rings for a day | **passed**, run by itself (below) |

### Which build the results are for

- The table is the run recorded in [`emulator-2026-10-05.md`](emulator-2026-10-05.md), on
  the build with How It Works, About and the rows hidden until their pages exist (commit
  `ee13905`). **All eighteen passed.** Check 18 is new: it removes the app's settings
  file, which is what a first launch has, and sees How It Works open by itself, close on
  one press of Back, stay closed on the next launch, and open again from Settings, where
  its Done key goes back to Settings. Check 13 changed: Contact is tapped on About, and
  for each row whose page does not exist yet it reads the switch in `ui/Links.kt` and
  checks that the row is not on screen. One thing learnt on the way: the first attempt
  that day stopped inside check 16, which sets a PIN to read the locked screen and clears
  it again; Android kept its swipe lock up for a moment after the PIN was gone, one
  `wm dismiss-keyguard` missed it, and the app then started behind the lock screen. The
  script now asks, looks at what Android reports and asks again (`unlock`). Checks 1 to 15
  had passed in that attempt; the recorded run is the complete one after the fix.
- **Checks 19 to 21 came that evening, with the freemium build** (the commit that adds
  this paragraph). All twenty-one passed in one run from a wiped app, 23 minutes, on a
  build of exactly that commit's code. Its report is not the recorded one: the recorded
  report is made again on the build that ends this round of work, and until then the file
  named above is still the eighteen-check run. What the three check: 19 sets a timer at
  Block for 15 minutes with the lever at Silence, sees a caller blocked by the timer's
  rule, moves the clock on 17 minutes and sees the lever's Silence back. 20 sets the
  present hour to Block on the week's grid with the lever at Off, sees the schedule switch
  itself on and block a caller, moves the lever to Off inside that hour and sees it hold
  as a pause, resumes, and moves the clock past the hour to see the call ring. 21 goes
  through the three plans with a test build's own keys: the ad tray is on screen on Free
  only; a schedule stored on Pro does nothing on No Ads or Free and runs again on Pro; and
  without Pro the Timer and Schedule rows open Plans. Three things learnt on the way: a
  row under the ad tray or in the gesture area is listed in the screen dump but cannot be
  tapped, so the script scrolls until its middle is clear of both; a check must clear what
  an earlier one stored (19 clears the schedule first); and a row for sale must not push a
  free row off the first screen, which is how check 4 found that the Automatic section
  belonged under Options and Notifications for anyone who has not bought Pro.
- **Check 22 came with the call-back rule**, later the same evening, and passes run by
  itself (`--only 22`). With the lever at Block a stranger is
  blocked; the user calls that number (`am start -a android.intent.action.CALL`), the app's
  log says Android showed it one outgoing call, and when the number calls back the app
  allows it by the rule `you-called` and Telecom records that it passed every filter and
  was set ringing; 25 hours on it is blocked again; with the switch in Options off, a
  dialled number is not kept (the table is read: one more row after the call, none once
  switched off) and is blocked when it calls back; and with the switch on again the first
  number is blocked too, which shows that switching off forgot it. One thing learnt, the
  hard way: right after another call has ended, Android's ringer can start late. It began
  15 ms after the call was set ringing in most trials, 2 seconds after in one, not within
  4 in the first run of this check and not within 12 in a full run. The wait is Android's
  own and the same for any caller: Telecom holds the ringer until its Bluetooth call
  service is bound again (`CallAudioManager.onCallEnteringRinging`, read 5 Oct 2026). So
  this check goes by Telecom's record that the call was set ringing, and reports the ringer
  beside it. One more run failed, rightly: it was made while the emulator was in
  emergency callback mode after a call to its emergency number (placed on the emulated
  modem to learn what Android does, `NOTES.md` N-43), and in that mode Android does not ask
  a screening app at all. On the emulator the mode did not end by itself or from its own
  dialog; a restart of the emulator ended it.
- **A full run from a wiped app on the build with the pause after an emergency call**
  (23 checks, 26 minutes, the restart included) passed 22. The one failure was check 22's
  wait for the ringer, described above; with the check corrected, 22 and 23 passed in order
  on that change's final build. The recorded report is still to be made on the build that
  ends this round.
- **Check 23 came with the pause after an emergency call** (ADR-008) and passed run by
  itself. **It never dials a real emergency number.** It puts a made-up number on Android's
  own test list (`cmd phone emergency-number-test-mode -a`), asks Android to call it, which
  only opens Android's dialer with the number in it, presses the dialer's own call button,
  reads the app's log (`outgoing call seen emergency=true`), ends the call with the
  dialer's own button and takes the number off the list again. The emulator is then in
  emergency callback mode, where Android asks no screening app about any call, and stays
  there past the five minutes it announces; the check restarts the emulator, which ends
  it. After that Home offers Resume, a stranger's call rings by the rule `paused`, and with
  the clock moved on 25 hours Resume is gone and the stranger is blocked again. It also
  reads the table of dialled numbers before and after: the number of an emergency call is
  not kept for call-backs. The check asks twice whether the device is an emulator, at the start of the script and again
  before it calls.
- The run before it, [`emulator-2026-10-04.md`](emulator-2026-10-04.md), passed its
  seventeen on the Switchboard screens with the India series rules, their Options switch
  and the Quick Settings tile (it names its parent commit, `425e191`). It is the run in
  which the Privacy Policy, Share App and Rate App rows were on screen and opened Chrome,
  the chooser and the Play Store. The same day's earlier runs passed their ten, eleven and
  twelve.
- The earlier run, [`emulator-2026-10-02.md`](emulator-2026-10-02.md), passed checks
  1 to 6 on the first, stock Material screens; checks 7 to 10 had never completed a run
  before 4 Oct.
- On 4 Oct the script was adapted to the new screens: pause is a key on Home, an allow
  entry is one key on the number's sheet, repeat callers is one strip of keys, and the
  sheet's facts are read by their tags. Three things about Android 16 were learnt on the
  way and are now handled: a time is one word on screen (a no-break space before AM or
  PM); Android adds a group summary of its own over an app's silent notifications, which
  is not counted; and WorkManager's job sits in its own job-scheduler namespace. Its own
  "viewing full screen" notice, shown over the first full-screen test ad, held the focus
  until tapped; the script taps it (it is Android's, not the ad's). The same day it gained
  check 11: a 1600 number with the lever at Block, then a 140 number with the lever at
  Silence, before and after the Options switch is turned on. Check 12 adds the Quick
  Settings tile through the status bar's own shell command and taps it the same way, as
  Android has no other way to do it from a script. Checks 13 to 17 came last: they drive
  the shade by Android's own resource ids (the clear-all button, a notification row's
  expand button, its first action), never by a word of copy; the action's label is compared
  with the one the app's strings file gives it. One thing learnt: Android bundles several
  notifications from one app and hides a child's actions, so the check clears the shade
  before it posts the one it looks at. `--only N` re-runs one check without a report.

## Done by hand

- **The spike** (ADR-002, 2 Oct): Android's own role prompt opened from the home screen
  and was accepted; block, silence and a contact's call behaved as documented.
- **Every screen of the Switchboard design**, on 4 Oct, at 360 dp: light at 100% text,
  dark at 100% and 135% text. The screenshots in [`screens/`](screens/) are from the
  build of that day, with made-up numbers;
  [`screens/contact-sheet.png`](screens/contact-sheet.png) shows them together. The Options
  shots (`07-options.png`, `19-options-india-dark.png`, `20-options-india-large-dark.png`)
  were retaken later that day with the India strip switched on, and the tile was captured
  in the Quick Settings panel, lit and paused (`21-tile-blocking.png`, `22-tile-paused.png`);
  the contact sheet predates both. Seen working: the lever (tap and drag), pause from Home and Resume, Options with the keys
  for who is filtered and repeat callers, the "Always block 140 numbers" strip off and on,
  the allow list switched on, a number typed into
  the field with its keys above the keyboard and committed for a day (the row, Home's
  Options strip, the history row and the sheet all say so), History with a repeat
  caller's "2nd call", the number's sheet from a row, Delete All's confirmation,
  Statistics with the counter, the milestone bar, the day chart as units and as bars, the
  period keys, the weekday and hour charts and the most frequent numbers, Settings,
  Licences. Also Home with the role taken away by `cmd role` (its one key opened
  Android's prompt, which showed the new icon) and Home after Android's notification
  permission was refused twice.
- **The changes of 5 Oct**, at 360 dp: How It Works on a first launch and from Settings
  (light at 100% text, dark at 100% and 135%), Settings in its new order, About, and Home
  with "Pause for" over its keys and the shorter line at Off. `01`, `02`, `04`, `06`, `13`,
  `15` and `16` in [`screens/`](screens/) were retaken that day, `23` to `28` are new, and
  the contact sheet was made again from all of them. Also seen, with Android set to
  destroy every activity it leaves: How It Works and Home each come back as they were, and
  the tutorial's Done still goes to Home on a first launch and to Settings from Settings.
- **The database's first change of version** (5 Oct, with the call-back rule). The build
  with the `dialled_numbers` table was installed over the freemium build, whose version 1
  database held six stopped calls and two allow entries after that evening's run, with no
  data cleared. Read before and after with `run-as`: version 1 then 2; the two old tables
  with the same rows (six stopped calls with the same first and last times and the same
  rules, two allow entries); the new table there and empty; nothing from Android's runtime,
  SQLite or Room in the log; the app open on Home. This is the step the founder's phone
  takes at its next install.
- **Call-backs on screen** (5 Oct), at 360 dp: the strip in Options on and off, light at
  100% text and dark at 135% (two lines in every case, so nothing under it moves when it is
  switched); Home's Options strip reading "Call-backs ring"; How It Works with its new line,
  and without it while the switch is off.
- **The test banner** loads into its tray on the home, options, history, statistics and
  settings screens, and the sill says "Advertisement" once it has. Nothing moves when it
  arrives.
- **The minified release build** (2 Oct, the first screens; `./gradlew assembleRelease`,
  signed with the debug key for that test only): cold start in 461 ms; a non-contact was
  rejected in Block and rang silently in Silence; no decision lines in logcat; no crash.
  Not repeated on the Switchboard screens.
- **The playbook's policy checker**, APK checks only, on that release build (2 Oct):
  target API 36, all permissions on the allowlist, no typed foreground service. Its one
  error, the advertising-ID declaration, is a launch task.

## On the founder's own phone

- **Block, on real calls (his report, 5 Oct 2026).** Using a food-delivery app, the founder found
  that the build "beautifully worked": calls from unknown numbers were blocked, with the lever at
  Block as last read on 4 Oct. Reported in his words and not seen: the guard rail keeps his phone's
  log and screens out of reach, so which calls these were is unrecorded. It settles the risk
  register's OEM item for Block on OxygenOS (`PLAN.md`, risk 6).

- **How It Works after an install over the earlier build (5 Oct 2026).** The merged build
  was installed on his phone and opened once over adb. The app came to the front with no
  crash, and the screen showing was How It Works, read by its tag alone; Home was not under
  it. The call-screening role stayed with the app. It was left open for him to read.
  Nothing else on his phone was looked at: Home's new caption, Settings and About there are
  not seen (`NOTES.md` N-41).

## Not yet exercised on a device

Built, with their logic unit-tested, but not yet seen working end to end:

- the Open Settings strip actually opening Android's notification settings (the strip
  itself was seen after two refusals of the permission), and Home on a device that
  cannot screen calls;
- the two licence links on the Licences screen (Contact, on About, is check 13);
- the Privacy Policy, Share App and Rate App rows since they were hidden on 5 Oct: they
  opened their targets in the run of 4 Oct, and check 13 taps them again once their
  switches in `ui/Links.kt` are on;
- **the pause after an emergency call, on a real phone: never to be exercised.** Nobody
  calls an emergency number to test an app. The unit tests and check 23 are the evidence;
- **call-backs on a real phone**: whether the founder's phone shows the app his outgoing
  calls as the emulator and Android's source say it will (ADR-007). If it did not, nothing
  would be kept and the rule would never match: the app as it was;
- a call from a withheld number: How It Works says such callers always ring, on Android's
  own documentation (ADR-002); the emulator's console cannot place one;
- a full-screen ad actually closing on Back without Android's notice in the way (it
  appeared, and was closed by the script);
- declining the role prompt;
- the consent form itself (the emulator is a US device, where none is required);
- tapping a day in the chart, touching the hour chart, and switching the period;
- "yesterday" and dated headings in History, and the "change against the week before"
  lines in Statistics (every emulator call was made the same day);
- the Quick Settings tile on a real phone's panel (added to the founder's over adb on 4 Oct, never tapped), and a long press on it;
- Silence mode with real calls on the founder's phone;
- a 1600 or 140 call arriving on an Indian SIM (the emulator's SIM is a US one, so check 11
  dials both with +91; the engine reads the ten-digit and leading-0 forms to the same key);
- TalkBack: the reading order written in `design/prototype/SPEC.md` is set in the
  code (traversal indices on Home, one node per row, chart bars as items) but has not
  been listened to.

## Raw captures

`raw/` is git-ignored: working screenshots and UI dumps from the emulator.
