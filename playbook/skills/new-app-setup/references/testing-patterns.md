# Testing and CI

> Shared across Cyan Harbor apps (master copy in the playbook). Written on
> Cricket Auction Simulator; its names, numbers and paths are the worked example.

## Commands

Run these from the repo root. If `pnpm` isn't found, use `corepack pnpm`.

```bash
pnpm -r typecheck                                   # strict TS incl. noUnusedLocals/Parameters
pnpm -r --filter "./packages/**" test -- --ci       # engine, content, presentation, testkit (ts-jest)
pnpm --filter @cyan/mobile test -- --ci             # both mobile projects
cd apps/mobile && npx jest src/game/store.test.ts -t "never shows a blank price"   # one test
```

**CI** (`.github/workflows/ci.yml`) is a single job, "Typecheck & test", on
Ubuntu with Node 20 and pnpm 9.15.0. It runs `install --frozen-lockfile`,
then typecheck, then the package tests, then the mobile tests, on pushes to
`main` and on every PR. A green run is the merge gate.

Watch CI by exit code, per the founder's instruction not to read logs:

```bash
gh pr checks <N> --watch > /dev/null 2>&1; echo CI_EXIT=$?
```

Create the PR first and let the checks register before you watch; watching
too early races the runner. Only if it fails, fetch the *minimum* failing
output.

## The mobile Jest setup (`apps/mobile/jest.config.cjs`)

There are two projects, routed by file location:
- **storage**: ts-jest in Node over `src/storage/**/*.test.ts`. The
  repositories run against `MemoryKeyValueStore`.
- **ui**: jest-expo with `@testing-library/react-native` 13 over everything
  else (`src`, `app`, `test`). The `@/` alias points at `src/`. Write UI
  tests as `.tsx`.

`test/setup.ts` mocks **only native modules**, so the real store,
repositories and engine all run:
- `react-native-worklets`, registered before Reanimated because Reanimated's
  mock imports it
- `react-native-reanimated`, with the hooks its official mock leaves out
- `react-native-safe-area-context` (the official mock, with zero insets)
- `react-native-svg`
- `expo-router`
- `expo-sqlite` (an in-memory `test/mocks/sqliteMemory.ts`)
- `expo-constants`
- `react-native-google-mobile-ads`
- `react-native-iap`

Because the iap module is an empty mock, the store runs `DevBillingGateway`
(₹149, every purchase succeeds).

A global `afterEach` resets the store to its initial state. Jest also runs
with `clearMocks: true`.

## Helpers

- `test/renderScreen.tsx` provides `renderScreen(<Screen/>, { state })`. It
  seeds the zustand store, then renders inside `TestProviders`. It also
  exports `seedStore`, `resetStore`, `storeState()`, `useGame`, and the
  `GameStore` type.
- `test/factories.ts` provides `makeAuction(opts)`, `makeBiddingAuction`,
  `makeCompletedAuction`, `beatUntil(state, predicate)`, `TEST_TEAM_ID`
  (`monsoon`), `TEST_BUILD_STAMP` and `emptySlotSummaries`. Always build
  state through the real engine, never with hand-written fixtures.
- `test/pressable.ts` exists because RN 0.86 exports `Pressable` as
  `memo(Pressable)`. `UNSAFE_*ByType(Pressable)` would find nothing and let
  the accessibility sweep pass while checking nothing, so this helper unwraps
  it.
- `test/mocks/backHandlerMock.ts` keeps the BackHandler contract, since
  RN 0.86 dropped its own mock.
- In store tests, a `flush()` helper (`setImmediate`) drains the
  `void repo.save(...)` writes before you read results back.

## Rules

- **Every test must be able to fail.** No `toBeTruthy()` on things that
  always exist, and no snapshot dumps. After writing an important test,
  mutation-check it: revert the fix, see it fail, then restore.
- Assert what a player sees: roles, names and visible text. Don't assert on
  style objects or component names.
- Don't pin copy the founder may change. Use a `testID` or assert behaviour
  instead. The one deliberate copy guard is the dark-pattern regex, plus the
  "no banner/interstitial claims" check on the buy sheet.
- **RNTL v13:** use `toBeSelected()`, `toBeDisabled()`, `toBeEnabled()`,
  `toBeOnTheScreen()`, and `getByRole("button", { name })`. The removed
  `toHaveAccessibilityState` doesn't exist.
- **A disabled `Button` isn't found by `getByRole`.** `design/ui.tsx` renders
  a disabled Button as a plain labelled `View` (not `accessible`), so query it
  with `getByLabelText("Buy")` and then assert `toBeDisabled()`. The 1.0.1
  no-offer tests hit this.
- **Timers:** use `jest.useFakeTimers()` with `act(() => jest.advanceTimersByTime(ms))`
  for reveal auto-open, beats and ad timeouts. Restore real timers in
  `afterEach`.
- **Virtualized lists** (`FlatList`) render only about 10 rows in tests. To
  mount everything, fire `layout`, `contentSizeChange` and `scroll` with a
  very tall viewport, then flush the timers. See `renderBoard()` in
  `src/screens/watchlist.test.tsx`. Keep one test that asserts the first
  render is bounded, because that guards the performance fix.
- **The real ad path:** `src/screens/monetization.realAd.test.tsx` mocks
  `@/ads/factory` with a controller whose `present()` promise you resolve by
  hand (`"earned" | "closed" | "unavailable"`). Use it to test loading and
  no-ad states and the room pause.

## Guard tests worth knowing

| Test | Guards |
|---|---|
| `auction-engine/src/purity.test.ts` | no clock, random, IO or React in the engine |
| `auction-engine/src/persistence/compatibility.test.ts` | the frozen v1 save still loads |
| `presentation/src/boundary.test.ts` | no React Native in presentation |
| `apps/mobile/test/launch-gate.test.ts` | the package name and live ad ids; only our publisher or Google's test publisher appear in src |
| `apps/mobile/test/dependency-shape.test.ts` | shipped overridden deps are callable from CJS (query-string, decode-uri-component, nanoid/non-secure) |
| `apps/mobile/src/screens/accessibility.test.tsx` | every Pressable has a role and label |
| `monetization.test.tsx` (DARK_PATTERN regex) | no urgency or discount copy on the offer |
| `billing/playBilling.test.ts` | the ₹149 path against the real adapter (the iap module is otherwise stubbed globally) |
| `ads/rewardedAdMob.test.ts` | consent before init; present() outcomes, timeout and the late-load guard |

## `packages/game-testkit`: whole-auction simulations

- `runAuction(seed, difficulty)` and `simulate(runs, difficulty)` run
  AI-only auctions. `priceBand`,
  `estimateSessionMinutes` and `meanSessionMinutes` feed balance checks.
- Its tests guard:
  - session length (about 112 minutes at both difficulties)
  - contested wars (Hard contests more)
  - spending (rivals near-max their purses)
  - squad composition
  - purse integrity at every intermediate state
- Run it after any AI, timing or content change:
  `pnpm --filter @cyan/game-testkit test`.
