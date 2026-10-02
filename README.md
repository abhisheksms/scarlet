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

- 2 October 2026: Component 0 is in: a native Kotlin project (`core`, a pure
  Kotlin module, and `app`), CI, and the repo conventions (`CLAUDE.md`,
  `AGENTS.md`). The reference app was walked on the founder's phone; the notes are
  in [`docs/reference/notes.md`](docs/reference/notes.md).
- [`HANDOFF.md`](HANDOFF.md) is the brief this work started from.
  [`PLAN.md`](PLAN.md) is the master plan, [`FEATURES.md`](FEATURES.md) the feature
  inventory and its verification, [`TASKS.md`](TASKS.md) the work list and
  [`NOTES.md`](NOTES.md) the decisions and deviations.
