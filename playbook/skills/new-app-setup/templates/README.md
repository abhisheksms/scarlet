# Templates: what to copy and what to change

Copied from the cyan repo on 2 October 2026 (Expo SDK 57, React Native 0.86).
Check the versions against Expo's current SDK before installing.

| Template | Goes to | Change |
|---|---|---|
| `CLAUDE.md` | repo root | the `<…>` placeholders; keep the rules |
| `AGENTS.md` | repo root | the product-rules section; keep the rest |
| `claude-settings.json` | `.claude/settings.json` | nothing (enables this plugin for every session in the repo) |
| `github-workflows/ci.yml` | `.github/workflows/ci.yml` | the app's package filter (`@<scope>/mobile`); Node 22 (GitHub is retiring Node 20 runners) |
| `package.json` | repo root | `name`, `description`; drop the `pnpm.overrides` that no longer apply |
| `pnpm-workspace.yaml`, `npmrc` → `.npmrc`, `tsconfig.base.json`, `gitignore` → `.gitignore` | repo root | nothing |
| `app/package.json` | `apps/mobile/package.json` | `name` (`@<scope>/mobile`), the `@cyan/*` workspace deps, drop unused native modules |
| `app/app.json` | `apps/mobile/app.json` | `name`, `slug`, `scheme`, `package`, `versionCode` 1, the AdMob app id (Google's test id until launch), `extraProguardRules` only if the same native modules ship, `owner` stays `cyan-harbor` |
| `app/app.config.ts` | `apps/mobile/app.config.ts` | nothing (build stamp) |
| `app/metro.config.js`, `app/babel.config.js`, `app/tsconfig.json`, `app/jest.config.cjs` | `apps/mobile/` | `NATIVE_ONLY_ON_WEB` list; the storage/ui project split if the folders differ |
| `app/withAndroidManifest.example.js` | `apps/mobile/plugins/` | only if the app needs manifest edits no library plugin makes (service types, permission caps); the pattern, not the content |
| `app/test-setup.ts` | `apps/mobile/test/setup.ts` | the `expo-constants` mock (name, slug, package); drop mocks for modules the app doesn't use |
| `app/launch-gate.test.ts` | `apps/mobile/test/launch-gate.test.ts` | `CHOSEN_ANDROID_PACKAGE`, `LIVE_ANDROID_APP_ID`; until the ids go live, assert the test ids instead |
| `package/jest.config.cjs`, `package/tsconfig.json` | `packages/<engine>/` | nothing |
| `package/purity.test.ts` | `packages/<engine>/src/purity.test.ts` | nothing; it greps the engine for clock, random, IO and React |
| `knowledge-base/` | `<app>-knowledge-base/` | everything product-specific; keep the structure, the rules and the ADR format. ADR-001 (stack inherited) is re-dated; ADR-002/003/005/007/008 from cyan are re-accepted or superseded |
| the `project-method` skill's templates | `PLAN.md`, `PRODUCT.md`, `design/CLAUDE_DESIGN_BRIEF.md`, later `design/design_handoff_<app>/CLAUDE_CODE_PROMPT.md` | everything |
