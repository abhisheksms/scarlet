# ADR-001: Stack inherited from the playbook

## Status

Accepted (<date>, founder's call, PLAN.md §2.1).

## Context

Cricket Auction Simulator went from first design doc to launch-ready in about
three weeks on a stack that is fully documented and verified in the playbook's
`TECH_STACK.md`. Every piece of it survived contact with real devices, a Play
launch and a non-technical founder reading the code. <This app> has no
requirement that stack cannot meet: <why: state-heavy, offline, …>.

Re-deriving a stack per project would spend the scarcest resource, founder
attention, on the least differentiating decision.

## Decision

The playbook's stack is the baseline, adopted verbatim: Expo + React Native +
TypeScript strict, pnpm workspaces (Corepack-pinned, hoisted node linker, no
task orchestrator), one zustand store, expo-sqlite behind a narrow key-value
contract, Reanimated (transform/opacity only), hand-drawn react-native-svg,
Jest two-project testing with React Native Testing Library, and GitHub
Actions with a single verify job as the only merge gate.

Two things are explicitly not inherited:

- **Version numbers.** This app takes the latest stable Expo SDK at
  Component 0 time and lets jest-expo, RNTL and react-native track it, then
  checks Play's current target-API and Billing Library requirements.
- **The dependency list.** Only the platform choices carry over; every native
  dependency is re-justified when its component needs it.

Any deviation from `TECH_STACK.md` requires a new ADR in this directory.

## Consequences

- Zero stack ramp-up: tooling, CI shape, test harness and the hard-won gotchas
  (Metro in a monorepo, Babel plugin ordering, jest-expo/SDK coupling) are
  known-good on day one.
- Sessions start productive; the conventions are already written down.
- The codebases stay mutually legible; patterns and fixes transfer both ways.
- The constraints transfer too: native-module compatibility work at every SDK
  upgrade, JS-thread discipline for smoothness.
- The stack is a floor, not a ceiling; the burden of proof for changing it is
  an ADR, which is the intent.
