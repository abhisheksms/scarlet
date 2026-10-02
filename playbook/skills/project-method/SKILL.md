---
name: project-method
description: How a Cyan Harbor project runs from idea to launch - the phases (spec, design, foundation with the hard-problem spike, build, founder playtest rounds, launch) with checkable exit criteria, the founder's decision gates, PLAN.md as the binding spine, the knowledge base before any code, the self-contained Claude Design brief, the recreate-don't-port handoff bundle, and Impeccable's place in design review with the verdict rule. Use it when starting or re-planning a project, when asked "what phase are we in", "what's next" or "can we start building", when writing or updating PLAN.md, when briefing Claude Design or reviewing its boards and prototype, and when deciding the order of work.
---

# Project method

Cyan went from first design doc to launch-ready in about three weeks (23 July
to 14 August 2026) and turqoise wrote the method down two days later. The
long version is `TECH_STACK.md` §9 in the `new-app-setup` skill; this is the
operating version.

## The recipe

1. **Spec before code.** The knowledge base first: vision, scope with an
   explicit out-list, domain model, state machine, architecture,
   implementation plan as numbered components, testing strategy. Every
   hard-to-reverse decision is an ADR before it is implemented.
2. **PLAN.md is the spine.** Roles, decisions locked, the founder's gates with
   recommendations, phases with exit criteria, risks, next actions. Nothing in
   the knowledge base may contradict it; the knowledge base elaborates it.
3. **Design before code, strictly.** A self-contained brief goes to Claude
   Design; three named direction boards come back; the founder picks one; an
   interactive HTML prototype of every surface and state follows; Impeccable
   rounds run on it until clean; the handoff bundle is frozen. Building waits
   for the freeze.
4. **CI is the first PR**, before any feature work. Green CI is the only merge
   gate.
5. **The hard problem goes first**, as a device spike before any UI: the thing
   that, if it can't be done, makes the rest pointless (cyan: nothing; turqoise:
   cues with the screen locked). Device runs happen on the founder's explicit
   ask.
6. **Engine before UI.** A pure, seeded, deterministic engine with a purity test
   and a simulation harness, then components in numbered order, one PR each,
   each with its tests.
7. **Founder rounds on a real phone.** Plain-English feedback in, one PR of
   fixes-plus-tests out, numbered. Recurring feedback becomes a rule in
   `FOUNDER_TASTE.md`; docs are corrected after every round.
8. **Launch from written checklists**, with the risky facts (package name, ad
   ids) enforced by tests, not memory. The `play-launch`, `play-policy-guard`
   and `android-release` skills are those checklists.

## Phases and exit criteria

| Phase | Work | Exit |
|---|---|---|
| P1 Spec | knowledge base + ADRs + PLAN.md | the founder signs off on vision, scope and the first gates |
| P2 Design | `/impeccable init` → brief → boards → pick → prototype → review rounds → handoff bundle | the founder approves the prototype on their own phone; the bundle is frozen |
| P3 Foundation | Component 0 (scaffold, CI green on an empty app) and the spike | CI gates merges; the spike's ADR flips to Accepted with the mechanism named |
| P4 Build | components in order: engine → storage → hero screen → the rest → polish pass | the full loop works on a device; the simulation harness is green |
| P5 Founder rounds | numbered playtest rounds | the founder can't find anything that feels wrong twice in a row |
| P6 Launch | monetization component, store kit, launch-gate tests, signed builds | live on Play |

Two carve-outs from "design first": Component 0, because every later merge
goes through its CI, and the spike's code, because it needs no design. Both
exist before the freeze; nothing else does.

## Roles

| Who | Owns |
|---|---|
| Founder | direction, taste, every gate, device playtesting, the store and ad accounts |
| Claude Code | everything engineering: architecture, implementation, tests, CI, docs; the courier between Impeccable and Claude Design |
| Claude Design | the direction boards, the prototype, every revision; it has no repo access, so the brief must be self-contained |
| Impeccable | finding what's wrong (critique, polish, audit, detector scans); never deciding what's right |

