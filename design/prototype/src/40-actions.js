/* ───────────── rendering, motion and what each control does ───────────── */

const M = { key: '', winH: 0, bayH: 0, timeW: 0 };
let K = 1;               // scale of the phone on the bench
let afterClose = null;   // what to do once an overlay has left
let closing = false;

/* The display and the bay are one size in every state: the tallest thing each can ever show at this width and text size.
   In Compose this is a custom layout that measures the candidates; here a hidden box does it. */
function computeMetrics() {
  const ph = $('#phone'), w = (ph && ph.clientWidth) || 360, st = stats();
  const sig = [w, S.ts, S.clock, hindi(), st.total === 0, st.today.b + st.today.s, st.week.b, st.week.s].join('|');
  if (M.key === sig) return;
  M.key = sig;
  const box = $('#measure');
  box.className = ['sw', S.theme, hindi() ? 'hi' : ''].join(' ');
  box.style.cssText = `--ts:${S.ts};width:${w}px`;
  const h = html => { box.innerHTML = `<div style="width:${w - 32}px">${html}</div>`; fitKeys(box); tieWidows(box); return box.firstElementChild.getBoundingClientRect().height; };
  const win = (kind, o) => h(`<div class="win" style="min-height:0">${statusH(kind, o).replace(' id="status"', '')}</div>`);
  let sMax = Math.max(win('off'), win('cannot'), win('role'));
  for (const scope of ['all', 'intl']) for (const k of ['silence', 'block']) sMax = Math.max(sMax, win(k, { scope }));
  for (const until of [fTime(at(0, 20, 30)), L('tomorrow', { t: fTime(at(1, 19, 30)) })]) sMax = Math.max(sMax, win('paused', { until }));
  if (st.total === 0) for (const scope of ['all', 'intl']) sMax = Math.max(sMax, win('first', { scope }));
  M.winH = Math.ceil(sMax + h(`<div class="win" style="min-height:0">${tilesH(st)}</div>`));
  M.bayH = Math.ceil(Math.max(h(pauseKeys()), h(mainKey(L('resume'), 'x')), h(mainKey(L('role_btn'), 'x')), h(`<p class="note">${esc(L('privacy'))}</p>`)));
  // the widest a time can be, for History's time column where the browser has no subgrid
  box.innerHTML = [at(0, 10, 48), at(0, 22, 48)].map(d => `<span class="hrow-time" style="display:inline-block">${fTime(d)}</span>`).join('<br>');
  M.timeW = Math.ceil(Math.max.apply(null, Array.from(box.children, e => e.getBoundingClientRect().width)));
  box.innerHTML = '';
}

// Keys share a row equally unless a label needs more. If they cannot share one row, four keys go two by two; otherwise the strip stacks.
function fitKeys(root) {
  for (const k of root.querySelectorAll('.keys')) {
    k.classList.remove('is-two', 'is-stack');
    if (k.scrollWidth <= k.clientWidth + 0.5) continue;
    if (k.children.length === 4) { k.classList.add('is-two'); if (k.scrollWidth <= k.clientWidth + 0.5) continue; k.classList.remove('is-two'); }
    k.classList.add('is-stack');
  }
}

function render(o) {
  o = o || {};
  if (o.still) S.still = true;
  syncShell();
  computeMetrics();
  const tpl = document.createElement('template');
  tpl.innerHTML = appH(M).trim();
  tieWidows(tpl.content);
  morph($('#phone'), tpl.content.firstElementChild);
  fitKeys($('#phone'));
  balanceText($('#phone'));
  const inp = $('#numField');
  if (inp && inp.value !== S.field) inp.value = S.field;
  if (S.still) requestAnimationFrame(() => requestAnimationFrame(() => { S.still = false; const p = $('#phone'); if (p) p.classList.remove('still'); }));
  drawPanel();
}

/* The page around the app follows it, before anything is measured: the frame or the whole window on a phone, edge to edge in
   full screen and in screenshots, and the browser's own bars in the colour of what they touch. */
