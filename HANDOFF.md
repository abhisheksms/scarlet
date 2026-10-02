# Handoff: scarlet, 2 October 2026

Paste this into a Claude Code session started in
`/Users/abhisheksms27/desktop/sahara/scarlet` after `git pull origin main`. It
replaces the cloud session that built the playbook and received the task below.

## Who and what

- **Founder:** Abhishek, Cyan Harbor Studios (a Google Play organization
  account; domain `cyanharborstudios.com`). Product and taste are his; he is a
  Java backend engineer who reads the code. He wants outcomes first, plain
  English, no yapping, initiative on everything reversible, and confirmation
  before anything outward or irreversible. Hand over one command, not a list.
- **Projects:** `cyan` (Cricket Auction Simulator, Expo / React Native, live on
  Google Play since 2 Oct 2026; repo `abhisheksms/cyan` at `922adad`),
  `turqoise` (an interval timer, stopped at the design stage on 16 Aug 2026;
  `abhisheksms/turqoise` at `d4fb6b4`), and **`scarlet`**, this repo, the third
  project and the studio's first money app.
- **This repo's `main` (`ea4ea22`)** holds the studio playbook under
  `playbook/`: a Claude Code plugin with six skills extracted from cyan and
  turqoise: `project-method`, `founder-taste`, `play-policy-guard`,
  `android-release`, `play-launch`, `new-app-setup`. Sessions in this repo load
  them through the `.claude/skills/` symlinks. Other repos install the plugin
  with `playbook/skills/new-app-setup/templates/claude-settings.json`
  (marketplace `abhisheksms/scarlet`). The playbook is the master copy of
  everything generic; app-specific things stay in the app.

## Read first, in this order

1. `playbook/README.md`
2. `playbook/skills/project-method/SKILL.md` (the method) and
   `playbook/skills/founder-taste/FOUNDER_TASTE.md` (binding for every screen,
   string and price)
3. `playbook/skills/new-app-setup/references/SECURITY_CHECKLIST.md`, section 0
   (permissions and platform policy come before money for this app)
4. `playbook/skills/play-policy-guard/SKILL.md` and its
   `references/policy-digest.md`
5. `playbook/skills/play-launch/references/studio-account.md` (what the studio
   already has: org account, AdMob publisher, domain, site, merchant profile)

## The task, in the founder's words

Build a feature-for-feature Android rebuild of "Block Unknown Callers" by Life Software Lab:
https://play.google.com/store/apps/details?id=com.lifesoftwarelab.android.incomingcallcontrol

Why: this is the first money app for my studio, Cyan Harbor Studios (my Play Console organization account; domain cyanharborstudios.com). The reference app is a small developer's one-job app that earns from ads, and I want feature parity first. A later phase will add India-specific rules (auto-block 140-series telemarketing numbers, always allow 160-series bank and government service numbers) and a one-time Pro unlock. So keep the blocking logic as an ordered list of rules held as data, so prefix rules can be added later without a rewrite.

What the listing says the app does (read the listing yourself too):
- One switch blocks or silences every call from a number not saved in contacts.
- Block mode rejects the call. Silence mode lets it ring silently, and the call still appears in the phone's call log.
- Optional notification each time a call is handled.
- History of blocked and silenced calls, a per-number details screen, and a "temporary allow" feature (exact behavior unknown).
- Times follow the phone's 12/24-hour setting.
- No account or login, and it claims it never reads contacts.
- Contains ads.

How to study the reference app: this is a clean-room rebuild. Learn features only from the listing and the app's visible behavior. Don't download, pull or decompile its APK, and don't copy its code, name, icon, screenshots or text; copied material gets apps removed under Google Play's impersonation and intellectual-property policies. The app is installed on my OnePlus 12, connected to this Mac over USB. You can launch it, navigate with adb input, and capture screens with screencap and uiautomator dump to inventory every screen and setting.

