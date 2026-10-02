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

## Open, in order

| # | What | Needs |
|---|---|---|
| 1 | Run `tools/verify_emulator.py` to the end on the final build: re-confirm check 6, and get checks 7 to 10 (scope, repeat caller, milestone, weekly report) to complete for the first time | the emulator |
| 2 | Exercise by hand, or add to the script, what `docs/verification/README.md` lists under "Not yet exercised on a device" (row delete, Delete All, share, licences, the Settings links, a full-screen ad appearing, the role-lost notice, the locked-screen notification) | the emulator |
| 3 | Independent code review. One was started on 2 Oct and stopped, at the founder's request, before it reported. Its brief asked about: every path to `respondToCall`; time boundaries; state written during composition in `HistoryScreen` and `StatisticsScreen` (it converges, but belongs in a `LaunchedEffect`); the ads lifecycle; numbers never reaching logs, ads or shared text | nothing |
| 4 | Phone session 2 on the reference app (below) | the founder's four answers and the phone on USB |
| 5 | A Kotlin-aware pass of the playbook's policy checker, as a PR in falcon. Today its APK checks run on this app; its source checks look for TypeScript | nothing |

## Phone session (reference app)

| Item | Status |
|---|---|
| Session 1: read-only walk of every screen | done (2026-10-02) |
| Session 2: flip its own toggles and restore them; mode selector; delete confirmation; a live call | waiting on the founder's four yes/no answers. The phone was unplugged during the evening of 2 Oct |

## Waiting on the founder

See `PLAN.md` §3: G3 (name and package), G5 (cut any parity feature?), G6
(full-screen ads), G7 (ad personalisation and content cap), G8 (the time-saved
figure). None blocks the build.

## For launch, not for phase one

- The privacy page at the address in `ui/Links.kt`, the store listing, the Data safety
  document and the Advertising ID declaration (the policy checker's one error today).
- The consent form seen on a test device set to an EEA geography.
- A signed release build on a physical phone (the studio's definition of done).
- Live ad ids, only from the founder; `LaunchGateTest` changes in the same commit.

## Not started, by instruction

- India rules (140-series block, 160-series allow).
- Pro unlock.
- Anything on the founder's phone beyond observing the reference app.
- Play Console, AdMob, live ad ids, a release build, the upload keystore.
