# 06 — Frequent callers, and Plus

Code: `core/src/main/kotlin/.../core/frequent/`, `RuleBook.startRules`, `app/.../screening/SeenCallRecorder.kt`,
`FrequentCallerWatch.kt`, `app/.../ui/FrequentCallersScreen.kt`, `billing/PlayStore.kt`.
Tests: `FrequentCallersTest`, `RuleEngineTest` (the frequent-caller tests), `PlansTest`,
`PlanOffersTest`, `PriceLineTest`; emulator check 28. Decisions: ADR-010 and ADR-011.

## What it is

The callers that keep coming back to this phone, found by the app itself in its own record
of calls, listed with their figures, and blocked with one key. The ask, 10 October 2026:
"a class of numbers which look very similar and appear every day or appear very commonly",
blocked "whereas allowing the other numbers which could be important".

## What the app sees

Android hands a screening app every call from a number outside the user's contacts: the
number and the moment (ADR-002). Since this round the app notes each such call while it is
on, allowed or not, as a `SeenCall(numberKey, atMillis)` in the `seen_calls` table, kept
sixty days. At Off, a pause included, nothing is noted. History is unchanged: it shows the
calls the app stopped (ADR-003).

## What a frequent caller is

`FrequentCallers.classes(calls, now, zone, settings, allowedKeys, dialledKeys, limits)`,
pure Kotlin, over the last sixty days:

| Kind | `start` | Listed when |
|---|---|---|
| A class | what its numbers share: the key less its last three digits (a thousand numbers), or less its last four where the callers spread wider than one block and the wider class has three numbers of its own | at least 3 different numbers, 6 calls, on 3 different days |
| One number | its whole key, a short code included | at least 3 calls, on 3 different days; not when it is inside a class listed |

Left out before counting: a number on the allow list, a number the user called (as far as
the app still keeps those, ADR-007), anything one of the user's number rules covers, a
class already blocked, and India's two reserved series (140 has its own switch; 160 always
rings). A start is never shorter than "+", the country code and one digit. The list is the
busiest first, at most twenty. `FrequentCallers.figures(calls, start, …)` gives the live
figures of a blocked start, however few its calls now are. The thresholds are
`FrequentLimits`, one place.

## When it runs

After every call the app is asked about, once the answer has gone (`ScreeningService`,
then `SeenCallRecorder.note` and `FrequentCallerWatch.afterCall`): the record is read, the
classes found, and a class not announced before is announced once in a quiet notification
on the "Frequent callers" channel that opens the list. With Auto-block on (Plus, off as
installed) the new class is blocked at the same moment and the notification says blocked.
The announced starts are kept in the settings so nothing is announced twice. The screens
work the list out again from the record whenever it or the settings change.

## What blocking does

A blocked start is kept in `ScreeningSettings.frequentCallers` (newest first, one line of
text in the settings file, `NumberRules.encodeStarts`). `RuleBook.startRules` turns each
into `Rule(frequent-caller, NumberStartsWith(start), the lever's action)`: silenced at
Silence, cut off at Block, placed by length among the user's own number rules and the
series. At the same length the user's own rule comes first, then a blocked frequent
caller, then a series. A longer Always Ring rule inside a blocked class lets that one
number through. No automatic pass (a repeat call, the international scope) lets a blocked
frequent caller through.

**Frequent Only**, the third scope: after the start rules and the passes, `others-out-of-scope`
lets every other call ring, so the lever's stop acts on the blocked frequent callers alone.
At Off, and during a pause, every call rings as before.

## The screens

- **Options › Unknown numbers**: a third key, Frequent Only, on Plus. Under it, a row,
  Frequent callers, with the figures as its state: "2 blocked · 1 found", or "None yet".
- **Frequent callers**: Auto-block (a switch on Plus; the word Plus and a way to the plans
  on the others); Found, each row the shared start with an ellipsis or the number, and its
  figures (calls · numbers · days), with a Block key on Plus or the word Plus; Blocked, the
  same rows with a cross that unblocks.
- **Home**: "Blocking frequent callers" / "Silencing frequent callers" at the Frequent Only
  scope, and the lever's legend to match.
- **Plans**: Plus first, the monthly price as the plate's figure with "A month" under it,
  its rows (Frequent callers, Timer, Schedule, Number rules, No Ads), then "2 months free,
  then" over two main keys named by price and period, and "Renews until cancelled in Google
  Play." under them. A subscriber has a Manage Subscription row that opens Google Play's
  own page for it.
- **How It Works**: a section, Frequent callers, and a line on Plus under In Pro and Plus.
- **The notification**: "1 frequent caller found", or "… blocked" with Auto-block on.

The words: "frequent callers", never "spam". A row names the thing and shows figures; the
explaining is in How It Works (falcon, `FOUNDER_TASTE.md` §14).

## Plus

`Tier.PLUS`, above Pro; `Plans.has(PLUS, FREQUENT_CALLERS)` only; Pro's three features on
Pro and Plus. One subscription in Google Play, `plus`, with a monthly and a yearly base plan
and a free-trial offer of two months on each for a buyer who has never had it. `PlayStore`
asks Google Play for the subscription beside the one-time products, reads each base plan's
terms from the offers the buyer is eligible for (the last pricing phase is the price that
recurs, a phase at no charge is the free stretch), and starts a purchase with the chosen
offer's token. A running subscription in `queryPurchasesAsync(SUBS)` is Plus; one that has
run out is simply absent. Without Plus, `Plans.limit` empties the blocked list, switches
Auto-block off and reads Frequent Only as all unknown numbers; nothing stored is lost.

A test build shows the planned terms (`PlannedPrices.plus`: ₹49 a month, ₹299 a year, two
months free) where Google Play has none, and its keys give the plan without a payment.

## Not confirmed

- Nothing of the subscription has met Google Play: the product, its base plans and the
  offer wait for Play Console (launch plan 6.7); the reading of a free-trial phase is from
  Google's reference.
- Whether the thresholds fit real phones: three days, six calls, three numbers were chosen
  by reasoning. The founder's phone is the first real record.
- The class widths outside India's numbering plan.
- Whether a trial is available to every buyer in India (Google Play's note on recurring
  mandates).
