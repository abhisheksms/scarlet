# <App> — Design Brief

You are designing <a one-line description> called **<app>** (working name),
for <platforms>. This brief is self-contained: everything you need is in it,
and everything in it is deliberate. Where it constrains you, the constraint
was earned on a shipped product; treat every one as intentional, not
negotiable.

## 1. The product

<What it does, in one paragraph, including the money surfaces it has and the
surfaces that must never carry one.>

<Why the category is beatable, and what this app wins on.>

**Who it's for**: <adults; the posture they're in; what they need and don't>.

**The moments of use**, in order of importance:

1. <the core moment, with its physical conditions>
2. <setup>
3. <the finish, as a payoff beat>

## 2. The surfaces and their states

Design all of them. <The hero surface> is the hero; everything else exists to
get the user into it and out of it with zero friction.

1. **<Surface>** — <states: empty, populated, …>. No welcome screen, no
   tutorial, no empty-state poetry.
2. **<Surface>** — …
n. **Settings** — <toggles>, **Remove Ads** (localized price) and **Restore
   Purchases** rows, a small selectable version stamp.

**The money, and its limits.** <Exactly which surfaces carry an ad slot
(contained, labeled, composed when nothing loads) and the purchase sheet
(localized price placeholder, store voice, no urgency of any kind). Which
surfaces must have no slot at all, not even an empty one.>

## 3. Hard constraints — every one intentional

- **Portrait phone first.** The reference device is a cheap, small Android
  phone, not a flagship.
- <Legibility rule: what must be readable at what distance.>
- <State encoding rule: never from color alone up close; always paired with
  text and shape.>
- **One-handed targets.** Big, bottom-reachable, forgiving. Nothing
  destructive next to the primary action.
- <The product's law: what must never interrupt the core moment.>
- **Reduced-motion parity is designed, not retrofitted.** Every state change
  reads perfectly with motion off.
- **Screen-reader narration order is part of the design.** Specify it per
  screen.
- **Motion communicates state change only.** Nothing animates while state
  holds still.

## 4. The ambition — three directions

The founder wants <the ambition>, earned through discipline, never through
cliché. Channel it into exactly three named directions. Push each one hard; a
timid board is a useless board.

- **<Direction A>** — <one line>
- **<Direction B>** — <one line>
- **<Direction C>** — <one line>

Each board must cite **non-UI references**: industrial design, signage,
instruments, architecture, film production design. No board may reference
another app in the category or a dashboard template.

**Banned outright, in all three directions:** neon-on-black; purple-blue
gradients; glassmorphism as a default material; identical-card grids;
HUD/sci-fi kitsch (scanlines, hexagons, reticles, corner brackets, fake
terminals); placeholder graphics standing in for content that doesn't exist.

## 5. Deliverables

**Stage 2 — now**: three static boards, one per direction, each containing
the hero surface in at least two states, one secondary surface, the reference
citations, and candidate type and color choices. The founder picks one; the
pick is final.

**Stage 3 — after the pick**: one interactive HTML prototype in the chosen
direction. Every surface, every state listed in §2, the money surfaces in
loaded and empty states, real copy throughout (no lorem), final values: exact
colors, type sizes, named motion durations and easings, the reduced-motion
variant demonstrable, the per-screen narration order written down.
Self-contained HTML; revision notes from a design-review tool come back in
rounds. Notes marked as detector findings are must-fix; the rest are the
founder's calls, relayed.

## 6. Voice — the copy rules

- **Title Case on buttons and CTAs.** Body text is sentence case.
- **Talk to adults.** Terse and confident. No coaching, no reassurance, no
  explaining what a button plainly does. When in doubt, delete the sentence.
- **No narrator chrome.** No welcome screens, no "resume anytime", no chummy
  asides. When the app speaks, it speaks as the tool.
- **No filler labels** restating what the screen already shows.
- The word "AI" never appears in user-facing text.
