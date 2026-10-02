# Reference app: phone-session notes

Private research. These notes describe another developer's app as a user sees it.
They sit behind the firewall: nothing here is shipped, and no label, text, colour or
layout from it is copied into the product. `FEATURES.md` is the inventory in our own
words; the product follows `FEATURES.md` and the knowledge base, never this file.

| | |
|---|---|
| App | Block Unknown Callers, by Life Software Lab |
| Package | `com.lifesoftwarelab.android.incomingcallcontrol` |
| Version | 1.4.5 (versionCode 45), minSdk 29, targetSdk 36 |
| Installed | from Google Play, first installed 2026-07-22, updated 2026-09-21 |
| Phone | OnePlus 12 (CPH2573), OxygenOS 16.0.10.501, Android 16 (API 36) |
| Display | 360 dp wide, font scale 1.35, dark theme, 24-hour clock, locale en-GB, India |
| Session 1 | 2026-10-02, 17:22 to 17:36 IST, read-only |
| State found | holds the call-screening role; mode **Off**; per-call notifications off; report set to weekly; 156 calls blocked in total, 0 silenced |

## How it was studied

Over adb only: `am start`, `input tap/swipe/keyevent`, `screencap`, `uiautomator dump`,
and read-only `dumpsys` (package, role, notification channels). No APK was pulled,
listed or decompiled. `tools/refcap.py`, `tools/reftap.py`, `tools/reftap_number.py`
and `tools/refdump_masked.py` are the helpers.

Raw screenshots and UI dumps are in `docs/reference/raw/`, which is git-ignored: the
history and statistics screens show the real numbers that called the founder's phone.
One capture (the system share sheet, which lists the founder's own contacts as share
targets) was deleted on the spot.

Session 1 changed nothing on the phone: no toggle was flipped, the mode was not
touched, nothing was deleted or added.

## What the system says about it (read-only)

- **Role:** it is the holder of `android.app.role.CALL_SCREENING`. The default dialer
  is Google's Phone app.
- **Permissions requested:** INTERNET, ACCESS_NETWORK_STATE, POST_NOTIFICATIONS,
  WAKE_LOCK, RECEIVE_BOOT_COMPLETED, FOREGROUND_SERVICE, the advertising-ID and
  Privacy Sandbox ad permissions (AD_ID, ACCESS_ADSERVICES_AD_ID / ATTRIBUTION /
  TOPICS), and the install-referrer binding. **No contacts permission, no call-log
  permission, no phone-state permission.** WAKE_LOCK, RECEIVE_BOOT_COMPLETED and
  FOREGROUND_SERVICE are what WorkManager adds to any app that includes it.
- **POST_NOTIFICATIONS** shows as granted with the flag `GRANTED_BY_ROLE`: on this
  phone, holding the call-screening role granted the notification permission without a
  prompt.
- **Notification channels that exist:** one for the periodic report (low importance)
  and one for milestones (default importance). There is no per-call channel yet, which
  fits per-call notifications being off: it is probably created when that is turned on.
- **Cold start:** 181 ms to first frame (`am start -W`), process not running before.

## Screens

### S-01 Home

Top bar: the app name and a gear icon (opens Settings). The body scrolls.

1. A card with a three-way selector: off, silence, block. One is highlighted. Under
   it, one line describes the current state (for off: blocking is disabled and all
   calls ring; for block, per the listing screenshot: calls from numbers not stored in
   contacts are blocked).
2. A card with two rows: a switch for per-call notifications (with a one-line
   explanation), and a row that opens Advanced Settings (subtitle: filter scope,
   exceptions and more).
3. A card with two rows: one opens History; one opens Statistics and carries a live
   one-line summary: today's count, then the last seven days' blocked and silenced
   counts.
4. A large native ad (headline, media, call-to-action button). It loads after the
   screen appears.
5. Two actions side by side: Share (opens the system share sheet with the app's Play
   Store link) and Review (opens the app's page in the Play Store app; not the in-app
   review dialog).

### S-02 Settings (gear)

1. A card titled for the weekly / monthly report (a summary of calls blocked or
   silenced) with a three-way selector: off, weekly, monthly. Weekly was selected.
2. A large native ad, inserted **after** the screen has rendered. It pushes the rows
   below it down by about 980 px. A tap aimed at a row lands on the ad if it arrives
   as the ad does: this happened during the session (the Play install sheet for the
   advertised app opened; it was closed without installing).
3. Three rows: privacy policy, open-source licences, contact.

No version number is shown anywhere. No privacy-choices (consent) entry was visible
(India is outside the regions where the consent form is required).

