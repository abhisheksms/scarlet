# Handoff: *Going Once* — IPL Auction Simulator (core loop)

> Worked example from cyan (Cricket Auction Simulator, July 2026), kept
> verbatim: the handoff from design to build that the method is based on.

> **Historical snapshot (July 2026).** This is the design handoff the app was
> built from, kept as delivered. Where it differs from the shipped app, the app
> and the knowledge base win. Known differences: the game ships as *Cricket
> Auction Simulator* (no "IPL" anywhere public); the purchase is ₹149, not ₹49;
> the bid ladder is +₹10 L below ₹1 Cr, +₹20 L to ₹3 Cr, +₹50 L above
> (`packages/auction-engine/src/money/bid-increment.ts`); the skip sheet has no
> "Not now" row; the buy sheet lists two perks and no banner/interstitial
> claims. Current state: root `CLAUDE.md` and `docs/`.

## Overview
*Going Once* is a single-player, AI-first IPL-auction simulator. The player picks a franchise, then bids against 9 AI-run rival clubs to build a squad within a fixed purse. This package covers the **full playable loop**:

**Team select → Player reveal → Live auction (auctioneer's call) → Sold → Auction summary → (loop back).**

It also includes the two monetization surfaces (rewarded-ad batch-skip and a ₹49 "Remove Ads Forever" one-time purchase) and the AI-bidding model.

The experience target: high tension, real consequence, "build your squad your way" — no coaching, no hand-holding.

---

## About the design files
The files in this bundle are **design references created in HTML/JS** — an interactive prototype showing intended look, motion, and behavior. **They are not production code to copy directly.**

- The task is to **recreate these designs in the target codebase's environment** using its established patterns and libraries. This is a mobile game (Android-first, portrait phone), so the likely target is the native/app stack the team already uses.
- If no environment exists yet, choose the most appropriate framework for a real-time, animation-heavy mobile game and implement there.
- The prototype is built as a "Design Component" (`.dc.html`) that runs in a design tool. `support.js` is that tool's runtime — **ignore it for implementation**; it is included only so the HTML opens and runs for reference.

### How to run the reference
Open `Going Once - Playable Loop.dc.html` in a browser. It boots on the Team Select screen. `Going Once - 1a Build.dc.html` is the static design board (visual direction + the five engagement-hook explorations) — reference only.

---

## Fidelity
**High-fidelity.** Final colors, typography, spacing, motion, and interaction behavior. Recreate the UI faithfully using the codebase's own component library, but treat every measurement, color, and copy string here as intentional. The one deliberate placeholder is the **player avatar** (a wireframe stick-figure) — see *Assets*.

---

## Screenshots
Reference captures live in `screenshots/` (2× phone-screen crops). The mock uses an iOS-style status bar for illustration only — **target is Android-first, portrait phone.**

| File | Screen / state |
|---|---|
| `01-team-select.png` | Team Select — difficulty + franchise grid (top) |
| `02-team-select-chosen.png` | Team Select — franchise chosen, primary "Start auction" active |
| `03-player-reveal.png` | Player Reveal — Pranav Deshmukh (1 earmark), AGE/counters/₹ spacing, footer actions |
| `04-skip-batch-choice-sheet.png` | Skip-batch **choice sheet** (Watch ad / Remove ads ₹49 / Not now) |
| `05-remove-ads-buy-sheet.png` | **Remove Ads Forever ₹49** buy sheet (Google Play Billing mock) |
| `06-rewarded-ad-overlay.png` | Rewarded-ad overlay (5s → Claim reward) |
| `07-live-auction.png` | Live Auction — BIDDING WAR, "5 still in", RISING bid, pundit line, feed (jump/out), rail, Pass/Bid footer |
| `08-auction-you-leading-war.png` | Live Auction — you leading (leading bar footer), war glow |
| `09-sold.png` | Sold — rival win variant (grey SOLD stamp, winner chip, "You passed" footnote). *Your-win variant is the same layout in accent; unsold uses an UNSOLD stamp.* |
| `10-summary-league.png` | Auction Summary — stat grid + league table (your row highlighted) |
| `11-summary-squad.png` | Auction Summary — "Review squad" toggled (signings list / empty-state) |

Also see the **marquee reveal** (2 earmarks) in the auction anatomy: a marquee player shows `{Team A} & {Team B} are circling this player` with two crests.

---

## Design language ("Floodlit Theatre")
Dark, restrained, premium. A near-black ground with a single blurple accent used as **line and glow**, never as a flood. Contrast comes from tone, not saturation. Generous negative space around the player card is intentional — it creates anticipation; **do not fill it with ads or chrome.** Warm amber (`#f0b79b`) is the single "heat/alert" signal (final call, timer stress, jump bids).

This design consumes the **Nocturne** design system. Pull colors/type/spacing from Nocturne tokens (`var(--color-*)`, `var(--font-*)`) in the target app rather than hardcoding — the hex values below are the resolved values for reference/QA.

---

## Design tokens

### Color (resolved values)
| Token | Value | Use |
|---|---|---|
| App canvas (outside phone) | `#0d0e14` | Prototype backdrop only |
| `--color-bg` | `#161826` | Screen background |
| `--color-surface` | ~`#1e2030` | Cards, tiles, inputs |
| `--color-divider` | subtle 1px on surface | Hairline borders |
| `--color-text` | `#e9e9ed` | Primary text (never pure white) |
| Muted text | `color-mix(text 45–62%)` | Secondary/metadata |
| `--color-accent` | `#9184d9` | Accent line/glow, primary borders |
| `--color-accent-100/200/300` | light→mid ramp | Accent text on dark (use 100/200 for legible accent text) |
| Heat / alert | `#f0b79b` | Final call, ≤3s timer, jump bids, drop-outs, overseas warnings |
| Ad-gold | badge `#cdae6e` bg / `#e6c98f` text | "AD" chip only |

Accent-on-dark passes ~3:1 (fine for large text, icons, chrome — **not** body copy; use `--color-accent-100/200` for small accent text). Never pure black/white.

### Typography
- **Family:** Inter for both headings (`--font-heading`) and body (`--font-body`). Headings weight **500 max** — hierarchy is size + space, never heavier than 500.
- **Scale in use (px):** player name reveal 27 / auction 20; screen titles 23–24; current-bid number 42 (weight 500); sold price 44 (weight 300); base price 34; section labels 9–10 (uppercase, letter-spacing .14–.2em); body 11–13; monospace eyebrows use `ui-monospace, Menlo` at 9–11.
- Numeric displays (prices, stats, timers) use the heading font.

### Spacing / radius / motion
- Dense scale (Nocturne density 0.7×). Common paddings 8–22px; gaps 6–14px.
- Radius: screen cards 13–14px; tiles 11–13px; pills/tags 20–22px; phone screen 32px.
- Lay out sibling groups with flex/grid + `gap` (not margins).
- **Motion:** screen-in fade+rise 0.34s `cubic-bezier(.2,.7,.3,1)`; bottom-sheets slide-up 0.28s `cubic-bezier(.2,.8,.3,1)`; SOLD stamp pop 0.5s `cubic-bezier(.2,.9,.3,1.4)`; call-pulse (breathing) 1.6s at "going once", 0.9s at "going twice"; war glow pulse 1.1s; live-dot pulse 0.9s; feed rows fade-in 0.25s.

---

## Currency & formatting
Purse and prices are in **lakhs internally** (₹120 Cr purse = `12000`). Display formatter:
- `< 100` → `₹{n} L` (e.g. `₹75 L`)
- `≥ 100` → `₹{n/100} Cr`, one decimal, trailing `.0` stripped (e.g. `₹1.5 Cr`, `₹2 Cr`)
- Note the **space** before the unit (`₹2 Cr`, not `₹2Cr`).
- Bid increment tiers: `< ₹2 Cr` → +₹20 L; `< ₹5 Cr` → +₹25 L; `≥ ₹5 Cr` → +₹50 L.

---

## Ruleset (game constants)
- 10 franchises; player controls 1, AI controls 9.
- Purse: **₹120 Cr** each.
- Squad: **18–25** players, **max 8 overseas** (overseas cap hard-blocks bidding/signing at 8/8).
- Two difficulties: **Easy** (cautious rivals, forgiving) / **Hard** (rivals contest scarcity & bait wars).
- Player pool is served in **batches by role** (Wicketkeepers → Top-order Batters → All-rounders → Pace bowlers → Spinners), one lot at a time.
- **Skip player**: free; resolves that one lot AI-side.
- **Skip batch**: resolves the rest of the current role-batch AI-side; gated behind a rewarded ad **or** the ₹49 ad-removal (see Monetization).
- No haptics.

*(The prototype ships a 10-player sample pool for demonstration; the real pool/economy will come from the backend — see Backend & services.)*

---

## Screens / views

### 1. Team Select
- **Purpose:** choose difficulty + franchise, then start.
- **Layout:** scrolling column. Title "Choose your franchise" + subtitle. **Difficulty**: 2-up segmented cards (Easy / Hard); selected card gets an accent inset ring + glow (Hard also shows a check). **Franchise**: 2-column grid of 10 tiles, each = crest + name + city; selected tile shows accent ring + check. Sticky footer button.
- **Footer:** disabled state "Pick a franchise to start" (secondary, 50% opacity) until a team is chosen → then primary "Start auction · {Hard|Easy}" (accent border + glow), height 54, radius 14.
- **Crests:** each franchise has a color + simple geometric glyph (see franchise table). Rendered as a rounded-square tile: `background: color-mix(teamColor 20%, surface)`, `inset ring color-mix(teamColor 55%)`.

### 2. Player Reveal
- **Purpose:** show the next lot before bidding; decide open / skip player / skip batch.
- **Header row:** difficulty tag (outline, warm) + `Batch {pos} of {size} · {RoleBatch}` on the left; `Lot {n} of 10` on the right. If arriving from a skip, a one-line flash strip appears: `Last lot · {name} → {team} · {price}` (or an ads-removed confirmation).
- **Body (centered):** "NEXT UP" eyebrow → circular avatar (112px, accent radial glow behind) → player name (27px) → tag row: role (accent) / country (`India`, or the full name with a plane icon for overseas, e.g. `✈ Australia`) / **`AGE {n}`** (outline) → 4 stat columns (BAT / BOWL / FLD / FORM; values ≥80 tint accent, `—` for N/A) → BASE PRICE label + big price (34px).
- **Earmark line (pre-stated rivalry):** if any rival has "earmarked" this lot, a small accent pill: `{Team} have earmarked this player` (1 target) or `{Team A} & {Team B} are circling this player` (2 targets), preceded by their crest(s). Marquee players always draw 2. **This is not cosmetic** — earmarked rivals are guaranteed into the auction with a raised ceiling, so they visibly contest.
- **Overseas block:** if the player is overseas and you're at 8/8, show a warm warning line and lock "Open bidding".
- **Footer:** primary **"Open bidding"** (accent border + glow, 54px). Row below: **"Skip player"** (secondary, with skip-glyph) + **"Skip batch"** (secondary; shows a small gold **AD** chip when ads are not removed, no chip after purchase).

### 3. Live Auction — the auctioneer's call
This is the core screen. The player card stays spatially stable; state layers on top of it.
- **Header:** left = **BIDDING WAR** chip (accent, pulsing dot) once ≥3 bids, else the difficulty tag; plus `Lot {n} of 10`. Right = a live "**{N} still in**" counter with a pulsing warm dot (this replaces a numeric timer).
- **Player block:** small avatar (76px) with a pulsing accent glow during a war; name (20px) + role/country tags.
- **Bid card** (accent-bordered, glowing):
  - `CURRENT BID` label (becomes `CURRENT BID · RISING` during a war).
  - Current amount (42px).
  - Leader row: crest + `{Team} leads` / `You lead` / `No bids yet`.
  - **Call strip** (divider above it): a large call line (18px) on the left + **3 pips** on the right. The call is the tension engine — see *Interactions*.
- **Below card:** `Next bid +₹{inc} · purse after ₹{x}`.
- **LIVE FEED:** running list (last 7), each a colored dot + line. Tones: your bids (accent), normal rival bids (muted), **jump bids** `{Team} jumps to ₹{x}` (warm), **drop-outs** `{Team} out` (dim).
- **TABLE rail** (toggleable): all 10 crests in a row with per-team status label (`Leads` / `You` / `In` / `Out` / `—`); leader gets an accent ring; out teams dim. Shows the field narrowing to a duel.
- **Footer:**
  - When **you lead**: a non-interactive primary bar `You're leading · ₹{x}` + sub reflecting the call ("Going twice — almost yours").
  - When **you don't lead**: **Pass** (secondary, fixed 100px) + **Bid ₹{next}** (primary, with "purse after ₹{x}" sub). If you can't afford or are overseas-blocked, the Bid button is disabled with the reason ("Purse too low" / "Overseas 8 / 8").

### 4. Sold (under the hammer)
- **Purpose:** the payoff beat. Auto-advances after ~1.9s (or tap "Next player").
- **Layout (centered):** big avatar (120px) with a **SOLD** stamp badge (pop animation); when **you** win, avatar bg/ring/glyph/stamp all shift to accent; a rival win uses that team's color; unsold uses neutral grey with an **UNSOLD** stamp.
- Player name (26px) + role/country. If sold: price (44px, weight 300) + winner chip (crest + team, `· you` suffix when yours) + a footnote (`Added to your squad · {n}/25 · purse ₹{x}` for you, or `You passed — no cost to your purse`). If unsold: "No bids met the base price — the lot goes unsold."

### 5. Auction Summary
- **Purpose:** close the session; loop back.
- **Layout:** "AUCTION COMPLETE" eyebrow → "Your squad is set" → your crest + team/city/difficulty → a 2×2 stat grid (**SPENT / PURSE LEFT / SQUAD (n/25) / OVERSEAS (n/8)**) → a rule-validity line (green check when within caps) → **league table** (all 10 clubs by spend, your row highlighted accent) with a toggle to **Review squad** (your signings list: name / role·country / price; empty-state if none). Footer: "Review squad" toggle + "Done" (→ restart to Team Select).

### Monetization sheets (calm, opt-in)
- **Skip-batch choice sheet** (bottom sheet, slide-up): title `Skip the {RoleBatch} batch` + reassuring subline (no countdown, no pressure). Two option rows:
  1. **Watch a short ad** — "Skip this batch now — free" → plays the rewarded ad.
  2. **Remove ads forever · ₹49** — "Skip batches instantly — no ads, ever" → opens the buy sheet.
  Plus a quiet "Not now".
- **Rewarded-ad overlay:** full-screen "REWARDED AD" mock (scanning shimmer, "Your ad plays here"), a 5s progress bar, then "Claim reward · skip batch". Cancelable.
- **Buy sheet (Remove Ads Forever):** shield icon + `Remove Ads Forever` / `One-time purchase · this Google Play account` / **₹49**. Three checked perks: *Skip player batches instantly — no ad · No banners or interstitials · No gameplay or auction advantage.* A "Pay with Google Play · UPI / cards" row. **Buy · ₹49** primary + "Not now". Fine print: *"No gameplay, AI, or auction advantage. Removes banners, interstitials & rewarded-ad requirements."*
- After purchase: batch-skip becomes **direct** (no sheet, no ad); the AD chip disappears. Purchase is persisted.

---

## Interactions & behavior

### The auctioneer's call (replaces a countdown timer)
Instead of a ticking clock, tension comes from the auctioneer's escalating call, on **jittered** (non-constant) timing:
- **Stages:** `0` resting → `1` "Going once…" → `2` "Going twice…" → `3` "Sold!" (→ Sold screen).
- At **stage 0** the call line shows a **pundit one-liner** reacting to the last bid (e.g. *"A statement bid from Ember Forge"*, *"The room narrows to Copper Hawks"*, *"It's a duel now"*, *"The room turns to you"*). It is chosen once per bid so it doesn't flicker.
- **Any bid** (yours or a rival's) resets the call to stage 0 and re-arms a fresh jittered delay.
- **Pips:** 3 dots; `i < stage` are lit; they warm to `#f0b79b` and the leading pip glows at stage ≥2.
- **Call pulse:** none at stage 0; breathing 1.6s at "going once"; faster 0.9s + warm color at "going twice".
- **Player Pass** is decisive: it fast-forwards the AI-only remainder of the lot to a result immediately (no watching rivals bid each other up).

### Jitter timing (per beat, scaled by a `pace` factor)
- open (first beat): 450–1000ms · after any bid: 800–1900ms · "going once" dwell: 1400–2500ms · "going twice" dwell: 1600–2800ms. Multiply by `pace` (prototype default 0.6; range 0.6–1.6).
- The hammer holds "Sold!" ~650ms on the auction screen before flipping to the Sold screen.

### Navigation flow
Team Select →(Start)→ Reveal →(Open bidding)→ Auction →(hammer/Pass)→ Sold →(auto ~1.9s / Next)→ Reveal (next lot) → … last lot → Summary →(Done)→ Team Select.
Reveal → Skip player → resolves 1 lot → next Reveal. Reveal → Skip batch → (ad or purchase) → resolves rest of batch → next batch's Reveal (or Summary).

---

## AI bidding model (make rivals feel human, not algorithmic)
Each rival has a **persona** (stable per franchise) that governs *when* and *how much* it bids:
- **aggressor** (Ember Forge, Sunset Royals, Golden Meridian): bids early & often, higher ceilings, most likely to jump-bid.
- **lurker** (Copper Hawks, Emerald Vanguard): quiet early, high chance to **snipe** at "going once/twice".
- **disciplined** (Monsoon Arc, Azure Current, Titan Coast): steady minimum raises, folds cleanly at ceiling.
- **emotional** (Crimson Sabres, Frost Lions): can get caught up — extra appetite during a war, occasional jump.

Per lot, each interested rival gets a private **max (ceiling)** = `base × (1.4 … ~4)` (personas/marquee widen the spread; earmarked rivals get a raised floor). On each jittered beat:
1. Mark any non-leading rival whose ceiling `< next bid` as **out** (feed: "{team} out"; dims in rail).
2. Each eligible rival independently "wants in" with probability = `desire(persona, stage, war) × ceilingFactor × globalAggression × difficulty × marqueeFactor` (capped ~0.92). `ceilingFactor` makes them **hesitate as price nears their own max**.
3. If any want in → the strongest bids. **Jump bids:** aggressors/emotional sometimes raise 2–3 increments (feed: "{team} jumps to ₹x"), clamped to their ceiling. This resets the call.
4. If none want in → the call advances one stage; at stage 3 the hammer falls.

Wars self-terminate (price only rises, ceilings are finite). `globalAggression` (prototype default 0.9; range 0.2–0.9) and `pace` are the two tuning knobs.

**Skip resolution (closed-form, instant):** for skipped lots the winner is the top ceiling, price = `min(topMax, secondMax + increment)`, floored at base; single-interest → base; no interest → unsold.

---

## State management
Per-session state to model:
- `screen` (select | reveal | auction | sold | summary)
- `myTeamId`, `difficulty`
- `lotIndex`, and per-batch position
- `myPurse` (lakhs), `mySquad[]` (player + price), `myOverseas` count
- `teamSpend{}` (per rival, accumulated across live wins + skipped-lot resolutions)
- `lastSale` (for the Sold screen)
- Auction-lot object (transient): `bid, leader, feed[], rivals[{id,persona,max,out,earmarked}], stage, count, comment`
- UI flags: `showAd, adCountdown, adReady, showSkipSheet, showBuy, showSquad, headerFlash`
- **Entitlement:** `adsRemoved` — **persisted** (prototype uses localStorage `goingOnce.adsRemoved`; in production this is the verified Play Billing entitlement, see below).

Timers: a self-scheduling jittered timeout drives the call; a ~1.9s Sold auto-advance; a 1s rewarded-ad countdown. Clear all on screen change/unmount.

---

## Backend & services

> The user will send detailed **Cyan** backend/integration docs separately. This section captures the contract the UI expects so it can be wired up; treat names as placeholders where noted and reconcile with the incoming docs.

### Player pool & economy (Cyan backend)
The prototype hardcodes a 10-player sample and random-ish rival budgets. In production these should come from the backend:
- **Player pool / lots**: ordered by role-batch, each with `name, role, country, overseas, age, bat/bowl/fld/form, base, batch, marquee`.
- **Rival budgets / difficulty tuning**: starting purses, persona assignment, difficulty modifiers.
- **Season payoff loop** (designed on the 1a board, not yet in this loop): finishing position + re-auction — expect backend endpoints for standings/simulation.
- Anything authoritative (economy, anti-cheat, seeds for reproducible auctions) should live server-side. *(Confirm exact Cyan endpoints/DTOs against the forthcoming docs.)*

### Rewarded ads — AdMob
- Rewarded ad is the **primary ad format**, user-initiated only. Used to unlock **Skip batch**.
- **No banner ads on the auction/gameplay screen** (immersion + accidental-click risk). If banners are ever tested, restrict to static screens.
- Avoid interstitials in V1.
- The reward callback grants the batch-skip; the UI's 5s overlay is a stand-in for the real ad SDK lifecycle (loaded → shown → rewarded → closed).

### Purchase — Google Play Billing
- Product id: **`remove_ads_lifetime`**
- Type: **one-time, non-consumable**
- Checkout: **Google Play Billing only** — no subscription, no third-party payment aggregator. Google Play presents UPI/cards; **the app must never collect payment details itself.**
- Entitlement effect: removes banners, interstitials, and rewarded-ad requirements; **batch skip becomes directly available**. **No gameplay, AI, auction, or season advantage.**
- Price: **₹49** launch hypothesis for India (grandfather early buyers if price later rises). Use Google Play's price experiments to optimize later — don't hardcode assumptions.
- Restore/verify entitlement on launch and per device/account; `adsRemoved` in the UI must be driven by the **verified** entitlement, not local flag alone (local persistence is only an offline cache).

---

## Franchises (name · city · color · crest glyph · persona)
| id | Name | City | Color | Crest | Persona |
|---|---|---|---|---|---|
| monsoon | Monsoon Arc | Mumbai | `#6fb0cf` | arc | disciplined |
| ember | Ember Forge | Chennai | `#d6805a` | flame/triangle | aggressor |
| copper | Copper Hawks | Mohali | `#b6745a` | down-chevron | lurker |
| azure | Azure Current | Delhi | `#7f8fe0` | waves | disciplined |
| crimson | Crimson Sabres | Hyderabad | `#c56b7a` | sabre | emotional |
| emerald | Emerald Vanguard | Bengaluru | `#5aa6a0` | shield | lurker |
| sunset | Sunset Royals | Jaipur | `#cdae6e` | crown | aggressor |
| titan | Titan Coast | Ahmedabad | `#6fa3b0` | wave | disciplined |
| frost | Frost Lions | Lucknow | `#9aa7c4` | double-chevron | emotional |
| golden | Golden Meridian | Kolkata | `#c9a24a` | sun | aggressor |

Crests are simple inline-SVG geometric marks (see the prototype's `glyph()` for exact paths) — placeholder identities; swap for real franchise branding when available.

---

## Assets
- **No external images.** All iconography is inline SVG (crests, gavel/ad glyphs, stat/skip icons — Phosphor-style stroke icons per Nocturne).
- **Player avatar is a deliberate placeholder** (wireframe stick-figure). Replace with a restrained fictional portrait, silhouette, or role illustration on a dark background — do not ship the stick-figure. Keep it centered in the 112px (reveal) / 76px (auction) / 120px (sold) circular frame with the accent radial glow.
- Fonts: **Inter** (headings + body).

---

## Known deferrals / notes for the developer
Designed but **not** in this loop (present on the 1a board / discussed): persistent **live purse gauge** on the auction screen, **watchlist** (per-player max price + reveal alerts), **season payoff** (finish position + re-auction), **rivalry + shareable squad card**. Also open: replacing the placeholder avatar, a low-contrast **accessibility pass** (FORM labels / metadata / muted greys), and QA on **short Android screens** (the reference phone is tall; verify no overlap once purse/leader/controls stack).

---

## Files in this bundle
- `Going Once - Playable Loop.dc.html` — the full interactive prototype (all 5 screens + monetization). **Primary reference.** Open in a browser to click through it.
- `Going Once - 1a Build.dc.html` — static design board: visual direction + the 5 engagement-hook explorations. Context/reference.
- `support.js` — design-tool runtime so the HTML runs. **Not part of the implementation.**
- `screenshots/` — 11 reference captures of every screen/state (see the *Screenshots* section above).
- `CLAUDE_CODE_PROMPT.md` — the kickoff prompt to paste into Claude Code alongside this folder.

*(Backend/Cyan detail docs to follow from the product owner.)*
