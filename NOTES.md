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
