# FEATURES — the reference app's features, ours, and the proof

Every feature of the reference app (version 1.4.5, studied 2 October 2026), described
in our own words, with where it was seen, how this app implements it, and how that
was verified. The observation notes are in `docs/reference/notes.md`.

**Source** is one of:

- **listing** — the app's Google Play page (description, "What's new", screenshots, Data safety), read 2 Oct 2026;
- **screen S-nn** — a screen of the reference app as seen on the founder's phone (numbering as in the notes);
- **system** — what Android's own app-info and role data report about the installed app;
- **brief** — stated in the founder's brief and not independently observed.

**Status** is `done`, `in progress`, `planned`, or `not built` with the reason.
**Verification** names a unit test, or an emulator check from
`tools/verify_emulator.sh` with its recorded result in `docs/verification/`.

> This file is filled in as components land. Rows marked `planned` have their
> design in the knowledge base and no code yet.

## A. The switch and the role

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-01 | One control with three states: off, silence, block | listing; screen S-01 | `Mode` in `core/rules/ScreeningSettings.kt`; selector on the home screen | engine done, screen planned | `RuleEngineTest`: *off lets every call ring*, *block mode rejects…*, *silence mode silences…* |
| F-02 | A line under the control stating what is happening now | listing (screenshot); screen S-01 | home screen status line, including "paused until" | planned | |
| F-03 | Block: a call from a number not in contacts is rejected | listing | rule `unknown-caller` → `BLOCK`; `CallResponse` disallow + reject | engine done, service planned | `RuleEngineTest`: *block mode rejects a caller who is not a contact* |
| F-04 | Silence: the call comes through without ringing and shows in the phone's call log | listing | rule `unknown-caller` → `SILENCE`; `setSilenceCall(true)` | engine done, service planned | `RuleEngineTest`: *silence mode silences a caller who is not a contact* |
| F-05 | A contact always rings, and the app never reads contacts | listing; system (no contacts permission requested) | no `READ_CONTACTS`; Android passes a screening app only non-contacts (ADR-002); rule `contact` as the written guard | engine done, service planned | `RuleEngineTest`: *a contact rings in block mode and in silence mode* |
| F-06 | Becomes the phone's call-screening app through Android's own prompt | system (it holds the role); flow not observed | `RoleManager.createRequestRoleIntent(ROLE_CALL_SCREENING)` when silence or block is chosen | planned | |
| F-07 | No account, no sign-in | listing | there is none | done | nothing to sign in to: no network code of our own |

## B. Notifications

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-08 | Optional notification for each handled call, switched on the home screen | listing; screen S-01 | `notify/` handled-call channel; switch on home; permission asked when switched on | planned | |
| F-09 | A summary of handled calls, off / weekly / monthly | screen S-02; system (a report channel exists) | `ReportPlanner` in core; a daily WorkManager check; report channel | planner done, job planned | `ReportPlannerTest` |
| F-10 | A notification when a round total is reached | system (a milestone channel exists); screen S-06 shows the milestone | `Milestones.crossed` in core; milestone channel | logic done, notification planned | `MilestonesTest` |

## C. Advanced options

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-11 | Scope: filter every unknown caller, or only unknown callers from abroad | listing (screenshot); screen S-04 | `Scope`; rule `domestic-out-of-scope`; `PhoneNumbers.isInternational` | engine done, screen planned | `RuleEngineTest`: *international-only scope…* (3 tests); `PhoneNumbersTest` |
| F-12 | Pause filtering for a chosen time | listing (screenshot); screen S-04 (durations not yet seen) | `pausedUntilMillis`; rule `paused` | engine done, screen planned | `RuleEngineTest`: *while paused…*, *the pause ends at its end time…* |
| F-13 | Let a number ring when it calls again within a set time | listing (screenshot); screen S-04 (window not yet seen) | rule `repeat-call` over the app's own log | engine done, screen planned | `RuleEngineTest`: 5 repeat-call tests |
| F-14 | An allow list of numbers that always ring | screen S-04, S-05 (editor not yet seen) | `allowed_numbers` table; rule `allow-list`; entries may expire | engine done, storage and screen planned | `RuleEngineTest`: 6 allow-list tests |

## D. History

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-15 | A list of handled calls, newest first, grouped by day: number, outcome, time | listing; screen S-05 | `handled_calls` table; history screen | planned | |
| F-16 | The current day's group is headed "today" rather than a date | listing (screenshot) | day headings: today, yesterday, then the date | planned | |
| F-17 | Add a number to the allow list from its history row | screen S-05 | row action | planned | |
| F-18 | Delete one history row | listing (screenshot); screen S-05 | row action | planned | |
| F-19 | Delete the whole history | screen S-05 | top-bar action behind a confirmation | planned | |
| F-20 | Times follow the phone's 12-hour or 24-hour setting | brief; screen S-05 (24-hour seen) | `TimeText` in core, fed the system setting | text done, screens planned | `TimeTextTest` |
| F-21 | Numbers are grouped for reading | listing (screenshot); screen S-05 | `PhoneNumbers.display` | done (core) | `PhoneNumbersTest` |

