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
