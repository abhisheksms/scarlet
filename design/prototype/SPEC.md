# Scarlet design spec: Switchboard

<!-- Written by tools/docs.js from the prototype's Values, TalkBack and Components tabs. Change the text in src/60-panel.js, run build.py, then run docs.js again. -->

Every final value of the Stage 3 prototype, `scarlet-prototype.html`. Where this page and the prototype differ, the prototype is right and this page is stale.

## Values

### Colour, by Material 3 role

A fixed palette. Nothing is taken from the wallpaper. The lamps have no colour of their own: a lit lamp is onSurface, a held one a ring of it, a dark one lampGlass. Blocked is a filled mark and silenced an open one.

| Role | Light | Dark | Used for |
| --- | --- | --- | --- |
| surface | #D2D6D1 | #1C1E21 | The panel: every screen’s ground |
| surfaceContainerHigh | #E7EAE6 | #282B2F | Raised plates: the lever plate, keys, sheets, dialogs |
| surfaceContainerHighest | #F4F6F3 | #34383D | A key’s face when it sits on a plate; the bevel under a recess |
| surfaceContainerLow | #C3C8C3 | #131416 | The ad slot |
| onSurface | #16181A | #ECE9E0 | Text, engraving, lit lamps, chart units, marks |
| onSurfaceVariant | #3E4447 | #A9ADB1 | Secondary text, captions, lever labels not set |
| outline | #6B726E | #70757B | Key edges, lamp bezels, chart baselines |
| outlineVariant | #A7ADA8 | #0E0F10 | Engraved hairlines; the 3 dp drop under a key |
| primary | #16181A | #ECE9E0 | Main key, latched key, the selected day |
| onPrimary | #F0EDE4 | #16181A | Text on those |
| inverseSurface | #0E0F11 | #0E0F11 | The display window, the field, the lever slot, the switch slot |
| inverseOnSurface | #F0EDE4 | #F0EDE4 | Text and marks in the display |

scrim is black at 50% in light and 62% in dark. Roles not listed take these values: secondary and tertiary are primary; their containers and primaryContainer are surfaceContainerHigh; error is attention.

#### Outside Material 3

| Name | Light | Dark | Used for |
| --- | --- | --- | --- |
| inverseOnSurfaceVariant | #A9ADB1 | #A9ADB1 | Secondary text in the display |
| inverseOutlineVariant | #2C2F33 | #2C2F33 | Rules inside the display; a drum’s edge |
| attention | #FF8A5B | #FF8A5B | One use: the square beside the role-missing sentence, inside the display |
| lampGlass | #9AA19C | #3A3E44 | A lamp that is not lit |
| lampGlassHigh | #C9CEC9 | #5A5F66 | Its highlight, as a radial gradient from 36% 30% |
| lampOnHigh | #5B6368 | #FFFFFF | The highlight of a lit lamp; its body is onSurface |
| handle | #1D2023 | #D9D5CB | The lever handle and the switch handle |
| handleHigh | #42474B | #F6F3EA | Their top edge, as a vertical gradient |
| handleRidge | #6A7075 | #8C8A84 | Three ridges on the lever handle |
| drumHigh | #1D2023 | #1D2023 | A counter drum, top |
| drumLow | #121315 | #121315 | A counter drum, bottom |

lampHalo is a 5 dp ring of onSurface at 16% in light and 20% in dark. handleDrop is black at 38% in light and 60% in dark: the 3 dp drop under the handle and the main key.

Measured contrast: text is 5.8:1 or better on its ground in both themes. A control’s edge is 3.06:1 or better against what surrounds it.

### Type, in sp

Hanken Grotesk, with Noto Sans Devanagari for Hindi. Both are under the SIL Open Font License 1.1. Hanken Grotesk’s figures are the same width at every weight with no font feature to switch on. It has no rupee sign: ₹ comes from the Devanagari companion or the system font. Ligatures are off. Capitals on controls are a style, like engraving; the strings stay in Title Case and sentence case.

