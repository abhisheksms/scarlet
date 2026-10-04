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
- No welcome screen or tour.
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
