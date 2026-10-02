# ADR-003: Pure deterministic auction engine

> Carried over from Cricket Auction Simulator. Re-accept or supersede it for
> the new app and record the date in Status.

## Status

Accepted

## Decision

Implement the auction engine as a pure TypeScript package using seeded
randomness and serializable actions. The same boundary also contains the
post-auction abstract season simulator, which consumes finalized squads and an
explicit derived seed without mutating auction state.

## Consequences

- Reproducible bugs
- Fast tests
- Easier balancing
- Possible future multiplayer foundation
- Strict separation from UI, ads, billing, entitlements, backend clients, and
  platform code is required
- Reproducible and balanceable post-auction season outcomes
