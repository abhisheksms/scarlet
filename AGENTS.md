# Instructions for coding agents

This file applies to the entire repository.

## Source of truth

Before planning or changing the product:

1. Read `PLAN.md` (the spine: decisions locked, the founder's gates, the phases).
2. Read `knowledge-base/README.md` and the documents for the part being changed.
3. Check every ADR in `knowledge-base/adr/` that touches the decision.
4. Check `FEATURES.md` for the feature's row and `TASKS.md` for its status.

The knowledge base is authoritative. Do not silently change product scope, the
screening rules, the permissions the app asks for, or an accepted ADR. A change to
any of those is a new ADR or an amendment, dated.

## The clean-room line

This app rebuilds the *feature set* of an existing app. What carries over is the
job, the list of features and how they behave. What never carries over: code, text
(names, labels, descriptions), the icon, colours and layout as trade dress, or
anything copied from a screenshot. The reference app's name and its developer's
name never appear in the product, the listing or the code. Do not pull, list or
decompile the reference APK. `docs/reference/` is research behind a firewall: write
the product from `FEATURES.md` and the knowledge base, never from the notes.


The other half of the line is Google Play's Repetitive Content rule: a rebuild has to be
different in ways a user can name, before any public track, or the store may remove it
and the strike reaches every app on the studio's account. The checks and this app's
record are `docs/play-repetitive-content.md`.
## Screening integrity

- The decision for a call comes only from the ordered rule list in `core`
  (`RuleBook` builds it from settings, `RuleEngine` walks it). The service is a thin
  adapter: it gathers facts, asks for a decision and answers Telecom. No rule logic
  in the service, the UI or the database layer.
- A rule is data: an id, a condition and an action. Adding a kind of rule must not
  require rewriting the engine or the callers.
- When anything goes wrong while screening (a timeout, a read error, an unparsable
  number), **the call is allowed**. Wrongly letting a call ring is an annoyance;
  wrongly blocking one can cost the user something that matters.
- The app answers Telecom well inside its five-second limit. Nothing slow (network,
  ads, heavy queries) may run before the answer.
- Only calls the app blocked or silenced are stored. Calls it allowed are not.

## Privacy and permissions

- **No contacts permission.** Android only passes a screening app the calls from
  numbers outside the user's contacts; that is the whole basis of the product
  (ADR-002). Do not add READ_CONTACTS.
- **No call-log, SMS or phone-state permission** (ADR-003). The app keeps its own
  log of the calls it handled, on the device only.
- The handled-call log, the allow list and the settings never leave the device. No
  analytics SDK, no crash-reporting SDK, no backend, without a new ADR and matching
  changes to the Data safety form and the privacy page in the same PR.
- A caller's number is personal data. It is hidden on the lock screen
  (private notification with a public version that carries no number), kept out of
  ordinary logs in release builds, and never placed in an ad request.
- `POST_NOTIFICATIONS` is asked for in context, when the user turns on something
  that notifies, never at cold launch. A refusal degrades that feature only.
- `allowBackup` stays `false`.

## Advertising integrity

- Ad unit ids live in one file (`ads/AdUnits.kt`); the AdMob app id, which the
  manifest also needs, is one value at the top of `app/build.gradle.kts`. All are
  Google's published test ids until the founder supplies live ones; `LaunchGateTest`
  pins them.
- The UMP consent flow runs before the ads SDK is initialised, and Settings offers
  the privacy-choices entry point whenever UMP says it is required.
- An ad never moves content after the screen has drawn. Every ad slot has reserved
  space. An ad never sits where a tap aimed at a control can land on it.
- No ad is shown during the role request, over a confirmation dialog, or in a way
  that hides the on/off state of blocking.
- The ads SDK is never initialised from the screening service or from
  `Application.onCreate`: a call must not wait on it.
- A later paid unlock goes through Google Play Billing only, shows Play's own price,
  and is derived from Play ownership. It is out of scope until the founder starts
  that phase.

## Quality bar

Work component by component, avoid speculative abstractions, and do not call work
complete until `FEATURES.md` shows the feature's verification and it has been seen
working on the emulator. Report what the screen showed, not only what the tests say.

- Plain, readable Kotlin: the reviewer is a Java backend engineer. Small classes,
  descriptive names, no reflection tricks, no DI framework.
- Every dependency is first-party (AndroidX, Google) or equally reputable, and each
  one's reason is recorded in `knowledge-base/docs/04-application-architecture.md`.
- Screens hold at 360 dp wide with font scale 1.35, in light and dark.
- Motion communicates state or is absent.

## Cost-aware delegation

Delegate bounded, checkable grunt work (repetitive tests, mechanical refactors,
documentation consistency checks) to lower-cost subagents with a narrow scope,
explicit files and acceptance criteria. The primary agent keeps product decisions,
architecture, dependency selection, security, privacy, release and integration, and
reviews every delegated diff. Delegation never transfers accountability.

## Git

Branch from `origin/main`, one PR per component, CI green, squash-merge, stage
explicit paths. Check the PR against `docs/SECURITY_CHECKLIST.md` before merging.
