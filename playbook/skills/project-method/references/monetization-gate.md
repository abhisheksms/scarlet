# The monetization gate

Every Cyan Harbor app answers this before its design brief is written, because
the money surfaces must be designed with craft from the first board, never
retrofitted. The founder calls it; the recommendation goes on record with the
options, and the losing options stay in the ADR unedited (turqoise's ADR-006
is the worked example, in `../examples/`).

## The three shapes

**(a) The cyan model**: free, calm ads on non-core surfaces, one lifetime
remove-ads purchase.
- Proven: cyan's ads and billing adapters, consent flow, Data safety answers,
  privacy page and launch-gate tests port almost verbatim.
- Costs: the ads SDK is the heaviest native dependency the studio carries, and
  it brings the Data safety and privacy obligations with it. "Calm surfaces" can
  be scarce in a focused utility.

**(b) No ads**: a complete free core plus one lifetime "Pro" unlock (extra
content, themes, history, voices).
- The most founder-taste answer: price with self-respect, nothing hostile near
  the core loop, no ads SDK in the tree, same billing adapter.
- The free tier must be genuinely complete. Pro sells *more*; it never
  un-cripples. A crippled free tier is a dark pattern with extra steps.
- Revenue arrives later and depends on Pro content being desirable, so this
  choice and the v1 nice-to-haves gate interlock.

**(c) Paid upfront.**
- The cleanest product, almost no monetization code.
- Forfeits the search-install funnel that the category-keyword naming exists
  to win. Users in most categories don't pay before trying.

## Binding details for (a), whatever the app

- Digital goods through Google Play Billing only; one non-consumable; grant
  only on `PURCHASED`; acknowledge every purchase; Restore is authoritative;
  the entitlement prevents SDK initialization, default-deny until ownership is
  resolved; price always from Play, and no offer means no price shown.
- Ads only where the product's law allows (cyan: opt-in rewarded, never during
  bidding; turqoise: Home and Complete only, never in a session). Write the
  allowed surfaces down as a rule, and enforce it with a test that monetization
  surfaces cannot render in the forbidden states.
- No rewarded format unless the product has an honest reward to give.
- Google's test ad ids until immediately before production, pinned by a
  launch-gate test; live ids pinned by the same test after the switch.
- No fake urgency, no countdowns on a one-time purchase, true cost before any
  press, tests asserting dark-pattern copy is absent.
- The ads SDK enters the dependency tree at the monetization component, late
  in the build order, and nowhere earlier.
