/**
 * Two Jest projects run from a single `pnpm --filter @cyan/mobile test`:
 *
 *   storage — pure storage-layer tests (no React Native / native modules). The
 *             repositories take a KeyValueStore, so they run against
 *             MemoryKeyValueStore under plain ts-jest in Node. UNCHANGED.
 *   ui      — React Native screen/component tests under jest-expo (SDK 57),
 *             rendered headlessly with @testing-library/react-native. Native
 *             modules are mocked in test/setup.ts.
 *
 * Location routes a file to a project: anything under src/storage runs in
 * "storage", everything else in "ui". No test file can fall through the gap.
 * Write UI tests as `.tsx` (they need JSX).
 *
 * @type {import('jest').Config}
 */
module.exports = {
  projects: [
    {
      displayName: "storage",
      rootDir: __dirname,
      preset: "ts-jest",
      testEnvironment: "node",
      roots: ["<rootDir>/src/storage"],
      testMatch: ["**/*.test.ts"],
      clearMocks: true,
    },
    {
      displayName: "ui",
      rootDir: __dirname,
      preset: "jest-expo",
      roots: ["<rootDir>/src", "<rootDir>/app", "<rootDir>/test"],
      testMatch: ["**/*.test.ts", "**/*.test.tsx"],
      // src/storage belongs to the "storage" project above.
      testPathIgnorePatterns: ["/node_modules/", "<rootDir>/src/storage/"],
      clearMocks: true,
      // Mirrors the `@/*` -> `./src/*` alias from tsconfig.json.
      moduleNameMapper: {
        "^@/(.*)$": "<rootDir>/src/$1",
      },
      setupFilesAfterEnv: ["<rootDir>/test/setup.ts"],
    },
  ],
};
