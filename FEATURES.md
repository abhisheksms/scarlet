# FEATURES — the reference app's features, ours, and the proof

Every feature of the reference app (version 1.4.5, studied 2 October 2026), described
in our own words, with where it was seen, how this app implements it, and what has
actually been shown to work. The observation notes are in `docs/reference/notes.md`.
The look of every screen is the Switchboard design (`design/prototype/`), recreated in
Compose on 4 October 2026.

**Source** is one of:

- **listing** — the app's Google Play page (description, "What's new", screenshots, Data safety), read 2 Oct 2026;
- **screen S-nn** — a screen of the reference app as seen on the founder's phone (numbering as in the notes);
- **system** — what Android's own app-info and role data report about the installed app;
- **brief** — stated in the founder's brief and not independently observed.

**Status** is `done` (built, and shown working as the Verification column says) or
`built` (the code is in and its logic is unit-tested, but it has not yet been exercised
end to end on a device). One row is `not built`, with its reason: F-44, translations.

**Verification** names exactly what was proven:

- a **unit test** (`./gradlew test`: 106 tests, 73 in `core` and 33 in `app`, all passing);
- **emulator check N** — check N of `tools/verify_emulator.py`, which simulates calls
  with `adb emu gsm call` and reads what Android then did (Telecom's event log, the
  system call log, the notification manager);
- **seen on the emulator** — opened and looked at by hand on 4 Oct 2026, on the Switchboard
  screens, at 360 dp, light and dark, 100% and 135% text; screenshots in
  `docs/verification/screens/`.

**Where the emulator evidence stands** (details in `docs/verification/README.md`):

- **All ten checks passed** in the recorded run of 4 Oct 2026
  (`docs/verification/emulator-2026-10-04.md`), on the Switchboard screens: the done line
  (checks 1 to 6) and the four parity checks (scope, repeat caller, milestone, weekly
  report), the last four for the first time.

Code paths are under `app/src/main/kotlin/com/cyanharborstudios/callblock/` unless they
start with `core/`.

## A. The switch and the role

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-01 | One control with three states: off, silence, block | listing; screen S-01 | `Mode` in `core/…/rules/ScreeningSettings.kt`; the lever on Home (`ui/parts/Lever.kt`): three stops, a handle you tap a row for or drag, a lamp that says which stop is in effect | done | emulator checks 1 and 2 choose the modes by tapping the lever's rows; `RuleEngineTest`: *off lets every call ring*. Dragging the handle has not been exercised on a device |
| F-02 | A line under the control stating what is happening now | listing (screenshot); screen S-01 | the display window at the top of Home (`ui/parts/Display.kt`) says one sentence, chosen in the order: the device cannot screen, the role is missing, paused (until when), the mode. The lamp beside the mode says the same: lit, a ring while paused, dark when nothing is filtered | done | seen on the emulator: the mode's sentence (`screens/02-home-block.png`, `16-home-large-dark.png`), paused with Resume (`03-home-paused.png`), the role lost to another app (`05-home-role-missing.png`) and the first-launch legend (`01-home-first.png`) |
| F-03 | Block: a call from a number not in contacts is rejected | listing | rule `unknown-caller` → `BLOCK`; `ScreeningService` answers disallow + reject | done | **emulator check 1**; also the minified release build, by hand; `RuleEngineTest`: *block mode rejects a caller who is not a contact* |
| F-04 | Silence: the call comes through without ringing and shows in the phone's call log | listing | rule `unknown-caller` → `SILENCE`; `setSilenceCall(true)` | done | **emulator check 2** (Telecom: `SKIP_RINGING`; call log: missed); also the release build, by hand |
| F-05 | A contact always rings, and the app never reads contacts | listing; system (no contacts permission requested) | no `READ_CONTACTS`; Android passes a screening app only non-contacts (ADR-002); rule `contact` as the written guard | done | **emulator check 3** (the service is not even invoked); `ManifestPermissionsTest`: *nothing about contacts, the call log, the phone or SMS is requested* |
| F-06 | Becomes the phone's call-screening app through Android's own prompt | system (it holds the role); flow not observed | `ScreeningRole`; the prompt opens when silence or block is chosen without the role; the mode applies only if the user accepts | done | by hand on the emulator in the spike: prompt shown, app chosen, role held (ADR-002 amendment). Declining the prompt was not tried. The scripted checks set the role directly so they can be repeated |
| F-07 | No account, no sign-in | listing | there is none | done | nothing to sign in to: the app has no network code of its own |

