# Scarlet Stage 3: what comes back with the prototype

<!-- Written by tools/docs.js from the prototype's Notes and Words tabs. Change the text in src/60-panel.js, run build.py, then run docs.js again. -->

The brief asks for four things with every delivery: the decisions it did not dictate, every wording change with old beside new, anything in it that is wrong for the user, and anything that could not be opened or done.

## Decisions the brief did not dictate

1. **Nothing below the display moves.** The display and the bay under the lever are each as tall as their tallest state. Throwing the lever, pausing or losing the role changes words and lamps, never positions.
2. **Pause lives on Home only.** Four keys under the lever: one tap from opening the app. Options no longer repeats it.
3. **With the switch at Off, the place of the pause keys carries the privacy line.** On the board it sat at the foot of first launch only.
4. **One pattern for every small choice:** a caption and a strip of keys. Momentary keys act at once (pause, allow). Latching keys hold a setting (who is filtered, repeat window, period, summary). The three dialogs are gone.
5. **Repeat callers is one control.** Off sits in the same strip as 5, 15 and 30 Min.
6. **Fixing a mistake is two taps.** A history row is one large target that opens the number’s sheet; a length key there allows it and closes the sheet; the row then says so. The three-dot menu is gone.
7. **Deleting one call** sits on the sheet beside “This call”, away from the allow keys.
8. **No snackbars.** Every result is written where the action was.
9. **The notification should carry the action:** one, “Allow For 1 Hour”, on an unlocked phone only. It is the courier case without opening the app. A permanent allow stays inside the app.
10. **Statistics is ranked:** the all-time counter is the headline; the last 7 days come second; the period keys and what they drive come last. “Last 30 days” appears once the history is older than a week, so a new user is not shown one count three times.
11. **The day chart counts in units up to 8 calls a day** and becomes bars with totals above that. The weekday and hour charts never scale below 4, so a single call is not a full-height bar.
12. **The hour chart is read by touching or dragging across it.** 24 bars cannot each be a 48 dp target.
13. **Notifications blocked in Android:** the row on Home, and the summary keys in Settings, become a link that opens Android’s settings.
14. **A device that cannot screen** says so in the display from the first frame, and the lever will not leave Off.
15. **History and the sheet call a number allowed only while the allow list is on.**
16. **India rules have no section of their own.** The 140 rule sits under Who is filtered; the 160 rule sits after the allow list, with the other ways a call gets through.
17. **The slot does not rise with the keyboard.** An ad directly above the keys invites a wrong tap.
18. **Licences gains the two typefaces** and a second licence link, which the Open Font License requires once the fonts ship.
19. **Screens cut; sheets rise.** The lever keeps the timings from the board.
20. **Keys in a strip share its width equally** unless a label needs more; then that key takes what it needs. If the labels cannot share one row, the strip stacks.
21. **The icon is 5% smaller inside its layer,** so the key’s corner clears the 66 dp safe circle. Nothing else about it changed.
22. **The sentence in the display is balanced:** its lines are as even as they can be without leaving a word alone. A dialog’s question is the same. In every other sentence the last two words stay on one line. In Compose that is `LineBreak.Heading` for the first and a no-break space before the last word for the second.
23. **Parts joined by a middle dot wrap as parts.** A part that starts a new line drops its dot: “Today, 17:41 · Blocked” on one line, the time over the outcome on two.

## Where I think the brief is wrong for the user

