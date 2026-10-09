# What the paid unlock could hold

The founder's note, 4 October 2026: "Need to research for some premium features, some which
are really helpful maybe move some there and also remove ads in premium". This is that
research, done on 5 October 2026. Nothing here is built. The pick is gate G11 in
[`PLAN.md`](../PLAN.md).

## The short answer

- **Sell one thing, once: Pro.** It removes every ad, and it adds two features built for
  it: **a schedule**, and **rules by a number's first digits**. Everything that is free
  today stays free.
- **Start at ₹149, paid once.** Never a subscription, no trial wall, nothing that needs a
  server or a new permission.
- The two features are the ones the closest app on Play sells that this app can also
  build. The price sits inside what one-time unlocks in this category cost in India.

## What was decided (5 October 2026, evening)

The founder read this and asked for more than it recommended: "Add 2 premimum features: To
block during certain hours of the week. And to office silence or block for the next few
mins hours. Make it freemium, think of some marketing model, maybe 2 tiers, think of more
such features and add them". Built the same evening ([`NOTES.md`](../NOTES.md), N-42):

| Plan | Holds | Price to start (recommended) |
|---|---|---|
| **Free** | everything the app did before, with the ad tray | nothing |
| **No Ads** | the same app with no ads | ₹99, once |
| **Pro** | no ads, the **timer** (Off, Silence or Block for a while), the **schedule** (hours of the week) and, added later the same evening, **number rules** (always ring or always block numbers by how they start) | ₹199, once; ₹100 for someone who already has No Ads |

How it is meant to sell, without a single pop-up:

- **The features are on Home, where they would be used.** Without Pro the Timer and
  Schedule rows say in a few words what they do, carry the word Pro, and lead to the plans.
  They sit below the free rows, never above them.
- **The plans screen says the one thing this category's buyers ask for:** each is paid
  once, no subscription.
- **Two steps, not one.** The cheap step is what two reviewers of the modelled app asked
  for and could not buy. The dearer step is for people who want the app to run itself.
  Someone who took the first step pays only the difference for the second.
- **The free app is the advertisement.** It keeps every feature it had, so its reviews
  and its place in search are earned by the whole app, not by a cut-down one.
- **Prices are Play Console's to test.** The app shows whatever price Play returns.

What changed from the recommendation below: the timer took the place of the first-digit
rules as the second Pro feature (his choice), and there are two paid plans, not one (his
"maybe 2 tiers"). The first-digit rules were then built as Pro's third feature, under his
"think of more such features and add them" ([`NOTES.md`](../NOTES.md), N-45). The plans' names and prices are gate G12 in [`PLAN.md`](../PLAN.md).
Nothing can be bought yet: that needs the app in Play Console.

## How this was read

34 call-blocker listings on Google Play's India store (15 caller-ID apps with a spam
database and an account, 19 small blockers), their developers' own sites and help pages,
and the reviews each listing shows. The full table, with a link for every fact, is
[`reference/premium-tiers-2026-10-05.md`](reference/premium-tiers-2026-10-05.md); a
research sub-agent compiled it, and six of its facts were read again on the live pages
before this was written (the last section). Everything below is from a listing or a
developer's own page unless it says "a reviewer says".

Two limits. Play's web page shows one price range for all of an app's purchases, never a
price per item, so a range cannot tell a one-time unlock from a subscription. And a
listing shows about twenty reviews, so the complaints below are what was said, not how many
people feel that way.

## What the category sells

"Could this app build it" means: with Android's call-screening role alone, and with no
contacts, call-log, SMS or phone-state permission, no account and no server.

| Sold feature | Apps selling it, of 34 | Could this app build it? |
|---|---|---|
| Remove ads | 17 | Yes |
| A spam-number database that updates itself and blocks from it | 8 | No: needs a server and a live database |
| Look up who a number belongs to | 7 | No: needs a database |
| See who looked you up | 5 | No: needs accounts and a server |
| A schedule: block only in set hours or on set days | 5, one of them unclear | Yes |
| Block hidden numbers | 4, two of them unclear | No: Android does not hand those calls to a screening app |
| Rules by first digits, a number series or a pattern | 3 | Yes |
| Block other countries, or chosen ones | 2 | Yes. International-only is already free here |
| A bigger list | 2 | Yes. The lists here have no cap |
| Backup to the cloud | 2 | No. To a file on the phone: yes, and the closest app gives that free |
| A password on the app | 1 | Yes |
| Silence in place of reject; only contacts ring; no notification for a stopped call | 1 or 2 each | Free here already |

Also sold, and out of reach or beside the point for this app: call recording, an assistant
that answers for you, voice-fraud detection, SMS filtering, badges and video ringtones,
family plans.

## The closest app to this one

Gingko Lab's "Call Blocker - Robocall & Spam" (10,000+ installs, rated 4.4) uses the same
Android screening role. **Free:** block every number not in contacts, an allowed list, a
history, basic statistics. **Pro, paid once** (its listing shows ₹235 to ₹435 across its
items): hidden numbers, other countries, a carrier-verification check, a list with
prefixes, suffixes and ranges, and schedules. Its developer, answering a review: the paid
option is one-time, not a subscription, and blocking unknown calls is free.

