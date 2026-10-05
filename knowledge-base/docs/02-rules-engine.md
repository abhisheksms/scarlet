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
    val lastDialledAtMillis: Long?, // when the user last called this number themselves
)
```

The engine has no clock, no storage and no Android in it. Time and history arrive
as arguments, which is why every boundary can be unit-tested.

## The mode in effect (added 5 October 2026)

The lever is no longer the only thing that sets the mode. `ModeClock.at(settings,
atMillis, zone)` answers "which mode is in effect at this moment, why, until when, and
what follows":

1. **A running timer** (`timerUntilMillis`, `timerMode`). A pause is a timer at Off; a
   timer can also hold Silence or Block for a while.
2. **The schedule's hour**, while the schedule is switched on. `WeekSchedule` holds one
   value for each of the week's 168 hours: a mode, or nothing. The hour is the phone's own
   (the time zone is an argument), so a daylight-saving change or a journey is followed.
3. **The lever's own stop** (`mode`), when no timer runs and the hour asks for nothing.

The timer at Silence or Block and the schedule are Pro's. `Plans.limit(settings, tier)`
(`core/plans`) gives the settings as the user's plan may use them, and the settings store
applies it as it reads the file, so nothing downstream, the screening service first of all,
ever sees a schedule that has not been paid for. Nothing stored is thrown away.

`ModeNow` carries the mode, its source (`TIMER`, `SCHEDULE`, `LEVER`), the moment it next
changes by itself and the mode that follows. Home and the Quick Settings tile show it;
the rule list is built from it.

One timer the user does not start: after a call to an emergency number the app pauses
itself for 24 hours (`EmergencyPause.after`, ADR-008). It is an ordinary pause, so the rule
list, Home and the tile need to know nothing more.

`LeverMoves.move` says what moving the lever changes. A move always ends a running timer.
Outside the schedule's hours it sets the lever's own stop. Inside an hour the schedule has
set, the lever's own stop is left alone and the move holds until the schedule next
changes, stored as a timer: tonight is overridden, tomorrow night the schedule is back.

## Conditions

| Condition | Holds when |
|---|---|
| `Always` | always |
| `CallerIsContact` | the caller is in the user's contacts |
| `NumberAllowed(expiryByNumberKey)` | the number's key is in the map and its entry is permanent (`null`) or the call arrives before its expiry |
| `CalledAgainWithin(windowMillis)` | we handled this number less than `windowMillis` ago (and not "in the future") |
| `DialledWithin(windowMillis)` | the user called this number themselves less than `windowMillis` ago (and not "in the future") |
| `NumberIsDomestic` | the number is not from another country |
| `NumberInSeries(countryCode, nationalPrefix)` | the number's key (its E.164 form) starts with "+", the country code and the prefix: India's 140 and 160 series |

Boundaries are exclusive at the end: a timer until 18:00 no longer applies at 18:00;
an allow that expires at 18:00 no longer applies at 18:00; a 15-minute repeat window
no longer applies at exactly 15 minutes; a number called at 18:00 can ring back until
18:00 the next day and not at it; a schedule's hour ends as the next one starts.

## The list the app uses

`RuleBook.build(settings, allowList, atMillis, zone)` turns the user's settings into the
list for a call that arrives at that moment. It first asks `ModeClock` for the mode in
effect.

**When that mode is Off**, every call rings and the list says why:

| Mode's source | The list |
|---|---|
| the lever | `off`: `Always` → ALLOW |
| a timer (a pause) | `contact`, then `paused`: `Always` → ALLOW |
| the schedule's hour | `contact`, then `scheduled-off`: `Always` → ALLOW |

**When it is Silence or Block**, with everything switched on the list is, in order:

| # | id | Condition | Action | Present when |
|---|---|---|---|---|
| 1 | `contact` | `CallerIsContact` | ALLOW | always |
| 2 | `allow-list` | `NumberAllowed(map)` | ALLOW | the allow list is switched on |
| 3 | `you-called` | `DialledWithin(24 hours)` | ALLOW | call-backs are let through (on as installed) |
| 4 | `in-160-service` | `NumberInSeries(91, "160")` | ALLOW | always |
| 5 | `in-140-promotional` | `NumberInSeries(91, "140")` | BLOCK | the user has switched "Always block 140 numbers" on |
| 6 | `repeat-call` | `CalledAgainWithin(w)` | ALLOW | repeat callers are let through |
| 7 | `domestic-out-of-scope` | `NumberIsDomestic` | ALLOW | scope is "international only" |
| 8 | `unknown-caller` | `Always` | BLOCK or SILENCE | always; the action is the mode in effect |

The last rule's id says what chose the mode: `unknown-caller` for the lever,
`unknown-caller-on-timer` for a timer, `unknown-caller-on-schedule` for the schedule. It is
stored with each handled call, so the history can later say not only that a caller was
unknown but why the app was blocking at that hour.

Only two rules can block or silence: `in-140-promotional`, once the user has asked for
it, and the last. The order follows one principle: the user's own choices first (a
contact, a pause, the allow list, a number they called), then India's series, then the
automatic passes, then the default for an unknown caller. So a 140 number the user put on
the allow list rings, one they called themselves rings when it calls back, and one that
calls during a pause rings; but neither "international only" nor
the repeat-caller pass lets one through once the user has asked for 140 calls to be
blocked. A test asserts which rules can block, and one asserts the order.

## A number the user called (added 5 October 2026, ADR-007)

Android shows a screening app each outgoing call to a number that is not in the user's
contacts. The app notes the number's key and the time (`DialledNumberRecorder`, the
`dialled_numbers` table), and `you-called` lets that number ring when it calls back within
24 hours: the clinic, the courier, the support line. The fact reaches the engine as
`lastDialledAtMillis`, like every other fact, so the rule is tested without a phone.

A number is kept only while it can still let a call ring: each new outgoing call deletes
the older rows, nothing is written while the switch is off, and switching it off deletes
them all. The window is one setting (`callBackWindowMinutes`) with no control of its own.

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

## India's number series (added 4 October 2026)

TRAI assigns two series for commercial calls: **140** for promotional calls by registered
telemarketers, and **160** for service and transactional calls (1600: banks, insurers and
other regulated financial entities, government bodies; 1601: utilities, couriers and
logistics). Its third amendment to the TCCCPR (18 September 2026) prohibits call-management
apps from blanket blocking, filtering or tagging calls from these series, and keeps the
consumer's "full freedom to block, or filter calls on their own devices". Sources, all
TRAI's own or the government's press bureau: PIB releases of 12 February 2025, 19 November
2025 and 17 December 2025; TRAI press releases 91/2026 (10 July 2026) and 119/2026
(18 September 2026). `docs/play-repetitive-content.md` quotes them.

What follows for the list:

- `in-160-service` is always there while the lever is on, and has no switch. A blocker
  that rejects every unknown number would be blocking the bank's own call, which is both
  the thing TRAI forbids and the missed call that makes people switch a blocker off.
- `in-140-promotional` exists only once the user has switched it on, in Options; off as
  installed. Blocking promotional calls is the user's choice on their own device, never
  the app's blanket rule. When the deciding rule is shown on a screen, the word is
  "promotional", never "spam": the regulation forbids tagging these calls as spam.
- Both match on the number's key, so "1401234567", "01401234567" and "+91 140 123 4567"
  are one number, and the same digits under another country's code never match.

## Adding a rule

One `Condition`, one line in `RuleEngine.matches`, one row in `RuleBook.build`. No
caller changes: the service, the storage and the screens deal only in decisions.

A new way of choosing the *mode* (as the timer and the schedule are) is not a rule: it
is a line in `ModeClock`, and the rule list is then built for the mode it gives.