| Style | M3 slot | Size / line | Weight | Case and tracking | Used for |
| --- | --- | --- | --- | --- | --- |
| Numeral | displayMedium | 40 / 42 | 500 |   | Counter drums; today’s count on Home |
| Lever | headlineSmall | 26 / 30 | 700 set, 600 not | caps, +0.04 em | Off, Silence, Block |
| Display | titleLarge | 21 / 27 | 500 |   | The sentence in the window; the number on a sheet; a dialog’s question |
| Title | titleMedium | 18 / 22 | 600 | caps, +0.08 em | Screen titles |
| Main key | labelLarge | 17 / 21 | 700 | caps, +0.05 em | Resume, Set As Screening App |
| Number | bodyLarge | 17 / 22 | 500 |   | Phone numbers in lists; the field |
| Key | labelLarge | 15 / 18 | 600 | caps, +0.03 em | Every other key |
| Lead | bodyLarge | 15 / 20 | 400 or 500 |   | Counts beside a mark; times; the second line when the role is missing |
| Body | bodyMedium | 14 / 19 | 400 |   | Sentences, details, second lines of rows |
| Strip | titleSmall | 13 / 17 | 600 | caps, +0.08 em | Row titles. Section titles use +0.1 em |
| Note | bodySmall | 13 / 18 | 400 |   | The privacy line, milestone labels, the build stamp |
| Caption | labelMedium | 12 / 16 | 600 | caps, +0.1 em | Captions over keys, tile captions, day headings. Chart labels use +0.06 em |
| Slot | labelSmall | 11 / 14 | 600 | caps, +0.1 em | The word Advertisement |

In the prototype every size and line height lands on a whole pixel at both text sizes, and the embedded Latin face is given an ascent of 69.7% and a descent of 0: its ascent less its descent, so the baseline stays where the font puts it. Both are there so that every browser sets the same lines in the same places. Neither is a design value. The app takes its sizes and its font metrics from Android.

### Space and shape, in dp

- Side margins 16. Between blocks 16 on Home and 12 elsewhere. Inside a block 8. Between keys 8.
- Corners: 4 on keys, the handle, the field and the panel switch (3 on its handle); 6 on plates, the display, a sheet’s top edge and a dialog; 2 on chart units. Lamps are circles of 28, the Off ring 22.
- Targets: a lever row is 62 tall (67 at 135% text) and as wide as the screen; a key 48; a main key 56; a strip 58; header buttons 48 square; a history row at least 56; a day in the chart 48 wide.
- The display and the bay under the lever each take the height of their tallest state at the current width and text size, so the lever and the strips never move. At 360 dp and 100% that is 226 and 75.
- Depth is drawn, never cast: a recess has a 2 dp black line along its top edge and a 1 dp highlight under it; a key has a 1 dp outline ring and a 3 dp drop of outlineVariant; a plate has a 1 dp ring and a 2 dp drop. No shadows, no blur.
- The ad slot: a sill as tall as its label’s line (20 sp), the banner’s own height (60 at 360 dp wide), then the gesture inset. It does not rise with the keyboard. With ads removed only the gesture inset remains.
- The status bar inset is left clear above every screen. Home starts 8 below it.

### Motion

| Name | Duration | Easing | What moves |
| --- | --- | --- | --- |
| lever.travel | 180 ms | cubic-bezier(0.2, 0, 0, 1) | The handle moves to the new stop. One haptic tick as it seats. |
| lamp.cut | 0 ms | none | The old lamp goes dark at once. A lamp never shows a position the handle has left. |
| lamp.warm | 240 ms, starting at 120 ms | cubic-bezier(0, 0, 0.2, 1) | The new lamp warms up. |
| display.swap | 120 ms. After a lever move it starts at 180 ms | linear | The sentence cross-fades. The window does not change size. |
| lever.locked | 160 ms | cubic-bezier(0.2, 0, 0, 1) | On a device that cannot screen: the handle moves 7 dp towards Silence and returns. |
| key.press | 60 ms | linear | A key’s face drops 3 dp while pressed. A latched key stays down. |
| switch.slide | 120 ms | cubic-bezier(0.2, 0, 0, 1) | The panel switch’s handle crosses 20 dp. |
| sheet.rise | 220 ms | cubic-bezier(0.2, 0, 0, 1) | A sheet rises from the bottom edge. |
| sheet.fall | 160 ms | cubic-bezier(0.3, 0, 1, 1) | A sheet leaves. A dialog fades out over the same time. |
| scrim.fade | 160 ms | linear | The scrim and a dialog fade in. |
| row.press | 0 ms | none | A row, strip or tile takes a 6% tint of onSurface while pressed. No ripple. |
| screen.change | 0 ms | none | A cut. Every screen lands settled, with nothing arriving after the first frame. |

**Reduced motion.** Every duration is 0. Handle, lamp and sentence change in the same frame; sheets and dialogs appear and leave at once; the locked lever does not move. The haptic tick stays. Switch Motion to Reduced to see it.

### Icon

The drawings are in the Values tab: Adaptive, round, Squircle, 48 dp, Themed, light, Themed, dark, Notification.

As picked. Background #1C1E21; the slot #0B0C0D; the key #F5C63C; the handset #F0EDE4. The drawing is scaled to 95% about the centre of its 108 dp layer, which puts every part inside the 66 dp safe circle. The themed icon and the notification icon are the same two shapes in one colour.

## TalkBack order

The order TalkBack reads each screen in. Quoted text is what is spoken.