Of what it sells, this app can build the schedule and the first-digit rules. Hidden
numbers it cannot: Android's own reference says calls with a withheld number are never
passed to a screening app. The carrier check depends on Indian networks filling in a
value nobody has confirmed they fill.

## What one-time unlocks cost in India

| App | What the listing shows | What it buys |
|---|---|---|
| CallBlocker Pro: Block Unknown | ₹199, one item | the whole app, after a 14-day trial; "no subscription" |
| Calls Blocker (RYO) | ₹160, one item | more than three numbers in the list, and no ads |
| CallDefendy | ₹120, one item | not stated |
| Gingko Lab's blocker | ₹235 to ₹435 | lifetime Pro |
| Calls Blacklist | ₹59 to ₹299 | a lifetime unlock, sold beside a subscription |
| Call Blocker (lithiumS) | ₹160 to ₹240 | a reviewer says ad removal |

The studio's own first app sells its one unlock at ₹149. That is inside this band, so it is
the price to start from; the taste doc's rule applies as written: test it in Play Console
after a few weeks, and discount later rather than raise later.

## What people complain about, and what they praise

- **A one-time purchase turned into a subscription.** Seven reviews of one blocker between
  July and September 2026, most at one star. One says a single payment is the most they
  would accept; others had paid for lifetime ad removal and say the ads came back.
- **Basics behind the paywall.** One blocker's reviewers say its contacts allow-list, its
  pause and the switch for its own notification all need its Pro, and that other apps give
  these free. Another's listing says the free list stops at three numbers.
- **Paying and still seeing ads.** Three apps, in reviews from July and August 2026.
- **Full-screen ads**, above all after a call or ones that cannot be closed, draw the
  angriest reviews. Small banners along the bottom draw mild ones, with one exception that
  793 people marked helpful.
- **A purchase lost on a new phone**, where it hangs on the app's own account.
- **People asking to pay once to be rid of ads.** Two of them are on the listing of the
  app this one is modelled on (27 June and 16 September 2026). Its developer's answer: the
  app is ad-supported and no ad-free purchase can be promised.
- **Praise** goes to free apps and to one-time unlocks: one reviewer calls a lifetime
  option the best value, and another bought it only to support the developer.

## What that means here

**In Pro**

1. **No ads at all.** The banner and the full-screen ad both go, and the ads code is not
   even started for someone who has paid: "paid and still see ads" is the complaint to
   never earn.
2. **A schedule.** The lever sets itself by the clock: Block at night, Off in the day, or
   only on working days. Five apps sell it, the closest app among them. It needs the time
   and nothing else.
3. **Rules by first digits.** "Always block numbers that start with …", or always allow
   them: a number series, an area, a country by its code. Three apps sell it. India's 140
   switch and the rule that 1600-series calls always ring stay free and stay as they are.

**Free, as today:** the three modes, pause, the repeat-caller pass, the allow list, the
international-only choice, History, Statistics, the summary, the per-call notification and
its switch, the Quick Settings tile, the India rules, How It Works, and the reason and
Always Block that are planned. The app this one is modelled on gives its whole feature set
free and sells nothing, and the reviews above show what happens to a blocker whose free
version does less than its neighbours'. The three differences that Play's Repetitive
Content rule rests on have to be seen by every user, so they cannot sit behind a purchase.

**Not for sale, because this app cannot or should not do it:** a spam database, number
lookup, caller names, hidden-number blocking, SMS, call recording, anything with an account.

**Could join Pro later, not now:** a backup of the lists and settings to a file (the
closest app gives this free, so it is a weak thing to charge for) and a password on the app
(one app sells it).

## "Maybe move some there": should anything free today move behind Pro?

Recommended: nothing. Nobody would lose a feature, since the app is not published, but the
free app would then do less than the app it is modelled on, and "this is free elsewhere"
is the review this category writes most readily.

If one thing is to move anyway, the least harmful is the lower half of Statistics: the
weekday and hour charts and the 30-day and 90-day periods. The counter, the last seven days
and the most frequent numbers would stay free. That is option (c) below.

## The choice (gate G11)

| | Pro holds | For | Against |
|---|---|---|---|
| (a) | no ads | smallest to build; what two reviewers of the modelled app asked for | nothing "really helpful" in it |
| **(b)** | no ads, a schedule, first-digit rules | the two sellable features this app can build; nothing taken from the free app | two features to design and build before Pro can launch |
| (c) | as (b), and Statistics' charts move behind it | a fuller Pro | the free app falls below the modelled app's |

Recommendation: **(b), at ₹149.**

## What it takes to build

- **The purchase.** Google Play's billing library, one product, paid once. The product is
  created in Play Console, which is the founder's step; numbered steps come with the build.
  Restore Purchases is a row in Settings, and Play carries the purchase to a new phone with
  the Google account, so there is no account of our own to lose it with. The prototype's
  "Later" frame already draws both rows in Settings.
