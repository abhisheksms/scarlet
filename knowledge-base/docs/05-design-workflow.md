# 05 — Design workflow

How scarlet gets its design. The method is the studio's (the playbook's
`project-method/references/design-workflow.md`); this document records what is
specific to this app.

## Why now

Phase one was built on a light pass (gate G4): stock Material 3 components that
nobody chose. On 3 October 2026 the founder asked for a full Claude Design pass:
UI, UX and mockups. G4 is open again, and its gate is now the direction pick.

## The cast

| Who | Does |
|---|---|
| **Founder** | pastes the brief into Claude Design; picks the direction (G4); approves the prototype; makes every taste call |
| **Claude Design** | the boards, the prototype, every revision |
| **Claude Code** | wrote the brief; carries review notes back; freezes the handoff; recreates the design in Compose |
| **Mobbin** | a library of real app screens and flows, used as a reference for behaviour (below) |

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
   surface and state, saved under `design/prototype/`. Each round is checked
   against the brief's hard constraints (§4) and copy rules (§8), and the findings
   go back to Claude Design in plain English. The founder approves it in his own
   phone's browser, at real size.
4. **The handoff bundle.** `design/design_handoff_scarlet/`: the spec with every
   value final and the load-bearing decisions named, a screenshot of every surface
   in every state, the prototype, and the build brief. Frozen means frozen: a
   later change is a product change with a note, never a silent edit.
5. **Build.** Recreate the design in Compose; never port the prototype's HTML.
   Colours land in `ui/theme/Theme.kt` by Material 3 role. A Compose app has no
   browser preview, so each screen is checked on the emulator at 360 dp with 135%
   text, light and dark, against the bundle, before its PR merges.

## Mobbin

The founder asked for it on 3 October. It is used for **behaviour**: what is on a
screen, in what order, which states exist, how a flow moves. It is never used for
**the look**: colour, type, icons and words are our own. No app from this
category is a reference, and a pattern counts as a convention only when two
unrelated apps both do it. Every reference consulted is listed with the pattern
taken from it, and the list is kept in `design/`.

This relaxes one line of the studio's brief template, which bans citing other
apps on a board. Boards still cite non-UI references for the look; Mobbin informs
behaviour only.

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
