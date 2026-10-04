# NOTES — decisions and deviations

Newest last. Each entry: what was decided or found, why, and what it changes.

## 2026-10-02

**N-01 The reference is bigger than the brief.** The brief lists a switch, two modes,
an optional notification, history, a per-number details screen and "temporary allow".
The installed app (1.4.5) also has: a scope setting (all unknown callers, or only
international ones), pause, repeat-caller pass-through, an allow list, a statistics
screen with a milestone, three charts and a top-numbers list, a weekly or monthly
report, a milestone notification, share and review links. "Feature-for-feature"
means all of it, so all of it is in `FEATURES.md` and in scope. The founder can cut
(PLAN gate G5).

**N-02 "Per-number details" is a sheet in Statistics, not a screen in History.** In the
reference, tapping a history row does nothing; the details (counts, first and last
time) open from the top-numbers list in Statistics. Ours opens the same sheet from
both places (addition A-03).

**N-03 "Temporary allow".** Nothing in the reference carries that name. Two things fit
it: *pause* (everything rings for a while) and the *allow list* (one number rings).
Until the phone session shows whether the reference's allow entries can expire, ours
does both: pause, and allow entries that are permanent or expire. The done-line check
"temporary allow lets the number ring until it expires" is verified against an
expiring allow entry, and against pause.

**N-04 Contacts permission is not needed.** Confirmed in Android's documentation
(quoted in ADR-002): a screening app without `READ_CONTACTS` is passed only calls
from numbers outside the contacts. The reference requests no contacts permission
either. Emulator confirmation is appended to ADR-002 when the spike runs.

**N-05 Withheld numbers cannot be screened.** Android does not pass calls with a
restricted, unknown, unavailable or payphone presentation to a screening app. They
ring. This is a platform limit, written into the scope document and owed to the
store listing.

**N-06 A blocked call always shows in the system call log.** Android logs it as
"blocked" and ignores `setSkipCallLog` from third-party screening apps. The brief's
"`setSkipCallLog(false)` keeps the entry" is therefore moot; the call is not used.
Blocked calls do not raise a missed-call notification (`setSkipNotification(true)`):
the app's own optional notification replaces it.

**N-07 compileSdk 37, targetSdk 36.** Current AndroidX refuses to compile against
less than 37. The Android 17 platform (`platforms;android-37.0`) was added to the
Mac's SDK with `sdkmanager`. The target stays 36, Play's requirement. ADR-005.

**N-08 Gradle's distribution could not be downloaded from Java on this network.**
`services.gradle.org` redirects to GitHub's release CDN; Java picks one address of
that host and it times out here, while `curl` tries the others. The 9.6.1 zip was
fetched with `curl` into `~/.gradle/wrapper/dists/…`, checked against Gradle's
published SHA-256, and that checksum is pinned in `gradle-wrapper.properties`.
Maven Central and Google's Maven are unaffected. If a new Gradle version is ever
needed on this Mac, fetch it the same way.

**N-09 A separate emulator.** `scarlet_phone` (Pixel 4a profile, API 36, Google Play
image) was created so the first app's `budget_phone` keeps its state. The test
contact, the role and the clock-format changes all happen there.

**N-10 Raw captures of the reference stay out of git.** The history and statistics
screens on the founder's phone show the real numbers that called him.
`docs/reference/raw/` is git-ignored; the notes describe structure only. The handoff
asked for screenshots under `docs/reference/`; this is a deliberate deviation for
privacy. One capture that listed the founder's contacts as share targets was deleted
immediately.

**N-11 An ad was tapped by accident during the study.** The reference's Settings
screen inserts a large ad after it has drawn; a tap aimed at a row landed on it and
opened a Play install sheet, which was closed without installing. The adb helpers now
tap by label and only when the target has not moved between two reads. This is also
the origin of the product law in `PLAN.md` §2.10.

**N-12 Ads: where ours differ from the reference, on purpose.** Reserved-space banner
instead of a late-loading native block; a capped full-screen ad on *leaving* History
or Statistics instead of on opening them. Reasons and the founder's gate are in
ADR-006 and PLAN G6. Parity of the surface, not of the behaviour that Play's policy
warns against.

**N-13 Placeholder name.** The launcher label is "Call Blocker", a plain category
term chosen so nothing echoes the reference's name. The founder confirms the name
and the package before release (G3).

