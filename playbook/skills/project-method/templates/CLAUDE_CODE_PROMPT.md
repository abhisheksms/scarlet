# Prompt for Claude Code

Paste everything below into a fresh Claude Code session in the app repo, with
the frozen `design/design_handoff_<app>/` bundle present.

---

I'm building **<app>**, <one line>. You're implementing the core loop from an
approved design.

**Attached:**
- `design/design_handoff_<app>/`: the design handoff. Read `README.md` first;
  it's the spec. `screenshots/` has every surface in every state. The
  interactive HTML prototype shows the real timing and interaction.
- `<app>-knowledge-base/`: the authoritative source for the product's
  mechanics, data model and economy.

**Important:** the HTML files are a **design reference to recreate**, not
production code to port. Rebuild them natively using this codebase's own
stack, patterns and component library (React Native, Reanimated, hand-drawn
SVG).

**Scope:** <the surfaces and the monetization surfaces, named>.

**Treat as intentional, not incidental:**
- <the first load-bearing design decision>
- <the second>
- <monetization stays calm: no countdowns or pressure; the purchase grants no advantage>
- Currency, limits, caps and copy strings are exact; see the README.

Where the bundle and the knowledge base conflict, the knowledge base wins.

Start by reading the README and the knowledge base, then tell me your
implementation plan and anything that conflicts with the existing codebase
before you build.
