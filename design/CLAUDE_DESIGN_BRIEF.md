# Scarlet — Design Brief

You are designing an Android call blocker (internal name **scarlet**; the name
users will see is not chosen yet), for phones only. The app is built and
working. What it lacks is a design. This brief is self-contained: everything
you need is in it, and everything in it is deliberate. Where it constrains you,
the constraint was earned on a shipped product or is held by a test in this
one; treat each as intentional.

## 0. Where the product lives

The source is public: **https://github.com/abhisheksms/scarlet** (branch
`main`). Read it before you draw. It is the truth about what the app does
today. In this order:

1. `FEATURES.md` — every feature, one row each.
2. `knowledge-base/docs/00-product-vision.md` and `01-v1-scope.md` — what the
   app is, what is in, what is never.
3. `app/src/main/res/values/strings.xml` — every word on screen today.
4. `docs/verification/screens/contact-sheet.png` — the current build, every
   screen side by side (since 4 October 2026 that build is the Switchboard
   design itself, recreated in Compose).
5. `app/src/main/kotlin/com/cyanharborstudios/callblock/ui/` — one Kotlin file
   per screen (`HomeScreen.kt`, `OptionsScreen.kt`, `HistoryScreen.kt`,
   `StatisticsScreen.kt`, `NumberDetailsSheet.kt`, `SettingsScreen.kt`), the
   parts they are built from in `ui/parts/`, and `ui/theme/` for the palette,
   the type scale and the motion.

A single file opens at
`https://raw.githubusercontent.com/abhisheksms/scarlet/main/<path>`.

Treat the current screens as a wireframe. The content, the states and the
behaviour are right. The look is stock Material 3 that nobody chose: keep what
the app does, and feel free to rearrange every screen.

One folder is out of bounds: `docs/reference/`. It holds research notes about
another developer's app. Do not open it, and do not look up or imitate any
existing call-blocking app. Nothing of theirs may enter this design; a design
that resembles another app gets ours removed from Google Play.

If you cannot open the repository, say so in your first line and work from this
brief alone. It is complete without it.

## 1. The product

One job: calls from numbers that are not in your contacts do not interrupt
you. One control with three positions. **Off**: every call rings. **Silence**:
the call comes through but the phone does not ring, and you can still answer.
**Block**: the call is rejected. Every call the app stops is listed in a
history kept on the phone.

There is no account, and the app never reads the contact list: Android itself
only hands it the calls from numbers outside the contacts. Nothing about a call
leaves the device. All of that is true, and the design may lean on it.

It earns from ads: one banner slot in reserved space along the bottom of every
screen (see "The money" in §2). A one-time purchase that removes ads comes
later.

The category is beatable. It is caller-ID apps that ask for contacts, the call
log and an account, and small blockers that work but bury themselves in ads
that load late and slide a row out from under your thumb. Nobody in it wins on
craft or on honesty. This app wins on three things: it needs nothing private;
it never costs a call that mattered (contacts always ring, filtering can be
paused, chosen numbers can be allowed); and its screen holds still.

**Who it's for**: adults in India first, on a mid-range Android phone, often
with large text and often in the dark theme. They get several telemarketing,
loan and "KYC" calls a day and have stopped answering unknown numbers. They are
not frightened of anything. They are tired of being interrupted. The app
respects them by being quiet and exact.

**The moments of use**, in order of importance:

1. **The glance.** Open the app, or read its notification: is it on, and what
   did it stop today? Two to five seconds, one hand, sometimes outdoors.
2. **The exception.** A courier is at the gate, a cab is arriving, a bank is
   due to call back. Let everything ring for an hour, or let one number
   through. Ten seconds, under mild pressure. If this is slow or unclear, the
   user misses a call that mattered and uninstalls.
3. **Setup.** Once. Install, choose Silence or Block, accept Android's own
   prompt, done. Under a minute, with no tour.
4. **The look back.** Now and then: the history and the statistics. This is
   the payoff for having the app, and it is where a wrongly stopped call gets
   found and fixed.

## 2. The surfaces and their states

Design all of them. **Home is the hero**; everything else exists to serve the
four moments above.

1. **Home.** The three-position control, what is happening right now in plain
   words, a notifications switch, and the ways into Options, History,
   Statistics and Settings. Home carries no app name and no logo: the top of
   the screen belongs to the state. States:
   - *Off, first launch*: nothing handled yet. No welcome screen.
   - *Silence* and *Block*, each with its one-line meaning.
   - *International only*: the same two modes when the user has narrowed
     filtering to callers from abroad.
   - *Paused*: "Paused until 18:30. Every call rings." with Resume.
   - *Role missing*: Android is not sending calls to this app, so nothing is
     filtered. One action: Set As Screening App.
   - *Cannot screen*: a device without the feature. One sentence, no action.
   - *Notifications blocked* in system settings, with a way to open them.
   - Entry rows carry live numbers: "6 calls today" or "Nothing today"; "31
     blocked, 3 silenced in 7 days".
