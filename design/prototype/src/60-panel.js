/* ───────────── the control panel: switches, the index of screens, and the written spec ───────────── */

const TABS = [['switches', 'Switches'], ['screens', 'Screens'], ['values', 'Values'], ['talkback', 'TalkBack'], ['components', 'Components'], ['words', 'Words'], ['notes', 'Notes']];
const SWITCHES = [
  ['frame', 'On a phone', [[true, '360 × 800 frame'], [false, 'This screen']], 'The frame is the same picture on every phone. This screen is the app at the phone’s own width'],
  ['theme', 'Theme', [['light', 'Light'], ['dark', 'Dark']]],
  ['ts', 'Text size', [[1, '100%'], [1.35, '135%']]],
  ['clock', 'Clock', [[24, '24-hour'], [12, '12-hour']]],
  ['data', 'Data', [['new', 'New user'], ['three', 'Three calls'], ['heavy', 'Heavy user'], ['thousands', 'Thousands']], 'Choosing one resets the app'],
  ['ad', 'Ad slot', [['loaded', 'Loaded'], ['empty', 'Empty'], ['removed', 'Removed']]],
  ['motion', 'Motion', [['full', 'Full'], ['reduced', 'Reduced']]],
  ['role', 'Screening role', [['held', 'Held'], ['missing', 'Not held'], ['unavailable', 'Device can’t screen']], 'What Android reports'],
  ['an', 'Android notifications', [['notAsked', 'Not asked'], ['allowed', 'Allowed'], ['blocked', 'Blocked']], 'What Android reports'],
  ['consent', 'Consent required', [[false, 'No'], [true, 'Yes']], 'Adds Privacy Choices to Settings'],
  ['later.india', 'Later: India rules', [[false, 'Off'], [true, 'On']], 'Two rows in Options'],
  ['later.ads', 'Later: Remove Ads', [[false, 'Off'], [true, 'On']], 'Rows in Settings'],
  ['later.hindi', 'Later: Hindi', [[false, 'Off'], [true, 'On']], 'Home only'],
];
const getSw = k => k.indexOf('later.') === 0 ? S.later[k.slice(6)] : S[k];
function setSw(k, raw) {
  const def = SWITCHES.find(s => s[0] === k), v = def[2].map(o => o[0]).find(x => String(x) === raw);
  if (k.indexOf('later.') === 0) S.later[k.slice(6)] = v;
  else if (k === 'data') resetApp(v);
  else S[k] = v;
  if (k === 'role' && v === 'unavailable') { S.mode = 'off'; S.pausedUntil = null; }
  if (k === 'later.ads' && !v && S.ad === 'removed') S.ad = 'loaded';
  if (k !== 'frame') S.preset = null;
  render({ still: true });
}

function openPanel() { $('#panel').classList.add('is-open'); drawPanel(true); const x = $('#panelClose'); if (x) x.focus(); }
function closePanel() { $('#panel').classList.remove('is-open'); }

let panelSig = '';
function drawPanel(force) {
  const panel = $('#panel');
  if (!panel || S.testing) return;
  const docked = window.matchMedia('(min-width:1020px)').matches;
  if (!docked && !panel.classList.contains('is-open')) return;
  const dyn = S.tab === 'switches' ? SWITCHES.map(s => getSw(s[0])).join('|') : S.tab === 'screens' ? String(S.preset) : S.tab === 'components' ? [S.theme, S.ts, S.clock].join('|') : '';
  const sig = S.tab + '#' + dyn;
  if (!force && sig === panelSig) return;
  panelSig = sig;
  $('#tabs').innerHTML = TABS.map(t => `<button role="tab" aria-selected="${S.tab === t[0]}" data-tab="${t[0]}">${t[1]}</button>`).join('');
  const body = $('#panelBody'), keep = force ? 0 : body.scrollTop;
  body.innerHTML = '<div class="pb">' + PANELS[S.tab]() + '</div>';
  tieWidows(body);
  balanceText(body);
  body.scrollTop = keep;
}

/* ── Switches ── */
const PANELS = {};
PANELS.switches = () => SWITCHES.map(s => `<div class="swrow"><span>${s[1]}${s[3] ? `<small>${s[3]}</small>` : ''}</span><div class="seg" role="group" aria-label="${s[1]}">${s[2].map(o => `<button data-sw="${s[0]}" data-v="${o[0]}" aria-pressed="${getSw(s[0]) === o[0]}">${o[1]}</button>`).join('')}</div></div>`).join('')
  + `<p class="muted" style="margin-top:14px">The clock is fixed at Friday 2 October 2026, 19:30, so every frame can be reproduced. A pause of one hour ends at ${S.clock === 24 ? '20:30' : '8:30 PM'}.</p>
<p class="muted">On a phone the prototype opens as the 360 × 800 frame: real size where the screen allows, shrunk where it does not, and the same picture on every phone. This screen lays the app out at the phone’s own width instead, as the app itself will.</p>
<p class="muted">Dragging the lever handle works as well as tapping a row. The system back gesture goes back inside the prototype where the browser allows it.</p>
<p>${fsCan() ? '<button class="pbtn" id="fsBtn" data-fs>Full Screen</button> <span class="muted" id="fsMsg">A browser tab is shorter than the app by the height of its bars. Full screen shows the real 800 dp.</span>'
    : '<span class="muted">A browser tab is shorter than the app by the height of its bars, and full screen is not available here. Inside a viewer, open the HTML file itself in a browser. On an iPhone, in Safari, Share then Add to Home Screen opens it without the bars.</span>'}</p>
<p><button class="pbtn" data-selftest>Run Self-Check</button> <span class="muted">Measures every screen and state in this browser against the layout rules. It takes a few seconds and returns the app to Home.</span></p>`;

/* ── Screens ── */
PANELS.screens = () => {
  const groups = [];
  for (const p of PRESETS) { let g = groups.find(x => x[0] === p[1]); if (!g) { g = [p[1], []]; groups.push(g); } g[1].push(p); }
  return `<p class="muted">Every surface and state in §2 of the brief. A choice sets the data and the state; theme, text size, clock, motion and the ad slot stay as switched.</p>`
    + groups.map(g => `<h2>${g[0]}</h2><div class="plist">${g[1].map(p => `<button data-preset="${p[0]}" aria-current="${S.preset === p[0]}">${esc(p[2])}</button>`).join('')}</div>`).join('');
};