## B. Notifications

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-08 | Optional notification for each handled call, switched on the home screen | listing; screen S-01 | `notify/Notifier.handledCall`; a strip with a panel switch on Home; permission asked only when switched on; when Android has notifications switched off, the strip says so and opens Android's settings | done | **emulator check 4** (none while off, one per call while on). Android's permission prompt was seen by hand on 4 Oct, opened from the strip after the permission was revoked, and after two refusals the strip said "Switched off in Android's settings" and offered Android's settings (`screens/06-home-notifications-blocked.png`) |
| F-09 | A summary of handled calls, off / weekly / monthly | screen S-02; system (a report channel exists) | `core/…/stats/ReportPlanner`; `reports/ReportWorker` checks daily; one notification per finished week or month, none for an empty one | done | `ReportPlannerTest` (7); `StatisticsTest`: *a report period counts its first and last day…*; **emulator check 10**: switched on mid-week nothing is sent; with the clock moved past the week's end and the daily job run, one notification arrives with that week's counts |
| F-10 | A notification when a round total is reached | system (a milestone channel exists, at default importance, so it makes a sound); screen S-06 | `Milestones.crossed`; announced once each (10, 25, 50, 100, 250 …). **Deliberately different**: ours is silent, like every notification this app posts | done | `MilestonesTest` (5); **emulator check 9**: one milestone notification at the tenth handled call, none before it |

