/* ───────────── self-check: measured in whatever browser this is ─────────────
   1. The layout rules of §4, over every screen and state at this window's size.
   2. Sameness: in a fixed 360 × 800 frame, every block of text must break where the reference browser broke it,
      and Home's blocks must sit where they sat there. A phone that passes draws the design as every other phone that passes.
   3. Every character must come from the two embedded typefaces, never from the phone's own.
   tools/check.js runs it headless. On a phone: Run Self-Check in the Switches tab, or the address #selftest. */

// Problems in the frame as it stands. Lengths are dp: measured from the app's own corner and divided by the bench's scale.
function checkNow() {
  const out = [], phone = $('#phone'), view = $('#view');
  const pr = phone.getBoundingClientRect(), W = phone.offsetWidth, H = phone.offsetHeight, k = pr.width / W || 1;
  const box = el => { const r = el.getBoundingClientRect(); return { l: (r.left - pr.left) / k, r: (r.right - pr.left) / k, t: (r.top - pr.top) / k, b: (r.bottom - pr.top) / k, w: r.width / k, h: r.height / k }; };
  const name = el => (typeof el.className === 'string' && el.className ? '.' + el.className.trim().split(/\s+/).join('.') : el.tagName.toLowerCase()) + ' “' + (el.textContent || '').replace(/\s+/g, ' ').trim().slice(0, 28) + '”';
  const r1 = n => Math.round(n * 10) / 10;

  // nothing off the edge, nothing cut off
  for (const el of phone.querySelectorAll('*')) {
    const b = box(el);
    if (!b.w || !b.h) continue;
    if (el.closest('.days')) { if (b.r > W - 10.9 || b.l < 10.9) out.push('outside its column: ' + name(el)); continue; }
    if (b.r > W + 0.6 || b.l < -0.6) out.push(`off the edge, ${Math.round(b.l)} to ${Math.round(b.r)} of ${W}: ` + name(el));
    const cs = getComputedStyle(el);
    if ((cs.overflowX === 'hidden' || cs.overflowX === 'clip') && el.scrollWidth > el.clientWidth + 1 && !el.matches('.view,.sw,.layer')) out.push('cut off: ' + name(el));
    // an inline box is as tall as the font's own metrics say, and browsers read those differently; anything drawn on one would differ too
    if (cs.display === 'inline' && (parseFloat(cs.borderTopWidth) || parseFloat(cs.borderBottomWidth) || !/rgba\(0, 0, 0, 0\)|transparent/.test(cs.backgroundColor))) out.push('an inline box with a border or a background, which each browser sizes its own way: ' + name(el));
  }
  // text that may not wrap still has to fit its place
  for (const el of phone.querySelectorAll('.nb,.num,.sheet-num,.lever-label,.hrow-time,.frow-n,.hdr-a,.key,.key-main,.axl,.dlab,.hax span,.strip-v')) {
    const r = el.getBoundingClientRect();
    if (!r.width) continue;
    let par = el.parentElement;
    while (par && getComputedStyle(par).display === 'inline') par = par.parentElement;
    const q = par.getBoundingClientRect(), over = Math.max(r.right - q.right, q.left - r.left) / k;
    if (over > 0.6) out.push(`wider than its place by ${Math.round(over)}: ` + name(el));
    if (el.scrollWidth > el.clientWidth + 1 && getComputedStyle(el).display !== 'inline') out.push('does not fit: ' + name(el));
  }
  // every target at least 48 dp. Android's and Google's stand-ins are not ours to size.
  for (const el of phone.querySelectorAll('[data-act],.arow-x,.hdr-b')) {
    if (el.matches('.scrim,.sb-proto,.sb-fs') || el.closest('.standin,.shade')) continue;
    const b = box(el);
    if (b.w && (b.w < 47.5 || b.h < 47.5)) out.push(`target ${Math.round(b.w)} × ${Math.round(b.h)}: ` + name(el));
  }
  // Home: where the lever, the display and the strips sit, and whether it scrolls
  const scr = view.firstElementChild;
  let home = null;
  if (scr && scr.classList.contains('home')) {
    const lever = box(scr.querySelector('.lever')), win = box(scr.querySelector('.win')), strips = box(scr.querySelector('.strips'));
    home = { leverTop: r1(lever.t), leverBottom: r1(lever.b), display: r1(win.h), stripsBottom: r1(strips.b), fold: r1(box(view).b), over: Math.max(0, Math.ceil(view.scrollHeight - view.clientHeight - 0.5)) };
  }
  return { problems: out, home, w: W, h: H, scale: r1(k * 100) / 100 };
}

