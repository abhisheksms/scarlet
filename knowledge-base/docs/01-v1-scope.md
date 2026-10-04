# 01 — Phase-one scope

Phase one is feature parity with the reference app as observed on 2 October 2026
(`docs/reference/notes.md`). `FEATURES.md` carries one row per feature with its
source, our implementation and its verification; this document is the summary and
the out-list.

## Must (phase one)

**The switch**
- Three states: off, silence, block. One line under it says what is happening now,
  including when filtering is paused and until when.
- Choosing silence or block when the app does not hold Android's call-screening role
  asks for the role. If the user declines, the switch stays off.
- If the role is later taken by another app, the home screen says so and offers to
  take it back.

**What gets filtered**
- Scope: every caller not in contacts, or only callers from abroad who are not in
  contacts.
- Pause: let everything ring for a chosen time, then resume by itself.
- Repeat callers: optionally let a number ring when it calls again within a window
  of being blocked or silenced.
- Allow list: numbers that always ring, typed in or added from a history row. An
  entry can be permanent or can expire.
- India's number series (added 4 October 2026, ahead of any public track): service and
  transactional calls from the 160 series (1600: banks, insurers and other regulated
  financial entities, government bodies; 1601: utilities, couriers, logistics) always
  ring while the lever is on. Promotional calls from the 140 series are blocked once the
  user switches that on in Options; off as installed, because TRAI's rules of September
  2026 bar a call-management app from blocking the series on its own while leaving the
  user free to block whatever they choose (`02-rules-engine.md`).
- A Quick Settings tile (added 4 October 2026, the founder's pick for the third
  difference): lit while calls are filtered; one tap pauses filtering for an hour, the
  next resumes it; when nothing is filtered a tap opens the app.

**What the user sees afterwards**
- An optional notification for each handled call. The number is hidden on a locked
  screen.
- History of handled calls, grouped by day, newest first: number, outcome, time.
  Per row: allow this number, delete the row. Delete everything, behind a
  confirmation.
- Statistics: all-time blocked and silenced; a milestone with progress to the next;
  an estimate of time saved with its assumption stated; the last 7 and 30 days with
  the change against the stretch before; a 7-day chart; a period selector (7, 30, 90
  days) for a by-weekday chart, a by-hour chart and the most frequent numbers; a
  details sheet for one number (counts, first and last time).
- A weekly or monthly summary notification (off by default) and a notification when
  a milestone is reached.
- Times follow the phone's 12-hour or 24-hour setting; dates follow its locale;
  numbers are grouped for reading.

**Around it**
- Settings: report frequency, privacy policy, privacy choices when Google's consent
  platform requires them, open-source licences, contact, and a build stamp.
- Share the app (its store link) and a link to rate it.
- Ads from AdMob with Google's test unit ids, behind the UMP consent flow, in
  reserved space.
- Light and dark themes following the system. Every control labelled for TalkBack.
  Layout holds at 360 dp with font scale 1.35.

## Later (written down, not started)

- **A reason on every handled call** in History and on the number's sheet, with Always
  Allow and Always Block one tap away, which brings a block list of chosen numbers. The
  deciding rule is already stored with every call; the surface waits for design round 2
  (`docs/play-repetitive-content.md`, difference 2). Before any public track.
- **Pro unlock** (phase two): one non-consumable through Google Play Billing; removes
  ads; price shown from Play.
- Indian-language strings (Hinglish register first).
- A home-screen widget for the switch.
- Export of the history.

## Never

- Reading contacts, the call log or SMS; asking for an account; sending any call
  data off the device.
- Caller identification from a shared database (it needs exactly the data this app
  refuses to take).
- Tagging or labelling any call as spam, or a spam report inside the app: TRAI forbids
  tagging the designated number series, and an in-app report would have to reach the
  operators' platform. A stopped call is labelled with the rule that stopped it, no more.
- Ads that load late and move content, ads placed to catch a tap, urgency copy, or
  making the free app worse to sell the unlock.
- The reference app's name, wording, icon, colours or screenshots.

## Known platform limits (say them, do not hide them)

- Calls with a hidden or withheld number are **not** passed to screening apps by
  Android, so they ring as usual. The dialer's own "block unknown / private numbers"
  setting covers those.
- A blocked call still appears in the system call log, marked as blocked. Android
  ignores a third-party app's request to keep it out.
- If another app is chosen as the phone's call-screening app, this one stops
  receiving calls.
