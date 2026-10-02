# Knowledge Base Maintenance

> Carried over from Cricket Auction Simulator's knowledge base; the rules apply
> to every Cyan Harbor app. Prune the backend bullets if no backend is planned.

## Purpose

This knowledge base exists so a new developer or LLM can understand:

- what is being built,
- why key decisions were made,
- what is in scope,
- where logic belongs,
- what must not be changed casually.

## Documentation rules

- Every major architectural decision gets an ADR.
- Every rule change updates the gameplay document and tests.
- Every persisted-state change updates the save schema documentation.
- Every analytics event is documented before implementation.
- Every billing product, entitlement behavior, purchase-state transition, and
  Play Console configuration change is documented before implementation.
- Every backend service requires an activating ADR, named data owner, API or
  event contract, operational owner, privacy review, service objectives, and
  failure behavior before implementation.
- A conditional backend baseline never authorizes implementation. The first
  backend-dependent capability requires a new activating ADR defining the user
  need, data ownership, offline behavior, minimum infrastructure, security and
  privacy, operational cost, and rollback or degradation behavior.
- Every MySQL schema change uses a reviewed migration and rollback or
  roll-forward plan.
- Every Redis key family documents its source of truth, TTL, invalidation, and
  cache-flush behavior.
- Every Kafka topic documents ownership, keying, retention, schema version,
  retry, idempotency, and dead-letter behavior.
- Every production backend capability defines Grafana dashboards and actionable
  alerts from structured OpenTelemetry-compatible signals.
- Deprecated ideas are marked as deprecated rather than silently removed.
- Documents must describe current behavior, not aspirational behavior, unless clearly labelled.

## Recommended working documents

Add these as implementation begins:

- `docs/player-data-schema.md`
- `docs/save-schema.md`
- `docs/analytics-events.md`
- `docs/ui-screen-map.md`
- `docs/audio-cues.md`
- `docs/game-balance-log.md`
- `docs/engagement-and-accessibility-spec.md`
- `docs/season-outcome-spec.md`
- `docs/billing-operations-runbook.md`
- `docs/backend-api-contracts.md`
- `docs/backend-data-dictionary.md`
- `docs/kafka-event-catalog.md`
- `docs/observability-and-slo-runbook.md`
- `docs/disaster-recovery-runbook.md`
- `docs/release-checklist.md`

## LLM handoff prompt

Use this when starting a new implementation session:

> Read `README.md`, `AGENTS.md`, and the relevant documents under `docs/`. Summarize the current product constraints, architecture boundaries, and acceptance criteria before changing code. Do not introduce new dependencies or alter game rules without explaining the trade-off and updating the relevant ADR or documentation.
