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
| `screening` | `ScreeningService` (the Android entry point) and `CallScreener` (gathers facts, asks `core`, returns a decision). The service only translates a decision into a `CallResponse` |
| `data` | Room database (`handled_calls`, `allowed_numbers`), `SettingsStore` over DataStore, and the repositories the rest of the app talks to |
| `notify` | notification channels and the three notifications: handled call, periodic report, milestone |
| `reports` | a daily WorkManager job that asks `core` whether a report is due |
| `ads` | ad unit ids (one file), the UMP consent flow, the banner slot, the full-screen ad gate |
| `ui` | Compose screens: home, advanced, history, statistics, settings, licences; a small theme |

One activity. Navigation is `navigation-compose` with plain string routes. Objects
are wired by hand in `AppContainer`; there is no dependency-injection framework.
State flows one way: stores expose `Flow`s, a screen's view model combines them into
one immutable state, the screen renders it and sends user actions back.

## Storage

- **Room**, two tables. `handled_calls(id, number_raw, number_key, at_millis,
  action, rule_id)` indexed on time and on key. `allowed_numbers(number_key PK,
  number_raw, added_at_millis, expires_at_millis NULL)`. Queries are Room's
  compile-checked, parameterised SQL. The schema is exported to `app/schemas/` so a
  migration can be tested when one is needed.
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
| JUnit 4 | unit tests |

No analytics, no crash reporting, no Firebase, no networking library of our own, no
image loader, no chart library (the three charts are drawn with Compose `Canvas`).

## Permissions

Declared by the app: `POST_NOTIFICATIONS`, `INTERNET` and `ACCESS_NETWORK_STATE`
(ads), `AD_ID` (ads). Merged in by WorkManager: `WAKE_LOCK`,
`RECEIVE_BOOT_COMPLETED`, an untyped `FOREGROUND_SERVICE`. The service is protected
by `BIND_SCREENING_SERVICE`, so only the system can bind to it. Nothing else: no
contacts, call log, phone state or SMS. The list is pinned by a test.

## Testing

| Layer | Where | What |
|---|---|---|
| Rules, numbers, statistics, time text | `core/src/test` | plain JUnit, every boundary |
| Screening coordinator, launch gate | `app/src/test` | JUnit with in-memory fakes of the stores |
| Ringing, call log, notifications, 12/24-hour display | emulator, `tools/verify_emulator.sh` | simulated calls; evidence in `docs/verification/` |
