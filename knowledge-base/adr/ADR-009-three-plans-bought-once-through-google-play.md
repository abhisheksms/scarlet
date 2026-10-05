# ADR-009: Three plans, each paid plan bought once through Google Play

## Status

Accepted (2026-10-05). The founder's ask of that day: "Make it freemium, think of some
marketing model, maybe 2 tiers". The plans' names and prices are his to confirm
(`PLAN.md` G12).

## Context

The studio's default is a free app with one lifetime remove-ads unlock, no tiers. For
this app the founder asked for paid features and perhaps two tiers. What the category
sells and what its buyers complain about is in `docs/premium-research.md`: one-time
prices between about ₹120 and ₹299, and the sharpest reviews about subscriptions that
replaced a one-time purchase, basics moved behind a paywall, and ads still shown after
paying.

Google Play requires digital goods, ad removal included, to be sold through its own
billing, at its own price.

## Decision

- **Three plans.** Free: the whole app, with ads. No Ads: the same app without ads. Pro:
  no ads, and the timer, the schedule and number rules. Nothing that was free moved.
- **Three products in Google Play, each bought once, none consumable:** `no_ads`, `pro`,
  and `pro_upgrade`, which is Pro for someone who has No Ads, priced at the difference.
  No subscription. The ids are pinned by `LaunchGateTest`: they are permanent once made
  in Play Console.
- **Google Play is the only record of what the user owns.** Each time the app comes to
  the front it is asked what has been bought, and the answer sets the tier the settings
  keep. A purchase that has not been paid for yet grants nothing. When Google Play cannot
  be asked, the tier stays as it was last told: a failure neither grants a plan nor takes
  one away. A refund shows up as a purchase that is no longer there, and the tier drops
  at the next answer.
- **A purchase is acknowledged** once it has been paid for, as Google Play requires
  within three days.
- **The price shown is Google Play's own text**, and nothing is offered until Google Play
  has given one. A test build, which Google Play sells nothing to, shows the planned
  prices instead and says that is what they are (added 6 October 2026, at the founder's
  "add pricing"); a build from Google Play never shows them. `PriceLineTest` holds that.
- **Restore Purchases** asks again at the user's press and says what it found.
- **Nothing of a purchase is kept or logged**: no token, no order id. The settings hold
  the tier's name and nothing else.
- **What a plan may use is decided in one place** (`core/plans/Plans.kt`) and applied as
  the settings are read, so the screening service, Home and the tile cannot disagree.
- **Ads are never started for a paid plan.**
- **A test build can try each plan** with keys on the plans screen. A release build has
  none; the switch is compiled out with `BuildConfig.DEBUG`.
- The library is Google's Play Billing Library, version 9.1.0, the one the studio's first
  app ships. It adds one permission, `com.android.vending.BILLING`, which lets the app
  talk to Google Play's own purchase service and nothing else.

## Consequences

Positive:

- The cheap step is what two reviewers of the modelled app asked for and could not buy;
  the dearer step is for people who want the app to run itself.
- A user who changes phone, or reinstalls, has their plan back without doing anything.

Negative:

- **A purchase cannot be tried until the app exists in Play Console**, with the three
  products made and active, and then only on a copy installed from Play by a licence
  tester. Until then every path but the purchase itself is what has been checked.
- The upgrade's price is the difference between two other prices only for as long as
  someone keeps it so in Play Console.
- The tier the settings keep is a copy. Someone with a rooted phone can change it and
  get No Ads or Pro without paying, until Google Play is next asked. The studio accepts
  that for conveniences; it would not for anything that gave an advantage over other
  people.
- Two more screens of Google's (the purchase sheet and its result) sit in the app's
  path, and they are Google's to change.