let shellSig = '';
function syncShell() {
  const root = document.documentElement, framed = S.frame && !S.shot, sig = [S.theme, !!S.shot, !!fsOn(), framed].join('|');
  if (sig === shellSig) return;
  shellSig = sig;
  root.classList.toggle('e2e', !!S.shot || !!fsOn());
  root.classList.toggle('shot', !!S.shot);
  root.classList.toggle('framed', framed);
  root.setAttribute('data-app', S.theme);
  let m = document.querySelector('meta[name="theme-color"]');
  if (!m && document.head) { m = document.createElement('meta'); m.name = 'theme-color'; document.head.appendChild(m); }
  if (m) m.content = framed && window.innerWidth < 600 ? '#0B0C0D' : S.theme === 'dark' ? '#1C1E21' : '#D2D6D1';
  fit();
}

/* ── motion ── */
const reduced = () => S.motion === 'reduced';
function tick() { try { if (navigator.vibrate) navigator.vibrate(10); } catch (e) { /* no haptics here */ } }
// The sentence in the display cross-fades over 120 ms. After a lever move it waits 180 ms for the handle to seat.
function swapStatus(oldHTML, delay) {
  const st = $('#phone #status');
  if (!st || oldHTML == null || reduced() || st.innerHTML === oldHTML || !st.animate) return;
  const ghost = document.createElement('div');
  ghost.className = 'st-ghost'; ghost.setAttribute('aria-hidden', 'true'); ghost.innerHTML = oldHTML;
  const kids = Array.from(st.children);
  st.appendChild(ghost);
  const total = delay + 120, off = delay / total;
  kids.forEach(k => k.animate([{ opacity: 0 }, { opacity: 0, offset: off }, { opacity: 1 }], { duration: total, easing: 'linear' }));
  const g = ghost.animate([{ opacity: 1 }, { opacity: 1, offset: off }, { opacity: 0 }], { duration: total, easing: 'linear', fill: 'forwards' });
  g.onfinish = () => ghost.remove();
}
function withSwap(delay, change) {
  const st = $('#phone #status'), old = st ? st.innerHTML : null;
  change();
  render();
  swapStatus(old, delay);
}
function nudge() {
  const h = $('#phone [data-handle]');
  if (!h || reduced()) return;
  h.classList.remove('is-nudge-down'); void h.offsetWidth; h.classList.add('is-nudge-down');
  setTimeout(() => h.classList.remove('is-nudge-down'), 200);
}

/* ── the mode ── */
function setMode(m) {
  withSwap(180, () => { S.mode = m; });
  if (reduced()) tick(); else setTimeout(tick, 180);
}
// Returns true when the handle should stay where it was put.
function chooseMode(m) {
  if (m === S.mode) return false;
  if (S.role === 'unavailable' && m !== 'off') { nudge(); return false; }
  if (m !== 'off' && S.role !== 'held') { open({ type: 'sys', kind: 'role', pending: m }); return false; }
  setMode(m);
  return true;
}

/* ── moving between screens. The system back gesture is wired in where the browser allows it. ── */
let hDepth = 0, hOK = true, backWait = 0;
function pushNav() { if (!hOK) return; try { history.pushState({ p: hDepth + 1 }, ''); hDepth++; } catch (e) { hOK = false; hDepth = 0; } }
function leave(s) {
  if (s === 'options') { S.field = ''; S.fieldErr = false; S.fieldOpen = false; }
  if (s === 'settings') S.restoreMsg = false;
  if (s === 'statistics') S.hourSel = null;
}
function go(s) {
  const v = $('#view');
  S.stack.push({ s: S.screen, y: v ? v.scrollTop : 0 });
  leave(S.screen); S.screen = s; pushNav(); render();
  $('#view').scrollTop = 0;
}
function open(o) {
  S.overlay = o; pushNav(); render();
  // focus moves into what opened: Cancel on the confirmation, otherwise the surface itself
  const f = $('#phone .dialog [data-act="back"]') || $('#phone .sheet') || $('#phone .standin') || $('#phone .shade');
  if (f) { if (!f.hasAttribute('tabindex') && f.tagName !== 'BUTTON') f.setAttribute('tabindex', '-1'); try { f.focus({ preventScroll: true }); } catch (e) { /* fine */ } }
}
function closeOverlay() {
  const fin = () => { closing = false; S.overlay = null; const f = afterClose; afterClose = null; render(); if (f) f(); };
  const layer = $('#phone .layer');
  if (reduced() || !layer) return fin();
  closing = true; layer.classList.add('is-leaving');
  setTimeout(fin, 160);
}
function doBack() {
  if (closing) return;
  if (S.overlay) return closeOverlay();
  if (S.stack.length) { const p = S.stack.pop(); leave(S.screen); S.screen = p.s; render(); $('#view').scrollTop = p.y; }
}
function uiBack() {
  if (hOK && hDepth > 0) {
    const tk = ++backWait;
    try { history.back(); } catch (e) { hOK = false; return doBack(); }
    setTimeout(() => { if (backWait === tk) { hOK = false; hDepth = 0; doBack(); } }, 700);   // the viewer swallowed it
  } else doBack();
}
window.addEventListener('popstate', () => { backWait++; if (hDepth > 0) { hDepth--; doBack(); } });