Constraints:
- Native Kotlin, not Expo or React Native, since the core is an Android system service. I'm a Java backend engineer and will review the code, so keep it plain and readable.
- Use Android's call-screening role (CallScreeningService, requested through RoleManager). Don't request call-log permissions; keep your own log of screened calls. The reference app's no-contacts claim suggests a third-party screening app only receives calls from numbers outside the contacts list. Verify that in the Android docs and on the emulator. If contacts permission turns out to be necessary, add it, record why in NOTES.md, and carry on.
- minSdk: the call-screening role and silencing need Android 10 (API 29); confirm this. Target whatever API level Google Play currently requires for new apps.
- Working package name: com.cyanharborstudios.callblock, with an original placeholder app name. I'll confirm both before release.
- Ads: AdMob with Google's published test ad unit IDs only, plus Google's UMP consent flow.
- Test blocking on the Android emulator: simulate incoming calls with `adb emu gsm call <number>` and add a test contact there.
- Track work in TASKS.md, and record decisions and deviations in NOTES.md.

Done means:
- FEATURES.md lists every feature of the reference app, with its source (listing or an observed screen), mapped to its implementation and a verification.
- `./gradlew assembleDebug` and `./gradlew test` pass, with unit tests covering the rule logic.
- On the emulator, verified with simulated calls:
  - a non-contact is rejected in Block mode;
  - a non-contact rings silently in Silence mode and appears in the system call log;
  - a contact rings normally;
  - history and number details show correct times under both 12-hour and 24-hour settings;
  - the notification appears only when enabled;
  - temporary allow lets the number ring until it expires.

Stops: keep going without asking whenever a step doesn't need me. Stop and ask before:
- granting or changing any role or setting on my physical phone (a call screener there would start blocking my real calls), or uninstalling anything from it;
- publishing, uploading to Play Console, creating accounts, or using real AdMob IDs;
- deleting files outside this repo or force-pushing.
Stop when the done line is met. Don't start the India rules or the Pro unlock.

Report back with:
1. Needed from me (first): decisions or access you're waiting on.
2. Parity table: feature | source | status (done, or not built plus reason) | verification (command and result, or test name).
3. Permissions and roles: each one, why it's needed, and any Play policy note.
4. Couldn't confirm: reference-app behavior you couldn't observe, and where you looked.
5. Build, test and install commands.

## Additions the founder made after that prompt

- Keep every finding in this repo. Commit as you go: branch from `origin/main`,
  PR, CI, squash-merge. He merges his own PRs without asking.
- Docs the playbook may still be missing (app launch, founder taste and the
  like) are in `https://github.com/abhisheksms/falcon`. Read it; merge what the
  playbook lacks into `playbook/` as its own PR; use it here.
- The reference app is installed on his OnePlus 12, tethered over USB to this
  Mac.

## How the playbook applies here (reconcile, don't ignore)

- **Stack.** The task overrides the playbook's Expo / React Native stack. Write
  `knowledge-base/adr/ADR-001-stack.md` as "Superseded for scarlet: native
  Kotlin, because the core is a CallScreeningService"; the playbook's stack ADR
  (`new-app-setup/templates/knowledge-base/docs/adr/ADR-001-stack-inherited.md`)
  is the format. The Expo config templates don't apply. The CLAUDE.md and
  AGENTS.md conventions, the knowledge base, the security checklist, the policy
  guard, the launch kit and the release procedure do, with Gradle outputs under
  `app/build/outputs` instead of `apps/mobile/android`.
- **Method, lightly.** `PLAN.md` from `project-method/templates/PLAN.md`; a
  `knowledge-base/` with `00-product-vision`, `01-v1-scope` (must / later /
  never, with the India rules and the Pro unlock written down under "later"),
  the rules-engine model and the screening flow, and ADRs for the
  hard-to-reverse calls: the call-screening role, no call-log permissions, rules
  as data, minSdk 29, AdMob test ids with UMP. `FEATURES.md`, `TASKS.md` and
  `NOTES.md` at the root, as the founder asked. Design-first is a light pass for
  a one-job utility: no Claude Design boards unless he asks; founder taste still
  binds every screen and string (Title Case buttons, no narrator copy, no "AI",
  no dark patterns, every control labelled for TalkBack).
