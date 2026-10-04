# CLAUDE.md

Working notes for Claude Code in this repo. Product policy lives in `AGENTS.md`
and `knowledge-base/`; read those before changing behaviour. This file covers how
to *operate* the repo: commands, layout, conventions, gotchas. The studio's shared
playbook (founder taste, Play policy guard, Android release, Play launch, project
method) is the `cyan-harbor` plugin enabled in `.claude/settings.json`; its files
are in the private `falcon` repo under `playbook/` (cloned next to this repo at
`../falcon`).

## What this is

**scarlet** is the internal name. The app is an Android call blocker: calls from
numbers that are not in the user's contacts are rejected or rung silently. Native
Kotlin, Jetpack Compose, no backend, no account, no contacts permission. Published
by **Cyan Harbor Studios**. Package `com.cyanharborstudios.callblock`. The launcher
label "Call Blocker" is a placeholder; the founder confirms the name and the package
before release (PLAN.md gate G3).

It is a clean-room rebuild of an existing app's feature set. `FEATURES.md` is the
inventory, in our own words. **"Block Unknown Callers", "Easy Call Blocker" (the name
its own description uses) and "Life Software Lab" must never appear in the product,
the listing or the code**, and nothing from that app (code, text, icon, colours,
screenshots) is copied. `docs/reference/` holds the
observation notes; they are research, never shipped.

The founder is a Java backend engineer who reads the code. Keep it plain: small
classes, clear names, no clever Kotlin, no dependency-injection framework, few
comments and only where the *why* is not obvious. **Read
`../falcon/playbook/skills/founder-taste/FOUNDER_TASTE.md` before writing any UI,
copy or monetization surface.**

## Layout

```
core/                   Pure Kotlin, no Android. Runs in plain JVM unit tests.
  rules/                the ordered rule list and the engine that walks it
  numbers/              parsing, matching keys and display of phone numbers
  stats/                statistics, milestones, report periods
  time/                 12-hour / 24-hour text
app/                    The Android app. The only module that touches the device.
  screening/            CallScreeningService: asks core for a decision, answers Telecom
  data/                 Room (handled calls, allow list) and DataStore (settings)
  notify/               notification channels and the three kinds of notification
  reports/              the WorkManager job behind the weekly / monthly report
  ads/                  AdMob and the UMP consent flow; ad unit ids live in one file
  ui/                   Compose screens, one file per screen
    parts/              the Switchboard parts the screens are built from: display window, lever, keys, strips, plates, charts
    theme/              the palette by Material 3 role, the type scale on Hanken Grotesk, the motion tokens
  res/font/             Hanken Grotesk, a variable font (SIL OFL; licence text in docs/)
knowledge-base/         Product spec and ADRs. Authoritative.
design/                 The Claude Design brief and the Switchboard prototype (design/prototype/, with SPEC.md)
docs/                   SECURITY_CHECKLIST, reference notes, verification record
tools/                  adb helpers: reference capture, emulator verification
FEATURES.md             every reference feature -> our implementation -> verification
TASKS.md / NOTES.md     work tracking / decisions and deviations
PLAN.md                 the master plan and the founder's gates
```

## Commands

Gradle needs JDK 17. This Mac's default `java` is 22, so set `JAVA_HOME` first.

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v17)
export ANDROID_HOME=/opt/homebrew/share/android-commandlinetools