**N-14 The "time saved" figure.** The reference shows time saved at what works out
to 30 seconds a call. Ours keeps the figure and states the assumption next to it,
because an unexplained number would be a made-up one (G8).

**N-15 `RECEIVE_BOOT_COMPLETED` is declared by the app.** WorkManager asks for it so
its scheduled work survives a restart, but the ads SDK's manifest strips it
(`tools:node="remove"`). Without it the weekly or monthly report would silently stop
after a reboot until the app was next opened. It is a normal permission with no Play
declaration. The reference app holds it too. `ManifestPermissionsTest` pins it with
this reason.

**N-16 The home switch shows only the chosen mode's description.** The first version
gave each of the three modes its own card with a description. At the founder's own
phone settings (360 dp, 135% text) that took two thirds of the screen. Now the three
sit in one group and only the selected one is expanded, so the switch, its state, any
notice and the first rows fit without scrolling.

**N-17 Share and Rate moved to Settings.** The reference has them at the bottom of its
home screen. Ours keeps the home screen for state; both are rows in Settings.

**N-18 PR #4 was merged about a minute before its CI run finished.** The watcher
(`gh pr checks --watch`) returned as soon as the first run was cancelled by a
follow-up push. Both runs, the PR's and the one on `main`, finished green.
`tools/merge_when_green.sh` now asks for the named check's state on the PR's head and
merges only on success.

**N-19 A real number nearly went into the screenshots.** For demo data I typed a
telemarketing number I had seen in the reference app's history on the founder's
phone. It was caught before anything was saved or sent; the captures were deleted and
redone with made-up numbers. No real caller's number is in the repo.

**N-20 The playbook's policy checker is Expo-shaped.** Its APK checks ran on the
release build (target API 36, permissions all on the allowlist, no typed foreground
service; one error, the advertising-ID declaration, which is a launch task). Its
source checks look for TypeScript and do not apply. The Kotlin-side equivalents are
unit tests here: `ManifestPermissionsTest`, `LaunchGateTest`, `ProductTextTest`. A
Kotlin-aware pass of the checker belongs in falcon as its own PR; not done.

**N-21 The legacy Mobile Ads SDK.** Google's page marks `play-services-ads` as in
maintenance mode and points new apps at its "next-gen" SDK. Phase one uses the legacy
one (25.5.0) because its API is the documented, stable one; the ads code is one small
package. Revisit when live ids are wired (ADR-006).

**N-22 The report setting is one write, and the job is scheduled after it.** Switching
reports on used to write the frequency and the "already reported" key separately, and
schedule the job at once; the job could have run between the two and sent a report for
a week the user had not asked about. Found while writing the report check, before it
was ever seen to happen.

**N-23 What is proven, as of the end of 2 October.** The six done-line checks passed
on the emulator in one recorded run. A re-run on the final build passed the first
five and was stopped during the sixth; four further checks (scope, repeat caller,
milestone, weekly report) have never finished a run. `FEATURES.md` marks those rows
`built`, not `done`, and `docs/verification/README.md` lists everything not yet
exercised on a device. The minified release build was smoke-tested by hand.

**N-24 The reference has a second name, and it is now on the never-ship list.** Its
listing is titled one way, but the description's body calls the app "Easy Call
Blocker" throughout. `ProductTextTest` and `play-policy-config.json` guarded only the
title and the developer; both now carry the second name too. Our working label, "Call
Blocker", is generic and matches neither, but it is the first two words of a live
app's title and two-thirds of that second name, so gate G3 needs a more distinctive
final title.

**N-25 What the study of the reference still lacks.** Asked before starting the next
section. A second, screen-free look at the phone and another read of the listing
(`docs/reference/notes.md`, session 1b) settled some things: it has no widget, tile,
shortcuts or links, so parity needs none; its report notification is silent and its
milestone one is not. They also turned up three things that were missing from the
inventory: the listing is translated into at least eleven languages (new row F-44,
not built), it has a first-run flow that was never seen, and the listing says "updated
on 2 Oct" while the copy studied dates from 21 Sep. Against the studio's teardown
playbook, the steps not done are the ones that need the founder: its switches flipped,
a live call, a fresh install, about a hundred reviews, the competitor set and search
suggestions. `TASKS.md` lists them with what each needs.

