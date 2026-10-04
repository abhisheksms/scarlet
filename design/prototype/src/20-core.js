/* ───────────── state, formatting, counting ───────────── */

const $ = (s, r) => (r || document).querySelector(s);
const $$ = (s, r) => Array.from((r || document).querySelectorAll(s));
// Both faces are wanted before anything is measured. Left alone, the Devanagari face loads only when Hindi is first drawn.
function loadFonts() {
  if (!document.fonts || !document.fonts.load) return Promise.resolve();
  return Promise.all([document.fonts.load("500 21px 'Hanken Grotesk'", 'A'), document.fonts.load("500 21px 'Noto Sans Devanagari'", '\u0915')])
    .then(() => document.fonts.ready).catch(() => {});
}
// The last two words of a sentence stay on one line, so no line holds a single word.
// Done by hand because browsers that are asked to do it (text-wrap: pretty) each break the lines differently.
const TIE = '.legend p,.note,.strip-t,.strip-d,.lead,.body,.empty,.saved,.lic b,.lic span';
function tieWidows(root) {
  for (const el of root.querySelectorAll(TIE)) {
    let n = el.lastChild;
    while (n && n.nodeType === 1) n = n.classList.contains('later') ? n.previousSibling : n.lastChild;   // a Later tag is not part of the sentence
    if (!n || n.nodeType !== 3) continue;
    const s = n.nodeValue, i = s.lastIndexOf(' ');
    if (i > 0 && s.indexOf(' ') !== i && s.length - i <= 13) n.nodeValue = s.slice(0, i) + '\u00A0' + s.slice(i + 1);
  }
}
// The sentence in the display, and a dialog's question, are balanced: the narrowest box that keeps their number of lines,
// widened again if that left a line of one word and a wider box avoids it.
// Browsers offer this too (text-wrap: balance) but need not agree on where the lines end. This gives one answer everywhere.
const BALANCE = '.say,.say-sub,.dialog-t';
function balanceText(root) {
  for (const el of root.querySelectorAll(BALANCE)) {
    el.style.maxWidth = '';
    const full = el.clientWidth, h = el.offsetHeight;
    if (!full || !h) continue;
    let lo = 40, hi = full;
    while (lo < hi) {
      const mid = (lo + hi) >> 1;
      el.style.maxWidth = mid + 'px';
      if (el.offsetHeight > h) lo = mid + 1; else hi = mid;
    }
    let w = hi;
    el.style.maxWidth = w + 'px';
    if (w < full && loneWord(el)) {
      for (let t = w + 2; t <= full; t += 2) { el.style.maxWidth = t + 'px'; if (!loneWord(el)) { w = t; break; } }
    }
    el.style.maxWidth = w < full ? w + 'px' : '';
  }
}
// Does any line of this text hold a single word?
function loneWord(el) {
  const walk = document.createTreeWalker(el, NodeFilter.SHOW_TEXT), rg = document.createRange(), gap = (parseFloat(getComputedStyle(el).lineHeight) || 16) * 0.6, counts = [];
  let top = null;
  while (walk.nextNode()) {
    const n = walk.currentNode, re = /\S+/g;
    let m;
    while ((m = re.exec(n.nodeValue))) {
      rg.setStart(n, m.index); rg.setEnd(n, m.index + m[0].length);
      let t = null;
      for (const r of rg.getClientRects()) if (r.width > 0) { t = r.top; break; }
      if (t === null) continue;
      if (top === null || t > top + gap) counts.push(0);
      counts[counts.length - 1]++;
      top = t;
    }
  }
  return counts.length > 1 && counts.some(c => c === 1);
}
const esc = s => String(s).replace(/[&<>"]/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));

const S = {
  // prototype switches
  theme: 'light', ts: 1, clock: 24, data: 'heavy', ad: 'loaded', motion: 'full', consent: false,
  frame: true,           // on a phone: the 360 × 800 frame, the same picture on every phone. false lays the app out at the phone's own width
  later: { india: false, ads: false, hindi: false },
  // what Android reports
  role: 'held',          // held | missing | unavailable
  an: 'allowed',         // Android notifications: allowed | blocked | notAsked
  // the app
  screen: 'home', stack: [], overlay: null,
  mode: 'block', scope: 'all', pausedUntil: null, repeat: 15,
  allowOn: true, allowed: [], notif: true, summary: 'weekly',
  period: 30, daySel: 6, hourSel: null,
  rows: [], base: null,
  field: '', fieldErr: false, fieldOpen: false,
  india: { b140: false, a160: false }, restoreMsg: false,
  tab: 'switches', preset: null,
};

function resetApp(kind) {
  const d = mkBase(kind);
  S.data = kind; S.rows = d.rows; S.base = d.base;
  S.screen = 'home'; S.stack = []; S.overlay = null;
  S.scope = 'all'; S.pausedUntil = null; S.period = 30; S.daySel = 6; S.hourSel = null;
  S.field = ''; S.fieldErr = false; S.fieldOpen = false; S.restoreMsg = false;
  S.india = { b140: false, a160: false };
  if (kind === 'new') {
    S.mode = 'off'; S.role = 'missing'; S.an = 'notAsked'; S.notif = false; S.repeat = 0; S.allowOn = false; S.allowed = []; S.summary = 'off';
  } else if (kind === 'three') {
    S.mode = 'block'; S.role = 'held'; S.an = 'allowed'; S.notif = false; S.repeat = 0; S.allowOn = false; S.allowed = []; S.summary = 'off';
  } else {
    S.mode = 'block'; S.role = 'held'; S.an = 'allowed'; S.notif = true; S.repeat = 15; S.allowOn = true; S.summary = 'weekly';
    S.allowed = [{ n: N.d, until: null }, { n: N.e, until: at(1, 10, 5) }];
  }
}

/* ── words ── */
const hindi = () => S.later.hindi && S.screen === 'home';
function L(k, vars) {
  let s = (hindi() && HI[k] != null) ? HI[k] : T[k];
  if (vars) for (const v in vars) s = s.replace('{' + v + '}', vars[v]);
  return s;
}

/* ── times and numbers, as the phone is set ── */
const NB = ' ';
const p2 = n => String(n).padStart(2, '0');
function fTime(d) {
  const h = d.getHours(), m = p2(d.getMinutes());
  if (S.clock === 24) return p2(h) + ':' + m;
  return (h % 12 || 12) + ':' + m + NB + (h < 12 ? 'AM' : 'PM');
}
function fHour(h) {
  if (S.clock === 24) return p2(h) + ':00';
  return (h % 12 || 12) + NB + (h < 12 ? 'AM' : 'PM');
}
const fDate = d => d.getDate() + NB + MON[d.getMonth()] + NB + d.getFullYear();
const fDateTime = d => fDate(d) + ', ' + fTime(d);
const dayNo = d => Math.round(new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime() / 864e5);
const dayDiff = d => dayNo(d) - dayNo(NOW);          // 0 today, -1 yesterday, 1 tomorrow
function fUntil(d) {
  const dd = dayDiff(d);
  if (dd === 0) return fTime(d);
  if (dd === 1) return L('tomorrow', { t: fTime(d) });
  return fDateTime(d);
}
function fDay(d) {
  const dd = dayDiff(d);
  return dd === 0 ? 'Today' : dd === -1 ? 'Yesterday' : fDate(d);
}
const fNum = n => n.toLocaleString('en-IN');
const plural = (n, one, many) => fNum(n) + ' ' + (n === 1 ? one : many);
const calls = n => plural(n, 'call', 'calls');
const ordinal = n => n + (n % 100 >= 11 && n % 100 <= 13 ? 'th' : ['th', 'st', 'nd', 'rd', 'th', 'th', 'th', 'th', 'th', 'th'][n % 10]);
function fSaved(total) {
  const min = Math.floor(total * 30 / 60);
  if (min < 1) return 'under a minute';
  if (min < 60) return min + ' min';
  return fNum(Math.floor(min / 60)) + ' hr ' + (min % 60) + ' min';
}

/* ── counting: every figure on every screen comes from here ── */
function stats() {
  const b = S.base, rows = S.rows;
  const all = { b: b.all[0], s: b.all[1] };
  const daily = b.daily.map(x => ({ b: x[0], s: x[1] }));
  const wd = b.wd[S.period].slice(), hr = b.hr[S.period].slice(), hr7 = b.hr[7].slice();
  const top = Object.assign({}, b.top[S.period]);
  const latest = {};
  for (const r of rows) {
    const f = r.a === 'B' ? 'b' : 's', k = keyOf(r.n);
    all[f]++; daily[dayIndex(r.t)][f]++;
    wd[r.t.getDay()]++; hr[r.t.getHours()]++; hr7[r.t.getHours()]++;
    if (r.n != null) { const kk = k || r.n; top[kk] = (top[kk] || 0) + 1; if (!latest[kk] || r.t > latest[kk]) latest[kk] = r.t; }
  }
  const total = all.b + all.s;
  const week = daily.reduce((a, d) => ({ b: a.b + d.b, s: a.s + d.s }), { b: 0, s: 0 });
  const weekTotal = week.b + week.s;
  const d30 = b.d30 + rows.length;
  const pct = (cur, prev, given, at0) => (given != null && cur === at0) ? given : (prev ? Math.round((cur - prev) * 100 / prev) : null);
  const busiest = a => { const m = Math.max.apply(null, a); return m === 0 ? null : a.indexOf(m); };
  const order = 'abcde';
  const topList = Object.keys(top).filter(k => top[k] > 0).map(k => ({ n: N[k] || k, c: top[k], k }))
    .sort((x, y) => y.c - x.c || ((latest[y.k] || 0) - (latest[x.k] || 0)) || (order.indexOf(x.k) - order.indexOf(y.k))).slice(0, 10);
  const reached = LADDER.filter(m => m <= total).pop() || null;
  const next = LADDER.find(m => m > total) || null;
  // the 30-day line only says something once the history is older than a week
  const older = d30 > weekTotal || total > d30;
  return {
    all, total, daily, week, weekTotal, today: daily[6], d30, older,
    pct7: pct(weekTotal, b.prev7, b.pct7, b.week0), pct30: pct(d30, b.prev30, b.pct30, b.d300),
    busiest7: busiest(hr7), wd, hr, busyWd: busiest(wd), busyHr: busiest(hr), top: topList, reached, next,
  };
}

function numDetails(n) {
  const k = keyOf(n), base = (k && S.base.num[k]) || { b: 0, s: 0, first: null };
  const mine = S.rows.filter(r => r.n === n).sort((x, y) => x.t - y.t);
  const c = { b: base.b + mine.filter(r => r.a === 'B').length, s: base.s + mine.filter(r => r.a === 'S').length };
  const hidden = base.b + base.s > 0;
  const first = hidden && base.first ? base.first : (mine[0] ? mine[0].t : null);
  const last = mine.length ? mine[mine.length - 1].t : (hidden ? base.first : null);
  return { b: c.b, s: c.s, total: c.b + c.s, first, last };
}

/* ── the allow list. A number counts as allowed only while the list is on. ── */
const liveAllowed = () => S.allowed.filter(e => e.until == null || e.until > NOW);
const allowEntry = n => (S.allowOn && n != null) ? liveAllowed().find(e => e.n === n) || null : null;
function allowAdd(n, minutes) {
  S.allowed = S.allowed.filter(e => e.n !== n);
  S.allowed.push({ n, until: minutes == null ? null : new Date(NOW.getTime() + minutes * 6e4) });
  S.allowOn = true;
}
const allowText = e => e.until == null ? 'Always' : 'Until ' + fUntil(e.until);

/* ── what Home says: role missing, then paused, then the mode ── */
const total0 = () => S.base.all[0] + S.base.all[1] + S.rows.length === 0;
const isPaused = () => S.mode !== 'off' && S.pausedUntil != null && S.pausedUntil > NOW;
function statusKind() {
  if (S.role === 'unavailable') return 'cannot';
  if (S.mode !== 'off' && S.role !== 'held') return 'role';
  if (isPaused()) return 'paused';
  if (S.mode === 'off') return total0() ? 'first' : 'off';
  return S.mode;
}
function lampState(row) {
  const k = statusKind();
  if (row === 'off') return S.mode === 'off' ? 'lit' : 'dark';
  if (S.mode !== row || k === 'role' || k === 'cannot') return 'dark';
  return k === 'paused' ? 'held' : 'lit';
}
const notifState = () => S.an === 'blocked' ? 'blocked' : (S.notif && S.an === 'allowed' ? 'on' : 'off');
function excSummary() {
  const parts = [], n = S.allowOn ? liveAllowed().length : 0;
  if (S.repeat) parts.push(L('repeat_ring'));
  if (n === 1) parts.push(L('one_allowed')); else if (n > 1) parts.push(L('n_allowed', { n }));
  return parts.length ? parts : [L('no_exc')];   // drawn with a middle dot between them
}

/* ── a small DOM morph, so elements persist between renders and their transitions can run ── */
function morph(from, to) {
  for (const a of Array.from(from.attributes)) if (!to.hasAttribute(a.name)) from.removeAttribute(a.name);
  for (const a of Array.from(to.attributes)) if (from.getAttribute(a.name) !== a.value) from.setAttribute(a.name, a.value);
  const fc = Array.from(from.childNodes), tc = Array.from(to.childNodes);
  for (let i = 0; i < tc.length; i++) {
    const f = fc[i], t = tc[i];
    if (!f) { from.appendChild(t); continue; }
    if (f.nodeType !== t.nodeType || (f.nodeType === 1 && (f.tagName !== t.tagName || f.getAttribute('data-k') !== t.getAttribute('data-k')))) { from.replaceChild(t, f); continue; }
    if (f.nodeType === 3) { if (f.nodeValue !== t.nodeValue) f.nodeValue = t.nodeValue; continue; }
    if (f.nodeType === 1) morph(f, t);
  }
  for (let i = fc.length - 1; i >= tc.length; i--) from.removeChild(fc[i]);
}