- **The schedule and the rules** are each a new kind of rule in the engine, which was built
  for that (ADR-004), and each needs a screen drawn in the Switchboard look first.
- **Paper.** A decision record for the purchase, "purchases handled by Google Play" in the
  Data safety answers and the privacy page, and tests that pin the product's id.

## Three side findings

- **For gate G6 (full-screen ads).** The harshest ad reviews in the category are about
  full-screen ads. This app shows one at most every three minutes, on leaving History or
  Statistics; the research is a reason to keep it that rare or to drop it.
- **Asked for, and cannot be built:** blocking on one SIM only, which a reviewer of the
  modelled app asked for. Android tells a screening app the number, the direction, two
  times and a verification value, and nothing about the SIM.
- **Not unique any more:** one paid blocker lists a Quick Settings toggle, and one small
  India-focused blocker lists number-series blocking with 140 as its example. Neither is the
  app this one is modelled on, which has neither, so the record in
  [`play-repetitive-content.md`](play-repetitive-content.md) stands.

## What was checked again, and what was not confirmed

Read again on the live pages on 5 October, after the table was compiled: the closest
app's price range and its developer's two replies (one-time, not a subscription; schedules
are in the paid option); CallBlocker Pro's ₹199 and "one-time purchase, no subscription";
RYO's ₹160; on the modelled app's listing, the "Contains ads" label, the request to pay
for ad removal and the developer's answer; and Android's reference page for
`CallScreeningService` (last updated 16 September 2026) on calls with a withheld number.
All six matched.

Not confirmed:

- Whether a purchase is one-time or a subscription for about thirteen of the 34 apps:
  their listings, sites and replies do not say.
- Rupee prices of single items, except where a listing has only one item.
- Truecaller's own pricing pages, which could not be read; its plan prices come from its
  App Store listing for India.
- Whether hidden-number blocking works in the two apps that sell it, and how. Not tested
  on a device.
- Whether Indian networks fill in the verification value Android offers.

## Prices read again (10 October 2026)

The founder, on 10 October: "think as a marketing agent on the pricing that would make
the user pay money, the pricing needs to be competitive". So the listings were read again
that day, on Play's India store and, for the first time, as a buyer in the United States
sees them. What follows from it is in [`launch/LAUNCH_PLAN.md`](launch/LAUNCH_PLAN.md),
"Pricing": the numbers stay at ₹99, ₹199 and ₹100.

| App on Play | Installs shown | India, per item | United States, per item |
|---|---|---|---|
| CallDefendy: Call Blocker | 1,000+ | ₹120 | $1.09 |
| Calls Blocker (RYO Software) | 10,000+ | ₹160 | $1.49 |
| CallBlocker Pro: Block Unknown | 100+ | ₹199 | $4.99 |
| Call Blocker (lithiumS) | 100,000+ | ₹160 to ₹240 | $1.99 to $2.49 |
| Call Blocker - Robocall & Spam (Gingko Lab) | 10,000+ | ₹235 to ₹435 | $8.99 to $14.99 |
| Calls Blacklist - Call Blocker | 10,000,000+ | ₹59 to ₹299 | $1.99 to $14.99 |
| Unknown Call & Contact Blocker (Growtons) | 100,000+ | ₹10 to ₹99 | $0.99 to $9.99 |
| Should I Answer? | 1,000,000+ | ₹180 to ₹820 | $1.99 to $9.99 |
| CallShield: Spam Call Blocker | 5,000+ | ₹50 to ₹300 | not read |
| Spam Call Blocker: Call Shield (BharatSoft Labs) | 1,000+ | ₹49 to ₹499 | not read |
| Call Blocker - Phone app (KiteTech) | 10,000,000+ | ₹25 to ₹999.99 | not read |
| Block calls and spam texts (KiteTech) | 50,000,000+ | ₹25 to ₹500 | not read |
| Call Blocker - Phone - ID (Applika) | 1,000,000+ | ₹260 to ₹1,300 | not read |
| Truecaller | 1,000,000,000+ | ₹10 to ₹8,499 | not read |

- **Nothing moved in five days** among the one-time unlocks: the six prices this document
  compared on 5 October are the same. One range changed (BharatSoft's: ₹59 to ₹799 then,
  ₹49 to ₹499 now), and one caller-ID app's listing, Hiya's, no longer opens on Play under
  the package id read on 5 October, in India or in the United States.
- **The app this one is modelled on** still lists no in-app purchases.
- **India is priced on its own by the apps that bother.** CallBlocker Pro and the closest
  app charge far more in the United States than their India price converts to. The three
  cheapest (CallDefendy, RYO, lithiumS) look converted from one base price.
- **How it was read.** Each listing's page was fetched with the country set (`gl=IN`, then
  `gl=US`) and the range taken from the app's own block of the page's data, the one that
  carries its install count; the same page also carries the ranges of the developer's
  other apps and of similar apps, which were left out. Nothing was installed or saved.
  Grade A for what the page shows. As before, a range covers every item an app sells, so
  only the three single-item apps give an exact price. The United Kingdom's page showed
  this reader no price line, so no pound prices are given.
