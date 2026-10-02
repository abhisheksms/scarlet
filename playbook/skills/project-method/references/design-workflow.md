# 07 — Design Workflow

> Shared across Cyan Harbor apps (master copy in the playbook). Written for
> turqoise; read "turqoise" as the app being designed. The surfaces, states and
> directions named here are that app's; the stages, the courier rule and the
> verdict rule are the method.

How turqoise gets designed, precisely enough to run. This is P2 in PLAN.md §5,
built on the shape cyan proved (TECH_STACK.md §9, Phase 2): design happens as
its own standalone artifact, gets frozen with every intention named, and is
handed to Claude Code with the instruction *recreate, don't port*. Turqoise
adds one new participant — **Impeccable**, a design-review skill running
inside Claude Code — because this app's stated ambition ("futuristic and
surreal") is exactly the ambition that slides into AI slop without a
mechanical guard (PLAN.md risk #5). Impeccable's own founding claim is the
reason it exists in this pipeline: design without context produces generic
output. So context comes first, and everything downstream reads it.

## The cast

| Who | Runs | Owns |
|---|---|---|
| **Founder** | Answers `/impeccable init`'s strategy questions; pastes the brief into Claude design; picks the direction (G4); approves the prototype; rules on every advisory verdict | Taste. Every judgment call ends with them |
| **Claude design** | Produces the direction boards, the prototype, and every revision | The design itself |
| **Claude Code** | Installs and operates Impeccable; ferries verdicts between Impeccable and Claude design; freezes the handoff bundle; implements it; keeps the web preview alive for build-time audits | Everything mechanical |
| **Impeccable** | `critique` / `polish` / `audit` rounds on the prototype; detector scans; build-time audits on the real app | Finding what's wrong. Never deciding what's right |

Claude design and Impeccable never talk directly — Claude design has no repo
access, Impeccable has no design authority. Claude Code is the courier and
the founder is the judge.

## Stage 1 — Context (`/impeccable init`)

`npx impeccable install`, then `/impeccable init`, run in this repo, founder
and Claude Code together (~10 minutes). Init asks strategy questions —
audience, positioning, emotional register, references. **The founder answers
them, not Claude.** Where PLAN.md or FOUNDER_TASTE.md already holds the
answer, that answer is given verbatim; where a decision gate is open, the
answer says so — an invented answer here poisons every verdict downstream,
because init writes `PRODUCT.md` (strategy) and `DESIGN.md` (visual system)
and **every later Impeccable command reads those two files as truth**. From
this point they are binding design context: a critique verdict that
contradicts them is a bug in the context files, and fixing the context is a
founder conversation, not an edit.

## Stage 2 — Direction boards (→ gate G4)

The founder pastes `design/CLAUDE_DESIGN_BRIEF.md` into a fresh Claude design
conversation. The brief is fully self-contained on purpose — Claude design
has no repo access, so anything not in the brief does not exist for it.

Claude design returns **three named static boards** — *Chronograph*,
*Signal*, *Terminal Velocity* — each exploring the live-timer screen plus one
secondary screen, each citing non-UI references, none touching the banned
clichés (the brief carries the full list). The founder picks one. That is
**gate G4**: a pure taste call, the founder's alone, no recommendation on
record anywhere. The pick and one sentence of why get recorded here in the
knowledge base; the unpicked boards stay in `design/` — a direction rejected
for v1 is still a measured data point about the founder's taste.

## Stage 3 — Prototype and Impeccable rounds

Claude design builds the **interactive HTML prototype** in the chosen
direction: all six surfaces, every state the brief lists, real copy, final
type/color/motion values. Claude Code saves it under `design/prototype/` and
the review loop begins — Impeccable runs **on that HTML in a live browser**:

1. `/impeccable critique` — hierarchy, clarity, emotion.
2. `/impeccable polish` — the finishing pass on what critique surfaced.
3. `/impeccable audit` — accessibility, responsiveness, performance.

`bolder` and `overdrive` run **only where the chosen direction earns them**
— Terminal Velocity earns motion pushes, Chronograph earns almost none — and
`quieter` runs the moment futurism tips into slop. Never speculatively:
unearned intensity is precisely how risk #5 happens.

Each round's output goes back to Claude design as plain-English revision
notes (verdict, screen, what to change, why), Claude design revises, and the
loop repeats **until a round comes back clean**: zero detector findings and
no advisory item the founder wants acted on. Exit criterion for the stage:
the founder approves the prototype **on their own phone's browser, portrait,
at real size** — the three-meter and sweaty-thumb constraints cannot be
judged on a laptop, for the same reason cyan's layouts were judged on a
cheap phone.

## Stage 4 — The handoff bundle

Freeze `design/design_handoff_turqoise/`, built exactly like cyan's
`design_handoff_going_once/`:

- **`README.md`** — the design spec. Every measurement, color, type size,
  motion duration, and copy string is final and *intentional*, with the
  load-bearing decisions named so they survive translation the way cyan's
  "no countdown timer" did. For turqoise the named decisions are at minimum:
  *the background IS the state*, *numerals are the largest thing on screen*,
  *nothing interrupts a session*, *reduced-motion is a designed variant, not
  a fallback*.
- **Screenshots of every surface in every state** — the brief's state list
  is the completeness checklist; a state without a screenshot is not frozen.
- **The interactive prototype itself.**
- **`CLAUDE_CODE_PROMPT.md`** — the ready-to-paste build brief: recreate
  this design in the app's own stack (React Native, Reanimated, hand-drawn
  SVG), **never port the prototype's HTML/CSS/JS**, and where the bundle
  and the knowledge base conflict, the knowledge base wins.

