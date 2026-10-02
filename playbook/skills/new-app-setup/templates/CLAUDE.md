# CLAUDE.md

Working notes for Claude Code in this repo. Product policy lives in `AGENTS.md`
and the knowledge base; read those before changing behaviour. This file covers
how to *operate* the repo: commands, layout, conventions, gotchas. The studio's
shared playbook (founder taste, Play policy guard, Android release, Play launch,
new-app setup) is the `cyan-harbor` plugin enabled in `.claude/settings.json`.

## What this is

**<Listing title>** (internal name: <codename>) — an Android-first, offline-first
<one-line description>. React Native + Expo SDK <n>, TypeScript, pnpm
workspaces. No backend. Published by **Cyan Harbor**. Naming: Play listing title
is "<Listing title>", launcher label is "<Label>" (`name` in app.json), Expo slug
stays `<slug>` (changing it breaks project linkage). <Any trademark that must
never appear in store copy.>

The founder is non-technical: product, taste and direction are their strength.
Keep source legible, explain changes in plain English, and prefer clear names
over clever ones. Plain-English map of the codebase: `docs/FOUNDERS_GUIDE.md`.
**Read the `founder-taste` skill before writing any UI, copy, or monetization
surface.**

## Layout

```
apps/mobile/            Expo app — the only shipped artifact
  app/                  expo-router entry (_layout.tsx, index.tsx)
  src/screens/          every screen + its co-located *.test.tsx
  src/game/             zustand store, <App>.tsx shell, ErrorBoundary
  src/design/           tokens, icons, shared UI primitives
  src/storage/          SQLite key-value store + repositories
  src/ads|billing/      native adapters, each with a no-native fallback
  test/                 UI test harness (setup, mocks, helpers)
packages/<engine>       Pure deterministic engine — reducers, rules, RNG
packages/<content>      Catalogs, content schema versions
packages/presentation   Selectors mapping engine state to view models
<app>-knowledge-base/   Authoritative product spec + ADRs
docs/                   FOUNDERS_GUIDE, SECURITY_CHECKLIST, launch/
play-policy-config.json Per-app settings for the Play policy checker
```

## Commands

`pnpm` may not be on `PATH` in a fresh shell — `corepack pnpm …` works.

```bash
pnpm -r typecheck                              # every package (CI gate)
pnpm -r --filter "./packages/**" test          # engine + content + presentation
pnpm --filter @<scope>/mobile test             # both mobile Jest projects
```

CI (`.github/workflows/ci.yml`) runs exactly those three. Green CI is the merge
gate. Release builds are made **locally** with Gradle and the upload keystore;
the exact commands and checks are in the `android-release` skill.

**End-to-end codebase knowledge lives in the project skill
`.claude/skills/<app>-codebase-expert/`.** Keep it current when you change
something it describes.

## The engine is pure — keep it that way

`packages/<engine>` is a seeded, deterministic reducer. Same seed plus same
actions always yields the same outcome. Nothing in it may touch dates,
`Math.random`, storage, network, ads, billing or React. All randomness goes
through `src/rng`. `src/purity.test.ts` enforces it. Breaking this breaks
save/resume.

**Money is integer units, never floats.** <State the unit and the formatter's
home.>

## UI tests

`apps/mobile/jest.config.cjs` runs **two Jest projects** from one command, routed
by file location: **storage** (ts-jest in Node over `src/storage`) and **ui**
(jest-expo + `@testing-library/react-native` over everything else).
`test/setup.ts` mocks **only native modules**, so the real store, repositories
and engine all run.

Write screen tests as `.tsx`, co-located next to the screen. Rules that matter:

- **Every test must be able to fail.** No `toBeTruthy()`, no snapshot dumps.
- Build state with the real engine via `test/factories.ts`, not hand-made fixtures.
- Assert what a user sees, not style objects or component names.
- **Never pin copy the founder has already asked to change.** Reach for a stable
  `testID` and assert the behaviour instead.
- RNTL is v13: use `toBeSelected()`, not the removed `toHaveAccessibilityState`.

## Accessibility

Every `Pressable` ships `accessibilityRole` and `accessibilityLabel`. A control
whose selected state is shown only by a glyph also needs
`accessibilityState={{ selected }}`. When visible text carries a decorative `·`,
give speech a clean label. Bare Pressables announce as inert text and become
unreachable; this once hid an entire paid upgrade path from screen readers.

## Working agreements

- **Branch from `origin/main`, never from local `main`**
  (`git fetch && git switch -c <name> origin/main`). Several sessions share a
  checkout; before opening a PR, check `gh pr diff --name-only` lists only the
  files you meant to change.
- Branch → commit → `gh pr create` → wait for CI → squash-merge. Create the PR,
  let checks register, *then* watch with exit codes only
  (`gh pr checks N --watch > /dev/null; echo CI_EXIT=$?`).
- **Before merging anything, check it against `docs/SECURITY_CHECKLIST.md`** and
  surface anything relevant in the PR body.
- Never sweep unrelated uncommitted files into a commit — stage explicit paths.
- Commit `pnpm-lock.yaml` alongside any dependency change; CI uses
  `--frozen-lockfile`.
- No dark patterns, ever. No fake urgency, no countdowns on a one-time purchase,
  no ads triggered by losses or scarcity. Engagement comes from craft.

## Google Play policy

Policy is checked, not remembered. Use the `play-policy-guard` skill: run its
checker before any Play upload and after any change to ads, purchase or payment
wording, permissions or `app.json` android config, dependencies, data
collection, the privacy policy or site, the listing or graphics, or names. An
ERROR blocks the upload. The app's Play Console answers and audit log are in
`docs/launch/APP_RECORD.md`.

## Launch gate

`android.package` is **`com.cyanharborstudios.<app>`**. The first Play upload
makes it permanent: never change it. Ad ids: Google's test ids until the founder
sends the live ones; `test/launch-gate.test.ts` pins whichever is current and
fails if they drift. Never tap an ad in this app on a real device.

## Debugging a build on someone's phone

Settings shows a build stamp (`v<version> · <date>`) injected by `app.config.ts`.
Ask for it before investigating any "this change is missing" report; a stale
install has faked that before. A hand-installed build can't see Play purchases:
test purchases and Restore only on a copy installed from Play (internal testing).
