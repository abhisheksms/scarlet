# 03 — The screening flow

What happens between Android handing the app a call and the phone ringing or not.
Platform facts here were checked against developer.android.com on 2 October 2026
(the pages are named in ADR-002 and ADR-005) and then on the emulator.

## Becoming the screening app

Android lets the user choose **one** call-screening app. The app asks with
`RoleManager.createRequestRoleIntent(ROLE_CALL_SCREENING)`; Android shows its own
dialog. The app asks only when the user picks silence or block, never at launch.
`RoleManager.isRoleHeld` is checked every time the home screen resumes, because the
user can hand the role to another app at any time from system settings.

## What Android sends, and what it does not

For each incoming call Android binds to the app's `CallScreeningService` and calls
`onScreenCall(details)`, **before the phone rings**. It does so only when:

- the call's handle is a `tel:` number;
- the number is **not in the user's contacts** (unless the app holds
  `READ_CONTACTS`, which this app does not);
- the number is not withheld: calls with a restricted, unknown, unavailable or
  payphone presentation are never passed to a screening app.

The details carry the number, the direction, the creation time and the caller's
verification status. Nothing else.

Android also binds to the service **when the user places a call** to a number that is not
in the contacts, so that a screening app can show who is being called. The direction says
which it is. Nothing has to be answered for an outgoing call (the reference for
`CallScreeningService`, read 5 October 2026, and seen on the emulator that day; ADR-007).

The app must answer with `respondToCall` **within 5 seconds**, or Android carries on
as if the call were allowed.

## What the app does

```
onScreenCall(details)
  a call the user is making:
      if Android says the number is an emergency number: pause for 24 hours   (DataStore)
      if call-backs are switched on, note the number's key and the time   (Room)
      return                                (nothing to answer)
  gather facts, with a 4-second cap:
      settings                              (DataStore)
      allow list                            (Room)
      last time this number was handled     (Room)
      last time the user called this number (Room)
      number -> key, isInternational        (core/numbers)
  rules    = RuleBook.build(settings, allowList, now, time zone)
  decision = RuleEngine.decide(rules, call)
  if the action is ALLOW:
      respondToCall(details, allow)         <- the phone rings
  if the action is SILENCE or BLOCK:
      store the handled call                (Room; a few milliseconds, so it is never missing)
      respondToCall(details, response for the action)      <- the phone rings silently, or not at all
      post a notification if they are on
      post a milestone notification if this call reached one
```

If gathering facts throws or runs out of time, the answer is ALLOW.

| Action | `CallResponse` | What the user gets |
|---|---|---|
| ALLOW | defaults | the phone rings |
| SILENCE | `setSilenceCall(true)` | the incoming-call screen appears, no ringtone or vibration from the ringer; if unanswered, a missed call in the system call log |
| BLOCK | `setDisallowCall(true)`, `setRejectCall(true)`, `setSkipNotification(true)` | the call is rejected as if declined; no missed-call notification; an entry of type "blocked" in the system call log |

`setSkipCallLog` is not used: Android ignores it for anything but the carrier's or
the system's screening app, and logs a blocked call as blocked regardless.

## What is stored

Handled calls: the number as it arrived, its key, the time, whether it was blocked or
silenced, and the id of the rule that decided. Allowed calls are not stored.

Numbers the user called that are not in their contacts, while call-backs are switched on:
the number's key and the time, one row a number, for 24 hours at most (ADR-007).

Nothing leaves the device.

## Process and timing

The service may be started cold by an incoming call, with no activity running.
`Application.onCreate` therefore does no slow work: no ads SDK, no network. The
decision needs one small preferences file and three indexed queries.