### Home

1. The sentence in the display: “Callers outside your contacts are rejected.” It is a polite live region, so a change of mode, a pause or a lost role is spoken.
2. Mode, a radio group of three: “Off, 1 of 3”, “Silence, 2 of 3”, “Block, selected, 3 of 3”. Each option’s description is its sentence.
3. The bay under the lever. “Let every call ring for”, then “15 minutes”, “1 hour”, “4 hours”, “24 hours”. When paused, the one key “Resume”. When the role is missing, “Set As Screening App”. With the switch at Off, the privacy line as text.
4. “History, 6 calls today”, button.
5. “Statistics, 31 blocked, 3 silenced in 7 days”, button.
6. “Options, Repeat callers ring, 2 numbers allowed”, button.
7. “Notifications, On, for each stopped call”, switch. When Android blocks them: “Notifications, switched off in Android’s settings, Open Settings”, button.
8. “Settings”, button.
9. The ad, read by Google’s own view, last.

History, Statistics and Settings are drawn inside the display, above the lever. They are read after the lever and its keys, so set the traversal order; do not take it from the layout.

### Options

1. “Back”, button. “Options”, heading.
2. “Who is filtered”, a radio group: “Everyone”, “International Only”. Then the sentence under it.
3. Later: “Always block 140 numbers, Off”, switch.
4. “Repeat callers”, its sentence, then a radio group: “Off”, “5 minutes”, “15 minutes”, “30 minutes”.
5. “Allow list, On”, switch.
6. Each number as one item, “+91 98765 43210, Always”, followed by “Remove +91 98765 43210”, button.
7. “Phone number to allow”, edit box. An entry with no digits is refused with “Enter a phone number.”, spoken at once.
8. “Let this number ring”, then “1 hour”, “24 hours”, “Always”. They appear once the field has focus.
9. Later: “Always allow 160 numbers, Off”, switch.

### History

1. “Back”, button. “History”, heading. “Delete All”, button.
2. “Today”, heading.
3. Each call as one item and one button: “19:04, +91 140 123 4567, Blocked, 2nd call that day”. An allowed number adds “on the allow list” or “rings until 20:30”.
4. “Yesterday”, heading, and so on by date.

The confirmation: “Delete all history?”, “Statistics are counted from the history, so they reset too.”, “Cancel”, “Delete All”. Focus starts on Cancel.

### Number details

1. The number, heading.
2. “9 calls stopped. 9 blocked, 0 silenced.”, one item.
3. “First: 26 Aug 2026, 10:14.” “Last: 2 Oct 2026, 19:04.” Each label and value is one phrase.
4. “Let this number ring”, then “1 hour”, “24 hours”, “Always”. For a number already allowed: “On the allow list, Always”, then “Remove From Allow List”, button.
5. “This call: Today, 19:04, Blocked”, then “Delete This Call”, button.

On screen the delete key sits near the top, away from the allow keys. In the reading order it comes last. A caller with no number has no allow keys.

### Statistics

1. “Back”, button. “Statistics”, heading. “Share”, button.
2. “156 calls stopped. 148 blocked, 8 silenced.”, one item.
3. “100 reached. Next: 250.”
4. “About 1 hr 18 min, at 30 seconds a call.”
5. “Last 7 days”, heading. “34 calls.”
6. The chart, bar by bar, each a button: “Saturday: 3 blocked, 0 silenced” to “Friday: 5 blocked, 1 silenced, selected”. The line under the chart repeats the selected bar, so it is skipped.
7. “12% fewer than the week before. Busiest around 11:00.”
8. “Last 30 days”, heading. “121 calls. 9% more than the 30 days before.”
9. “Period”, a radio group: “7 days”, “30 days, selected”, “90 days”.
10. “By weekday”, heading. “Most on Tuesday.” Then seven bars: “Sunday: 6” to “Saturday: 11”.
11. “By hour”, heading. “Most around 11:00.” Then 24 bars: “00:00: 0” to “23:00: 0”.
12. “Most frequent”, heading. Rows: “+91 140 123 4567, 9 calls”, button.

### Settings

1. “Back”, button. “Settings”, heading.
2. “Summary notification”, its sentence, then a radio group: “Off”, “Weekly”, “Monthly”.
3. “Privacy Policy”, button. Where consent is required: “Privacy Choices”, button.
4. “Open-Source Licences”, “Contact”, “Share App”, “Rate App”, buttons.
5. Later: “Remove Ads, one-time purchase”, with the price, button. “Restore Purchases”, button.
6. The build stamp, as text: “Version 0.1.0, 2 October 2026”.

### Licences

1. “Back”, button. “Open-Source Licences”, heading.
2. Each library as one item: its name, the copyright holder, the licence.
3. “Read The Apache License 2.0”, button. “Read The SIL Open Font License 1.1”, button.

