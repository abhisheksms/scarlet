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
  rules/                the ordered rule list and the engine that walks it; the mode in effect (timer, schedule, lever)
  plans/                the three plans and what each may use
  numbers/              parsing, matching keys and display of phone numbers
  stats/                statistics, milestones, report periods
  time/                 12-hour / 24-hour text
app/                    The Android app. The only module that touches the device.
  screening/            CallScreeningService: asks core for a decision, answers Telecom; notes the numbers the user calls; pauses after an emergency call
  data/                 Room (handled calls, allow list, numbers called in the last day) and DataStore (settings)
  notify/               notification channels and the three kinds of notification
  reports/              the WorkManager job behind the weekly / monthly report
  ads/                  AdMob and the UMP consent flow; ad unit ids live in one file
  billing/              Google Play Billing: what the user owns, what is on sale, the purchase screen
  tile/                 the Quick Settings tile: pause filtering for an hour, or resume it, with one tap
  ui/                   Compose screens, one file per screen
    parts/              the Switchboard parts the screens are built from: display window, lever, keys, strips, plates, charts
    theme/              the palette by Material 3 role, the type scale on Hanken Grotesk, the motion tokens
  res/font/             Hanken Grotesk, a variable font (SIL OFL; licence text in docs/)
knowledge-base/         Product spec and ADRs. Authoritative.
design/                 The Claude Design brief and the Switchboard prototype (design/prototype/, with SPEC.md)
docs/                   SECURITY_CHECKLIST, reference notes, verification record
  launch/               LAUNCH_PLAN.md: every step to a live Play listing, who does it, what it waits for
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
and never uninstall anything, without asking first. An `adb install` on that phone
waits, for as long as it takes, on the phone's own install-scan screen until he
confirms it there; read the package's `lastUpdateTime` and the role holder before
and after, and say what changed.

**Never dial an emergency number, on the phone or on the emulator.** The pause after an
emergency call is checked by emulator check 23, which puts a made-up number on Android's
own test list (`cmd phone emergency-number-test-mode`) and calls that. The emulator then
sits in emergency callback mode, where Android asks no screening app about any call, until
it is restarted; the check restarts it.

## The design is the prototype; recreate it, never port it

`design/prototype/` is the Switchboard prototype from Claude Design and `SPEC.md` beside
it holds every value: colours by Material 3 role, type in sp, space and shape in dp, the
motion, the TalkBack order of each screen. The screens recreate it in Compose: colours
land in `ui/theme/Theme.kt` by role, type in `Type.kt`, durations in `Motion.kt`, and
the parts (`ui/parts/`) are the components the spec names. On Home nothing moves under a
finger: the display window is laid out as tall as the states the lever and the pause keys
can reach from the current one (`TallestOf`), and the bay under the lever as tall as its
keys; at Off the bay is absent, and the rare states (a first run's legend, the role
missing) may make the window taller for their own time (`NOTES.md` N-57). Every row
carries a line icon beside its word, drawn in `ui/parts/Icons.kt` in the design's own
stroke style; no icon library. A control shows its state in its own body, without colour:
the lever's stops and the panel switch carry a lamp that is lit for what is in effect
(`NOTES.md` N-58). Check every screen on the emulator at
360 dp (`adb -s emulator-5554 shell wm density 480` on `scarlet_phone`), light and
dark, 100% and 135% text, before a UI change merges.

**A part that moves under a finger** keeps one position, written by the finger and by its
own travel, and read only where the part is placed or drawn, never while composing. It
answers the finger, not the store: it stands where the user put it at once, and goes back
to the stored value if the store has not followed within a second. `ui/parts/Lever.kt` is
the model and emulator check 25 its guard. A change to how something moves is measured
frame by frame before and after (`docs/verification/lever-handle-2026-10-06.md` says how).

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
- `ProductTextTest` also fails on a reassurance or a denial, a chatty word, an
  exclamation mark or a vague phrase in any shipped string: the founder's copy rule.

## The words on the screen

The founder's rule for every app (falcon, `FOUNDER_TASTE.md` §14, 9 October 2026, and the
`ui-copy` skill): a label names the thing in one to three words, in the words the phone's
own apps use ("unknown numbers", "allowed", "blocked"); a status is a state word; a
sentence only where a label cannot carry it; the explaining lives in How It Works and
nowhere else; nothing on a screen reassures or denies; no figurative phrases. Before
changing any string, read that skill and run its checker over `strings.xml`:

```bash
python3 -I ../falcon/playbook/skills/ui-copy/scripts/copy_lint.py app/src/main/res/values/strings.xml
```

Errors are bugs; each warning is a decision (rewrite, move to How It Works, or accept with
a reason in `NOTES.md`). The rewrite of 9 October is the worked example (`NOTES.md` N-56).

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

The launch's steps, in order, are `docs/launch/LAUNCH_PLAN.md`. Keep its "where things
stand" paragraph and its rows current as they are done, and add a dated paragraph when a
fact changes. This repo is public: the studio's identity strings stay in the first app's
plan and never come here.

`applicationId` is **`com.cyanharborstudios.callblock`**; the first Play upload makes
it permanent. Ad ids are Google's published **test** ids until the founder sends
live ones: the two unit ids in `ads/AdUnits.kt`, the app id at the top of
`app/build.gradle.kts`. `LaunchGateTest` pins the package name and the ad ids and
fails if they drift. It also pins the three product ids (`no_ads`, `pro`, `pro_upgrade`):
once made in Play Console a product's id can never be changed or used again. A purchase
can only be tried on a copy installed from Play by a licence tester, never on a build
installed over adb. The plans screen is a price list (`ui/PlansScreen.kt`,
`billing/PlanOffers.kt`): the price is Google Play's own; a test build shows the planned
ones (`billing/PlannedPrices.kt`), says so, and its Buy keys give the plan without a
payment. Nothing on it calls a plan popular or the best, claims a saving or counts down
(`ProductTextTest`). The prices, why they were chosen and what is watched after launch are
in `docs/launch/LAUNCH_PLAN.md`, "Pricing". `ManifestPermissionsTest` pins the merged permission list;
`ProductTextTest` keeps the reference app's name, urgency copy and off-Play payment
wording out of everything that ships. Never tap a live ad on a real device.

A row never opens a page that does not exist. `ui/Links.kt` has two switches, both off
today: `PRIVACY_PAGE_LIVE` (About's Privacy Policy row) and `STORE_PAGE_LIVE` (Share App,
Rate App, and the link in shared statistics). Google Play requires the privacy row in any
build uploaded to it, so the first goes on with the page's deploy, before the first
upload; `LaunchGateTest` refuses live ad ids while either is off.

## Debugging a build on someone's phone

Settings shows a build stamp (`v<version> · <date>`). Ask for it before
investigating any "this change is missing" report.
