---
name: android-release
description: How Cyan Harbor builds, checks and ships an Android release of an Expo / React Native app - versionCode bumps, expo prebuild, the local signed Gradle build with the upload keystore, verifying the AAB and APK (target API, 16 KB alignment, signer, ABIs, R8 map), the emulator pass by accessibility label, store screenshots at Play's 2:1 limit, getting a build onto the founder's phone, Play internal testing and promotion to production, and what only the founder can do. Use it for any "build", "release", "ship", "upload to Play", "new build for my phone", "screenshot the app", "run it on the emulator" or "versionCode" request, and after any dependency or Expo SDK upgrade.
---

# Android release

The order is: merge → bump → build → check the artefact → see it on a device →
internal testing → promote. The commands and the gotchas learned on the first
app are in `references/build-release-ops.md`; the helper scripts are in
`scripts/`. Everything below is per app: `<app>` is the app's short name,
`<package>` its `android.package`.

## 1. Before building
- [ ] The change is merged to `main` and CI is green.
- [ ] `android.versionCode` in `apps/mobile/app.json` is higher than every build
      Play has seen. Play rejects a versionCode it has already accepted. Bump
      `version` too when users should see a new version name.
- [ ] If the change touches ads, the purchase or its wording, permissions,
      `app.json`'s android config, SDKs, data collection, the privacy page or
      the listing: the `play-policy-guard` checker reports 0 errors.

## 2. Build (on the founder's Mac)
- [ ] `npx expo prebuild --platform android --no-install` if `app.json` or a
      plugin changed. `--clean` only after package-name or plugin removals.
- [ ] Signed Gradle build from `apps/mobile/android`:

```bash
PW=$(cat ~/CyanHarbor/keystore/<app>-upload-keystore-password.txt) && \
JAVA_HOME=$(/usr/libexec/java_home -v17) ./gradlew :app:bundleRelease :app:assembleRelease \
  -Pandroid.injected.signing.store.file="$HOME/CyanHarbor/keystore/<app>-upload.jks" \
  -Pandroid.injected.signing.store.password="$PW" \
  -Pandroid.injected.signing.key.alias=<app>-upload \
  -Pandroid.injected.signing.key.password="$PW"
```

Nothing secret is written into a tracked file, and a `prebuild` can't clobber
the signing config. Never print or echo the password. A full build takes 10–30
minutes; run it in the background and read only the exit status.

**The upload keystore** lives outside the repo at `~/CyanHarbor/keystore/`, one
per app, so `expo prebuild --clean` can't destroy it. Generate a new one for a
new app (`keytool -genkeypair -v -keystore <app>-upload.jks -alias <app>-upload
-keyalg RSA -keysize 2048 -validity 10000`), write the password to a file next to
it, and back the folder up off the laptop. Play App Signing holds the real
signing key; a lost upload key can only be replaced through a reset request to
Google.

## 3. Check the build, not the config
- [ ] `aapt2 dump badging`: the new versionCode and the required targetSdk.
- [ ] `keytool -printcert -jarfile`: the app's upload-key SHA-1 on the AAB and the APK.
- [ ] `scripts/check-aab.py`: every arm64 library 16 KB-aligned (exit 0).
- [ ] ABIs are exactly `arm64-v8a` and `armeabi-v7a`.
- [ ] Policy checker with `--apk … --aab … --online --gradle`: 0 errors.
- [ ] On the emulator (`budget_phone`), every screen the change touches, also at
      360dp with 115% text. Jest can't see layout. Drive it with
      `scripts/adb_drive.py` (dump, find, tap by label, shot).
- [ ] Copy the AAB, APK and R8 map to `~/CyanHarbor/builds/<date>-build<N>/`.
      Move older builds to `superseded/`, so an old AAB can't be uploaded by
      mistake.
- [ ] Copy both to the founder's Drive (`My Drive/<App> builds/`, through Drive
      for desktop) and check each copy with `cmp`. Stage the AAB in
      `~/CyanHarbor/play-upload/` for the upload.

## 4. Internal testing
- [ ] Play Console → Test and release → Internal testing → Create new release →
      upload the AAB → release notes → Next → Save and publish. Internal
      testing has no review; the update can take 10–30 minutes to reach the
      phone.
- [ ] Install it on the founder's phone from Play, not by hand. Billing only
      works on a copy installed from Play. A hand-installed copy must be
      uninstalled first, because Play signs with a different key.
- [ ] On that copy: every purchase's price shows, Restore finds the test purchase,
      and saved state survives the update.

## 5. Production
- [ ] Promote the same release from internal testing to production. Don't upload
      a new AAB. Add "What's new" notes. Google reviews updates too; with managed
      publishing off, it goes live as soon as it's approved.

## Definition of done
A change is complete only when it works in a **release build on a physical
Android device**. Steps 3 and 4 are that gate.

## Never
- Tap a real ad on a real device. That is invalid traffic on the studio's own
  AdMob account. To test the reward flow, use a throwaway sample-ads build
  (Google's test unit id, output named `TEST-ONLY-…`).
- Commit the keystore or print its password.
- Ship a build with Google's test ad ids. Keep a launch-gate test that pins the
  live ids and fails CI if they drift.
- Drive Play Console, AdMob, Cloudflare, Google payments or banking yourself.
  Give numbered steps and read the founder's screenshots.
