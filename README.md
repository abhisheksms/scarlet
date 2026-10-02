# scarlet

The third Cyan Harbor project. It starts from the studio's playbook: a Claude
Code plugin of six skills (project method, founder taste, the Google Play policy
guard, the Android release procedure, the Play launch kit and new-app setup)
extracted from the first two projects, cyan (live on Google Play) and turqoise
(stopped at the design stage).

The playbook lives in the studio's private `falcon` repo under `playbook/`. It
was extracted here on 2 October 2026 and moved there the same day.

Sessions in this repo get the skills by enabling the plugin: see
[`.claude/settings.json`](.claude/settings.json). falcon is private, so that
needs access to it: your own GitHub credentials on your machine, or, in a cloud
session, falcon attached next to this repo.

## Where things stand

- 2 October 2026: phase one (feature parity) is built. A native Kotlin app
  (`core`, a pure Kotlin module with the screening rules, and `app`), 100 unit tests,
  CI. The six done-line checks passed on the emulator; four further checks and a
  handful of controls have not been exercised on a device yet. The exact state is in
  [`docs/verification/README.md`](docs/verification/README.md), and what the app looks
  like is in [`docs/verification/screens/contact-sheet.png`](docs/verification/screens/contact-sheet.png).
- [`PLAN.md`](PLAN.md) is the master plan with the founder's open gates,
  [`FEATURES.md`](FEATURES.md) the feature inventory and what is proven for each row,
  [`TASKS.md`](TASKS.md) what is open, in order, and [`NOTES.md`](NOTES.md) the
  decisions and deviations. [`HANDOFF.md`](HANDOFF.md) is the brief this work started
  from; [`docs/reference/notes.md`](docs/reference/notes.md) is the walk through the
  reference app.
- Not started, by instruction: the India rules, the Pro unlock, anything on the
  founder's phone beyond observing the reference app, live ad ids, a release.
