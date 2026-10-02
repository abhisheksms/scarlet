# <App> Knowledge Base

The authoritative product spec for <app>, a Cyan Harbor app built with the
method that built Cricket Auction Simulator: knowledge base before code,
design before code, engine before UI. Everything here is written so a fresh
Claude Code session, or a human developer, can build the right thing without
being re-briefed in chat.

## How this fits the other root documents

- **`PLAN.md`** (repo root) is the binding spine: roles, locked decisions, the
  founder's gates, phases, risks. Nothing here may contradict it; these docs
  elaborate it and never re-open it.
- **`docs/FOUNDER_TASTE.md`** is the voice and taste contract, portable across
  every Cyan Harbor app. It governs all UI, copy and monetization surfaces,
  and the writing in these docs too.
- **The playbook's `TECH_STACK.md`** is the verified inventory of the stack,
  adopted per ADR-001. Stack facts live there, once.
- **`docs/SECURITY_CHECKLIST.md`** is the pre-merge security gate, ordered by
  this app's real risk.

## Reading order

Read top to bottom the first time; each document assumes the ones above it.
Create each as implementation reaches it; keep the numbering.

| Doc | What it settles |
|---|---|
| `docs/00-product-vision.md` | why this app deserves to exist; the pillars every later call is tested against |
| `docs/01-v1-scope.md` | what v1 is and, more importantly, is not; the out-list that stops scope creep; the product's own laws |
| `docs/02-<engine>-model-and-state-machine.md` | the pure deterministic engine: entities, events, time |
| `docs/03-<the hard problem>.md` | the category's make-or-break problem and the spike that settles it |
| `docs/04-application-architecture.md` | packages, layers, the one-direction dependency rule, the native adapter table with fallbacks |
| `docs/05-implementation-plan.md` | build order as numbered components, each with tests and a checkable exit criterion |
| `docs/06-testing-strategy.md` | what green CI must prove; the simulation harness; the device matrix |
| `docs/07-design-workflow.md` | the Claude Design + Impeccable pipeline; the recreate-don't-port handoff |
| `docs/08-engagement-loop.md` | retention mechanics, derived from real state |
| `docs/09-billing-and-entitlements.md` | the one product, entitlement behaviour, Play Console config |
| `docs/10-knowledge-base-maintenance.md` | the documentation rules (copied from the playbook) |
| `adr/` | one file per hard-to-reverse decision, with status |

## ADRs

Anything expensive to reverse (a dependency, a persisted schema, a platform
mechanism, a monetization shape) gets an Architecture Decision Record in
`adr/` **before** implementation. Format: `adr/ADR-000-template.md`. Status
goes `Proposed` → `Accepted`; later `Superseded by ADR-nnn` if replaced, never
deleted. The ADRs carried over from the playbook are re-accepted for this app
with the date, or superseded with the reason.

## Decision gates

`PLAN.md` §3 holds the founder's calls. When a gate closes:

1. Record the resolution as a dated note in the document that owns the topic.
2. Add or flip the ADR if the resolution is hard to reverse.
3. Sweep every doc that hedged on the open gate and make it decisive.

Until a gate closes, docs mark the dependency inline ("Decision gate G2") and
never silently assume an outcome.

## Maintenance rules

These kept cyan's knowledge base honest through the entire build. They are
not optional.

- **Every major decision gets an ADR.** Decisions made in chat evaporate when
  the session ends; the ADR is what lets the next session start aligned
  instead of re-litigating.
- **Docs describe current intent, not aspiration.** During spec everything is
  intent, labelled as such. Once code exists: when code and doc disagree, one
  of them is corrected in the same PR. A doc allowed to drift trains readers
  to ignore every doc.
- **Deprecated ideas are marked, never deleted.** The reason an option lost is
  as valuable as the option that won. Tag it `Deprecated (date, why)` and
  leave it standing.
- **Every rule states its why.** A why-less rule gets "optimized" away by
  whoever reads it next. Cyan's engine purity survived every refactor because
  the doc said exactly what breaks without it.
- **Gate resolutions are recorded here** the day the founder calls them.
- **Numbers quoted from code get re-checked against code.** Durations,
  defaults and limits in these docs are illustrative; the source file named
  beside each one is authoritative.
- **Taste findings go to `docs/FOUNDER_TASTE.md`, not here.** This knowledge
  base is the app's mechanics; taste is portable and lives in the portable
  file, whose master copy is in the playbook.

## Documents to add as implementation begins

Each is created when its subject becomes real, not before:
`docs/save-schema.md`, `docs/analytics-events.md`, `docs/ui-screen-map.md`,
`docs/<cue or content>-catalog.md`, `docs/game-balance-log.md`,
`docs/release-checklist.md`.

## Handoff prompt for a new session

> Read `PLAN.md`, `docs/FOUNDER_TASTE.md`, this README, and the knowledge base
> in reading order. State the current constraints and open gates before
> changing anything. No new dependencies and no rule changes without the
> trade-off written down and the relevant doc or ADR updated in the same PR.
