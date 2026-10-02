# Tech Stack — Cricket Auction Simulator

> Shared across Cyan Harbor apps (master copy in the playbook). Written on
> Cricket Auction Simulator; its names, numbers and paths are the worked example.

A complete, verified inventory of the technology this app is built on, written
so the next Cyan Harbor app (Android or iOS) can be started from it without
re-discovering anything. Every version number below was checked against
`package.json` files and `pnpm-lock.yaml`; every architectural claim against the
source. Snapshot: commit `7792cfa`, 14 August 2026, updated since through the
Expo SDK 57 upgrade and update 1.0.1 (September 2026).

---

## 1. At a glance

| Layer | Choice | Version (declared → resolved) |
|---|---|---|
| Platform | Expo (managed workflow, dev builds) | `expo ~57.0.24` |
| UI framework | React Native (New Architecture) | `0.86.3` (pinned) |
| React | react / react-dom | `19.2.3` (pinned) |
| Language | TypeScript, `strict` everywhere | `^5.6.3` → 5.9.3 |
| Routing | expo-router (single route; screens are store-driven) | `~57.0.22` |
| State | zustand (one store, no middleware) | `^5.0.2` → 5.0.14 |
| Persistence | expo-sqlite via a narrow key-value contract | `~57.0.3` |
| Animation | react-native-reanimated (transform/opacity only) + react-native-worklets | `~4.5.1` / `0.10.1` |
| Vector art | react-native-svg (hand-drawn icons/crests) | `15.15.4` (pinned) |
| Ads | react-native-google-mobile-ads (rewarded only) | `^17.0.0` → Mobile Ads SDK 25.4.0, UMP 4.0.0 |
| Billing | react-native-iap / OpenIAP (one non-consumable) | `^16.6.2` → Play Billing 9.1.0 |
| Monorepo | pnpm workspaces (no turbo/nx), Corepack-pinned | `pnpm@9.15.0`, Node ≥ 20 |
| Tests | Jest 29: ts-jest (Node) + jest-expo (UI) + RNTL v13 | jest 29.7.0, RNTL 13.3.3 |
| CI | GitHub Actions, single `verify` job | actions v4 generation |
| Native builds | Local Gradle, signed with the upload keystore (EAS profiles exist but aren't used for releases); R8 shrinking on since build 7 | JDK 17 |
| Android levels | Play requires API 36 for new apps (31 Aug 2026) | min 24 / target 36 / compile 36 |
| Backend | **None.** Offline-first, no accounts, no network API | ADR-002 |

Published by **Cyan Harbor**. Android-first; the codebase contains no
iOS-specific configuration yet (no `ios` key in `app.json`), but nothing in the
stack is Android-only — Expo, react-native-iap and the ads SDK all support iOS.

---

## 2. Monorepo & tooling

```
cyan/
  apps/mobile/                 @cyan/mobile — the only shipped artifact
  packages/auction-engine/     @cyan/auction-engine — pure deterministic engine
  packages/game-content/       @cyan/game-content — players, teams, catalogs
  packages/presentation/       @cyan/presentation — selectors + money formatting
  packages/game-testkit/       @cyan/game-testkit — simulation harness
  cricket-auction-knowledge-base/  product spec + ADRs (authoritative)
  docs/                        founders guide, security checklist, launch plan
  site/                        static studio site (plain HTML/CSS, no build)
```

- **pnpm workspaces**, globs `packages/*` + `apps/*` (`pnpm-workspace.yaml`).
  Internal deps use the `workspace:*` protocol under the `@cyan/*` namespace.
- **pnpm pinned via Corepack**: `packageManager: "pnpm@9.15.0"` in root
  `package.json`; `engines.node: ">=20"`. No `.nvmrc`.
- **`.npmrc` is load-bearing**: `node-linker=hoisted` is required for Metro's
  resolver in a pnpm monorepo (Expo's recommendation), plus
  `link-workspace-packages=true`, `prefer-workspace-packages=true`,
  `save-workspace-protocol=rolling`.
- **No task orchestrator** — no turbo, nx, or lerna. Plain recursive pnpm:
  `pnpm -r typecheck`, `pnpm -r --filter "./packages/**" test`,
  `pnpm --filter @cyan/mobile test`.
- **Packages ship raw TypeScript source** (`main`/`types` point at
  `src/index.ts`) — no build step anywhere except the app itself. Metro
  compiles workspace TS directly; Node tools use ts-jest.
- **Metro** (`apps/mobile/metro.config.js`): watches the workspace root,
  resolves from both app and root `node_modules`,
  `disableHierarchicalLookup: true`. Native-only modules
  (`react-native-google-mobile-ads`, `react-native-iap`, `expo-sqlite`) are
  stubbed to empty modules on the **web** platform so the web preview builds;
  the adapters detect the stub and fall back to dev behaviour.
- **Babel**: `babel-preset-expo` only. It adds the react-native-worklets
  plugin for Reanimated 4 by itself; listing a plugin again applies it twice.
- **`pnpm.overrides`** in root `package.json` force-patch transitive
  build-tooling CVEs (`brace-expansion`, `postcss`, `ajv`, `uuid` 11.x,
  `@xmldom/xmldom` 0.8.13); a comment documents that none reach the shipped
  bundle. `tar` stays at 6.2.1 deliberately (tar 7 is ESM-only and breaks
  `@expo/cli`; real fix is the next Expo SDK).

### TypeScript configuration

`tsconfig.base.json`, extended by all four packages (the app extends
`expo/tsconfig.base` instead):

- `strict`, plus the stricter extras: `noUncheckedIndexedAccess`,
  `exactOptionalPropertyTypes`, `noImplicitOverride`,
  `noFallthroughCasesInSwitch`, `noUnusedLocals`, `noUnusedParameters`.
- `target: ES2021`, `module: CommonJS`, `isolatedModules`.
- Every package defines `typecheck: "tsc --noEmit"` — the fastest CI failure.

Root dev-tooling versions: jest `^29.7.0`, ts-jest `^29.2.5` → 29.4.12,
prettier `^3.3.3` → 3.9.6, `@types/node` `^22.9.0` → 22.20.1. (The root
`lint`/`format` scripts reference eslint/prettier but no config files exist —
linting is effectively `expo lint` in the app only.)

---

## 3. Architecture: four layers, one direction

```
game-content ──▶ auction-engine ◀── presentation
                      ▲                  ▲
                      └────── mobile ────┘
```

The dependency arrows only point inward. The engine depends on nothing. This
layering is the single most load-bearing decision in the codebase (ADR-003) and
the one to copy first into any new game.

### 3.1 `@cyan/auction-engine` — pure deterministic engine

**Zero runtime dependencies** (its `package.json` has none at all). No React,
no RN, no Expo, no storage, no network, no `Date.now`, no `Math.random`. The
rule is enforced by a test, not convention: `src/purity.test.ts` greps every
engine source file (comments stripped) for forbidden identifiers.

- **State machine**: `AuctionState` moves through phases `PLAYER_REVEAL →
  OPEN_BIDDING → GOING_ONCE → GOING_TWICE → SOLD | UNSOLD → BETWEEN_PLAYERS →
  COMPLETED`, plus `PAUSED` (with `resumePhase`). Seven serializable actions:
  `START_BIDDING`, `USER_BID`, `USER_PASS`, `BEAT`, `ADVANCE_PLAYER`, `PAUSE`,
  `RESUME`.
- **`reduceAuction(state, action) → AuctionTransition`** returns
  `{ nextState, emittedEvents, scheduledIntent }`. The engine never reads a
  clock: `ScheduledIntent` names the *timing intent* (`beat` with a jitter
  window, `hammer` hold, `advance` delay) and the UI schedules real timers.
  `BEAT` — the auctioneer's heartbeat — replaces any countdown.
- **Event log**: append-only `AuctionEvent[]` (`PLAYER_REVEALED`,
  `BIDDING_OPENED`, `TEAM_BID`, `TEAM_OUT`, `TEAM_BACK_IN`, `GOING_ONCE`,
  `GOING_TWICE`, `PLAYER_SOLD`, `PLAYER_UNSOLD`, `AUCTION_COMPLETED`), each
  with an ordinal `index`. The UI's live feed and batch roster are projections
  of this log.
- **Seeded RNG** (`src/rng/random.ts`): xmur3 string-hash → mulberry32 uint32
  stream, dependency-free. The generator state is a single uint32 **serialized
  inside `AuctionState`**, so a resumed save continues identically.
  `Rng.fork(namespace)` derives independent streams — the season simulator's
  seed is forked from the auction seed so presentation can never disturb it.
- **Money** (`src/money/money.ts`): `Lakhs` is a branded non-negative
  **integer** count of INR lakhs. ₹1 Cr = 100. Floats are forbidden;
  `subtractMoney` throws on negative results. Formatting lives in
  `@cyan/presentation`, never in the engine.
- **Ruleset** (`src/model/config.ts`, `DEFAULT_RULESET`, id
  `"ipl-2026-baseline"`): purse `lakhs(12500)` (₹125 Cr), per-player cap
  `lakhs(3000)` (₹30 Cr), squad 18–25, max 8 overseas.
- **Bid ladder** (`src/money/bid-increment.ts`, the founder's final call):
  **+₹10 L below ₹1 Cr, +₹20 L below ₹3 Cr, +₹50 L at/above ₹3 Cr.** Every
  raise is exactly one rung — jump bids were removed.
- **AI bidders** (`src/ai/`): each of the 9 rivals gets a hidden per-lot
  `ceiling` (never shown to the UI or other AI). Four `Persona` types —
  `aggressor`, `lurker`, `disciplined`, `emotional` — with desire tables per
  call stage (lurkers snipe late, emotional teams heat up in wars). Valuation
  is a demand-index (convex curve over ratings; superstars pull clear) times
  budget-per-remaining-slot, so money follows talent and purses finish near
  empty. `ROLE_COMFORT` loads stop any team hoarding one role. Grudges
  (duels remembered 8 lots, snatches 40) raise desire but **never** raise a
  ceiling — pressure without market distortion. A deterministic pundit line
  (`ai/commentary.ts`) is flavour derived from public facts.
- **Persistence contract** (`src/persistence/save.ts`):
  `serializeAuction`/`deserializeAuction` are plain lossless JSON —
  `AuctionState` carries the hidden ceilings and RNG position, so a resumed
  auction is bit-identical. `AUCTION_SCHEMA_VERSION = 1` plus a
  required-keys check; a versioned fixture (`auction-save-v1.json`) guards
  compatibility in tests.
- **Season payoff** (`src/season/season.ts`, `SEASON_ALGORITHM_VERSION = 2`):
  deterministic abstract round-robin with scorelines and net run rate,
  four-team playoff, champion — seeded by forking the auction seed. Cannot
  read ads/analytics/watchlists/difficulty. `season/rivalry.ts` derives
  presentation-only rivalry deltas.
- Engine helpers the app calls beyond the reducer: `createAuction`,
  `resolveLotToEnd` (fast-forwards the AI after a pass), `skipCurrentPlayer`,
  `skipBatch`, `simulateSeason`, `deriveRivalryDeltas`, `evaluateSquad`,
  `isSquadValid`, `deriveEarmarks`, `nextBid`.

### 3.2 `@cyan/game-content` — fictional catalogs

One dependency: the engine (for types + validation). Content is code, not
JSON — validated at module load by the engine's `makePlayer` (malformed
content throws before any auction starts).

- **182 players** across five role batches served in order: Wicketkeepers
  (36), Top-order Batters (37), All-rounders (36), Pace bowlers (37),
  Spinners (36). 117 Indian, 65 overseas across 8 nations. Base prices in
  {50, 75, 100, 150, 200} lakhs.
- **Derived tiers, never hand-flagged lists**: superstars = `max(bat,bowl) ≥
  90 && reputation ≥ 93` (currently 7); marquee = top 14 by a deterministic
  stature sort. Change the ratings and the tiers follow.
- **10 fictional franchises** (`TEAM_PROFILES`): id, name, city, engine
  `Persona`, hex colour, and a `CrestGlyph` identity (10 geometric glyphs) —
  content stores only the identity; the design system draws the SVG. Persona
  spread: 3 disciplined, 3 aggressor, 2 lurker, 2 emotional.
  `toEngineTeams(humanId)` marks one team `controller: "human"`, nine `"ai"`.
- `CONTENT_SCHEMA_VERSION = 1`. No real player/franchise identities — a legal
  rule with its own tests (name distance, no 1:1 kit/crest mapping).

### 3.3 `@cyan/presentation` — view models without React

One dependency: the engine. **No React, no React Native** — pure functions
`AuctionState → view model`, unit-testable in Node. A boundary test scans its
imports to keep it that way.

- `formatMoney` is the single money-display convention (`₹85 L`, `₹2 Cr`,
  `₹1.5 Cr`, `—` for null).
- ~20 selectors: `revealInfo`, `callInfo` (going-once pips), `warInfo`
  (war = 3+ bids; final duel), `leaderInfo`, `humanBidStatus` (with reason
  texts like "Purse too low"), `liveFeed` (event-log projection),
  `tableRail`, `purseGauge`, `soldInfo`, `summaryInfo`, `seasonSummary`,
  `teamsBoard`, `publicSquad` (public info only — never AI ceilings),
  `batchRoster`, `sharePayload` (sanitized: no AI internals, no billing
  state, no device ids), `playerArchetype` (deterministic one-liner from
  stats).

### 3.4 `@cyan/game-testkit` — simulation harness

Depends on engine + content. Runs full headless auctions deterministically
and audits **every intermediate state** (committed money ≤ purse; AI must
retain fill-money for remaining minimum-squad slots) plus finished-squad
rules. `simulate(runs, …)` aggregates spend share/spread, unsold rate,
bidding-war distribution; `priceBand` tracks named-player price ranges;
`estimateSessionMinutes` converts beat windows into wall-clock estimates.
Balance changes are validated here, not by hand-playing: tests pin spend
floor (≥ 92% of purse), unsold-rate ceilings, role-glut bounds, and mean
session length (112 ± 8 minutes on both difficulties).

---

## 4. The mobile app (`@cyan/mobile`)

### 4.1 Shell & navigation

expo-router with **exactly two route files**: `app/_layout.tsx`
(GestureHandlerRootView → SafeAreaProvider → StatusBar `style="light"` (light
icons over the dark theme) → headerless Stack with fade animation) and `app/index.tsx`
(`<GameErrorBoundary><Game /></GameErrorBoundary>`). All "navigation" is
store-driven: `Game.tsx` maps `auction.phase` to a screen (Home / TeamSelect
/ Reveal / Auction / Sold / Summary) and renders overlays in a fixed stacking
order. The router exists for deep-link scheme (`cyanauction`) and future
expansion, not for screen flow. One `BackHandler` hook (`useAndroidBack`)
closes the topmost overlay first, then exits to home.

### 4.2 State: one zustand store

`src/game/store.ts` — a single `create<GameStore>()` store (`useGame`) owning
selection, engine state, monetization state, and overlay flags. Key patterns:

- **The engine drives, the store schedules.** Every game action calls the pure
  `reduceAuction` and hands the result to `_apply`, which stores state,
  autosaves, then turns the engine's `ScheduledIntent` into real `setTimeout`s
  — jittered by difficulty pace and player/bid tempo. Timers live in module
  scope so they can always be cancelled. `Math.random` appears **only**
  app-side (beat jitter, seed generation: `g-<random int>`), never in the
  engine.
- **Autosave at resting phases only** (`PLAYER_REVEAL`, `SOLD`, `UNSOLD`,
  `COMPLETED`) — never mid-bid. Exit mid-bid applies `PAUSE` first so the
  frozen phase serializes safely.
- **Overlay-pause discipline**: one function (`_syncOverlayPause`) pauses the
  engine when the first overlay opens and resumes on the last close. The
  player-stats sheet is deliberately excluded — bidding runs live under it.
- **`init()` order**: entitlement load → billing restore reconcile → localized
  price query → ad init *only if* the ad policy allows → a foreground hook
  that re-checks ownership whenever the app returns, so a pending payment that
  clears in the background is granted and acknowledged → rivalries/watchlist →
  save-slot summaries. Ownership unresolved = ads denied (default-deny).
- Two save slots, slot-picker home screen, no auto-resume.

### 4.3 Persistence: SQLite behind a key-value contract

`src/storage/` — one narrow interface (`KeyValueStore`: get/set/remove with a
`schemaVersion` per row) with three implementations: `SqliteKeyValueStore`
(expo-sqlite, single `kv` table, db `cyan.db`), `LocalStorageKeyValueStore`
(web preview), `MemoryKeyValueStore` (tests). Four repositories on top:

| Repository | Key(s) | Notes |
|---|---|---|
| `GameRepository` | `game.slot.1`, `game.slot.2` | envelope `{myTeamId, difficulty, auctionJson}`; corrupt saves are **cleared, never crash** |
| `SqliteEntitlementRepository` | `entitlement.remove_ads` | local cache of Play ownership; idempotent grant/revoke |
| `RivalryRepository` | `rivalry.records` | idempotent via applied-auction-id list; narrative only |
| `WatchlistRepository` | `watchlist.player-ids` | app-only; invisible to engine/AI |

Newer-schema rows are treated as missing (forward-compatible), corrupt rows
fall back safely. Engine state goes in as an opaque serialized string — the
DB never knows the auction's shape.

### 4.4 Native adapters: ads & billing

Both follow the same pattern worth copying: **narrow TS interface → factory →
lazy `require()` probe of the native module inside try/catch → graceful
fallback**. Native absence (web stub, Expo Go, unit tests) can never block
gameplay.

- **Ads** — rewarded-only, user-initiated (batch skip). `RewardedAdController`
  interface; AdMob implementation requests non-personalized ads; a dev
  overlay (5s countdown) is the fallback. The live unit (since 2026-09-23) is
  `REWARDED_AD_UNIT_ID` in `src/ads/adIds.ts`, the one place real ids go.
  Setting it back to `undefined` falls back to the SDK's `TestIds.REWARDED`, so
  reverting to test ads is one edit. `test/launch-gate.test.ts` pins the live
  app and unit ids, and fails CI if they drift or disagree, or if a publisher
  other than ours or Google's sample one appears in source.
  `deriveAdPolicy`: unresolved ownership → no ads; entitled → no ads + direct
  batch skip.
- **Billing** — one product, `remove_ads_lifetime`, non-consumable.
  `BillingGateway` interface (`queryOffer`/`purchase`/`restore`) with
  `PlayBillingGateway` (react-native-iap: grant only on `PURCHASED`, treat
  Android `purchaseStateAndroid === 2` PENDING as granting nothing,
  acknowledge via `finishTransaction` — unacknowledged purchases are
  auto-refunded by Play in ~3 days, restore is authoritative) and
  `DevBillingGateway` (instant success mock). Displayed price is always
  Play's localized price. With no offer from Play the purchase is hidden and
  Buy disabled, never shown with a built-in price (1.0.1).
- **Analytics** — an interface with ~10 coarse events; `ConsoleAnalytics` in
  dev, `NoopAnalytics` in production. No third-party analytics SDK ships.
  No purchase tokens/receipts ever pass through it.

### 4.5 Design system

`src/design/` — no UI kit dependency; everything hand-built:

- `tokens.ts` — the "Nocturne / Floodlit Theatre" theme: near-black blue
  ground `#161826`, blurple accent `#9184d9`, warm heat `#f0b79b`, never pure
  black/white; Inter for text (weights cap at 500), Menlo for numbers; spacing
  4–22; radius tile 12 / card 14 / pill 22; named motion durations
  (screenIn 340ms, soldPop 500ms, …).
- `ui.tsx` — primitives (`Card`, `Tag`, `Pill`, `Button` with
  primary/secondary/leading/standing variants, `StatColumns`).
- `icons.tsx` / `Crest.tsx` — hand-drawn `react-native-svg` glyphs on 24×24;
  crests are tinted rounded squares + geometric glyph, generated from team
  colour (`mixHex` — an RN substitute for CSS `color-mix`).
- `motion.tsx` — reanimated components, every one `useReducedMotion()`-aware,
  transform/opacity only (ADR-005: no Skia without a profiled prototype).

The static `site/` reuses these tokens in plain CSS so studio site and game
read as one product.

### 4.6 Screens

12 screen/overlay files in `src/screens/`, each with a co-located test
(plus dedicated `accessibility` and real-ad-flow suites):
home (save slots), team select, splash (2.4s gavel choreography), reveal
(7s auto-open), auction room (live feed, table rail, purse gauge, bid/pass),
sold/unsold stamp, season summary (points table + NRR + OS share), watchlist
(sortable scouting board, 50-player cap), teams board + squad inspector
(public info only), batch roster, settings (restore purchases, build stamp),
monetization sheets (skip-batch chooser, remove-ads purchase, rewarded-ad
overlay).

---

## 5. Native configuration & builds

- **`app.json`** — name "Cricket Auction", slug `going-once` (**never change
  it** — it is the EAS project linkage; the Play listing title "Cricket
  Auction Simulator" is a store-console concern, not a repo one), version
  1.0.1, portrait, dark UI, `newArchEnabled: true`, scheme `cyanauction`,
  owner `cyan-harbor`.
- **Android**: package `com.cyanharborstudios.cricketauction` (permanent
  since the first Play upload — never change), `versionCode 7` (1.0.0 went to
  production as build 5; 1.0.1 is build 6, and build 7 adds R8),
  `allowBackup: false` (a cloud restore must not bypass Play as the
  entitlement source of truth), adaptive icon on `#161826`.
- **Plugins**: `expo-router`, `react-native-google-mobile-ads`, `expo-sqlite`,
  `expo-status-bar`, and `expo-build-properties` (native ABIs limited to
  `armeabi-v7a` and `arm64-v8a`, since the x86 emulator ABIs doubled build
  time; R8 minify and resource shrinking, with keep rules for the Nitro and
  OpenIAP billing bridge).
  The Kotlin 1.9.25 pin is gone (RN 0.86 needs Kotlin 2.x)
  and so is the `react-native-iap` plugin entry — v16 ships no config plugin,
  and prebuild fails outright if it is listed.
- **`app.config.ts`** — dynamic config that only injects build identity:
  short commit (EAS env → CI env → local git → `"dev"`) and build date.
  Settings renders the selectable stamp `v{version} · <date>` (`src/buildInfo.ts`)
  so a stale install can never fake a bug report.
- **`eas.json`** — `development` (dev client APK), `preview` (internal APK),
  `production` (app-bundle, `autoIncrement`). House rule: build **locally**
  (`eas build --local` / Gradle), never cloud EAS, and never build unprompted.
- The knowledge base's baseline is Android 8 (API 26), but the build ships
  Expo 57's default minSdk 24 (Android 7). Compile and target are API 36.
- **No iOS config yet.** For an iOS app add the `ios` key (bundle id under
  `com.cyanharborstudios.*`), StoreKit products mirroring the Play setup
  (react-native-iap abstracts both), and App Tracking Transparency review for
  the ads SDK.

---

## 6. Testing

**57 test files** across the repo; the philosophy is "mock the platform, run
the product" — nothing stubs app logic.

- **Engine/content/presentation/testkit**: ts-jest in Node, byte-identical
  jest configs. Engine: 21 suites including purity (source grep),
  save-compatibility fixtures, reducer unit + integration. Presentation: a
  boundary test enforcing its import allowlist. Testkit: statistical
  balance suites over dozens of seeded runs.
- **Mobile** (`jest.config.cjs`, one command, two projects):
  - *storage* — ts-jest, Node, `src/storage` only.
  - *ui* — jest-expo + `@testing-library/react-native` v13
    (`toBeSelected()`, not the removed `toHaveAccessibilityState`),
    everything else.
- **`test/setup.ts` mocks only native modules**: reanimated (official mock),
  safe-area (official mock), svg (inert host elements), expo-router (spyable
  mock), expo-constants (literal mirroring app.json), **expo-sqlite → a real
  in-memory SQL-verb implementation** so the actual `SqliteKeyValueStore` and
  repositories run, and empty stubs for ads/iap reproducing the no-native
  fallback. Global `afterEach` factory-resets the zustand singleton and the
  in-memory DBs.
- **`test/factories.ts` builds state with the real engine**
  (`makeAuction`, `makeBiddingAuction`, `beatUntil`, `makeCompletedAuction`)
  — no hand-made fixtures. `renderScreen(ui, { state })` merges partial state
  into the real store.
- House rules: every test must be able to fail; assert what a player sees;
  never pin copy that is in flux (use `testID`); a launch-gate test pins the
  package and the live ad ids, and fails CI if they drift.

---

## 7. CI & engineering process

- **`.github/workflows/ci.yml`** — the only workflow. Push to `main` + all
  PRs, concurrency-cancelled per ref. One job: checkout → pnpm 9.15.0 →
  Node 20 (pnpm cache) → `pnpm install --frozen-lockfile` →
  `pnpm -r typecheck` → package tests `--ci` → mobile tests `--ci`.
- **Green CI is the merge gate.** Flow: branch → commit → `gh pr create` →
  wait for checks → squash-merge. No pre-merge review pass beyond the
  advisory security checklist (`docs/SECURITY_CHECKLIST.md`), which is read
  before merging and surfaced in the PR body.
- Lockfile committed with every dependency change; explicit staging only
  (never `git add -A` — and `*.apk`/`*.aab` are gitignored because a release
  APK is ~70 MB).

---

## 8. Cross-cutting decisions to inherit in the next app

These are the durable, portable rules (ADRs + `AGENTS.md` +
`docs/FOUNDER_TASTE.md` — the taste doc explicitly says to copy it into every
new Cyan Harbor project):

1. **Offline-first, no backend, no accounts** (ADR-002). If a backend is ever
   activated (its own ADR required), the pre-decided baseline is MySQL as the
   durable record, Redis as disposable cache only, Kafka for genuinely async
   work with versioned idempotent contracts, Grafana over
   OpenTelemetry-compatible signals (ADR-008).
2. **Pure deterministic core** (ADR-003/006): game rules in a dependency-free
   TS package; seeded RNG serialized in state; time injected; purity enforced
   by test; UI schedules, engine decides.
3. **Money is integers** in the smallest domain unit, branded type, formatting
   in a presentation layer.
4. **Multiplayer-ready shape without multiplayer** (ADR-004): actor ids on
   actions, ordered serializable events, no UI refs in domain state.
5. **Lean dependencies** (ADR-005): no production dependency for a minor
   effect; every native dep justified; transform/opacity motion with a
   reduced-motion alternative; no haptics.
6. **Monetization with no dark patterns**: one honest non-consumable,
   rewarded-only user-initiated ads, ads denied while ownership is
   unresolved, offers only on calm surfaces, never triggered by losses or
   scarcity, no fake urgency ever. Tests assert the absence of dark-pattern
   copy.
7. **Engagement integrity**: never fabricate wars/scarcity/rivalry; watchlist
   private and invisible to AI; season outcomes independent of ads,
   analytics, difficulty, presentation.
8. **Accessibility as polish**: every Pressable has role + label; glyph-only
   states get `accessibilityState`; clean speech labels; TalkBack users exist
   at scale in this market.
9. **Content identity rule**: fictional, culturally authentic names; no
   near-spellings of real players; crests/colors never map 1:1 to real
   franchises; "IPL" never in store copy.
10. **Security posture** (`docs/SECURITY_CHECKLIST.md`): protect the
    entitlement, the store/ad accounts, SDK data disclosures, and the signing
    pipeline; parameterized SQL; `JSON.parse` never `eval`; secrets only in
    EAS/CI stores; keystore never in the repo; a rooted user cheating their
    own offline game is accepted risk — but only because nothing it unlocks
    affects fairness.
11. **Founder taste** (`docs/FOUNDER_TASTE.md`): no AI-slop tells, Title Case
    CTAs, diegetic teaching, core play screens never scroll, 60fps on a cheap
    phone in release builds, ~2s choreographed splash, sound off until
    excellent, "friction is allowed; deception is not."
12. **Build identity in Settings** (selectable version + date stamp) — ask for
    it before debugging any "change is missing" report.

---

## 9. How cyan was actually built — the working method

The process is as reusable as the stack. 85 commits took the project from
first design doc (23 July 2026) to launch-ready (14 August 2026) — about
three weeks — and the phases below are visible in the git history.

### The division of labor

The founder is **non-technical by design**: product judgment, taste, and
direction are their contribution; Claude Code is the engineering partner that
writes every line. Three documents make that split work:

- **`CLAUDE.md` + `AGENTS.md`** — the standing operating contract for the AI:
  commands, conventions, product policy, and hard rules (billing integrity,
  no dark patterns, engine purity). New sessions start aligned instead of
  being re-briefed.
- **`docs/FOUNDERS_GUIDE.md`** — a plain-English floor plan of the codebase
  ("brains vs. body") so the founder can follow "I fixed the AI bidding"
  without reading code.
- **`docs/FOUNDER_TASTE.md`** — the founder's product taste codified as
  checkable rules once patterns emerged from feedback (written 4 August,
  after three feedback rounds). It explicitly says to copy it into every new
  Cyan Harbor project.

The knowledge base even contains a JS/TS learning plan for the founder
(doc 10) — understanding grows alongside the product, but never blocks it.

### Phase 1 — Spec before code (23 July)

The repo's first real commit is "add initial design docs": the
`cricket-auction-knowledge-base/` — fourteen numbered documents with a
prescribed reading order (product vision → v1 scope → core loop → domain
model → auction state machine → AI bidding → application architecture →
implementation plan → testing strategy → maintenance rules → engagement
loop → billing) plus eight ADRs recording every irreversible decision
(RN + TypeScript, offline-first, pure engine, AI-first single-player, lean
deps, deterministic season, Play Billing, conditional backend). **Nothing
was coded until the decisions were written down**, and the KB's own rule
keeps it honest: docs describe current behavior, not aspiration; deprecated
ideas are marked, never deleted.

### Phase 2 — Design before code

Visual design happened as its own artifact, not inside the app:
`design_handoff_going_once/` holds a **high-fidelity interactive HTML
prototype** (a clickable "playable loop" plus a static design-direction
board, built with Claude as `.dc.html` design components), 11 annotated
reference screenshots, and a handoff README that doubles as the design spec —
final colors, type, spacing, motion, and copy, with the intentional decisions
named so they survive translation: *no countdown timer* (tension comes from
the auctioneer's jittered call), *rivals must feel human* (the persona
model), *monetization stays calm*. Crucially the bundle includes
`CLAUDE_CODE_PROMPT.md` — a ready-to-paste brief telling Claude Code to
**recreate the design in the codebase's own stack, not port the prototype
code**, and to read the knowledge base as authoritative where the two
conflict.

### Phase 3 — Engine-first build (28 July)

One day, one branch: "Build Going Once core loop — engine, content,
presentation, Expo app" landed the whole four-layer architecture, then the
knowledge base's implementation plan was executed as numbered components in
the same PR stream — motion (7), persistence (8), squad strategy (9), AI
depth + the simulation harness (10), season payoff and rivalries (11),
onboarding/settings/accessibility (12), real AdMob + Play Billing adapters
(13). Each component landed with its tests; the purity test and the
simulation harness existed before any balance tuning did.

### Phase 4 — Iterate under CI (29 July onward)

PR #2 added GitHub Actions, and from then on **green CI was the merge gate**:
branch → PR → typecheck + full test suite → squash-merge, one feature per PR
(purse gauge, watchlist, team inspector, web preview, EAS config…). An
advisory PR-review bot was tried and later dropped — CI plus tests proved to
be the right gate, and extra pre-merge process just slowed the loop.

### Phase 5 — Founder feedback rounds (August)

The defining rhythm of the polish phase: the founder **plays a real APK on a
physical phone**, sends back a plain-English list of everything that feels
wrong, and each list becomes one numbered PR of fixes-plus-tests — round 3
(rename, marquee set, overlay pause, de-slop), rounds 4+4b (package locked,
batch roster, superstars, NRR table), round 5 (final bid ladder, 5-bidder
wars, ad-flow fix). Recurring feedback became codified rules
(`FOUNDER_TASTE.md`) so later rounds converge faster, and docs were
corrected after each round to keep describing reality. The founder makes the
final product calls — price (₹49 → ₹99 → ₹149), the bid ladder, the
permanent package name — and the build stamp in Settings exists so a stale
install can never confuse a feedback round.

### Phase 6 — Launch hardening (August)

Version 1.0.0, app icon and feature graphic, the static studio site with a
hosted privacy policy, business verification for Play (Udyam done; D-U-N-S
the long pole), a documented local signed-AAB build procedure, and
launch-gate tests that fail CI if a live ad id sneaks in before production.

### Phase 7 — Launch (September)

Google's rules for new apps moved while the app waited on D-U-N-S: target API
36, 16 KB-aligned native libraries and Play Billing 8+. That forced the Expo
57 / RN 0.86 / react-native-iap 16 upgrade. The organization account
(converted 2026-09-22) skipped the 12-testers / 14-days rule, so the path was
internal testing, then production. Before submitting, a read-only policy
audit became the `play-policy-guard` skill: a checker run before every upload,
plus a record of every Play Console answer. Build 5 (1.0.0) went to Google's
production review on 2026-09-25 with the ₹149 purchase live for India. The
first update, 1.0.1, removed the last hardcoded price: with no offer from
Play, the purchase is hidden.

### The recipe, distilled for the next app

1. Write the knowledge base first — vision, scope, domain, state machine,
   architecture, implementation plan, testing strategy — and record every
   hard-to-reverse decision as an ADR before coding.
2. Design as a standalone high-fidelity interactive prototype with an
   explicit handoff brief for Claude Code: *recreate, don't port*, and name
   which design decisions are intentional.
3. Build the pure engine (with its purity test and simulation harness)
   before any UI.
4. Execute the implementation plan as numbered components, each with tests.
5. Turn on CI before feature work scales; keep it the only merge gate.
6. Polish through numbered founder playtest rounds on a real device —
   plain-English feedback in, one PR of fixes-plus-tests out.
7. Codify taste into a portable document as patterns emerge; correct the
   docs after every round.
8. Drive launch from written checklists, with the risky gates (ad ids,
   package name) enforced by tests, not memory.

---

## 10. Known gotchas (learned the hard way)

- `pnpm` may not be on PATH in a fresh shell — `corepack pnpm …` always works.
- Changing the Android package name makes Android treat the app as brand new
  (old installs keep their data); changing the EAS slug breaks project
  linkage. Both are now frozen.
- The bid ladder changed twice before launch; the code in
  `packages/auction-engine/src/money/bid-increment.ts` is authoritative
  (**+₹10 L < ₹1 Cr, +₹20 L < ₹3 Cr, +₹50 L ≥ ₹3 Cr**) and root `CLAUDE.md`
  now matches it — but any doc quoting an increment is worth re-checking
  against that file, since stale copies of the older ladders circulated in
  earlier docs and briefs.
- The knowledge base's `com.abhisheksms.cyanauction` app id
  (`cricket-auction-knowledge-base/docs/06-application-architecture.md`) is
  superseded by `com.cyanharborstudios.cricketauction` (resolved 2026-08-04
  in root `CLAUDE.md`).
- jest-expo and RNTL versions track the Expo SDK — upgrade them together with
  the SDK, never independently.
- Reanimated 4 (SDK 57): don't list its Babel plugin — `babel-preset-expo`
  adds the react-native-worklets plugin itself, and listing it again applies
  it twice.
- Ad ids went **live** on 2026-09-23 (the org account skipped closed testing).
  Never tap your own live ads — that violates AdMob policy. Before AdMob has
  reviewed the app, even registered test devices get no fill, so exercise the
  reward flow with a throwaway sample-ads build (see the project skill's
  monetization reference).
- **Only a Play-installed copy can see purchases.** An APK installed by hand
  (adb or Drive) is signed with the upload key, not Play's key. Play still
  returns the product and price, but Restore finds nothing, even for the buyer.
  Test buying and restoring through internal testing.