// Where each block of text breaks: one entry per piece of text, with the offset of every word that starts a new line.
// A word's place is the first box its range draws. Two boxes are on different lines when they are more than
// half a line apart, since two fonts on one line (Latin figures in a Hindi sentence) sit a few pixels apart.
function lineBreaks(root) {
  const out = [], walk = document.createTreeWalker(root, NodeFilter.SHOW_TEXT), rg = document.createRange();
  while (walk.nextNode()) {
    const n = walk.currentNode, s = n.nodeValue;
    if (!s.trim()) continue;
    const gap = (parseFloat(getComputedStyle(n.parentElement).lineHeight) || 16) * 0.6, starts = [], re = /\S+/g;
    let top = null, m, drawn = false;
    while ((m = re.exec(s))) {
      rg.setStart(n, m.index); rg.setEnd(n, m.index + m[0].length);
      let first = null, last = null, inner = 0;
      for (const r of rg.getClientRects()) {
        if (!(r.width > 0)) continue;   // after a forced break some engines add an empty box at the end of the line before
        if (first === null) first = r.top;
        else if (r.top > last + gap) inner++;   // a long word broken inside itself
        last = r.top;
      }
      if (first === null) continue;
      drawn = true;
      if (top !== null && first > top + gap) starts.push(m.index);
      for (let k = 0; k < inner; k++) starts.push(m.index + '+');
      top = last;
    }
    if (drawn) out.push(starts.length ? starts.length + 1 + '@' + starts.join(',') : '1');
  }
  return out.join('|');
}
const hash5 = s => { let h = 2166136261; for (let i = 0; i < s.length; i++) { h ^= s.charCodeAt(i); h = Math.imul(h, 16777619); } return ('0000' + (h >>> 0).toString(36)).slice(-5); };

// Every box and every line break in the frame, for comparing two browsers element by element (tools/same.js).
function layoutDump() {
  const phone = $('#phone'), pr = phone.getBoundingClientRect(), k = pr.width / phone.offsetWidth || 1, q = n => Math.round(n / k * 100) / 100, out = [];
  const path = el => { const p = []; for (let e = el; e && e !== phone; e = e.parentElement) p.unshift((typeof e.className === 'string' && e.className ? e.className.trim().split(/\s+/)[0] : e.tagName.toLowerCase()) + ':' + Array.prototype.indexOf.call(e.parentElement.children, e)); return p.join('/'); };
  for (const el of phone.querySelectorAll('*')) {
    if (el.closest('svg') && el.tagName.toLowerCase() !== 'svg') continue;
    const r = el.getBoundingClientRect();
    if (!r.width && !r.height) continue;
    // an inline box's top and height are the font's own metrics, drawn nowhere: only its place along the line is compared
    const inline = getComputedStyle(el).display === 'inline';
    out.push([path(el), q(r.left - pr.left), inline ? 0 : q(r.top - pr.top), q(r.width), inline ? 0 : q(r.height)]);
  }
  return { boxes: out, lines: lineBreaks(phone) };
}