## E. Statistics

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-22 | Home shows today's count and the last seven days' blocked and silenced counts | screen S-01 | `StatsSummary.today`, `.lastSevenDays` | logic done, screen planned | `StatisticsTest` |
| F-23 | The latest milestone reached, with progress to the next | screen S-06 | `Milestones.progress` | logic done, screen planned | `MilestonesTest` |
| F-24 | All-time blocked and silenced totals | screen S-06 | `StatsSummary.allTime` | logic done, screen planned | `StatisticsTest`: *totals split blocked from silenced* |
| F-25 | An estimate of time saved | screen S-06 | 30 seconds a call, with the assumption shown (gate G8) | logic done, screen planned | `StatisticsTest`: *time saved counts every handled call* |
| F-26 | Last 7 days: calls handled, busiest hour, change against the week before | screen S-06 | `StatsSummary` | logic done, screen planned | `StatisticsTest`: 4 tests |
| F-27 | Last 30 days: calls handled, change against the 30 days before | screen S-06 | `StatsSummary` | logic done, screen planned | `StatisticsTest`: *thirty-day change…* |
| F-28 | A 7-day chart, blocked and silenced stacked per day; tapping a day shows its numbers | screen S-06 | Compose `Canvas` chart | logic done, screen planned | `StatisticsTest`: *the last seven days are today and the six days before it* |
| F-29 | A period selector (7, 30, 90 days) for the charts below it | screen S-06 | `periodDays` | logic done, screen planned | `StatisticsTest`: *weekday and hour charts cover only the chosen period* |
| F-30 | Calls by day of the week, naming the busiest day | screen S-06 | `StatsSummary.byWeekday` | logic done, screen planned | same |
| F-31 | Calls by hour of the day, naming the busiest hour | screen S-06 | `StatsSummary.byHour` | logic done, screen planned | same |
| F-32 | The most frequent numbers, ranked, with counts | screen S-06 | `StatsSummary.topNumbers` | logic done, screen planned | `StatisticsTest`: 2 top-number tests |
| F-33 | Details for one number: blocked, silenced, total, first and last time handled | screen S-07 | `Statistics.detailsFor`; bottom sheet | logic done, screen planned | `StatisticsTest`: *details for a number…* |
| F-34 | Share the statistics as a short text | screen S-06 | system share sheet | planned | |

## F. Settings and the rest

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-35 | A link to the privacy policy | screen S-02; listing | Settings row | planned | |
| F-36 | An open-source licences screen | screen S-02, S-03 | Settings row and screen | planned | |
| F-37 | A way to contact the developer | screen S-02; listing | Settings row (email) | planned | |
| F-38 | Share the app's store link | screen S-01 | home action | planned | |
| F-39 | A link to the app's store page for a review | screen S-01 | home action | planned | |
| F-40 | Light and dark themes following the system | listing (light screenshots); phone (dark) | Material 3 theme | planned | |

## G. Ads

| ID | Feature | Source | Our implementation | Status | Verification |
|---|---|---|---|---|---|
| F-41 | An ad on every screen | screens S-01, S-02, S-04, S-05, S-06 | a banner in reserved space at the bottom edge; nothing shifts when it loads (ADR-006) | planned | |
| F-42 | A full-screen ad around History and Statistics | screens S-05, S-06 | shown on leaving those screens, capped; on-open is one constant away (gate G6) | planned | |
| F-43 | Consent flow where the law requires it | brief | UMP before the ads SDK starts; Privacy Choices in Settings when required | planned | |

## Ours, not in the reference

| ID | Addition | Why |
|---|---|---|
| A-01 | A build stamp in Settings | studio rule: a stale install can fake a bug |
| A-02 | The home screen shows when the role has been lost to another app | otherwise the switch would lie |
| A-03 | Tapping a history row opens that number's details | in the reference the row does nothing and details are reachable only from statistics |
| A-04 | The handled-call notification hides the number on a locked screen | a caller's number is personal data |
| A-05 | An allow entry can expire | the brief's "temporary allow"; see `NOTES.md` |

## Reference behaviour not yet confirmed

Listed in `docs/reference/notes.md` under "Not observed in session 1". The rows above
that depend on it (F-06, F-12, F-13, F-14, F-17, F-18, F-19) are built from the
visible label and our own design where the detail is unknown.
