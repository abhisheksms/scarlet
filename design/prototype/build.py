#!/usr/bin/env python3
"""Builds scarlet-prototype.html, one self-contained file, from src/ and fonts/.

  python3 build.py                  write scarlet-prototype.html beside this script
  python3 build.py --check          exit 1 if scarlet-prototype.html is not what the sources build
  python3 build.py --artifact PATH  also write the page as a Claude Artifact body (no doctype, head or body tags)

Standard library only. The output is the same bytes on every platform.
"""
import base64, pathlib, sys

root = pathlib.Path(__file__).resolve().parent
src, fonts, out = root / 'src', root / 'fonts', root / 'scarlet-prototype.html'
TITLE = 'Scarlet Prototype'


def text(p):
    return p.read_text(encoding='utf-8').replace('\r\n', '\n')


def b64(p):
    return base64.b64encode(p.read_bytes()).decode('ascii')


def write(p, s):
    with open(p, 'w', encoding='utf-8', newline='\n') as f:
        f.write(s)


def build():
    font_css = (
        "/* Fonts embedded so the file needs no network.\n"
        "   Hanken Grotesk: Copyright 2021 The Hanken Grotesk Project Authors (https://github.com/marcologous/hanken-grotesk).\n"
        "   Noto Sans Devanagari: Copyright 2022 The Noto Project Authors (https://github.com/notofonts/devanagari).\n"
        "   Both are licensed under the SIL Open Font License, Version 1.1: https://openfontlicense.org */\n"
        # Chrome on Android moves a pixel from a font's ascent to its descent whenever the descent rounds down, which sets
        # text one pixel higher than Safari does. A descent of zero cannot round down. The ascent becomes ascent minus descent
        # (1000 - 303 units), so the baseline stays exactly where the font's own metrics put it. Safari ignores both lines.
        "@font-face{font-family:'Hanken Grotesk';font-style:normal;font-weight:100 900;font-display:block;"
        "ascent-override:69.7%%;descent-override:0%%;line-gap-override:0%%;"
        "src:url(data:font/woff2;base64,%s) format('woff2');"
        "unicode-range:U+0000-00FF,U+0131,U+0152-0153,U+02BB-02BC,U+02C6,U+02DA,U+02DC,U+0304,U+0308,U+0329,U+2000-206F,U+20AC,U+2122,U+2191,U+2193,U+2212,U+2215,U+FEFF,U+FFFD}\n"
        "@font-face{font-family:'Noto Sans Devanagari';font-style:normal;font-weight:100 900;font-display:block;"
        "src:url(data:font/woff2;base64,%s) format('woff2');"
        "unicode-range:U+0900-097F,U+1CD0-1CF9,U+200C-200D,U+20A8,U+20B9,U+20F0,U+25CC,U+A830-A839,U+A8E0-A8FF,U+11B00-11B09}\n"
    ) % (b64(fonts / 'hanken-grotesk-latin-wght-normal.woff2'), b64(fonts / 'noto-sans-devanagari-devanagari-wght-normal.woff2'))
    css = font_css + text(src / 'app.css') + '\n' + text(src / 'shell.css')
    js = '\n'.join(text(p) for p in sorted(src.glob('*.js'), key=lambda p: p.name))
    body = text(src / 'body.html')
    assert '</script' not in js.lower(), 'the script would close early'
    script = "<script>\n(() => {\n'use strict';\n" + js + "\n})();\n</script>"
    artifact = f"<title>{TITLE}</title>\n<style>\n{css}\n</style>\n{body}\n{script}\n"
    standalone = (
        "<!doctype html>\n<html lang=\"en\">\n<head>\n<meta charset=\"utf-8\">\n"
        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, viewport-fit=cover\">\n"
        "<meta name=\"color-scheme\" content=\"light dark\">\n"
        # the phone numbers on screen are made up: an iPhone must not turn them into links that dial
        "<meta name=\"format-detection\" content=\"telephone=no, date=no, address=no, email=no\">\n"
        # added to a phone's home screen, it opens without the browser's bars
        "<meta name=\"mobile-web-app-capable\" content=\"yes\">\n"
        "<meta name=\"apple-mobile-web-app-capable\" content=\"yes\">\n"
        "<meta name=\"apple-mobile-web-app-status-bar-style\" content=\"default\">\n"
        "<meta name=\"apple-mobile-web-app-title\" content=\"Scarlet\">\n"
        f"<title>{TITLE}</title>\n"
        "<style>\n:root{padding:env(safe-area-inset-top,0px) 0 env(safe-area-inset-bottom,0px);box-sizing:border-box}\n[hidden]{display:none!important}\n"
        f"{css}\n</style>\n</head>\n<body>\n{body}\n{script}\n</body>\n</html>\n")
    return standalone, artifact, js, css


def main(argv):
    standalone, artifact, js, css = build()
    if '--check' in argv:
        same = out.exists() and text(out) == standalone
        print('scarlet-prototype.html ' + ('matches its sources' if same else 'does NOT match its sources: run python3 build.py'))
        return 0 if same else 1
    write(out, standalone)
    if '--artifact' in argv:
        target = pathlib.Path(argv[argv.index('--artifact') + 1])
        target.parent.mkdir(parents=True, exist_ok=True)
        write(target, artifact)
    print('scarlet-prototype.html: %.0f KB (script %d lines, styles %d lines)' % (len(standalone.encode('utf-8')) / 1024, js.count('\n'), css.count('\n')))
    return 0


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