2. **Options.** *Who is filtered*: everyone outside the contacts, or only
   international callers outside the contacts. *Exceptions*: **Pause** (off;
   choosing a length of 15 minutes, 1, 4 or 24 hours; active, showing when it
   ends), **Repeat callers** (a number that calls again within 5, 15 or 30
   minutes of being stopped rings), **Allow list** (off; on and empty; on with
   numbers, each marked Always or "Until 18:30"; adding a number by typing it,
   for always, 1 hour or 24 hours; an invalid entry; removing a number). The
   lengths on offer are ours and may change if a better set serves.
3. **History.** Calls the app stopped, newest first, grouped under Today,
   Yesterday and then dates. Each row: the number, Blocked or Silenced, the
   time. A row can be allowed or deleted; everything can be deleted, behind a
   confirmation that says the statistics reset too. States: empty; a few
   rows; a heavy day with the same number repeating; a call with no number;
   the confirmation; the moment after "allow".
4. **Number details** (a sheet). One number: blocked, silenced, total, first
   and last time. Then either Allow This Number, or "On the allow list" with
   Remove From Allow List.
5. **Statistics.** All-time blocked and silenced; the last milestone reached
   and progress to the next (10, 25, 50, 100, 250, 500, 1,000 …); an estimate
   of time saved, counted at 30 seconds a call and saying so; the last 7 days
   (count, busiest hour, change against the week before); the last 30 days
   (count, change); a day-by-day chart of the last 7 days, blocked and
   silenced stacked, where tapping a day gives its numbers; a 7 / 30 / 90 day
   selector that drives a by-weekday chart, a by-hour chart and the ten most
   frequent numbers. A share action sends counts as text. States: nothing
   yet; three calls; thousands.
6. **Settings.** Summary notification: Off, Weekly or Monthly. Privacy Policy;
   Privacy Choices (present only where the law requires consent);
   Open-Source Licences; Contact; Share App; Rate App; a small build stamp
   (`v0.1.0 · 2026-10-02`). Calm and boring in the best way.
7. **Licences.** A plain list.
8. **Notifications** (Android draws the frame; you choose the icon and the
   words). *Call blocked* or *Call silenced* with the number, which is hidden
   on a locked screen. *Milestone*: "100 calls handled". *Summary*: "Last
   week" with "31 blocked, 3 silenced". All three are silent.

**The money, and its limits.**

- **The banner slot** sits along the bottom edge of every screen, above the
  gesture bar. It has its full height (about 60 dp on a 360 dp phone) from
  the first frame, whether or not an ad ever loads, so nothing moves when one
  arrives. Design it as a tenant: visibly contained, never mimicking content,
  and composed when it is empty, because offline is normal. The screen's
  hierarchy must not bend to it.
- **No control may sit against the slot.** A tap aimed at the app must never
  land on an ad. That rules out a bottom navigation bar; navigation is a hub
  on Home with a back arrow elsewhere.
- **Never an ad** on a dialog, a sheet, a confirmation, in a list, or over
  the three-position control.
- A full-screen ad can appear when the user leaves History or Statistics, at
  most once every three minutes. Google draws it. Design nothing for it and
  let no screen depend on it.

**Later — leave room, do not build the boards around these.** Draw one frame
for each, marked Later, to prove the layout survives them:

- *Remove Ads*: a one-time purchase. Two rows in Settings (Remove Ads with a
  price placeholder, Restore Purchases) and one purchase sheet in Google
  Play's voice, with no urgency of any kind. Then every screen without the
  slot, leaving no hole.
- *India rules*: two more choices, one that always blocks the 140-series
  numbers telemarketers must use, one that always allows the 160-series
  numbers banks, insurers and government services call from. Options is the
  likely home; propose a better one if there is one.
- *Translations*: Hindi first. Allow strings 40% longer.

**Not yours to draw.** Android's role prompt, the system share sheet, Google's
consent form and the ads themselves. Draw what is on screen before and after
each.

## 3. What the design has to solve

Answer each of these in the work. They are the reason for this brief.

1. **The switch is the product.** Three positions, one always current. Home
   must say what is happening now in one glance: the mode, and any exception
   to it. Today a selected radio row and a sentence do this. Find the form
   that is unmistakable at arm's length and still fits at 135% text.
2. **Off is not broken, and on is not an alarm.** Off must look deliberate,
   and plainly not filtering. Silence and Block are the app doing its job:
   they should feel settled. Today blocked calls wear the error red and silenced
   ones amber; decide whether a good outcome should wear the colour of a
   fault.
