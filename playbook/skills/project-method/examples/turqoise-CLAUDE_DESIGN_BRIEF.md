# Turqoise — Design Brief

> Worked example from turqoise (the second Cyan Harbor project, August 2026),
> kept verbatim. The template version is in `../templates/`.

You are designing an interval timer app called **turqoise** (working name),
for Android and iOS phones. This brief is self-contained — everything you
need is in it, and everything in it is deliberate. Where it constrains you,
the constraint was earned on a shipped product; treat every one as
intentional, not negotiable.

## 1. The product

Turqoise runs work/rest intervals hands-free: prepare, work, rest, set-rest
(the longer breather between sets), cooldown; rounds and sets; saved
routines; quick-start presets (Tabata, EMOM, HIIT 30/30).
Precise sound and vibration cues fire even with the screen locked. No
accounts, no network dependence. V1 has exactly two money surfaces, both
specified in §2: a single dignified ad slot (Home and Complete only) and a
remove-ads purchase sheet. The live timer, Ready, and the builder carry no
ads, ever — that is law, not layout.

The category is crowded with functional-but-joyless utilities and ad-stuffed
slop. Nobody in it wins on craft. Turqoise wins on craft: instant to start,
glanceable from across a gym floor, and striking enough that people show it
to a friend. The screenshot must stop a scroll.

**Who it's for**: people mid-effort. A lifter between sets. Someone on a mat
at home. A runner doing 30/30s. They are adults; the app respects them by
being excellent and silent, never by encouraging them.

**The moments of use** — design for these three, in this order of importance:

1. **Far and sweaty** (the session): phone propped against a water bottle
   one to three meters away. Glances, not reads. Sometimes eyes closed —
   the sounds carry it. Hands shaky, wet, mid-rep.
2. **Close and quick** (setup): thirty seconds, warm hands, one thumb.
   Pick or build a routine, prop the phone, hit Start, step back.
3. **The finish**: the workout ends. This is a payoff beat, not a stop.

## 2. The six surfaces and their states

Design all six. The live timer is the hero; everything else exists to get
the user into it and out of it with zero friction.

1. **Home / routines** — *empty state*: first launch, no routines yet. The
   quick-start presets carry it — Tabata, EMOM, HIIT 30/30, each two taps
   from running (tap it, tap Start). No welcome screen, no tutorial, no
   empty-state poetry; the presets ARE the onboarding. — *Populated state*:
   saved routines, each showing its name and its shape (total time, rounds),
   each two taps from running.
2. **Routine builder** — create and edit: prepare/work/rest/set-rest/
   cooldown durations, rounds, sets. Duration entry designed for thumbs,
   not keyboards. Segments can carry names ("Burpees", "Plank") — design
   the builder and timer so names read beautifully when present and leave
   no hole when absent (the founder decides at gate G5 whether names ship
   in v1; design as if yes).
3. **Ready / pre-start** — after tapping a routine, before commitment:
   routine name, the shape of what's coming, one huge Start. This screen
   serves the prop-and-step-back gesture — Start is the last thing touched
   at close range, so it is unmissable.
4. **Live timer — the hero.** The full-screen color IS the interface: the
   screen does not contain an indicator, it is one. One color each for
   prepare, work, rest, set-rest, and cooldown, flipping edge-to-edge at the
   boundary instant. The remaining-time numerals are the largest element on
   screen; the segment word (WORK / REST / GET READY / SET REST / COOL DOWN)
   is huge beside them and always agrees with the color. Round counter
   present. Pause and skip reachable with one sweaty thumb. States to
   design, each distinct at three meters:
   - *prepare* — the pre-work runway
   - *work*
   - *rest*
   - *set rest* — the longer breather between sets
   - *cooldown* — the wind-down after the last work segment
   - *paused* — unmistakably not-running from across the room
   - *final 3 seconds* — beeps sound at T−3, T−2, T−1; give them a visual
     mirror
   - *final round* — the last round's work must feel different; knowing it's
     the last one changes how a person spends it
   - the *boundary flip* itself — the one moment motion carries meaning
5. **Complete / summary** — the payoff beat. Routine name, total time, work
   time, rounds and sets completed. Closure with dignity: it may celebrate
   once, earned, and then be still. Never just stop, never confetti-spam.
6. **Settings** — sound on/off; a toggle per cue kind (countdown beeps,
   segment boundary, halfway, final round, complete); vibration on/off with
   a test pulse; keep-screen-awake (default on); respect-silent-mode
   (default off — cues play even in silent mode; the user opened a timer to
   hear it); **Remove Ads** (with the localized price) and **Restore
   Purchases** rows; and a small selectable version stamp. No volume
   control — cues follow media volume. Calm and boring in the best way.

**The money, and its limits.** Two monetization surfaces to design, no more:

- **The ad slot** — on Home and Complete/summary ONLY. The design must make
  an ad coexist with craft: visibly contained, labeled as an ad, never
  mimicking content, and the surface must stay composed when no ad loads
  (offline is normal). It is a tenant, not a feature — the screen's
  hierarchy must not bend to it.
