#!/usr/bin/env python3
"""Capture one screen of the device for the reference inventory: a screenshot, a
downsized copy that is cheap to read, the UI hierarchy, and a printed list of nodes.

Usage: SERIAL=<adb serial> tools/refcap.py <name> [--quiet]

Output goes to docs/reference/raw/ (git-ignored: captures of the founder's phone can
show real callers' numbers). Nothing here is ever shipped or copied into the product.
"""
import os
import re
import subprocess
import sys
import xml.etree.ElementTree as ET

RAW = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "docs", "reference", "raw")


def adb(*args):
    serial = os.environ.get("SERIAL")
    base = ["adb"] + (["-s", serial] if serial else [])
    return subprocess.run(base + list(args), capture_output=True)


def main():
    name = sys.argv[1]
    quiet = "--quiet" in sys.argv
    shot = os.path.join(RAW, "shots", name + ".png")
    small = os.path.join(RAW, "shots", name + "-small.png")
    dump = os.path.join(RAW, "dumps", name + ".xml")
    with open(shot, "wb") as f:
        f.write(adb("exec-out", "screencap", "-p").stdout)
    subprocess.run(["sips", "-Z", "1100", shot, "--out", small], capture_output=True)
    adb("shell", "uiautomator", "dump", "/sdcard/refcap.xml")
    xml = adb("exec-out", "cat", "/sdcard/refcap.xml").stdout.decode("utf-8", "replace")
    adb("shell", "rm", "-f", "/sdcard/refcap.xml")
    if "<" not in xml:
        print("(empty dump: screen animating? use the screenshot)")
        return 0
    xml = xml[xml.index("<"):]
    with open(dump, "w") as f:
        f.write(xml)
    if quiet:
        return 0
    for n in ET.fromstring(xml).iter("node"):
        text, desc = n.get("text", ""), n.get("content-desc", "")
        flags = "".join(
            c if n.get(a) == "true" else "-"
            for a, c in (("clickable", "C"), ("checkable", "K"), ("checked", "X"), ("scrollable", "S"), ("enabled", "E"))
        )
        if text or desc or n.get("clickable") == "true" or n.get("checkable") == "true":
            cls = n.get("class", "").split(".")[-1]
            b = re.findall(r"\d+", n.get("bounds", ""))
            print(f"{flags} {cls:<14} text={text!r} desc={desc!r} pkg={n.get('package','').split('.')[-1]} {b}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
