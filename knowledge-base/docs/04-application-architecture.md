# 04 — Application architecture

## Modules

```
core   pure Kotlin (JVM). Rules, numbers, statistics, time text. No Android, no clock, no storage.
app    the Android app. The only module that touches the device.
```

`app` depends on `core`; `core` depends on nothing in the project. A reviewer can
read `core` top to bottom without knowing Android.

## Inside `app`

| Package | Responsibility |
|---|---|
| `screening` | `ScreeningService` (the Android entry point) and `CallScreener` (gathers facts, asks `core`, returns a decision). The service only translates a decision into a `CallResponse`. `DialledNumberRecorder` notes the numbers the user calls, for the call-back rule; `EmergencyCallPause` starts a day's pause after a call to an emergency number |
| `data` | Room database (`handled_calls`, `allowed_numbers`, `dialled_numbers`), `SettingsStore` over DataStore, and the repositories the rest of the app talks to |
| `notify` | notification channels and the three notifications: handled call, periodic report, milestone; and the receiver behind the stopped-call notification's one action, Allow For 1 Hour |
| `reports` | a daily WorkManager job that asks `core` whether a report is due |
| `ads` | ad unit ids (one file), the UMP consent flow, the banner slot, the full-screen ad gate |
| `ui` | Compose screens (home with the timer's sheet, options, schedule, history, statistics, settings, plans, how it works, about, licences), the Switchboard parts they are built from (`ui/parts`: the display window, the lever, keys, strips, plates, charts) and the theme (`ui/theme`: palette, type, motion) |

One activity. Navigation is `navigation-compose` with plain string routes. Objects
are wired by hand in `AppContainer`; there is no dependency-injection framework.
State flows one way: stores expose `Flow`s, a screen's view model combines them into
one immutable state, the screen renders it and sends user actions back.

## Storage

- **Room**, three tables. `handled_calls(id, number_raw, number_key, at_millis,
  action, rule_id)` indexed on time and on key. `allowed_numbers(number_key PK,
  number_raw, added_at_millis, expires_at_millis NULL)`. `dialled_numbers(number_key PK,
  at_millis)`, added in schema version 2 (5 October 2026, ADR-007). Queries are Room's
  compile-checked, parameterised SQL. Each version's schema is exported to `app/schemas/`,
  and Room writes the step from one to the next from those files (an auto-migration).
  The step from 1 to 2 was seen on the emulator: the new build installed over a version 1
  database with rows in both tables, which were all still there
  (`docs/verification/README.md`).
- **DataStore (preferences)** for the handful of settings.
- `allowBackup` is `false`: the log is the user's and stays on this phone.

## Dependencies, and why each is there

| Dependency | Why |
|---|---|
| Jetpack Compose (BOM), Material 3, activity-compose, lifecycle, navigation-compose | the UI toolkit Google recommends for new apps; one language, less code to review than XML layouts with adapters |
| Room (+ KSP for its compiler) | the handled-call log and the allow list; parameterised SQL checked at build time |
| DataStore | settings, readable from a coroutine in the screening service |
| WorkManager | the daily check for a due report. It is also a transitive dependency of the ads SDK, so it adds nothing new to the APK |
| kotlinx-coroutines | the screening path and stores are suspending code |
| libphonenumber (in `core`) | Google's own library for parsing, comparing and formatting phone numbers; the only way to tell an international number from a domestic one reliably |
| Google Mobile Ads SDK, UMP | ads and the consent flow (ADR-006) |
| Hanken Grotesk (a font file, `app/src/main/res/font/`) | the design's one typeface (`design/prototype/SPEC.md`), a variable font from its own repository, under the SIL Open Font License (`docs/OFL-HankenGrotesk.txt`). Its figures are the same width at every weight, which the times and counts rely on |
| JUnit 4 | unit tests |

No analytics, no crash reporting, no Firebase, no networking library of our own, no
image loader, no chart library (the three charts are drawn with Compose `Canvas`).

## Permissions

Declared by the app: `POST_NOTIFICATIONS`, and `RECEIVE_BOOT_COMPLETED` so the
daily report check is still scheduled after a restart (WorkManager asks for it, but
the ads SDK's manifest strips it, so the app declares it itself). Merged in by
WorkManager: `WAKE_LOCK` and an untyped `FOREGROUND_SERVICE`. Merged in by the ads
SDK: `INTERNET`, `ACCESS_NETWORK_STATE`, `AD_ID` and the three `ACCESS_ADSERVICES_*`
permissions. The service is protected by `BIND_SCREENING_SERVICE`, so only the system
can bind to it. Nothing else: no contacts, call log, phone state or SMS.
`ManifestPermissionsTest` pins the merged list.

WorkManager is initialised on demand (`CallBlockApp` implements
`Configuration.Provider`; its start-up initializer is removed in the manifest), so a
process started by an incoming call does not pay for it.

## Testing

| Layer | Where | What |
|---|---|---|
| Rules, numbers, statistics, time text | `core/src/test` | plain JUnit, every boundary |
| Screening coordinator, the dialled-number recorder, the emergency pause, launch gate | `app/src/test` | JUnit with in-memory fakes of the stores |
| Ringing, call log, notifications, 12/24-hour display | emulator, `tools/verify_emulator.py` | simulated calls; evidence in `docs/verification/` |
