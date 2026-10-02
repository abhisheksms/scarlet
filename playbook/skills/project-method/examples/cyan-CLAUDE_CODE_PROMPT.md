# Prompt for Claude Code

> Worked example from cyan (Cricket Auction Simulator, July 2026), kept
> verbatim: the handoff from design to build that the method is based on.

> **Historical (July 2026):** the one-time brief used to start the build. It is
> done; don't paste it into a new session. Current guidance is root `CLAUDE.md`.

Copy everything below into Claude Code, and attach the `design_handoff_going_once/` folder plus your Cyan/backend docs.

---

I'm building **Going Once**, an Android-first, portrait, single-player IPL-auction simulator game. You're implementing the core loop from an approved design.

**Attached:**
- `design_handoff_going_once/` — the design handoff. Read `README.md` first; it's the spec. `screenshots/` has all 11 screens/states. `Going Once - Playable Loop.dc.html` is a clickable HTML prototype — open it in a browser to feel the real timing and interaction.
- Cyan cyan/cricket-auction-knowledge-base docs — the authoritative source for the backend, player pool, and economy.

**Important:** the HTML files are a **design reference to recreate**, not production code to port. Rebuild them natively using this codebase's own stack, patterns, and component library. `support.js` is a design-tool runtime — ignore it.

**Scope:** the five screens (Team Select → Player Reveal → Live Auction → Sold → Auction Summary), plus the two monetization surfaces (rewarded-ad batch skip, and the ₹49 `remove_ads_lifetime` one-time purchase via Google Play Billing only). Include the backend wiring: player pool, economy, and entitlement verification per the Cyan docs.

**Treat as intentional, not incidental:**
- **No countdown timer.** Tension comes from the auctioneer's call — pundit line → "Going once…" → "Going twice…" → "Sold!" — on *jittered*, non-constant timing. Any bid resets it.
- **Rivals must feel human, not algorithmic.** The persona model (aggressor / lurker / disciplined / emotional), jump bids, visible drop-outs, and ceiling hesitation are the point of that system.
- **Monetization stays calm.** No countdowns or pressure in the choice sheet; the purchase grants zero gameplay, AI, auction, or season advantage.
- Currency, increments, squad/overseas caps, and copy strings are exact — see the README.

Start by reading the README and the docs, then tell me your implementation plan and anything that conflicts with the existing codebase before you build.

---

**Note:** the README's *Backend & services* section was written from design-side context; treat your Cyan docs as authoritative where they differ. Deferred items (purse gauge, watchlist, season payoff, squad card, real player art) are listed at the end of the README — not in this scope.
