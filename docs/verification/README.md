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
tools/verify_emulator.py          # about thirty-five minutes for all twenty-six checks; check 23 restarts the emulator
tools/verify_emulator.py --only 16   # one check again, on the app as it is; no report
                                     # (6 and 13 need a stopped call in the history; 18 removes the settings
                                     # file; 23 restarts the emulator)
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
| 19 | A timer holds Block for a while, then the lever's own stop is back | **passed** |
| 20 | The schedule blocks in the hours it was given, and a lever move inside them holds until they end | **passed** |
| 21 | Each plan holds what it says: ads on Free only, the timer and the schedule on Pro only | **passed** |
| 22 | A number the user called rings when it calls back, for a day | **passed** |
| 23 | After a call to an emergency number, every call rings for a day | **passed** |
| 24 | A number rule always blocks, or always rings, the numbers that start its way; the longer start wins; Pro only | **passed** |
| 25 | The lever's handle stays under the finger, seats at the nearest stop when let go, and goes on to the next when flicked | **passed** |
| 26 | On Statistics a day and a period can be chosen, the hour chart follows a finger sideways, and the page still scrolls from it | **passed** |

### Which build the results are for

- **The table is the run recorded in [`emulator-2026-10-10.md`](emulator-2026-10-10.md)** (the icons build of the words round, below; the words build's own run is [`emulator-2026-10-10-words.md`](emulator-2026-10-10-words.md), and the run before both, on the build with the lever and the hour chart fixed, is [`emulator-2026-10-09.md`](emulator-2026-10-09.md)). **The earlier record:**
  one run from a wiped app, 34 minutes with the emulator's restart, on a clean build of
  `main` at commit `cac5ee3`, the build in which the lever's handle and the hour chart
  follow a finger. **All twenty-six passed.** It is the file that went on the founder's
  phone that night, byte for byte; he asked for it while the first run was at check 7.
  It is the third run of that night, and the two before it each found a check that
  waited for the wrong thing, on an emulator that had been left asleep for three days:
  - **The first run: 25 of 26, check 13 failed.** After a tap on Contact the window in
    front was still the app's. Android's log showed that the app had started the mail
    app, and that the mail app, which has no account on the emulator, closed itself
    0.14 seconds later. Check 13 now goes by Android's own record of what it started
    for the app, and writes the window in front beside it. Broken two ways on purpose
    (the Contact row starting nothing, Share starting nothing) it failed each time.
  - **The second run: checks 1 to 22 passed, 23 failed, and the script stopped.** After
    check 23's restart the two calls drew no decision, and Telecom had no record of
    them: they were placed before the phone service was in service, on a system still
    busy starting up, where the app then did not open in time for check 24. The
    restart used to wait a fixed 15 and 10 seconds. It now waits for the device to go
    away, to come back, for the phone service to be in service, and for Android to
    finish its start-up broadcasts. Check 23 then passed twice by itself.
- **The run of 6 October is [`emulator-2026-10-06.md`](emulator-2026-10-06.md):**
  one run from a wiped app, 31 minutes with the emulator's restart, on a clean build of
  `main` at commit `5886181`, the build with the pause's key named for what it brings back
  and the planned prices in a test build. **All twenty-four passed.** It is the build that
  went on the founder's phone a minute later.
- **The run of 5 October is [`emulator-2026-10-05.md`](emulator-2026-10-05.md):**
  one run from a wiped app, late on 5 October, 31 minutes with the emulator's restart, on a
  clean build of `main` at commit `48d5f7b`. That build holds everything built that day:
  the tutorial, the three plans and Pro's three features, call-backs, the pause after an
  emergency call, and buying through Google Play. **All twenty-four passed.** In this run
  the ringer for check 22's call back had not started within twelve seconds, which is
  Telecom's own wait described below; the check goes by Telecom's record that the call was
  set ringing, and reports the ringer beside it. The file replaces that afternoon's report
  of the same name, the eighteen-check run on commit `ee13905`, which is in the
  repository's history. The entries below say what each later check does and what was
  learnt on the way to it; where one says a report was still to be recorded, this is that
  report.
- **Checks 13 and 18 came that afternoon, with the founder's first notes** (the
  eighteen-check run on commit `ee13905`). Check 18 removes the app's settings
  file, which is what a first launch has, and sees How It Works open by itself, close on
  one press of Back, stay closed on the next launch, and open again from Settings, where
  its Done key goes back to Settings. Check 13 changed then: Contact is tapped on About, and
  for each row whose page does not exist yet it reads the switch in `ui/Links.kt` and
  checks that the row is not on screen. One thing learnt on the way: the first attempt
  that day stopped inside check 16, which sets a PIN to read the locked screen and clears
  it again; Android kept its swipe lock up for a moment after the PIN was gone, one
  `wm dismiss-keyguard` missed it, and the app then started behind the lock screen. The
  script now asks, looks at what Android reports and asks again (`unlock`). Checks 1 to 15
  had passed in that attempt; the run recorded that afternoon was the complete one after
  the fix.