3. **The exception must be faster than the interruption.** Pause is two
   screens deep today. From opening the app, "let everything ring for an
   hour" should take one or two taps, and Home must then say that everything
   rings, until when, and offer the way back.
4. **One truth when several things apply.** International-only scope, an
   active pause, repeat callers, numbers on the allow list and a missing role
   can all hold at once. State the combination without turning Home into a
   dashboard. When they compete: role missing, then paused, then the mode.
5. **First launch without a welcome.** No tour, no carousel. Off on day one,
   with nothing in the history, has to make the next action obvious by
   itself. Draw the three beats: first launch, Android's prompt over it, on.
6. **Fixing a mistake.** A call the user wanted was stopped. From History,
   "let this number ring" must take the fewest steps, for good or for a
   while. Today it is a three-dot menu on every row, or the sheet. Fewer and
   larger targets win. Say whether the notification should carry the action.
7. **History at volume.** Six to ten rows a day, with one number repeating.
   Decide what a row needs, what a repeat deserves, and what empty says.
8. **Statistics with a hierarchy.** Seven blocks of equal weight in one long
   scroll today. Rank them. Something is the headline, and something may not
   deserve to exist: the time-saved estimate is under review. It has to look
   right with three calls and with three thousand.
9. **One pattern for three small choices.** Pause length, repeat window and
   allow-for-how-long are three separate dialogs today.
10. **The icon.** It must read as "calls, stopped" at 48 dp in a store search
    result, beside a dozen icons that are a handset inside a shield, and not
    be one of them.

## 4. Hard constraints — every one intentional

- **A small, cheap phone, in portrait.** Frames are 360 × 800 dp. Everything
  must also hold at 135% text, which is the founder's own phone setting: show
  it. Android draws edge to edge, behind the status bar and the gesture bar.
- **The mode and its status are above the fold** in every Home state, at both
  text sizes, with the banner slot present. At 100% text Home does not
  scroll.
- **The screen holds still.** All data is on the phone and instant. No
  spinners, skeletons or shimmer, and nothing that arrives after the first
  frame.
- **State is never told by colour alone.** Always a word, and a position or a
  shape. Text contrast 4.5:1, controls 3:1, in both themes.
- **One-handed.** Primary controls in the lower two thirds, targets at least
  48 dp, nothing destructive beside a primary action.
- **Light and dark are both first-class**, with a fixed palette of our own
  rather than colours taken from the wallpaper, so the app looks the same on
  every phone.
- **Buildable in Jetpack Compose with Material 3 on Android 10 and later.**
  It will be recreated in Kotlin, not ported from your HTML. Custom-drawn
  controls are welcome. Backdrop blur is not available before Android 12, so
  nothing may depend on it. Give sizes in dp, type in sp, and map every
  colour to a named role so it drops into the theme.
- **Fonts and icons must be free to ship** (SIL Open Font License or Apache
  2.0), with tabular numerals, and with a Devanagari companion for the Hindi
  that follows. No words inside images.
- **Both clocks.** Times follow the phone: "19:04" and "7:04 PM" must both
  fit wherever a time appears.
- **Motion communicates a change of state and nothing else.** The mode
  changing is the one moment that earns it. Name the durations and easings,
  and design the reduced-motion version alongside.
- **Screen-reader order is part of the design.** Write the TalkBack order for
  each screen. A chart is read bar by bar.
- **Honest charts.** Zero baseline, no smoothing, no 3D, and designed for
  none, one and many.
- **Only true numbers.** Every figure is a real count from the phone's own
  log. No scores, no "threat level", no meter that measures nothing.

## 5. The ambition — three directions

The founder has not named a look for this app. What he asks of every product
is that it must not look generated, and that polish is the point: every
control finished, nothing inert, nothing said twice. Its store screenshot also
has to stop a scroll, because it will sit in search results beside a dozen
look-alikes. The current build is what happens with no direction at all: stock
components and a stack of identical rounded cards.

The app's character comes from its job. It stands at a door and decides who
gets to knock. Channel that into exactly three named directions. Push each one
hard; a timid board is a useless board.

- **Switchboard** — the app as a piece of telephone hardware. A three-position
  key you can almost feel, a lamp that says which way it is thrown, engraved
  labels, and nothing else on the panel. The trust of a tool whose state reads
  from across a room. Made of: telephone exchange cordboards and key-and-lamp
  units, railway signal-box lever frames, Braun and Olivetti control panels.
- **Gate Register** — the app as the visitors' book at a gate. Typographic,
  ruled and tabular: who called, when, turned away or let through quietly.
  The numerals and the times are the beauty, and the switch is the sign on
  the gate. Made of: the register at an apartment-society gate, railway
  timetables and reservation charts, bank passbooks, departure boards.
