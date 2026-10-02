import { readFileSync } from "node:fs";
import { join } from "node:path";
import { PUBLISHER_ID, REWARDED_AD_UNIT_ID, TEST_REWARDED_AD_UNIT_ID, usesLiveAdUnit } from "../src/ads/adIds";

/**
 * Facts that are cheap to change by accident and expensive to get wrong, and
 * that neither a screenshot nor a type check would catch:
 *
 *   android.package  The first Play upload locks it permanently. The founder
 *                    chose com.cyanharborstudios.cricketauction (2026-08-04);
 *                    it must not drift again.
 *   AdMob ids        Went LIVE 2026-09-23. They must be Cyan Harbor's own and
 *                    must agree with each other — an app id and a unit id from
 *                    different publishers is the classic silent failure, where
 *                    the SDK starts fine and then simply never fills.
 *
 * Until 2026-09-23 this file asserted the opposite: that only Google TEST ids
 * shipped. Flipping it was the deliberate decision to serve live ads, which is
 * exactly the moment this guard exists to make somebody stop and think.
 */

const APP_JSON = JSON.parse(readFileSync(join(__dirname, "..", "app.json"), "utf8")) as {
  expo: {
    android: { package: string };
    plugins: (string | [string, Record<string, unknown>])[];
  };
};

/** Founder's decision, 2026-08-04 — studio + game (see CLAUDE.md, "Launch gate"). */
const CHOSEN_ANDROID_PACKAGE = "com.cyanharborstudios.cricketauction";

/** Cyan Harbor's AdMob app id for this Android app. Public, not a secret. */
const LIVE_ANDROID_APP_ID = "ca-app-pub-8927071309083589~5965655388";

/** Google's published sample publisher id. Every AdMob test unit sits under it. */
const GOOGLE_TEST_PUBLISHER = "ca-app-pub-3940256099942544";

function pluginConfig(name: string): Record<string, unknown> | undefined {
  const entry = APP_JSON.expo.plugins.find((p) => (Array.isArray(p) ? p[0] === name : p === name));
  return Array.isArray(entry) ? entry[1] : undefined;
}

describe("launch gate", () => {
  it("keeps the chosen android.package", () => {
    // The first Play upload makes this permanent — changing it later means a
    // new listing and losing every install.
    expect(APP_JSON.expo.android.package).toBe(CHOSEN_ANDROID_PACKAGE);
  });

  it("ships Cyan Harbor's own AdMob app id", () => {
    const ads = pluginConfig("react-native-google-mobile-ads");
    // Guards the guard: a renamed plugin key would otherwise leave this
    // assertion checking nothing at all.
    expect(ads).toBeDefined();
    expect(ads?.androidAppId).toBe(LIVE_ANDROID_APP_ID);
  });

  it("serves a live rewarded unit, not the SDK's test fallback", () => {
    expect(usesLiveAdUnit()).toBe(true);
    expect(REWARDED_AD_UNIT_ID).not.toBe(TEST_REWARDED_AD_UNIT_ID);
  });

  it("points the app id and the rewarded unit at the same publisher", () => {
    // Mismatched publishers fail silently: the SDK initializes, requests an ad,
    // and never fills. Nothing in the app surfaces that as an error.
    expect(LIVE_ANDROID_APP_ID.startsWith(`${PUBLISHER_ID}~`)).toBe(true);
    expect(REWARDED_AD_UNIT_ID?.startsWith(`${PUBLISHER_ID}/`)).toBe(true);
  });

  it("hardcodes no AdMob id belonging to anyone else", () => {
    // Only two publishers may ever appear in source: ours, and Google's sample
    // publisher (the documented revert target). Anything else is a copy-paste
    // from a tutorial, and would pay a stranger.
    const sources = sourceFiles(join(__dirname, "..", "src"));
    expect(sources.length).toBeGreaterThan(20);

    const allowed = [PUBLISHER_ID, GOOGLE_TEST_PUBLISHER];
    const offenders = sources.filter((file) => {
      const matches = readFileSync(file, "utf8").match(/ca-app-pub-\d+/g) ?? [];
      return matches.some((id) => !allowed.includes(id));
    });
    expect(offenders).toEqual([]);
  });
});

function sourceFiles(dir: string): string[] {
  const { readdirSync, statSync } = require("node:fs") as typeof import("node:fs");
  return readdirSync(dir).flatMap((entry: string) => {
    const path = join(dir, entry);
    if (statSync(path).isDirectory()) return sourceFiles(path);
    return /\.tsx?$/.test(path) && !/\.test\.tsx?$/.test(path) ? [path] : [];
  });
}
