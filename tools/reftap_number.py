#!/usr/bin/env python3
"""Tap the Nth visible phone-number label of the reference app (to open its details).

Usage: SERIAL=<adb serial> tools/reftap_number.py [N]

Same safety as reftap.py (reference package only, two reads, no movement), and the
number itself is never printed.
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


def numbers():
    adb("shell", "uiautomator", "dump", "/sdcard/reftap.xml")
    xml = adb("exec-out", "cat", "/sdcard/reftap.xml").stdout.decode("utf-8", "replace")
    adb("shell", "rm", "-f", "/sdcard/reftap.xml")
    if "<" not in xml:
        return []
    out = []
    for n in ET.fromstring(xml[xml.index("<"):]).iter("node"):
        if n.get("package") != PKG:
            continue
        compact = re.sub(r"[\s\-()+]", "", n.get("text", ""))
        if re.fullmatch(r"\d{5,}", compact):
            out.append(tuple(map(int, re.findall(r"\d+", n.get("bounds", "")))))
    return out


def main():
    index = int(sys.argv[1]) if len(sys.argv) > 1 else 0
    first = numbers()
    time.sleep(1.5)
    second = numbers()
    if len(second) <= index:
        print("NOT FOUND: number", index)
        return 1
    if first != second:
        print("MOVED, not tapping")
        return 2
    x1, y1, x2, y2 = second[index]
    adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))
    print("tapped number label", index, (x1, y1, x2, y2))
    return 0


if __name__ == "__main__":
    sys.exit(main())