./gradlew test                       # all unit tests (core + app)
./gradlew assembleDebug lintDebug    # debug APK at app/build/outputs/apk/debug/
```

CI (`.github/workflows/ci.yml`) runs exactly those. Green CI is the merge gate.

Emulator: the AVD `scarlet_phone` (Pixel 4a profile, API 36 Google Play image) is
this app's own; `budget_phone` belongs to the first app. Calls are simulated with
`adb -s emulator-5554 emu gsm call <number>`.

```bash
$ANDROID_HOME/emulator/emulator -avd scarlet_phone -no-window -no-audio &
adb -s emulator-5554 install -r app/build/outputs/apk/debug/app-debug.apk
tools/verify_emulator.py             # the done-line checks; writes docs/verification/
```

## Two devices are usually attached. Always pass `-s`.

The founder's phone (a OnePlus 12 with a work profile) is often on USB while the
emulator runs. **Every adb command names its device with `-s`.** Since 4 October
2026 the phone runs this app as its call-screening app, at the founder's ask, with
the lever at Block: a regression there costs him real calls, so every build goes
through the emulator checks before it goes near the phone, and an install on the
phone happens only on his ask. Never grant or change a role or a setting there,
and never uninstall anything, without asking first.

## The design is the prototype; recreate it, never port it

`design/prototype/` is the Switchboard prototype from Claude Design and `SPEC.md` beside
it holds every value: colours by Material 3 role, type in sp, space and shape in dp, the
motion, the TalkBack order of each screen. The screens recreate it in Compose: colours
land in `ui/theme/Theme.kt` by role, type in `Type.kt`, durations in `Motion.kt`, and
the parts (`ui/parts/`) are the components the spec names. Nothing below the display
window on Home moves between states: the window and the bay under the lever are laid
out as tall as their tallest state (`TallestOf`). Check every screen on the emulator at
360 dp (`adb -s emulator-5554 shell wm density 480` on `scarlet_phone`), light and
dark, 100% and 135% text, before a UI change merges.

## The core module is pure — keep it that way

`core` has no Android dependency and no clock, storage or network of its own: time
and data come in as arguments. That is what makes the rule tests trustworthy.
Screening rules are an ordered list of data (`Rule(id, condition, action)`), first
match wins. A new kind of rule is a new `Condition` and one line in
`RuleEngine.matches`; nothing else changes. See
`knowledge-base/docs/02-rules-engine.md`.

## Tests

- **Every test must be able to fail.** After writing one, break the code once.
- Rule, number, statistics and time-text tests live in `core/src/test`.
- App-side logic that needs no device (the screening coordinator with fake stores,
  the launch gate) lives in `app/src/test`.
- **Never pin copy.** Assert behaviour and state, not wording. On a device, find
  controls by their test tag (exposed as resource ids), not by their text.
- Behaviour that only a device can show (ringing, the call log, notifications) is
  verified on the emulator by `tools/verify_emulator.py` and recorded in
  `docs/verification/`.

## Accessibility

Every clickable element has a label TalkBack can read: text, or a
`contentDescription` for icon-only controls. A control whose state is shown only by
colour also sets its `selected` / `stateDescription` semantics. Layouts must hold at
360 dp with font scale 1.35 (the founder's own phone setting).

## Working agreements

- **Branch from `origin/main`, never from local `main`**
  (`git fetch && git switch -c <name> origin/main`).
- Branch → commit → `gh pr create` → wait for CI → squash-merge. Watch with exit
  codes only (`gh pr checks N --watch > /dev/null; echo CI_EXIT=$?`).
- **Before merging anything, check it against `docs/SECURITY_CHECKLIST.md`.**
- Stage explicit paths; never sweep unrelated files into a commit.
- No dark patterns, ever. No fake urgency, no ads placed where a tap can land on
  them by accident, nothing that makes the free app worse on purpose.
- Record decisions and deviations in `NOTES.md`; track work in `TASKS.md`.

## Google Play policy

Policy is checked, not remembered: `play-policy-config.json` plus the playbook's
`play-policy-guard` checker, before any Play upload and after any change to ads,
permissions, SDKs, data collection, the privacy page or the listing.

This app's sensitive surface is the **call-screening role**. It requests no
call-log, SMS, phone-state or contacts permission, and must stay that way: those
are declaration-gated on Play. If a feature seems to need one, stop and write an
ADR first.

Play policy: before starting a new app or submitting a build, run the checks in
docs/play-repetitive-content.md. A violation can cost the whole developer account.

## Launch gate

`applicationId` is **`com.cyanharborstudios.callblock`**; the first Play upload makes
it permanent. Ad ids are Google's published **test** ids until the founder sends
live ones: the two unit ids in `ads/AdUnits.kt`, the app id at the top of
`app/build.gradle.kts`. `LaunchGateTest` pins the package name and the ad ids and
fails if they drift. `ManifestPermissionsTest` pins the merged permission list;
`ProductTextTest` keeps the reference app's name, urgency copy and off-Play payment
wording out of everything that ships. Never tap a live ad on a real device.

## Debugging a build on someone's phone

Settings shows a build stamp (`v<version> · <date>`). Ask for it before
investigating any "this change is missing" report.
