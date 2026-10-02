#!/usr/bin/env python3
"""Tap an element of the reference app by its visible label, only when it is safe.

Usage: SERIAL=<adb serial> tools/reftap.py <label> [--contains] [--index N]

The reference app loads ads late and they push rows down, so a tap aimed at
coordinates from an older capture can land on an ad. This reads the screen twice,
1.5 s apart, and taps only if the target belongs to the reference app's package and
has not moved between the two reads.
"""
import os
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET

PKG = "com.lifesoftwarelab.android.incomingcallcontrol"


def adb(*args):
    serial = os.environ.get("SERIAL")
    base = ["adb"] + (["-s", serial] if serial else [])
    return subprocess.run(base + list(args), capture_output=True)


def matches(label, contains):
    adb("shell", "uiautomator", "dump", "/sdcard/reftap.xml")
    xml = adb("exec-out", "cat", "/sdcard/reftap.xml").stdout.decode("utf-8", "replace")
    adb("shell", "rm", "-f", "/sdcard/reftap.xml")
    if "<" not in xml:
        return []
    out = []
    for n in ET.fromstring(xml[xml.index("<"):]).iter("node"):
        if n.get("package") != PKG:
            continue
        for value in (n.get("text", ""), n.get("content-desc", "")):
            if value and (label in value if contains else value == label):
                out.append(tuple(map(int, re.findall(r"\d+", n.get("bounds", "")))))
                break
    return out


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    label = args[0]
    contains = "--contains" in sys.argv
    index = int(sys.argv[sys.argv.index("--index") + 1]) if "--index" in sys.argv else 0
    first = matches(label, contains)
    time.sleep(1.5)
    second = matches(label, contains)
    if not second or len(second) <= index:
        print("NOT FOUND:", label)
        return 1
    if first != second:
        print("MOVED, not tapping:", first, "->", second)
        return 2
    x1, y1, x2, y2 = second[index]
    adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))
    print("tapped", repr(label), (x1, y1, x2, y2))
    return 0


if __name__ == "__main__":
    sys.exit(main())
