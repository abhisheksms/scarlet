// Two browsers, one window size: is every element in the same place, and does every line break at the same word?
//
//   node same.js --size=402x874                      Chromium against WebKit (needs: npx playwright install webkit)
//   node same.js --size=412x905 --a=chromium --b=firefox
//   node same.js --size=402x874 --save=chromium.json     write one browser's measurements to a file
//   node same.js --against=chromium.json --with=other.json    compare two saved files
//
// Each state is drawn as the app alone at that size (the "shot" link), in the light theme, at both text sizes and both clocks.
// A difference is a box that is more than half a pixel away, or a block of text that breaks elsewhere.
const fs = require('fs');
const { open, flag } = require('./lib');

// runs in the page: every state's boxes and line breaks
const COLLECT = `(async () => {
  const P = window.scarletPrototype, out = {};
  await document.fonts.ready;
  let n = 0;
  for (const id of P.presets) for (const ts of ['100', '135']) for (const clock of ['24h', '12h']) {
    if (id === 'home-135' && ts === '100') continue;
    const link = [id, 'light', ts, clock, 'loaded', 'shot'].join('.');
    out[link] = P.dump(link);
    if (++n % 8 === 0) await new Promise(r => setTimeout(r, 0));
  }
  return JSON.stringify({ browser: navigator.userAgent, window: [innerWidth, innerHeight], dpr: devicePixelRatio, states: out });
})()`;

function compare(a, b, tol) {
  const diffs = [];
  let boxes = 0, worst = 0;
  for (const link of Object.keys(a.states)) {
    const A = a.states[link], B = b.states[link];
    if (!B) { diffs.push(`${link}: missing in the second`); continue; }
    if (A.lines !== B.lines) diffs.push(`${link}: text breaks at different words`);
    const mb = new Map(B.boxes.map(x => [x[0], x]));
    if (A.boxes.length !== B.boxes.length) diffs.push(`${link}: ${A.boxes.length} elements drawn in the first, ${B.boxes.length} in the second`);
    let shown = 0;
    for (const x of A.boxes) {
      const y = mb.get(x[0]);
      if (!y) continue;
      boxes++;
      const d = Math.max(Math.abs(x[1] - y[1]), Math.abs(x[2] - y[2]), Math.abs(x[3] - y[3]), Math.abs(x[4] - y[4]));
      worst = Math.max(worst, d);
      if (d > tol && shown++ < 3) diffs.push(`${link}: ${x[0]} is at ${x.slice(1).join(', ')} in the first and ${y.slice(1).join(', ')} in the second (x, y, width, height)`);
    }
  }
  return { diffs, boxes, worst, states: Object.keys(a.states).length };
}

async function collect(name, width, height, scale) {
  const pw = require('playwright');
  if (!pw[name]) throw new Error(`Unknown browser "${name}".`);
  const browser = await pw[name].launch();
  const { page } = await open(browser, { width, height, scale, hash: 'home-block.shot' });
  const data = JSON.parse(await page.evaluate(COLLECT));
  await browser.close();
  return data;
}

if (require.main === module) {
  (async () => {
    const tol = +flag('tolerance', 0.5);
    let a, b;
    if (flag('against')) {
      a = JSON.parse(fs.readFileSync(flag('against'), 'utf8'));
      b = JSON.parse(fs.readFileSync(flag('with'), 'utf8'));
    } else {
      const [w, h] = String(flag('size', '360x800')).split('x').map(Number), scale = +flag('scale', 1);
      a = await collect(flag('a', 'chromium'), w, h, scale);
      if (flag('save')) { fs.writeFileSync(flag('save'), JSON.stringify(a)); console.log(`Saved ${Object.keys(a.states).length} states from ${a.browser}`); return; }
      b = await collect(flag('b', 'webkit'), w, h, scale);
    }
    const r = compare(a, b, tol);
    console.log(`first:  ${a.browser}, window ${a.window.join(' x ')}, pixel ratio ${a.dpr}`);
    console.log(`second: ${b.browser}, window ${b.window.join(' x ')}, pixel ratio ${b.dpr}`);
    console.log(`${r.states} states, ${r.boxes} boxes compared. Furthest apart: ${r.worst.toFixed(2)} px. Differences over ${tol} px or in line breaks: ${r.diffs.length}.`);
    r.diffs.slice(0, 60).forEach(d => console.log('  ' + d));
    process.exit(r.diffs.length ? 1 : 0);
  })().catch(e => { console.error(e); process.exit(2); });
}

module.exports = { COLLECT, compare };
