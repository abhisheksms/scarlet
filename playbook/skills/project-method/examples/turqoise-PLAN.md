# Turqoise — Master Plan

> Worked example from turqoise (the second Cyan Harbor project, August 2026),
> kept verbatim. The template version is in `../templates/`.

**Product**: an interval timer app (working name *turqoise*), in the family of
Timer Plus — the tool you open at the gym, on a mat, or at a desk to run
work/rest intervals hands-free. Android + iOS, same stack as cyan
(see `TECH_STACK.md`).

**Thesis**: the interval-timer category is crowded with functional-but-joyless
utilities and ad-stuffed slop. Nobody in it wins on craft. Turqoise does what
cyan did to its category: instant to start, glanceable from across a gym
floor, cues you can feel with your eyes closed, and a look striking enough
that people show it to a friend — futuristic, but earned through discipline,
never through neon-and-glassmorphism clichés. The full taste contract is
`docs/FOUNDER_TASTE.md`; it applies to every surface of this app.

This document is the master plan — what we're building, what's decided, what
the founder still has to call, in what order the work happens, and how the
design pipeline (Claude design + Impeccable) fits. The detailed thinking
lives in `knowledge-base/` (read its README for the reading order), mirroring
the method that built cyan (`TECH_STACK.md` §9).

---

## 1. Roles

| Who | Owns |
|---|---|
| **Founder** | Product direction, taste, final calls at every decision gate, device playtesting, store presence |
| **Claude Code** | Everything engineering: architecture, implementation, tests, CI, docs |
| **Claude design** | Design directions + the interactive high-fidelity prototype, from the brief in `design/CLAUDE_DESIGN_BRIEF.md` |
| **Impeccable** (`/impeccable`) | The design conscience: init context, critique/polish rounds on the prototype, audits on the built app via the web preview |

## 2. Decisions locked (inherited or made now)

1. **Stack = cyan's stack**, verbatim (founder's call): Expo + React Native +
   TypeScript, pnpm workspaces, zustand, expo-sqlite behind a KV contract,
   Reanimated, hand-drawn SVG, Jest two-project testing, GitHub Actions CI as
   the only merge gate. `TECH_STACK.md` is the reference; deviations need an
   ADR.
2. **Android + iOS** from day one in code and config (the founder asked for
   both; cyan was Android-only in config). Ship order is called (G1,
   2026-08-14): **Android first** — iOS stays buildable throughout and ships
   when the Apple developer account exists.
3. **Offline-first, no backend, no accounts** (ADR-003). A timer that needs
   the internet is broken by design.
4. **Pure deterministic timer engine** (ADR-002): display state is a pure
   function of (routine, event log, now). Wall-clock timestamps, never tick
   counting — that's what makes it drift-free, resumable after process death,
   and testable headless. The cyan engine pattern, transferred.
5. **The hard problem goes first** (ADR-005, spike): background execution and
   exact-time cues with the screen locked are the make-or-break of this
   category. Component 1 is a device spike *before* any UI work — on Android
   now, per G1; the iOS spike runs before the iOS ship, not before the
   Android launch.
6. **Haptics are allowed — as cues only** (ADR-004). Cyan banned haptics as
   decoration; a workout timer's haptic is functional signal. The re-check
   condition cyan's taste doc demanded has been met.
7. **No dark patterns, ever** — and stricter here than cyan: **nothing may
   ever interrupt a running workout.** No ad, no prompt, no upsell, no
   rating beg, under any circumstance, mid-session.
8. **Method = cyan's method**: knowledge base before code, design before
   code, engine before UI, CI before feature work scales, founder playtest
   rounds on real devices, taste codified as it emerges.

## 3. Decision gates (founder calls, with recommendations)