- **Harbour Light** — the app as a signal. The whole screen is the state: one
  field of colour for Off, another for Silence, another for Block, readable
  before a word is. The calmest of the three. Made of: harbour and lighthouse
  signal lamps, the "On Air" light outside a studio door, a hotel
  do-not-disturb hanger, a room lit by a single colour, dusk.

Each board cites what it is made of, and the citations are **not other apps**:
industrial design, signage, instruments, print, architecture, light.

**Banned outright, in all three directions:**

- Shields, padlocks, and ticks inside shields.
- Alarm, siren, skull or hooded-hacker imagery, and any red used to frighten.
- Radar sweeps, "scanning" animations, and a pulsing power button.
- Protection scores and gauges.
- Mascots, 3D phone renders, and stock illustrations of people on phones.
- Confetti, trophies or badges for a milestone.
- Neon on black, purple-blue gradients, gradient blobs.
- Glass and frosted panels as a default material.
- Identical-card grids and stacks.
- Emoji as icons.
- Placeholder graphics standing in for content that does not exist.

These are the costume of a security app. If a board needs one to feel
trustworthy, the board has failed.

## 6. Deliverables

**Stage 2 — now, in this conversation**: three static boards, one per
direction, named as above. Each contains:

- Home in four states: Off on first launch, Block on for a heavy user,
  Paused, and Role missing. At least one in each theme, and one at 135% text.
  Every phone frame includes the banner slot.
- The three-position control drawn large, with the change between positions
  described.
- One secondary surface, History or Statistics, whichever shows the direction
  better, using the sample data in §8.
- The palette for light and dark as hex values with role names, and the
  typeface with its licence.
- An icon candidate (the adaptive icon, and the single-colour version used
  for themed icons) and the small notification icon.
- What the direction is made of, cited.
- One sentence each answering questions 1 to 4 of §3 for this direction.

The founder picks one board. The pick comes back to you, and it is final.

**Stage 3 — after the pick**: one interactive HTML prototype in the chosen
direction, self-contained. Every surface and every state in §2, the Later
frames included. Switches in the prototype for theme, text size (100% and
135%), clock (12 and 24 hour), data (new user and heavy user) and the ad slot
(loaded, empty, removed). Real words throughout. Final values: exact colours
mapped to Material 3 roles, the type scale in sp, spacing and shapes in dp,
named motion durations and easings, the reduced-motion variant demonstrable,
the TalkBack order per screen written down, and an inventory of every
component with its states. Revision notes will come back in rounds.

**With every delivery, hand back:**

- the decisions you made that this brief did not dictate;
- every change to wording, old beside new;
- anything in this brief you think is wrong for the user, and why. Push back
  rather than comply quietly;
- anything you could not open or could not do.

## 7. Voice — the copy rules

The words in `strings.xml` are the starting point. Improve them under these
rules; the prototype's words are final.

- **Title Case on buttons and actions** ("Allow This Number", "Set As
  Screening App"). Everything else is sentence case.
- **Talk to adults.** Terse and confident. No coaching, no reassurance, no
  explaining what a button plainly does. When in doubt, delete the sentence.
- **No narrator.** No welcome, no "you're all set", no chummy asides. When the
  app speaks, it speaks as the tool: what it will do, and what it did.
- **No fear and no praise.** Never "protected", "safe", "secure" or "threat".
  The app does not know who is calling; it knows the caller is not in the
  contacts. A stopped call is blocked or silenced, never "spam" or "scam". A
  milestone is a number, not a congratulation.
- **No urgency**, anywhere, least of all near money.
- **No filler labels** restating what the screen already shows.
- The word "AI" never appears.

## 8. Sample data

Use these and nothing else. Every phone number below is made up; never show a
real one.

- **New user**: nothing handled. Then three calls: two blocked, one silenced.
  Next milestone 10.
- **Heavy user**: 156 handled in all, 148 blocked and 8 silenced. Milestone
  100 reached, next 250. About 1 hr 18 min saved.
- **Last 7 days**, oldest first: 3, 1, 7, 8, 5, 4, 6 (today). 34 in all, 31
  blocked and 3 silenced, 12% fewer than the week before, busiest around
  11:00.
- **Last 30 days**: 121, 9% more than the 30 days before. By weekday, Sunday
  to Saturday: 6, 22, 25, 20, 19, 18, 11. By hour: almost nothing before
  09:00 or after 21:00, peaks at 11:00 and 17:00.
- **Most frequent**: +91 140 123 4567 (9 calls), +91 80 4567 8901 (6),
  +91 22 1234 5678 (4), +91 98765 43210 (3), +1 415-555-0137 (2).
- **Today's history**: 19:04 +91 140 123 4567 blocked; 17:41 +91 80 4567 8901
  blocked; 16:12 +91 22 1234 5678 silenced; 13:30 +91 140 123 4567 blocked;
  11:26 +91 98765 43210 blocked; 10:02 +1 415-555-0137 blocked.