/* ── Values ── */
// Home's measurements in the 360 × 800 frame, as the reference browser drew them (tools/reference.js)
const frameRef = () => (typeof REFERENCE === 'object' && REFERENCE && REFERENCE.frame) || null;
const ROLES = [
  ['surface', '#D2D6D1', '#1C1E21', 'The panel: every screen’s ground'],
  ['surfaceContainerHigh', '#E7EAE6', '#282B2F', 'Raised plates: the lever plate, keys, sheets, dialogs'],
  ['surfaceContainerHighest', '#F4F6F3', '#34383D', 'A key’s face when it sits on a plate; the bevel under a recess'],
  ['surfaceContainerLow', '#C3C8C3', '#131416', 'The ad slot'],
  ['onSurface', '#16181A', '#ECE9E0', 'Text, engraving, lit lamps, chart units, marks'],
  ['onSurfaceVariant', '#3E4447', '#A9ADB1', 'Secondary text, captions, lever labels not set'],
  ['outline', '#6B726E', '#70757B', 'Key edges, lamp bezels, chart baselines'],
  ['outlineVariant', '#A7ADA8', '#0E0F10', 'Engraved hairlines; the 3 dp drop under a key'],
  ['primary', '#16181A', '#ECE9E0', 'Main key, latched key, the selected day'],
  ['onPrimary', '#F0EDE4', '#16181A', 'Text on those'],
  ['inverseSurface', '#0E0F11', '#0E0F11', 'The display window, the field, the lever slot, the switch slot'],
  ['inverseOnSurface', '#F0EDE4', '#F0EDE4', 'Text and marks in the display'],
];
const EXTRA = [
  ['inverseOnSurfaceVariant', '#A9ADB1', '#A9ADB1', 'Secondary text in the display'],
  ['inverseOutlineVariant', '#2C2F33', '#2C2F33', 'Rules inside the display; a drum’s edge'],
  ['attention', '#FF8A5B', '#FF8A5B', 'One use: the square beside the role-missing sentence, inside the display'],
  ['lampGlass', '#9AA19C', '#3A3E44', 'A lamp that is not lit'],
  ['lampGlassHigh', '#C9CEC9', '#5A5F66', 'Its highlight, as a radial gradient from 36% 30%'],
  ['lampOnHigh', '#5B6368', '#FFFFFF', 'The highlight of a lit lamp; its body is onSurface'],
  ['handle', '#1D2023', '#D9D5CB', 'The lever handle and the switch handle'],
  ['handleHigh', '#42474B', '#F6F3EA', 'Their top edge, as a vertical gradient'],
  ['handleRidge', '#6A7075', '#8C8A84', 'Three ridges on the lever handle'],
  ['drumHigh', '#1D2023', '#1D2023', 'A counter drum, top'],
  ['drumLow', '#121315', '#121315', 'A counter drum, bottom'],
];
const TYPE = [
  ['Numeral', 'displayMedium', '40 / 42', '500', '', 'Counter drums; today’s count on Home'],
  ['Lever', 'headlineSmall', '26 / 30', '700 set, 600 not', 'caps, +0.04 em', 'Off, Silence, Block'],
  ['Display', 'titleLarge', '21 / 27', '500', '', 'The sentence in the window; the number on a sheet; a dialog’s question'],
  ['Title', 'titleMedium', '18 / 22', '600', 'caps, +0.08 em', 'Screen titles'],
  ['Main key', 'labelLarge', '17 / 21', '700', 'caps, +0.05 em', 'Resume, Set As Screening App'],
  ['Number', 'bodyLarge', '17 / 22', '500', '', 'Phone numbers in lists; the field'],
  ['Key', 'labelLarge', '15 / 18', '600', 'caps, +0.03 em', 'Every other key'],
  ['Lead', 'bodyLarge', '15 / 20', '400 or 500', '', 'Counts beside a mark; times; the second line when the role is missing'],
  ['Body', 'bodyMedium', '14 / 19', '400', '', 'Sentences, details, second lines of rows'],
  ['Strip', 'titleSmall', '13 / 17', '600', 'caps, +0.08 em', 'Row titles. Section titles use +0.1 em'],
  ['Note', 'bodySmall', '13 / 18', '400', '', 'The privacy line, milestone labels, the build stamp'],
  ['Caption', 'labelMedium', '12 / 16', '600', 'caps, +0.1 em', 'Captions over keys, tile captions, day headings. Chart labels use +0.06 em'],
  ['Slot', 'labelSmall', '11 / 14', '600', 'caps, +0.1 em', 'The word Advertisement'],
];
const MOTION = [
  ['lever.travel', '180 ms', 'cubic-bezier(0.2, 0, 0, 1)', 'The handle moves to the new stop. One haptic tick as it seats.'],
  ['lamp.cut', '0 ms', 'none', 'The old lamp goes dark at once. A lamp never shows a position the handle has left.'],
  ['lamp.warm', '240 ms, starting at 120 ms', 'cubic-bezier(0, 0, 0.2, 1)', 'The new lamp warms up.'],
  ['display.swap', '120 ms. After a lever move it starts at 180 ms', 'linear', 'The sentence cross-fades. The window does not change size.'],
  ['lever.locked', '160 ms', 'cubic-bezier(0.2, 0, 0, 1)', 'On a device that cannot screen: the handle moves 7 dp towards Silence and returns.'],
  ['key.press', '60 ms', 'linear', 'A key’s face drops 3 dp while pressed. A latched key stays down.'],
  ['switch.slide', '120 ms', 'cubic-bezier(0.2, 0, 0, 1)', 'The panel switch’s handle crosses 20 dp.'],
  ['sheet.rise', '220 ms', 'cubic-bezier(0.2, 0, 0, 1)', 'A sheet rises from the bottom edge.'],
  ['sheet.fall', '160 ms', 'cubic-bezier(0.3, 0, 1, 1)', 'A sheet leaves. A dialog fades out over the same time.'],
  ['scrim.fade', '160 ms', 'linear', 'The scrim and a dialog fade in.'],
  ['row.press', '0 ms', 'none', 'A row, strip or tile takes a 6% tint of onSurface while pressed. No ripple.'],
  ['screen.change', '0 ms', 'none', 'A cut. Every screen lands settled, with nothing arriving after the first frame.'],
];
const swatch = h => /^#[0-9A-F]{6}$/i.test(h) ? `<span class="swatch"><i style="background:${h}"></i>${h}</span>` : h;
const table = (head, rows, cls) => `<div class="tw"><table class="tbl"><thead><tr>${head.map(h => `<th>${h}</th>`).join('')}</tr></thead><tbody>${rows.map(r => `<tr>${r.map((c, i) => `<td class="${(cls && cls[i]) || ''}">${c}</td>`).join('')}</tr>`).join('')}</tbody></table></div>`;
PANELS.values = () => { const FR = frameRef(); return `
<h2>Colour, by Material 3 role</h2>
<p>A fixed palette. Nothing is taken from the wallpaper. The lamps have no colour of their own: a lit lamp is onSurface, a held one a ring of it, a dark one lampGlass. Blocked is a filled mark and silenced an open one.</p>
${table(['Role', 'Light', 'Dark', 'Used for'], ROLES.map(r => [r[0], swatch(r[1]), swatch(r[2]), r[3]]), ['k'])}
<p>scrim is black at 50% in light and 62% in dark. Roles not listed take these values: secondary and tertiary are primary; their containers and primaryContainer are surfaceContainerHigh; error is attention.</p>
<h3>Outside Material 3</h3>
${table(['Name', 'Light', 'Dark', 'Used for'], EXTRA.map(r => [r[0], swatch(r[1]), swatch(r[2]), r[3]]), ['k'])}
<p>lampHalo is a 5 dp ring of onSurface at 16% in light and 20% in dark. handleDrop is black at 38% in light and 60% in dark: the 3 dp drop under the handle and the main key.</p>
<p>Measured contrast: text is 5.8:1 or better on its ground in both themes. A control’s edge is 3.06:1 or better against what surrounds it.</p>
<h2>Type, in sp</h2>
<p>Hanken Grotesk, with Noto Sans Devanagari for Hindi. Both are under the SIL Open Font License 1.1. Hanken Grotesk’s figures are the same width at every weight with no font feature to switch on. It has no rupee sign: ₹ comes from the Devanagari companion or the system font. Ligatures are off. Capitals on controls are a style, like engraving; the strings stay in Title Case and sentence case.</p>
${table(['Style', 'M3 slot', 'Size / line', 'Weight', 'Case and tracking', 'Used for'], TYPE, ['k', '', 'n'])}
<p>In the prototype every size and line height lands on a whole pixel at both text sizes, and the embedded Latin face is given an ascent of 69.7% and a descent of 0: its ascent less its descent, so the baseline stays where the font puts it. Both are there so that every browser sets the same lines in the same places. Neither is a design value. The app takes its sizes and its font metrics from Android.</p>
<h2>Space and shape, in dp</h2>
<ul>
<li>Side margins 16. Between blocks 16 on Home and 12 elsewhere. Inside a block 8. Between keys 8.</li>
<li>Corners: 4 on keys, the handle, the field and the panel switch (3 on its handle); 6 on plates, the display, a sheet’s top edge and a dialog; 2 on chart units. Lamps are circles of 28, the Off ring 22.</li>
<li>Targets: a lever row is 62 tall (67 at 135% text) and as wide as the screen; a key 48; a main key 56; a strip 58; header buttons 48 square; a history row at least 56; a day in the chart 48 wide.</li>
<li>The display and the bay under the lever each take the height of their tallest state at the current width and text size, so the lever and the strips never move.${FR ? ` At 360 dp and 100% that is ${FR.display} and ${FR.bay}.` : ''}</li>
<li>Depth is drawn, never cast: a recess has a 2 dp black line along its top edge and a 1 dp highlight under it; a key has a 1 dp outline ring and a 3 dp drop of outlineVariant; a plate has a 1 dp ring and a 2 dp drop. No shadows, no blur.</li>
<li>The ad slot: a sill as tall as its label’s line (20 sp), the banner’s own height (60 at 360 dp wide), then the gesture inset. It does not rise with the keyboard. With ads removed only the gesture inset remains.</li>
<li>The status bar inset is left clear above every screen. Home starts 8 below it.</li>
</ul>
<h2>Motion</h2>
${table(['Name', 'Duration', 'Easing', 'What moves'], MOTION, ['k', 'n'])}
<p><b>Reduced motion.</b> Every duration is 0. Handle, lamp and sentence change in the same frame; sheets and dialogs appear and leave at once; the locked lever does not move. The haptic tick stays. Switch Motion to Reduced to see it.</p>
<h2>Icon</h2>
<div class="icons"><figure>${iconAdaptive(96, '50%')}<figcaption>Adaptive, round</figcaption></figure><figure>${iconAdaptive(96, '26%')}<figcaption>Squircle</figcaption></figure><figure>${iconAdaptive(48, '50%')}<figcaption>48 dp</figcaption></figure><figure>${iconThemed(96, '#DCE3EA', '#22303C')}<figcaption>Themed, light</figcaption></figure><figure>${iconThemed(96, '#2A323A', '#C9D6E2')}<figcaption>Themed, dark</figcaption></figure><figure><span style="display:block;background:#24272B;border-radius:8px;padding:12px">${iconNotif(48, '#FFFFFF')}</span><figcaption>Notification</figcaption></figure></div>
<p style="margin-top:10px">As picked. Background #1C1E21; the slot #0B0C0D; the key #F5C63C; the handset #F0EDE4. The drawing is scaled to 95% about the centre of its 108 dp layer, which puts every part inside the 66 dp safe circle. The themed icon and the notification icon are the same two shapes in one colour.</p>`; };

