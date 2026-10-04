// Screenshots of the app alone, edge to edge, as it stands on a phone of the given size.
//
//   node snap.js                                   every screen and state: light and dark, 100% text, 24-hour clock
//   node snap.js --sizes=100,135 --clocks=24h,12h  more of them
//   node snap.js his-sheet.dark.135.12h set        only these (a screen id, or an id with switches)
//   node snap.js --out=../../design_handoff_scarlet/screens --size=360x800 --scale=3
//
// Defaults: 360 x 800 at 3 pixels per dp, into tools/shots/. One PNG per state, named by its address.
const fs = require('fs');
const path = require('path');
const { launch, open, flag, words } = require('./lib');

(async () => {
  const [width, height] = String(flag('size', '360x800')).split('x').map(Number);
  const out = path.resolve(process.cwd(), String(flag('out', path.join(__dirname, 'shots'))));
  const themes = String(flag('themes', 'light,dark')).split(','), sizes = String(flag('sizes', '100')).split(','), clocks = String(flag('clocks', '24h')).split(',');
  fs.mkdirSync(out, { recursive: true });
  const { browser } = await launch();
  const { page, errors } = await open(browser, { width, height, scale: +flag('scale', 3), touch: true, hash: 'home-block.shot' });
  const all = await page.evaluate(() => window.scarletPrototype.presets);
  const links = [];
  const asked = words();
  for (const a of asked.length ? asked : all) {
    if (a.includes('.')) { links.push(a); continue; }
    if (!all.includes(a)) { console.log(`No screen is called ${a}. The ids: ${all.join(' ')}`); process.exit(2); }
    for (const theme of themes) for (const ts of sizes) for (const clock of clocks) links.push([a, theme, ts, clock].join('.'));
  }
  for (const link of links) {
    await page.evaluate(l => window.scarletPrototype.link(l + '.shot'), link);
    await page.waitForTimeout(80);
    await page.screenshot({ path: path.join(out, link + '.png') });
  }
  await browser.close();
  for (const e of errors) console.log('  ' + e);
  console.log(`${links.length} screenshots, ${width} x ${height}, in ${out}`);
  process.exit(errors.length ? 1 : 0);
})().catch(e => { console.error(e); process.exit(2); });
