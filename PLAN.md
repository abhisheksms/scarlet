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
`docs/reference/notes.md` and inventoried in `FEATURES.md`. Phase two (not started,
by instruction) adds India-specific prefix rules and a one-time Pro unlock.

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
   module** (ADR-004). Phase two's prefix rules are new rows, not a rewrite.
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

## 3. Decision gates (founder calls, with recommendations)

| Gate | Question | Options | Recommendation / resolution | Needed by |
|---|---|---|---|---|
| G1 | Ship order | Android only | **Resolved by the brief (2026-10-02): Android only.** | done |
| G2 | Monetization shape | (a) ads now, Pro unlock later · (b) other | **Resolved by the brief: (a).** Ads with test ids in phase one; the Pro unlock is phase two | done |
| G3 | Name and package | Play title, launcher label, package | Working values: label "Call Blocker", package `com.cyanharborstudios.callblock`. The package becomes permanent at the first upload. **Open**: the founder confirms both before release; the title should carry the words people search ("call blocker", "block unknown calls", "spam") and must not echo either name the reference uses (its title, and a second one in its description: `docs/reference/notes.md`). The working label is generic and close to both that second name and a live app's title, so the final one must be more distinctive | before store assets |
| G4 | Design direction | light pass / full Claude Design boards | **Reopened 3 Oct 2026: the founder asked for the full pass.** Phase one was built on a light pass (stock Material 3, holding at 360 dp and font scale 1.35). The brief is `design/CLAUDE_DESIGN_BRIEF.md`, with three directions: Switchboard, Gate Register, Harbour Light. **Resolved 2026-10-04: Switchboard** ("switchboard is good"; "i dont like gate register one"), with two asks: no lamp colours, and type and a look that resemble the Uber app. Round 1 of the prototype is in `design/prototype/` | done |
| G5 | Scope beyond the brief | build / cut each | The reference has more than the brief listed: a filter scope (all unknown, or international only), pause, repeat-caller pass-through, an allow list, statistics with charts, milestones, a weekly or monthly report, share and review links. **Built for parity.** The founder may cut any of them (taste §10); `FEATURES.md` has one row each. One thing found later is **not built**: the reference's listing is translated into at least eleven languages (F-44); ours is English only until the founder picks languages | founder round 1 |
| G6 | Full-screen ads | (a) as the reference: a full-screen ad on the tap that opens History and Statistics · (b) a full-screen ad when *leaving* those screens, capped · (c) none | **(b) for now, behind one switch**, with (a) one line away. Reason: Play's ads policy bans full-screen ads that appear "when the user has chosen to do something else", and the studio's account is the asset. The founder decides before live ids go in | before live ad ids |
| G7 | Ad personalisation and content cap | consent-based personalised ads · always non-personalised; content cap PG / T / MA | Follow UMP consent (personalised only where consent is given); cap ad content at PG as the first app does. **Open** | before live ad ids |
| G8 | The "time saved" figure | keep with its assumption shown · cut | Keep, and say what it assumes (30 seconds a call). The founder may cut it as filler | founder round 1 |

Every resolution is recorded in the knowledge base the day it is made.

## 4. What phase one is (scope snapshot)

Full detail in `knowledge-base/docs/01-v1-scope.md`; the row-by-row inventory with
sources and verification is `FEATURES.md`.

A home screen with a three-way switch (off, silence, block) and the state in one
line; an optional notification per handled call; advanced options (scope, pause,
repeat callers, allow list); a history of handled calls grouped by day; statistics
(totals, a milestone, recent weeks and months, three charts, the most frequent
numbers and a details sheet for one number); a weekly or monthly summary
notification; settings with the privacy policy, privacy choices, licences, contact
and a build stamp; ads with test ids behind the consent flow.

**Out of scope for phase one**: the India prefix rules (auto-block 140-series,
always allow 160-series), the Pro unlock, any purchase, caller identification, a
blocklist of chosen numbers, SMS, iOS, tablets, any backend, analytics or crash
reporting. Every addition must name what it displaces.

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
- **P5 Founder rounds** — on the founder's ask only; installing on his phone is a
  stop (it would take over his call screening).
- **P6 Launch** — phase two and the launch kit. Not started.

**P2 Design** was a light pass folded into P4. It was reopened on 3 Oct 2026 (G4).
Exit: a direction picked, a prototype approved on the founder's phone, the handoff
bundle frozen, and the screens recreated in Compose.

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
| 3 | Being taken for a copy of the reference app | clean-room rules in `AGENTS.md`; our own name, words, palette, icon and layout; a trademark list in `play-policy-config.json` |
| 4 | Scope: the reference is larger than the brief | `FEATURES.md` is the list; G5 lets the founder cut |
| 5 | Wrongly blocking a call that mattered | any failure allows the call; contacts never reach the service; repeat callers, the allow list and pause exist for exactly this |
| 6 | OEM builds (the founder's OxygenOS) treating the role differently from AOSP | verified on the emulator now; the founder's phone needs his explicit go-ahead and is a P5 item |

## 7. Next actions

See `TASKS.md` for the live list. Decisions waiting on the founder are G3, G5,
G6, G7 and G8 above.
