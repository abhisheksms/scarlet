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
start of about half a second, no crash. The build with checks 13 to 17 (`5b67da2`, which
differs from the tile build only by one test tag) followed at 19:07, again at his ask and
again through the scan screen; the role stayed ours, the cold start was clean. His phone
runs what main holds. At his word
("you figure out") the tile was then added to his Quick Settings over adb
(`cmd statusbar add-tile`), as the last of his tiles; Android's own editor, or
`cmd statusbar remove-tile`, takes it out again. The tile was not tapped: a tap would have
paused his screening for an hour. The guard rail refused two reads of his phone as personal
data, the app's own log (to see how 140 and 1600 numbers arrive) and a capture of his
panel, so both stay with him: a 140 or 1600 number in History shows the form it arrived in.

**N-38 Five more checks, for what had only been built.** With both installs done the founder
said "you figure out", read as: close the loose ends without him. The verification README
listed what had been built but never exercised on a device; five scripted checks now cover
most of it (13 to 17). What was learnt:

- **The shade can be driven without a word of copy.** Its clear-all button, a notification
  row's expand button and the row's first action all have Android's own resource ids. The
  action's label is read from the app's strings file and compared, so the script still
  pins no copy of its own.
- **Android bundles several notifications from one app** and hides a child's actions
  until the bundle and then the child are expanded. The first version of check 16 failed
  on that alone: the action was attached (the notification manager said `actions=1`) but
  not on screen. The check now clears the shade before it posts the one notification it
  looks at.
- **A locked screen hides the number only behind a secure lock.** With a swipe lock Android
  shows the full notification, so the check sets a PIN on the emulator, locks, reads the
  screen, and clears the PIN again.
- **`--only N`** re-runs one check on the app as it is, with no wipe and no report, which
  turned a twenty-five-minute cycle into a two-minute one while check 16 was being fixed.
  The recorded report always comes from a full run.
- **One tag was added for testability**: the allow row's remove button in Options
  (`remove-allowed`). Nothing else in the app changed.

**N-39 The first day on the founder's phone.** Two things came back, on 4 and 5 October.

- **A real result.** The founder tried the build while using a food-delivery app and reported
  that it "beautifully worked": it "blocked all the calls from unknown numbers". These are the
  first real calls on a real phone, on OxygenOS, which the emulator could not give; the risk
  register's OEM item (PLAN risk 6) is settled for Block. It is in his words and not seen: his
  phone's log and screens are out of reach (the guard rail refused those reads on 4 Oct), so
  which calls they were, and whether one was a delivery partner's, are unrecorded. A delivery
  partner is an unknown number like any other, so Block stops them too. The ways through are the
  Quick Settings tile (one tap pauses for an hour), Allow For 1 Hour on the stopped-call
  notification, and the repeat-caller keys in Options.
- **A first reaction, and a decision.** On 4 Oct: "The app feels very unnatural. I don't know
  why. For a newbie, it would feel totally a person won't even won't be able to understand."
  Claude's reading, kept for the next round and not adopted: the lever has no equivalent in any
  phone app, every label is in capitals, and the words are the app's own rather than a
  newcomer's. On 5 Oct he decided: "let's stick with this design", with new feedback to come.
  So G4 stands and the lever, capitals and word changes were not logged as round-2 notes. The
  tutorial section (`TASKS.md`, open item 0) is still owed and is the planned answer for a
  newcomer.

