# Founder Taste — Abhishek / Cyan Harbor

What the founder actually wants, distilled from months of shipped decisions on
Cricket Auction Simulator — every rule below was extracted from a real call
they made, with their own words as the anchor. This file is portable: **copy it
into `docs/` of every new Cyan Harbor project** and read it before writing a
line of UI, copy, or monetization.

The one-line version: **build like FC Mobile, price like you mean it, talk to
adults, and never fake anything.**

---

## 1. The mission

> "You are the captain now, you have to make money for me, by building great
> and addictive products."
> "Great, addictive, long-lasting products… that's what defines us."

Revenue through craft. "Addictive" means mastery, variety, payoff, and
replayability — earned depth that brings players back for months. It never
means manipulation. Longevity is the bar, not first-session delight.

## 2. No AI slop — the founding allergy

> "It makes the app look like AI slop."

The founder can smell generated-feeling product instantly, and it is the thing
they hate most. The known tells, each one shipped-and-then-removed here:

- **Welcome/tutorial screens** explaining what the user can plainly see.
- **Chummy narrator copy**: "Your call, no rush", "Resume anytime",
  "optional ›", "You'll manage this team against 9 AI rivals."
- **Placeholder graphics** standing in for content that doesn't exist — the
  avatar circle above a player with no photo. If there's no asset, design
  without it.
- **Goofy invented names**: "i dont want goofy names like monsoon arc, ember
  forge." Fiction must sit close to the real thing (city + plausible surname,
  real auction rules), never fantasy-flavored.
- **Generic filler labels**: "Outbid", "You passed", "Lot 3 / 130",
  "Table · 10" — anything that restates what the screen already shows.
- The word **"AI"** in player-facing text. Rivals are franchises, in-fiction.

The benchmark, in their words: make it look "like an actual game, like in FC
mobile or F1 clash."

## 3. Talk to adults

> "The user is an adult not a kid."

- Terse, confident copy. No coaching, no reassurance, no over-explaining
  what a button does. When in doubt, **remove the sentence** — deletion beat
  rewriting every single time it came up.
- **Title Case on buttons and CTAs** — "'New game' text looks too juvenile -
  it should be New Game." Body text stays sentence case.
- Auctioneer/domain patter is welcome flavor ("Going once — stay sharp",
  "SOLD"); narrator hand-holding is not. The line: flavor speaks *as the
  world*, slop speaks *at the user*.
