# ADR-006: Monetization model

> Worked example from turqoise (the second Cyan Harbor project, August 2026),
> kept verbatim. The template version is in `../templates/`.

## Status

Accepted (2026-08-14) — the founder called G2: **option (a), the cyan
model**. The options below, including the recommendation that was on record
while this ADR was Proposed, stand unedited for the archive; the Decision
section is what binds.

## Context

Cyan's model — free, calm rewarded ads, one lifetime remove-ads unlock —
shipped clean and honest. This category is different: competitors stuff ads
exactly where users are most vulnerable (sweating, resting, mid-set), and
the founder's rule for turqoise is stricter than cyan's ever needed to be:
**nothing may ever interrupt a running workout — no ad, no prompt, no
upsell, no rating beg, under any circumstance, mid-session** (PLAN.md §2.7).

That rule is not one of the options below. It binds all of them.

## Options

**(a) Cyan clone** — free + calm ads on non-session surfaces + one lifetime
remove-ads unlock.

- Proven mechanics; cyan's ads and billing adapters port almost verbatim.
- But "calm surfaces" barely exist in a utility this focused: home and the
  finish summary are the whole app outside a session. Ads there tax exactly
  the screens the thesis says must feel instant and glanceable — and the ads
  SDK is the heaviest native dependency either app has ever carried.

**(b) No ads at all** — free core + one lifetime "Pro" unlock (voice packs,
themes, history log — the G5 deferrals). **Recommended.**

- "No ads, ever" is a genuine differentiator in a category defined by ad
  slop, and the most founder-taste answer: price with self-respect, revenue
  through craft, nothing hostile near a workout.
- One honest non-consumable; cyan's billing adapter and entitlement
  repository reuse directly; no ads SDK ever enters the dependency tree.
- The free core must stay genuinely complete — Pro sells *more*, it never
  un-cripples. A free tier that feels crippled is a dark pattern with extra
  steps.
- Costs: revenue arrives later than ad revenue would, and conversion depends
  on Pro content being desirable — so G2(b) and the G5 scope call interlock
  and should be decided together.

**(c) Paid upfront.**

- The cleanest product; almost no monetization code.
- But it forfeits the search-install funnel the G3 naming strategy exists to
  win, and this category's users do not pay before trying. The weakest fit
  with the distribution thesis; the recommendation stands against it.

## Decision

Turqoise ships the cyan model: a **free app, with ads, and one lifetime
remove-ads purchase**. The binding details — product law, not preferences:

- **Ads appear only on calm non-session surfaces.** The candidate surfaces
  are Home and Complete/summary — and only those. Never on Ready, never on
  the live timer in any state, never in the builder.
- **The no-interruption rule is unchanged and binds ads absolutely.**
  Nothing appears during a session, ever (PLAN.md §2.7, doc 01).
- **Ad format and placement specifics are a design question** for the
  brief's surfaces, decided in founder rounds — this ADR fixes where an ad
  may exist, not what it looks like.
- **The billing and entitlement architecture is cyan's, verbatim:** one
  non-consumable Play product (remove-ads, lifetime); grant only on
  `PURCHASED`; acknowledge every purchase; restore is authoritative; the
  entitlement kills all ads *and* ad-SDK initialization — default-deny
  until ownership is resolved; Google **test** ad ids until immediately
  before production, with a launch-gate test enforcing it; purchase tokens
  never appear in logs, analytics, or anywhere else.
- **No rewarded formats.** A timer has no skip-gate analog, so there is no
  honest rewarded placement to build; rewarded ads are out unless a future
  founder call adds one.

## Consequences

- `EntitlementRepository` (doc 04) is live scope, no longer dormant; the
  ads and billing adapters join the native adapter table, and Component 9
  (doc 05) is committed — still late in the build order, and no
  monetization code exists before it.
- The ads SDK — the heaviest native dependency either app has carried —
  enters the dependency tree at Component 9 and nowhere earlier. The
  remove-ads entitlement prevents its initialization outright, so an
  owner's build never runs ad code.
- The design brief now designs the ad slot (Home and Complete only) and the
  remove-ads purchase sheet; the slot exists from the first board, so it is
  designed with craft rather than retrofitted.
- Cyan's integrity rules carry over wholesale: no fake urgency, no
  countdowns on a one-time purchase, true cost stated before any press,
  localized store price always displayed, tests asserting the absence of
  dark-pattern copy.
- The no-interruption rule stays enforceable in code: monetization surfaces
  may render only on home and post-summary states, never while a session is
  running — and a test asserts exactly that.
