// Example (from turqoise's spike): an Expo config plugin for manifest edits the
// library plugins don't make themselves. The pattern is what to copy: never hand-edit
// android/ (it is generated); declare the change here and reference the plugin in app.json.
// Spike-only manifest surgery the library plugins don't do themselves:
// 1. notify-kit's foreground service needs an explicit Android 14+ service
//    type (a typeless FGS start is rejected on API 34+).
// 2. SCHEDULE_EXACT_ALARM is capped at API 32 — on 33+ we rely on
//    USE_EXACT_ALARM, which Play sanctions for timer apps.
const { withAndroidManifest, AndroidConfig } = require("expo/config-plugins");

const FGS_NAME = "app.notifee.core.ForegroundService";

module.exports = function withSpikeAndroidManifest(config) {
  return withAndroidManifest(config, (config) => {
    const manifest = config.modResults;
    const app = AndroidConfig.Manifest.getMainApplicationOrThrow(manifest);

    app.service = app.service ?? [];
    const existing = app.service.find((s) => s.$["android:name"] === FGS_NAME);
    const attrs = {
      "android:name": FGS_NAME,
      "android:foregroundServiceType": "mediaPlayback",
    };
    if (existing) Object.assign(existing.$, attrs);
    else app.service.push({ $: attrs });

    const perms = manifest.manifest["uses-permission"] ?? [];
    const schedule = perms.find(
      (p) => p.$["android:name"] === "android.permission.SCHEDULE_EXACT_ALARM"
    );
    if (schedule) schedule.$["android:maxSdkVersion"] = "32";

    return config;
  });
};