**N-40 The founder's feedback notes of 4 and 5 October, and what was done about each.** On
5 October the founder handed over the notes he had kept while using the build ("i had these
notes saved as founder feedback, can you act on them"). His words, the reading taken, and the
change:

- **"People have to learn, tutorial section at the start and a feature for tutorial."** Built:
  How It Works (`ui/HowItWorksScreen.kt`). It opens by itself on a launch until it has been
  closed once (its back arrow, the system's Back or its Done key: one press each), never in the
  way of a screen a notification asked for, and again from the first row of Settings. This is the
  form he asked of the first app on 4 Oct (falcon, `FOUNDER_TASTE.md` §13), so for this app it
  replaces N-34's "a surface the user opens, never at launch". It was built ahead of Claude
  Design, from the Switchboard parts as they are: sections under engraved rules, and a plate
  like the lever's own with the three stops in the lever's type. It says who always rings
  (contacts, the allow list, the 1600 series, callers who hide their number), what each stop
  does to every other call, that Android asks once for the screening role, how to pause for a
  delivery, and that stopped calls are in History. The line about hidden numbers ("This app
  cannot see those calls.") is Android's documented behaviour (ADR-002, which says it should be
  said plainly); it has not been seen on a device, and the emulator's console has no way to
  place a call with a withheld number.
- **"What do you mean - let every call ring for?"** The caption over Home's four keys is now
  "Pause for". The brief's own words were "Pause For"; round 1 of the prototype changed them
  (`design/prototype/HANDBACK.md`).
- **"No account? What? Remove this sentence."** Read as the one sentence "No account.": with
  the lever at Off, Home now says "Your contacts are never read. Nothing about a call leaves
  this phone." The whole line was round 1's addition. If he meant all of it, it is one string.
- **"Web not working. Hide the privacy policies do we need all these?"**, with the licences
  screen and the privacy page answering 404. The studio's site works; this app's privacy page
  has not been written, it is a launch task. A row that opens a missing page is a dead control,
  so three rows are hidden until their pages exist, behind two switches in `ui/Links.kt`:
  Privacy Policy (`PRIVACY_PAGE_LIVE`), and Share App and Rate App (`STORE_PAGE_LIVE`). Shared
  statistics carry no store link until then either. Whether they are needed: the privacy link,
  yes, Google Play requires it inside the app; the licences, yes, the Apache licence and the
  typeface's licence require their notices to ship with it; Contact, Share App and Rate App are
  parity rows he may cut (G5). What is shown sits one level down: Settings has one About row,
  and About holds the licences and Contact, and the privacy policy once it is live.
  `LaunchGateTest` refuses live ad ids while either switch is off.
- **"Summary notif top section needs a rewrite maybe move it somewhere else."** Rewritten: the
  three keys come first, and one sentence under them says what the chosen key does ("Every
  Monday, one notification tells you how many calls were stopped last week."). It stays in
  Settings, under How It Works. The other natural home was Statistics, and leaving Statistics
  can show a full-screen ad: a setting should not sit behind one.
- **"Need to research for some premium features, some which are really helpful maybe move some
  there and also remove ads in premium."** `docs/premium-research.md`, and a gate for his pick
  (`PLAN.md` G11). Nothing of the purchase is built.
- **"The design needs to be more intuitive."** Written on 4 Oct, before "let's stick with this
  design" on 5 Oct (N-39). Taken as the sum of the notes above; the direction is unchanged.
- **"History section looks slick."** Nothing changed there. It is the screen to hold the
  others to.
- Two more lines were ideas for other products, not feedback on this app. They are kept
  outside this repo.

What goes back to Claude Design for round 2: How It Works and About as built, Settings' new
order, and the three wordings ("Pause for", the two-sentence privacy line, the summary's
sentences). Seen on the emulator at 360 dp, light at 100% text and dark at 135%. Emulator
check 18 covers the first launch, the one-press close, the launch after it and the replay from
Settings; check 13 now also holds that a hidden row is not on screen.

**N-41 The fourth install on the founder's phone, and the tutorial there.** On 5 October he
handed over his notes with the phone on USB ("i have the phone tethered via usb debugging"),
so the merged build went on it once all eighteen emulator checks had passed on that same file.
Before: the build installed on 4 Oct at 19:07, and the app holding the call-screening role.
The install returned in nine seconds; whether the phone's install-scan screen appeared and was
confirmed at once, or did not appear, is not known. After: installed on 5 Oct at 18:44, the
role still with the app, its data kept. The app was then opened once over adb to see it start:
it came to the front with no crash, and the screen showing was How It Works, read by its tag
alone and not by its content, with Home not under it. It was left open for him; it counts as
seen only when he closes it. So an install over an older build shows the tutorial once, which
is what "at the start" asks for. Nothing on the phone was tapped or changed, and Home, Settings
and About on his phone are his to look at: they were not read.

**N-42 Freemium: the Automatic section, the timer, the schedule and three plans.** On the
evening of 5 October the founder asked, in a dictated message: "Maybe add a new section apart
from office silence block to distinguish from competitors. Add 2 premimum features: To block
during certain hours of the week. And to office silence or block for the next few mins hours.
Make it freemium, think of some marketing model, maybe 2 tiers, think of more such features and
add them". Read as: "office" is "Off"; a new section on Home beside the lever; two premium
features, a weekly schedule and a timer that holds Off, Silence or Block for a while; a free app
with paid plans, perhaps two of them; and more features of that kind, built. It answers gate
G11. What was decided on the way, all of it reversible:

- **The lever now shows the mode in effect.** Until now the handle stood where the user left it
  and a pause was told by its lamp. With a schedule that cannot work: an hour set to Off while
  the lever stands at Block would leave the handle on Block, and a tap on Block would then do
  nothing. So the handle stands at whatever is in effect, a timer or the schedule can put it
  there, the lamp is a ring while they hold it, and the display's second line says until when
  and what follows ("Until 8:25 PM. Then Off."). A pause now moves the handle to Off. This
  changes a state the prototype drew.
- **A pause is the timer's free part.** The four keys under the lever stay free and stay where
  they are. The Pro timer adds Silence and Block as things to switch to for a while.
- **Moving the lever inside the schedule's hours** holds until those hours end and leaves the
  lever's own stop alone: tonight is overridden, tomorrow night the schedule is back. Outside
  the schedule's hours a move sets the lever's own stop, as before.
- **The schedule is a grid of the week's hours**, set by dragging across a day, not a list of
  time ranges with pickers: seven rows of twenty-four, and a top row that sets every day at
  once. It is the hour chart's idiom turned into a control. Whole hours only.
- **Three plans, each bought once: Free, No Ads, Pro.** He said "maybe 2 tiers"; two paid plans
  is the reading taken. No Ads is the cheap one two reviewers of the modelled app asked for;
  Pro holds No Ads and the features. No subscription (`docs/premium-research.md`). The studio's
  taste doc said "no tiers"; this is his call to change, recorded there.
- **Nothing can be bought yet.** The plans screen says "Not on sale in this build." A purchase
  needs the app in Play Console, which waits on the name and the package (G3). A test build
  has three keys on the plans screen to try each plan; a release build has none.
- **What a plan may use is decided in one place** (`core/plans/Plans.kt`), and the settings
  are cut down to the user's plan as they are read, so the screening service, Home and the tile
  cannot disagree. A schedule stored on Pro stays stored if Pro goes, and runs again if it
  comes back.

One mistake worth keeping: the week grid's touch code is started at a row's first touch and
then lives as long as the screen. It kept calling the callbacks it was given then, which
carried the schedule as it stood then, so a second drag on a row undid whatever had been set
on other rows in between. Caught by tapping the same hour twice on the emulator; the callbacks
are now read fresh each time (`rememberUpdatedState`).

**N-43 Call-backs: a number the user called rings when it calls back.** The first of the "more
such features" the founder asked for on 5 Oct 2026, and free. Decisions, all recorded in ADR-007:

- **Why this one.** The promise is that the app never costs a call that mattered, and the
  unknown call a person most wants is the one they asked for: they rang the clinic, the
  courier or a support line, and it rings back. Until now only the pause covered it.
- **No permission.** Android shows the chosen screening app each outgoing call to a number
  outside the contacts. Read on the reference page for `CallScreeningService` and in Telecom's
  own source, then seen on the emulator. The call log is still never read (ADR-003).
- **One switch, on as installed**, in Options: Call-backs. No choice of lengths: 24 hours,
  one value in the settings. A row of keys can come if anyone asks.
- **What is kept**: the number's key and the time, for the day it can still ring back, on the
  phone only. Nothing while the switch is off; switching it off deletes what was kept. No
  screen lists these numbers.
- **The words.** "Call-backs" on the strip and "Call-backs ring" in Home's summary of Options,
  beside "Repeat callers ring". With the rule on as installed, that summary no longer reads
  "No exceptions" on a new install.
- **How It Works** gains one line under Always Ring, "Numbers you called in the last 24
  hours.", shown only while the switch is on, so the tutorial never says what is not so.
- **The database moved to version 2**, its first change. Room writes the step from the two
  exported schema files. Seen on the emulator: this build installed over a version 1 database
  that held six stopped calls and two allow entries, all still there afterwards.

Two things learnt while checking it. Right after another call has ended, Android's ringer can
start late: Telecom holds it until its Bluetooth call service is bound again, for any caller.
So emulator check 22 goes by Telecom's record that the call passed every filter and was set
ringing, and reports the ringer beside it (`docs/verification/README.md` has the timings). And
an emergency call cannot be ended by the end-call key
from a script; on the emulator it was ended from its own notification. That call was placed on
the emulator only, to learn two things about Android: the app is shown an outgoing emergency
call like any other, and for the minutes of emergency callback mode afterwards Telecom skips
call filtering altogether, so the app is not even asked (its log line, read that evening:
"Skipping call filtering ... (ecm=true ...)").

**N-44 After a call to an emergency number, the app pauses itself for a day.** Found while
checking N-43, and built because it is the app's first promise: it never costs a call that
mattered. The call back after an emergency comes from a number nobody has saved. ADR-008 has
the decision and the sources; in short:

- Android lets every call through only while the phone is in emergency callback mode, a few
  minutes where a network has it at all. Apple's Phone app turns its own screening off for 24
  hours after such a call. This app now does the same, as an ordinary pause.
- It needs no permission. Android shows a screening app the outgoing call, and
  `TelephonyManager.isEmergencyNumber` says what counts as an emergency number on this phone.
- No switch. Home shows the pause and its end time, with Resume. Home does not say why; that is
  a note for the next design round. How It Works has the sentence.
- With the lever at Off and nothing else filtering, nothing changes.
- The number of an emergency call is not kept for the call-back rule: nobody is called back
  from the number they dialled.

It can never be tried on a real phone. Emulator check 23 puts a made-up number on Android's own
test list of emergency numbers and calls it from Android's dialer. Two things learnt: Android
will not place an emergency call for another app (the request only opens the dialer with the
number in it), and after any such call the emulator stays in emergency callback mode past the
five minutes it announces, where Android asks no screening app about any call, until the
emulator is restarted. The check restarts it.

**N-45 Number rules: always ring or always block numbers by how they start (Pro).** The third
Pro feature, and the one the premium research had recommended as the second before the founder
chose the timer. Decisions:

- **One idea: how a number starts.** A country code, an office's shared first digits, a call
  centre's range. Two keys: Always Ring, Always Block. No wildcards, no patterns in the middle
  of a number, no "ends with": every extra kind of rule is a thing to explain.
- **The longest start wins**, and India's two series are in the same contest. So "+92: block"
  with "+92 21: ring" does what it says in either order, and a careless "+91: block" cannot
  take the bank's 1600 call with it. Someone who types the 160 series itself and presses
  Always Block gets it: that is the consumer's own freedom under TRAI's amendment, used on
  purpose (`knowledge-base/docs/02-rules-engine.md`).
- **Typed like a number.** "080 4567" on an Indian phone is +91 80 4567; "0092" is +92. A rule
  is shown spaced the way a number is while it is being typed, and the caption over the two
  keys says the start as the app reads it, country code and all ("Numbers that start +91 80
  4567"), before a key is pressed.
- **In Options, last on the screen**, built from the allow list's own parts: rows with a remove
  button, the recessed field, two keys over the keyboard. Without Pro it is one row with the
  word Pro that opens the plans, under everything that is free.
- **Kept in the settings as one line of text**, like the schedule, and cut to an empty list for
  any plan but Pro as the settings are read. The stored list is never touched by that.
- **Off means off.** A rule that blocks does not block at Off or during a pause: Home's
  sentence, "Every call rings.", stays true.

One thing a deliberate break found: no test told "starts with" from "has these digits
somewhere". One was added (+91 79180 12345 does not start with +91 80).

**N-46 Buying a plan goes through Google Play.** The last piece of "make it freemium". ADR-009
has the design; the choices worth a note:

- **Google Play is asked, not trusted from memory.** Each time the app comes to the front it
  asks what the user owns, and that answer sets the tier. The purchase screen's own result is
  not used for that: it lists only what was just bought, so someone with No Ads who started a
  payment for the upgrade that is still waiting would have looked like someone with nothing.
- **A failure changes nothing.** No answer from Google Play leaves the tier as it was.
- **Three products, one of them an upgrade.** `pro_upgrade` is Pro for someone who has No Ads,
  at the difference. The plans screen offers each person only the product for their own step.
- **The price is Google Play's own text**, and there is no key to buy until there is a price.
  Without one the plate says which of three things is so: Google Play is being asked, it could
  not be reached, or nothing is on sale in this build.
- **Version 9.1.0 of Google's library**, the one the first app ships. It adds one permission,
  which only lets the app talk to Google Play's purchase service.
- **What could not be done**: a purchase. That needs the app and its three products in Play
  Console and a copy installed from Play. On the emulator, which has no Google account, the
  screen says Google Play could not be reached, which is so. The screen as it is with prices was
  looked at once with made-up prices put in by a change that was thrown away.

One thing learnt: a build made right after a thrown-away change was not byte for byte the build
made before it, though every file inside was the same. A clean build was. So "the same build as
the one tested" is checked against a clean build.

**N-47 The record after the round.** Late on 5 Oct 2026, with everything above merged: all
twenty-four emulator checks passed in one run from a wiped app, on a clean build of `main` at
commit `48d5f7b` (`docs/verification/emulator-2026-10-05.md`); the screens the round changed
were retaken and eight new ones added, with made-up numbers; the contact sheet was made again.
Nothing of this round is on the founder's phone: it still runs the build of 18:44 that day, and
an install there waits for his ask. The next install takes the database from version 1 to 2,
which has been seen working on the emulator over a database with rows in it.

**N-48 The fifth install on the founder's phone: the freemium build.** Late on 5 October he
plugged the phone in and asked for it ("phone is plugged in, install it"). The file installed is
the one all twenty-four emulator checks had passed on 37 minutes earlier, compared byte for byte
with a clean build of `main`. Before: the build of 18:44 that day, the app holding the
call-screening role. The install returned in 16 seconds. After: updated at 23:59:46, the role
still with the app. The app was opened once over adb, with the phone unlocked, to see it start:
it came to the front with no crash and showed Home, read by its tags alone, and its process was
still up eleven seconds later. Home reads the stopped calls as it opens, so the database's first
change of version ran on his own data. Nothing on the phone was tapped, changed or read beyond
those tags.

Two things for the next install. At first adb listed no device although the Mac saw the phone
on USB: it was offering file transfer only, with no debugging interface, until USB debugging
was on again on the phone. `ioreg -p IOUSB` shows the phone, and
`ioreg -c IOUSBHostInterface -l` shows whether an "ADB Interface" is among what it offers,
which tells a cable problem from a setting on the phone in two commands. And what is new on his
phone with this build, for him to know: Call-backs is on as installed, so a number he calls can
ring back for a day; a call to an emergency number pauses the app for a day; the plans screen is
there, with a test build's keys to try each plan, and says Google Play has nothing on sale.

**N-49 The founder's first two notes on the freemium build.** Minutes after it went on his
phone he sent a screenshot of Home, paused, and wrote: "Instead of resume, rename to resume
blocks or something, it s confusing and add pricing".

- **The key that ends a pause says what it brings back.** Since N-42 the lever stands at Off
  during a pause, so the screen showed Off, "Paused until 04:03", and a key that said only
  Resume: nothing said what would resume. The key now reads Resume Blocking or Resume Silencing,
  by what ending the pause brings back at that moment (`ModeClock.afterEndingTimer`: the
  schedule's hour if it has set one, else the lever's own stop), and End Pause in the one case
  where that is Off. His words were "resume blocks"; Blocking is the app's word for the mode.
  The key's tag is unchanged, so no check was touched.
- **Pricing.** Read as: put the prices on the plans screen, at the numbers recommended, since
  he named no others (`PLAN.md` G12). A price really lives in Play Console, and the studio's
  rule is that the store's own price is the one shown. So a test build, which Google Play
  sells nothing to, now shows the planned prices on the plates and says that is what they are
  ("₹99, once. The planned price. This test build cannot buy."); a build from Google Play
  shows Google Play's price or none. One function decides which (`billing/PriceLine.kt`), and
  a test holds that a build from Google Play never shows a planned price. Another test holds
  that the upgrade's planned price is the difference between the two plans'.

Seen on the emulator at 360 dp, in dark with large text as on his phone and in light: the key
on one line in both wordings, and the plans on Free and on No Ads. Checks 1, 6, 12 and 21
passed on the build before it merged.

**N-50 The sixth install on the founder's phone: his two notes acted on.** On 6 October he
answered the report of N-49 with "install it". The full run of twenty-four emulator checks on
that build was still going, so the install waited for it: all twenty-four passed at 01:03, on
a clean build of `main` (`docs/verification/emulator-2026-10-06.md`), and the same file went
on the phone at 01:04. Before: the build of 23:59, the app holding the call-screening role.
After: updated at 01:04:08, the role still with the app. Opened once over adb: Home, no crash.
The pause he had started before midnight was still running, and its key read "Resume
Blocking" on his own phone, which is the change he asked for, seen where he asked for it. Only
that label and the app's tags were read. The plans screen was not opened there.

**N-51 The founder's third note on that build: "Slider is not smooth".** 6 October, the
morning after N-50. Read as the lever on Home, the one control that slides under a finger.
Before anything was changed its handle was measured on the emulator, by writing down where
it was placed in every frame (`docs/verification/lever-handle-2026-10-06.md`). Four
faults, all in how the handle moved and none in what the lever does:

- **It jumped when let go.** The handle had two positions: the finger's while it was
  dragged, the stored mode's otherwise. Let go, it went straight to the stored mode, which
  was still the old one for the few hundredths of a second the store takes, and then
  travelled the whole way again. Carried from Off to Block and let go on Block, it jumped
  116 dp back to Off and came down a second time. Let go short of half-way, it jumped back
  to its stop with no travel at all.
- **It trailed the finger by 8 dp**, Android's allowance before a touch counts as a drag,
  and stood still until then.
- **A flick did nothing** unless it carried the handle past half-way.
- **The whole lever was composed again in every frame** the handle moved, because the
  handle's position was read while composing.

What it does now (`ui/parts/Lever.kt`, `ui/parts/LeverHandle.kt`):

- The handle has one position, counted in stops. The finger writes it and so does the
  handle's own travel, and it is read only where the handle is placed.
- It follows the finger from the first pixel, and can be taken hold of anywhere in its own
  cell of the row, 72 dp wide and as tall as the row. The handle alone is 46 by 36 dp,
  smaller than the 48 dp Android asks of a touch target.
- Let go, it glides from where it is to the nearest stop. Flicked, it goes on to the next
  stop that way, unless it had moved less than 0.15 of a row: a finger that only twitched on
  the handle changes nothing. A flick is 400 dp a second or faster, the figure Material uses
  for its drawer; its sheets use 125, and a lever that decides whether calls ring should be
  the harder of the two to move by accident.
- It stands at the chosen stop at once, and the stored mode catches up. Home tells the lever
  whether the stop was taken, which it is not when Android must first be asked for the
  role. If the stored mode has not followed within a second the handle goes back to it, so
  the lever cannot go on showing a mode that is not in effect. The lamp of the stop it left
  goes dark at once; the new lamp still waits for the stored mode.
- The tick comes when the handle seats, from the lever itself. It used to come from Home,
  180 ms after the tap, wherever the handle was.

**A deviation from the prototype's spec.** `lever.travel` there is 180 ms on
cubic-bezier(0.2, 0, 0, 1). The travel is now a spring with no bounce that seats in the same
180 ms (`Motion.leverTravel`). A fixed curve starts from rest every time, so a handle let
go mid-flick would stop dead and start again; a spring starts from the speed the handle
already has. At the same length it is also the gentler: on a screen drawing 60 frames a
second the curve moved the handle up to 41 dp in one frame, more than its own height, and
the spring moves it 29.

Not changed: what a lever move does (`LeverMoves`), the rows' tags, a tap on a row, and the
locked lever, which gives 7 dp, now under a finger as well as on a tap.

Proven by `LeverHandleTest` (5; the rule broken eleven ways, each caught) and emulator
check 25 (broken four ways, each caught; it also passes with the phone's animations off).
By hand on the emulator: without the role a drag or a tap asks Android, the handle goes back
if the user refuses and stands at the stop if they accept; a locked lever gives 7 dp and no
more; a store slowed by a second and a half sends the handle back after one second. Not
proven: how it feels under a real finger, and on his phone.

**N-52 Found on the way: the hour chart did not follow a finger either.** Looking for every
part that moves under a finger (N-51) turned up the hour chart on Statistics. The design
says "Touch or drag anywhere on it to read an hour". A touch read an hour; a drag read
nothing more. Finger down on 2 AM and carried to 3 PM, the line beside the heading still
said 2 AM. The cause: the touch was started again each time the chosen hour changed, and a
new start waits for a new finger. `FEATURES.md` had said it in its own way since 4 October:
"Touching the chart was not exercised".

Two things changed in `HourChart` (`ui/parts/Charts.kt`):

- The touch is started once, and reads the chosen hour as it goes.
- A finger that moves sideways reads the hours it passes. A finger that moves up or down
  is scrolling the page: the chart lets it go and reads nothing, and the hour it touched
  first is put back to what was chosen before. Until now a finger that landed on the chart
  could not scroll the page at all. This is a small step away from the design's "drag
  anywhere".

Both drags the app had were on the list of things not yet exercised on a device, and both
were broken. The rule this leaves, which goes to falcon: a gesture the design names is
tried on a device before the build is handed over.

Emulator check 26 is new. On Statistics it taps a day, changes the period and puts it back,
puts a finger on the hour chart and moves it sideways, lifts, taps the hour again, and
swipes down from the chart. So the day chart's tap and the period keys, on that same list,
are exercised now, and both worked. Each day and each hour carries a tag for it (`day-0`
to `day-6`, `hour-0` to `hour-23`).

Seen and not changed: a sheet shows a grip at its top edge, as the prototype draws it, and
cannot be dragged down. It leaves on Back, on a tap outside it, or by its own keys. That is
the founder's to decide (`TASKS.md`, open item 14).

**N-53 The seventh install on the founder's phone, and two checks that waited for the
wrong thing.** 9 October, just after midnight. The work of N-51 and N-52 had stopped on
the 6th when the Mac slept; both fixes were now merged (pull requests 34 and 35) and the
full run of twenty-six checks was on its seventh when he wrote: "I mean, there is USB
debugging as well. Am I phone is tethered? Why do I need an emulator?" The answer given:
he does not; the checks do, because they place fake calls, wipe the app's data, move the
clock and restart the device, and none of that can be done on the phone that screens his
real calls. Offered the install at once or after the run, he chose at once. The changed
parts' own checks had passed before the merge, and the code that answers calls was not
touched.

- **The install.** Before: the build of 6 Oct, 01:04, the app holding the call-screening
  role. After: updated at 00:53:17, in 14 seconds, the role still with the app. Opened once
  over adb: Home, no crash, the new handle's tag on screen.
- **How the phone draws it.** With the phone on the cable the app's own frame counter can
  be read (`dumpsys gfxinfo`: frame times, nothing of his). Set to zero after the install
  and read when he had used the app for a while: 5,639 frames, 173 of them late (3.07%),
  half drawn within 12 ms, nine in ten within 16 ms, one in a hundred over 27 ms. That is a
  debug build; a release build draws faster. His own word on how the lever feels has not
  come yet.
- **The first run failed on check 13, and the check was at fault.** 25 of 26 passed. After
  a tap on Contact the window in front was still the app's, and it happened again in two of
  the next four tries. Android's log settled it: the app had started the mail app's compose
  screen (`START ... act=android.intent.action.SENDTO ... result code=2`) and the mail
  app's task closed 0.14 seconds later. The emulator has no account, and its mail app
  sometimes closes itself when opened. The check went by the window in front a few seconds
  later. It now goes by Android's record that it started an expected target for the app's
  own uid, and writes the window in front beside it. Broken two ways on purpose it failed
  each time.
- **The second run stopped at check 23, and again the check was at fault.** Checks 1 to 22
  passed. After check 23's restart of the emulator the two calls drew no decision, and
  Telecom had no record of either: they were placed before the phone service was in
  service. The emulator had slept for three days and had just been restarted for the
  second time in an hour, and it was too busy to open the app in time for check 24, where
  the script stopped. The restart had waited a fixed 15 and then 10 seconds, which had
  always been enough. It now waits for the device to go away, for it to come back, for
  the phone service to be in service, and for Android to finish its start-up broadcasts.
- **The third run passed all twenty-six**, in 34 minutes, on the same file
  (`docs/verification/emulator-2026-10-09.md`).

Both faults are the one check 22's ringer taught on 5 October
(`docs/verification/README.md`): a check waits for the thing itself, and judges by the
platform's own record of it.

For the next time he is waiting with the phone on the cable: say which device is doing
what, and offer the install as soon as the changed parts have passed their own checks.
