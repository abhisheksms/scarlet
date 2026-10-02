# ADR-005: minSdk 29, targetSdk 36, compileSdk 37

## Status

Accepted (2026-10-02).

## Context

Checked on developer.android.com on 2026-10-02:

- `RoleManager` and `ROLE_CALL_SCREENING`: "Added in API level 29". Before Android 10
  only the default dialer could screen calls.
- `CallScreeningService.CallResponse.Builder.setSilenceCall`: "Added in API level 29".
- Target API (`google/play/requirements/target-sdk`, last updated 2026-10-01): "New
  apps and app updates must target Android 16 (API level 36) or higher to be
  submitted to Google Play", from 31 August 2026.
- The reference app, as the system reports it, also has minSdk 29 and targetSdk 36.

## Decision

- `minSdk = 29`. Both things the app is for (the role, silencing) need it.
- `targetSdk = 36`, Play's current requirement for new apps.
- `compileSdk = 37`: the AndroidX libraries current on 2026-10-02 (Compose 1.12,
  navigation 2.10) declare a minimum compile SDK of 37. Compiling against a newer SDK
  does not change runtime behaviour; `targetSdk` does. Lint's `NewApi` check guards
  against calling anything newer than `minSdk` without a version check.

## Consequences

- Android 9 and older are excluded. On those versions a third-party app cannot
  screen calls at all, so nothing is lost.
- The target API moves every 31 August; the playbook's policy digest tracks the
  date. Re-check before each release.
- `setRejectedAsMissed` (added in SDK 37.2) is not used; revisit when the target
  moves to 37.