### S-03 Open-source licences

A plain text page: a heading, then library groups with their licence (AndroidX: Core
KTX, AppCompat, Material Components, ConstraintLayout, Navigation, Lifecycle,
WorkManager, RecyclerView, Room; more below, not read).

### S-04 Advanced Settings

1. A "filter scope" card: a two-way selector, all or international numbers, and a
   caption saying it applies to numbers not saved in contacts.
2. A large native ad (same late insertion as Settings).
3. A card with two switches: pause filtering for a duration; allow repeated calls
   within a time.
4. A card with one switch: an allow list of numbers that are always let through.

All three switches were off, so what they reveal when on was not seen in session 1.

### S-05 History

Reached from Home. **A full-screen interstitial ad plays first** (a playable ad with a
close button after a few seconds, then an end card that needs a second close).

Top bar: back, the title, and a bin icon (delete all; not tapped). A large native ad is
pinned under the top bar; the list scrolls beneath it.

The list is grouped by calendar day: a card per day, headed by the date as
`yyyy-MM-dd` (the listing's screenshot shows "Today" for the current day), then one
row per handled call, newest first:

- the caller's number, formatted for reading (international format with the country
  code for the calls on this phone; the listing's US screenshot shows national
  format);
- the outcome (blocked or silenced) in an accent colour, then the time as `HH:mm` (the
  phone is on the 24-hour clock);
- a button that adds the number to the allow list (not tapped);
- a bin icon that deletes the row (not tapped).

Tapping or long-pressing a row does nothing.

### S-06 Statistics

Reached from Home. **A full-screen interstitial ad plays first** (video with a skip
button after about six seconds, then an end card with a close button).

Top bar: back, the title, a share icon. The body is one long scroll:

1. Milestone card: the latest milestone reached (100 calls handled), a progress bar,
   and the next goal (250).
2. Two tiles: total blocked, total silenced.
3. "Time saved" card: a duration and the number of interruptions avoided. 156 calls
   showed as about 1 hr 18 min, which is 30 seconds a call.
4. A large native ad.
5. Seven-day card: calls handled in the last 7 days, the busiest hour (as `HH:00`),
   and the change against the week before as a percentage with an arrow.
6. Thirty-day card: calls handled in the last 30 days and the change against the 30
   days before.
7. Chart, last 7 days: one bar per day, labelled by weekday, blocked and silenced
   stacked, with a legend. Tapping a bar selects it and a line under the chart gives
   that day's blocked, silenced and total. At font scale 1.35 the legend wraps one
   letter per line.
8. "Analysis period" chips: 7, 30 or 90 days (30 was selected). It governs the two
   charts below.
9. Chart by weekday (Sunday to Saturday) over the period, with a caption naming the
   busiest weekday; a tapped bar shows its total.
10. Chart by hour of day (0 to 23) over the period, with a caption naming the busiest
    hour; a tapped bar shows its total.
11. Top numbers over the last 30 days: a ranked list (ten rows seen) of number and
    count. Tapping a row opens S-07.

The share icon opens the system share sheet with a short text: a one-line boast, total
blocked, total silenced, the last-30-days count, and the Play Store link.

### S-07 Number details (bottom sheet)

Opened from a row of the top-numbers list. Shows the number, then five label / value
rows: blocked, silenced, total handled, first handled, last handled (both as
`yyyy-MM-dd HH:mm`). No actions. It does not expand.

## Money map

| Where | Format | When |
|---|---|---|
| Home, Settings, Advanced Settings, Statistics | large native ad (video or image, install button) | loads after the screen renders and shifts the content below it |
| History | large native ad pinned under the top bar | same |
| Opening History | full-screen interstitial (seen: a Liftoff / Vungle playable) | on the tap, before the screen |
| Opening Statistics | full-screen interstitial (seen: an AdMob video) | on the tap, before the screen |

No purchase, no "remove ads", no rewarded ads. Two ad networks seen in one session, so
it uses mediation. Whether the interstitials are frequency-capped is not known yet.

## Not observed in session 1

- What the three Advanced switches and the notification switch reveal when turned on
  (durations, windows, the allow-list editor).
- What the allow-list button on a history row does (a prompt, a duration, nothing).
- The status line and any prompt when switching to silence or block; the role request
  (the app already holds the role).
- Whether deleting a row or deleting all asks for confirmation.
- The per-call notification, the report notification and the milestone notification
  themselves.
- What a blocked or silenced call looks like in the system call log on this phone.
- The privacy-policy and contact rows (they leave the app; the listing gives the
  policy URL and the support address).
