# ADR-007: Google Play Billing for one-time ad removal

> Carried over from Cricket Auction Simulator. Re-accept or supersede it for
> the new app and record the date in Status.

## Status

Accepted

## Context

Version one is primarily ad-supported. Some users will value a simple,
low-priced way to remove advertising, but a general payment system,
subscription, custom backend, or premium economy would add disproportionate
policy, security, dependency, and maintenance cost.

Ad removal is a digital in-app benefit in a Google Play-distributed Android
application. Purchase handling must also remain independent of the auction
engine and must not create a competitive advantage.

## Decision

- Add one version-one paid product: `remove_ads_lifetime`.
- Configure it in Google Play as a one-time non-consumable.
- Launch in India at a ₹49 price hypothesis and display the localized price
  returned by Google Play. *(Price superseded: see Amendments.)*
- Use Google Play Billing in the Play-distributed build.
- Do not add a separate payment aggregator, web checkout, external purchase
  link, alternate billing, subscription, consumable, or paid gameplay product.
- Keep billing, purchase coordination, entitlement persistence, and ad-access
  policy in the mobile application boundary.
- Keep the auction engine, AI, content, and season simulator unaware of billing
  and entitlement state.
- Suppress every ad initialization, request, preload, and display while the
  entitlement is active.
- Give entitled users direct batch skip without a rewarded ad.
- Handle pending, purchased, cancelled, failed, unavailable, restore, refund,
  and revocation states explicitly.
- Grant only from `PURCHASED`, apply idempotently, and acknowledge the
  non-consumable purchase.
- Preserve a verified local entitlement during offline and transient store
  failure.
- Deny ad access until initial ownership reconciliation resolves, preventing ad
  initialization for an owner after reinstall.
- Re-query and retry interrupted acknowledgement idempotently on the next
  billing connection without persisting raw purchase tokens in application
  domain storage.
- Do not expose purchase tokens, order identifiers, raw receipts, or billing
  payloads to analytics, crash reporting, logs, saves, shares, AI, or the
  engine.
- Accept a client-only version-one implementation because there is no custom
  backend and the entitlement is low-value and non-competitive.
- Select the concrete maintained React Native billing bridge during Component
  13 after an Expo development-build and release-impact spike. *(Selected:
  see Amendments.)*

## Consequences

Positive:

- Users receive a clear, honest low-cost ad-free option.
- Google Play owns payment methods and checkout policy.
- Version one avoids a subscription and general payment architecture.
- Billing failures cannot corrupt or block the game engine.
- Entitled users do not pay and then encounter rewarded-ad gates.

Negative:

- The app gains a native billing dependency and Play Console operational work.
- Purchase, restore, pending, refund, offline, and reinstall paths require
  physical-device testing.
- Client-only verification is less tamper-resistant and less immediate for
  refund reconciliation than a secure backend.
- The ₹49 price remains a hypothesis until real usage supports a price
  experiment.

## Revisit when

- more paid products or subscriptions are proposed,
- iOS or cross-platform entitlement sharing is required,
- fraud or refund latency becomes material,
- user accounts or a custom backend are introduced,
- policy or regional billing requirements change.

## Amendments

- **Price (before launch, 2026):** ₹49 → ₹99 → ₹149 by founder decision. The
  decision to display only Google Play's localized price stands; since 1.0.1
  the purchase is hidden when Play has no offer.
- **Billing bridge (September 2026):** `react-native-iap` 16 (OpenIAP, Play
  Billing Library 9.1), chosen during the Expo SDK 57 upgrade because Play
  requires Billing Library 8+ for new apps.
- **Launch (2026-09-25):** 1.0.0 was submitted to Google Play. A license-test
  purchase was granted, acknowledged and restored on the internal testing
  track. Buyers outside India can pay only after the cross-border (RBI PA-CB)
  verification of the studio's payments profile.