- **The remove-ads purchase sheet** — one lifetime purchase. Localized price
  placeholder (never a hardcoded figure), Google Play voice, no urgency of
  any kind: no countdowns, no "limited", no guilt on decline. State the
  trade plainly and let the adult decide.

**The live timer, Ready, and the builder remain ad-free by law.** Design no
slot there — not an empty one, not a collapsed one; those layouts must not
even have a place where an ad could go.

## 3. Hard constraints — every one intentional

- **Portrait phone first.** The reference device is a cheap, small Android
  phone, not a flagship.
- **Readable at three meters.** Numerals are the largest thing on the
  screen. If anything competes with them for size, the hierarchy is wrong.
- **State legible from color alone at distance — and never from color alone
  up close.** The across-the-room read is pure color; for colorblind users
  and certainty, color is always paired with the segment word and shape.
  Pick five distinct colors — prepare, work, rest, set-rest, cooldown —
  that survive colorblindness and gym lighting.
- **One-handed, sweaty-thumb targets.** Big, bottom-reachable, forgiving.
  Nothing destructive sits next to Pause.
- **Nothing interrupts a session.** No modal, prompt, badge, rating ask, or
  celebration mid-session — the design must not even have a slot where one
  could go.
- **Ads only where §2 allows.** Home and Complete carry the one contained,
  labeled slot; the live timer, Ready, and the builder never carry any, in
  any state.
- **Reduced-motion parity is designed, not retrofitted.** Every state change
  must read perfectly with motion off — color and label carry it. Design
  the reduced-motion variant of each transition alongside the full one.
- **Screen-reader narration order is part of the design.** For each screen,
  specify the TalkBack/VoiceOver read order and what gets announced at
  segment boundaries. This ships to a market where screen-reader users
  exist at scale.
- **Motion communicates state-change only.** A field that breathes with the
  interval clock is state; a shimmer that decorates is not. Nothing animates
  while state holds still.

## 4. The ambition — three directions

The founder wants **futuristic and surreal** — earned through discipline,
never through cliché. Channel that ambition into exactly three named
directions. Push each one hard; a timid board is a useless board.

- **Chronograph** — instrument-grade precision. Dial logic, monumental
  tabular numerals, mechanical calm, the beauty of a tool built to be
  trusted with time. The future as a perfect instrument.
- **Signal** — light as the interface. The screen is a colored field that
  breathes with the interval; typography floats in light. The closest of the
  three to surreal — a room that changes color around you.
- **Terminal Velocity** — dark, kinetic, speed-and-data. Motion is the hero;
  numerals feel like telemetry from something fast. Intensity earned by the
  clock, not painted on.

Each board must cite its references — and they must be **non-UI
references**: industrial design, signage, instruments, architecture, film
production design. Cite what the direction is actually made of. No board may
reference another timer app or a dashboard template.

**Banned outright, in all three directions:**

- Neon-on-black
- Purple-blue gradients
- Glassmorphism as a default material (frosted panels everywhere)
- Identical-card grids
- HUD/sci-fi kitsch: scanlines, hexagons, reticles, corner brackets, fake
  terminals
- Placeholder graphics standing in for content that doesn't exist — if
  there's no asset, design without it

If a board needs any of these to feel futuristic, the board has failed;
these are the costume of the future, and turqoise wants the machinery.

## 5. Deliverables

**Stage 2 — now, in this conversation**: three static boards, one per
direction, each named as above and containing: the live-timer screen in at
least *work* and *rest* states, one secondary screen of your choice, the
reference citations, and candidate type and color choices. Static is fine;
no interactivity needed. The founder picks one board — that pick will come
back to you, and it is final.

**Stage 3 — after the pick**: one interactive HTML prototype in the chosen
direction. All six surfaces, every state listed in §2 — the ad slot on Home
and Complete in both loaded and empty states, and the remove-ads sheet,
included — real copy throughout (no lorem, no "Routine 1"), and final
values: exact colors, exact type sizes,
named motion durations and easings, the reduced-motion variant demonstrable,
and the per-screen narration order written down. Self-contained HTML — it
will be reviewed in a live browser, and revision notes from a design-review
tool will come back to you in rounds. Notes marked as detector findings are
must-fix; the rest are the founder's calls, relayed.

## 6. Voice — the copy rules

The prototype's copy is final copy, so write it under these rules:

- **Title Case on buttons and CTAs** ("Start Workout", "Save Routine").
  Body text is sentence case.
- **Talk to adults.** Terse and confident. No coaching ("You've got
  this!"), no reassurance, no explaining what a button plainly does. When
  in doubt, delete the sentence — deletion beats rewriting.
- **No narrator chrome.** No welcome screens, no "resume anytime", no
  chummy asides. The app never speaks *at* the user; when it speaks at all,
  it speaks as the tool — "GET READY", "REST", "Done" — the way an
  instrument labels itself.
- **No filler labels** restating what the screen already shows. Every
  element earns its pixels; live information beats identity and decoration.
- The app's respect is its silence. No motivational-poster copy anywhere —
  the user brought the motivation; turqoise brings the precision.