- **Rules as data.** An ordered list of rules (contact → allow; temporary allow
  → allow until expiry; later: prefix allow 160-series, prefix block 140-series;
  default → block or silence per the switch), first match wins, in a pure Kotlin
  module with unit tests. The service is a thin adapter that maps a decision to
  a `CallResponse`.
- **Policy.** The checker's config (`play-policy-guard/policy-config.example.json`)
  is written for an Expo repo. For Kotlin set `source_dirs` to `app/src/main`,
  `android_dir` to `app`, and stub or adapt `expo_app_json`; the listing,
  graphics, Data safety, privacy and APK checks apply unchanged. Add "Block
  Unknown Callers" and "Life Software Lab" to `trademark_terms`: they must never
  appear in the product, listing or code. Data safety: AdMob's four rows; the
  app's own call log is on-device only, which is exempt, but say so in the
  privacy page.
- **Release**, when it comes: the `android-release` skill, a new upload keystore
  `~/CyanHarbor/keystore/callblock-upload.jks`. Not now; the done line stops at
  the emulator.

## Studying the reference app (clean room)

- Allowed sources: the Play listing, and the app's visible behaviour on the
  phone. Not allowed: pulling its APK (no `adb pull`, no `pm path` tricks),
  decompiling, or copying code, name, icon, screenshots or text.
- Drive it over adb: launch with
  `adb shell monkey -p com.lifesoftwarelab.android.incomingcallcontrol -c android.intent.category.LAUNCHER 1`,
  then `uiautomator dump` and `screencap`. The playbook's
  `playbook/skills/android-release/scripts/adb_drive.py` does dump / find / tap
  / shot by accessibility label (`SERIAL` picks the device). Keep observation
  screenshots under `docs/reference/` for the inventory; never ship them.
- Inventory every screen, setting, toggle, state, and the structure of the
  history and details screens into `FEATURES.md`, each row marked
  "source: listing" or "source: screen <name>", described in your own words.
- Phone rules: never grant or change a role or setting, never uninstall, and
  never install the new app on it without asking. Cyan's build notes record
  that the founder's earlier phone had a work profile (user 10) and installs
  needed `--user 0`; check `adb shell pm list users` before any install
  anywhere.

## Android facts to verify against developer.android.com before coding

- `CallScreeningService` receives `onScreenCall` for incoming calls. When the
  app holds `ROLE_CALL_SCREENING` but is not the default dialer, Android 10+
  only routes calls from numbers not in the user's contacts, which is the basis
  of the "never reads contacts" claim. Confirm in the docs, then on the emulator
  with a test contact.
- `CallResponse.Builder`: `setDisallowCall(true)` + `setRejectCall(true)` is
  block; `setSilenceCall(true)` (API 29) is ring silently; `setSkipCallLog(false)`
  keeps the system call-log entry; `setSkipNotification`. Hence minSdk 29.
- Target SDK: Play has required API 36 for new apps since 31 Aug 2026 (cyan's
  launch notes); confirm the current requirement.
- `RoleManager.createRequestRoleIntent(ROLE_CALL_SCREENING)`;
  `POST_NOTIFICATIONS` (API 33+) requested in context, only when the user turns
  notifications on.
- Emulator: a Google APIs image with the Phone app, `adb emu gsm call <number>`,
  a contact added in the emulator's Contacts app, the role granted there.

## Open items outside the task

- Switch cyan to the plugin (its `.claude/settings.json` pointing at
  `abhisheksms/scarlet`) and delete cyan's duplicate copies of the skills and
  docs.
- Copy the founder's own launch notes,
  `~/CyanHarbor/playbooks/google-play-launch-playbook.md`, into
  `playbook/skills/play-launch/references/`.
- Merge falcon's docs into the playbook.

## First command

```bash
cd /Users/abhisheksms27/desktop/sahara/scarlet && git pull origin main && ls -la .claude/skills && claude
```

Then: "Read HANDOFF.md and start with the Read-first list."
