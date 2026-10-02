# ADR-001: Native Kotlin, superseding the playbook's Expo stack for scarlet

## Status

Accepted (2026-10-02, founder's call in the brief). **Supersedes, for this app only,**
the playbook's stack ADR (`new-app-setup/templates/.../ADR-001-stack-inherited.md`).

## Context

The studio's proven stack is Expo / React Native / TypeScript. This app's core is not
a screen: it is an Android system service (`CallScreeningService`) that the platform
binds to before the phone rings, possibly into a cold process, with five seconds to
answer. A JavaScript runtime in that path would add start-up time and a bridge for no
benefit, and the role request, the notification channels and the background job are
all native APIs. The founder is a Java backend engineer and will review the code.

## Decision

- Native Android in Kotlin. Two Gradle modules: `core` (pure Kotlin, no Android) and
  `app`.
- UI in Jetpack Compose with Material 3. One activity, `navigation-compose`.
- Room for the two tables, DataStore for settings, WorkManager for the periodic
  report, coroutines throughout.
- Manual wiring in one `AppContainer`. No dependency-injection framework.
- Versions are the latest stable on the day of Component 0 (2026-10-02): AGP 9.4.1,
  Kotlin 2.4.20, Gradle 9.6.1, Compose BOM 2026.09.00. They live in
  `gradle/libs.versions.toml`.
- What still carries over from the playbook: the method (spec, pure engine first, CI
  as the gate), `CLAUDE.md` / `AGENTS.md` conventions, the knowledge base, the
  security checklist, the policy guard, the launch kit and the release procedure,
  with Gradle outputs under `app/build/outputs`.

## Consequences

Positive:

- The screening path is a few hundred lines of Kotlin with no runtime to start.
- The engine is plain JVM code: fast tests, readable by a Java engineer.
- No prebuild step, no native-module compatibility work at SDK upgrades.

Negative:

- None of the first app's components, test harness or config templates transfer.
- The playbook's policy checker assumes an Expo repo; its config needs adapting
  (`source_dirs: app/src/main`, `android_dir: app`).
- Android only. An iOS version would be a separate app (CallKit's call directory
  works very differently).

## Amendments

- 2026-10-02: `compileSdk` is 37 while `targetSdk` stays 36. The current AndroidX
  libraries declare a minimum compile SDK of 37; the Android 17 platform was added to
  the Mac's SDK for it. See ADR-005.
