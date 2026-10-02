---
name: new-app-setup
description: How to start a new Cyan Harbor app from the studio's proven stack - Expo / React Native / TypeScript pnpm monorepo with a pure deterministic engine package, a thin app, adapters for ads, billing and storage, two Jest projects, GitHub Actions CI, a product knowledge base with ADRs, CLAUDE.md and AGENTS.md conventions, the security checklist and the Play policy config. Use it when the founder says "new app", "next project", "clone this app", "same stack as cyan", "set up the repo", or asks which versions, folders, tests or docs a new project should have; and when porting an existing app's feature list into a Cyan Harbor build.
---

# New app setup

The first app (Cricket Auction Simulator, repo `abhisheksms/cyan`) is the
reference implementation. `references/TECH_STACK.md` is its verified inventory,
written so the next app starts from it without re-discovering anything; the
version table there is the starting point, then check Expo's current SDK and
Play's current requirements (target API and Billing Library dates move every
year). `templates/` holds the files to copy, and `templates/README.md` says what
to change in each.

## Steps

0. **Run the method, not just the scaffold.** The `project-method` skill owns
   the order: PLAN.md and the knowledge base first, the design brief and the
   frozen handoff before building, Component 0 and the hard-problem spike as
   the only carve-outs. This skill is what Component 0 copies.
1. **Read first:** `founder-taste` (how it should feel), `TECH_STACK.md`
   (what it's built from), and the knowledge-base rules in
   `templates/knowledge-base/AGENTS.md` (what must stay true).
2. **Identity and platforms.** Decide with the founder: Play title (the category
   keyword people search), launcher label, repo name, `android.package`
   `com.cyanharborstudios.<app>` (permanent after the first upload), app slug for
   the site, Expo slug (never changes after EAS linkage), and whether `ios` is
   configured from day one (turqoise did; cyan didn't). Haptics are a per-product
   call, recorded in an ADR. Record every decision in the knowledge base with
   the date.
3. **Repo skeleton.** Root: `package.json`, `pnpm-workspace.yaml`, `.npmrc`
   (hoisted linker: load-bearing for Metro), `tsconfig.base.json`, `.gitignore`,
   `CLAUDE.md`, `AGENTS.md`, `.github/workflows/ci.yml`, `.claude/settings.json`
   (enables this plugin), `play-policy-config.json`, `docs/` (SECURITY_CHECKLIST,
   FOUNDERS_GUIDE, launch/), and a `<app>-knowledge-base/`. Then `apps/mobile`
   and one package per pure concern.
4. **Architecture, one way only:** user action → store → engine reducer → new
   state → presentation selectors → screens. The engine package is pure
   TypeScript with seeded randomness and a purity test; content and presentation
   are pure too; the app is the only layer that touches the device. Ads, billing
   and storage are adapters with a no-native fallback so Expo Go, web and tests
   run without them. One zustand store, one expo-router route, SQLite through a
   narrow key-value contract.
5. **Tests from day one.** ts-jest for packages; two Jest projects in the app
   (storage in Node, UI under jest-expo + RNTL) with only native modules mocked;
   an accessibility sweep over every Pressable; a launch-gate test that pins the
   package name and ad ids; `--ci` in CI. `references/testing-patterns.md` has
   the idioms and the traps.
6. **Monetization shape.** Opt-in rewarded ads and at most one non-consumable,
   outside the engine, price from Play, no dark patterns, entitlement derived
   from Play ownership. Ad ids live in one file. Data safety rows follow the SDKs.
7. **Knowledge base.** Start from `templates/knowledge-base/`: README, AGENTS,
   the document list, the ADR template and the stack ADRs carried over
   (re-accept them for the new app), `component-backlog.json`.
8. **Policy and launch.** `play-policy-config.json` from the policy guard's
   example; `docs/launch/` from the `play-launch` templates; run the checker
   before the first upload.
9. **First device build** via the `android-release` skill: new upload keystore,
   `budget_phone` emulator pass, build stamp in Settings.

## Porting another app's features (the "clone" case)

When the brief is "build something like app X in our stack":
- Write the feature inventory into the knowledge base as `00-product-vision.md`
  and `01-v1-scope.md` (must / later / never), screen by screen, with the data
  model and the monetization surfaces. Decide what to subtract (founder-taste §10).
- Copy mechanics and flows, never code, assets, names, art or copy. Run new names
  through the policy checker's lists; add that domain's trademarks and real people
  to `play-policy-config.json`.
- Anything the source app does that breaks a red line (payment steering, dark
  patterns, under-13 appeal, real identities) is out of scope, not a feature.

## What stays in the app repo, not here

The app's codebase-expert skill (`.claude/skills/<app>-codebase-expert/`), its
knowledge base and ADRs, `FOUNDERS_GUIDE.md`, `docs/launch/` with the real
listing, Data safety, graphics, launch plan and app record, the live ad ids and
product ids, `play-policy-config.json`, the site pages. When something generic is
learned there (a new policy check, a build gotcha, a taste call), move it here.
