# FEATURES — the reference app's features, ours, and the proof

Every feature of the reference app (version 1.4.5, studied 2 October 2026), described
in our own words, with where it was seen, how this app implements it, and what has
actually been shown to work. The observation notes are in `docs/reference/notes.md`.

**Source** is one of:

- **listing** — the app's Google Play page (description, "What's new", screenshots, Data safety), read 2 Oct 2026;
- **screen S-nn** — a screen of the reference app as seen on the founder's phone (numbering as in the notes);
- **system** — what Android's own app-info and role data report about the installed app;
- **brief** — stated in the founder's brief and not independently observed.

**Status** is `done` (built, and shown working as the Verification column says) or
`built` (the code is in and its logic is unit-tested, but it has not yet been exercised
end to end on a device). Nothing in the inventory was left unbuilt.

**Verification** names exactly what was proven:

- a **unit test** (`./gradlew test`: 100 tests, 67 in `core` and 33 in `app`, all passing);
- **emulator check N** — check N of `tools/verify_emulator.py`, which simulates calls
  with `adb emu gsm call` and reads what Android then did (Telecom's event log, the
  system call log, the notification manager);
- **seen on the emulator** — opened and looked at by hand on 2 Oct 2026; screenshots in
  `docs/verification/screens/`.

**Where the emulator evidence stands** (details in `docs/verification/README.md`):

- Checks 1 to 6, the done line, **all passed** in the recorded run of 2 Oct 2026
  (`docs/verification/emulator-2026-10-02.md`). Three small edits came after that run
  (the compact mode switch, a one-write report setting, extra test tags). A re-run on
  the final build passed checks 1 to 5 and was stopped, at the founder's request,
  while check 6 was running.
- Checks 7 to 10 (scope, repeat caller, milestone, weekly report) are scripted but
  **have not completed a run**. The rows that depend on them say `built`.

Code paths are under `app/src/main/kotlin/com/cyanharborstudios/callblock/` unless they
start with `core/`.

## A. The switch and the role

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-01 | One control with three states: off, silence, block | listing; screen S-01 | `Mode` in `core/…/rules/ScreeningSettings.kt`; `ModeSelector` in `ui/HomeScreen.kt` | done | emulator checks 1 and 2 choose the modes by tapping the control; `RuleEngineTest`: *off lets every call ring* |
| F-02 | A line under the control stating what is happening now | listing (screenshot); screen S-01 | the chosen mode shows its one-line description; a notice appears when filtering is paused ("until HH:mm") or the role is missing | done | seen on the emulator: the mode's description (`screens/01-home.png`, `08-home-large-dark.png`) and the paused notice with its Resume button. The role-missing notice has not been exercised |
| F-03 | Block: a call from a number not in contacts is rejected | listing | rule `unknown-caller` → `BLOCK`; `ScreeningService` answers disallow + reject | done | **emulator check 1**; also the minified release build, by hand; `RuleEngineTest`: *block mode rejects a caller who is not a contact* |
| F-04 | Silence: the call comes through without ringing and shows in the phone's call log | listing | rule `unknown-caller` → `SILENCE`; `setSilenceCall(true)` | done | **emulator check 2** (Telecom: `SKIP_RINGING`; call log: missed); also the release build, by hand |
| F-05 | A contact always rings, and the app never reads contacts | listing; system (no contacts permission requested) | no `READ_CONTACTS`; Android passes a screening app only non-contacts (ADR-002); rule `contact` as the written guard | done | **emulator check 3** (the service is not even invoked); `ManifestPermissionsTest`: *nothing about contacts, the call log, the phone or SMS is requested* |
| F-06 | Becomes the phone's call-screening app through Android's own prompt | system (it holds the role); flow not observed | `ScreeningRole`; the prompt opens when silence or block is chosen without the role; the mode applies only if the user accepts | done | by hand on the emulator in the spike: prompt shown, app chosen, role held (ADR-002 amendment). Declining the prompt was not tried. The scripted checks set the role directly so they can be repeated |
| F-07 | No account, no sign-in | listing | there is none | done | nothing to sign in to: the app has no network code of its own |

## B. Notifications

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-08 | Optional notification for each handled call, switched on the home screen | listing; screen S-01 | `notify/Notifier.handledCall`; switch on home; permission asked only when switched on | done | **emulator check 4** (none while off, one per call while on). The permission prompt itself was not seen: holding the role grants the permission |
| F-09 | A summary of handled calls, off / weekly / monthly | screen S-02; system (a report channel exists) | `core/…/stats/ReportPlanner`; `reports/ReportWorker` checks daily; one notification per finished week or month, none for an empty one | built | `ReportPlannerTest` (7); `StatisticsTest`: *a report period counts its first and last day…*. The scheduled job has not been run on a device (emulator check 10, not yet completed) |
| F-10 | A notification when a round total is reached | system (a milestone channel exists); screen S-06 | `Milestones.crossed`; announced once each (10, 25, 50, 100, 250 …) | built | `MilestonesTest` (5). The notification has not been seen on a device (emulator check 9, not yet completed) |

