# ADR-008: After a call to an emergency number, the app pauses itself for a day

## Status

Accepted (2026-10-05). Claude's call inside the founder's ask of that day ("think of
more such features and add them"). His to overrule.

## Context

After someone calls an emergency number, the calls that follow come from numbers nobody
has saved: a control room ringing back, an ambulance crew asking for the gate, a hospital.
Those are exactly the calls this app stops, and the app's first promise is that it never
costs a call that mattered.

What Android does by itself, read in Telecom's source and seen on the emulator on
5 October 2026 (ADR-007 has the details): for an incoming call the network marks as an
emergency call, and for every call while the phone is in emergency callback mode, Telecom
skips call filtering, so a screening app is not asked. On the emulator that mode was set
to last five minutes. Whether a phone on an Indian network enters it after a call to 112
is not known here, and five minutes is short for a call back from a hospital.

What another platform does: Apple's Phone app turns its own screening of unknown callers
off for 24 hours ("If you call emergency services, call screening turns off for 24
hours", support.apple.com/111106, page dated September 2026, read 5 October 2026).

What the app can know without a permission: Android shows a screening app the outgoing
call to an emergency number like any other outgoing call to a number outside the contacts
(seen on the emulator), and `TelephonyManager.isEmergencyNumber` answers for the phone's
own SIMs, network and list with no permission (its reference page, dated 28 August 2026,
names none; seen answering on the emulator).

## Decision

- When the app is shown an outgoing call to a number Android calls an emergency number,
  and the settings can stop a call at all, it starts **a pause of 24 hours**: the same
  pause the user starts from Home, shown there with its end time, ended early by Resume or
  by moving the lever. A pause or timer already running is replaced by it.
- **No switch.** Like the 160 series, this is a floor, not a preference. The user who
  wants filtering back presses Resume.
- When the lever is at Off and nothing else filters, nothing is changed: there is nothing
  to pause.
- How It Works says it in one sentence. Home does not say why it is paused; a line for that
  is a note for the next design round.
- The decision of what changes is in `core` (`EmergencyPause.after`), so it is tested
  without a phone. The app side is two small pieces: `EmergencyCallPause` and one function
  that asks Android whether a number is an emergency number.

## Consequences

Positive:

- The call back after an emergency rings, for a day, whatever the lever said.
- It fails towards the app as it was: if a phone's Android did not show the app that
  outgoing call, or could not say whether the number is an emergency number, nothing
  changes.

Negative:

- A pocket-dialled emergency call also pauses the app for a day. The pause is on Home with
  Resume beside it.
- The user who moves the lever back to Block within the day ends the pause: that is their
  act, and the lever always wins.
- **It can never be tried on a real phone**, because nobody calls an emergency number to
  test an app. The evidence is the unit tests and emulator check 23, which puts a made-up
  number on Android's own test list of emergency numbers, on the emulator only, and calls
  that.