// Characters that would be drawn by a font of the phone's own: their width changes when the last-resort family does.
function strayCharacters(chars) {
  const box = $('#measure'), mk = fam => { const s = document.createElement('span'); s.style.cssText = `position:absolute;left:0;top:0;white-space:pre;font-size:40px;letter-spacing:0;font-family:'Hanken Grotesk','Noto Sans Devanagari',${fam}`; box.appendChild(s); return s; };
  const a = mk('monospace'), b = mk('serif'), bad = [];
  for (const ch of chars) {
    a.textContent = b.textContent = ch + ch + ch;
    if (Math.abs(a.getBoundingClientRect().width - b.getBoundingClientRect().width) > 0.01) bad.push(ch);
  }
  a.remove(); b.remove();
  return bad;
}

const TEST_COMBOS = [];
for (const theme of ['light', 'dark']) for (const ts of ['100', '135']) for (const clock of ['24h', '12h']) TEST_COMBOS.push([theme, ts, clock]);
const refKey = ids => hash5(ids.join(','));

// The frame: 360 × 800 whatever the window is. The layout rules again, where every block of text breaks, and where Home's
// blocks sit. One theme is enough, since colour moves nothing.
async function framePass(ids, onStep) {
  const root = document.documentElement, lines = [], groups = {}, problems = [];
  let frame = null;
  root.classList.add('ref'); M.key = '';
  try {
    let n = 0;
    for (const id of ids) for (const ts of ['100', '135']) for (const clock of ['24h', '12h']) {
      if (id === 'home-135' && ts === '100') { lines.push('-----'); continue; }
      const link = [id, 'light', ts, clock, 'loaded', 'shot'].join('.');   // shot: the app alone, the same on every device
      applyLink(link);
      lines.push(hash5(lineBreaks($('#phone'))));
      const r = checkNow();
      for (const p of r.problems) problems.push(`${link}, in the 360 × 800 frame: ${p}`);
      if (r.home) {
        if (ts === '100' && r.home.over) problems.push(`${link}: Home scrolls at 100% text in the 360 × 800 frame, ${r.home.over} dp too tall`);
        if (r.home.leverBottom > r.home.fold + 0.05) problems.push(`${link}: in the 360 × 800 frame the lever ends at ${r.home.leverBottom}, below the fold at ${r.home.fold}`);
        groups[[S.data, ts === '100' ? '100%' : '135%', clock, hindi() ? 'Hindi' : 'English'].join(', ')] = [r.home.leverTop, r.home.display, r.home.stripsBottom].join(' / ');
        if (id === 'home-block' && ts === '100' && clock === '24h') frame = { display: M.winH, bay: M.bayH, strips: r.home.stripsBottom, fold: r.home.fold, line: parseFloat(getComputedStyle($('#phone .say')).lineHeight) };
      }
      if (onStep) onStep(++n);
      if (n % 6 === 0) await new Promise(res => setTimeout(res, 0));
    }
  } finally { root.classList.remove('ref'); M.key = ''; }
  return { key: refKey(ids), lines: lines.join(''), home: groups, frame, problems };
}