## C. Options

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-11 | Scope: filter every unknown caller, or only unknown callers from abroad | listing (screenshot); screen S-04 | `Scope`; rule `domestic-out-of-scope`; `PhoneNumbers.isInternational` against the SIM's country | done | `RuleEngineTest` (3 scope tests); `PhoneNumbersTest`; `CallScreenerTest`: *whether a number is international depends on the phone's own country*; **emulator check 7** (a domestic non-contact rings, one from abroad is blocked) |
| F-12 | Pause filtering for a chosen time | listing (screenshot); screen S-04 (its durations were not seen) | `pausedUntilMillis`; rule `paused`; four keys under the lever on Home (15 Min, 1 Hr, 4 Hr, 24 Hr, our choice), one tap from opening the app; while paused, Resume takes their place and the display says until when; resumes by itself | done | **emulator check 6** (paused from Home's 15 Min key: rings while paused, blocked once it ends); Resume seen by hand (`03-home-paused.png`); `RuleEngineTest`: *the pause ends at its end time, not after it* |
| F-13 | Let a number ring when it calls again within a set time | listing (screenshot); screen S-04 (its window was not seen) | rule `repeat-call` over the app's own log; one strip of keys in Options: Off, 5, 15 or 30 Min (our choice) | done | `RuleEngineTest` (5 repeat-call tests); `CallScreenerTest`: *a repeat call is recognised from the app's own log*; **emulator check 8** (blocked the first time, rings the second) |
| F-14 | An allow list of numbers that always ring | screen S-04, S-05 (its editor was not seen) | `allowed_numbers` table; rule `allow-list`; a number is typed into a recessed field in Options and committed with one length key (1 Hr, 24 Hr, Always), or allowed from its sheet; each entry says Always or until when, with a remove button | done | **emulator check 6** (an entry made with one key on the number's sheet, expiring after an hour); `RuleEngineTest` (6 allow-list tests). Typing a number in and committing it for a day was seen by hand on 4 Oct (`07-options.png`); removing an entry was not exercised |

## D. History

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-15 | A list of handled calls, newest first, grouped by day: number, outcome, time | listing; screen S-05 | `handled_calls` table written by `HandledCallRecorder`; `ui/HistoryScreen.kt`: the time, the number, then the outcome as a mark and a word; a later call from the same number that day says which call it is | done | emulator check 5 reads the list; seen on the emulator with a repeat caller's "2nd call" (`screens/08-history.png`, `18-history-large-dark.png`) |
| F-16 | The current day's group is headed "today" rather than a date | listing (screenshot) | headings: today, yesterday, then the date in the phone's locale | done | the "today" heading: seen on the emulator (`screens/08-history.png`). Yesterday and dated headings were not seen: every emulator call was made the same day |
| F-17 | Add a number to the allow list from its history row | screen S-05 | the whole row opens the number's sheet; one length key there allows it and closes the sheet; the row then says "On the allow list" or "Rings until …" | done | **emulator check 6** allows from the sheet with one key; the row then says "Rings until …" (`09-number.png`, `08-history.png`) |
| F-18 | Delete one history row | listing (screenshot); screen S-05 | the Delete key on the number's sheet, beside "This call", away from the allow keys | built | the key is on the sheet (`09-number.png`); not yet pressed on a device |
| F-19 | Delete the whole history | screen S-05 | Delete All in the header, behind a confirmation plate that says the statistics reset too | built | the confirmation seen on the emulator (`10-delete-all.png`) and cancelled; not yet carried through |
| F-20 | Times follow the phone's 12-hour or 24-hour setting | brief; screen S-05 (24-hour seen) | `core/…/time/TimeText`, fed the system setting, re-read whenever the app returns to the front | done | **emulator check 5** (each stored time compared with what the screens show, in both settings); `TimeTextTest` (6), `DayRelationTest` (6) |
| F-21 | Numbers are grouped for reading | listing (screenshot); screen S-05 | `PhoneNumbers.display` | done | `PhoneNumbersTest`; seen on the emulator |

## E. Statistics

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-22 | Home shows today's count and the last seven days' blocked and silenced counts | screen S-01 | `StatsSummary.today`, `.lastSevenDays` on the History and Statistics rows | done | seen on the emulator (`screens/02-home-block.png`); `StatisticsTest` |
| F-23 | The latest milestone reached, with progress to the next | screen S-06 | `Milestones.progress`; a bar with a notch at the last milestone reached (`ui/parts/Counter.kt`) | done | `MilestonesTest`; seen on the emulator before and after the first milestone (`screens/11-statistics.png`, `17-statistics-large-dark.png`) |
| F-24 | All-time blocked and silenced totals | screen S-06 | `StatsSummary.allTime`; the total on drums, like a message register, at the top of Statistics | done | `StatisticsTest`: *totals split blocked from silenced*; seen on the emulator on the drums (`11-statistics.png`) |
| F-25 | An estimate of time saved | screen S-06 | 30 seconds a call, with that assumption written under the figure (gate G8) | done | `StatisticsTest`: *time saved counts every handled call*; seen on the emulator |
| F-26 | Last 7 days: calls handled, busiest hour, change against the week before | screen S-06 | `StatsSummary` | done | `StatisticsTest` (4 tests); count and busiest hour seen on the emulator. The change line was not seen: the emulator had no earlier week to compare with |
| F-27 | Last 30 days: calls handled, change against the 30 days before | screen S-06 | `StatsSummary` | done | `StatisticsTest`: *thirty-day change…*; count seen on the emulator, change line not seen (same reason) |
| F-28 | A 7-day chart, blocked and silenced stacked per day; tapping a day shows its numbers | screen S-06 | `DayChart` in `ui/parts/Charts.kt`: up to 8 calls a day each call is one unit (solid blocked, outlined silenced), past that bars with the day's total above; each day a 48 dp target readable by TalkBack | done | `StatisticsTest`: *the last seven days are today and the six days before it*; the chart seen as units (`11-statistics.png`) and as bars past eight calls (`17-statistics-large-dark.png`). Tapping a day was not exercised |
| F-29 | A period selector (7, 30, 90 days) for the charts below it | screen S-06 | `statsPeriodDays`; three latching keys | done | `StatisticsTest`: *weekday and hour charts cover only the chosen period*; the selector seen on the emulator. Switching the period was not exercised |
| F-30 | Calls by day of the week, naming the busiest day | screen S-06 | `StatsSummary.byWeekday`; `WeekdayChart`, each bar with its count above; the week starts where the phone's region starts it | done | same test; `screens/12-statistics-charts.png` |
| F-31 | Calls by hour of the day, naming the busiest hour | screen S-06 | `StatsSummary.byHour`; `HourChart`: touch or drag across it to read an hour; labels follow the 12/24-hour setting | done | same test; `screens/12-statistics-charts.png`. Touching the chart was not exercised |
| F-32 | The most frequent numbers, ranked, with counts | screen S-06 | `StatsSummary.topNumbers` (ten at most) | done | `StatisticsTest` (2 top-number tests); seen on the emulator |
| F-33 | Details for one number: blocked, silenced, total, first and last time handled | screen S-07 | `Statistics.detailsFor`; `ui/NumberDetailsSheet.kt`, opened from Statistics and from History | done | emulator check 5 opens it from History and reads this call's line by its tag; `StatisticsTest`: *details for a number…*; `screens/09-number.png`. Opening it from the Statistics list was not exercised |
| F-34 | Share the statistics as a short text | screen S-06 | system share sheet; counts and the store link, never a number | built | not yet exercised on a device |

## F. Settings and the rest

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-35 | A link to the privacy policy | screen S-02; listing | Settings row. The page itself does not exist yet (launch task) | built | the row is on the screen (`screens/13-settings.png`); it was not tapped |
| F-36 | An open-source licences screen | screen S-02, S-03 | `ui/LicencesScreen.kt`, with the typeface and its licence link as the Open Font License requires | done | opened on the emulator (`screens/14-licences.png`); its two links were not tapped |
| F-37 | A way to contact the developer | screen S-02; listing | Settings row, opens an email to `contact@cyanharborstudios.com` | built | the row is on the screen; it was not tapped |
| F-38 | Share the app's store link | screen S-01 | Settings row (moved off the home screen) | built | the row is on the screen; it was not tapped |
| F-39 | A link to the app's store page for a review | screen S-01 | Settings row; opens the Play Store app, else the web page | built | the row is on the screen; it was not tapped. There is no store page until the app is published |
| F-40 | Light and dark themes following the system | listing (light screenshots); phone (dark) | `ui/theme/Theme.kt`: the Switchboard palette by Material 3 role, fixed, light and dark; Hanken Grotesk throughout | done | seen on the emulator in both (`15-home-dark.png`), also at 360 dp with 135% text (`16-home-large-dark.png`, `17-statistics-large-dark.png`, `18-history-large-dark.png`) |
| F-44 | Offered in many languages | listing: its name and short description are translated in all eleven other languages tried. Its own screens were not seen in another language (the phone is set to English) | English only. All user-facing text is already in `res/values/strings.xml`, so a translation is one new resource file per language | **not built**: found on 2 Oct after the build; which languages, and whether for the first release, is the founder's call (gate G5) | none |

## G. Ads

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-41 | An ad on every screen | screens S-01, S-02, S-04, S-05, S-06 | one banner under every screen, in a recessed tray that has its full height from the first frame, with a sill that names the ad once it has loaded (`ads/BannerSlot.kt`). **Deliberately different**: the reference's ads load late and move the rows | done (test ids) | seen on the emulator: Google's test banner loads in the tray on the home, options, history, statistics and settings screens, and the sill then says "Advertisement"; `LaunchGateTest` (5) pins the ids to Google's test publisher |
| F-42 | A full-screen ad around History and Statistics | screens S-05, S-06 | at most one every three minutes, when the user *leaves* those screens (`ads/AdPlacements.kt`). **Deliberately different**: the reference shows it on the tap that opens them; that behaviour is one constant away (gate G6) | done (test ids) | `AdPlacementsTest` (7) covers when one is wanted. On 4 Oct the test interstitial appeared on the emulator after leaving Statistics and was closed by the verification script |
| F-43 | Consent flow where the law requires it | brief | UMP runs before the ads SDK starts; Settings shows Privacy Choices when UMP requires it (`ads/AdsController.kt`) | built | the emulator is a US device, where no form is required: the path "not required, so ads start" was seen. **The form itself was not seen**; that needs a test device set to an EEA geography |

## Ours, not in the reference

| ID | Addition | Why | Verification |
|---|---|---|---|
| A-01 | A build stamp in Settings (`v0.1.0 · date`) | studio rule: a stale install can fake a bug | seen on the emulator (`screens/13-settings.png`) |
| A-02 | The display says "Nothing is being filtered." when the role has been lost to another app, with one key, Set As Screening App | otherwise the switch would lie | seen on the emulator after the role was taken away (`screens/05-home-role-missing.png`); the key opened Android's role prompt |
| A-03 | Tapping a history row opens that number's details | in the reference the row does nothing and details are reachable only from statistics | emulator check 5 |
| A-04 | The handled-call notification is silent and hides the number on a locked screen | the app exists to reduce interruptions; a caller's number is personal data | the notification posts (emulator check 4); the locked-screen view was not looked at |
| A-07 | The stopped-call notification carries one action on an unlocked phone, Allow For 1 Hour | the courier case without opening the app (the design's decision 9; `notify/AllowNumberReceiver.kt`) | built; not yet exercised on a device |
| A-05 | An allow entry can expire (1 hour, 24 hours) | the brief's "temporary allow"; see `NOTES.md` N-03 | emulator check 6 |
| A-06 | Layout holds at 360 dp with 135% text | the founder's own phone setting; the reference's chart legend breaks there | seen on the emulator at those settings, in the dark theme: home, options, history, number details, statistics and settings |

## Checked, and absent in the reference

So parity does not call for them. Asked of Android on the founder's phone on 2 Oct
(`docs/reference/notes.md`, session 1b): no home-screen widget, no Quick Settings tile,
no launcher shortcuts, no links that open the app, no share target. From the listing
and every screen: no purchase of any kind.

## Deliberate differences from the reference

One row was left out: translations (F-44), found after the build. Three behaviours were
changed on purpose (F-41, F-42: where and when ads appear; F-10: the milestone
notification is silent) and two placements were moved (F-38, F-39, from the home
screen to Settings). The reasons are in ADR-006 and `NOTES.md`.

## Reference behaviour that could not be confirmed

These need the reference app's own switches to be flipped, or a real call, on the
founder's phone. Session 1 was read-only and the founder's go-ahead for the rest is
still open (`docs/reference/notes.md`, "Not observed"). Where a detail was unknown,
ours is our own design, named above:

- the durations offered by its pause, the window offered for repeat callers, and its
  allow-list editor (F-12, F-13, F-14);
- whether its allow entries can be temporary, and so what the brief's "temporary
  allow" refers to (A-05);
- whether its delete actions ask for confirmation (F-18, F-19);
- its status line in silence mode, and any prompt on switching modes (F-02, F-06);
- what its three notifications say, and whether the per-call one makes a sound (F-08,
  F-09, F-10; the report is silent and the milestone is not, by their channels);
- whether its full-screen ads are capped (F-42).

Three more cannot be seen on that phone at all, or not without leaving the app:

- **its first-run flow** (any introduction, the order of its prompts, when the first ad
  appears). The app was already set up, and a fresh start would mean clearing its data
  (F-06);
- **whether its own screens are translated**, and into which languages (F-44);
- **whether Play holds a newer build** than the 1.4.5 of 21 Sep that was studied: the
  listing says "updated on 2 Oct".
