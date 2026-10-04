// Records how the reference browser draws the design, and writes it into the prototype (src/75-reference.js).
// The self-check then compares any other browser, or a phone, with it: where every block of text breaks, and where Home's blocks sit.
//
//   node reference.js      run after a change that is meant to move text, then run check.js in every browser
//
// The reference browser is Chromium, the engine of Chrome on Android.
const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');
const { open } = require('./lib');

(async () => {
  const { chromium } = require('playwright');
  const browser = await chromium.launch();
  const { page, errors } = await open(browser, { width: 1280, height: 900 });
  const r = await page.evaluate(() => window.scarletPrototype.selfTest());
  const version = browser.version();
  await browser.close();
  const other = r.problems.filter(p => !/reference browser/.test(p)).concat(errors);
  if (other.length) { console.log('Not written. The layout check has problems of its own:'); other.forEach(p => console.log('  ' + p)); process.exit(1); }
  const ref = { key: r.reference.key, lines: r.reference.lines, home: r.reference.home, frame: r.reference.frame, by: 'Chromium ' + version };
  const out = '/* Written by tools/reference.js. How the reference browser draws the design in a 360 × 800 frame:\n'
    + '   lines is one five-character hash per screen, text size and clock, of where every block of text breaks;\n'
    + '   home is lever top / display height / strips bottom; frame is Home with heavy data at 100% text, which the spec quotes.\n'
    + '   The self-check compares the browser it runs in with these. */\n'
    + 'const REFERENCE = ' + JSON.stringify(ref, null, 1) + ';\n';
  const file = path.resolve(__dirname, '..', 'src', '75-reference.js');
  const before = fs.existsSync(file) ? fs.readFileSync(file, 'utf8') : '';
  fs.writeFileSync(file, out);
  const build = path.resolve(__dirname, '..', 'build.py');
  let built = false;
  for (const py of ['python3', 'python', 'py']) { const b = spawnSync(py, [build], { stdio: 'inherit' }); if (!b.error && b.status === 0) { built = true; break; } }
  console.log((before === out ? 'Reference unchanged' : 'Reference written') + `: signature ${r.signature}, by ${ref.by}.` + (built ? '' : ' Could not run build.py: run it now.'));
})().catch(e => { console.error(e); process.exit(2); });