| Gate | Question | Options | Recommendation / resolution | Needed by |
|---|---|---|---|---|
| G1 | Ship order | Android first / both simultaneously | **RESOLVED (2026-08-14): Android first.** iOS stays buildable in code and config throughout and ships when the Apple developer account exists; the spike runs on Android now, and the iOS spike happens before the iOS ship, not before the Android launch | closed |
| G2 | Monetization | (a) cyan clone: free + calm ads + remove-ads lifetime IAP · (b) no ads at all: free core + one lifetime "Pro" unlock (voice packs, themes, history) · (c) paid upfront | **RESOLVED (2026-08-14): (a), the cyan model** — free app, ads on calm non-session surfaces only, one lifetime remove-ads IAP; binding details in ADR-006 | closed |
| G3 | Store naming | Search-first title in the "Interval Timer / HIIT / Tabata" cluster; launcher name short; slug frozen at EAS creation | run the cyan naming play (category term in title, brand in subtitle) | before store assets |
| G4 | Design direction | Pick 1 of the 3 direction boards Claude design produces (see brief) | — (taste call, founder's alone) | after direction boards |
| G5 | v1 nice-to-haves | voice announcements (TTS), history log, named segments, themes — which make v1? | **named segments yes** (cheap, big value); voice + history + themes → v1.1/Pro | before Component 5 |

Every gate resolution gets recorded in the knowledge base (decision docs or
ADR status flip), same as cyan.

## 4. What v1 is (scope snapshot)

Full detail in `knowledge-base/docs/01-v1-scope.md`. The one-paragraph
version: build and run interval routines — prepare / work / rest segments,
rounds and sets, saved routines, quick-start presets (Tabata, EMOM, HIIT
30/30) — with a live timer screen readable at three meters, color-coded
states, precise audio + haptic cues that survive screen-off and
backgrounding, pause/resume/skip, a clean finish summary, full
TalkBack/VoiceOver support, and reduced-motion parity. **Out of scope for
v1**: accounts, sync, wearables, music integration, social anything,
subscriptions, exercise content/videos, and any feature that requires a
network connection.

## 5. Phases & exit criteria

Mirrors cyan's six phases (`TECH_STACK.md` §9), adapted.

**Ordering rule (founder, 2026-08-14): design first, strictly.** The design
is finalized and its handoff bundle frozen (P2 exit) before app building
proceeds; the Component 1 spike's code exists but its device runs — and
every APK build, here and always — happen only on the founder's explicit
ask. Cyan's order, kept: design → handoff → build.

- **P1 Spec** *(now)* — knowledge base + ADRs written and founder-approved.
  Exit: founder signs off on vision, scope, and gates G1–G2 answered or
  consciously deferred.
- **P2 Design** — `/impeccable init` in this repo (writes PRODUCT.md +
  DESIGN.md context); founder takes `design/CLAUDE_DESIGN_BRIEF.md` to
  Claude design → 3 direction boards → G4 pick → interactive prototype of
  the six core surfaces → Impeccable rounds (critique → polish → audit,
  `bolder`/`overdrive` only where the direction earns it) → handoff bundle
  with a CLAUDE_CODE_PROMPT, cyan-style ("recreate, don't port").
  Exit: founder approves the prototype; handoff bundle frozen.
- **P3 Foundation** — Component 0 (workspace scaffold + CI green on an empty
  app) and Component 1 (the background/cue **device spike** — Android now,
  per G1; the iOS spike runs before the iOS ship).
  Exit: cues fire with screen locked on a real Android phone; ADR-005 flips
  from Proposed to Accepted with the chosen Android mechanism (the iOS half
  is verified before the iOS ship).
- **P4 Build** — components in order (engine → storage → live screen →
  builder/home → cues polish → settings/a11y), each landing with tests,
  one PR each, CI gating.
  Exit: full loop works on device; simulation harness green.
- **P5 Founder rounds** — numbered playtest rounds on real devices,
  plain-English feedback in, one fixes-plus-tests PR out per round; taste
  additions flow back into `docs/FOUNDER_TASTE.md`.
  Exit: founder can't find anything that feels wrong twice in a row.
- **P6 Launch** — monetization component (the G2 model: free + ads on calm
  non-session surfaces + remove-ads lifetime IAP), store kit, launch-gate
  tests, signed builds, staged rollout.
  Exit: live on Play (App Store follows when the Apple developer account
  exists, per G1).

## 6. Risk register

| # | Risk | Mitigation |
|---|---|---|
| 1 | **Background/lock-screen cue reliability** (Doze, battery savers, iOS suspension) | Front-loaded device spike (P3); engine schedules cues at absolute timestamps so even a delayed wake computes the right state; document per-OEM caveats honestly |
| 2 | **Timing precision** (JS timer drift) | Never count ticks; derive from monotonic/wall clock; engine property-tests assert zero cumulative drift |
| 3 | **Category competition** (hundreds of interval timers) | Win on craft — G2 keeps every session surface ad-free, so calm remains the differentiator; search-first naming (G3); the design must be the screenshot that stops a scroll |
| 4 | **Scope creep** (wearables, music, content…) | v1 out-list is written down; every addition needs the founder to name what it displaces |
| 5 | **"Futuristic" sliding into AI-slop** | Impeccable's detector rules + anti-references are binding in P2; FOUNDER_TASTE bans the tells; direction boards must cite non-UI references |

## 7. Immediate next actions

1. Founder reads this plan + knowledge base; answers G1, G2 — **done,
   2026-08-14** (G1: Android first; G2: the cyan model — see §3 and ADR-006).
2. Run `npx impeccable install` + `/impeccable init` in this repo (10
   minutes, founder + Claude Code together — it asks strategy questions the
   founder should answer, not Claude).
3. Founder takes `design/CLAUDE_DESIGN_BRIEF.md` to Claude design for the
   three direction boards.
4. In parallel, Claude Code starts P3 Component 0/1 (scaffold + spike) — the
   spike needs no design.
