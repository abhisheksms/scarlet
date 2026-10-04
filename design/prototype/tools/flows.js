// The flows the brief names, driven with real clicks, typing and drags, and what the screen says after each step.
//
//   node flows.js                     in Chromium
//   node flows.js --browser=webkit    in WebKit (needs: npx playwright install webkit)
//
// run(d) takes a driver with ten small methods (see playwrightDriver below), so the same steps can be pointed
// at another automation tool. The WebKit run recorded in the README used WebDriver through this interface.
const { launch, open, URL } = require('./lib');

async function run(d, log) {
  log = log || console.log;
  let fails = 0, n = 0;
  const ok = (cond, msg) => { n++; if (!cond) { fails++; log('FAIL ' + msg); } else log('ok   ' + msg); };
  const go = hash => d.go(hash);
  const st = () => d.eval(() => { const s = window.scarletPrototype.state; return { mode: s.mode, role: s.role, screen: s.screen, overlay: s.overlay && (s.overlay.type + ':' + (s.overlay.kind || '')), paused: !!s.pausedUntil, notif: s.notif, an: s.an, scope: s.scope, allowed: s.allowed.map(e => e.n + '|' + (e.until ? 'T' : 'A')), allowOn: s.allowOn, rows: s.rows.length, period: s.period, summary: s.summary, ad: s.ad, hourSel: s.hourSel, daySel: s.daySel, repeat: s.repeat, field: s.field, fieldErr: s.fieldErr }; });
  // text as read: any run of spaces, no-break spaces included, is one space
  const text = sel => d.eval(s => { const e = document.querySelector(s); return e ? e.textContent.replace(/\s+/g, ' ').trim() : null; }, sel);
  const has = (sel, cls) => d.eval(a => document.querySelector(a[0]).classList.contains(a[1]), [sel, cls]);
  const click = (sel, i, o) => d.click(sel, i, o);
  const settle = () => d.wait(450);

  // 1. first launch, three beats
  await go('home-first.light.100.24h');
  ok((await text('#phone #status')).startsWith('Every call rings.'), 'first launch says Every call rings');
  ok((await text('#phone .bay')).startsWith('No account.'), 'the bay carries the privacy line at Off');
  await click('#phone .lever-row[data-v="block"]');
  ok((await st()).overlay === 'sys:role' && (await st()).mode === 'off', 'choosing Block without the role opens Android’s prompt; the lever waits');
  await click('#phone [data-act="back"]', 1); await settle();   // the user declines
  ok((await st()).mode === 'off' && !(await st()).overlay, 'declining leaves the lever at Off');
  await click('#phone .lever-row[data-v="block"]');
  await click('#phone [data-act="roleAccept"]'); await settle();
  ok((await st()).mode === 'block' && (await st()).role === 'held', 'accepting sets Block');
  ok((await text('#phone #status')) === 'Callers outside your contacts are rejected.', 'the display says rejected');
  ok(await has('#phone .lever-row[data-v="block"] .lamp', 'is-lit'), 'the Block lamp is lit');
  ok((await text('#phone .bay')).startsWith('Let every call ring for'), 'the bay now holds the pause keys');

  // 2. the exception: one tap
  await click('#phone [data-act="pause"][data-v="60"]'); await d.wait(200);
  ok((await text('#phone #status')) === 'Paused until 20:30. Every call rings.', 'pause 1 Hr: Paused until 20:30');
  ok(await has('#phone .lever-row[data-v="block"] .lamp', 'is-held'), 'the lamp is held');
  await click('#phone [data-act="resume"]'); await d.wait(200);
  ok((await text('#phone #status')) === 'Callers outside your contacts are rejected.', 'Resume restores the mode’s sentence');
  await click('#phone [data-act="pause"][data-v="1440"]'); await d.wait(200);
  ok((await text('#phone #status')) === 'Paused until tomorrow, 19:30. Every call rings.', '24 Hr: until tomorrow, 19:30');
  await click('#phone [data-act="resume"]');

  // 3. notifications: permission asked at the moment of switching on
  await click('#phone [data-act="notif"]');
  ok((await st()).overlay === 'sys:notifperm', 'switching notifications on asks Android');
  await click('#phone [data-act="permDeny"]'); await settle();
  ok((await st()).an === 'blocked' && (await text('#phone .strips')).includes('Switched off in Android’s settings'), 'denied: the row says so');
  await click('#phone .strips [data-act="sys"][data-v="asettings"]');
  await click('#phone [data-act="permAllow"]'); await settle();
  ok((await st()).an === 'allowed' && (await st()).notif === false, 'allowed in settings; the wish is still off');
  await click('#phone [data-act="notif"]');
  ok((await st()).notif === true && (await text('#phone .strips')).includes('On, for each stopped call'), 'now on');

  // 4. Options: who is filtered, repeat callers, the allow list by typing
  await click('#phone [data-act="go"][data-v="options"]');
  ok((await st()).screen === 'options', 'Options opens');
  await click('#phone [data-act="scope"][data-v="intl"]');
  await click('#phone [data-act="repeat"][data-v="5"]');
  await click('#phone [data-act="allowToggle"]');
  ok((await st()).allowOn && (await text('#phone .scr')).includes('No numbers yet.'), 'allow list on and empty');
  await d.focus('#numField');
  ok(await d.count('#phone [data-act="allowTyped"]') === 3, 'focusing the field shows the three length keys');
  await click('#phone [data-act="allowTyped"][data-v="always"]');
  ok((await st()).fieldErr && (await text('#phone .err')) === 'Enter a phone number.', 'an empty entry is refused in place');
  await d.type('#numField', '+91 98765 43210');
  ok(!(await st()).fieldErr && (await st()).field === '+91 98765 43210', 'typing clears the error and the field keeps what was typed');
  await click('#phone [data-act="allowTyped"][data-v="60"]');
  ok((await st()).allowed.join() === '+91 98765 43210|T' && (await st()).field === '', 'number added for an hour, field cleared');
  ok((await text('#phone .arow')).includes('Until 20:30'), 'the row says Until 20:30');
  await click('#phone .arow-x');
  ok((await st()).allowed.length === 0, 'removing a number');
  await click('#phone [data-act="back"]'); await settle();
  ok((await st()).screen === 'home' && (await text('#phone #status')) === 'International callers outside your contacts are rejected.', 'Home says international');
  ok((await text('#phone .strips')).includes('Repeat callers ring'), 'the Options strip summarises the exception');

  // 5. History: fix a mistake in two taps
  await go('his-heavy.light.100.24h');
  await click('#phone .hrow', 1);
  ok((await st()).overlay === 'sheet:' && (await text('#phone .sheet-num')) === '+91 80 4567 8901', 'a row opens the number’s sheet');
  await click('#phone [data-act="allow"][data-v="60"]'); await settle();
  ok(!(await st()).overlay && (await st()).allowed.some(a => a.startsWith('+91 80 4567 8901|T')), 'one key allows and closes');
  ok((await d.eval(() => document.querySelectorAll('#phone .hrow')[1].textContent.replace(/\s+/g, ' '))).includes('Rings until 20:30'), 'the row says Rings until 20:30');
  await click('#phone .hrow', 1);
  ok((await text('#phone .sheet')).includes('On the allow list') && (await text('#phone .sheet')).includes('Remove From Allow List'), 'the sheet now offers Remove From Allow List');
  await click('#phone [data-act="unallow"]'); await settle();
  ok(!(await st()).allowed.some(a => a.startsWith('+91 80 4567 8901')), 'removed again');
  await click('#phone .hrow', 0);
  await click('#phone [data-act="delCall"]'); await settle();
  ok((await st()).rows === 14, 'Delete removes one call');
  await click('#phone [data-act="askDeleteAll"]');
  ok((await text('#phone .dialog')).startsWith('Delete all history?Statistics are counted from the history, so they reset too.'), 'the confirmation says statistics reset too');
  await click('#phone .dialog [data-act="back"]'); await settle();
  ok((await st()).rows === 14, 'Cancel keeps the history');
  await click('#phone [data-act="askDeleteAll"]');
  await click('#phone [data-act="deleteAll"]'); await settle();
  ok((await st()).rows === 0 && (await text('#phone .empty')) === 'No calls blocked or silenced yet.', 'Delete All empties History');
  await click('#phone [data-act="back"]'); await settle();
  ok((await text('#phone .tiles')).includes('Nothing today') && (await text('#phone .tiles')).includes('Nothing yet'), 'Home’s tiles reset too');
  await click('#phone [data-act="go"][data-v="statistics"]');
  ok((await d.eval(() => document.querySelector('#phone .drums').getAttribute('aria-label'))) === '0' && await d.count('#phone .hdr [data-v="share"]') === 0, 'Statistics reads 000 and Share is gone');

  // 6. Statistics
  await go('st-heavy.light.100.24h');
  ok((await d.eval(() => document.querySelector('#phone .drums').getAttribute('aria-label'))) === '156', 'the counter reads 156');
  const body = await text('#phone .scr');
  for (const need of ['148 blocked', '8 silenced', '100 reached', 'Next: 250', 'About 1 hr 18 min, at 30 seconds a call', '34 calls', 'Fri: 5 blocked, 1 silenced', '12% fewer than the week before. Busiest around 11:00.', '121 calls', '9% more than the 30 days before', 'Most on Tuesday', 'Most around 11:00', '+91 140 123 4567', '9 calls', '6 calls', '4 calls', '3 calls', '2 calls'])
    ok(body.includes(need), 'Statistics shows: ' + need);
  await click('#phone .dcol', 3);
  ok((await text('#phone .scr')).includes('Tue: 8 blocked, 0 silenced'), 'tapping a day gives its numbers');
  await click('#phone [data-act="period"][data-v="7"]');
  ok((await st()).period === 7 && (await text('#phone .scr')).includes('5 calls'), '7 days drives the lists');
  const hrs = await d.box('#phone [data-hrs]');
  await d.clickAt(hrs.x + hrs.width * (11.5 / 24), hrs.y + hrs.height / 2);
  ok((await st()).hourSel === 11 && (await text('#phone .scr')).includes('11:00: 6 calls'), 'touching the hour chart reads an hour');
  await d.eval(() => { document.querySelector('#view').scrollTop = 0; });
  await click('#phone .hdr [data-v="share"]');
  ok((await text('#phone .standin')).includes('Call Blocker has stopped 156 calls from numbers outside my contacts: 148 blocked, 8 silenced.'), 'the share text');
  await click('#phone .standin [data-act="back"]'); await settle();

  // 7. Settings
  await go('set.light.100.24h');
  await click('#phone [data-act="summary"][data-v="monthly"]');
  ok((await st()).summary === 'monthly', 'summary set to monthly');
  await click('#phone [data-act="go"][data-v="licences"]');
  ok((await text('#phone .scr')).includes('Hanken Grotesk') && (await text('#phone .scr')).includes('SIL Open Font License 1.1'), 'Licences lists the typefaces');
  await click('#phone [data-act="back"]'); await settle();
  ok((await st()).screen === 'settings', 'Back returns to Settings');

  // 8. Later: buying Remove Ads
  await go('later-ads.light.100.24h');
  await click('#phone [data-act="sys"][data-v="buy"]');
  await click('#phone [data-act="bought"]'); await settle();
  ok((await st()).ad === 'removed' && await d.count('#phone .slot') === 0 && (await text('#phone .scr')).includes('Ads removed'), 'after buying: no slot, and the row says Ads removed');

  // 9. dragging the lever
  await go('home-block.light.100.24h');
  const hb = await d.box('#phone [data-handle]'), hx = hb.x + hb.width / 2, hy = hb.y + hb.height / 2;
  await d.drag(hx, hy, hx, hy - 60); await settle();
  ok((await st()).mode === 'silence', 'dragging the handle up one stop sets Silence');
  await d.drag(hx, hy - 62, hx, hy - 200); await settle();
  ok((await st()).mode === 'off', 'dragging to the top sets Off');
  const y0 = await d.eval(() => document.querySelector('#phone [data-handle]').getBoundingClientRect().top - document.querySelector('#phone .lever').getBoundingClientRect().top);
  ok(Math.abs(y0 - 13) < 0.6, 'the handle rests at the Off stop (' + y0 + ')');

  // 10. a device that cannot screen: the lever will not leave Off
  await go('home-cannot.light.100.24h');
  await click('#phone .lever-row[data-v="silence"]', null, { force: true }); await settle();
  ok((await st()).mode === 'off' && !(await st()).overlay, 'the locked lever stays at Off');

  // 11. a notification, tapped, opens History with Home beneath it
  await go('ntf.light.100.24h');
  await click('#phone .ncard', 0);
  ok((await st()).screen === 'history' && !(await st()).overlay, 'tapping a notification opens History');
  await click('#phone [data-act="back"]'); await settle();
  ok((await st()).screen === 'home', 'Back from there is Home');

  // 12. touch, where the driver can send it: a tap is a click, and the lever answers
  if (d.tap) {
    await go('home-block.light.100.24h');
    await d.tap('#phone .lever-row[data-v="silence"]'); await settle();
    ok((await st()).mode === 'silence', 'a tap on Silence sets it');
    await d.tap('#phone [data-act="go"][data-v="history"]');
    ok((await st()).screen === 'history', 'a tap on the History tile opens History');
  }

  // 13. the screenshot frame
  await go('home-block.light.100.24h.shot');
  ok(await d.eval(() => { const b = document.querySelector('#phone .sb-proto'); return document.documentElement.classList.contains('shot') && (!b || getComputedStyle(b).display === 'none'); }), 'shot hides the prototype’s own button');
  await go('home-block.light.100.24h');
  ok(await d.eval(() => !document.documentElement.classList.contains('shot')), 'and a link without it brings it back');

  return { fails, n };
}

