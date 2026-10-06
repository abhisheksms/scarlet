# The lever's handle, frame by frame, 6 October 2026

The founder's note on the build of 6 October: "Slider is not smooth". This is the
measurement behind `NOTES.md` N-51: where the lever's handle was in every frame, before the
change and after it, for the same gestures.

## How it was measured

- The emulator `scarlet_phone` at 360 dp wide (3 px to a dp), drawing 60 frames a second.
- A log line was put where the handle is placed, one where the lever is composed and one
  where a stop is chosen, in a throwaway copy of each build: `main` at `580c26d`, and the
  change. The lines were taken out again; the product carries none.
- The gestures were `adb shell input tap` and `input swipe`, the same for both builds. A
  swipe is a finger moving in a straight line at one speed, so the right place for the
  handle in every frame is known.
- A position is the handle's top edge in pixels inside the lever's plate: 39 on Off, 225 on
  Silence, 411 on Block. A row is 186 px, which is 62 dp.

## What the handle did

| Gesture | Before | After |
|---|---|---|
| Carried from Off to Block in 0.6 s and let go on Block | 24 px (8 dp) behind the finger all the way. Let go at 387, its next frame was at **39, back on Off**; then it came down to 411 a second time, over 185 ms | Under the finger all the way, and on 411 when let go. Nothing moves after that |
| Carried 0.4 of a row up from Block and let go | Let go at 361, its next frame was on 411: **a jump of 17 dp**, no travel | Let go at 336, then 346, 364, 379, 390, 398, 403, 406, 408, 411 |
| Carried 0.7 of a row down from Off and let go | Let go at 142, its next frame was at **39, back on Off**; then it travelled to Silence | Let go at 167, then 177, 191, 203, 211, 216, 220, 222, 225 |
| Flicked 0.35 of a row down from Off in 50 ms | Moved 29 px and went back. Still Off | Went on to Silence: 96 when let go, then 124, 155, 180, 196, 207, 215, 219, 221, 223, 225 |
| A tap on Block with the lever at Off | Moved up to 124 px in one frame: 41 dp, more than the handle is tall | Moved up to 88 px in one frame: 29 dp |
| The biggest move in one frame, for the three carries above | 348 px, 50 px, 103 px | 13 px, 18 px, 14 px |
| Times the whole lever was composed while the handle was carried from Off to Block | 46, once for every frame | 4 |

## The handle no longer waits for the store

On the emulator the store answers in about 25 ms, so a tap starts the handle no sooner than
it did: 46 ms after the tap, against 44. That is the two frames any travel takes to begin.
The difference shows when the store is slow, which was tried with a throwaway build:

- **Storing a lever move made 300 ms slower.** The handle started 49 ms after the tap and
  was seated at 222 ms. The stored mode arrived at 332 ms. The old handle could not have
  started before then.
- **Made 1,500 ms slower.** The handle went to Block and was seated at 218 ms. At 1,027 ms,
  with the mode still Off, it went back to Off. At 1,533 ms the mode arrived and it went to
  Block. So the handle cannot go on showing a stop that was not stored: after a second it
  shows what is in effect.

## What stands guard now

- `LeverHandleTest`, five tests of which stop a handle takes when it is let go. The rule
  was broken eleven ways and a test failed each time.
- **Emulator check 25.** It holds the handle with a finger and reads where the handle is,
  which must be within 2 dp of the finger; lets it go short of half-way, and past half-way;
  and flicks it down and up; with a call after each move to show the mode is the real one.
  Broken four ways on purpose, it failed each time: the 8 dp wait put back (it read the
  handle 24 px from the finger, which is the old lever), the flick's speed ignored, the
  handle moving half as far as the finger, and the choice not taken.
- No scripted check can see one frame, so none watches for the jump. It cannot come back
  the way it was: the handle had two positions then and has one now.

## By hand on the emulator, the same day

- **Without the call-screening role.** Carried to Block and let go: Android's prompt opens,
  and with it refused the handle is back on Off. A tap on Block: the same. Carried to
  Silence with the prompt accepted: the lever stands at Silence.
- **A locked lever** (a throwaway build that treats the emulator as unable to screen). Held
  1.2 rows down, the handle is 21 px below its seat, which is the 7 dp it gives, and no
  further. Let go, flicked hard, or a row tapped: it is back on Off.
- **With the phone's animations switched off** the handle goes straight to its seat, and
  check 25 passes.
- **Home at 360 dp**, light and dark, at 100% and 135% text, with the handle at rest and
  held half-way: it looks as it did.

## Not shown

- A real finger. Every touch above was injected, and moved at one speed.
- The founder's phone. Its screen can draw 120 frames a second, and the build on it is a
  debug build.
