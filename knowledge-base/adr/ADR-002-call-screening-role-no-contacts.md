# ADR-002: The call-screening role, and no contacts permission

## Status

Accepted (2026-10-02): on the documentation, and confirmed the same day on the emulator
(see Amendments).

## Context

The product promise is "calls from numbers not in your contacts do not interrupt
you" without the app ever reading the contacts. Android 10 introduced
`RoleManager.ROLE_CALL_SCREENING`: the user picks one app, and the platform binds to
that app's `CallScreeningService` for incoming calls before the phone rings.

Checked on developer.android.com on 2026-10-02
(`reference/android/telecom/CallScreeningService`, page last updated 2026-08-28):

> Only calls where the handle scheme is `PhoneAccount.SCHEME_TEL` are passed for call
> screening. Further, only calls which are not in the user's contacts are passed for
> screening, unless the `CallScreeningService` has been granted
> `Manifest.permission.READ_CONTACTS` permission by the user.

> Calls with a `Call.Details.getHandlePresentation()` of
> `PRESENTATION_RESTRICTED`, `PRESENTATION_UNKNOWN`, `PRESENTATION_UNAVAILABLE` or
> `PRESENTATION_PAYPHONE` presentation are not provided to the `CallScreeningService`.

> A `CallScreeningService` must respond to a call within 5 seconds.

The reference app's manifest, as the system reports it on the founder's phone,
requests no contacts permission and holds this role, which agrees with the above.

## Decision

- The app screens calls only through `CallScreeningService`, holding
  `ROLE_CALL_SCREENING`, requested with `RoleManager.createRequestRoleIntent` when the
  user first chooses silence or block.
- The app **does not request `READ_CONTACTS`**. "A contact always rings" is enforced
  by the platform; the engine keeps a `contact` rule as the written promise and as a
  guard (see `02-rules-engine.md`).
- The service is declared with `android:permission="android.permission.BIND_SCREENING_SERVICE"`
  and the `android.telecom.CallScreeningService` intent filter, exported so the
  system can bind, and nothing else can.
- The app never tries to be the default dialer.

## Consequences

Positive:

- No contacts, call-log or phone permission: nothing declaration-gated on Play, and a
  privacy claim that is literally true.
- The platform does the contact lookup, so the app cannot get it wrong.

Negative:

- Withheld numbers cannot be screened by this app. They ring. Said plainly in the
  scope document and to be said in the listing.
- Only one screening app can be active. If the user picks another, this one goes
  quiet; the home screen must show that state.
- A caller whose number is saved in contacts in a different form is the platform's
  matching problem, not ours, and not something the app can see.

## Amendments

- 2026-10-02, the spike. Emulator `scarlet_phone` (Android 16, API 36, Google Play
  image), this app holding the role after accepting Android's prompt from the home
  screen, no contacts permission, one saved contact (5559990001). Calls simulated with
  `adb emu gsm call`:

  | Call | Mode | Service | What Android did |
  |---|---|---|---|
  | 5551230001, not a contact | Block | `decision=BLOCK rule=unknown-caller` | no call left active; system call log row of type 6 (blocked), block reason 1 (call-screening service) |
  | 5551230002, not a contact | Silence | `decision=SILENCE rule=unknown-caller` | call state RINGING; Telecom event `SKIP_RINGING (Silent ringing requested)`; no ringtone player; after the caller hung up, a call log row of type 3 (missed) |
  | 5559990001, the contact | Silence | **not invoked** | Telecom event `FILTERING_COMPLETED ([Allow, logged, notified, contact exists])`, then `START_RINGER`; a ringtone player started |

  So the platform does skip a screening app for contacts when it has no contacts
  permission, exactly as documented. No contacts permission is needed.
- 2026-10-02: holding the role also grants `POST_NOTIFICATIONS` without a prompt
  (flag `GRANTED_BY_ROLE`), seen on the emulator for this app and on the founder's
  OxygenOS phone for the reference app. The in-context permission request still exists
  for the case where the user later revokes it.
