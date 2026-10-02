# ADR-004: Haptics are cues, not decoration

> Worked example from turqoise (the second Cyan Harbor project, August 2026),
> kept verbatim. The template version is in `../templates/`.

## Status

Accepted (PLAN.md §2.6). **Supersedes, for this app only, the no-haptics
rule in cyan's ADR-005 and application-architecture doc ("The app does not
use haptic feedback"). Cyan's rule still stands in cyan.**

## Context

Cyan banned haptics under its lean-dependencies-and-purposeful-motion
policy, and rightly: in an auction game played eyes-on-screen, a buzz is
decoration, and decoration that costs a native capability fails the policy
twice.

The founder-taste doc demands that every subtraction record its re-check
condition (FOUNDER_TASTE.md §10): delete the surface, keep the capability,
write down what would bring it back. The condition that brings haptics back
is a product whose user is *not looking at the screen*. Turqoise is exactly
that product: the phone is on the floor three meters away or in a pocket,
the gym is loud, the user's eyes are closed mid-plank. PLAN.md's thesis
line — "cues you can feel with your eyes closed" — is a haptics requirement,
stated as product intent.

## Decision

Haptics are allowed in turqoise **as cues only**: a haptic mirror for each
of doc 02's five cue kinds — countdownBeep, boundary, halfway, finalRound,
complete. The complete rule set:

- A haptic always accompanies a state change the user needs to know about.
  It never fires for button presses, navigation, or celebration.
- Haptics fire only during a running session — plus a single test pulse in
  Settings so the user can feel what they are enabling.
- Patterns are distinguishable by cue type (work-start must not feel like
  rest-start) and the pattern vocabulary is small and fixed; doc 03
  specifies it.
- Independently disableable from sound in Settings — gyms are loud, offices
  are quiet, and the two toggles serve different rooms.
- Delivered through the `Haptics` adapter (doc 04): no native module means a
  no-op, and the app remains fully functional.

## Consequences

- The no-decoration principle survives intact — this narrows an exception,
  it does not open a door. Any haptic outside the cue list above is a
  defect, and tests can assert the call sites.
- One added native capability (expo-haptics or equivalent), justified under
  the dependency policy by a named product requirement rather than vibes.
- Cue timing precision now matters for haptics exactly as it does for
  audio — the ADR-005 device spike must verify haptic latency with the
  screen locked, not just sound.
- The taste-doc mechanism worked: this reversal cites its recorded re-check
  condition instead of quietly contradicting an old rule. Every future
  reversal of an inherited rule should look like this ADR.