/* ── TalkBack ── */
const TB = [
  ['Home', [
    'The sentence in the display: “Callers outside your contacts are rejected.” It is a polite live region, so a change of mode, a pause or a lost role is spoken.',
    'Mode, a radio group of three: “Off, 1 of 3”, “Silence, 2 of 3”, “Block, selected, 3 of 3”. Each option’s description is its sentence.',
    'The bay under the lever. “Let every call ring for”, then “15 minutes”, “1 hour”, “4 hours”, “24 hours”. When paused, the one key “Resume”. When the role is missing, “Set As Screening App”. With the switch at Off, the privacy line as text.',
    '“History, 6 calls today”, button.',
    '“Statistics, 31 blocked, 3 silenced in 7 days”, button.',
    '“Options, Repeat callers ring, 2 numbers allowed”, button.',
    '“Notifications, On, for each stopped call”, switch. When Android blocks them: “Notifications, switched off in Android’s settings, Open Settings”, button.',
    '“Settings”, button.',
    'The ad, read by Google’s own view, last.',
  ], 'History, Statistics and Settings are drawn inside the display, above the lever. They are read after the lever and its keys, so set the traversal order; do not take it from the layout.'],
  ['Options', [
    '“Back”, button. “Options”, heading.',
    '“Who is filtered”, a radio group: “Everyone”, “International Only”. Then the sentence under it.',
    'Later: “Always block 140 numbers, Off”, switch.',
    '“Repeat callers”, its sentence, then a radio group: “Off”, “5 minutes”, “15 minutes”, “30 minutes”.',
    '“Allow list, On”, switch.',
    'Each number as one item, “+91 98765 43210, Always”, followed by “Remove +91 98765 43210”, button.',
    '“Phone number to allow”, edit box. An entry with no digits is refused with “Enter a phone number.”, spoken at once.',
    '“Let this number ring”, then “1 hour”, “24 hours”, “Always”. They appear once the field has focus.',
    'Later: “Always allow 160 numbers, Off”, switch.',
  ]],
  ['History', [
    '“Back”, button. “History”, heading. “Delete All”, button.',
    '“Today”, heading.',
    'Each call as one item and one button: “19:04, +91 140 123 4567, Blocked, 2nd call that day”. An allowed number adds “on the allow list” or “rings until 20:30”.',
    '“Yesterday”, heading, and so on by date.',
  ], 'The confirmation: “Delete all history?”, “Statistics are counted from the history, so they reset too.”, “Cancel”, “Delete All”. Focus starts on Cancel.'],
  ['Number details', [
    'The number, heading.',
    '“9 calls stopped. 9 blocked, 0 silenced.”, one item.',
    '“First: 26 Aug 2026, 10:14.” “Last: 2 Oct 2026, 19:04.” Each label and value is one phrase.',
    '“Let this number ring”, then “1 hour”, “24 hours”, “Always”. For a number already allowed: “On the allow list, Always”, then “Remove From Allow List”, button.',
    '“This call: Today, 19:04, Blocked”, then “Delete This Call”, button.',
  ], 'On screen the delete key sits near the top, away from the allow keys. In the reading order it comes last. A caller with no number has no allow keys.'],
  ['Statistics', [
    '“Back”, button. “Statistics”, heading. “Share”, button.',
    '“156 calls stopped. 148 blocked, 8 silenced.”, one item.',
    '“100 reached. Next: 250.”',
    '“About 1 hr 18 min, at 30 seconds a call.”',
    '“Last 7 days”, heading. “34 calls.”',
    'The chart, bar by bar, each a button: “Saturday: 3 blocked, 0 silenced” to “Friday: 5 blocked, 1 silenced, selected”. The line under the chart repeats the selected bar, so it is skipped.',
    '“12% fewer than the week before. Busiest around 11:00.”',
    '“Last 30 days”, heading. “121 calls. 9% more than the 30 days before.”',
    '“Period”, a radio group: “7 days”, “30 days, selected”, “90 days”.',
    '“By weekday”, heading. “Most on Tuesday.” Then seven bars: “Sunday: 6” to “Saturday: 11”.',
    '“By hour”, heading. “Most around 11:00.” Then 24 bars: “00:00: 0” to “23:00: 0”.',
    '“Most frequent”, heading. Rows: “+91 140 123 4567, 9 calls”, button.',
  ]],
  ['Settings', [
    '“Back”, button. “Settings”, heading.',
    '“Summary notification”, its sentence, then a radio group: “Off”, “Weekly”, “Monthly”.',
    '“Privacy Policy”, button. Where consent is required: “Privacy Choices”, button.',
    '“Open-Source Licences”, “Contact”, “Share App”, “Rate App”, buttons.',
    'Later: “Remove Ads, one-time purchase”, with the price, button. “Restore Purchases”, button.',
    'The build stamp, as text: “Version 0.1.0, 2 October 2026”.',
  ]],
  ['Licences', [
    '“Back”, button. “Open-Source Licences”, heading.',
    'Each library as one item: its name, the copyright holder, the licence.',
    '“Read The Apache License 2.0”, button. “Read The SIL Open Font License 1.1”, button.',
  ]],
  ['Notifications', [
    'Android reads the app’s name, the title, the text and the time: “Call blocked, +91 140 123 4567”.',
    'On a locked screen only “Call blocked”.',
    '“Allow For 1 Hour”, button, on an unlocked phone only.',
    'The milestone: “100 calls stopped”. The summary: “Last week, 31 blocked, 3 silenced”.',
  ]],
];
PANELS.talkback = () => `<p>The order TalkBack reads each screen in. Quoted text is what is spoken.</p>` + TB.map(s => `<h2>${s[0]}</h2><ol>${s[1].map(x => `<li>${x}</li>`).join('')}</ol>${s[2] ? `<p class="muted">${s[2]}</p>` : ''}`).join('');

