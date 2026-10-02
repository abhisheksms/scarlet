#!/usr/bin/env python3
"""Drive the app on an emulator or a USB/Wi-Fi phone over adb, by accessibility label.

Usage:
  adb_drive.py dump                      list every labelled node: '*' = clickable, then label and bounds
  adb_drive.py find <label>              nodes whose label/text contains <label>
  adb_drive.py tap <label> [exact]       tap the centre of the best match (clickable preferred)
  adb_drive.py shot <name> [maxpx]       save a screenshot to $SHOTS_DIR (default ./shots), plus a
                                         downsized copy <name>-small.png (maxpx, default 1000) that is
                                         cheap to Read

Environment: SERIAL (adb serial, default: the only connected device), SHOTS_DIR.

Gotchas (see references/build-release-ops.md):
  - `uiautomator dump` fails while something animates forever. The reveal screen's 7s progress
    bar is exactly that, so dump returns nothing there. Screenshot it and tap by coordinates instead.
  - A dump taken mid-animation can be partial. Treat an empty result as "unknown", not as a state.
  - Never tap a real ad. Live ad units are invalid traffic on the founder's account.
"""
import os
import re
import shutil
import subprocess
import sys
import xml.etree.ElementTree as ET


def adb(*args: str) -> subprocess.CompletedProcess:
    serial = os.environ.get("SERIAL")
    base = ["adb"] + (["-s", serial] if serial else [])
    return subprocess.run(base + list(args), capture_output=True)


def nodes():
    adb("shell", "uiautomator", "dump", "/sdcard/ui.xml")
    xml = adb("exec-out", "cat", "/sdcard/ui.xml").stdout.decode("utf-8", "replace")
    if "<" not in xml:
        return []
    root = ET.fromstring(xml[xml.index("<"):])
    out = []
    for n in root.iter("node"):
        b = re.findall(r"\d+", n.get("bounds", ""))
        if len(b) == 4:
            out.append((n.get("text", ""), n.get("content-desc", ""), tuple(map(int, b)), n.get("clickable") == "true"))
    return out


def find(label: str, exact: bool = False):
    hits = []
    for text, desc, bounds, clickable in nodes():
        for value in (desc, text):
            if value and (value == label if exact else label in value):
                hits.append((value, bounds, clickable))
                break
    return hits


def main() -> int:
    if len(sys.argv) < 2:
        print(__doc__)
        return 2
    cmd = sys.argv[1]
    if cmd == "dump":
        found = nodes()
        if not found:
            print("(empty dump: screen still animating? screenshot instead)")
        for text, desc, bounds, clickable in found:
            if text or desc:
                print(("*" if clickable else " "), repr(desc or text)[:80], bounds)
        return 0
    if cmd == "find" and len(sys.argv) > 2:
        for hit in find(sys.argv[2]):
            print(hit)
        return 0
    if cmd == "tap" and len(sys.argv) > 2:
        exact = len(sys.argv) > 3 and sys.argv[3] == "exact"
        hits = sorted(find(sys.argv[2], exact), key=lambda h: not h[2])  # clickable first
        if not hits:
            print("NOT FOUND:", sys.argv[2])
            return 1
        value, (x1, y1, x2, y2), _ = hits[0]
        adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))
        print("tapped", repr(value), (x1, y1, x2, y2))
        return 0
    if cmd == "shot" and len(sys.argv) > 2:
        shots = os.environ.get("SHOTS_DIR", "shots")
        os.makedirs(shots, exist_ok=True)
        full = os.path.join(shots, sys.argv[2] + ".png")
        with open(full, "wb") as f:
            f.write(adb("exec-out", "screencap", "-p").stdout)
        small = os.path.join(shots, sys.argv[2] + "-small.png")
        maxpx = sys.argv[3] if len(sys.argv) > 3 else "1000"
        if shutil.which("sips"):  # macOS
            subprocess.run(["sips", "-Z", maxpx, full, "--out", small], capture_output=True)
        else:
            shutil.copyfile(full, small)
        print(full)
        print(small)
        return 0
    print(__doc__)
    return 2


if __name__ == "__main__":
    sys.exit(main())