- **"No hand-holding" never means "no teaching."** The mass-market audience
  includes first-time gamers. Teach *diegetically* — the first auction is the
  tutorial, the auctioneer's voice can carry guidance in-fiction — never
  through narrator overlays. If day-1 retention data argues with the allergy,
  believe the data. *(Captain's amendment, founder agreed 2026-08-04.)*

## 4. Screen economics

- **Live state stays visible — that's the actual rule.** "Fits one screen"
  is the means: core play screens never scroll, and live information wins
  space over identity and decoration. But density is achieved through
  **hierarchy, not cramming** — FC Mobile is dense because everything has a
  size rank, not because everything is small. *(Captain's amendment, founder
  agreed 2026-08-04.)*
- **Test on a cheap, small phone** — the mass-market Indian device, not the
  founder's flagship. A layout that only works at 6.7" hasn't shipped.
- Kill any element that doesn't earn its pixels — labels, counters,
  duplicated data ("purse after" shown twice). One place, once.
- **Before deleting *information* (as opposed to decoration), check where
  else it lives — move it if it lives nowhere.** The "purse after" cut only
  worked because the Bid button still carried it at the commit point.

## 5. Realism as the texture

The founder's fiction imitates reality's *rules*, not its names:

- Real-world mechanics adopted wholesale: IPL bid increments, overseas squad
  caps, marquee sets, points table with Net Run Rate, ₹ crore/lakh money.
- Names sit adjacent to real ones (Chennai Sentinels, Mumbai Mariners) —
  instantly legible to the target fan, legally fictional. **The cliff:**
  crests, colors and kits must never map one-to-one to real franchises, and
  trademarked league names ("IPL") never appear anywhere public. Adjacency is
  taste; imitation is a takedown. *(Captain's amendment.)*
- Full country names with the airplane icon, not codes. "Australia", never
  "Overseas · AUS".
- Superstars must exist and must cost the earth — the market should produce
  the gasp the real one does.

## 6. Opponents must genuinely compete

- Rival AI should be "aggressive and spendthrift and competent" — near-maxing
  their budgets, punishing passivity, holding grudges, coming back into
  contests. A soft opponent is disrespect.
- Emotion must be **derived from real state** — animosity, scarcity, bidding
  wars all come from actual events, tuned to be *events, not constants*.
  Never fabricate drama.

## 7. Immersion over instrumentation

> "I can immerse myself better and provide better feedback."

- The founder tests by *playing*, on a real phone, and their bug reports come
  from feel ("this is blunt", "this is useless", "I had to click twice").
  Optimize for time-to-device: local builds, fast iteration, build stamps so
  a stale install can never lie.
- Payoff beats matter: skipping a player still shows who won it; a season
  ends in a full table. Never let convenience skip the story.
- Motion must communicate state (a gavel strike, a call resetting), never
  decorate. "Just blunt" is a real defect; so is particle soup.

## 8. Monetization — confident and clean

> "I feel this app can sell."

- **Price with self-respect.** ₹49 → ₹99 → ₹149 across two founder calls:
  price is a quality signal, and discounting later beats raising later. Treat
  price as a Play Console experiment — confidence *plus* measurement, never
  confidence alone.
- One honest product: a single remove-ads unlock. No consumables, no tiers,
  no gameplay advantage, ever.
- **Friction is allowed; deception is not — and the line is exact: we may
  refuse to remove real texture; we may never add artificial sand.** Showing
  the true sold-screen at normal pace is legitimate because the payoff beat
  exists for real play. Slowness that exists *only* to sell relief from
  itself is a dark pattern wearing honest clothes. Fake countdowns, urgency
  copy, ads triggered by losses — never. The tests literally assert the
  absence of dark-pattern copy; keep that. *(Captain's amendment, founder
  agreed 2026-08-04.)*
- Every paid control states its true cost *before* it is pressed ("1 Free",
  then "Watch Ad"). A surprise on first press reads as a broken app.

## 9. Naming and distribution

> "Needs to resonate with large audience." · "I want this to show up in the
> search ranking, so that more players install it."

- **Search beats brand** at this stage: the app is named by its category
  ("Cricket Auction Simulator"), because that's what the audience types.
  Clever names lose ("Going Once… will not make it"); non-category names
  lose ("Auction King dosnt shout cricket").
- Mass-market India is the audience: major Indian languages are on the
  roadmap, Hinglish register preferred over formal translation.
- Studio brand: **Cyan Harbor** (packages: `com.cyanharborstudios.*`). The
  splash carries the studio credit; the home screen carries nothing — no
  self-branding inside the product.

## 10. Ruthless subtraction — with a re-check date

Features must justify themselves against the current reality, not a roadmap:

> "The players are unknown so there is no point in sharing the squad."
> "Remove the Feedback section, there are play store comments already."

If the ecosystem provides it (Play reviews), delete it. If the content can't
support it yet, delete it. But **every subtraction records its re-check
condition**: delete the surface, keep the capability, and write down what
would bring it back. Share Squad is the proof — removed while players were
unknown, restored the moment the founder planned for real-player licensing,
because sharing is the cheapest acquisition channel an offline game has.
*(Captain's amendment, founder agreed 2026-08-04.)*

## 11. Polish and performance ARE the vibe

*(Captain's section; the founder asked for this emphasis, 2026-08-04.)*

Jank reads as slop exactly the way bad copy does. A premium price (§8) is a
promise the frame rate has to keep.

- **60fps on a budget device is the bar**, measured in profiled release
  builds on real hardware — not on the founder's flagship, not in dev mode.
  The audience found this app by typing a search into a cheap phone.
- **Cold start fast, splash short** (~2s and choreographed), screens land
  settled — no layout shift, no spinner flicker, no image pop-in.
- Animate transforms and opacity; keep live-screen layout stable; motion
  always communicates state. One dropped frame during "Going twice…" costs
  more than any feature gains.
- **Sound is an open question the taste hasn't answered yet.** A real game in
  this genre has a gavel, a room, a call. Prototype it properly (off by
  default until it's excellent); don't ship a half-baked bleep because a
  checklist said "audio".
- Accessibility is part of polish, not compliance: every control announces
  itself, and TalkBack users exist at scale in this market.
- The founder notices single dead pixels of quality — an inert button, a
  duplicated label, a two-press dismissal. Assume everything will be seen.

## 12. Working with the founder

- **"No yapping, get the job done efficiently and accurately."** Lead with
  outcomes. Honest numbers, root causes, and real trade-offs — they take
  pushback well ("AdMob is not on the critical path") and reverse themselves
  cleanly when wrong ("my bad, I was using a previous build").
- They give real authority and expect initiative back — surface the plan,
  don't ask permission for the reversible.
- They park decisions explicitly when unsure (package name) and close them
  decisively later. Respect a park; never un-park it yourself.
- They read code as a sanity check: keep it legible, explain in plain
  English, maintain a founders' guide.
- Quality gates are non-negotiable background: tests that can fail, security
  checklist before merge, no dark patterns — these should never need the
  founder's attention to stay true.