### Notifications

1. Android reads the app’s name, the title, the text and the time: “Call blocked, +91 140 123 4567”.
2. On a locked screen only “Call blocked”.
3. “Allow For 1 Hour”, button, on an unlocked phone only.
4. The milestone: “100 calls stopped”. The summary: “Last week, 31 blocked, 3 silenced”.

## Components

Every component and its states, drawn in the theme and text size now switched.

### Display window

Dark in both themes. It holds one sentence, chosen in the order role missing, paused, mode, with the gear and two entry tiles. Its height is the tallest of its states and does not change.

Drawn in the Components tab: A mode, First launch, Paused, Role missing, Cannot screen.

### Entry tile

A way into History or Statistics that carries a live count. States: a count; a plain line when there is nothing.

Drawn in the Components tab: Counts, Nothing.

### Lever

Three stops. The handle is the position, the lamp says it is in effect, the engraving names it. Tap a row or drag the handle. States: Off, Silence, Block, paused (the lamp becomes a ring), role missing (every lamp dark), locked (a device that cannot screen: the handle will not leave Off).

Drawn in the Components tab: Off, Silence, Block, Paused, Role missing.

### Key

A raised key with a 3 dp drop. Momentary keys act at once: pause lengths, allow lengths, Delete, Cancel. Latching keys hold one choice of a set and stay down: who is filtered, the repeat window, the period, the summary. States: rest, pressed (down 3 dp), latched. Keys in a strip are equal in width unless a label needs more. A strip stacks when its labels cannot share one row.

Drawn in the Components tab: Momentary, Latching, Stacked.

### Main key

One per screen at most, and only when one action outranks the rest. States: rest, pressed.

Drawn in the Components tab: Resume, Role.

### Strip

A full-width row between engraved rules. Trailing part by kind: a chevron stays in the app, an arrow leaves it, a panel switch toggles, a value states a price, nothing for a statement.

Drawn in the Components tab: Kinds.

### Panel switch

A slot with a sliding handle. Left is off, right is on, and the row says the word.

### Header

Back, the title in capitals, and at most one action: a text action (Delete All) or an icon (Share). It stays in place while the screen scrolls.

Drawn in the Components tab: With an action.

### History row

Time, number, then the outcome as a mark and a word. A later call from the same number that day says which call it is. A number on the allow list says so. The whole row is the target and opens the number’s details.

Drawn in the Components tab: Rows.

### Sheet

Number details. States: not allowed (three allow keys), allowed (the entry and Remove From Allow List), no number (no allow keys), opened from Statistics (no This call line). It covers the ad slot. See History and Statistics in Screens.

### Dialog

One use: Delete All. A plate over the scrim with two keys of equal weight. Cancel has the focus when it opens.

Drawn in the Components tab: Delete All.

### Field

A recessed display you can type in. States: empty, focused, filled, error (a 2 dp ring and the message under it).

Drawn in the Components tab: Empty, Error.

### Allow-list row

The number, then Always or Until and a time, and a 48 dp remove button.

Drawn in the Components tab: Rows.

### Counter

The all-time count on drums, like a message register. At least three drums; leading zeros are dimmer. A fourth drum appears at 1,000.

Drawn in the Components tab: Nothing, 156, 3,412.

### Milestone bar

From zero to the next milestone, with a notch at the last one reached. States: none reached, reached and next, past the top of the ladder (full, no next).

Drawn in the Components tab: None reached, Reached.

### Day chart

Seven days, oldest first. Up to 8 calls a day each call is one unit: solid for blocked, outlined for silenced. Past 8 the columns become bars to one scale with the day’s total above. A day is a 48 dp target; the selected day’s label inverts and its counts are written under the chart.

Drawn in the Components tab: Units, Bars, One call.

### Weekday chart

Seven bars from zero, each with its count above. Not selectable: the numbers are already written.

Drawn in the Components tab: 30 days.

### Hour chart

24 bars from zero. Touch or drag anywhere on it to read an hour; the line beside the heading changes from “Most around” to that hour’s count.

Drawn in the Components tab: 30 days, An hour chosen.

### Ad slot

A recessed tray along the bottom edge. States: loaded (the word Advertisement on the sill, the banner under it), empty (the tray alone), removed (no tray). Same height loaded or empty.

Drawn in the Components tab: Loaded, Empty.

### Notification

Android draws it. Ours are the small icon, the title, the text and one action. Three kinds, all silent: a stopped call, a milestone, a summary. On a locked screen a stopped call shows its title only.

Drawn in the Components tab: A call, Locked, Milestone, Summary.

### Stand-in

A dashed box marks anything Android or Google draws. It is not part of the design.
