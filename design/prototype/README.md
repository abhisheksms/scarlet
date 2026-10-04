# Scarlet prototype (Stage 3)

One interactive HTML prototype of every surface and state in §2 of `design/CLAUDE_DESIGN_BRIEF.md`, the Later frames included, in the direction picked at gate G4. It is the reference for the handoff bundle and for the Compose build. It is not ported: stage 5 of `knowledge-base/docs/05-design-workflow.md` recreates it.

Round 1, 4 October 2026. Made by Claude Design.

## The pick

Switchboard, first version. The founder chose it on 4 October 2026 and asked for two changes: no lamp colours, and type and a look that resemble the Uber app. His words on the choice: "switchboard is good" and "i dont like gate register one"; on the first version, "the icons look good in it". The lamps now have no colour of their own. Uber's typeface is proprietary, so Hanken Grotesk stands in its place (`HANDBACK.md`, "What I could not do").

## What is here

| File | What it is |
| --- | --- |
| `scarlet-prototype.html` | The prototype. One file, fonts inside, no network. Built from `src/` and `fonts/` |
| `SPEC.md` | Every final value: colours by Material 3 role, type in sp, space and shape in dp, motion, the icon, the TalkBack order of each screen, every component and its states |
| `HANDBACK.md` | What the brief asks for with each delivery: decisions it did not dictate, every wording change old beside new, where the brief is wrong for the user, what could not be done |
| `src/`, `build.py` | The sources and the build. `src/60-panel.js` holds the text of `SPEC.md` and `HANDBACK.md` |
| `fonts/` | The two embedded faces and their licences |
| `tools/` | Checks, screenshots and the Markdown writer. Nothing in it ships |

`SPEC.md` and `HANDBACK.md` are written from the prototype by `tools/docs.js`. If either disagrees with the prototype, the prototype is right.

## Open it

On a computer, open `scarlet-prototype.html` in a browser. The phone is on the left, the controls and the written spec on the right.

On an Android phone, open the file with Chrome. `Full Screen`, in the prototype's status bar, takes the browser's bars away.

On an iPhone, Safari does not open a local HTML file, and the Files preview does not run scripts; the page says it did not start. Serve this folder instead (`python3 -m http.server`, then `http://<this computer>:8000/scarlet-prototype.html` on the same network) and open it in Safari. Share, then Add to Home Screen, opens it without Safari's bars.

## Drive it

- **Switches** tab: theme, text size (100% and 135%), clock, data (new user, three calls, heavy user, thousands), ad slot (loaded, empty, removed), motion, what Android reports (screening role, notification permission, consent), and the three Later frames.
- **Screens** tab: all 50 surfaces and states, one tap each.
- An address for each: the file's URL, `#`, a screen id, then switches joined by dots. `#his-sheet.dark.135.12h.empty.reduced`. Tokens: `light` `dark`, `100` `135`, `24h` `12h`, `loaded` `empty` `removed`, `full` `reduced`, `frame` `screen`, `shot`.
- From a script: `window.scarletPrototype` has `presets` (the ids), `link(address)`, `state`, `check()`, `selfTest()` and `dump(address)`.

The clock is fixed at Friday 2 October 2026, 19:30. The data is the brief's §8 and nothing else. Anything Android or Google draws (the role prompt, the share sheet, the consent form, the purchase sheet, the ad) is a dashed stand-in.

## The same on every phone

On a phone the prototype opens as the 360 × 800 frame: real size where the screen allows it, shrunk where it does not, the same picture everywhere. The switch **On a phone: This screen** (or `screen` in the address) lays the app out at the phone's own width instead, as the app itself will. A OnePlus 12 gives a browser about 412 × 905 dp and an iPhone 18 Pro 402 × 874, so in that mode a line breaks at a different word in 56 of the 198 states measured.

Four things make two browsers draw the frame alike, and all four are in `src/app.css` and `build.py`:

- every text size and line height is a whole number of pixels (Safari rounds a fractional line height down, Chrome does not);
- no line breaking is left to the browser: the display's sentence is balanced by `balanceText`, other sentences keep their last two words together (`tieWidows`), both in `src/20-core.js`;
- every character comes from the two embedded faces, the stand-ins included;
- the Latin face is given `ascent-override: 69.7%` and `descent-override: 0%`, which stops Chrome on Android setting some sizes of text one pixel higher than Safari.

**Self-check.** `#selftest` in the address, or Run Self-Check in the Switches tab, measures the browser it is opened in: the layout rules of the brief's §4 at that screen's own size and in the frame, and whether every line in the frame breaks where it does in the reference browser. Two phones that both report no problems and the signature `BL8I5` draw the same frame. The signature changes whenever text is meant to move (see Change it).

## Check it

```
cd design/prototype/tools
npm install
npx playwright install chromium
node check.js      # §4 layout rules over every state, at four window sizes, and sameness with the reference
node flows.js      # the brief's flows with real clicks, typing and drags: 74 steps
node words.js      # §7: no ruled-out words, Title Case on buttons and actions
```

Each prints `NO PROBLEMS` or what it found, and exits non-zero on a problem. `--browser=webkit` runs `check.js` and `flows.js` in Playwright's WebKit after `npx playwright install webkit`; `node same.js --size=402x874` then compares Chromium and WebKit element by element.

What round 1 measured, on 4 October 2026:

| Check | Chromium 141 (Chrome's engine) | WebKit 2.52.6 (Safari's engine) |
| --- | --- | --- |
| Layout rules, 396 states, at 360 × 800, 402 × 874 and 412 × 905 | 0 problems | 0 problems |
| Lines break as in the reference, signature `BL8I5` | yes | yes |
| Flows with real input | 74 of 74 | 72 of 72 (no touch taps) |
| Copy rules | 0 problems | same text |

Between the two engines, at each of the three sizes: 17,984 element boxes over 198 states, none further apart than 0.02 px; no line breaking at a different word; all 77 text styles on the same baseline.

Not done: a run on a real OnePlus 12 or iPhone. The WebKit run was WebKitGTK on Linux, driven by a WebDriver script that is not in this folder, so `--browser=webkit` and the two-browser form of `same.js` are untried here.

## Change it

Review notes come back in rounds. For each round:

1. Edit `src/`. The design is `app.css`, `30-views.js` and the strings in `10-data.js`; the written spec is `60-panel.js`.
2. `python3 build.py` writes `scarlet-prototype.html`. `python3 build.py --check` says whether the committed file matches its sources.
3. If text was meant to move, `node tools/reference.js` records how Chromium now draws the frame and rebuilds. Otherwise leave it: `check.js` failing on the reference is the alarm for text that moved by accident.
4. `node tools/check.js && node tools/flows.js && node tools/words.js`.
5. `node tools/docs.js` rewrites `SPEC.md` and `HANDBACK.md`.

`node tools/snap.js` writes a PNG of every state (360 × 800 at 3 pixels per dp, the app alone) into `tools/shots/`, or into `--out=`: the screenshots the handoff bundle needs. `--themes=`, `--sizes=` and `--clocks=` choose which.

`python3 build.py --artifact PATH` also writes the page without its `<html>` shell, for publishing as a Claude Artifact.

## Fonts

Hanken Grotesk (Latin) and Noto Sans Devanagari, both under the SIL Open Font License 1.1; the licence texts are beside the files. They are the web subsets from Fontsource 5.3.0, enough for the prototype. The app needs the full families from their own repositories, named in the licence files.
