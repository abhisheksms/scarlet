// Shared by the tools: where the prototype is, which browser to drive, and how to open a page on it.
const path = require('path');
const { pathToFileURL } = require('url');

const FILE = path.resolve(__dirname, '..', 'scarlet-prototype.html');
// The built file by default. PROTO_URL points the tools at a served copy instead.
const URL = process.env.PROTO_URL || pathToFileURL(FILE).href;

// --name=value or --name on the command line
function flag(name, fallback) {
  const hit = process.argv.find(a => a === '--' + name || a.startsWith('--' + name + '='));
  if (!hit) return fallback;
  return hit.includes('=') ? hit.slice(hit.indexOf('=') + 1) : true;
}
const words = () => process.argv.slice(2).filter(a => !a.startsWith('--'));

// chromium, webkit or firefox: whichever Playwright has installed. --browser=webkit, or PROTO_BROWSER.
async function launch() {
  const name = flag('browser', process.env.PROTO_BROWSER || 'chromium');
  const pw = require('playwright');
  if (!pw[name] || typeof pw[name].launch !== 'function') throw new Error(`Unknown browser "${name}". Use chromium, webkit or firefox.`);
  return { name, browser: await pw[name].launch() };
}

// A page on the prototype at one window size. errors collects anything the page throws or logs as an error.
async function open(browser, o) {
  const ctx = await browser.newContext({ viewport: { width: o.width, height: o.height }, deviceScaleFactor: o.scale || 1, hasTouch: !!o.touch });
  const page = await ctx.newPage();
  const errors = [];
  page.on('pageerror', e => errors.push('page error: ' + e.message));
  page.on('console', m => { if (m.type() === 'error') errors.push('console error: ' + m.text()); });
  await page.goto(URL + (o.hash ? '#' + o.hash : ''), { waitUntil: 'load' });
  await page.evaluate(async () => { await document.fonts.ready; });
  await page.waitForTimeout(200);
  return { page, ctx, errors };
}

module.exports = { FILE, URL, flag, words, launch, open };