function fullScreen(fail) {
  const el = document.documentElement, enter = el.requestFullscreen || el.webkitRequestFullscreen, exit = document.exitFullscreen || document.webkitExitFullscreen;
  try {
    if (fsOn()) { if (exit) exit.call(document); }
    else if (enter) { const p = enter.call(el); if (p && p.catch) p.catch(fail); }
    else fail();
  } catch (e) { fail(); }
}

/* ── what each control does ── */
function gateNotifications(then) {
  if (S.an === 'allowed') return true;
  if (S.an === 'notAsked') open({ type: 'sys', kind: 'notifperm', then });
  return false;
}
function applyThen(then) {
  if (!then) return;
  if (then === 'notif') S.notif = true;
  else if (then.indexOf('summary:') === 0) S.summary = then.slice(8);
}
function act(a, v) {
  if (closing) return;
  const o = S.overlay;
  switch (a) {
    case 'panel': return openPanel();
    case 'fs': return fullScreen(() => { S.noFS = true; render(); });
    case 'go': return go(v);
    case 'back': return uiBack();
    case 'mode': return void chooseMode(v);
    case 'pause': return withSwap(0, () => { S.pausedUntil = new Date(NOW.getTime() + (+v) * 6e4); });
    case 'resume': return withSwap(0, () => { S.pausedUntil = null; });
    case 'role': return open({ type: 'sys', kind: 'role', pending: null });
    case 'roleAccept': { const p = o && o.pending; S.role = 'held'; afterClose = p ? () => setMode(p) : null; return uiBack(); }
    case 'notif':
      if (notifState() === 'on') { S.notif = false; return render(); }
      if (gateNotifications('notif')) { S.notif = true; render(); }
      return;
    case 'permAllow': S.an = 'allowed'; applyThen(o && o.then); return uiBack();
    case 'permDeny': S.an = 'blocked'; return uiBack();
    case 'sys': return open({ type: 'sys', kind: v });
    // Options
    case 'scope': S.scope = v; return render();
    case 'repeat': S.repeat = +v; return render();
    case 'allowToggle': S.allowOn = !S.allowOn; if (!S.allowOn) { S.field = ''; S.fieldErr = false; S.fieldOpen = false; } return render();
    case 'unallowRow': S.allowed = S.allowed.filter(e => e.n !== v); return render();
    case 'allowTyped': {
      const raw = S.field.trim().replace(/\s+/g, ' ');
      if (!/\d/.test(raw)) { S.fieldErr = true; return render(); }   // a real number has digits
      allowAdd(raw, v === 'always' ? null : +v);
      S.field = ''; S.fieldErr = false; S.fieldOpen = false;
      const inp = $('#numField'); if (inp) inp.blur();
      return render();
    }
    case 'india': S.india[v] = !S.india[v]; return render();
    // History and the sheet
    case 'row': { const r = S.rows.find(x => x.id === +v); return r ? open({ type: 'sheet', num: r.n, callId: r.id }) : undefined; }
    case 'top': return open({ type: 'sheet', num: v });
    case 'allow': if (o) allowAdd(o.num, v === 'always' ? null : +v); return uiBack();
    case 'unallow': if (o) S.allowed = S.allowed.filter(e => e.n !== o.num); return uiBack();
    case 'delCall': if (o) S.rows = S.rows.filter(r => r.id !== o.callId); return uiBack();
    case 'askDeleteAll': return open({ type: 'dialog' });
    case 'deleteAll': S.rows = []; S.base = mkBase('new').base; S.daySel = 6; S.hourSel = null; return uiBack();
    // Statistics
    case 'period': S.period = +v; S.hourSel = null; return render();
    case 'day': S.daySel = +v; return render();
    // Settings
    case 'summary':
      if (v === 'off') { S.summary = 'off'; return render(); }
      if (gateNotifications('summary:' + v)) { S.summary = v; render(); }
      return;
    case 'bought': S.ad = 'removed'; S.restoreMsg = false; return uiBack();
    case 'restoreNone': S.restoreMsg = true; return uiBack();
    // a notification was tapped: the app opens on that screen, with Home beneath it
    case 'ntap': S.overlay = null; S.stack = [{ s: 'home', y: 0 }]; S.screen = v; hDepth = 0; render(); $('#view').scrollTop = 0; return;
  }
}

