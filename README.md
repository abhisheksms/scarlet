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

- 5 October 2026: phase one (feature parity) is built and the Switchboard design is on
  every screen. A native Kotlin app (`core`, a pure Kotlin module with the screening
  rules, and `app`), 127 unit tests, CI, and all eighteen emulator checks passing. Two of
  the three differences Play's Repetitive Content rule asks of a rebuild are in: India's
  number series and the Quick Settings tile (below). The founder's first notes from using
  it were acted on that day: a tutorial section, How It Works, which opens by itself until
  it has been closed once; plainer words on Home and in Settings; and no row that opens a
  page which does not exist yet ([`NOTES.md`](NOTES.md), N-40). The exact state is in
  [`docs/verification/README.md`](docs/verification/README.md), and what the app looks
  like is in [`docs/verification/screens/contact-sheet.png`](docs/verification/screens/contact-sheet.png).
- [`PLAN.md`](PLAN.md) is the master plan with the founder's open gates,
  [`FEATURES.md`](FEATURES.md) the feature inventory and what is proven for each row,
  [`TASKS.md`](TASKS.md) what is open, in order, and [`NOTES.md`](NOTES.md) the
  decisions and deviations. [`HANDOFF.md`](HANDOFF.md) is the brief this work started
  from; [`docs/reference/notes.md`](docs/reference/notes.md) is the walk through the
  reference app.
- Not started, by instruction: the Pro unlock (what it could hold is researched in
  [`docs/premium-research.md`](docs/premium-research.md)), live ad ids, a release. The founder's
  phone runs the build at his own ask since 4 October 2026; nothing changes there
  without it.

## Play's Repetitive Content rule

Checked 4 October 2026; the record is [`docs/play-repetitive-content.md`](docs/play-repetitive-content.md).
Closest Play app: Block Unknown Callers (Life Software Lab). Difference a user would
notice: this app knows India's number series, so service calls from banks, insurers and
government bodies (the 1600 and 1601 series) always ring and one switch blocks every
140-series promotional call; it pauses from Quick Settings with one tap; and it shows
the rule behind every call it stopped, with Always Allow and Always Block one tap away. None of Cyan Harbor's published apps shares
its core functionality, content or flow: the studio's one live app is a cricket auction
game. A parity-only build stays on internal testing ([`PLAN.md`](PLAN.md), gate G9).