**N-26 The milestone notification stays silent.** The reference's makes a sound; ours
does not, like every notification this app posts. An app whose job is fewer
interruptions should not add one to congratulate itself. Recorded as a deliberate
difference on F-10.

## 2026-10-03

**N-27 A full design pass, at the founder's request.** Phase one was built on a light
pass (gate G4), which left the app in stock Material 3. The founder asked for a prompt
for Claude Design so he can start on UI, UX and mockups, so G4 is open again. The
brief is `design/CLAUDE_DESIGN_BRIEF.md`, written on the studio's brief template, and
the method is `knowledge-base/docs/05-design-workflow.md`. Three things in it are
Claude Code's own calls, for the founder to overrule:

- The three directions (Switchboard, Gate Register, Harbour Light). The founder has
  not named a look for this app, so they were derived from its job.
- Home carries no app name (founder taste §9: no self-branding inside the product).
  The build shows the placeholder name there today.
- One frame each for the Pro unlock, the India rules and Hindi, marked Later, so the
  layout does not need redoing in phase two. Nothing of phase two is built.

**N-28 The brief points at this repo, and the repo is public.** That is why Claude
Design can read it. It also means `docs/reference/notes.md`, which names the app
this one rebuilds, is public too. The brief never names that app and puts
`docs/reference/` out of bounds. Whether the repo should stay public is the
founder's call.

**N-29 (deprecated on 4 Oct 2026 by N-30) Mobbin is for behaviour, never for the
look.** The founder asked for Mobbin
to improve the design. A Mobbin connector exists (search screens, flows, sections)
and needs his account. The studio's brief template bans citing other apps on a
board; that stays true for the look, and Mobbin is allowed for what is on a screen
and how a flow moves. No app from this category is a reference, and the references
consulted are listed with what was taken from each. The same line the teardown
playbook draws: if our screen beside theirs reads as a repaint, it is redone.

## 2026-10-04

