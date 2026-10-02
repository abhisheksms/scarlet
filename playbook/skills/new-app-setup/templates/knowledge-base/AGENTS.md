# Instructions for developers and coding agents

Before changing code:

1. Read `docs/00-product-vision.md`.
2. Read `docs/01-v1-scope.md`.
3. Read the document related to the component being changed.
4. Check the ADRs in `docs/adr/`.

## Non-negotiable rules

- The engine must remain independent of React Native, Expo, storage, analytics,
  ads, and UI code.
- Rules must never be implemented inside React components.
- All randomness must come from a seeded random source.
- Every engine behavior change requires unit tests.
- Opponents or rules systems must not read private user inputs or cheat.
- Save files must contain a schema version. Old saves must keep loading: new
  fields are optional, existing fields are never renamed or removed, and
  breaking changes go through a migration with a frozen real save as a fixture.
- Do not add a backend without a new ADR. Version one has no custom backend;
  ADR-008 records a conditional baseline but does not authorize implementation.
- Keep billing, purchase, entitlement, and localized-price state outside the
  engine. Use Google Play Billing for any digital entitlement. Never hardcode
  the displayed price. Never log purchase tokens, order ids or billing payloads.
- An active remove-ads entitlement must suppress every ad request and unlock
  every ad-gated convenience without an ad.
- Avoid adding dependencies for small utilities or minor visual effects.
- Prefer explicit, readable code over framework cleverness.
- Update documentation when changing rules or architecture.
- Never use protected logos, league or brand marks, photos, real-person data, or
  recognizably altered real identities without documented rights.
- Never commit production secrets, ad keys, signing keys, or service-account
  credentials.
- Haptics: <decide per product. Cyan banned them as decoration (its ADR-005);
  turqoise allowed them as cues only, citing the recorded re-check
  condition (its ADR-004). Record the call here and in an ADR.>
- Smoothness must be validated in profiled release builds on representative
  physical Android devices.
- Motion must communicate state. Avoid continuous decorative animation,
  layout-heavy effects, and nonessential background work.
- Never fabricate urgency, scarcity, rivalry or results. Never trigger
  advertising from a loss, near miss, or scarcity message.
- Outcomes must remain seeded, versioned, deterministic, and independent of
  ads, analytics, presentation settings, and difficulty.
- <Product-specific rules.>

## Definition of done

A component is complete only when:

- behavior is implemented,
- automated tests exist,
- failure states are handled,
- documentation is updated,
- dependency and performance impact have been reviewed,
- the feature works in a release build on a physical Android device.
