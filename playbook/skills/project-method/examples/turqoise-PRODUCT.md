# Product

> Worked example from turqoise (the second Cyan Harbor project, August 2026),
> kept verbatim. The template version is in `../templates/`.

<!-- Drafted from knowledge-base/ for /impeccable init to confirm with the
founder. Impeccable reads this before every design command. -->

## Register

product

## Users

People mid-effort: a lifter between sets, someone on a mat at home, a runner
doing 30/30s, a physio patient counting holds. They open the app with wet
hands and elevated heart rate, glance at it from across a room, and hear it
with the phone in a pocket. They are adults; they need a precise instrument,
not encouragement, streaks, or coaching.

## Product Purpose

Turqoise runs work/rest interval routines hands-free — prepare, work, rest,
set-rest, cooldown; rounds and sets; saved routines; Tabata/EMOM/HIIT
presets — with audio and haptic cues so precise the user never looks at the
screen, surviving screen-off and backgrounding for a full 45-minute session.
Success: open-to-running in two taps, state readable at three meters from
color alone, zero missed cues, and a session that is never — under any
circumstance — interrupted by anything.

## Brand Personality

An instrument, not a coach. Precise, calm, quietly futuristic. The register
is a beautifully engineered tool: it speaks only when spoken to, signals
with light and sound instead of words, and earns "surreal" through craft —
monumental numerals, purposeful motion, color as state — never through
decoration. Three-word personality: **precise, calm, striking**.

## Anti-references

- The interval-timer category standard: cluttered utility screens, ad
  banners wedged against controls, settings sprawl, gamified streaks.
- AI-slop futurism: neon-on-black, purple-blue gradients,
  glassmorphism-by-default, HUD-scanline kitsch, glowing particles.
- Fitness-app cheerleading: motivational copy, confetti, "You crushed it!"
- Anything from `docs/FOUNDER_TASTE.md`'s slop-tell list — that document
  binds every surface of this app.

## Design Principles

1. **Glanceable truth.** The current state (work / rest / prepare / set-rest
   / cooldown) is legible from color alone at three meters, colorblind-safe
   when paired with label and shape. Numerals are the largest thing on
   screen.
2. **The screen is the cue.** Full-screen color state, not widgets. Motion
   communicates state-change only — never idles, never decorates.
3. **Two taps to running.** Density and hierarchy over navigation. Core
   surfaces never scroll during a session.
4. **Nothing interrupts a session.** No ad, prompt, upsell, or rating beg
   mid-session, ever. Ads live only on calm non-session surfaces (Home,
   Complete), contained and labeled, never mimicking content.
5. **One-hand, sweaty-thumb targets.** Pause/skip reachable and large;
   destructive actions out of the blast radius.
6. **Reduced-motion parity is designed, not retrofitted.** Every state
   change has a static, equally informative form.

## Accessibility & Inclusion

Launch-blocking, not polish: full TalkBack/VoiceOver support with specified
narration order per screen; every control has a role and label; color-state
always paired with text/shape (colorblind-safe); WCAG-AA contrast on all
text; cues perceivable through any single channel alone (audio, haptic, or
visual); `prefers-reduced-motion` respected everywhere.