**N-30 Mobbin dropped. N-29 is deprecated.** The founder: "Now ignore mob and it's
not open source. Let's continue with Claude design." Mobbin's connector works only
on its paid plans (Mobbin's own documentation, read 4 Oct). The brief's Mobbin
section and its table of references are removed, so its later sections move up one
(Deliverables §6, Voice §7, Sample data §8). The studio's brief template applies as
written again: a board cites no other app.

**N-31 G4 resolved: Switchboard. Round 1 of the prototype is in the repo.** The founder
picked Switchboard on 4 October ("switchboard is good", "i dont like gate register one")
and asked for two changes, no lamp colours and type and a look that resemble the Uber
app; Claude Design records both under "The pick" in `design/prototype/README.md`. The
package was committed exactly as delivered, and its own check (`python3
design/prototype/build.py --check`) passes. Two things to know about it: `tools/` is a
Node project (Playwright) that checks the prototype, and nothing in it ships or counts as
an app dependency; the prototype embeds web subsets of Hanken Grotesk and Noto Sans
Devanagari, while the app will ship the full Hanken Grotesk family from its own
repository (SIL Open Font License), and the Devanagari face only once Hindi is built.

**N-32 The Switchboard screens, built from round 1.** The founder asked for the app to be
built from the prototype on 4 October ("build the app ... make it happen"), ahead of the
review rounds, his own approval on his phone and the handoff bundle, so the screens were
recreated in Compose from round 1 (`design/prototype/SPEC.md` is the spec). Checked on the
emulator at 360 dp, light and dark, 100% and 135% text (`docs/verification/screens/`), and
the ten emulator checks were re-run on the new screens. What was decided on the way:

- **Hanken Grotesk ships; Noto Sans Devanagari does not.** The full variable font comes
  from the family's own repository (SIL Open Font License; `docs/OFL-HankenGrotesk.txt`),
  144 KB. The Devanagari face waits for Hindi, so Licences lists one typeface, not the
  prototype's two.
- **The notification's one action is built.** The brief asked whether the stopped-call
  notification should carry an action; the design says yes, "Allow For 1 Hour", on an
  unlocked phone only. It goes through a receiver that is not exported, and the locked
  screen's public version carries neither the number nor the action. Not yet exercised on
  a device.
- **"Switched off in Android's settings" is told apart from "not asked yet"** by a stored
  flag set the first time the permission prompt is shown, plus Android's own
  "ask again" answer. On Android 12 and below, where there is no runtime permission, a
  disabled channel counts as switched off. The snackbars are gone, as the design asks.
- **A time is one word.** The space before AM or PM is a no-break space in `TimeText`, so
  "Paused until 10:39 AM." never breaks before "AM". Three time tests changed with it.
- **Nothing below the display moves.** The display window and the bay under the lever are
  laid out as tall as their tallest state (`ui/parts/Tallest.kt`), so throwing the lever,
  pausing or losing the role changes words and lamps, never positions.
- **The keyboard scrolls the field and its keys above itself; the ad slot stays put.** The
  activity resizes for the keyboard (`adjustResize`), and the screens' area has the slot's
  height taken out of its insets, so the keyboard inset a screen sees is only the part that
  covers the screen.
- **Reduced motion** follows the phone's animator duration scale (0 means off): every
  duration is then 0 and the haptic tick stays.
- **Weekday charts start the week where the phone's region starts it**, as before; the
  prototype shows Sunday first.
- **The verification script was adapted to the new flows**: pause is a key on Home, an
  allow entry is one key on the number's sheet, repeat callers is one strip of keys. It
  also now closes Android's own "viewing full screen" notice, which held the focus over the
  first test interstitial and stopped Back from closing it.
- **The emulator is 780 dp tall**, not the brief's 800, so Home scrolls by about 20 dp at
  100% text there; the lever and the display stay above the fold.

**N-33 The founder's phone screens with our build.** On 4 October, after the Switchboard
build merged, the founder had his phone on USB and asked for the install. The install
kept the data of an earlier build that was already on the phone (lever at Block, summaries
monthly), and the call-screening role stayed with the app; the phone had reported the
reference app as the holder before the install. Told plainly, he chose: "keep ours
screening, leave it". What follows from it: a regression on Home or in screening costs
him real calls, so every build goes through the ten emulator checks before it goes near
the phone, nothing is installed there without his ask, and the role and the phone's
settings are his to change.

**N-34 A tutorial section is owed.** After the build, the founder set a rule for every app:
"every app we build MUST have a short tutorial section that even a 5 year old can
understand" (falcon, `FOUNDER_TASTE.md` §13). This app has none yet. It is a surface the
user opens, not a welcome tour (the brief's "no welcome screen or tour" stands), so it goes
to Claude Design as a review note for round 2 and is built once it is drawn in the
Switchboard look. `TASKS.md` carries it as the first open item.

**N-35 Different on purpose: Play's Repetitive Content rule, and India's number series.**
On 4 October the founder forwarded an analysis of Google Play's Repetitive Content rule
(the Spam policy: "We don't allow apps that merely provide the same experience as other
apps already on Google Play"; one strike can reach every app on the studio's account) and
asked for it to be ensured here. He called it a policy to bypass; there is no bypass, only
being different in ways a user can name, which is what the studio's teardown method always
meant. The studio's checks live in falcon and the app's copy is
`docs/play-repetitive-content.md` (merged as proposed in PR #14, corrected the same day).
What checking the proposal against this repo and against TRAI changed:

- **Two of the three proposed differences were parity.** The repeat-caller pass is a
  feature of the reference app (F-13, seen on its Options screen), and so are the
  international scope (F-11) and the weekly summary (F-09) that were listed as later
  improvements. The real differences are India's number rules and a reason on every
  handled call with Always Block; the third is the founder's pick (G10). The file's own
  rule applied: stop and ask when the model turns out to have a proposed difference.
- **"1600 series" is the 160 series.** TRAI's 1600 numbers belong to banks, insurers and
  other regulated financial entities and to government bodies; 1601 was added for
  utilities, couriers and logistics. The rule matches the prefix 160 and covers both, as
  the brief and the knowledge base had said since 2 October.
- **TRAI changed the shape of the 140 rule.** The proposal had both rules on by default
  with a switch each. TRAI's third amendment to the TCCCPR (18 September 2026, press
  release 119/2026) prohibits call-management apps from blanket blocking, filtering or
  tagging calls from the 1600, 1601 and 140 series, "since such tagging … risks mislabeling
  genuine commercial communications and government communications as spam", and adds that
  "individual consumers retain full freedom to block, or filter calls on their own
  devices". Its clarification of 10 July 2026 (91/2026) says "any tagging, blocking or
  filtering of the calls originating from 1600 series numbers is not permitted", and that
  customers block 140 calls through the DND registry. So: the 160 rule is always on and has
  no switch (a switch would only let the app break the rule, and a blocker that rejects
  every unknown number is already blocking the bank's call); the 140 rule is off as
  installed and is the user's own switch in Options, built from the prototype's Later frame
  as drawn. The 160 switch that frame also drew was not built; a statement replaces it
  (review note for round 2). No screen says "spam": a stopped call is labelled with the rule
  that stopped it.
- **Where the series sit in the list.** After the user's own choices (contact, pause, allow
  list) and before the automatic passes (repeat caller, international scope). A 140 number
  the user allowed rings; one that calls during a pause rings; neither "international only"
  nor the repeat-caller pass lets one through once the user asked for 140 calls to be
  blocked. The invariant "only the last rule can block" became "the 140 rule and the last";
  its test changed with it.
- **Every test can fail.** The engine was broken twice on purpose (the series test reading
  the end of the key instead of its start; the 160 row left out): four tests failed each
  time, and passed again once restored.
- **Sources and their grades.** Google's policy page, read today (A). TRAI's series and
  deadlines: the government press bureau's releases of 12 Feb 2025, 19 Nov 2025 and 17 Dec
  2025, read today (A). The July clarification and the September amendment: TRAI's own
  press releases 91/2026 and 119/2026, read today from their PDFs (A); MediaNama's report of
  19 Sep 2026 (B) was the lead. The amendment's enforcement timeline (the blanket-blocking
  ban 30 days after notification) comes from that report, not from TRAI's text.
- **Not seen yet.** How a 1600 or 140 call arrives on an Indian SIM. The engine reads
  "1401234567", "01401234567" and "+91 140 123 4567" into one key, so the rule holds for
  every form a network is likely to send. One real call of each on the founder's phone
  would close this.
- **The merge rule met a document that was wrong in places.** PR #14 recorded the
  proposal as it stood and was merged under the standing rule; this change corrects it in
  the same hour. The record keeps both.

**N-36 The Quick Settings tile, the third difference.** The founder picked it on 4 October
("go with the quick settings tile") from the candidates the reference app lacks (N-35). What
was decided on the way:

- **One tap, one hour.** The tile is lit while calls are filtered; a tap pauses filtering
  for an hour, the length of Home's second key, and the next tap resumes it. Home shows the
  same pause (Resume, "Paused until …"), because both read the one setting. A dialog with
  Home's four lengths is one step away and is a review note for round 2.
- **When nothing is filtered, a tap opens the app** instead: the lever at Off, the role
  lost to another app, or a device that cannot screen. The tile's second line says which,
  in Home's order of states, so the tile never pretends to filter.
- **The tile's words are the only new copy** and Android draws the rest. They go to
  Claude Design with the next notes.
- **The service runs only while the tile is on screen.** Android binds it then; it reads
  the settings flow and writes one value. Exported with the system's own permission, like
  the screening service; the manifest test now expects exactly three exported components.
- **Checked the way a user taps it.** The emulator check adds the tile and taps it through
  the status bar's own shell command (`cmd statusbar add-tile`, `click-tile`), then reads
  Home and makes a call: paused after one tap, filtering again after the next.

**N-37 The second install on the founder's phone.** On 4 October, after the series rules
merged, the founder asked for the build on his phone ("install it on my phone"). Read
before: the earlier build of that morning, the app holding the call-screening role, the
phone awake and unlocked. `adb install -r` then sat for nine minutes: his OnePlus puts its
own install-scan screen over an install from a computer and waits for him to confirm it,
which he did ("confirmed the install on the phone"). Read after: installed at 17:31, the
role still ours, a cold start of about half a second, no crash; the data and the lever
stayed as they were, as `-r` keeps them. Nothing else on the phone was touched. The build
on the phone was the one with the series rules and the Options switch (`1c9fbf0`). The
tile build (`56313b3`) followed at 18:13 the same way, at his ask ("install the tile build
on my phone too"): the scan screen again, his confirmation, the role still ours, a cold
start of about half a second, no crash. His phone now runs what main holds.