1. **§8, the totals.** 156 calls in all cannot hold 121 in the last 30 days plus the 111 that “9% more” implies for the 30 days before. Both figures are shown as written.
2. **§8, the week.** 34 calls cannot be 12% fewer than a whole number: against 38 it is 11%, against 39 it is 13%. Shown as written.
3. **§8, the numbers.** Five numbers, whose counts add to 24 in 30 days, cannot fill a week of 34 calls. History lists three days and then says so.
4. **The allow list’s own switch.** In today’s build, with the list switched off, History and the sheet still say “On the allow list”. The prototype fixes the wording (decision 15). The simpler fix is to drop the switch: an empty list is already off.
5. **A device that cannot screen.** In today’s build, tapping Silence or Block there does nothing and says nothing.
6. **A 720 dp tall phone.** Home needs 688 dp above the slot at 100% text. At 360 × 800 it fits with 8 dp to spare; at 360 × 720 it scrolls by about 72 dp. The lever and the display stay above the fold. Only moving Notifications off Home would fix it.
7. **Strings 40% longer.** The display holds three lines at 100% text. English’s longest sentence already takes three, so a translation of it has little room before a fourth line pushes Home about 19 dp past the fold. The Hindi draft fits in three. Translators need that limit; the other strings have their 40%.
8. **Time saved** is parked, so it stays, as one quiet line under the counter.

## Figures the brief does not give

- Yesterday’s and Wednesday’s rows, built from the same five numbers and one caller with no number, inside the day totals of the chart.
- Which calls of the week were silenced: Monday, Wednesday and today.
- By hour for 30 days; by weekday and by hour for 7 and 90 days; the 7 and 90 day lists of frequent numbers; each number’s first call and all-time split.
- 90 days is treated as all time (156), because the first call is 38 days old.
- The whole Thousands set: 3,412 calls on a much busier line, to load the charts.
- Last month’s summary, 114 blocked and 6 silenced.

## What I could not do

- Run it on a OnePlus 12 or an iPhone. It was run in Chromium, which is Chrome’s engine, and in WebKit, which is Safari’s, at 360 × 800, 402 × 874 and 412 × 905. Between the two, every element sits within 0.02 px, every line breaks at the same word and every text style has the same baseline. Both were driven through every flow with real clicks, typing and drags. Run Self-Check, in the Switches tab, repeats the measurements in whatever browser this is and says whether it draws the frame as the reference does.
- Use the typeface of the Uber app, as the founder asked. Uber Move is proprietary. Hanken Grotesk, under the SIL Open Font License, stands in its place.
- Draw Android’s role prompt, the share sheet, Google’s consent form, Play’s purchase sheet or the ads. Each is a labelled stand-in that only lets the flow continue.
- Have the Hindi read by a native speaker.
- Show the real price. ₹000 is a placeholder for Google Play’s localised price.

## Deep links

Each screen in the Screens tab has an address: the file’s URL, then `#` and its id. Add switches with dots: `#his-sheet.dark.135.12h.empty.reduced`. The ids are in `window.scarletPrototype.presets`, for scripting the screenshots of the handoff bundle.

Add `shot` for the app alone, edge to edge, without the prototype’s own buttons: `#home-block.dark.100.24h.shot` in a window of 360 × 800 is the frame for a screenshot. On a phone, `frame` and `screen` choose between the 360 × 800 frame and the app at the phone’s own width. `#selftest` runs the self-check on opening.

## Words

Every change to `strings.xml`, old beside new. A string not listed is unchanged.

