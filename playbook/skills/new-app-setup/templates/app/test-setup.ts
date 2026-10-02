/**
 * Global setup for the "ui" Jest project (see jest.config.cjs).
 *
 * Every native module the app touches is mocked here so ANY screen can be
 * rendered headlessly. Nothing here stubs app logic — the zustand store, the
 * auction engine, the repositories and the design system all run for real.
 *
 * To override a mock for one file, call `jest.mock(...)` at the top of that
 * test file; a test file's registration runs after this setup and wins.
 */

// Imported at module scope on purpose: RNTL registers its auto-cleanup hooks on
// first import, and a lazy require inside afterEach would do that mid-test
// ("Hooks cannot be defined inside tests") for any suite that never imports it.
import { cleanup } from "@testing-library/react-native";
import { resetSqliteDatabases } from "./mocks/sqliteMemory";
import { resetExpoRouterMock } from "./mocks/expoRouterMock";

// ---------------------------------------------------------------------------
// Animation
// ---------------------------------------------------------------------------
// Reanimated 4 moved its worklet runtime into react-native-worklets, whose
// native module cannot load under Jest; the package's own mock stands in. It
// must be registered before Reanimated's mock, which imports it.
jest.mock("react-native-worklets", () => require("react-native-worklets/src/mock"));

// Reanimated's official jest mock, plus the hooks it deliberately leaves out
// (its source marks them "ADD ME IF NEEDED").
jest.mock("react-native-reanimated", () => {
  const Reanimated = require("react-native-reanimated/mock");
  return {
    ...Reanimated,
    useReducedMotion: () => false,
    default: { ...Reanimated.default, call: () => {} },
  };
});

// ---------------------------------------------------------------------------
// Layout / chrome
// ---------------------------------------------------------------------------
// Official safe-area mock: insets resolve to 0 and SafeAreaProvider renders
// synchronously instead of waiting for a native measurement.
jest.mock("react-native-safe-area-context", () => require("react-native-safe-area-context/jest/mock").default);

jest.mock("react-native-svg", () => require("./mocks/svgMock").svgModuleMock());

jest.mock("expo-router", () => require("./mocks/expoRouterMock").expoRouterModuleMock());

// ---------------------------------------------------------------------------
// Storage
// ---------------------------------------------------------------------------
// In-memory SQLite so the REAL SqliteKeyValueStore + repositories run: saves,
// resumes and deletes round-trip exactly as they do on device.
jest.mock("expo-sqlite", () => require("./mocks/sqliteMemory").expoSqliteModuleMock());

// ---------------------------------------------------------------------------
// App metadata
// ---------------------------------------------------------------------------
// Mirrors app.json + app.config.ts so src/buildInfo.ts resolves a full stamp
// ("v0.1.0 · abc1234 · 1 Aug 2026") instead of degrading to "dev".
// Assert against TEST_BUILD_STAMP from test/factories.ts.
jest.mock("expo-constants", () => {
  const expoConfig = {
    name: "Going Once",
    slug: "going-once",
    version: "0.1.0",
    scheme: "cyanauction",
    orientation: "portrait",
    android: { package: "com.cyanharborstudios.cricketauction", versionCode: 1 },
    ios: { bundleIdentifier: "com.cyanharborstudios.cricketauction", buildNumber: "1" },
    extra: {
      eas: { projectId: "c68f4e16-1d9c-4c39-86fc-521228a1d9db" },
      router: { origin: false },
      buildInfo: { commit: "abc1234", buildDate: "2026-08-01" },
    },
  };
  const Constants = {
    expoConfig,
    expoGoConfig: null,
    manifest: expoConfig,
    manifest2: null,
    easConfig: null,
    appOwnership: null,
    executionEnvironment: "storeClient",
    expoVersion: "52.0.0",
    nativeAppVersion: "0.1.0",
    nativeBuildVersion: "1",
    installationId: "jest-installation",
    sessionId: "jest-session",
    deviceName: "jest",
    isDevice: false,
    debugMode: false,
    statusBarHeight: 0,
    systemFonts: [] as string[],
    platform: { android: { versionCode: 1 }, ios: { buildNumber: "1" } },
    getWebViewUserAgentAsync: async () => null,
  };
  return {
    __esModule: true,
    ...Constants,
    default: Constants,
    ExecutionEnvironment: { Bare: "bare", Standalone: "standalone", StoreClient: "storeClient" },
    AppOwnership: { Expo: "expo", Standalone: "standalone", Guest: "guest" },
  };
});

// ---------------------------------------------------------------------------
// Monetization natives
// ---------------------------------------------------------------------------
// Both adapters feature-detect their native module and fall back when it is
// absent (rewardedAdMob.ts / playBilling.ts). Empty modules reproduce the
// Expo Go / web-stub environment, so tests get the deterministic paths:
// DevBillingGateway (purchase always succeeds) and the 5s rewarded-ad overlay
// instead of a real AdMob presentation.
//
// A test that wants the linked-native path instead can override per file, e.g.
//   jest.mock("react-native-google-mobile-ads", () => ({ RewardedAd: { ... } }));
jest.mock("react-native-google-mobile-ads", () => ({ __esModule: true }));
jest.mock("react-native-iap", () => ({ __esModule: true }));

// ---------------------------------------------------------------------------
// Per-test isolation
// ---------------------------------------------------------------------------
afterEach(() => {
  // Unmount first: this hook is registered before RNTL's own auto-cleanup, so
  // without it the store reset below would re-render a live tree outside act().
  cleanup();
  // Reset the zustand singleton to its factory state. Any beat/ad timer still
  // pending is harmless afterwards: the store's callbacks bail out when
  // `auction` is null.
  //
  // Required lazily, not imported at module scope. The store builds its billing
  // gateway and ad controller at import time, so importing it here would pin
  // the real ones in place before a test file's own `jest.mock(...)` has had a
  // chance to register — and no test could ever reach a "purchase failed" or
  // "Play says not-owned" path. Requiring inside the hook lets the test file
  // load the store first, mocks and all, and returns that same instance.
  const { useGame } = require("../src/game/store") as typeof import("../src/game/store");
  useGame.setState(useGame.getInitialState(), true);
  resetSqliteDatabases();
  resetExpoRouterMock();
});
