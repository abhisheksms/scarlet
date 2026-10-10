# ADR-011: Plus, a subscription, holds the frequent callers and everything in Pro

## Status

Accepted (2026-10-10). The founder's call of that day: "for sure, this would be part of a
recurring subscription. Where I would like to keep the first few months free, I mean
that's up to you the pricing and all you can analyze yourself use your best marketing
acumen which ensures that we are retaining the customers but also making money". The
prices and the free stretch are Claude's recommendation, his to change (`PLAN.md` G14).

## Context

ADR-009 set three plans, each bought once, and ruled out a subscription, because the
category's angriest reviews are about a one-time purchase that turned into one. The
founder asked for the frequent callers (ADR-010) to be sold as a subscription. The two
are reconciled by keeping every one-time plan exactly as it is and adding the
subscription above them: nobody who buys once is ever moved to a subscription, and
nothing that is free today goes behind one.

A subscription needs something that keeps being delivered. Here it is: the record keeps
growing, the finder keeps finding, the blocked classes keep being stopped, and the list
and History show it every week. Google Play's own page on subscriptions (read 10 October
2026) asks for the trial's length, the price after it, that it converts on its own, and
how to cancel, all said before the purchase; and that a yearly price is never dressed up
as a monthly one.

## Decision

- **One subscription, `plus`, two base plans:** a month and a year. Plus holds the frequent
  callers (blocking, Auto-block, the Frequent Only scope) and everything in Pro (No Ads,
  the Timer, the Schedule, Number rules). While it runs the user's tier is Plus; when it
  ends they are back on whatever they bought once, Pro included.
- **The first two months are free**, on both base plans, as a free-trial offer in Play
  Console for a buyer who has never had the subscription. Google Play decides who is
  eligible and lists the offer only to them; the app shows whatever it is handed. Two
  months, not one or three: the finder needs days of calls before it lists anything and
  weeks before the relief is felt, and a longer stretch is forgotten by the time the first
  charge comes.
- **The prices to enter in Play Console, to start:** ₹49 a month and ₹299 a year in India;
  $1.99 and $11.99 in the United States as starting points. The reasons are the launch
  plan's Pricing section. They live in `billing/PlannedPrices.kt` for a test build to show;
  a build from Google Play shows only Google Play's own figures.
- **The page says the terms before the keys:** the free stretch over them ("2 months free,
  then"), each key named by its price and period ("₹49 a month", "₹299 a year"), and under
  them that it renews until cancelled in Google Play. The plate's large figure is the
  monthly price with "A month" under it; the yearly price is shown only as a year's price.
- **Manage Subscription**, a row on the plans screen for a subscriber, opens Google Play's
  own page for this subscription, which is where it is cancelled or changed. The app has
  no cancel key of its own.
- **Google Play is the only record**, as for the one-time plans: `queryPurchasesAsync` for
  subscriptions each time the app comes to the front; a subscription that has run out is
  no longer in the answer and the tier drops; a first purchase is acknowledged, renewals
  need no acknowledgement. The purchase is started with the offer token of the way of
  paying chosen. Nothing of it is kept or logged.
- **Pro owners pay the same.** A buyer who paid ₹199 once and then subscribes gets the
  frequent callers for the same monthly price as anyone; their Pro stays theirs for good.
  A cheaper Plus for Pro owners is possible later as a developer-determined offer in Play
  Console, and is not built.
- **Never:** a one-time plan turned into a subscription, a free feature moved behind Plus,
  a countdown, a claim of saving, a struck-through price that is not Google Play's own.

## Consequences

Positive:

- Recurring revenue from the feature that is the app's difference, and a retention loop
  that is real: the subscriber sees the blocked classes and their counts every time they
  open the app.
- The one-time plans stay honest and untouched; the reviews' complaint cannot be earned.
- A trial on Google Play's terms: the reminder before it ends is Google's, the cancellation
  is Google's, the eligibility is Google's.

Negative:

- **Nothing of this has been tried against Google Play.** The subscription, its base plans
  and its offer exist only once made in Play Console (launch plan 6.7); the offer's free
  phase is read as the pricing phase at no charge, written from Google's reference.
- **A trial can be unavailable to some buyers in India**: Google Play's own page says that
  where a payment method cannot take a recurring mandate, no-charge trials and auto-renewal
  are unavailable and a subscription becomes a single access pass. What those buyers are
  shown is Google Play's to decide.
- **A lapsed Plus subscriber who had set Frequent Only** is back on all unknown numbers at
  the lever's stop: more is blocked, not less. The display says so.
- Two prices and a free stretch on one plate are the most words the plans screen carries;
  it holds at 360 dp and 135% text, and is to be read by the founder on his phone.
