# ADR-005: Lean dependencies and purposeful motion

> Carried over from Cricket Auction Simulator. Re-accept or supersede it for
> the new app and record the date in Status.

## Status

Accepted

## Context

The live auction must feel immediate and visually smooth on representative
Android devices. Minor effects can easily create unnecessary native
dependencies, bundle growth, startup work, layout instability, and maintenance
risk.

## Decision

- Prefer ordinary React Native components for layout and controls.
- Use Reanimated for gameplay communication that benefits from motion.
- Use transform and opacity animation where practical.
- Add Skia only after a profiled prototype demonstrates a specific need that
  the existing stack cannot meet well.
- Do not add a production dependency for a minor effect or small utility.
- Measure native dependency impact in release builds.
- Keep animation presentation-only, interruptible, lifecycle-safe, and
  compatible with reduced-motion behavior.
- Derive bidding-war, outbid, scarcity, and purse emphasis from authoritative
  state and events.
- Keep pulses and cadence changes bounded to the factual state; no perpetual
  idle glow or continuous animation.
- Provide a static, equally informative reduced-motion alternative.
- Validate smoothness and input responsiveness on representative physical
  Android devices, including rapid contested-bidding fixtures.
- Do not implement haptic feedback.

## Consequences

Positive:

- Lower bundle and maintenance cost
- Fewer native integration failures
- More predictable release performance
- Clear separation between authoritative state and presentation
- Better accessibility

Negative:

- Some visual ideas will require a prototype and performance evidence before
  adoption
- Effects may need simplification on lower-performance devices
- Release-device profiling becomes part of the definition of done
