# ADR-007: A number the user called may ring back; the app keeps such numbers for a day

## Status

Accepted (2026-10-05). Claude's call inside the founder's ask of that day ("think of
more such features and add them"). His to overrule.

## Context

A blocker that stops every unknown number also stops the one unknown call the user
asked for: the clinic, the courier, the support line or the tradesman ringing back. The
pause covers it only if the user remembers to press it first.

Android shows the user's chosen screening app each **outgoing** call to a number that is
not in the contacts, as well as each incoming one. The reference for
`CallScreeningService` (developer.android.com, page dated 16 September 2026, read
5 October 2026) says the framework binds to it "when incoming calls are received (prior
to ringing) and when outgoing calls are placed", and Telecom's own source does the
binding only when the number is not a contact or the app holds the contacts permission
(`CallsManager.java`, `bindForOutgoingCallerId`, main branch, read the same day). Seen on
the emulator the same day: a call placed to a non-contact reached `onScreenCall` with the
outgoing direction. No permission is involved, and nothing has to be answered: only an
incoming call waits for `respondToCall`.

So the app can know that the user called a number without reading the call log, which
ADR-003 rules out.

## Decision

- A rule, `you-called`: an incoming call from a number the user called less than 24
  hours ago is allowed. It stands with the user's other choices (a contact, the allow
  list), ahead of every rule that can block. On as installed, with one switch in Options
  (Call-backs).
- To know it, the app keeps each such number's key and the time of the call in its own
  Room table, `dialled_numbers`: one row a number, the latest call replacing the earlier.
- A row is kept for as long as it can still let a call ring, and no longer: each new
  outgoing call deletes the rows older than the window. While the switch is off nothing is
  written, and switching it off deletes every row.
- The table never leaves the device and is not backed up, like the rest of the app's data.
- Nothing is shown of it: there is no list of dialled numbers on any screen, and the
  number is never written to the log.
- The window is one value in the settings (`callBackWindowMinutes`, 24 hours) with no
  control of its own. A choice of lengths can be added if anyone asks for one.

## Consequences

Positive:

- The call the user asked for rings, without their doing anything first. It needs no
  permission, so the manifest and the Data safety form are unchanged.
- It fails towards ringing nowhere new: if a phone's Android did not show the app
  outgoing calls, nothing would be stored and the rule would never match, which is the
  app as it was.

Negative:

- The app now holds, for up to a day, numbers the user called that are not in their
  contacts. That is personal data about third parties, on the device only. The privacy
  page and the security checklist say so.
- A caller the user rang back (returning a missed call from a stranger, say) can ring for
  a day. That is the user's own act, and the switch is there.
- It matches the number, not the organisation: a clinic that rings back from another
  line is still an unknown caller.
- Not confirmed on a real phone as of this date: the emulator and Android's own source
  agree, but a maker's build could differ. To be seen on the founder's phone when he next
  asks for a build there.

## An emergency call

Not part of this decision, recorded because it was checked alongside. Telecom skips
call filtering altogether for an incoming call the network marks as an emergency call or
that arrives in emergency callback mode (`CallsManager.java`, the same reading), so a
screening app is never asked about those. Seen on the emulator the same evening, with a
call to its emergency number placed on the emulated modem only: the app was shown that
outgoing call like any other; for the five minutes of emergency callback mode that
followed, a stranger's call with the lever at Block rang, and Telecom's log said why
("Skipping call filtering ... (ecm=true ...)"). Whether a phone on an Indian network
enters that mode after a call to 112 is not known here.