- **Checks 19 to 21 came that evening, with the freemium build.** All twenty-one passed
  then in one run from a wiped app, 23 minutes, on a build of that change's own code. What
  the three check: 19 sets a timer at
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
  on that change's final build.
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
- **Check 24 came with number rules** and passed run by itself. On Pro with the lever at
  Silence it types two rules into Options (the emulator's SIM is a US one, so the digits are
  read as +1): numbers that start 555 111, Always Block; the one number 555 111 0026, Always
  Ring. A number that starts the blocked way is blocked by the rule `number-rule`; the one
  number with the longer rule rings; a number that starts another way is silenced as
  before. On Free the first number is silenced like any other and the Number Rules row
  opens Plans; back on Pro it is blocked again; and with both rules removed by their rows'
  buttons it is silenced.
- **Check 25 came with the lever's handle** (`NOTES.md` N-51) and passed run by itself. It
  puts a finger on the handle with `input motionevent`, which leaves it down until told
  to lift, moves it 0.4 of a row and reads the screen: the handle's middle must be within
  2 dp of the finger. Let go there, the handle is back on Off. Carried 0.7 of a row and
  let go, it is on Silence and a stranger's call is silenced. Flicked 0.4 of a row in
  40 ms, still nearer the stop it left, it goes on to Block and the call is blocked; a
  flick up brings it back. The rows' places are read afresh before each step, because
  the tiles above the lever grow once there are calls to count. Where the handle was in
  every frame, before the change and after, is in
  [`lever-handle-2026-10-06.md`](lever-handle-2026-10-06.md).
- **Check 26 came with the hour chart** (`NOTES.md` N-52) and passed run by itself. It makes
  one call so that Statistics has charts to draw, then reads which day, period key and
  hour are chosen from their tags. Statistics opens on today; a tap on the third day
  chooses it; the 90-day key takes over and the earlier one is put back. On the hour
  chart a finger put down on the third hour chooses it, moved sideways to the sixteenth
  chooses that one, and lifted leaves it chosen; a tap on it lets it go. A swipe 500 px
  down the screen from the chart moves the page and chooses no hour.
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
- **Number rules on screen** (5 Oct), at 360 dp: Options on Pro with no rules, with the
  field being typed in, the caption saying the start as the app reads it and the two keys
  over the keyboard, and with three rules listed; Home's summary of Options counting them;
  the one row with the word Pro on Free. Light at 100% text; the rules listed and the Free
  row also in dark at 135%.
- **The plans screen with buying wired** (5 Oct), at 360 dp. On the emulator, which has no
  Google account: No Ads and Pro each say that Google Play could not be reached, there is no
  key to buy, and Restore Purchases says the same when pressed. No crash; Google's library
  logged that billing is not supported on the device. With made-up prices put in by a
  change that was then thrown away (nothing of it is in the repository): on Free, each paid
  plan shows its price and one key; on No Ads, Pro shows the upgrade's price with the
  sentence about the difference; light at 100% text and dark at 135%. **A purchase has not
  been made anywhere.**