## Decision gates

A gate is a question only the founder can answer, written down with options,
a recommendation and a "needed by". The standing five, from turqoise:

| Gate | Question | Needed by |
|---|---|---|
| G1 | Ship order: Android first, or both platforms | before Component 0 |
| G2 | Monetization shape: see `references/monetization-gate.md` | before the design brief |
| G3 | Store naming: category keyword in the title, launcher label, slug frozen at creation | before store assets |
| G4 | Design direction: pick one of the three boards | after the boards |
| G5 | v1 nice-to-haves: which ship, which go to v1.1 | before the component that needs them |

When a gate closes: record the resolution with the date in the document that
owns the topic, add or flip the ADR if it's hard to reverse, and sweep every
doc that hedged on it. PLAN.md holds the question; the knowledge base holds
the answer. A rule inherited from an earlier app can be reversed, but the
reversal cites the recorded re-check condition (turqoise's ADR-004 on haptics
is the model, in `examples/`).

## The design pipeline

`references/design-workflow.md` has the five stages in full. The parts that
bite:

- **Install Impeccable** (`npx impeccable install`, then `/impeccable init`)
  with the founder answering its strategy questions, never Claude. It writes
  `PRODUCT.md` and `DESIGN.md`, which every later Impeccable command treats as
  truth. A verdict that contradicts them is a bug in the context files.
- **The brief is the whole world** for Claude Design. `templates/CLAUDE_DESIGN_BRIEF.md`
  is the shape: product and moments of use, every surface with every state,
  hard constraints, three named directions with non-UI references, the banned
  clichés, deliverables per stage, and the voice rules.
- **The verdict rule.** Deterministic detector findings are bugs: fixed before
  the round closes, never argued with, never escalated. Judgment verdicts
  (hierarchy, emotion, bolder, quieter) are advisory to the founder, relayed in
  plain English with a recommendation. The tool never gets a vote on taste.
- **The handoff bundle** (`templates/CLAUDE_CODE_PROMPT.md`): the spec README
  with every value final and the intentional decisions named, screenshots of
  every surface in every state, the prototype, and the build prompt: recreate
  in the app's own stack, never port the prototype's code; the knowledge base
  wins on conflict. Frozen means frozen; later changes ride founder rounds with
  a paper trail.
- **Build-time audits** run on the real app in a browser through
  react-native-web (Metro stubs the native-only modules), before each screen's
  PR and before each founder round. The browser approximates; the phone
  certifies.

## Project history

- **cyan** (Cricket Auction Simulator): July to October 2026, live on Play
  2 October 2026. The reference implementation.
- **turqoise** (an interval timer): 14 to 16 August 2026, eleven commits. Spec,
  ADRs, PLAN.md, the design brief, the security checklist and a background-cue
  spike harness exist; no boards, no prototype, no app. It stopped at P2 while
  cyan's launch took the founder's attention. Its documents are the examples
  here; its timer engine spec and spike are reusable if a timer-like product
  comes back.
- **scarlet**: the third project, October 2026, started from this playbook.

## Files

- `templates/PLAN.md`: the master-plan skeleton.
- `templates/CLAUDE_DESIGN_BRIEF.md`: the brief skeleton with the rules that carry.
- `templates/PRODUCT.md`: Impeccable's product-context format.
- `templates/CLAUDE_CODE_PROMPT.md`: the handoff-to-build prompt skeleton.
- `references/design-workflow.md`: the five-stage pipeline and the verdict rule.
- `references/monetization-gate.md`: the three monetization shapes and what binds.
- `examples/`: turqoise's PLAN, brief, PRODUCT.md, ADR-004 (reversing an
  inherited rule), ADR-006 (a gate decided); cyan's design-handoff README and
  build prompt.
