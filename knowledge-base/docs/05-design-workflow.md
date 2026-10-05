# 05 — Design workflow

How scarlet gets its design. The method is the studio's (the playbook's
`project-method/references/design-workflow.md`); this document records what is
specific to this app.

## Why now

Phase one was built on a light pass (gate G4): stock Material 3 components that
nobody chose. On 3 October 2026 the founder asked for a full Claude Design pass:
UI, UX and mockups. G4 was reopened, with the direction pick as its gate, and was
resolved on 4 October 2026: Switchboard (Decisions below).

## The cast

| Who | Does |
|---|---|
| **Founder** | pastes the brief into Claude Design; picks the direction (G4); approves the prototype; makes every taste call |
| **Claude Design** | the boards, the prototype, every revision |
| **Claude Code** | wrote the brief; carries review notes back; freezes the handoff; recreates the design in Compose |

## Stages

1. **Brief.** `design/CLAUDE_DESIGN_BRIEF.md`. It is self-contained, and it also
   points Claude Design at this repo, which is public. It never names the
   reference app and it puts `docs/reference/` out of bounds: the firewall holds
   for design as it does for code.
2. **Direction boards, then gate G4.** Three static boards: *Switchboard*, *Gate
   Register*, *Harbour Light*. The founder picks one. The pick and one sentence of
   why are recorded below; no recommendation is recorded. The boards not picked
   stay in `design/boards/`.
3. **Prototype and review rounds.** One interactive HTML prototype of every
   surface and state, saved under `design/prototype/`. Round 1 arrived on 4 October
   2026; its `README.md` says what each file is, how it was checked and how a
   review round works. Each round is checked
   against the brief's hard constraints (§4) and copy rules (§7), and the findings
   go back to Claude Design in plain English. The founder approves it in his own
   phone's browser, at real size.
4. **The handoff bundle.** `design/design_handoff_scarlet/`: the spec with every
   value final and the load-bearing decisions named, a screenshot of every surface
   in every state, the prototype, and the build brief. Frozen means frozen: a
   later change is a product change with a note, never a silent edit.
5. **Build.** Recreate the design in Compose; never port the prototype's HTML.
   Colours land in `ui/theme/Theme.kt` by Material 3 role. A Compose app has no
   browser preview, so each screen is checked on the emulator at 360 dp with 135%
   text, light and dark, against the bundle, before its PR merges. Done on 4 October
   2026 from round 1, ahead of stages 3 and 4 (Decisions below).

## Mobbin (dropped 4 October 2026)

**Deprecated.** The founder asked for Mobbin on 3 October and dropped it the next
day: "Now ignore mob and it's not open source." Its connector works only on
Mobbin's paid plans (Mobbin's own documentation, read 4 October). The brief no
longer mentions it, and the studio's brief template applies as written again: a
board cites no other app.

The rule as it stood for that one day, kept for the record: Mobbin was a reference
for **behaviour** (what is on a screen, in what order, which states exist, how a
flow moves) and never for **the look**; no app from this category was a reference;
a pattern counted as a convention only when two unrelated apps both did it; every
reference consulted was to be listed in `design/`.

## What the design may not change

- What the app does (`FEATURES.md`).
- The ads law (ADR-006): one banner slot with reserved height, and no control
  against it. That rules out a bottom navigation bar.
- No welcome screen or tour. One surface is the founder's own exception (5 October
  2026): How It Works opens by itself until it has been closed once.
- State is never told by colour alone.
- Layout holds at 360 dp with 135% text.

## Decisions

