# Build, release and operations

> Shared across Cyan Harbor apps (master copy in the playbook). Learned on
> Cricket Auction Simulator, whose package, build numbers, sizes and SHA-1 are
> the worked example; swap in the app's own. The helper scripts named below
> live in this skill's `scripts/` folder.

Contents: native config · version bumps · local signed build · verifying the
AAB/APK · where builds live · getting a build to the founder · emulator
verification · store screenshots · sample-ads build · launch runbooks (Play,
AdMob, Cloudflare) · what's founder-only

## Native config lives in `app.json`, not `android/`

`apps/mobile/android/` is **gitignored and generated** by `expo prebuild`.
Hand edits there are lost. Change native behaviour through `app.json`:
- `plugins`: expo-router, react-native-google-mobile-ads (`androidAppId`),
  expo-sqlite, expo-status-bar, and `expo-build-properties` with
  `android.buildArchs: ["armeabi-v7a", "arm64-v8a"]`. The x86 emulator ABIs
  were dropped because they doubled build time. Apple Silicon emulators are
  arm64.
- **R8 is on from build 7.** `expo-build-properties` also sets
  `enableMinifyInReleaseBuilds`, `enableShrinkResourcesInReleaseBuilds` and
  `extraProguardRules`, which keep `com.margelo.nitro.**` and
  `dev.hyo.openiap.**` whole. The billing bridge (Nitro and OpenIAP) looks up
  its Kotlin classes from C++ by name. A renamed class would break purchases
  only at runtime, on a real device, which no build step would catch. The
  libraries ship their own consumer rules; the explicit keeps are a second
  line of defence. R8 cut the dex from 52.9 to 21.4 MB, the AAB from 55.6 to
  52.8 MB and the APK from 75.7 to 65.3 MB.
- `android.package`: `com.cyanharborstudios.cricketauction`. This is
  permanent.
- `android.versionCode` and `version`: see the next section.
- `newArchEnabled: true`, and orientation portrait.
- `android.blockedPermissions` strips what Expo's prebuild template adds but the
  game never uses: SYSTEM_ALERT_WINDOW, READ/WRITE_EXTERNAL_STORAGE and VIBRATE.
  Play's permissions policy wants only what shipped features need. Verify with
  `aapt2 dump permissions` on every build.

The Play requirements that forced the September 2026 SDK upgrade:
- **targetSdk 36**
- **16 KB page-aligned 64-bit native libraries**
- **Play Billing Library 8+**

All three are met by Expo 57 / RN 0.86 / react-native-iap 16 and are checked
on every build (see below).

## Version bumps

Every Play upload needs a higher `android.versionCode` in `app.json`. It is
7 for update 1.0.1: build 6 was 1.0.1 without R8, and build 7 is the same
code with R8. Build 5 (2026-09-24: the redrawn icon plus the
policy-audit fixes) was the launch build, submitted to production on
2026-09-25. Build 4, icon only, was never distributed. Bump it for each build that might go to Play,
then prebuild. `version` (1.0.1 now) is the user-facing name; bump it for real
releases, for example 1.0.1 for the founder's post-launch tweaks. The build
stamp in Settings shows `v{version} · {build date}`.

## Local signed build (how releases are made)

`CLAUDE.md` mentions EAS (profiles: development, preview, production), but
releases since September 2026 are built **locally**. From `apps/mobile`:

```bash
npx expo prebuild --platform android --no-install    # after any app.json/plugin change (non-clean keeps build caches)
# --clean only after package-name or plugin removals; it wipes android/ and forces a full native rebuild

cd android && PW=$(cat ~/CyanHarbor/keystore/upload-keystore-password.txt) && \
JAVA_HOME=$(/usr/libexec/java_home -v17) ./gradlew :app:bundleRelease :app:assembleRelease \
  -Pandroid.injected.signing.store.file="$HOME/CyanHarbor/keystore/cyanharbor-upload.jks" \
  -Pandroid.injected.signing.store.password="$PW" \
  -Pandroid.injected.signing.key.alias=cyanharbor-upload \
  -Pandroid.injected.signing.key.password="$PW"
```

