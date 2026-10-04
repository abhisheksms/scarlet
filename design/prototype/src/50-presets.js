/* ───────────── every surface and state in §2, one tap each ───────────── */

const deep = s => { S.screen = s; S.stack = [{ s: 'home', y: 0 }]; };
const PRESETS = [
  // id, group, name, set-up
  ['home-first', 'Home', 'Off, first launch', () => resetApp('new')],
  ['home-prompt', 'Home', 'First launch, Android’s prompt over it', () => { resetApp('new'); S.overlay = { type: 'sys', kind: 'role', pending: 'block' }; }],
  ['home-on', 'Home', 'First launch, on', () => { resetApp('new'); S.role = 'held'; S.mode = 'block'; }],
  ['home-consent', 'Home', 'First launch where consent is required', () => { resetApp('new'); S.consent = true; S.overlay = { type: 'sys', kind: 'consent' }; }],
  ['home-silence', 'Home', 'Silence', () => { resetApp('heavy'); S.mode = 'silence'; }],
  ['home-block', 'Home', 'Block, heavy user', () => resetApp('heavy')],
  ['home-off', 'Home', 'Off, heavy user', () => { resetApp('heavy'); S.mode = 'off'; }],
  ['home-intl-silence', 'Home', 'Silence, international only', () => { resetApp('heavy'); S.mode = 'silence'; S.scope = 'intl'; }],
  ['home-intl-block', 'Home', 'Block, international only', () => { resetApp('heavy'); S.scope = 'intl'; }],
  ['home-paused', 'Home', 'Paused', () => { resetApp('heavy'); S.pausedUntil = at(0, 20, 30); }],
  ['home-paused-24', 'Home', 'Paused until tomorrow', () => { resetApp('heavy'); S.pausedUntil = at(1, 19, 30); }],
  ['home-role', 'Home', 'Role missing', () => { resetApp('heavy'); S.role = 'missing'; }],
  ['home-cannot', 'Home', 'Cannot screen', () => { resetApp('new'); S.role = 'unavailable'; }],
  ['home-notif', 'Home', 'Notifications blocked', () => { resetApp('heavy'); S.an = 'blocked'; }],
  ['home-three', 'Home', 'Block, three calls', () => resetApp('three')],
  ['home-thousands', 'Home', 'Block, thousands', () => resetApp('thousands')],
  ['home-135', 'Home', 'Block at 135% text, dark', () => { resetApp('heavy'); S.ts = 1.35; S.theme = 'dark'; }],

  ['opt-new', 'Options', 'Defaults', () => { resetApp('new'); deep('options'); }],
  ['opt-heavy', 'Options', 'Repeat callers on, two numbers allowed', () => { resetApp('heavy'); deep('options'); }],
  ['opt-intl', 'Options', 'International only', () => { resetApp('heavy'); S.scope = 'intl'; deep('options'); }],
  ['opt-empty', 'Options', 'Allow list on and empty', () => { resetApp('new'); S.allowOn = true; deep('options'); }],
  ['opt-adding', 'Options', 'Adding a number', () => { resetApp('heavy'); deep('options'); S.field = N.c; S.fieldOpen = true; }],
  ['opt-invalid', 'Options', 'An invalid entry', () => { resetApp('heavy'); deep('options'); S.field = '+'; S.fieldOpen = true; S.fieldErr = true; }],

  ['his-empty', 'History', 'Empty', () => { resetApp('new'); deep('history'); }],
  ['his-few', 'History', 'A few rows', () => { resetApp('three'); deep('history'); }],
  ['his-heavy', 'History', 'A heavy day, one number repeating', () => { resetApp('heavy'); deep('history'); }],
  ['his-sheet', 'History', 'Number details', () => { resetApp('heavy'); deep('history'); S.overlay = { type: 'sheet', num: N.b, callId: 2 }; }],
  ['his-allowed', 'History', 'The moment after Allow', () => { resetApp('heavy'); allowAdd(N.b, 60); deep('history'); }],
  ['his-sheet-allowed', 'History', 'Number details, on the allow list', () => { resetApp('heavy'); deep('history'); S.overlay = { type: 'sheet', num: N.d, callId: 5 }; }],
  ['his-nonumber', 'History', 'A call with no number', () => { resetApp('heavy'); deep('history'); S.overlay = { type: 'sheet', num: null, callId: 8 }; }],
  ['his-confirm', 'History', 'Delete All, the confirmation', () => { resetApp('heavy'); deep('history'); S.overlay = { type: 'dialog' }; }],

  ['st-none', 'Statistics', 'Nothing yet', () => { resetApp('new'); deep('statistics'); }],
  ['st-three', 'Statistics', 'Three calls', () => { resetApp('three'); deep('statistics'); }],
  ['st-heavy', 'Statistics', 'Heavy user', () => { resetApp('heavy'); deep('statistics'); }],
  ['st-thousands', 'Statistics', 'Thousands', () => { resetApp('thousands'); deep('statistics'); }],
  ['st-sheet', 'Statistics', 'A frequent number’s details', () => { resetApp('heavy'); deep('statistics'); S.overlay = { type: 'sheet', num: N.a }; }],
  ['st-share', 'Statistics', 'Share', () => { resetApp('heavy'); deep('statistics'); S.overlay = { type: 'sys', kind: 'share' }; }],

  ['set', 'Settings', 'Settings', () => { resetApp('heavy'); deep('settings'); }],
  ['set-consent', 'Settings', 'With Privacy Choices', () => { resetApp('heavy'); S.consent = true; deep('settings'); }],
  ['set-blocked', 'Settings', 'Notifications blocked', () => { resetApp('heavy'); S.an = 'blocked'; deep('settings'); }],
  ['lic', 'Settings', 'Licences', () => { resetApp('heavy'); S.screen = 'licences'; S.stack = [{ s: 'home', y: 0 }, { s: 'settings', y: 0 }]; }],

  ['ntf', 'Notifications', 'In the shade', () => { resetApp('heavy'); S.overlay = { type: 'shade', kind: 'shade' }; }],
  ['ntf-lock', 'Notifications', 'On a locked screen', () => { resetApp('heavy'); S.overlay = { type: 'shade', kind: 'lock' }; }],

  ['later-ads', 'Later', 'Remove Ads, the Settings rows', () => { resetApp('heavy'); S.later.ads = true; deep('settings'); }],
  ['later-buy', 'Later', 'Remove Ads, the purchase sheet', () => { resetApp('heavy'); S.later.ads = true; deep('settings'); S.overlay = { type: 'sys', kind: 'buy' }; }],
  ['later-noads', 'Later', 'Ads removed, Home', () => { resetApp('heavy'); S.later.ads = true; S.ad = 'removed'; }],
  ['later-noads-set', 'Later', 'Ads removed, Settings', () => { resetApp('heavy'); S.later.ads = true; S.ad = 'removed'; deep('settings'); }],
  ['later-india', 'Later', 'India rules, in Options', () => { resetApp('heavy'); S.later.india = true; S.india = { b140: true, a160: true }; deep('options'); }],
  ['later-hindi', 'Later', 'Hindi, Home', () => { resetApp('heavy'); S.later.hindi = true; }],
  ['later-hindi-first', 'Later', 'Hindi, first launch', () => { resetApp('new'); S.later.hindi = true; }],
];

