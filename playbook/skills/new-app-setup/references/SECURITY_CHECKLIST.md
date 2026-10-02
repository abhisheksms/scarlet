# Security checklist — verify before every merge

> Shared across Cyan Harbor apps (master copy in the playbook). Written on
> Cricket Auction Simulator; its names, numbers and paths are the worked example.

Rules to check on **any** change before it merges to `main`, so we never
introduce a vulnerability. Written as Cricket Auction Simulator's gate, where
the headline risk was money; turqoise re-ordered it because its headline risk
was permissions and platform policy. Each app copies this file to `docs/`,
puts its own biggest risk first, and ticks boxes with dates as they're done.
The one asset every app shares is the **Cyan Harbor developer account**: a
policy strike on one app can take down all of them, so permissions and policy
are never below the fold.

## Threat model (what we're actually protecting)

The game is **offline-first, single-player, no backend, no user accounts, no
network API of its own**. That removes whole classes of risk (no server to
breach, no auth, no user data in transit). The real assets to protect are:

1. **The paid entitlement** (`remove_ads_lifetime`) — the only money path.
2. **The developer + ad + store accounts** (a policy/IP strike can ban the
   whole Cyan Harbor account — protect it like an asset).
3. **User device data** collected by third-party SDKs (AdMob) — a privacy +
   Play-policy obligation.
4. **The build/signing pipeline** (keystore, secrets) — never leak it.

The "attacker" for a single-player offline app is usually **the user on their
own rooted device** editing local state. That only matters where it touches
money or policy — a user cheating their own game is not a security issue.

---

## 0. Permissions, background execution & platform policy (when the app needs them)