function wire() {
  const stage = $('#stage');
  stage.addEventListener('click', e => {
    const el = e.target.closest('[data-act]');
    if (!el || !stage.contains(el)) return;
    act(el.dataset.act, el.dataset.v);
  });
  stage.addEventListener('input', e => {
    if (e.target.id !== 'numField') return;
    S.field = e.target.value; S.fieldErr = false; S.fieldOpen = true; render();
  });
  stage.addEventListener('focusin', e => {
    if (e.target.id !== 'numField') return;
    if (!S.fieldOpen) { S.fieldOpen = true; render(); }
    // as imePadding and bringIntoView would in the app: the field and its keys sit above the keyboard
    const v = $('#view'), f = $('#numField');
    if (v && f) v.scrollTop += (f.getBoundingClientRect().top - v.getBoundingClientRect().top) / K - 64;
  });
  stage.addEventListener('focusout', e => {
    // iPhone Safari can leave the page pushed up after its keyboard closes
    if (e.target.id === 'numField' && (window.scrollY || document.documentElement.scrollTop)) window.scrollTo(0, 0);
  });
  stage.addEventListener('keydown', e => {
    if (e.target.id === 'numField' && e.key === 'Enter') e.preventDefault();
  });
  // iPhone Safari shows :active only where something listens for touches
  document.addEventListener('touchstart', () => {}, { passive: true });
  document.addEventListener('keydown', e => {
    if (e.key !== 'Escape') return;
    if ($('#panel').classList.contains('is-open')) closePanel(); else if (S.overlay || S.stack.length) uiBack();
  });

  // dragging the lever handle
  let drag = null;
  stage.addEventListener('pointerdown', e => {
    const h = e.target.closest('[data-handle]');
    if (h) {
      const rowH = h.parentNode.querySelector('.lever-row').offsetHeight, idx = { off: 0, silence: 1, block: 2 }[S.mode];
      drag = { h, y0: e.clientY, rowH, idx, base: idx * rowH + (rowH - 36) / 2, moved: false };
      try { h.setPointerCapture(e.pointerId); } catch (x) { /* fine */ }
      return;
    }
    const hrs = e.target.closest('[data-hrs]');
    if (hrs) { scrub = { hrs, start: S.hourSel, moved: false, first: hourAt(hrs, e.clientX) }; setHour(scrub.first); }
  });
  stage.addEventListener('pointermove', e => {
    if (drag) {
      const dy = (e.clientY - drag.y0) / K;
      if (Math.abs(dy) > 3) drag.moved = true;
      if (!drag.moved) return;
      const y = Math.max((drag.rowH - 36) / 2, Math.min(2 * drag.rowH + (drag.rowH - 36) / 2, drag.base + dy));
      drag.h.classList.add('is-drag'); drag.h.style.transform = `translateY(${y}px)`; drag.y = y;
    } else if (scrub && e.buttons !== 0) {
      const i = hourAt(scrub.hrs, e.clientX);
      if (i !== S.hourSel) { scrub.moved = true; setHour(i); }
    }
  });
  const endDrag = () => {
    if (drag) {
      const d = drag; drag = null;
      if (!d.moved) return;
      const stop = Math.max(0, Math.min(2, Math.round((d.y - (d.rowH - 36) / 2) / d.rowH)));
      const kept = stop !== d.idx && chooseMode(['off', 'silence', 'block'][stop]);
      if (!kept) { d.h.classList.remove('is-drag'); d.h.style.transform = ''; }   // back to its stop
    }
    if (scrub) { if (!scrub.moved && scrub.start === scrub.first) setHour(null); scrub = null; }
  };
  stage.addEventListener('pointerup', endDrag);
  stage.addEventListener('pointercancel', endDrag);
}
let scrub = null;
function hourAt(hrs, x) { const r = hrs.getBoundingClientRect(); return Math.max(0, Math.min(23, Math.floor((x - r.left) / r.width * 24))); }
function setHour(i) { if (S.hourSel !== i) { S.hourSel = i; render(); } }