| String | Old | New | Why |
| --- | --- | --- | --- |
| `mode_silence_detail and three more` | don't, isn't, can't | don’t, isn’t, can’t | Typographic apostrophes in every string. |
| `role_missing` | Android isn't sending calls to this app, so nothing is being filtered. | Nothing is being filtered. / Android isn’t sending calls to this app. | Two lines: the consequence first, the cause under it. |
| `paused_until` | Paused until 3 Oct 2026, 19:30. Every call rings. | Paused until tomorrow, 19:30. Every call rings. | A pause never runs past tomorrow. Same change in “Until tomorrow, 10:05” on the allow list. |
| `notifications_detail` | For each blocked or silenced call | On, for each stopped call / Off | The switch’s state is now a word as well as a position. |
| `notifications_denied` | Notifications are switched off for this app. | Switched off in Android’s settings | No snackbar. The row itself says it and opens the settings. |
| `options_detail` | Scope, pause, repeat callers, allow list | No exceptions / Repeat callers ring · 2 numbers allowed | A live summary in place of a list of what is inside. |
| `statistics_week` | 0 blocked, 0 silenced in 7 days | Nothing yet / Nothing in 7 days | Only for zero. With calls the old string stays, on three lines. |
| `calls_handled, notification_milestone` | 100 calls handled | 100 calls stopped | One word for blocked plus silenced, the one the share text already uses. |
| `channel_handled_calls` | Handled calls | Stopped calls |   |
| `channel_reports` | Reports | Summaries | Settings calls it a summary. |
| `report_detail` | A count of handled calls, each Monday or on the 1st | A count of stopped calls, each Monday or on the 1st. |   |
| `exceptions_heading` | Exceptions | removed | Repeat callers and Allow list are titled sections of their own. Home still says “No exceptions”. |
| `scope_all` | Everyone outside your contacts | Everyone (key) / Everyone outside your contacts. (line under it) |   |
| `scope_international` | Only international callers outside your contacts | International Only (key) / Only international callers outside your contacts. (line under it) |   |
| `pause, pause_detail, pause_active, pause_for` | Pause / Let every call ring for a while / Every call rings until 19:30 / Pause For | Let every call ring for | Pause is on Home only, as a caption over four keys. |
| `minutes, hours` | 15 minutes, 1 hour, 4 hours, 24 hours | 15 Min, 1 Hr, 4 Hr, 24 Hr | On keys. The full words stay as the TalkBack labels. |
| `repeat_callers_detail` | A number that calls again soon after being stopped rings | A stopped number that calls again within this time rings. |   |
| `repeat_within` | Calls again within | removed | Off joins 5, 15 and 30 Min as one strip of keys. |
| `allow_list_detail` | Numbers that always ring | On / Off | Some entries are timed, so “always” was not true. |
| `add_number, allow_number_title, allow, allow_for` | Add Number / Allow a Number / Allow / For 1 hour | Let this number ring, then 1 Hr, 24 Hr, Always | No dialog. The length key is the commit. |
| `allow_this_number` | Allow This Number | Let this number ring, then 1 Hr, 24 Hr, Always | Same caption and keys on the sheet. |
| `more_for_number` | More for +91 … | removed | No row menu. |
| `delete` | Delete | Delete (spoken as Delete This Call) | Beside the line “This call”. |
| `added_to_allow_list` | +91 … is on the allow list. | On the allow list / Rings until 20:30 | Written on the row. No snackbar. |
| `total` | Total | 9 calls stopped | The total leads; blocked and silenced follow it. |
| `statistics_empty` | Nothing to show yet. | removed | The counter reads 000. |
| `time_saved, time_saved_basis` | About 1 hr 18 min saved / Counted at 30 seconds a call | About 1 hr 18 min, at 30 seconds a call | One line. |
| `day_by_day` | Day by day | removed as a heading | The chart sits under “Last 7 days”. It stays as the chart’s TalkBack label. |
| `licences_intro` | This app is built with these open-source libraries. | removed | The title says it. |
| `licence_text_link` | Read the Apache License 2.0 | Read The Apache License 2.0 | Every word capitalised, as on the other actions. |
| `%d in every count` | 3412 | 3,412 | Group thousands. |

### New strings

| Where | Words |
| --- | --- |
| Home, switch at Off | No account. Your contacts are never read. Nothing about a call leaves this phone. |
| Ad slot, when an ad is loaded | Advertisement |
| History row, a repeat | 2nd call, 3rd call |
| Number details | This call |
| Statistics, the counter | Calls stopped |
| Statistics, the milestone | 100 reached |
| Notification action (proposed) | Allow For 1 Hour |
| Licences | SIL Open Font License 1.1 / Read The SIL Open Font License 1.1 |
| Later, Settings | Remove Ads / One-time purchase / Restore Purchases / No purchase found for this Google account. / Ads removed |
| Later, Play product text | Remove Ads / Removes the ad from every screen. One-time purchase. |
| Later, Options | Always block 140 numbers / On. Telemarketers must call from these. / Always allow 160 numbers / On. Banks, insurers and government services call from these. |

The Hindi on the Later frame is a draft and has not been read by a native speaker.
