# scarlet — Master Plan

**Product**: an Android call blocker with one job: calls from numbers that are not
in your contacts are rejected or rung silently. No account, no contacts permission,
a history and statistics kept on the phone, ad-supported. Android only, native
Kotlin.

**Thesis**: the category is full of caller-ID apps that ask for contacts, the call
log and an account, and of small one-job blockers that work but bury themselves in
ads that move under your thumb. Android itself already knows who is in your
contacts and only hands a screening app the calls from everyone else, so the honest
version of this app needs no private data at all. We win on that honesty, on rules
that are easy to reason about, and on a screen that never shifts. The taste
contract is the playbook's `FOUNDER_TASTE.md`; it applies to every surface.

This document is the master plan: what we're building, what's decided, what the
founder still has to call, and in what order the work happens. The detail lives in
`knowledge-base/`. Nothing there may contradict this file.

Phase one (this plan) is **feature parity** with the reference app studied in
`docs/reference/notes.md` and inventoried in `FEATURES.md`. Phase two adds what Play's
Repetitive Content rule asks of a rebuild, differences a user could name (the India
number rules, built 4 October 2026; a reason on every handled call; a third, the
founder's pick), and paid plans beside the free app: asked for on 5 October 2026 and
built that day, buying them through Google Play included (ADR-009). The three products
are made in Play Console once the app exists there (G3, G12).

---

## 1. Roles

| Who | Owns |
|---|---|
| **Founder** | Product direction, taste, final calls at every gate, playtesting on his phone, the store and ad accounts |
| **Claude Code** | Everything engineering: architecture, implementation, tests, CI, docs, emulator verification |

Design began as a light pass. On 3 October 2026 the founder asked for a full
Claude Design pass (G4; `knowledge-base/docs/05-design-workflow.md`). Founder
taste binds every screen and string either way.

## 2. Decisions locked

1. **Stack: native Kotlin, not the playbook's Expo stack** (ADR-001). The core of the
   app is an Android system service.
2. **The call-screening role, and no contacts permission** (ADR-002). Android passes
   a screening app only the calls from numbers outside the user's contacts.
3. **No call-log, SMS or phone-state permission; the app keeps its own log**
   (ADR-003).
4. **Screening rules are an ordered list of data, first match wins, in a pure Kotlin
   module** (ADR-004). The India series rules were two new rows (4 October 2026).
5. **minSdk 29, targetSdk 36** (ADR-005).
6. **Ads: AdMob with Google's test unit ids only, behind the UMP consent flow**
   (ADR-006). Live ids arrive only from the founder.
7. **Offline, no backend, no account, no analytics.** The handled-call log, the allow
   list and the settings stay on the device.
8. **When screening fails for any reason, the call rings.**
9. **Clean room.** Features are learned from the Play listing and from the reference
   app's visible behaviour. No APK is pulled or decompiled; no code, name, icon,
   colour scheme, screenshot or text is copied.
10. **No dark patterns, ever**, and this product's own law: *an ad never moves
    content after the screen has drawn, and never sits where a tap aimed at a
    control can land on it.*
11. **Method**: spec, then the pure engine with tests, then the device spike (the
    screening service on the emulator), then the screens, with CI as the merge gate.
12. **Different from the reference in ways a user can name, before any public track**
    (4 October 2026). Play's Spam policy bans an app that merely repeats an experience
    already on the store; one strike reaches every app on the studio's account. The
    checks and this app's record are `docs/play-repetitive-content.md`. A parity-only
    build stays on internal testing (G9).

## 3. Decision gates (founder calls, with recommendations)

| Gate | Question | Options | Recommendation / resolution | Needed by |
|---|---|---|---|---|
| G1 | Ship order | Android only | **Resolved by the brief (2026-10-02): Android only.** | done |
| G2 | Monetization shape | (a) ads now, Pro unlock later · (b) other | **Resolved by the brief: (a).** Ads with test ids in phase one; the Pro unlock is phase two | done |
| G3 | Name and package | Play title, launcher label, package | Working values: label "Call Blocker", package `com.cyanharborstudios.callblock`. The package becomes permanent at the first upload. **Open**: the founder confirms both before release; the title should carry the words people search ("call blocker", "block unknown calls", "spam") and must not echo either name the reference uses (its title, and a second one in its description: `docs/reference/notes.md`). The working label is generic and close to both that second name and a live app's title, so the final one must be more distinctive | before store assets |
| G4 | Design direction | light pass / full Claude Design boards | **Reopened 3 Oct 2026: the founder asked for the full pass.** Phase one was built on a light pass (stock Material 3, holding at 360 dp and font scale 1.35). The brief is `design/CLAUDE_DESIGN_BRIEF.md`, with three directions: Switchboard, Gate Register, Harbour Light. **Resolved 2026-10-04: Switchboard** ("switchboard is good"; "i dont like gate register one"), with two asks: no lamp colours, and type and a look that resemble the Uber app. Round 1 of the prototype is in `design/prototype/`. **Reaffirmed 2026-10-05**, after his first day with the build on his phone and a first reaction that it "feels very unnatural" to a newcomer: "let's stick with this design"; his new feedback is to come (`NOTES.md` N-39) | done |
| G5 | Scope beyond the brief | build / cut each | The reference has more than the brief listed: a filter scope (all unknown, or international only), pause, repeat-caller pass-through, an allow list, statistics with charts, milestones, a weekly or monthly report, share and review links. **Built for parity.** The founder may cut any of them (taste §10); `FEATURES.md` has one row each. One thing found later is **not built**: the reference's listing is translated into at least eleven languages (F-44); ours is English only until the founder picks languages. **5 Oct 2026:** asked "do we need all these?" of the Settings rows, two parity rows, Share App and Rate App, are hidden until the app has a Play listing, and the privacy row until its page exists; none is cut (`NOTES.md` N-40) | founder round 1 |
| G6 | Full-screen ads | (a) as the reference: a full-screen ad on the tap that opens History and Statistics · (b) a full-screen ad when *leaving* those screens, capped · (c) none | **(b) for now, behind one switch**, with (a) one line away. Reason: Play's ads policy bans full-screen ads that appear "when the user has chosen to do something else", and the studio's account is the asset. The founder decides before live ids go in | before live ad ids |
| G7 | Ad personalisation and content cap | consent-based personalised ads · always non-personalised; content cap PG / T / MA | Follow UMP consent (personalised only where consent is given); cap ad content at PG as the first app does. **Open** | before live ad ids |
| G8 | The "time saved" figure | keep with its assumption shown · cut | Keep, and say what it assumes (30 seconds a call). The founder may cut it as filler | founder round 1 |
| G9 | The public track | (a) ship the parity build to production · (b) hold it on internal testing until the differences are in | **Resolved 2026-10-04: (b).** The founder forwarded the Play Repetitive Content analysis ("can you ensure this for scarlet"). The first difference, India's number series, was built the same day; the second (a reason on every call, Always Block) waits for design round 2; the third is G10 | before the first public track |
| G10 | The third difference | a Quick Settings tile that pauses filtering · schedules · custom prefix rules · a country allow list for the international scope | **Resolved 2026-10-04: the tile** ("go with the quick settings tile"). Lit while calls are filtered; one tap pauses filtering for an hour, the next resumes it; when nothing is filtered a tap opens the app. Built the same day (`TASKS.md` component 9). The reference has none of the four (checked on the founder's phone, 2 Oct); the repeat-caller pass first proposed is parity (F-13) | done |
| G11 | What the Pro unlock holds | (a) no ads only · (b) no ads, plus a schedule and rules by a number's first digits, both built for it · (c) as (b), and Statistics' charts move behind it | **Resolved 2026-10-05, by the founder:** "Add 2 premimum features: To block during certain hours of the week. And to office silence or block for the next few mins hours. Make it freemium, think of some marketing model, maybe 2 tiers, think of more such features and add them". So Pro holds a weekly schedule and a timer (Off, Silence or Block for a while; the pause stays free as its Off part), the app is free with paid plans, and nothing that was free moved. Built the same day (`TASKS.md` component 12, `NOTES.md` N-42). Under "more such features", Pro also got rules by a number's first digits that evening (component 15, N-45), and everyone got two free ones: call-backs (component 13) and a day's pause after an emergency call (component 14). The research behind the choice is `docs/premium-research.md` | done |
| G12 | The plans, their names and their prices | (a) one paid plan, Pro · (b) two paid plans, No Ads and Pro, each bought once, with an upgrade from the first to the second at the difference · (c) a subscription | **Built as (b), for the founder to confirm.** He said "maybe 2 tiers". No Ads is the cheap plan two reviewers of the modelled app asked for and could not buy; Pro holds No Ads and the features. Recommended prices to start: No Ads ₹99, Pro ₹199, the upgrade ₹100, all inside what one-time unlocks in this category cost in India, and to be tested in Play Console. Not (c): the sharpest reviews in the category are about subscriptions. Going back to one plan is one line in `core/plans/Plans.kt` and one plate on the plans screen. The prices are set in Play Console and the app shows Play's own | before the products are created in Play Console |

Every resolution is recorded in the knowledge base the day it is made.

## 4. What phase one is (scope snapshot)

Full detail in `knowledge-base/docs/01-v1-scope.md`; the row-by-row inventory with
sources and verification is `FEATURES.md`.

A home screen with a three-way switch (off, silence, block) and the state in one
line; an optional notification per handled call; advanced options (scope, pause,
repeat callers, allow list); a history of handled calls grouped by day; statistics
(totals, a milestone, recent weeks and months, three charts, the most frequent
numbers and a details sheet for one number); a weekly or monthly summary
notification; How It Works, the tutorial section, which opens by itself until it has
been closed once; settings with the summary, privacy choices where required and a
build stamp; an About screen with the licences and contact, and the privacy policy
once its page exists; ads with test ids behind the consent flow.

**Out of scope for phase one**: caller identification,
SMS, iOS, tablets, any backend, analytics or crash reporting. Every addition must name
what it displaces. **Added on 4 October 2026, ahead of any public track** (decision 12):
the India number rules (built: the 160 series always rings; the 140 series is blocked
once the user switches it on), a Quick Settings tile that pauses filtering with one tap
(built), and a reason on every handled call with Always Allow and Always Block, which
brings a block list of chosen numbers (to be drawn in round 2).

## 5. Phases and exit criteria

- **P1 Spec** — knowledge base, ADRs, the feature inventory from the listing and
  the phone session. Exit: `FEATURES.md` lists every observed feature with its source.
- **P3 Foundation** — Component 0 (scaffold, CI green on an empty app) and the
  spike: the screening service on the emulator. Exit: a simulated call from a
  non-contact is rejected in block mode, rings silently in silence mode, and a
  contact's call never reaches the service; ADR-002 flips to Accepted with what the
  emulator showed.
- **P4 Build** — components in order, one PR each, CI gating: core rules →
  storage → service and notifications → screens → reports and milestones → ads.
  Exit: the done line below.
- **P5 Founder rounds** — on the founder's ask only. Since 4 Oct 2026 the build is on
  his phone and screens his calls (his call: "keep ours screening, leave it"); an
  install there happens only on his ask, after the emulator checks.
- **P6 Launch** — the differences (G9, G10), the Pro unlock and the launch kit. The
  India rules are built; the rest is not started.

**P2 Design** was a light pass folded into P4. It was reopened on 3 Oct 2026 (G4).
Exit: a direction picked, a prototype approved on the founder's phone, the handoff
bundle frozen, and the screens recreated in Compose. On 4 Oct 2026 the screens were
recreated from round 1 of the prototype at the founder's ask; the approval on his phone
and the handoff bundle are still open.

**Done line for phase one** (from the brief):

- `FEATURES.md` lists every feature of the reference app with its source, mapped to
  its implementation and a verification.
- `./gradlew assembleDebug` and `./gradlew test` pass, with unit tests covering the
  rule logic.
- On the emulator, with simulated calls: a non-contact is rejected in block mode; a
  non-contact rings silently in silence mode and appears in the system call log; a
  contact rings normally; history and number details show correct times under both
  12-hour and 24-hour settings; the notification appears only when enabled; a
  temporary allow lets the number ring until it expires.

## 6. Risk register

| # | Risk | Mitigation |
|---|---|---|
| 1 | The platform does not behave as documented (contacts filtering, silencing) on some build | the spike runs first, on the emulator, and its evidence is recorded in ADR-002 and `docs/verification/` |
| 2 | A policy strike on the studio's one developer account | no declaration-gated permission; test ad ids pinned by a test; the ads law in §2.10; the policy checker before any upload |
| 3 | Being taken for a copy of the reference app, by a user or by Play's Repetitive Content rule | clean-room rules in `AGENTS.md`; our own name, words, palette, icon and layout; a trademark list in `play-policy-config.json`; differences a user can name before any public track, recorded and re-checked in `docs/play-repetitive-content.md` (decision 12, G9) |
| 4 | Scope: the reference is larger than the brief | `FEATURES.md` is the list; G5 lets the founder cut |
| 5 | Wrongly blocking a call that mattered | any failure allows the call; contacts never reach the service; repeat callers, the allow list and pause exist for exactly this |
| 6 | OEM builds (the founder's OxygenOS) treating the role differently from AOSP | verified on the emulator; on the founder's phone since 4 Oct 2026, at his ask. **Block settled 5 Oct 2026:** real calls from unknown numbers were blocked there (his report, `NOTES.md` N-39). Silence is not yet seen on it |

## 7. Next actions

See `TASKS.md` for the live list. Decisions waiting on the founder are G3, G5,
G6, G7, G8 and G12 above.
