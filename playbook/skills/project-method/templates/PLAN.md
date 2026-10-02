# <App> — Master Plan

**Product**: <one sentence: what it is, who it's for, the category it sits in>.
<Platforms>, same stack as the playbook (`TECH_STACK.md`).

**Thesis**: <why this app deserves to exist; what the category gets wrong; the
one thing this app wins on>. The taste contract is `docs/FOUNDER_TASTE.md`; it
applies to every surface.

This document is the master plan: what we're building, what's decided, what
the founder still has to call, in what order the work happens, and how the
design pipeline fits. The detailed thinking lives in `<app>-knowledge-base/`.
Nothing there may contradict this file.

---

## 1. Roles

| Who | Owns |
|---|---|
| **Founder** | Product direction, taste, final calls at every decision gate, device playtesting, store presence |
| **Claude Code** | Everything engineering: architecture, implementation, tests, CI, docs |
| **Claude Design** | Design directions + the interactive high-fidelity prototype, from `design/CLAUDE_DESIGN_BRIEF.md` |
| **Impeccable** (`/impeccable`) | The design conscience: init context, critique/polish rounds on the prototype, audits on the built app via the web preview |

## 2. Decisions locked (inherited or made now)

1. **Stack = the playbook's stack** (ADR-001): Expo + React Native + TypeScript,
   pnpm workspaces, zustand, expo-sqlite behind a KV contract, Reanimated,
   hand-drawn SVG, Jest two-project testing, GitHub Actions CI as the only
   merge gate. Deviations need an ADR.
2. **Platforms**: <Android first / both buildable from day one>.
3. **Offline-first, no backend, no accounts** (ADR-00n).
4. **Pure deterministic engine** (ADR-00n): <what the engine computes, from what inputs>.
5. **The hard problem goes first** (ADR-00n, spike): <the make-or-break capability>.
6. <Any inherited rule reversed, citing its re-check condition.>
7. **No dark patterns, ever**, and the product's own law: <e.g. nothing may interrupt a running session>.
8. **Method = the playbook's method**: knowledge base before code, design before
   code, engine before UI, CI before feature work scales, founder playtest
   rounds on real devices, taste codified as it emerges.

## 3. Decision gates (founder calls, with recommendations)

| Gate | Question | Options | Recommendation / resolution | Needed by |
|---|---|---|---|---|
| G1 | Ship order | Android first / both | | before Component 0 |
| G2 | Monetization | (a) ads + remove-ads · (b) no ads, Pro unlock · (c) paid | see the playbook's `monetization-gate.md` | before the design brief |
| G3 | Store naming | category term in the title, short launcher label, slug frozen | | before store assets |
| G4 | Design direction | one of the three boards | taste call, the founder's alone | after the boards |
| G5 | v1 nice-to-haves | <list> | | before Component <n> |

Every resolution is recorded in the knowledge base the day it's made.

## 4. What v1 is (scope snapshot)

Full detail in `<app>-knowledge-base/docs/01-v1-scope.md`. <One paragraph.>
**Out of scope for v1**: <the out-list; every addition must name what it displaces>.

## 5. Phases & exit criteria

**Ordering rule: design first, strictly.** The handoff bundle is frozen before
app building proceeds; the spike's code may exist, but device runs and every
APK build happen only on the founder's explicit ask.

- **P1 Spec** — knowledge base + ADRs. Exit: founder signs off on vision,
  scope, and G1–G2 answered or consciously deferred.
- **P2 Design** — `/impeccable init`; brief → 3 boards → G4 → prototype of every
  surface → Impeccable rounds → handoff bundle. Exit: founder approves the
  prototype on their phone; bundle frozen.
- **P3 Foundation** — Component 0 (scaffold + CI green on an empty app) and
  Component 1 (the spike). Exit: <the spike's checkable outcome>; its ADR
  flips to Accepted.
- **P4 Build** — components in order, one PR each, CI gating. Exit: full loop
  works on device; simulation harness green.
- **P5 Founder rounds** — numbered playtest rounds. Exit: nothing feels wrong
  twice in a row.
- **P6 Launch** — monetization component, store kit, launch-gate tests, signed
  builds, rollout. Exit: live on Play.

## 6. Risk register

| # | Risk | Mitigation |
|---|---|---|
| 1 | <the hard problem> | front-loaded spike; honest documentation of caveats |
| 2 | category competition | win on craft; search-first naming; the screenshot must stop a scroll |
| 3 | scope creep | the out-list is written down; every addition names what it displaces |
| 4 | the ambition sliding into AI slop | Impeccable's detector rules and the brief's anti-references are binding; FOUNDER_TASTE bans the tells |

## 7. Immediate next actions

1. Founder reads this plan + knowledge base; answers G1, G2.
2. `npx impeccable install` + `/impeccable init` (founder answers).
3. Founder takes `design/CLAUDE_DESIGN_BRIEF.md` to Claude Design.
4. Claude Code starts Component 0 and the spike research in parallel.
