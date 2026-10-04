// The layout rules of the brief's §4, measured over every screen and state.
// The measuring is the prototype's own self-check (src/70-selftest.js), so a phone's browser can run the same thing from #selftest.
//
//   node check.js                     the 360 x 800 frame, then the app alone at 360 x 800, 402 x 874 and 412 x 905
//   node check.js --browser=webkit    the same in WebKit (needs: npx playwright install webkit)
//   node check.js --size=393x852      one more window size
//   node check.js his                 only the screens whose id contains "his"
const { launch, open, flag, words } = require('./lib');

(async () => {
  const { name, browser } = await launch();
  const filter = words()[0] || '';
  const extra = flag('size', '');
  // The frame on the bench is the reference: exactly 360 x 800 with the gesture inset. "shot" gives the same frame in a phone-sized window.
  const runs = [['the 360 x 800 frame', 1280, 900, ''], ['360 x 800', 360, 800, 'shot'], ['402 x 874 (iPhone 18 Pro)', 402, 874, 'shot'], ['412 x 905 (OnePlus 12)', 412, 905, 'shot']];
  if (extra) { const [w, h] = String(extra).split('x').map(Number); runs.push([`${w} x ${h}`, w, h, 'shot']); }
  let bad = 0;
  for (const [label, width, height, mode] of runs) {
    const { page, ctx, errors } = await open(browser, { width, height, touch: width < 600, hash: mode ? 'home-block.' + mode : '' });
    const r = await page.evaluate(f => window.scarletPrototype.selfTest({ filter: f }), filter);
    const problems = r.problems.concat(errors);
    bad += problems.length;
    console.log(`${name}, ${label}: ${r.states} states, ${problems.length} problems, ${(r.ms / 1000).toFixed(1)} s. App area ${r.app.join(' x ')}. Same as the reference: ${r.sameChecked ? (r.problems.some(p => /reference browser/.test(p)) ? 'NO' : 'yes') : 'not compared'}, signature ${r.signature}.`);
    for (const p of problems) console.log('  ' + p);
    if (flag('home')) for (const g in r.home) console.log('  ' + g + ': lever top / display / strips bottom = ' + r.home[g]);
    await ctx.close();
  }
  await browser.close();
  console.log(bad ? `PROBLEMS: ${bad}` : 'NO PROBLEMS');
  process.exit(bad ? 1 : 0);
})().catch(e => { console.error(e); process.exit(2); });
