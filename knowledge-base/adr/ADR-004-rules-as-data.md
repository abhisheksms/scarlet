# ADR-004: Screening rules are an ordered list of data, in a pure module

## Status

Accepted (2026-10-02, founder's requirement in the brief).

## Context

Phase one decides a call from a handful of settings. Phase two adds India's number
series (allow 160, block 140) and a paid tier, and more rules will follow. If the
decision were an `if` chain inside the service, each addition would mean editing and
re-testing platform code that can only be exercised with real calls.

## Decision

- A rule is `Rule(id, condition, action)`: three plain values. `Condition` is a
  closed set of data classes; `Action` is ALLOW, SILENCE or BLOCK.
- The rule list is ordered and **the first matching rule wins**. No match means
  ALLOW.
- `RuleBook.build(settings, allowList)` produces the list from the user's settings.
  `RuleEngine.decide(rules, call)` walks it. Both live in the `core` module, which
  has no Android dependency, no clock and no storage: time and history are passed in.
- The Android service is a thin adapter. It gathers the facts, asks for a decision
  and maps the action to a `CallResponse`. It contains no rule logic.
- The id of the deciding rule is stored with every handled call.
- A new kind of rule is a new `Condition` plus one line in `RuleEngine.matches` and
  one row in `RuleBook`; callers do not change.

## Consequences

Positive:

- Every rule and every boundary is covered by plain JUnit tests that run in
  milliseconds, and a mutation of any boundary fails a test (checked on 2026-10-02).
- "Why was this call blocked?" has an answer in the data.
- Phase two is additive.

Negative:

- A little more structure than an `if` chain would need today.
- The rule list is rebuilt for each call. It is six small objects; this is free.

## Not decided here

Whether users will ever edit or reorder rules themselves. Today the order is fixed in
`RuleBook`. Because rules are data, a stored, user-ordered list is possible later
without changing the engine.

## Amendment, 2026-10-04

The first phase-two rows landed: `Condition.NumberInSeries(countryCode, nationalPrefix)`
and the rows `in-160-service` (allow, always) and `in-140-promotional` (block, behind the
user's switch), as foreseen and with no change to the engine or its callers. One
consequence changed: the list can now block before its last rule, so the test that
asserted "only the last rule can block" asserts "the 140 rule and the last".
`knowledge-base/docs/02-rules-engine.md` has the order and the TRAI basis for the defaults.
