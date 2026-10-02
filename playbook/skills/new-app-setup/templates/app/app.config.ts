import { execSync } from "node:child_process";
import type { ConfigContext, ExpoConfig } from "expo/config";

/**
 * Dynamic Expo config. Every static key still lives in app.json — this file
 * only spreads that config and injects the build identity, which app.json
 * cannot do because it is static JSON and cannot read env vars.
 *
 * Why this exists: an installed APK is otherwise anonymous, so there is no way
 * to tell a fresh build from a stale one on the device. Settings now shows
 * "v0.1.0 · aba2b55 · 1 Aug 2026" from the values injected here.
 */

/** Short commit for the build. EAS sets EAS_BUILD_GIT_COMMIT_HASH; CI sets
 * GITHUB_SHA; locally we ask git. Unknown (e.g. a tarball with no .git) → "dev". */
function resolveCommit(): string {
  const fromEnv = process.env.EAS_BUILD_GIT_COMMIT_HASH || process.env.GITHUB_SHA;
  if (fromEnv && fromEnv.trim()) return fromEnv.trim().slice(0, 7);
  try {
    const fromGit = execSync("git rev-parse --short=7 HEAD", {
      cwd: __dirname,
      stdio: ["ignore", "pipe", "ignore"],
      encoding: "utf8",
    }).trim();
    if (fromGit) return fromGit;
  } catch {
    // no git, or not a repo — fall through
  }
  return "dev";
}

export default ({ config }: ConfigContext): ExpoConfig => ({
  ...(config as ExpoConfig),
  extra: {
    ...config.extra,
    buildInfo: {
      commit: resolveCommit(),
      // Config is evaluated during the build, so "now" is the build date.
      buildDate: new Date().toISOString().slice(0, 10),
    },
  },
});