function applyPreset(id, quiet) {
  const p = PRESETS.find(x => x[0] === id);
  if (!p) return false;
  S.later = { india: false, ads: false, hindi: false }; S.consent = false;
  if (S.ad === 'removed') S.ad = 'loaded';
  p[3]();
  S.preset = id; hDepth = 0; afterClose = null; closing = false;
  render({ still: true });
  const v = $('#view'); if (v) v.scrollTop = 0;
  if (!quiet) { try { history.replaceState(history.state, '', '#' + id); } catch (e) { /* no deep links here */ } }
  return true;
}

// Deep links: one bare token, parts joined by dots. #his-sheet.dark.135.12h.empty.reduced
// "shot" draws the app alone at the window's size, edge to edge, for screenshots. On a phone, "frame" and "screen" choose
// between the 360 × 800 frame and the app at the phone's own width.
function applyLink(link) {
  const parts = String(link || '').replace(/^#/, '').split('.').filter(Boolean);
  if (!parts.length) return false;
  S.shot = false;
  for (const t of parts.slice(1)) {
    if (t === 'light' || t === 'dark') S.theme = t;
    else if (t === '100') S.ts = 1; else if (t === '135') S.ts = 1.35;
    else if (t === '24h') S.clock = 24; else if (t === '12h') S.clock = 12;
    else if (t === 'reduced') S.motion = 'reduced'; else if (t === 'full') S.motion = 'full';
    else if (t === 'shot') S.shot = true;
    else if (t === 'frame') S.frame = true; else if (t === 'screen') S.frame = false;
  }
  const ok = applyPreset(parts[0], true);
  for (const t of parts.slice(1)) if (t === 'loaded' || t === 'empty' || t === 'removed') S.ad = t;
  if (parts[0] === 'home-135') { for (const t of parts.slice(1)) { if (t === 'light' || t === 'dark') S.theme = t; if (t === '100') S.ts = 1; } }
  render({ still: true });
  return ok;
}
const applyHash = () => applyLink(location.hash);