// Playwright as the driver
function playwrightDriver(page) {
  const nth = (sel, i) => (i == null ? page.locator(sel).first() : page.locator(sel).nth(i));
  return {
    go: async hash => { await page.goto(URL + '#' + hash, { waitUntil: 'load' }); await page.evaluate(async () => { await document.fonts.ready; }); await page.waitForTimeout(150); },
    eval: (fn, arg) => page.evaluate(fn, arg),
    click: async (sel, i, o) => { await nth(sel, i).click(o && o.force ? { force: true } : {}); await page.waitForTimeout(60); },
    tap: async sel => { await page.locator(sel).first().tap(); await page.waitForTimeout(60); },
    count: sel => page.locator(sel).count(),
    focus: async sel => { await page.locator(sel).focus(); await page.waitForTimeout(80); },
    type: async (sel, text) => { await page.locator(sel).fill(text); await page.waitForTimeout(80); },
    box: async sel => { const l = page.locator(sel).first(); await l.scrollIntoViewIfNeeded(); return l.boundingBox(); },
    clickAt: async (x, y) => { await page.mouse.click(x, y); await page.waitForTimeout(80); },
    drag: async (x0, y0, x1, y1) => { await page.mouse.move(x0, y0); await page.mouse.down(); await page.mouse.move(x1, y1, { steps: 6 }); await page.mouse.up(); },
    wait: ms => page.waitForTimeout(ms),
  };
}

if (require.main === module) {
  (async () => {
    const { name, browser } = await launch();
    const { page, errors } = await open(browser, { width: 360, height: 800, touch: true });
    const r = await run(playwrightDriver(page));
    for (const e of errors) console.log('FAIL ' + e);
    const fails = r.fails + errors.length;
    await browser.close();
    console.log(fails ? `${name}: ${fails} of ${r.n} FAILED` : `${name}: all ${r.n} steps pass`);
    process.exit(fails ? 1 : 0);
  })().catch(e => { console.error(e); process.exit(2); });
}

module.exports = { run };