// Leaves the prototype on the screen it started from.
async function selfTest(o) {
  o = o || {};
  const t0 = Date.now(), keep = { theme: S.theme, ts: S.ts, clock: S.clock, ad: S.ad, motion: S.motion, shot: S.shot, frame: S.frame, preset: S.preset };
  await loadFonts();
  const all = PRESETS.map(p => p[0]), ids = all.filter(id => !o.filter || id.indexOf(o.filter) >= 0);
  const total = ids.length * 8 - (ids.indexOf('home-135') >= 0 ? 4 : 0), steps = total + (o.filter ? 0 : all.length * 4);
  const problems = [], groups = {}, chars = new Set();
  let n = 0, last = null, over100 = 0, same = null, ref = null;
  S.testing = true;
  S.frame = false;   // first the app at this window's own size
  try {
    for (const id of ids) for (const c of TEST_COMBOS) {
      if (id === 'home-135' && c[1] === '100') continue;
      const link = [id].concat(c, ['loaded'], keep.shot ? ['shot'] : []).join('.');
      applyLink(link);
      const r = last = checkNow();
      n++;
      for (const p of r.problems) problems.push(link + ': ' + p);
      for (const ch of $('#phone').textContent) chars.add(ch);
      if (r.home) {
        // the fold is a rule for a whole screen of 800 dp; a browser tab is shorter by its own bars
        if (r.h >= 799.5) {
          if (c[1] === '100' && r.home.over) problems.push(`${link}: Home scrolls at 100% text, ${r.home.over} dp too tall`);
          if (r.home.leverBottom > r.home.fold + 0.05) problems.push(`${link}: the lever ends at ${r.home.leverBottom}, below the fold at ${r.home.fold}`);
        } else if (c[1] === '100') over100 = Math.max(over100, r.home.over);
        const g = [S.data, c[1] === '100' ? '100%' : '135%', c[2], hindi() ? 'Hindi' : 'English'].join(', ');
        (groups[g] = groups[g] || {})[[r.home.leverTop, r.home.display, r.home.stripsBottom].join(' / ')] = true;
      }
      if (o.onStep) o.onStep(n, steps);
      if (n % 6 === 0) await new Promise(res => setTimeout(res, 0));
    }
    for (const ch of strayCharacters(Array.from(chars).filter(ch => /\S/.test(ch)))) problems.push(`“${ch}” (U+${ch.codePointAt(0).toString(16).toUpperCase().padStart(4, '0')}) is not in the embedded typefaces, so each phone draws it in a font of its own`);
    // then the frame: the rules again, and sameness against the reference that tools/reference.js wrote into this file
    if (!o.filter) {
      ref = await framePass(all, k => { if (o.onStep) o.onStep(n + k, steps); });
      const seen = new Set(problems.map(p => p.replace(/\.shot\b/, '').replace(', in the 360 × 800 frame', '')));
      for (const p of ref.problems) if (!seen.has(p.replace(/\.shot\b/, '').replace(', in the 360 × 800 frame', ''))) problems.push(p);
      if (typeof REFERENCE === 'object' && REFERENCE && REFERENCE.key === ref.key && REFERENCE.lines.length === ref.lines.length) {
        same = { lines: [], home: [] };
        let i = 0;
        for (const id of all) for (const ts of ['100%', '135%']) for (const clock of ['24h', '12h']) {
          if (REFERENCE.lines.substr(i * 5, 5) !== ref.lines.substr(i * 5, 5)) same.lines.push(`${id}, ${ts}, ${clock}`);
          i++;
        }
        for (const g in REFERENCE.home) {
          const a = String(REFERENCE.home[g]).split(' / ').map(Number), b = String(ref.home[g] || '').split(' / ').map(Number);
          if (a.some((v, j) => !(Math.abs(v - b[j]) <= 0.6))) same.home.push(`${g}: ${ref.home[g]} here, ${REFERENCE.home[g]} in the reference`);
        }
        if (same.lines.length) problems.push(`Text breaks at different words than in the reference browser, in ${plural(same.lines.length, 'state', 'states')}: ${same.lines.slice(0, 12).join('; ')}${same.lines.length > 12 ? '; …' : ''}`);
        for (const h of same.home) problems.push('Home sits differently than in the reference browser (lever top / display height / strips bottom). ' + h);
      }
    }
  } finally {
    S.testing = false;
    S.theme = keep.theme; S.ts = keep.ts; S.clock = keep.clock; S.motion = keep.motion; S.shot = keep.shot; S.frame = keep.frame;
    applyPreset(keep.preset || 'home-block', true);
    S.ad = keep.ad;
    render({ still: true });
  }
  const home = {};
  for (const g in groups) {
    const v = Object.keys(groups[g]);
    home[g] = v.join(' and ');
    if (v.length > 1) problems.push(`Home moves between states (${g}): lever top / display height / strips bottom are ${v.join(' and ')}`);
  }
  return { ok: !problems.length, states: n, problems, browser: navigator.userAgent, window: [window.innerWidth, window.innerHeight], app: last ? [last.w, last.h] : null,
    scale: last ? last.scale : 1, frameChecked: !!ref, homeOver: over100, home, reference: ref && { key: ref.key, lines: ref.lines, home: ref.home, frame: ref.frame }, sameChecked: !!same,
    signature: ref ? hash5(ref.lines).toUpperCase() : null, ms: Date.now() - t0 };
}