- **The screens after the freemium round** (late on 5 Oct), at 360 dp, on the build the
  recorded run was made on, with made-up numbers. Retaken in [`screens/`](screens/): Home
  on a first launch, at Block, paused, at Off, with the role lost and with notifications
  refused (`01` to `06`), Options (`07`, and `19` and `20` in dark), Settings (`13`, `28`),
  Home in dark and in dark with large text (`15`, `16`), and How It Works (`23` to `26`).
  New: Home on Pro with the Automatic section (`29`), the timer's sheet (`30`), Home while
  a timer holds Silence over the lever's Block (`31`), the schedule with nights at Block
  and working hours at Silence (`32`), Home in an hour the schedule has set (`33`), Plans
  as the emulator shows it, with Google Play not reachable (`34`), and number rules listed
  and being typed (`35`, `36`). The contact sheet was made again from all thirty-six.
  History, the number's sheet, the delete dialog, Statistics, the licences, the tile and
  About did not change and were not retaken.
- **The founder's two notes of 6 Oct**, at 360 dp, in dark with large text as on his phone
  and in light: Home paused from Block with its key reading Resume Blocking, and paused
  from Silence reading Resume Silencing, on one line each; pressing the key brings the
  pause keys back. The plans screen of a test build with the planned prices under No Ads
  and Pro on Free, and under Pro alone on No Ads with the sentence about the difference;
  no key to buy. `03` and `34` in [`screens/`](screens/) were retaken and the contact sheet
  made again. Checks 1, 6, 12 and 21 passed on that build before it merged.
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

- **The lever without the role, and a locked lever**, on 6 Oct: Android's role prompt
  declined twice (after a drag to Block, and after a tap on it) with the handle back on Off
  each time, and accepted once with the lever then at Silence; a throwaway build with the
  lever locked, whose handle gives 7 dp and no more. The whole account is in
  [`lever-handle-2026-10-06.md`](lever-handle-2026-10-06.md).

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

- **The freemium build, installed over the earlier one (5 Oct 2026, 23:59).** At his ask
  ("phone is plugged in, install it"), the build that had passed all twenty-four emulator
  checks 37 minutes before went on his phone: the same file, byte for byte. Before: the
  build of 18:44 that day, and the app holding the call-screening role. The install
  returned in 16 seconds. After: updated at 23:59:46, the role still with the app. Opened
  once over adb with the phone unlocked: it came to the front with no crash, the screen
  showing was Home (read by its tags alone: the lever's three stops, the four pause keys,
  the tiles, the ad tray), and the process was still up eleven seconds later. Home reads
  the stopped calls as it opens, so the database's step from version 1 to 2 ran on his
  own data without a failure. Nothing was tapped, and nothing of his data was read: not
  the lever's position, not a count, not a number (`NOTES.md` N-48).

- **The build with his two notes acted on (6 Oct 2026, 01:04).** At his ask ("install
  it"), once all twenty-four emulator checks had passed on that same file. Before: the
  build of 23:59 the night before, the app holding the call-screening role. The install
  returned in 9 seconds. After: updated at 01:04:08, the role still with the app. Opened
  once over adb with the phone unlocked: Home, no crash, the process still up eleven
  seconds later. A pause he had started was still running, and the key that ends it read
  "Resume Blocking": that one label, the app's own wording, was read, and otherwise only
  tags. The plans screen with its planned prices was not opened on his phone
  (`NOTES.md` N-50).

- **The build with the lever's handle and the hour chart fixed (9 Oct 2026, 00:53).** He
  asked why an emulator was needed with his phone on the cable and, offered the install
  at once or after the run, chose at once. The full run was then at check 7 of 26; the
  checks for the changed parts had passed before the merge. Before: the build of 6 Oct,
  01:04, the app holding the call-screening role. The install returned in 14 seconds.
  After: updated at 00:53:17, the role still with the app. Opened once over adb: Home, no
  crash, the new handle's tag on screen, the process still up eight seconds later. The
  app's own frame counter was set to zero then, and read once he had used the app for a
  while: 5,639 frames, 173 of them late (3.07%), half drawn within 12 ms and nine in ten
  within 16 ms. Only the app's tags and that counter were read. The file is the one both
  runs of that night tested, byte for byte (`NOTES.md` N-53). His word on the lever once
  he had tried it: "yeah it's very smooth" (N-54).

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
- **a purchase**: buying No Ads, Pro and the upgrade, a payment left waiting, a refund,
  and Restore Purchases after the app's data is cleared. All of it needs the app and its
  three products in Play Console and a copy installed from Play by a licence tester
  (`TASKS.md`, "For launch");
- **call-backs on a real phone**: whether the founder's phone shows the app his outgoing
  calls as the emulator and Android's source say it will (ADR-007). If it did not, nothing
  would be kept and the rule would never match: the app as it was;
- a call from a withheld number: How It Works says such callers always ring, on Android's
  own documentation (ADR-002); the emulator's console cannot place one;
- a full-screen ad actually closing on Back without Android's notice in the way (it
  appeared, and was closed by the script);