/* ── Components ── */
const specBox = (html, o) => `<div class="sw ${S.theme}${o && o.bare ? ' bare' : ''}" style="--ts:${S.ts};width:${(o && o.w) || 328}px" inert>${html.replace(/ id="[^"]*"/g, '')}</div>`;
const fig = (cap, html, o) => `<figure><figcaption>${cap}</figcaption>${specBox(html, o)}</figure>`;
const winSpec = (kind, o) => `<div class="win" style="min-height:0">${statusH(kind, o)}</div>`;
const leverSpec = (mode, lamp) => leverH({ mode, lamp: Object.assign({ off: 'dark', silence: 'dark', block: 'dark' }, lamp) });
const demoRow = (h, m, k, a) => ({ id: 0, t: at(0, h, m), n: k ? N[k] : null, a });
const INV = [
  ['Display window', 'Dark in both themes. It holds one sentence, chosen in the order role missing, paused, mode, with the gear and two entry tiles. Its height is the tallest of its states and does not change.', () => [
    fig('A mode', winSpec('block')), fig('First launch', winSpec('first')), fig('Paused', winSpec('paused', { until: fTime(at(0, 20, 30)) })), fig('Role missing', winSpec('role')), fig('Cannot screen', winSpec('cannot'))]],
  ['Entry tile', 'A way into History or Statistics that carries a live count. States: a count; a plain line when there is nothing.', () => [
    fig('Counts', `<div class="win" style="min-height:0">${tilesH({ today: { b: 5, s: 1 }, total: 156, weekTotal: 34, week: { b: 31, s: 3 } })}</div>`),
    fig('Nothing', `<div class="win" style="min-height:0">${tilesH({ today: { b: 0, s: 0 }, total: 0, weekTotal: 0, week: { b: 0, s: 0 } })}</div>`)]],
  ['Lever', 'Three stops. The handle is the position, the lamp says it is in effect, the engraving names it. Tap a row or drag the handle. States: Off, Silence, Block, paused (the lamp becomes a ring), role missing (every lamp dark), locked (a device that cannot screen: the handle will not leave Off).', () => [
    fig('Off', leverSpec('off', { off: 'lit' })), fig('Silence', leverSpec('silence', { silence: 'lit' })), fig('Block', leverSpec('block', { block: 'lit' })),
    fig('Paused', leverSpec('block', { block: 'held' })), fig('Role missing', leverSpec('block', {}))]],
  ['Key', 'A raised key with a 3 dp drop. Momentary keys act at once: pause lengths, allow lengths, Delete, Cancel. Latching keys hold one choice of a set and stay down: who is filtered, the repeat window, the period, the summary. States: rest, pressed (down 3 dp), latched. Keys in a strip are equal in width unless a label needs more. A strip stacks when its labels cannot share one row.', () => [
    fig('Momentary', keysBlock('Let every call ring for', PAUSE.map(p => ({ label: T[p[1]], act: 'x' })))),
    fig('Latching', keysH([{ label: '7 Days', act: 'x' }, { label: '30 Days', act: 'x', on: true }, { label: '90 Days', act: 'x' }])),
    fig('Stacked', keysH([{ label: 'Everyone', act: 'x', on: true }, { label: 'International Only', act: 'x' }], { stack: true }))]],
  ['Main key', 'One per screen at most, and only when one action outranks the rest. States: rest, pressed.', () => [fig('Resume', mainKey('Resume', 'x')), fig('Role', mainKey('Set As Screening App', 'x'))]],
  ['Strip', 'A full-width row between engraved rules. Trailing part by kind: a chevron stays in the app, an arrow leaves it, a panel switch toggles, a value states a price, nothing for a statement.', () => [
    fig('Kinds', `<div class="strips is-closed">${stripH({ title: 'Options', detail: ['Repeat callers ring', '2 numbers allowed'], trail: 'chev', act: 'x' })}${stripH({ title: 'Privacy Policy', trail: 'out', act: 'x' })}${stripH({ title: 'Notifications', detail: 'Off', trail: 'switch', act: 'x' })}${stripH({ title: 'Notifications', detail: 'On, for each stopped call', trail: 'switch', on: true, act: 'x' })}${stripH({ title: 'Notifications', detail: T.notif_blocked, trail: 'out', act: 'x' })}</div>`)]],
  ['Panel switch', 'A slot with a sliding handle. Left is off, right is on, and the row says the word.', null],
  ['Header', 'Back, the title in capitals, and at most one action: a text action (Delete All) or an icon (Share). It stays in place while the screen scrolls.', () => [
    fig('With an action', hdrH('History', '<button class="hdr-a">Delete All</button>').replace('margin:0 -12px', ''), { w: 336 })]],
  ['History row', 'Time, number, then the outcome as a mark and a word. A later call from the same number that day says which call it is. A number on the allow list says so. The whole row is the target and opens the number’s details.', () => [
    fig('Rows', `<div class="hist">${hrowH(demoRow(19, 4, 'a', 'B'), { nth: 2 })}${hrowH(demoRow(16, 12, 'c', 'S'), { nth: 1 })}${hrowH(demoRow(15, 47, null, 'B'), { nth: 1 })}${hrowH(demoRow(11, 26, 'd', 'B'), { nth: 1, allow: { until: null } })}${hrowH(demoRow(10, 2, 'e', 'B'), { nth: 1, allow: { until: at(0, 20, 30) } })}</div>`)]],
  ['Sheet', 'Number details. States: not allowed (three allow keys), allowed (the entry and Remove From Allow List), no number (no allow keys), opened from Statistics (no This call line). It covers the ad slot. See History and Statistics in Screens.', null],
  ['Dialog', 'One use: Delete All. A plate over the scrim with two keys of equal weight. Cancel has the focus when it opens.', () => [fig('Delete All', `<div style="margin:0 -24px">${dialogPlate()}</div>`, { w: 312 })]],
  ['Field', 'A recessed display you can type in. States: empty, focused, filled, error (a 2 dp ring and the message under it).', () => [
    fig('Empty', '<input class="field" placeholder="Phone number" tabindex="-1">'),
    fig('Error', '<div style="display:flex;flex-direction:column;gap:8px"><input class="field is-error" value="+" tabindex="-1"><p class="err"><i></i>Enter a phone number.</p></div>')]],
  ['Allow-list row', 'The number, then Always or Until and a time, and a 48 dp remove button.', () => [
    fig('Rows', `<div><div class="arow"><span class="arow-t"><span class="num">${N.d}</span><span class="strip-d">Always</span></span><span class="arow-x">${IC.x}</span></div><div class="arow"><span class="arow-t"><span class="num">${N.e}</span><span class="strip-d">Until ${fUntil(at(1, 10, 5))}</span></span><span class="arow-x">${IC.x}</span></div></div>`)]],
  ['Counter', 'The all-time count on drums, like a message register. At least three drums; leading zeros are dimmer. A fourth drum appears at 1,000.', () => [
    fig('Nothing', `<div class="win statwin" style="min-height:0">${drumsH(0)}</div>`), fig('156', `<div class="win statwin" style="min-height:0">${drumsH(156)}</div>`), fig('3,412', `<div class="win statwin" style="min-height:0">${drumsH(3412)}</div>`)]],
  ['Milestone bar', 'From zero to the next milestone, with a notch at the last one reached. States: none reached, reached and next, past the top of the ladder (full, no next).', () => [
    fig('None reached', `<div class="win statwin" style="min-height:0">${mileH(3, null, 10)}</div>`), fig('Reached', `<div class="win statwin" style="min-height:0">${mileH(156, 100, 250)}</div>`)]],
  ['Day chart', 'Seven days, oldest first. Up to 8 calls a day each call is one unit: solid for blocked, outlined for silenced. Past 8 the columns become bars to one scale with the day’s total above. A day is a 48 dp target; the selected day’s label inverts and its counts are written under the chart.', () => [
    fig('Units', dayChartH(DATA.heavy.t.daily.map(d => ({ b: d[0], s: d[1] })), 6)), fig('Bars', dayChartH(DATA.thousands.t.daily.map(d => ({ b: d[0], s: d[1] })), 6)),
    fig('One call', dayChartH([0, 0, 0, 0, 0, 0, 1].map(v => ({ b: v, s: 0 })), 6))]],
  ['Weekday chart', 'Seven bars from zero, each with its count above. Not selectable: the numbers are already written.', () => [fig('30 days', weekdayChartH(DATA.heavy.t.wd[30]))]],
  ['Hour chart', '24 bars from zero. Touch or drag anywhere on it to read an hour; the line beside the heading changes from “Most around” to that hour’s count.', () => [fig('30 days', hourChartH(DATA.heavy.t.hr[30], null)), fig('An hour chosen', hourChartH(DATA.heavy.t.hr[30], 17))]],
  ['Ad slot', 'A recessed tray along the bottom edge. States: loaded (the word Advertisement on the sill, the banner under it), empty (the tray alone), removed (no tray). Same height loaded or empty.', () => [fig('Loaded', slotPart(true), { bare: true, w: 360 }), fig('Empty', slotPart(false), { bare: true, w: 360 })]],
  ['Notification', 'Android draws it. Ours are the small icon, the title, the text and one action. Three kinds, all silent: a stopped call, a milestone, a summary. On a locked screen a stopped call shows its title only.', () => [fig('A call', ncardH({ when: fTime(at(0, 19, 4)), title: 'Call blocked', text: numH(N.a), action: 'Allow For 1 Hour' }), { w: 328 }), fig('Locked', ncardH({ when: fTime(at(0, 19, 4)), title: 'Call blocked' }), { w: 328 }), fig('Milestone', ncardH({ when: '21' + NB + 'Sep', title: '100 calls stopped' }), { w: 328 }), fig('Summary', ncardH({ when: 'Mon', title: 'Last week', text: '31 blocked, 3 silenced' }), { w: 328 })]],
  ['Stand-in', 'A dashed box marks anything Android or Google draws. It is not part of the design.', null],
];
PANELS.components = () => `<p>Every component and its states, drawn in the theme and text size now switched.</p><div class="inv">` + INV.map(c => `<div class="inv-i"><h3>${c[0]}</h3><p>${c[1]}</p>${c[2] ? `<div class="spec">${c[2]().join('')}</div>` : ''}</div>`).join('') + '</div>';

