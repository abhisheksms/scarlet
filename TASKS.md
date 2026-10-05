# TASKS

The live work list for phase one (feature parity). One PR per component. Status is
`done`, `in progress` or `todo`. Decisions and deviations are in `NOTES.md`; the
feature-by-feature state is in `FEATURES.md`.

## Components

| # | Component | Status | PR |
|---|---|---|---|
| 0 | Scaffold: Gradle project (`core`, `app`), CI, `CLAUDE.md`, `AGENTS.md`, reference notes, adb helpers | done | #2 |
| 1 | Spec (PLAN, knowledge base, ADRs, FEATURES) and the pure core: rules, numbers, statistics, milestones, report planner, time text, with tests | done | #3 |
| 2 | Storage: Room (handled calls, allow list) and settings | done | #4 |
| 3 | The spike: screening service, role request, notifications, the home screen. Emulator: block, silence, contact all confirmed (ADR-002) | done | #4 |
| 4 | Screens: options, history, statistics with charts and the number sheet, settings, licences | done | #5 |
| 5 | Report job (daily check, weekly or monthly notification) and milestone notification | done | #5 |
| 6 | Ads: UMP consent, one banner slot with reserved space, capped full-screen ad on leaving history or statistics, test ids pinned by the launch gate | done | #5 |
| 7 | Emulator verification script and its recorded run (the six done-line checks passed), `FEATURES.md` cut to what is proven, the compact home switch, the product-text guard test, `play-policy-config.json` | done | #6 |
| 8 | India's number series: the 160 rule (always), the 140 rule behind its Options switch, emulator check 11, and the Play Repetitive Content record corrected (`docs/play-repetitive-content.md`) | done | #15 |
| 9 | The Quick Settings tile, the third difference (G10): pause filtering for an hour or resume it with one tap; emulator check 12 | done | #16 |
| 11 | The engine learns a timer and a weekly schedule: `ModeClock` (a timer, then the schedule's hour, then the lever), `WeekSchedule`, `LeverMoves`; the rule list is built for the mode in effect at the call's moment. Nothing on screen changed | done | #24 |
| 12 | Freemium, at the founder's ask of 5 Oct (`NOTES.md` N-42): three plans (Free, No Ads, Pro) decided in `core/plans`; the Automatic section on Home with the Timer and the Schedule, both Pro; the lever shows the mode in effect; the plans screen; no ad tray on a paid plan; emulator checks 19 to 21. Nothing can be bought yet | done | #25 |
| 13 | Call-backs (`NOTES.md` N-43, ADR-007): a number the user called rings when it calls back within 24 hours; the `you-called` rule, the `dialled_numbers` table (schema version 2), the switch in Options, a line in How It Works; emulator check 22 | done | #26 |
| 14 | A day's pause after a call to an emergency number (`NOTES.md` N-44, ADR-008): `EmergencyPause` in `core`, `EmergencyCallPause` in the app, a sentence in How It Works; emulator check 23 with a made-up number on Android's test list | done | #27 |
| 15 | Number rules, Pro's third feature (`NOTES.md` N-45): `NumberRule` and its text form in `core`, the rules about how a number starts ordered by length in `RuleBook`, the section in Options, the plans screen and How It Works; emulator check 24 | done | #28 |
| 16 | Buying a plan through Google Play (`NOTES.md` N-46, ADR-009): the Play Billing Library, `billing/PlayStore.kt`, prices and Buy keys on the plans screen, Restore Purchases; the product ids and the one new permission pinned by tests. A purchase itself waits for Play Console | done | #29 |
| 17 | The record after the freemium round: all twenty-four emulator checks in one recorded run on the merged build, the changed screens retaken and eight new ones, the contact sheet made again | done | #30 |
| 10 | The founder's feedback notes of 4 and 5 October (`NOTES.md` N-40): How It Works, the tutorial section, which opens by itself until it has been closed once and again from Settings; "Pause for" over Home's keys; Home's privacy line without "No account"; Settings reordered, the summary reworded, the licences and Contact under one About row; rows whose pages do not exist yet are hidden; emulator check 18 | done | #22 |

## Open, in order

| # | What | Needs |
|---|---|---|
| 0 | ~~A short tutorial section~~ Done 5 Oct 2026 (component 10): How It Works, built at the founder's ask ("tutorial section at the start and a feature for tutorial") from the Switchboard parts, ahead of Claude Design. It opens by itself until it has been closed once, and from Settings after that. Still open: Claude Design draws it in round 2, and the founder reads it on his phone | the next review round |
| 1 | ~~The third difference (PLAN.md G10)~~ Done 4 Oct 2026: the founder picked the Quick Settings tile ("go with the quick settings tile"); built the same day (component 9). Still to see: the tile on a real phone's panel, and a long press on it | the founder's phone |
| 2 | **A reason on every handled call, and Always Block** (`FEATURES.md` D-02): drawn in round 2, then built: the reason line in History and on the sheet, the Always Block key, a block list in Options, and its rule in the engine. The copy never says "spam" | the next review round |
| 3 | How a 1600 or 140 call arrives on an Indian SIM (with +91, as ten digits, or with a leading 0). One real call of each on the founder's phone would settle it; the engine reads all three forms into one key, so the rule holds either way | the founder's say-so, and a real call |
| 4 | ~~Exercise by hand, or add to the script, what is not yet exercised on a device~~ Mostly done 4 Oct 2026 (checks 13 to 17: the links and share, removing an allow entry, the monthly report, the notification's action and the locked screen, the deletes). Still open, in `docs/verification/README.md`: the Open Settings strip, Home on a device that cannot screen, the two licence links, a full-screen ad closing without Android's notice, declining the role prompt, the consent form, the chart taps and the period keys, "yesterday" headings and the change lines, TalkBack | the emulator |
| 5 | Independent code review. One was started on 2 Oct and stopped, at the founder's request, before it reported. Its brief asked about: every path to `respondToCall`; time boundaries; state written during composition in `HistoryScreen` and `StatisticsScreen` (it converges, but belongs in a `LaunchedEffect`); the ads lifecycle; numbers never reaching logs, ads or shared text | nothing |
| 6 | Phone session 2 on the reference app (below) | the founder's four answers and the phone on USB |
| 7 | A Kotlin-aware pass of the playbook's policy checker, as a PR in falcon. Today its APK checks run on this app; its source checks look for TypeScript | nothing |
| 8 | The reference's first-run flow, and whether its screens are translated. Neither can be seen on the founder's phone (it would mean clearing the app's data, or changing the phone's language). A fresh install on the emulator would show both, and everything in session 2, with simulated calls and no risk to real ones | the founder's say-so, and his own sign-in to the Play Store on the emulator |
| 9 | The teardown playbook's steps that happen in the Play Store app on the phone: about a hundred reviews read and tagged, the competitor set, Play's search suggestions, and whether an update newer than 1.4.5 is waiting | the founder's say-so (it is his Play Store, signed in) |
| 10 | Translations (F-44): which languages, and whether for the first release | the founder (gate G5) |
| 11 | ~~What the Pro unlock holds~~ Answered by the founder on 5 Oct 2026 (`PLAN.md` G11): a schedule and a timer, a free app with paid plans, and more features of the kind. Built as component 12. Still his to settle: the plans' prices and names (`PLAN.md` G12) | the founder (G12) |
| 12 | The Quick Settings tile is out of sight until the user edits the panel by hand. Android 13 and later can ask to add it with one tap (`StatusBarManager.requestAddTileService`): a row for that, and a line in How It Works once it exists | the founder's say-so |
| 13 | **Whether the release build shrinks.** `app/build.gradle.kts` has had R8 and resource shrinking on for release builds since 2 Oct, and `docs/SECURITY_CHECKLIST.md` asks for a minified release. On 3 Oct, on the first app, the founder said size optimizations need his OK first, after an R8 build opened to a blank screen from Play (falcon, `founder/OPERATING_PRINCIPLES.md`). The two disagree, and it is his call. Nothing is at stake today, since no release build is being made; ask before the first one, and prove whichever he picks on a copy installed from Play | the founder |

Done since the list was written: `tools/verify_emulator.py` to the end on the final build
(4 Oct 2026, all checks pass; `docs/verification/emulator-2026-10-04.md`), and again late on
5 Oct 2026 on the build that ended the freemium round (all twenty-four pass;
`docs/verification/emulator-2026-10-05.md`).

## Design (opened 3 Oct 2026, gate G4)

The method is in `knowledge-base/docs/05-design-workflow.md`.

| # | Step | Status | Needs |
|---|---|---|---|
| 1 | The brief: `design/CLAUDE_DESIGN_BRIEF.md` | done | nothing |
| 2 | ~~Mobbin connected~~ Dropped on 4 Oct 2026 at the founder's call: its connector needs a paid plan. The brief no longer mentions it | dropped | nothing |
| 3 | Three direction boards from Claude Design | done (4 Oct 2026, in the Claude Design conversation; not saved as files) | nothing |
| 4 | The pick (G4), recorded with one sentence of why | done: Switchboard (4 Oct 2026; `knowledge-base/docs/05-design-workflow.md`) | nothing |
| 5 | Prototype of every surface and state, with review rounds against the brief | in progress: round 1 is in `design/prototype/` (4 Oct 2026; read its `README.md`). Review notes for round 2 so far: the India rules' Later frame (one switch, one statement); a reason on every handled call and Always Block; and, from 5 Oct, How It Works and About as built, Settings' new order and three wordings (`knowledge-base/docs/05-design-workflow.md`) | the notes carried back to Claude Design |
| 6 | Handoff bundle frozen in `design/design_handoff_scarlet/` | todo | the founder's approval of the prototype on his phone |
| 7 | Screens recreated in Compose, checked on the emulator at 360 dp and 135% text | done (4 Oct 2026, from round 1, at the founder's ask; `docs/verification/screens/`). A later change to the prototype is a change to the app | nothing |

## Phone session (reference app)

| Item | Status |
|---|---|
| Session 1: read-only walk of every screen | done (2026-10-02) |
| Session 1b: system queries only, screen untouched: no widget, tile, shortcuts or links; channel importances; version unchanged. The listing read again: translated, a second name, "updated on 2 Oct" | done (2026-10-02, about 22:50) |
| Session 2: flip its own toggles and restore them; mode selector; delete confirmation; a live call | waiting on the founder's four yes/no answers. The phone was back on USB at 22:50 on 2 Oct |

## Waiting on the founder

See `PLAN.md` §3: G3 (name and package), G5 (cut any parity feature?), G6
(full-screen ads), G7 (ad personalisation and content cap), G8 (the time-saved
figure). None blocks the build.

New for G3 (2 Oct): the working label "Call Blocker" is the first two words of a live
app's title, and two-thirds of the name the reference uses for itself in its own
description. It is generic, so it is not impersonation, but the final title needs to
be more distinctive than that.

## For launch, not for phase one

- The privacy page at the address in `ui/Links.kt`, the store listing, the Data safety
  document and the Advertising ID declaration (the policy checker's one error today).
  The studio's site is one upload that replaces everything on it, so this app's page goes
  into the same site folder as the first app's (today `site/` in the cyan repo), never
  into a zip of its own. The page and the Data safety document list what the app keeps on
  the phone and never sends anywhere: the stopped calls, the allow list, and since 5 Oct
  the numbers the user called in the last day (ADR-007).
- The two switches in `ui/Links.kt`: `PRIVACY_PAGE_LIVE` on once the page answers, which
  brings back About's Privacy Policy row and must happen before the first upload to any
  track; `STORE_PAGE_LIVE` on once the app has a Play listing, which brings back Share App,
  Rate App and the link in shared statistics. `LaunchGateTest` refuses live ad ids while
  either is off; emulator check 13 then taps the rows instead of checking they are absent.
- The consent form seen on a test device set to an EEA geography.
- A signed release build on a physical phone (the studio's definition of done).
- Live ad ids, only from the founder; `LaunchGateTest` changes in the same commit.
- The three products in Play Console (`no_ads`, `pro`, `pro_upgrade`, each a one-time
  product, ids exactly so), with their prices (`PLAN.md` G12). They can be made only once
  the app exists there. The studio's launch playbook has the fields, section 8 (falcon,
  `playbooks/google-play-launch-playbook.md`): keep "Backwards compatible" on for the
  purchase option, check India's row after setting prices (the bulk edit rounds), and
  keep the upgrade at Pro's price less No Ads'.
- One test purchase of each product by a licence tester, on a copy installed from Play:
  the sheet says "Test card, always approves", the plan is the user's afterwards, Restore
  Purchases finds it after the app's data is cleared, and three days later Order
  management shows the test orders not refunded (a refund means the app never
  acknowledged them). A build installed over adb cannot buy.

## Not started, by instruction

- *(The Pro unlock was on this list until the evening of 5 Oct 2026, when the founder
  asked for it: "Make it freemium". The plans and their features are built, component 12,
  and buying them through Google Play is wired, component 16. The products themselves are
  made in Play Console, which is his.)*
- *(The India rules were on this list until 4 Oct 2026, when the founder forwarded the
  Play Repetitive Content analysis and asked for them: "can you ensure this for scarlet".
  Built the same day, component 8.)*
- Changing anything on the founder's phone without his ask. The build has been on it
  since 4 Oct 2026, screening his calls at Block (`NOTES.md` N-33); installs there
  happen only on his ask, after the emulator checks. The latest is the freemium build,
  installed late on 5 Oct (`NOTES.md` N-48).
- Play Console, AdMob, live ad ids, a release build, the upload keystore.