Frozen means frozen: after this point, design changes ride founder playtest
rounds (P5) as product changes with a paper trail — never silent edits to
the bundle. The bundle is the contract implementation is measured against.

## Stage 5 — Build-time audits

Implementation (P4) follows the bundle. Impeccable stays in the loop through
cyan's web-preview trick, carried over deliberately: Metro stubs the
native-only modules on the web platform, so **the real app runs under
react-native-web in a browser** — which puts the actual shipped screens,
not mockups, inside reach of a browser-based design tool. Cadence:

- `/impeccable audit` when each screen component lands (a11y, responsive,
  performance smells), before its PR merges.
- `/impeccable critique` before each founder playtest round, so the founder's
  device time is spent on what only a human can feel.
- Findings are filed against the bundle: a mismatch is either an
  implementation bug (fix the app) or a discovered design truth (founder
  decides, bundle gets a recorded amendment).

The browser preview approximates; it never certifies. Reduced-motion
behavior, TalkBack/VoiceOver narration order, and touch-target feel are
verified **on real devices** — the preview exists to catch problems early
and cheaply, not to replace the phone in the founder's hand.

## The verdict rule — advisory vs. bug

One rule governs every Impeccable output in every stage, and it is the rule
that keeps the tool useful without letting it steer:

- **Deterministic detector findings** (the 59 anti-slop rules) **are bugs.**
  They are fixed before the round closes, never argued with, and never
  escalated to the founder — quality gates are non-negotiable background
  (FOUNDER_TASTE §12) and a rule that fires is a measurable defect, the same
  as a failing test.
- **Judgment verdicts** (critique's hierarchy/clarity/emotion readings,
  every `bolder`/`quieter` suggestion) **are advisory to the founder.**
  Claude Code carries them over in plain English with a recommendation
  attached; the founder's taste overrules the tool without apology or
  ceremony.

Why the split: a detector catches what is checkably wrong; judgment decides
what is right, and on this product what is right belongs to exactly one
person. A tool that gets a vote on taste is how every app in this category
ended up looking the same — which is the thing turqoise exists to beat.
