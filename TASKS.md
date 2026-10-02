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
| 4 | Screens: home, advanced options, history, statistics with charts and the number sheet, settings, licences | todo | |
| 5 | Report job and milestone notification | todo | |
| 6 | Ads: UMP consent, banner slot, full-screen ad gate, test ids, launch-gate test | todo | |
| 7 | Emulator verification of the done line, `FEATURES.md` verification column, policy config, security checklist pass | todo | |

## Phone session (reference app)

| Item | Status |
|---|---|
| Session 1: read-only walk of every screen | done (2026-10-02) |
| Session 2: flip its own toggles and restore them; mode selector; delete confirmation; a live call | waiting on the founder's four yes/no answers |

## Waiting on the founder

See `PLAN.md` §3: G3 (name and package), G5 (cut any parity feature?), G6
(full-screen ads), G7 (ad personalisation and content cap), G8 (the time-saved
figure). None blocks the build.

## Not started, by instruction

- India rules (140-series block, 160-series allow).
- Pro unlock.
- Anything on the founder's phone beyond observing the reference app.
- Play Console, AdMob, live ad ids, a release build, the upload keystore.
