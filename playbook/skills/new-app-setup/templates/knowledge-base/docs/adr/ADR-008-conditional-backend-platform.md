# ADR-008: Conditional backend platform

> Carried over from Cricket Auction Simulator. Re-accept or supersede it for
> the new app and record the date in Status.

## Status

Accepted

## Context

Version one is offline first and has no custom backend. Future capabilities such
as accounts, cloud saves, secure server-side entitlement verification,
leaderboards, remote challenges, or multiplayer may eventually justify backend
services.

Selecting a platform direction now can prevent inconsistent infrastructure
choices later, but pre-emptively deploying databases, caches, brokers, and
observability systems would add cost and operational risk before a workload
exists.

## Decision

Adopt the following conditional backend baseline:

- MySQL is the durable relational system of record.
- Redis provides disposable caching and bounded ephemeral coordination.
- Kafka provides asynchronous message processing where decoupling, replay,
  ordering by key, or independent scaling is required.
- Grafana is the shared observability, dashboard, investigation, and alerting
  surface.
- Backend services emit structured OpenTelemetry-compatible metrics, logs, and
  traces. The concrete collector and signal stores are selected with the first
  backend workload.

This ADR does not authorize a backend or add one to version one. A new
activating ADR must define the concrete product capability, data ownership,
offline behavior, API, security, privacy, reliability target, cost, and minimum
infrastructure.

Additional rules:

- Do not deploy a technology without an approved workload.
- Redis cannot be the sole durable store.
- Kafka is not the default synchronous request path.
- MySQL-to-Kafka state changes use a transactional outbox or an equivalent
  reliable publication pattern.
- Kafka consumers are idempotent and event contracts are versioned.
- Grafana does not replace application instrumentation.
- The auction engine remains independent of backend clients and infrastructure.
- Secrets, personal data, raw purchase tokens, and sensitive billing payloads
  stay out of Kafka, dashboards, metrics, traces, and ordinary logs.
- Supported product versions, cloud provider, managed versus self-hosted
  deployment, backend language, framework, and API style remain deferred until
  activation.

## Consequences

Positive:

- Future backend work has a coherent storage, cache, messaging, and
  observability direction.
- MySQL remains a clear source of truth while Redis stays rebuildable.
- Kafka workflows have explicit reliability and idempotency expectations.
- Grafana provides one operational surface across services and dependencies.
- Version one retains its low-cost offline-first architecture.

Negative:

- The selected stack carries meaningful operational complexity when activated.
- Kafka and a full telemetry pipeline may be disproportionate for an early
  single-service workload and must wait for a real need.
- Teams will need expertise in database operations, cache failure modes, event
  schema evolution, and production observability.
- A later workload may require an exception, which must be recorded in a new
  ADR rather than introduced silently.

## Revisit when

- the first backend-dependent capability is approved,
- scale or cost makes a selected technology unsuitable,
- compliance or data residency changes deployment requirements,
- managed-service limitations affect portability or reliability,
- the offline-first product promise changes.