/* ── Words: every string, old beside new ── */
const WORDS = [
  ['mode_silence_detail and three more', 'don\'t, isn\'t, can\'t', 'don’t, isn’t, can’t', 'Typographic apostrophes in every string.'],
  ['role_missing', 'Android isn\'t sending calls to this app, so nothing is being filtered.', 'Nothing is being filtered. / Android isn’t sending calls to this app.', 'Two lines: the consequence first, the cause under it.'],
  ['paused_until', 'Paused until 3 Oct 2026, 19:30. Every call rings.', 'Paused until tomorrow, 19:30. Every call rings.', 'A pause never runs past tomorrow. Same change in “Until tomorrow, 10:05” on the allow list.'],
  ['notifications_detail', 'For each blocked or silenced call', 'On, for each stopped call / Off', 'The switch’s state is now a word as well as a position.'],
  ['notifications_denied', 'Notifications are switched off for this app.', 'Switched off in Android’s settings', 'No snackbar. The row itself says it and opens the settings.'],
  ['options_detail', 'Scope, pause, repeat callers, allow list', 'No exceptions / Repeat callers ring · 2 numbers allowed', 'A live summary in place of a list of what is inside.'],
  ['statistics_week', '0 blocked, 0 silenced in 7 days', 'Nothing yet / Nothing in 7 days', 'Only for zero. With calls the old string stays, on three lines.'],
  ['calls_handled, notification_milestone', '100 calls handled', '100 calls stopped', 'One word for blocked plus silenced, the one the share text already uses.'],
  ['channel_handled_calls', 'Handled calls', 'Stopped calls', ''],
  ['channel_reports', 'Reports', 'Summaries', 'Settings calls it a summary.'],
  ['report_detail', 'A count of handled calls, each Monday or on the 1st', 'A count of stopped calls, each Monday or on the 1st.', ''],
  ['exceptions_heading', 'Exceptions', 'removed', 'Repeat callers and Allow list are titled sections of their own. Home still says “No exceptions”.'],
  ['scope_all', 'Everyone outside your contacts', 'Everyone (key) / Everyone outside your contacts. (line under it)', ''],
  ['scope_international', 'Only international callers outside your contacts', 'International Only (key) / Only international callers outside your contacts. (line under it)', ''],
  ['pause, pause_detail, pause_active, pause_for', 'Pause / Let every call ring for a while / Every call rings until 19:30 / Pause For', 'Let every call ring for', 'Pause is on Home only, as a caption over four keys.'],
  ['minutes, hours', '15 minutes, 1 hour, 4 hours, 24 hours', '15 Min, 1 Hr, 4 Hr, 24 Hr', 'On keys. The full words stay as the TalkBack labels.'],
  ['repeat_callers_detail', 'A number that calls again soon after being stopped rings', 'A stopped number that calls again within this time rings.', ''],
  ['repeat_within', 'Calls again within', 'removed', 'Off joins 5, 15 and 30 Min as one strip of keys.'],
  ['allow_list_detail', 'Numbers that always ring', 'On / Off', 'Some entries are timed, so “always” was not true.'],
  ['add_number, allow_number_title, allow, allow_for', 'Add Number / Allow a Number / Allow / For 1 hour', 'Let this number ring, then 1 Hr, 24 Hr, Always', 'No dialog. The length key is the commit.'],
  ['allow_this_number', 'Allow This Number', 'Let this number ring, then 1 Hr, 24 Hr, Always', 'Same caption and keys on the sheet.'],
  ['more_for_number', 'More for +91 …', 'removed', 'No row menu.'],
  ['delete', 'Delete', 'Delete (spoken as Delete This Call)', 'Beside the line “This call”.'],
  ['added_to_allow_list', '+91 … is on the allow list.', 'On the allow list / Rings until 20:30', 'Written on the row. No snackbar.'],
  ['total', 'Total', '9 calls stopped', 'The total leads; blocked and silenced follow it.'],
  ['statistics_empty', 'Nothing to show yet.', 'removed', 'The counter reads 000.'],
  ['time_saved, time_saved_basis', 'About 1 hr 18 min saved / Counted at 30 seconds a call', 'About 1 hr 18 min, at 30 seconds a call', 'One line.'],
  ['day_by_day', 'Day by day', 'removed as a heading', 'The chart sits under “Last 7 days”. It stays as the chart’s TalkBack label.'],
  ['licences_intro', 'This app is built with these open-source libraries.', 'removed', 'The title says it.'],
  ['licence_text_link', 'Read the Apache License 2.0', 'Read The Apache License 2.0', 'Every word capitalised, as on the other actions.'],
  ['%d in every count', '3412', '3,412', 'Group thousands.'],
];
const NEWWORDS = [
  ['Home, switch at Off', 'No account. Your contacts are never read. Nothing about a call leaves this phone.'],
  ['Ad slot, when an ad is loaded', 'Advertisement'],
  ['History row, a repeat', '2nd call, 3rd call'],
  ['Number details', 'This call'],
  ['Statistics, the counter', 'Calls stopped'],
  ['Statistics, the milestone', '100 reached'],
  ['Notification action (proposed)', 'Allow For 1 Hour'],
  ['Licences', 'SIL Open Font License 1.1 / Read The SIL Open Font License 1.1'],
  ['Later, Settings', 'Remove Ads / One-time purchase / Restore Purchases / No purchase found for this Google account. / Ads removed'],
  ['Later, Play product text', 'Remove Ads / Removes the ad from every screen. One-time purchase.'],
  ['Later, Options', 'Always block 140 numbers / On. Telemarketers must call from these. / Always allow 160 numbers / On. Banks, insurers and government services call from these.'],
];
PANELS.words = () => `<p>Every change to <code>strings.xml</code>, old beside new. A string not listed is unchanged.</p>
${table(['String', 'Old', 'New', 'Why'], WORDS.map(w => [`<code>${esc(w[0])}</code>`, `<span class="old">${esc(w[1])}</span>`, esc(w[2]), esc(w[3])]))}
<h2>New strings</h2>
${table(['Where', 'Words'], NEWWORDS.map(w => [esc(w[0]), esc(w[1])]), ['k'])}
<p class="muted">The Hindi on the Later frame is a draft and has not been read by a native speaker.</p>`;

