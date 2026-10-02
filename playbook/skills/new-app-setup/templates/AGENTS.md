# Instructions for coding agents

This file applies to the entire repository.

## Source of truth

Before planning or changing the product:

1. Read `<app>-knowledge-base/README.md`.
2. Read `<app>-knowledge-base/AGENTS.md`.
3. Read the documents related to the component being changed.
4. Check every ADR relevant to the decision.
5. Confirm the component's current status in
   `<app>-knowledge-base/component-backlog.json`.

The knowledge base is authoritative. Do not silently change product scope,
rules, architecture, or accepted ADR decisions.

## Cost-aware delegation

The primary agent should delegate bounded grunt work to lower-cost subagents
when the work can be specified precisely and verified reliably.

Good delegation candidates include:

- repetitive test-case implementation,
- mechanical refactors with an explicit pattern,
- fixture and fictional-content validation,
- documentation consistency checks,
- focused investigation of build or test output,
- isolated adapters or utilities with settled interfaces,
- running independent validation commands and summarizing failures.

The primary agent retains responsibility for:

- product and gameplay decisions,
- architecture and package boundaries,
- public interfaces and domain models,
- dependency selection,
- security, privacy, signing, release, and destructive operations,
- integration across components,
- reviewing delegated changes and validating the final result.

Delegation rules:

- Do not delegate a task when coordination would cost more than doing it
  directly.
- Give each subagent a narrow scope, explicit allowed files, relevant context,
  acceptance criteria, and required validation.
- Use parallel agents only for independent tasks with non-overlapping files.
- Subagents must not expand scope, introduce dependencies, or change an ADR
  without explicit approval.
- Subagents must preserve user changes and report files changed, assumptions,
  tests run, and unresolved concerns.
- Prefer lower-cost agents only when correctness is objectively checkable.
  Ambiguous or high-risk work stays with the primary agent.
- The primary agent must inspect the resulting diff and independently verify
  important behavior. Delegation never transfers accountability.

## Quality bar

Cost savings must never reduce product or engineering quality. Work component
by component, avoid speculative abstractions, and do not call work complete
until the repository's documented definition of done is satisfied.

## Performance and dependency discipline

- Smoothness is a product requirement. Validate interaction and animation in
  profiled release builds on representative physical Android devices.
- Keep the live screens' layout stable. Prefer transform and opacity animation
  over layout-heavy effects.
- Motion must communicate state. Do not add continuous decorative animation or
  background work.
- Do not add a production dependency for a minor visual effect or small
  utility. Record why every native dependency is necessary.
- Prefer ordinary React Native components and Reanimated. Use Skia only after a
  measured prototype demonstrates clear value.
- Haptics: <decide per product. Cyan banned them as decoration (its ADR-005);
  turqoise allowed them as cues only, citing the recorded re-check
  condition (its ADR-004). Record the call here and in an ADR.>

## Engagement integrity (fill in for this product)

- Derive every pressure, urgency, scarcity and rivalry cue from authoritative
  state. Never fabricate urgency.
- Private user inputs (watchlists, notes, settings) cannot affect outcomes.
- Losses, near misses and scarcity must not trigger ads.
- Outcomes are seeded and versioned and cannot depend on ads, analytics,
  presentation settings, or difficulty.
- <Product-specific rules go here.>

## Billing and advertising integrity

- The version-one paid product is limited to one Google Play non-consumable
  entitlement: remove ads.
- Keep billing, purchase state, entitlement state, and ad policy outside the
  engine.
- A Google Play-distributed build must use Google Play Billing for this digital
  entitlement. Do not add a separate payment aggregator, external checkout, or
  alternate-billing flow without a new ADR and policy review.
- Never hardcode a localized price in application UI. Query the current offer
  and formatted price from Google Play.
- When the remove-ads entitlement is active, do not initialize or request ads,
  and make every ad-gated convenience available without an ad.
- Never grant gameplay odds or competitive advantages through the entitlement.
- Never place purchase tokens, order identifiers, or billing payloads in
  analytics, crash reports, ordinary logs, or share payloads.

## Conditional backend standard

- Version one has no custom backend. Do not create backend services,
  infrastructure, cloud resources, or network dependencies unless a later ADR
  explicitly activates a backend for a concrete product capability.
- If a backend is activated, use MySQL as the durable system of record, Redis
  for disposable caching and bounded ephemeral coordination, Kafka for
  genuinely asynchronous message processing, and Grafana as the observability
  and alerting surface.
- Do not deploy any of those technologies until an activating ADR identifies
  the approved workload and minimum infrastructure.
- Redis must never become the only copy of durable product, billing, or player
  data. The backend must remain correct after a cache flush.
- Do not route ordinary synchronous request/response work through Kafka.
  Version event contracts, publish reliably, and make consumers idempotent.
- Grafana does not replace instrumentation. Emit structured
  OpenTelemetry-compatible metrics, logs, and traces, and select the concrete
  collector and signal stores when the backend is activated.
- Keep secrets, personal data, raw purchase tokens, and sensitive billing
  payloads out of Kafka messages, dashboards, metrics, traces, and ordinary
  logs.

## Code

- Semantically descriptive names; readable, clean code, not riddled with
  comments.
