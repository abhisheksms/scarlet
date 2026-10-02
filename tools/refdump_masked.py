#!/usr/bin/env python3
"""Print a captured UI dump with phone numbers masked, so real callers never reach a log.

Usage: tools/refdump_masked.py <name>   (reads docs/reference/raw/dumps/<name>.xml)
"""
import os
import re
import sys
import xml.etree.ElementTree as ET

RAW = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "docs", "reference", "raw")


def mask(s):
    # A phone number: five or more digits once spaces, dashes, brackets and '+' are removed.
    compact = re.sub(r"[\s\-()+]", "", s)
    if re.fullmatch(r"\d{5,}", compact):
        return re.sub(r"\d", "#", s)
    return s


def main():
    root = ET.parse(os.path.join(RAW, "dumps", sys.argv[1] + ".xml")).getroot()
    for n in root.iter("node"):
        text, desc = n.get("text", ""), n.get("content-desc", "")
        if (text or desc or n.get("clickable") == "true") and n.get("bounds") != "[0,0][0,0]":
            flags = "".join(
                c if n.get(a) == "true" else "-"
                for a, c in (("clickable", "C"), ("checkable", "K"), ("checked", "X"), ("scrollable", "S"))
            )
            cls = n.get("class", "").split(".")[-1]
            pkg = n.get("package", "").split(".")[-1]
            print(f"{flags} {cls:<10} text={mask(text)[:80]!r} desc={mask(desc)[:50]!r} {pkg} {n.get('bounds')}")


if __name__ == "__main__":
    main()