| Date | Decision |
|---|---|
| 2026-10-03 | G4 reopened: a full Claude Design pass. Brief written. The three directions are Claude Code's proposal, since the founder has not named a look for this app. Direction: not picked yet |
| 2026-10-04 | Mobbin dropped, at the founder's call. The brief's Mobbin section and its table of references are removed, so its later sections move up one: Deliverables §6, Voice §7, Sample data §8 |
| 2026-10-04 | **G4 resolved: Switchboard.** The founder chose it on 4 October 2026 and asked for two changes: no lamp colours, and type and a look that resemble the Uber app. His words on the choice: "switchboard is good" and "i dont like gate register one"; on the first version, "the icons look good in it" (recorded by Claude Design under "The pick" in `design/prototype/README.md`). The boards were not saved as files: they live in the Claude Design conversation, so `design/boards/` does not exist |
| 2026-10-04 | **Review note for round 2: a tutorial section.** The founder's rule for every app, said after the build: "every app we build MUST have a short tutorial section that even a 5 year old can understand". A surface the user opens (a "How it works" row), short, in the simplest words, in the Switchboard look; not a welcome tour. It goes back to Claude Design with the next notes, and is built once drawn |
| 2026-10-04 | **The screens were recreated in Compose from round 1**, at the founder's ask ("build the app ... make it happen"), before the review rounds, his approval on his phone and the handoff bundle. Every screen was checked on the emulator at 360 dp, light and dark, 100% and 135% text (`docs/verification/screens/`), and the ten emulator checks were re-run on the new screens (`docs/verification/`). Stages 3 and 4 stay open: review notes go back to Claude Design as before, and a change to the prototype from here on is a change to the app with a note in `NOTES.md` |
| 2026-10-04 | Round 1 of the Stage 3 prototype committed under `design/prototype/` exactly as delivered. `python3 design/prototype/build.py --check` reports that the built file matches its sources. The lamps carry no colour of their own; Uber's typeface is proprietary, so Hanken Grotesk (SIL Open Font License) stands in its place (`HANDBACK.md`, "What I could not do") |
| 2026-10-04 | **The India rules' Later frame: one switch built as drawn, one switch dropped.** Options' "Always block 140 numbers" switch was recreated from the Later frame (`OptionsScreen.kt`, tag `india-140`), off as installed. The "Always allow 160 numbers" switch was not built: TRAI's third amendment to the TCCCPR (18 Sep 2026) forbids any tagging, blocking or filtering of the 1600 and 1601 series, so the 160 rule is always on and a switch would only let the app break it (`02-rules-engine.md`). **Review note for round 2:** replace that switch with a statement in Options, in the direction's own words ("Service calls from the 1600 series, banks, insurers and government, always ring"), and re-read the 140 strip's copy against TRAI's words: "promotional", never "spam" |
| 2026-10-04 | **Review note for round 2: a reason on every handled call, and Always Block.** Play's Repetitive Content rule needs differences a user can name (`docs/play-repetitive-content.md`); the second is a reason line on each History row and on the number's sheet (the deciding rule is already stored: not in contacts, a promotional 140 number, allowed for an hour …), an Always Block key on the sheet beside the allow keys, and a block list in Options like the allow list. New surface: the block list. The copy never calls a call spam |
| 2026-10-04 | **Built outside the prototype: the Quick Settings tile**, the third difference (G10, the founder's pick). Android draws it; ours are the icon (the notification glyph), the label (the app's name) and the second line: Blocking, Silencing, Until 5:47 PM (TalkBack hears "Paused until 5:47 PM"; the longer form was cut off on the tile), Off, Not the screening app, Can't screen calls. Lit while calls are filtered; one tap pauses for an hour, the next resumes; a tap when nothing is filtered opens the app. **Review note for round 2:** the second line's words, and whether a tap should offer Home's four pause lengths in a dialog rather than one hour |
| 2026-10-05 | **The founder keeps the Switchboard design.** After his first day with the build on his phone he said, on 4 Oct: "The app feels very unnatural. I don't know why. For a newbie, it would feel totally a person won't even won't be able to understand." Claude's reading, for the next round and not adopted: the lever has no equivalent in any phone app, every label is in capitals, and the words are the app's own (Silence, Block, outside your contacts, No exceptions, Allow list) rather than a newcomer's. His call on 5 Oct: "let's stick with this design"; he will bring new feedback. G4 stands, nothing in the prototype changes for this, and the tutorial section logged above stays the planned answer for a newcomer |
| 2026-10-05 | **Built outside the prototype, at the founder's ask: How It Works and About, and three wordings.** He handed over his notes from two days with the build (`NOTES.md` N-40). How It Works is the tutorial section logged on 4 Oct, built now in the form he asked for ("tutorial section at the start and a feature for tutorial"): it opens by itself until it has been closed once, and from the first row of Settings after that. It uses the parts as they are: the header, sections under engraved rules, a plate like the lever's own with the three stops in the lever's type, and one main key, Done. About is one screen under Settings for the licences and Contact, and for the privacy policy once its page exists; Settings is now How It Works, the summary, About and the build stamp. The wordings: "Pause for" over Home's keys (round 1's "Let every call ring for" was not understood: "What do you mean - let every call ring for?"; the brief's own words were "Pause For"); Home's line at Off without its first sentence ("No account? What? Remove this sentence"); and the summary's one sentence became one for each key, under the keys. **Review notes for round 2:** draw How It Works and About; take Settings' new order and these words as given; the Privacy Policy, Share App and Rate App rows appear only once the pages they open exist. His one note of praise: "History section looks slick" |
| 2026-10-05 | **Built outside the prototype, at the founder's ask that evening: the Automatic section, the timer's sheet, the Schedule screen, the Plans screen; and the lever's meaning changed** (`NOTES.md` N-42). Home gains a section, Automatic, with two strips: Timer and Schedule. On Pro it sits right under the lever's bay; on the other plans the rows carry the word Pro, lead to Plans, and sit after Options and Notifications, so a row that is for sale never pushes a free one down. The timer is a sheet in the number sheet's idiom: latching keys for where to switch to, then the length, which is the commit. The Schedule is a grid, seven rows of twenty-four hours with a top row for every day, in the hour chart's idiom on a recessed dark track: a blocked hour is a solid unit, a silenced one an outlined unit, an hour set to Off a small ring. Plans is three plates, the user's own marked Yours. **The lever now stands at the mode in effect**, not where the user left it; while a timer or the schedule holds it the lamp is the ring the prototype drew for a pause, and the display has a second line for until when and what follows. A pause therefore moves the handle to Off, which the prototype's paused frame does not show. **Review notes for round 2:** draw all four surfaces and the display's second line; say whether the ring is the right sign for "held for a while"; the every-day row's dot where the days differ; and how the three plans should look when two of them have prices |
| 2026-10-05 | **Built outside the prototype, later that evening: three more surfaces, all from parts the prototype has** (`NOTES.md` N-43 to N-46). Options gains one strip with a switch, Call-backs, between the 140 strip and Repeat Callers, and a last section, Number Rules: on Pro the allow list's own row, recessed field and two keys over the keyboard, with the caption over the keys saying the start as the app reads it; on the other plans one strip with the word Pro. The Plans screen's plates gain a line for Google Play's price and one key each, and under them a Restore Purchases key with a line for what it found. How It Works gains two sentences and a renamed last section, In Pro. Review notes for round 2: draw these four; and decide whether Home should say why it is paused after a call to an emergency number, which today it does not (`knowledge-base/adr/ADR-008`). `docs/verification/screens/` has them as built (`07`, `29` to `36`) |
| 2026-10-06 | **A wording changed at the founder's ask: the key that ends a pause.** With the build on his phone, paused, he wrote: "Instead of resume, rename to resume blocks or something, it s confusing". The prototype's key says Resume. Since 5 Oct the lever stands at Off during a pause, so nothing on the screen said what would resume. The key is now named for what it brings back: Resume Blocking, Resume Silencing, or End Pause when what comes back is Off (an hour the schedule has set to Off). A note for round 2: draw the paused state with the lever at Off and this key (`NOTES.md` N-49; `docs/verification/screens/03-home-paused.png`) |