- the consent form itself (the emulator is a US device, where none is required);
- "yesterday" and dated headings in History, and the "change against the week before"
  lines in Statistics (every emulator call was made the same day);
- the Quick Settings tile on a real phone's panel (added to the founder's over adb on 4 Oct, never tapped), and a long press on it;
- Silence mode with real calls on the founder's phone;
- a 1600 or 140 call arriving on an Indian SIM (the emulator's SIM is a US one, so check 11
  dials both with +91; the engine reads the ten-digit and leading-0 forms to the same key);
- TalkBack: the reading order written in `design/prototype/SPEC.md` is set in the
  code (traversal indices on Home, one node per row, chart bars as items) but has not
  been listened to.

## The words round, 10 October 2026

Every string on the screens was rewritten on the night of 9 October to the founder's rule
for all his apps (`NOTES.md` N-56; falcon `FOUNDER_TASTE.md` §14 and the `ui-copy` skill):
the display shows a state, rows a noun and a state word, the explaining in How It Works,
the privacy line gone. No behaviour changed, so the checks are the proof that nothing
moved under the words:

- [`emulator-2026-10-10-words.md`](emulator-2026-10-10-words.md): all 26 checks passed, one
  run from a wiped app on the worktree's build of the new string table (the record names
  `5b791ae`, the commit it was branched from; the strings and the four screen files were
  uncommitted). After the run, two strings changed to Android's own words for the
  call-screening role ("default call screening app", "Set Default"); the checks do not read
  them.
- [`emulator-2026-10-10.md`](emulator-2026-10-10.md): the same 26 checks on the build with
  his three notes in (`NOTES.md` N-57: "Not blocking", the empty space closed, an icon beside
  every row's word). All 26 passed, one run from a wiped app, 34 minutes with the
  emulator's restart; the record again names `5b791ae`, the branch point, as the icons were
  still uncommitted. The committed build differs from that one by two strings he sent after
  it ("No blocks applied on any call" on the display at Off; "Always block numbers starting
  with 140"); on it, checks 1, 2 and 11 (the lever's modes and the 140 row) were re-run on a
  wiped app and passed, and every screen below was retaken. Three more strings then took
  the words of Android's own role dialog as the emulator shows it ("caller ID & spam app",
  "Set as default"; `screens/37-android-role-dialog.png`): on that build, the committed one,
  check 1 was re-run on a wiped app and passed, and the five screens those strings touch
  (`05`, `23` to `26`) were retaken.
- Every screen in [`screens/`](screens/) was retaken on the final build at 360 dp, light
  and dark, 100% and 135% text, with made-up numbers, and the contact sheet remade. Three
  new shots: `37-android-role-dialog` (Android's own dialog, so the app's words can be
  checked against it), `38-options-allowed-numbers` (the panel with a number typed) and
  `39-number-allowed` (an allowed number's sheet).

## Raw captures

`raw/` is git-ignored: working screenshots and UI dumps from the emulator.