For an app that must do anything with the screen locked or in the background
(turqoise's cues), these come before money, because they arrive before a
single rupee does and they are the most policy-sensitive things the studio
ships.

- [ ] **Every permission in `app.json` is one a shipped feature needs.** A
      spike may declare two candidate mechanisms at once; when the ADR flips to
      Accepted, the losing variant's permissions come out. Shipping a sensitive
      permission no code uses is a rejection risk and a lie to the user.
- [ ] `USE_EXACT_ALARM` ships **only if** exact-time cues actually ship; Play
      sanctions it for alarm and timer apps, and the Play Console declaration
      must be filed and truthful. `SCHEDULE_EXACT_ALARM` stays capped at
      `maxSdkVersion="32"` (a config plugin does this; keep it).
- [ ] Every foreground service declares a **type** (mandatory on Android 14+)
      that **matches what the service does**. A mismatched or undeclared type
      is both a crash on API 34+ and a policy violation.
- [ ] A foreground service or persistent notification **cancels on session
      end, stop and process death**. A service outliving its job is a battery
      bug and a policy problem, and the likeliest route to a strike.
- [ ] `POST_NOTIFICATIONS` is requested **in context**, never at cold launch,
      and a denial degrades the feature, never blocks the app.
- [ ] Notification and lock-screen text contains **no personal content** unless
      that was decided deliberately. User-authored names are data everywhere
      they render, including the lock screen.
- [ ] iOS: a background audio mode is declared **only if** that mechanism
      ships, and the technique is described accurately in App Store review
      notes. Never disguise it.
- [ ] `allowBackup` stays **`false`** unless someone makes a deliberate call: a
      cloud-backup restore must never silently re-grant a paid entitlement on a
      new device. The store's Restore is the source of truth.
- [ ] **Spike-only config never ships**: throwaway config plugins, app names and
      screens are deleted or replaced when the spike's component closes.

## 1. Money & entitlement integrity (highest priority)

- [ ] Paid entitlement is granted **only** from a Play Billing purchase in the
      `PURCHASED` state — never `PENDING`, never from a client-editable flag alone.
- [ ] Non-consumables are **acknowledged** (`finishTransaction`) or Google
      auto-refunds them.
- [ ] **Restore** re-derives ownership from Play (`getAvailablePurchases`), not
      from a local boolean.
- [ ] Local entitlement storage is treated as a **cache/convenience**, not the
      source of truth. (Accepted residual risk: a rooted user can flip the local
      flag to get the ₹149 remove-ads unlock free. It buys convenience, not
      advantage, so it's documented and accepted; do NOT let this pattern
      spread to anything that affects gameplay/fairness.)
- [ ] A billing failure **never** grants an entitlement and **never** blocks the
      game.
- [ ] No purchase tokens, order IDs, or receipts are logged or persisted in the
      KV store.

## 2. Ads, consent & privacy

- [ ] **Nobody taps a live ad.** The live AdMob ids have shipped in every
      build since 2026-09-23 (one constant in `src/ads/adIds.ts`), dev builds
      included. Self-clicks are invalid traffic and can ban the AdMob account.
      Exercise ads only on a device registered under AdMob → Settings → Test
      devices (the founder's phone is), or with a `TEST-ONLY` sample-ads build.
      Release builds must not be debuggable (the policy guard checks).
- [ ] The ad SDK is **not initialized for entitled (paid) users** — no ad calls,
      less data collection, honours the purchase.
- [ ] No ad is shown automatically, during active bidding, or triggered by a
      loss/scarcity moment (policy + KB integrity rule). Whatever the app's own
      law is (cyan: never during bidding; turqoise: never in a session), it is
      enforced by a test asserting monetization surfaces cannot render in those
      states, not by anyone's discipline.
- [x] Consent (UMP/GDPR/region rules) is respected before personalized ads;
      default to non-personalized where consent is absent. **Done 2026-09-22:**
      `initializeAds()` awaits `AdsConsent.gatherConsent()` (Google's UMP form,
      shown only where required — EEA/UK/Switzerland) *before* initializing the
      SDK, because the SDK may preload ads the moment it initializes. Every ad
      request is non-personalised regardless of consent
      (`requestNonPersonalizedAdsOnly: true`).
- [ ] The **Play Data Safety form + privacy policy stay accurate** to what the
      SDKs actually collect. Adding/upgrading an SDK → re-check both.

## 3. Local data & storage

- [ ] No secrets, credentials, PII, or purchase tokens stored in plaintext in
      SQLite / localStorage / AsyncStorage.
- [ ] SQL uses **parameterized queries** (`?` placeholders) only — never string
      concatenation / interpolation of values into SQL.
- [ ] Web (`localStorage`) persistence holds only non-sensitive game state.

## 4. Secrets & credentials

- [ ] **No secrets committed** — API keys, tokens, service-account JSON,
      keystores, `.env`. (AdMob App ID and Google TEST ad-unit IDs are public
      and are *not* secrets.)
- [ ] `.gitignore` continues to cover `.env*`, `*.keystore`, `*.jks`,
      `google-services.json`.
- [ ] Real secrets live **outside the repo** (the founder's Mac, or GitHub
      Actions secrets if CI ever needs one), never in source or `app.json`.
- [ ] The upload keystore stays offline at `~/CyanHarbor/keystore/` and backed
      up. It is never in the repo, and its password is never printed.

## 5. Input, deep links & deserialization

- [ ] External **deep-link input** (`cyanauction://` scheme, router params) is
      validated/whitelisted and never drives sensitive actions, purchases, or
      arbitrary navigation.
- [ ] Persisted state is **`JSON.parse`d, never `eval`d**, and is **validated /
      guarded** before use so a malformed or tampered save degrades safely
      (fallback), not crash or misbehave.
- [ ] Schema migrations handle old/unknown/corrupt versions without crashing.
- [ ] A corrupt persisted row is **dropped, not fatal**; a newer-schema row reads
      as missing rather than crashing.
- [ ] User-authored text is treated as data everywhere it renders, including
      notifications, the lock screen and share payloads.
- [ ] An app that declares no URL scheme keeps it that way until a feature needs one.

## 6. Dependencies & supply chain

- [ ] New dependencies are **vetted** (reputable, maintained, reasonable size) —
      no random low-reputation packages, especially ones with `postinstall`
      scripts.
- [ ] CI installs with a **frozen lockfile** (reproducible installs); the
      lockfile is committed and consistent with `package.json`.
- [ ] **Dependabot / advisory alerts** are reviewed; security patches applied
      promptly (keep Expo/RN within supported ranges).

### Triage method

Dependabot's "runtime" scope only means a package sits in some `dependencies`
block in the tree — it does **not** mean the package ships to players. Metro
bundles only what the app actually imports. To decide whether an alert reaches a
device, check the shipped bundle rather than guessing:

```bash
cd apps/mobile && npx expo export --platform android --source-maps --output-dir /tmp/exp
# the .map's "sources" array lists every module in the bundle
node -e 'const fs=require("fs");
         const m=JSON.parse(fs.readFileSync("/tmp/exp/_expo/static/js/android/<hash>.hbc.map","utf8"));
         console.log(m.sources.filter(s=>s.includes("node_modules/<pkg>/")).length)'
```

(Read and `JSON.parse` the map — `require` fails, because Node parses a
`.map` file as JavaScript.)

A build-time-only dependency is still a supply-chain risk — it executes on the
build machine (the founder's Mac, for release builds) and could tamper with the
artifact — so it gets patched when patching is safe. It just isn't an
emergency.

### Open advisories (re-reviewed 2026-08-03)

All alerts are transitive Expo SDK 52 build tooling, **none present in the
shipped Android bundle** (verified by the method above). Forced via
`pnpm.overrides` in the root `package.json`: `brace-expansion`, `postcss`,
`ajv` (same-major), and — after verifying the exact consumer call sites plus a
full local `prebuild → export → gradlew assembleRelease` chain — the major
bumps `uuid@<11 → 11.x` (`xcode` calls `require("uuid").v4()`, which v11 still
exports) and `@xmldom/xmldom@<0.8.13 → 0.8.13` (`@expo/plist` round-trips
correctly, tested through `plist.parse`).

**Partly resolved 2026-09-22 by the Expo SDK 57 upgrade.** The `tar` family
(including the one critical alert) is gone: `tar`, `fast-uri` and `image-size`
are no longer in the dependency tree at all, because `@expo/cli` 57 no longer
pulls them. Their overrides were dropped with them.

**Correction (2026-09-23):** the SDK 57 PR claimed `@xmldom/xmldom` went with
them. It did not — it survived at 0.8.13 *and* 0.9.10, and was 22 of the 29
alerts still open after that merge. Checking the lockfile rather than trusting
the claim is what caught it; `grep -nE 'xmldom' pnpm-lock.yaml` is the whole
check and takes a second.

The historical note is kept below because the reasoning matters if a similar
pin ever appears again.

> **Why tar could not simply be bumped (Expo SDK 52, Aug 2026):** tar 7's CJS
> build sets `__esModule: true` with no `default` export, so `@expo/cli`'s
> `_interopRequireDefault(require("tar")).default.extract(...)`
> (build/src/utils/npm.js) dereferenced `undefined` and crashed
> `expo install` / template extraction. Exposure was narrow anyway: on
> macOS/Linux `extractAsync` shells out to the **system** tar binary.

**Cleared 2026-09-23.** Every remaining alert is now pinned forward in root
`pnpm.overrides`, each to its own line's patched floor:

| Package | Was | Now | Reachable from the shipped app? |
|---|---|---|---|
| `@xmldom/xmldom@0.8` | 0.8.13 | 0.8.15 | No — `@expo/plist`, build time |
| `@xmldom/xmldom@0.9` | 0.9.10 | 0.9.12 | No — build time |
| `nanoid@3` | 3.3.16 | 3.3.19 | Only `nanoid/non-secure` via expo-router, already patched |
| `js-yaml@3` | 3.15.0 | 3.15.2 | No — dev tooling |
| `js-yaml@4` | 4.3.0 | 4.3.2 | No — build tooling |

Two major lines of the same package coexist here, which is why the keys are
version-scoped (`js-yaml@3` and `js-yaml@4`, not a flat `js-yaml`): a flat
override would force one consumer onto an incompatible major.

### Accepted, not fixed: `decode-uri-component` (medium, Dependabot #35)

Left at 0.2.2. The advisory's fix is 0.5.0, which **cannot** be taken here:
0.5.0 is ESM-only (`export default function`), while its consumer
`query-string` 7 is CommonJS and does `require('decode-uri-component')` then
calls the result. Under that pairing `require()` returns a module namespace
object, so every call site throws.

This one *does* reach the shipped Android bundle — `query-string` is how
expo-router parses URLs — so the failure would have been a runtime crash on the
first URL carrying a query string, on a device, with a clean build and green
tests. It was bundled by Metro without a warning and caught only by checking
the module format of an overridden package against its consumer.

Rules this leaves behind:

1. Before overriding anything, check whether it reaches the app bundle:
   `expo export --platform android --source-maps`, then grep the `.map` for the
   package name. Build-tooling-only is the easy case; a package in the bundle
   needs its module format checked against its consumer's.
2. `apps/mobile/test/dependency-shape.test.ts` now asserts the behaviour of the
   overridden packages that ship, so a repeat fails in CI instead of on a phone.

Exposure is low: the advisory concerns decoding malformed percent-encoding, and
this app is offline and single-player — it opens no untrusted URLs. Revisit at
the next Expo SDK upgrade, or sooner if `query-string` moves to a version that
imports 0.5.0 natively. It was still the only open alert on 2026-09-26.

## 7. Dynamic code / remote code execution

- [ ] **No `eval`, `new Function`, or execution of remote/untrusted code.**
- [ ] **No WebView** loading untrusted content. If one is ever added: no JS
      injection, no `allowFileAccess`, no untrusted URLs, HTTPS only.
- [ ] If Expo OTA Updates are ever enabled, updates are signed/verified and come
      only from our trusted channel.

## 8. Android hardening & logging

- [ ] Release builds are **not debuggable**; verbose/debug `console.*` logging is
      stripped or guarded (`__DEV__`) so nothing sensitive ships in release.
- [ ] **No cleartext HTTP** — all network (ad SDK, any future call) is HTTPS/TLS.
- [ ] No app component (activity/receiver/provider) is unnecessarily `exported`.
- [ ] `allowBackup` implications considered for the entitlement (don't let a
      cloud-backup restore silently re-grant a paid flag on a new device in a way
      that bypasses Play — Play restore is the source of truth).

## 9. Engine & data integrity

- [ ] The engine stays **pure and deterministic** (no `Date.now`/`Math.random`,
      no network, no ads/billing/storage imports) — this is also a security
      property: no side channels, fully reproducible, auditable.
- [ ] No sensitive data flows *into* the engine or the analytics event log
      (analytics stays coarse — no prices, receipts, tokens, or PII).

## 10. If a backend is ever added (future — currently N/A)

- [ ] Purchase verification moves **server-side** (verify tokens with Google Play
      Developer API); the client is never trusted for entitlement.
- [ ] Any auth, secrets, and validation live server-side; TLS everywhere; input
      validated; rate-limited; least-privilege service accounts.

---

## How we enforce this

- The merging captain (me) **runs through this checklist before merging any PR**,
  and calls out anything relevant in the PR description.
- It is **advisory + human-gated** — findings are surfaced for the founder to
  decide on; nothing is auto-changed.
- When a change touches money, ads, storage, deps, or native config, the
  relevant section above gets explicit attention.
- This file is versioned — update it as the architecture evolves (e.g., when a
  backend or new SDK lands).
