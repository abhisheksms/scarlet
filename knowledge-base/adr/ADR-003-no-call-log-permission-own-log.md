# ADR-003: No call-log permission; the app keeps its own log

## Status

Accepted (2026-10-02, founder's constraint in the brief).

## Context

A history of what was blocked could be read from the system call log, but
`READ_CALL_LOG` belongs to the SMS and Call Log permission group that Google Play
restricts to default dialer, assistant or SMS handlers and gates behind a
declaration form. Requesting it would put the studio's one developer account at risk
for a feature the app can provide itself: it is told about every call it handles.

## Decision

- No `READ_CALL_LOG`, `WRITE_CALL_LOG`, `READ_PHONE_STATE`, `READ_PHONE_NUMBERS`,
  `PROCESS_OUTGOING_CALLS` or SMS permission. A unit test pins the manifest's
  permission list.
- The app records each call it **blocks or silences** in its own Room table at the
  moment it decides: number as received, number key, time, outcome, deciding rule.
- Calls it allows are not recorded.
- The log never leaves the device, is not backed up (`allowBackup=false`), and the
  user can delete one row or all of it.

## Consequences

Positive:

- Nothing declaration-gated. The Data safety form has no call-log row.
- The history shows exactly the app's own actions, with the rule that caused each.

Negative:

- The app cannot show whether a silenced call was later answered, or how long it
  rang: it only knows its own decision.
- Uninstalling or clearing data loses the history. Accepted.
- The system call log still shows blocked calls as "blocked" and silenced ones as
  missed; that is Android's record, outside the app's control (`setSkipCallLog` is
  ignored for third-party screening apps).