- **Never print or echo the password.** It stays in the file.
- A full build takes about 10–30 minutes. It took 28 when the emulator was
  running at the same time, so don't overlap them. A JS-only change rebuilds
  `assembleRelease` in under a minute.
- Run long builds in the background and read only the exit status or the
  `BUILD SUCCESSFUL` / `FAILED` line.
- Outputs:
  - `android/app/build/outputs/bundle/release/app-release.aab` (for Play,
    about 53 MB)
  - `…/apk/release/app-release.apk` (for sideloading, about 65 MB)
  - `…/mapping/release/mapping.txt` (R8's rename map, about 75 MB). It
    turns an obfuscated stack trace back into real names.

## Verify every build before calling it done

```bash
OUT=apps/mobile/android/app/build/outputs
BT=$(ls -d /opt/homebrew/share/android-commandlinetools/build-tools/* | tail -1)
$BT/aapt2 dump badging $OUT/apk/release/app-release.apk | grep -E "^package:|targetSdkVersion"   # versionCode, targetSdk 36
keytool -printcert -jarfile $OUT/bundle/release/app-release.aab | grep SHA1   # upload key: CD:7B:6A:35:C7:07:CD:E1:44:10:B2:6F:D8:B6:2C:23:CC:FB:27:AD
T=$(mktemp -d) && unzip -q $OUT/bundle/release/app-release.aab 'base/lib/*' -d $T && ls $T/base/lib \
  && python3 <skill>/scripts/check-aab.py "$T/base/lib/arm64-v8a/*.so"          # expect {16384: N}, exit 0
unzip -l $OUT/bundle/release/app-release.aab | grep obfuscation/proguard.map  # R8's map rides inside the AAB
```

Because the map is inside the AAB, Play decodes crash reports by itself, and
the Play Console warning "no deobfuscation file" doesn't apply. For crashes
from a sideloaded APK, keep the map next to the build (see below).

`scripts/check-aab.py` (this skill) checks ELF PT_LOAD alignment. Exit codes: 0 = pass,
1 = a rejected library, 2 = nothing matched or nothing 64-bit. The ABI list
should be exactly `arm64-v8a` and `armeabi-v7a`.

After a merge, confirm the build's source equals `main`:
`git diff --quiet <built-commit> HEAD -- apps/ packages/`.

## Where builds live

On the founder's Mac:
- Current builds: `~/CyanHarbor/builds/<date>-build<N>/cricket-auction-v<version>-build<N>.{aab,apk}`,
  plus `…-build<N>-r8-mapping.txt` from build 7 on. Each build's map only
  fits that build.
- Throwaway test APKs: `TEST-ONLY-*.apk`.
- Anything superseded moves to `~/CyanHarbor/builds/superseded/`, so an old
  AAB can't be uploaded by mistake. Move, don't delete.
- Keystore: `~/CyanHarbor/keystore/`. Guard it; never commit it.

## Getting a build to the founder

The APK (about 72 MB) is over `SendUserFile`'s 30 MB limit. Options:
- **USB (since 2026-09-25):** the founder's phone (ColorOS) is on USB
  debugging. `adb install --user 0 -r <apk>`: the phone also has a work
  profile (user 10), and the install must never touch it. ColorOS shows a
  "Continue installation" screen that the founder taps; a slow install there
  is not a failure. Use this for UI checks, never for billing.
- **Google Drive:** Drive for desktop syncs
  `~/Library/CloudStorage/GoogleDrive-<founder's Gmail>/My Drive/Cricket Auction builds/`.
  Copy each build's APK and AAB there and check the copies with `cmp`. On the
  phone: Drive → ⋮ → Download → Install, allowing the source when asked.
- **Play internal testing:** set up since 2026-09-25. It's the proper
  channel for every update, and the only way to test billing.

Installing a sideloaded upload-key build over a Play-installed one, or the
reverse, fails because of signing. Uninstall first; that loses local saves.

## Emulator verification (do this before handing over any UI change)

- SDK: `ANDROID_HOME=/opt/homebrew/share/android-commandlinetools`. The
  emulator and the API 36 Google Play arm64 image were installed 2026-09-23.
- AVD `budget_phone`: Pixel 4a profile, 1080×2340 at 440 dpi, 2 GB RAM.

```bash
$ANDROID_HOME/emulator/emulator -avd budget_phone -no-window -no-audio -no-boot-anim \
  -gpu swiftshader_indirect -no-snapshot            # run in background
adb wait-for-device; adb shell 'while [ "$(getprop sys.boot_completed)" != "1" ]; do sleep 2; done'
adb shell 'wm size reset; wm density reset; settings put system font_scale 1.0'   # settings persist between boots
adb install -r <apk>
adb shell am start -W -n com.cyanharborstudios.cricketauction/.MainActivity
python3 <skill>/scripts/adb_drive.py dump            # list labels + bounds
python3 <skill>/scripts/adb_drive.py tap "New Game" exact
python3 <skill>/scripts/adb_drive.py shot teamselect 1000   # downsized screenshot to Read
adb shell 'wm size 720x1520; wm density 320; settings put system font_scale 1.15'  # budget phone: 360dp, 115% text
adb emu kill                                         # when done: frees the laptop
```

Gotchas:
- **`uiautomator dump` fails on the reveal screen.** Its 7-second progress bar
  never goes idle. Screenshot it instead, or detect it from a raw
  `adb exec-out screencap` pixel: the purple progress bar at the left edge.
  Tap by coordinates, and do it fast, before bidding auto-opens.
- Dumps taken mid-animation can come back partial. Treat a very short dump as
  "unknown", not as a real state.
- Don't run the emulator during a Gradle build. `system_server` restarted
  under the load, and an install silently failed.
- **A blank screen right after a build can be the Mac, not the app.** Build
  7's first launch took 14 s. The auction then drew nothing for minutes,
  though `dump` listed its text, with 159 MB free in the 2 GB AVD and a
  Gradle daemon still running. After `./gradlew --stop`, a force-stop and a
  relaunch (2 s), the same build drew every screen. Clear the load before
  blaming a build.
- Compare frame coordinates in screenshots with the density in mind. Use
  `dump` bounds to prove overlaps: build 2's "‹ Back" at y 175–224 sat inside
  the title at y 174–300.
- The live ad unit returns no-fill (code 3) on the emulator. Use the
  sample-ads build to test ads.
- Play Billing on the emulator has no Google account, so Buy correctly shows
  "Purchase didn't go through".
- **The room keeps running while you read a screenshot.** The reveal
  auto-opens after 7 s, and Sold holds for 2 s.
  - To pause, tap the reveal's batch label (about (950, 205) at 1080×2340).
    The batch roster opens, the auction pauses, and `dump` works while the
    roster is up.
  - To catch the next reveal or a Sold screen, chain `input tap`, `sleep` and
    `screencap` in one command. Polling `dump` is too slow.
- **Claude Code's permission classifier blocks the signed build command** as
  a production deploy, because it reads the upload-keystore password.
  - For a UI check, use the newest APK in `~/CyanHarbor/builds` if it
    already contains the change. Prove the emulator runs it by comparing
    `shasum` of that file and the installed `base.apk` (path from
    `adb shell pm path`).
  - Otherwise, build a throwaway test APK in a worktree. Run
    `npx expo prebuild --platform android --no-install`, then
    `./gradlew :app:assembleRelease -PreactNativeArchitectures=arm64-v8a`
    with no signing flags. That gives a debug-signed arm64 APK in about
    15 minutes from cold.
  - Because it's debug-signed, uninstall the upload-signed app on the
    emulator first, and reinstall the real build afterwards.

## App icon

`docs/launch/make-app-icon.py` draws the icon (gavel mid-strike on a cricket ball)
with Pillow and writes all four files: the Play icon, `icon.png`, the adaptive
foreground and `splash-icon.png`. To change the icon, edit the `Spec` defaults, run
it from the repo root, then bump `versionCode`, prebuild and rebuild. The
launcher icon is baked into the app. Check the result on the emulator's home
screen, not just in the PNG.

## Store screenshots

Play rejects screenshots whose long side is more than twice the short side,
so phone captures at 20:9 (1080×2400) fail. Capture on the emulator instead:

```bash
adb shell 'wm size 1080x1920; wm density 420; settings put global sysui_demo_allowed 1; \
  am broadcast -a com.android.systemui.demo -e command enter; \
  am broadcast -a com.android.systemui.demo -e command clock -e hhmm 1930; \
  am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false; \
  am broadcast -a com.android.systemui.demo -e command notifications -e visible false'
adb exec-out screencap -p > shot.png     # then convert to 24-bit RGB PNG (Pillow .convert("RGB"))
```

- The current set lives in `docs/launch/screenshots/`: bidding war, reveal,
  going twice, sold.
- To catch the brief Sold screen, take a rapid burst of `screencap` frames
  once the dump shows "You're leading" and "Going…", then pick the right
  frame from a contact sheet.
- Reset afterwards: `wm size reset; wm density reset;` and
  `am broadcast -a com.android.systemui.demo -e command exit`.
- Playing a full auction to reach the Summary and season table takes a long
  time. Skip those shots unless they're asked for.

## Sample-ads build (to test the ad flow before AdMob serves)

See `monetization.md`. In short:
1. Temporarily set `REWARDED_AD_UNIT_ID = TEST_REWARDED_AD_UNIT_ID`.
2. Run `assembleRelease`.
3. `git checkout` the file back and confirm the tree is clean.
4. Name the output `TEST-ONLY-…`.

## Launch runbooks (founder-run: give steps, read their screenshots)

The master plan is `docs/launch/LAUNCH_PLAN.md`, with status per row. The
listing text is `STORE_LISTING.md` (full description, one line per
paragraph). The form answers are in `DATA_SAFETY.md`. The screenshots are in
`screenshots/`, and the icon and feature graphic in `docs/launch/`.

**Play Console** — all of this was done by 2026-09-25 (build 5 submitted).
It's kept as the order for the next app. For an update to this one, follow
`docs/launch/RELEASE_CHECKLIST.md`: internal testing first, check it on the
phone installed from Play, then promote the same release to production.
The first-time order was:
1. Create the app: Cricket Auction Simulator, game, free.
2. App content:
   - Privacy URL: `https://cyanharborstudios.com/cricket-auction/privacy/`
   - App access: unrestricted.
   - Ads: yes.
   - Advertising ID: yes (advertising, analytics, fraud prevention).
   - Content rating: Game. No violence, sex, language, drugs, gambling or
     user interaction; yes to digital purchases.
   - Target audience: 13–15, 16–17 and 18+. Not under-13, which would bring
     in the Families policy.
   - Data safety: the four AdMob data types.
3. Store listing: website `https://cyanharborstudios.com`, which AdMob uses
   to find app-ads.txt.
4. Internal testing: add the founder's Gmail as a tester, upload the AAB,
   and accept Play App Signing.
5. Create the `remove_ads_lifetime` product at ₹149.
6. License testing: add the Gmail, then make one free test purchase.
7. Promote to production. Google's review of a new app takes a few days to a
   week.

The organization account is exempt from the 12-testers / 14-days rule.
Android developer verification is visible in the sidebar; confirm it shows
no issues.

**AdMob:** the test device is registered, and `app-ads.txt` is live. After
the app is live in production, link it via Apps → App settings → store
details, and ads start after AdMob's review. Optionally publish a GDPR message
under Privacy & messaging. AdMob approved the account on 2026-09-26; the
bank account (LAUNCH_PLAN D1) is the one money step left.

**Cloudflare:** `cyanharborstudios.com` is a Cloudflare **Worker with static
assets** named `bold-sunset-1149`, not a Pages project.
1. Build a zip from `site/`: `cd site && zip -r -X ../site.zip . -x '.*'`.
2. The founder uploads it under Workers & Pages → `bold-sunset-1149` → New
   deployment.
3. Confirm the result in the built-in browser: `/app-ads.txt` and
   `/cricket-auction/privacy/`.

**Payouts** (before the first payout, not before launch): the founder sees a
CA about GST, an LUT for export of services, and personal versus business
account. Then they add the bank account in the Play Console payments profile
and in AdMob → Payments. AdMob posts a PIN letter to the address.

## Founder-only actions (never do these yourself)

Anything inside Play Console, AdMob, Cloudflare, Google payments or banking.
Entering credentials or payment details. Accepting agreements. Permanent
deletions: for example, `multipass purge` was handed to the founder to run.
Give numbered steps, then verify the outcome from outside where possible,
for example by fetching the live site.
