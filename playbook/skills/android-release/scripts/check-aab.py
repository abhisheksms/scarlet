#!/usr/bin/env python3
"""Check that every 64-bit native library is 16 KB page-aligned.

Play rejects new apps whose arm64 libraries load on 4 KB boundaries (rule in
force since 31 Aug 2026). Config can claim anything; this reads the actual ELF
program headers out of the shipped .so files.

    unzip -q app-release.aab -d aab
    python3 docs/launch/check-aab.py "aab/base/lib/arm64-v8a/*.so"

Exits non-zero if any library would be rejected.
"""
import glob
import os
import struct
import sys
from collections import Counter

PT_LOAD = 1
REQUIRED_ALIGN = 16 * 1024


def max_load_align(path):
    """Largest PT_LOAD p_align in a 64-bit little-endian ELF.

    Returns None for a file that is not a 64-bit ELF at all — a 32-bit (v7a)
    library, which the 16 KB rule does not cover.
    """
    with open(path, "rb") as f:
        data = f.read()
    if data[:4] != b"\x7fELF" or data[4] != 2:
        return None
    e_phoff = struct.unpack_from("<Q", data, 0x20)[0]
    e_phentsize = struct.unpack_from("<H", data, 0x36)[0]
    e_phnum = struct.unpack_from("<H", data, 0x38)[0]
    aligns = [
        struct.unpack_from("<Q", data, e_phoff + i * e_phentsize + 0x30)[0]
        for i in range(e_phnum)
        if struct.unpack_from("<I", data, e_phoff + i * e_phentsize)[0] == PT_LOAD
    ]
    return max(aligns) if aligns else None


def main(pattern):
    paths = sorted(glob.glob(pattern))
    if not paths:
        print(f"no libraries matched {pattern!r}")
        return 2

    failures = []
    passes = []
    skipped = []
    for path in paths:
        align = max_load_align(path)
        if align is None:
            # Not a 64-bit ELF: the 16 KB requirement does not apply to it.
            skipped.append(os.path.basename(path))
        elif align < REQUIRED_ALIGN:
            failures.append((os.path.basename(path), align))
        else:
            passes.append(align)

    checked = len(passes) + len(failures)
    print(f"{len(passes)} of {checked} 64-bit libraries are >= {REQUIRED_ALIGN}-byte aligned")
    if passes:
        print("alignments:", dict(Counter(passes)))
    if skipped:
        print(f"skipped {len(skipped)} non-64-bit libraries (rule does not apply)")
    for name, align in failures:
        print(f"  REJECTED {name} align={align}")

    if not checked:
        print("nothing 64-bit was checked — point this at lib/arm64-v8a/*.so")
        return 2
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1] if len(sys.argv) > 1 else "aab/base/lib/arm64-v8a/*.so"))