/* ── Notes: the hand-back ── */
PANELS.notes = () => { const FR = frameRef(); return `
<h2>Decisions the brief did not dictate</h2>
<ol>
<li><b>Nothing below the display moves.</b> The display and the bay under the lever are each as tall as their tallest state. Throwing the lever, pausing or losing the role changes words and lamps, never positions.</li>
<li><b>Pause lives on Home only.</b> Four keys under the lever: one tap from opening the app. Options no longer repeats it.</li>
<li><b>With the switch at Off, the place of the pause keys carries the privacy line.</b> On the board it sat at the foot of first launch only.</li>
<li><b>One pattern for every small choice:</b> a caption and a strip of keys. Momentary keys act at once (pause, allow). Latching keys hold a setting (who is filtered, repeat window, period, summary). The three dialogs are gone.</li>
<li><b>Repeat callers is one control.</b> Off sits in the same strip as 5, 15 and 30 Min.</li>
<li><b>Fixing a mistake is two taps.</b> A history row is one large target that opens the number’s sheet; a length key there allows it and closes the sheet; the row then says so. The three-dot menu is gone.</li>
<li><b>Deleting one call</b> sits on the sheet beside “This call”, away from the allow keys.</li>
<li><b>No snackbars.</b> Every result is written where the action was.</li>
<li><b>The notification should carry the action:</b> one, “Allow For 1 Hour”, on an unlocked phone only. It is the courier case without opening the app. A permanent allow stays inside the app.</li>
<li><b>Statistics is ranked:</b> the all-time counter is the headline; the last 7 days come second; the period keys and what they drive come last. “Last 30 days” appears once the history is older than a week, so a new user is not shown one count three times.</li>
<li><b>The day chart counts in units up to 8 calls a day</b> and becomes bars with totals above that. The weekday and hour charts never scale below 4, so a single call is not a full-height bar.</li>
<li><b>The hour chart is read by touching or dragging across it.</b> 24 bars cannot each be a 48 dp target.</li>
<li><b>Notifications blocked in Android:</b> the row on Home, and the summary keys in Settings, become a link that opens Android’s settings.</li>
<li><b>A device that cannot screen</b> says so in the display from the first frame, and the lever will not leave Off.</li>
<li><b>History and the sheet call a number allowed only while the allow list is on.</b></li>
<li><b>India rules have no section of their own.</b> The 140 rule sits under Who is filtered; the 160 rule sits after the allow list, with the other ways a call gets through.</li>
<li><b>The slot does not rise with the keyboard.</b> An ad directly above the keys invites a wrong tap.</li>
<li><b>Licences gains the two typefaces</b> and a second licence link, which the Open Font License requires once the fonts ship.</li>
<li><b>Screens cut; sheets rise.</b> The lever keeps the timings from the board.</li>
<li><b>Keys in a strip share its width equally</b> unless a label needs more; then that key takes what it needs. If the labels cannot share one row, the strip stacks.</li>
<li><b>The icon is 5% smaller inside its layer,</b> so the key’s corner clears the 66 dp safe circle. Nothing else about it changed.</li>
<li><b>The sentence in the display is balanced:</b> its lines are as even as they can be without leaving a word alone. A dialog’s question is the same. In every other sentence the last two words stay on one line. In Compose that is <code>LineBreak.Heading</code> for the first and a no-break space before the last word for the second.</li>
<li><b>Parts joined by a middle dot wrap as parts.</b> A part that starts a new line drops its dot: “Today, 17:41 · Blocked” on one line, the time over the outcome on two.</li>
</ol>
<h2>Where I think the brief is wrong for the user</h2>
<ol>
<li><b>§8, the totals.</b> 156 calls in all cannot hold 121 in the last 30 days plus the 111 that “9% more” implies for the 30 days before. Both figures are shown as written.</li>
<li><b>§8, the week.</b> 34 calls cannot be 12% fewer than a whole number: against 38 it is 11%, against 39 it is 13%. Shown as written.</li>
<li><b>§8, the numbers.</b> Five numbers, whose counts add to 24 in 30 days, cannot fill a week of 34 calls. History lists three days and then says so.</li>
<li><b>The allow list’s own switch.</b> In today’s build, with the list switched off, History and the sheet still say “On the allow list”. The prototype fixes the wording (decision 15). The simpler fix is to drop the switch: an empty list is already off.</li>
<li><b>A device that cannot screen.</b> In today’s build, tapping Silence or Block there does nothing and says nothing.</li>
<li><b>A 720 dp tall phone.</b> ${FR ? `Home needs ${FR.strips} dp above the slot at 100% text. At 360 × 800 it fits with ${FR.fold - FR.strips} dp to spare; at 360 × 720 it scrolls by about ${80 - (FR.fold - FR.strips)} dp.` : 'At 360 × 800 Home fits at 100% text; at 360 × 720 it scrolls.'} The lever and the display stay above the fold. Only moving Notifications off Home would fix it.</li>
<li><b>Strings 40% longer.</b> The display holds three lines at 100% text. English’s longest sentence already takes three, so a translation of it has little room before a fourth line pushes Home${FR ? ` about ${FR.line - (FR.fold - FR.strips)} dp` : ''} past the fold. The Hindi draft fits in three. Translators need that limit; the other strings have their 40%.</li>
<li><b>Time saved</b> is parked, so it stays, as one quiet line under the counter.</li>
</ol>
<h2>Figures the brief does not give</h2>
<ul>
<li>Yesterday’s and Wednesday’s rows, built from the same five numbers and one caller with no number, inside the day totals of the chart.</li>
<li>Which calls of the week were silenced: Monday, Wednesday and today.</li>
<li>By hour for 30 days; by weekday and by hour for 7 and 90 days; the 7 and 90 day lists of frequent numbers; each number’s first call and all-time split.</li>
<li>90 days is treated as all time (156), because the first call is 38 days old.</li>
<li>The whole Thousands set: 3,412 calls on a much busier line, to load the charts.</li>
<li>Last month’s summary, 114 blocked and 6 silenced.</li>
</ul>
<h2>What I could not do</h2>
<ul>
<li>Run it on a OnePlus 12 or an iPhone. It was run in Chromium, which is Chrome’s engine, and in WebKit, which is Safari’s, at 360 × 800, 402 × 874 and 412 × 905. Between the two, every element sits within 0.02 px, every line breaks at the same word and every text style has the same baseline. Both were driven through every flow with real clicks, typing and drags. Run Self-Check, in the Switches tab, repeats the measurements in whatever browser this is and says whether it draws the frame as the reference does.</li>
<li>Use the typeface of the Uber app, as the founder asked. Uber Move is proprietary. Hanken Grotesk, under the SIL Open Font License, stands in its place.</li>
<li>Draw Android’s role prompt, the share sheet, Google’s consent form, Play’s purchase sheet or the ads. Each is a labelled stand-in that only lets the flow continue.</li>
<li>Have the Hindi read by a native speaker.</li>
<li>Show the real price. ₹000 is a placeholder for Google Play’s localised price.</li>
</ul>
<h2>Deep links</h2>
<p>Each screen in the Screens tab has an address: the file’s URL, then <code>#</code> and its id. Add switches with dots: <code>#his-sheet.dark.135.12h.empty.reduced</code>. The ids are in <code>window.scarletPrototype.presets</code>, for scripting the screenshots of the handoff bundle.</p>
<p>Add <code>shot</code> for the app alone, edge to edge, without the prototype’s own buttons: <code>#home-block.dark.100.24h.shot</code> in a window of 360 × 800 is the frame for a screenshot. On a phone, <code>frame</code> and <code>screen</code> choose between the 360 × 800 frame and the app at the phone’s own width. <code>#selftest</code> runs the self-check on opening.</p>`; };
