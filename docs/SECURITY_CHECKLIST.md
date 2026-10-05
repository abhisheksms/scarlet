# Security checklist — verify before every merge

Adapted from the studio's master copy (playbook, `new-app-setup/references/`). This
app's biggest risk is first: **permissions and platform policy**. It sits between the
phone network and the user's ringer, and it is published from the one Cyan Harbor
developer account that every app shares.

## Threat model

No backend, no account, no network code of our own. What there is to protect:

1. **The user's calls.** A bug that blocks a call that mattered is the worst thing
   this app can do.
2. **The developer, ad and store accounts.** A policy strike on one app can take
   down all of them.
3. **The numbers of people who called the user, and of people the user called.**
   Personal data about third parties, held on the device.
4. **The build and signing pipeline.**

## 0. Permissions, the role and platform policy

- [ ] The manifest requests **no** contacts, call-log, SMS, phone-state or
      phone-number permission. `ManifestPermissionsTest` pins the list; a new
      permission fails CI until the test and an ADR are changed together.
- [ ] Screening goes only through `CallScreeningService` with
      `ROLE_CALL_SCREENING`. The service is protected by
      `android.permission.BIND_SCREENING_SERVICE`.
- [ ] The role is requested **in context** (when the user picks silence or block),
      never at cold launch.
- [ ] `POST_NOTIFICATIONS` is requested in context, and a refusal degrades only the
      notification feature.
- [ ] Any failure, timeout or unreadable input while screening **allows the call**.
- [ ] **Nobody and nothing calls a real emergency number to test this app**, on any
      device. The day's pause after an emergency call (ADR-008) is checked on the emulator
      only, with a made-up number on Android's own test list of emergency numbers
      (`tools/verify_emulator.py`, check 23, which refuses anything but an emulator).
- [ ] The screening path does nothing slow before answering: no network, no ads SDK,
      no work in `Application.onCreate` beyond wiring objects.
- [ ] No foreground service of our own. WorkManager's untyped `FOREGROUND_SERVICE`
      is the only one in the merged manifest.
- [ ] Notification text on a locked screen carries **no phone number**
      (`VISIBILITY_PRIVATE` with a number-free public version).
- [ ] `allowBackup` stays **`false`**.
- [ ] The reference app's name and its developer's name appear nowhere in the app,
      the listing or the code. Nothing of theirs is copied. `ProductTextTest` holds
      the first half for everything under `src/main`.

## 1. Ads, consent and privacy

- [ ] Ad unit ids are Google's **test** ids until the founder sends live ones;
      `LaunchGateTest` pins them. Nobody taps a live ad on a real device.
- [ ] UMP consent is gathered before the ads SDK is initialised; Privacy Choices is
      offered in Settings whenever UMP requires it.
- [ ] Every ad has reserved space; none can move content or sit under a control.
      No ad over the role request, a confirmation, or the on/off state.
- [ ] No phone number, call count or other user data goes into an ad request.
- [ ] A row never opens a page that does not exist, and a build for any Play track
      shows the Privacy Policy row (About): `Links.PRIVACY_PAGE_LIVE` is on only once
      the page answers, and it is on before the first upload. `LaunchGateTest` refuses
      live ad ids while it is off.
- [ ] The Data safety form and the privacy page describe what the ads SDK collects
      and that the call log stays on the device, and they agree with each other.
      Adding or upgrading an SDK means re-checking both.

## 2. Local data

- [ ] The handled-call log, allow list, dialled numbers and settings never leave the
      device: no upload, no analytics, no crash reporter.
- [ ] A number the user called is kept only for the call-back rule: its key and the
      time, for 24 hours at most, never shown on a screen, not written while the switch
      is off, and deleted when it is switched off (ADR-007).
- [ ] SQL is Room's parameterised, compile-checked queries only.
- [ ] Phone numbers are not written to logcat in release builds.
- [ ] Text that came from outside (a caller's number) is treated as data wherever it
      is shown: notifications, share text, the screens.
- [ ] Shared text (statistics) contains counts only, never a number.

## 3. Secrets

- [ ] Nothing secret is committed: no keystore, password, service-account file or
      `.env`. AdMob ids are public and are not secrets.
- [ ] The upload keystore lives in `~/CyanHarbor/keystore/`, outside the repo.
- [ ] No personal identifier of the founder (phone serial, numbers, addresses) in
      the repo, a commit or a PR. Captures of his phone stay in the git-ignored
      `docs/reference/raw/`.

## 4. Inputs and components

- [ ] The only exported components are the launcher activity and two services only
      the system can bind: the screening service and the Quick Settings tile. No
      deep-link scheme, no exported receiver or provider of our own.
- [ ] `PendingIntent`s are immutable and explicit.
- [ ] A corrupt or unexpected stored value falls back to a safe default (mode off),
      never a crash in the screening path.

## 5. Dependencies and supply chain

- [ ] Every dependency is first-party (AndroidX, Google) or equally established, and
      its reason is recorded in `knowledge-base/docs/04-application-architecture.md`.
- [ ] The Gradle distribution's SHA-256 is pinned in `gradle-wrapper.properties`.
- [ ] Versions live in one catalog (`gradle/libs.versions.toml`); upgrades are their
      own PR.

## 6. Android hardening

- [ ] Release builds are not debuggable and are minified.
- [ ] All network traffic (the ads SDK's) is TLS; no cleartext exception.
- [ ] No WebView of our own, no dynamic code loading.

## 7. Engine integrity

- [ ] `core` stays free of Android, clocks, storage and network: facts and time are
      arguments. The decision for a call can always be reproduced from its inputs.
- [ ] Only the last rule in the list can block or silence (asserted by a test).

## How this is enforced

The merging agent walks this list before merging a PR and notes anything relevant in
the PR body. Items that a test can hold are held by a test, so they do not depend on
anyone's memory.