## C. Options

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-11 | Scope: filter every unknown caller, or only unknown callers from abroad | listing (screenshot); screen S-04 | `Scope`; rule `domestic-out-of-scope`; `PhoneNumbers.isInternational` against the SIM's country | built | `RuleEngineTest` (3 scope tests); `PhoneNumbersTest`; `CallScreenerTest`: *whether a number is international depends on the phone's own country*. Not yet shown with a call (emulator check 7, not yet completed) |
| F-12 | Pause filtering for a chosen time | listing (screenshot); screen S-04 (its durations were not seen) | `pausedUntilMillis`; rule `paused`; 15 minutes, 1, 4 or 24 hours (our choice); resumes by itself | done | **emulator check 6** (rings while paused, blocked once it ends); `RuleEngineTest`: *the pause ends at its end time, not after it* |
| F-13 | Let a number ring when it calls again within a set time | listing (screenshot); screen S-04 (its window was not seen) | rule `repeat-call` over the app's own log; 5, 15 or 30 minutes (our choice) | built | `RuleEngineTest` (5 repeat-call tests); `CallScreenerTest`: *a repeat call is recognised from the app's own log*. Not yet shown with a call (emulator check 8, not yet completed) |
| F-14 | An allow list of numbers that always ring | screen S-04, S-05 (its editor was not seen) | `allowed_numbers` table; rule `allow-list`; add by typing or from history; an entry is permanent or expires after 1 or 24 hours | done | **emulator check 6** (an entry made from a number's details, expiring after an hour); `RuleEngineTest` (6 allow-list tests). Typing a number in, and removing an entry, were not exercised |

## D. History

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-15 | A list of handled calls, newest first, grouped by day: number, outcome, time | listing; screen S-05 | `handled_calls` table written by `HandledCallRecorder`; `ui/HistoryScreen.kt` | done | emulator check 5 reads the list; seen on the emulator (`screens/02-history.png`) |
| F-16 | The current day's group is headed "today" rather than a date | listing (screenshot) | headings: today, yesterday, then the date in the phone's locale | done | the "today" heading: seen on the emulator (`screens/02-history.png`). Yesterday and dated headings were not seen: every emulator call was made the same day |
| F-17 | Add a number to the allow list from its history row | screen S-05 | the row's menu, or the button in the number's details | done | **emulator check 6** uses the button in the number's details. The row menu's own item was not exercised |
| F-18 | Delete one history row | listing (screenshot); screen S-05 | the row's menu | built | not yet exercised on a device |
| F-19 | Delete the whole history | screen S-05 | top-bar action, behind a confirmation that says the statistics reset too | built | not yet exercised on a device |
| F-20 | Times follow the phone's 12-hour or 24-hour setting | brief; screen S-05 (24-hour seen) | `core/…/time/TimeText`, fed the system setting, re-read whenever the app returns to the front | done | **emulator check 5** (each stored time compared with what the screens show, in both settings); `TimeTextTest` (6) |
| F-21 | Numbers are grouped for reading | listing (screenshot); screen S-05 | `PhoneNumbers.display` | done | `PhoneNumbersTest`; seen on the emulator |

## E. Statistics

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-22 | Home shows today's count and the last seven days' blocked and silenced counts | screen S-01 | `StatsSummary.today`, `.lastSevenDays` on the History and Statistics rows | done | seen on the emulator (`screens/01-home.png`); `StatisticsTest` |
| F-23 | The latest milestone reached, with progress to the next | screen S-06 | `Milestones.progress` | done | `MilestonesTest`; seen on the emulator (`screens/04-stats.png`) |
| F-24 | All-time blocked and silenced totals | screen S-06 | `StatsSummary.allTime` | done | `StatisticsTest`: *totals split blocked from silenced*; seen on the emulator |
| F-25 | An estimate of time saved | screen S-06 | 30 seconds a call, with that assumption written under the figure (gate G8) | done | `StatisticsTest`: *time saved counts every handled call*; seen on the emulator |
| F-26 | Last 7 days: calls handled, busiest hour, change against the week before | screen S-06 | `StatsSummary` | done | `StatisticsTest` (4 tests); count and busiest hour seen on the emulator. The change line was not seen: the emulator had no earlier week to compare with |
| F-27 | Last 30 days: calls handled, change against the 30 days before | screen S-06 | `StatsSummary` | done | `StatisticsTest`: *thirty-day change…*; count seen on the emulator, change line not seen (same reason) |
| F-28 | A 7-day chart, blocked and silenced stacked per day; tapping a day shows its numbers | screen S-06 | `ui/BarChart.kt`: bars are real elements, each tappable and readable by TalkBack | done | `StatisticsTest`: *the last seven days are today and the six days before it*; the chart seen on the emulator (`screens/04-stats.png`). Tapping a bar was not exercised |
| F-29 | A period selector (7, 30, 90 days) for the charts below it | screen S-06 | `statsPeriodDays` | done | `StatisticsTest`: *weekday and hour charts cover only the chosen period*; the selector seen on the emulator. Switching the period was not exercised |
| F-30 | Calls by day of the week, naming the busiest day | screen S-06 | `StatsSummary.byWeekday`; the week starts where the phone's region starts it | done | same test; `screens/05-stats-charts.png` |
| F-31 | Calls by hour of the day, naming the busiest hour | screen S-06 | `StatsSummary.byHour`; hour labels follow the 12/24-hour setting | done | same test; `screens/05-stats-charts.png` |
| F-32 | The most frequent numbers, ranked, with counts | screen S-06 | `StatsSummary.topNumbers` (ten at most) | done | `StatisticsTest` (2 top-number tests); seen on the emulator |
| F-33 | Details for one number: blocked, silenced, total, first and last time handled | screen S-07 | `Statistics.detailsFor`; `ui/NumberDetailsSheet.kt`, opened from Statistics and from History | done | emulator check 5 opens it from History and reads it; `StatisticsTest`: *details for a number…*; `screens/03-number.png`. Opening it from the Statistics list was not exercised |
| F-34 | Share the statistics as a short text | screen S-06 | system share sheet; counts and the store link, never a number | built | not yet exercised on a device |

## F. Settings and the rest

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-35 | A link to the privacy policy | screen S-02; listing | Settings row. The page itself does not exist yet (launch task) | built | the row is on the screen (`screens/07-settings.png`); it was not tapped |
| F-36 | An open-source licences screen | screen S-02, S-03 | `ui/LicencesScreen.kt` | built | the row is on the screen; the licences screen was not opened on a device |
| F-37 | A way to contact the developer | screen S-02; listing | Settings row, opens an email to `contact@cyanharborstudios.com` | built | the row is on the screen; it was not tapped |
| F-38 | Share the app's store link | screen S-01 | Settings row (moved off the home screen) | built | the row is on the screen; it was not tapped |
| F-39 | A link to the app's store page for a review | screen S-01 | Settings row; opens the Play Store app, else the web page | built | the row is on the screen; it was not tapped. There is no store page until the app is published |
| F-40 | Light and dark themes following the system | listing (light screenshots); phone (dark) | `ui/theme/Theme.kt`, a fixed teal palette of our own | done | seen on the emulator in both, also at 360 dp with 135% text |

## G. Ads

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-41 | An ad on every screen | screens S-01, S-02, S-04, S-05, S-06 | one banner under every screen, in a slot that has its full height from the first frame, so nothing shifts when it loads (`ads/BannerSlot.kt`). **Deliberately different**: the reference's ads load late and move the rows | done (test ids) | seen on the emulator: Google's test banner loads in the slot on the home, options, history, statistics and settings screens; `LaunchGateTest` (5) pins the ids to Google's test publisher |
| F-42 | A full-screen ad around History and Statistics | screens S-05, S-06 | at most one every three minutes, when the user *leaves* those screens (`ads/AdPlacements.kt`). **Deliberately different**: the reference shows it on the tap that opens them; that behaviour is one constant away (gate G6) | built (test ids) | `AdPlacementsTest` (7) covers when one is wanted. A full-screen ad has not yet been seen to appear on a device |
| F-43 | Consent flow where the law requires it | brief | UMP runs before the ads SDK starts; Settings shows Privacy Choices when UMP requires it (`ads/AdsController.kt`) | built | the emulator is a US device, where no form is required: the path "not required, so ads start" was seen. **The form itself was not seen**; that needs a test device set to an EEA geography |

## Ours, not in the reference

| ID | Addition | Why | Verification |
|---|---|---|---|
| A-01 | A build stamp in Settings (`v0.1.0 · date`) | studio rule: a stale install can fake a bug | seen on the emulator (`screens/07-settings.png`) |
| A-02 | The home screen says when the role has been lost to another app, with one button to take it back | otherwise the switch would lie | not yet exercised on a device |
| A-03 | Tapping a history row opens that number's details | in the reference the row does nothing and details are reachable only from statistics | emulator check 5 |
| A-04 | The handled-call notification is silent and hides the number on a locked screen | the app exists to reduce interruptions; a caller's number is personal data | the notification posts (emulator check 4); the locked-screen view was not looked at |
| A-05 | An allow entry can expire (1 hour, 24 hours) | the brief's "temporary allow"; see `NOTES.md` N-03 | emulator check 6 |
| A-06 | Layout holds at 360 dp with 135% text | the founder's own phone setting; the reference's chart legend breaks there | seen on the emulator at those settings: home (light and dark), options, history, number details and statistics (dark). Settings and licences were not looked at there |

## Deliberate differences from the reference

Nothing in the inventory was left out. Two behaviours were changed on purpose (F-41,
F-42: where and when ads appear) and two placements were moved (F-38, F-39, from the
home screen to Settings). The reasons are in ADR-006 and `NOTES.md`.

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
- what its three notifications say and whether they make a sound (F-08, F-09, F-10);
- whether its full-screen ads are capped (F-42).
