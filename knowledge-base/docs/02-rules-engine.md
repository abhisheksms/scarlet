# 02 — The rules engine

Code: `core/src/main/kotlin/.../core/rules/`. Tests: `core/src/test/.../rules/RuleEngineTest.kt`.

## The model

A **rule** is three plain values:

```kotlin
data class Rule(val id: String, val condition: Condition, val action: Action)
```

- `action` is `ALLOW`, `SILENCE` or `BLOCK`.
- `condition` is one of a closed set of data classes (below).
- `id` names the rule in logs, in tests and in the stored record of a handled call.

The **rule list** is ordered. `RuleEngine.decide(rules, call)` walks it and returns
the action of the **first rule whose condition holds**, with that rule's id. A call
that matches nothing is allowed.

The facts a condition may look at are one small value object:

```kotlin
data class IncomingCall(
    val number: PhoneNumber,        // key, isInternational, display
    val receivedAtMillis: Long,
    val callerIsContact: Boolean,   // see "Contacts" below
    val lastHandledAtMillis: Long?, // when we last blocked or silenced this number
)
```

The engine has no clock, no storage and no Android in it. Time and history arrive
as arguments, which is why every boundary can be unit-tested.

## Conditions

| Condition | Holds when |
|---|---|
| `Always` | always |
| `CallerIsContact` | the caller is in the user's contacts |
| `PausedUntil(untilMillis)` | the call arrives before `untilMillis` |
| `NumberAllowed(expiryByNumberKey)` | the number's key is in the map and its entry is permanent (`null`) or the call arrives before its expiry |
| `CalledAgainWithin(windowMillis)` | we handled this number less than `windowMillis` ago (and not "in the future") |
| `NumberIsDomestic` | the number is not from another country |

Boundaries are exclusive at the end: a pause until 18:00 no longer applies at 18:00;
an allow that expires at 18:00 no longer applies at 18:00; a 15-minute repeat window
no longer applies at exactly 15 minutes.

## The list the app uses

`RuleBook.build(settings, allowList)` turns the user's settings into the list. With
everything switched on it is, in order:

| # | id | Condition | Action | Present when |
|---|---|---|---|---|
| 1 | `contact` | `CallerIsContact` | ALLOW | always |
| 2 | `paused` | `PausedUntil(t)` | ALLOW | a pause has been set |
| 3 | `allow-list` | `NumberAllowed(map)` | ALLOW | the allow list is switched on |
| 4 | `repeat-call` | `CalledAgainWithin(w)` | ALLOW | repeat callers are let through |
| 5 | `domestic-out-of-scope` | `NumberIsDomestic` | ALLOW | scope is "international only" |
| 6 | `unknown-caller` | `Always` | BLOCK or SILENCE | always; the action is the switch |

When the switch is off the list is a single rule, `off`: `Always` → ALLOW.

Every rule but the last lets a call through; only the last can block or silence.
That property is asserted by a test.

## Contacts

The first rule says a contact always rings. On Android 10 and later the platform
enforces this before the app is involved: a screening app without the contacts
permission is only handed calls from numbers outside the contacts (ADR-002). So the
service passes `callerIsContact = false` for every call it sees, and the rule is the
written statement of the product's promise plus a guard: if the app were ever given
contacts access, or a platform build behaved differently, the adapter would supply
the fact and the list would already do the right thing.

## Numbers

`PhoneNumbers(homeRegion).parse(text)` (in `core/numbers`) gives each number:

- a **key**: its E.164 form, so "+91 80 4567 8901" and "080 4567 8901" on an Indian
  phone are the same number for the allow list and for repeat-caller matching;
- **isInternational**: its country code differs from the phone's home country;
- a **display** form: grouped for reading, with the country code if the number
  arrived with one.

A short code, or text that is not a number, keeps its digits as the key and is never
international. A call with no number has an empty key and matches no allow entry.

The home country comes from the SIM, then the network, then the locale.

## Adding a rule later (phase two, as an example)

India's telemarketers call from the 140 series and banks from the 160 series. To add
"always allow 160, always block 140":

1. Add one condition: `data class NumberStartsWith(val nationalPrefix: String) : Condition`.
2. Add its line to `RuleEngine.matches`.
3. Add two rows to `RuleBook.build`, before `unknown-caller`:
   `Rule("in-160-services", NumberStartsWith("160"), ALLOW)` and
   `Rule("in-140-telemarketing", NumberStartsWith("140"), BLOCK)`.

No caller changes: the service, the storage and the screens deal only in decisions.
Where exactly the two rows sit relative to pause and the allow list is a product
choice to make then.
