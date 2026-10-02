# ADR-002: Offline-first version one

> Carried over from Cricket Auction Simulator. Re-accept or supersede it for
> the new app and record the date in Status.

## Status

Accepted

## Decision

Version one must work without an account, custom backend, or continuous internet connection.

## Consequences

- Faster delivery
- Lower operating cost
- Simpler testing
- Easier privacy model
- Multiplayer and cloud saves are deferred
- A future backend must not silently make the version-one auction and season
  loop network-dependent; changing that promise requires a separate ADR