function selfTestReport(r) {
  const s = [], full = r.app[1] >= 799.5;
  s.push(r.ok ? `No problems in ${r.states} states.` : `${plural(r.problems.length, 'problem', 'problems')} in ${r.states} states.`);
  s.push('');
  s.push(`Browser: ${r.browser}`);
  s.push(`Window: ${r.window[0]} × ${r.window[1]}.`);
  s.push('Checked: every screen and state, in both themes, at 100% and 135% text, with both clocks.');
  s.push('Rules: nothing off the edge or cut off; every target at least 48 dp; nothing under the display moves between Home’s states; every character from the embedded typefaces.');
  s.push(`At this screen’s own size, ${r.app[0]} × ${r.app[1]} dp: the rules above` + (full ? ', and Home’s fold.' : `. Home’s fold is a rule for 800 dp of height${r.homeOver ? `; at 100% text Home is ${r.homeOver} dp taller than this window` : ''}.`));
  if (r.frameChecked) s.push('In the 360 × 800 frame: the rules above, and Home’s fold. At 100% text Home does not scroll; at 135% the lever stays above the fold.');
  if (r.signature) s.push(r.sameChecked
    ? `Same as the reference: ${r.problems.some(p => /reference browser/.test(p)) ? 'no, see below' : 'yes'}. In the frame every line breaks where it does in the reference browser, and Home sits where it does there. Signature ${r.signature}: two phones that show the same signature draw the same lines.`
    : `Signature ${r.signature}. No reference is built into this file to compare it with.`);
  if (r.app[0] < 360) s.push(`This screen is ${r.app[0]} dp wide. The design is specified from 360 dp. Browser zoom or a large system font size narrows a page.`);
  s.push(`Took ${(r.ms / 1000).toFixed(1)} s.`);
  if (r.problems.length) { s.push(''); s.push('Problems:'); r.problems.forEach(p => s.push('- ' + p)); }
  return s.join('\n');
}

let selfTestBusy = false;
async function runSelfTestUI() {
  if (selfTestBusy) return;
  selfTestBusy = true;
  let el = $('#stest');
  if (!el) {
    el = document.createElement('div');
    el.id = 'stest'; el.className = 'stest'; el.setAttribute('role', 'dialog'); el.setAttribute('aria-label', 'Self-check');
    el.innerHTML = '<div class="stest-in"><h1>Self-check</h1><p id="stestSay" aria-live="polite"></p><pre id="stestOut" tabindex="0"></pre><p><button class="pbtn" id="stestClose">Close</button></p></div>';
    document.body.appendChild(el);
    $('#stestClose').addEventListener('click', () => { if (!selfTestBusy) el.remove(); });
  }
  const say = $('#stestSay'), outEl = $('#stestOut');
  outEl.textContent = '';
  say.textContent = 'Checking…';
  try {
    const r = await selfTest({ onStep: (n, total) => { if (n % 12 === 0) say.textContent = `Checking ${n} of ${total}…`; } });
    say.textContent = r.ok ? 'No problems.' : plural(r.problems.length, 'problem', 'problems') + '.';
    outEl.textContent = selfTestReport(r);
  } catch (e) {
    say.textContent = 'The check stopped.';
    outEl.textContent = String((e && e.stack) || e);
  }
  selfTestBusy = false;
}
