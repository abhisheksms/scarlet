/* ───────────── drawing: every surface is a function of the state ───────────── */

const IC = {
  gear: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="12" cy="12" r="6.2"></circle><circle cx="12" cy="12" r="1.6" fill="currentColor" stroke="none"></circle><path stroke-width="3.2" d="M12 1.6v3M12 19.4v3M1.6 12h3M19.4 12h3M4.65 4.65l2.1 2.1M17.25 17.25l2.1 2.1M4.65 19.35l2.1-2.1M17.25 6.75l2.1-2.1"></path></svg>',
  chev: s => `<svg width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" aria-hidden="true"><path d="M9 5l7 7-7 7"></path></svg>`,
  out: '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" aria-hidden="true"><path d="M9 6h9v9M18 6 6.5 17.5"></path></svg>',
  back: '<svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" aria-hidden="true"><path d="M20 12H5M11 5.5 4.5 12l6.5 6.5"></path></svg>',
  share: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" aria-hidden="true"><circle cx="18" cy="5" r="2.6"></circle><circle cx="6" cy="12" r="2.6"></circle><circle cx="18" cy="19" r="2.6"></circle><path d="M8.3 10.7l7.4-4.3M8.3 13.3l7.4 4.3"></path></svg>',
  x: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" aria-hidden="true"><path d="M6 6l12 12M18 6 6 18"></path></svg>',
};
const HANDSET = 'M54 44 C37 44 23 51 19.5 60.5 C18 64.5 19 68 21 71 L24 75.5 C25.3 77.4 27.8 77.9 29.7 76.7 L37.2 72 C38.9 70.9 39.6 68.8 38.9 66.9 L37.4 62.6 C42.3 60 48 58.6 54 58.6 C60 58.6 65.7 60 70.6 62.6 L69.1 66.9 C68.4 68.8 69.1 70.9 70.8 72 L78.3 76.7 C80.2 77.9 82.7 77.4 84 75.5 L87 71 C89 68 90 64.5 88.5 60.5 C85 51 71 44 54 44 Z';
const iconAdaptive = (size, radius) => `<svg width="${size}" height="${size}" viewBox="18 18 72 72" role="img" aria-label="App icon" style="border-radius:${radius};display:block"><rect x="0" y="0" width="108" height="108" fill="#1C1E21"></rect><g transform="translate(54 54) scale(.95) translate(-54 -54)"><rect x="29" y="30" width="50" height="9" rx="4.5" fill="#0B0C0D"></rect><rect x="62" y="27" width="15" height="15" rx="3" fill="#F5C63C"></rect><g transform="translate(54 64) scale(.64) translate(-54 -60.75)"><path fill="#F0EDE4" d="${HANDSET}"></path></g></g></svg>`;
const iconThemed = (size, bg, fg) => `<svg width="${size}" height="${size}" viewBox="18 18 72 72" role="img" aria-label="Themed icon" style="border-radius:26%;display:block;background:${bg}"><g transform="translate(54 54) scale(.95) translate(-54 -54)"><rect x="29" y="31.5" width="50" height="6" rx="3" fill="none" stroke="${fg}" stroke-width="1.8"></rect><rect x="62" y="27" width="15" height="15" rx="3" fill="${fg}"></rect><g transform="translate(54 64) scale(.64) translate(-54 -60.75)"><path fill="${fg}" d="${HANDSET}"></path></g></g></svg>`;
const iconNotif = (size, fg) => `<svg width="${size}" height="${size}" viewBox="18 18 72 72" aria-hidden="true" style="display:block"><rect x="29" y="31" width="50" height="7" rx="3.5" fill="none" stroke="${fg}" stroke-width="2.6"></rect><rect x="62" y="26.5" width="16" height="16" rx="3" fill="${fg}"></rect><g transform="translate(54 65) scale(.7) translate(-54 -60.75)"><path fill="${fg}" d="${HANDSET}"></path></g></svg>`;

const PAUSE = [[15, 'k15', '15 minutes'], [60, 'k60', '1 hour'], [240, 'k240', '4 hours'], [1440, 'k1440', '24 hours']];
const ALLOW = [[60, '1 Hr', '1 hour'], [1440, '24 Hr', '24 hours'], [null, 'Always', 'Always']];
const later = () => '<span class="later">Later</span>';
const mark = a => `<i class="mk ${a === 'B' ? 'mk-b' : 'mk-s'}"></i>`;
// Parts joined by a middle dot. A part that wraps to a new line leaves its dot behind: see .dots.
const dotsH = parts => parts.length > 1 ? `<span class="dots">${parts.map(p => `<span>${p}</span>`).join('')}</span>` : (parts[0] || '');
const numH = n => `<span class="nb">${esc(n == null ? T.no_number : n)}</span>`;

/* ── small parts ── */
function keysH(items, o) {
  // items: [{label, act, v, on, aria}]; o.radio makes it a latching group
  o = o || {};
  const ks = items.map(k => `<button class="key${k.on ? ' is-latched' : ''}${k.cls ? ' ' + k.cls : ''}" data-act="${k.act}"${k.v != null ? ` data-v="${esc(k.v)}"` : ''}${o.radio ? ` role="radio" aria-checked="${!!k.on}"` : ''}${k.aria ? ` aria-label="${esc(k.aria)}"` : ''}>${esc(k.label)}</button>`).join('');
  return `<div class="keys${o.stack ? ' is-stack' : ''}" style="--n:${items.length}"${o.radio ? ` role="radiogroup" aria-label="${esc(o.radio)}"` : ''}>${ks}</div>`;
}
const keysBlock = (cap, items, o) => `<div class="keys-block"><span class="cap">${esc(cap)}</span>${keysH(items, o)}</div>`;
const mainKey = (label, act) => `<button class="key-main" data-act="${act}">${esc(label)}</button>`;
function stripH(o) {
  // o: {title, detail (text, or parts to join with a middle dot), trail:'chev'|'out'|'switch'|'none'|text, on, act, v, aria, tag}
  const trail = o.trail === 'switch' ? '<span class="pswitch" aria-hidden="true"></span>' : o.trail === 'chev' ? IC.chev(18) : o.trail === 'out' ? IC.out : o.trail === 'none' || !o.trail ? '' : `<span class="strip-v">${esc(o.trail)}</span>`;
  const inner = `<span class="strip-txt"><span class="strip-t">${esc(o.title)}${o.tag ? later() : ''}</span>${o.detail ? `<span class="strip-d">${Array.isArray(o.detail) ? dotsH(o.detail.map(esc)) : esc(o.detail)}</span>` : ''}</span>${trail}`;
  if (!o.act) return `<div class="strip">${inner}</div>`;
  const sw = o.trail === 'switch' ? ` role="switch" aria-checked="${!!o.on}"` : '';
  return `<button class="strip${o.on ? ' is-on' : ''}${o.cls ? ' ' + o.cls : ''}" data-act="${o.act}"${o.v != null ? ` data-v="${esc(o.v)}"` : ''}${sw}${o.aria ? ` aria-label="${esc(o.aria)}"` : ''}>${inner}</button>`;
}
const hdrH = (title, action) => `<div class="hdr"><button class="hdr-b" data-act="back" aria-label="Back">${IC.back}</button><h1 class="hdr-t">${esc(title)}</h1>${action || ''}</div>`;

/* ── the display window ── */
function statusInner(kind, o) {
  o = o || {};
  const say = t => `<p class="say">${esc(t)}</p>`;
  const intl = (o.scope || S.scope) === 'intl';
  switch (kind) {
    case 'first': return `<div class="st"><div class="st-col">${say(L('off_d'))}</div></div><div class="legend"><p><b>${esc(L('silence'))}</b>${esc(L(intl ? 'silence_i' : 'silence_d'))}</p><p><b>${esc(L('block'))}</b>${esc(L(intl ? 'block_i' : 'block_d'))}</p></div>`;
    case 'off': return `<div class="st">${say(L('off_d'))}</div>`;
    case 'silence': return `<div class="st">${say(L(intl ? 'silence_i' : 'silence_d'))}</div>`;
    case 'block': return `<div class="st">${say(L(intl ? 'block_i' : 'block_d'))}</div>`;
    case 'paused': return `<div class="st"><span class="st-bars" aria-hidden="true"><i></i><i></i></span>${say(L('paused', { t: o.until || fUntil(S.pausedUntil) }))}</div>`;
    case 'role': return `<div class="st"><span class="st-sq" aria-hidden="true"></span><div class="st-col">${say(L('role1'))}<p class="say-sub">${esc(L('role2'))}</p></div></div>`;
    case 'cannot': return `<div class="st">${say(L('cannot'))}</div>`;
  }
  return '';
}
const statusH = (kind, o) => `<div class="win-status${kind === 'first' ? ' is-first' : ''}" id="status" aria-live="polite">${statusInner(kind, o)}</div>`;
function tilesH(st) {
  const today = st.today.b + st.today.s, empty = st.total === 0 && today === 0;
  const cap = k => `<span class="tile-cap">${esc(L(k))}${IC.chev(16)}</span>`;
  const hist = today === 0 ? `<span class="tile-line">${esc(L('nothing_today'))}</span>` : `<span class="tile-num">${fNum(today)}</span><span class="tile-unit">${esc(L(today === 1 ? 'call_today' : 'calls_today'))}</span>`;
  const stat = st.total === 0 ? `<span class="tile-line">${esc(L('nothing_yet'))}</span>` : st.weekTotal === 0 ? `<span class="tile-line">${esc(L('nothing_7'))}</span>`
    : `<span class="tile-line">${mark('B')}${esc(L('n_blocked', { n: fNum(st.week.b) }))}</span><span class="tile-line">${mark('S')}${esc(L('n_silenced', { n: fNum(st.week.s) }))}</span><span class="tile-unit">${esc(L('in7'))}</span>`;
  return `<div class="tiles${empty ? ' is-empty' : ''}"><button class="tile" data-act="go" data-v="history">${cap('history')}${hist}</button><button class="tile" data-act="go" data-v="statistics">${cap('statistics')}${stat}</button></div>`;
}
function leverH(o) {
  // o: {mode, lamp:{off,silence,block}, locked}
  const idx = { off: 0, silence: 1, block: 2 }[o.mode];
  const rows = ['off', 'silence', 'block'].map(m => {
    const set = o.mode === m, ls = o.lamp[m];
    const lamp = m === 'off' ? `<span class="ring${ls === 'lit' ? ' is-lit' : ''}"></span>` : `<span class="lamp${ls === 'lit' ? ' is-lit' : ls === 'held' ? ' is-held' : ''}"></span>`;
    return `<button class="lever-row${set ? ' is-set' : ''}" role="radio" aria-checked="${set}" data-act="mode" data-v="${m}"${o.locked && m !== 'off' ? ' aria-disabled="true"' : ''}><span class="lever-tick"></span><span class="lever-label">${esc(L(m))}</span>${lamp}</button>`;
  }).join('');
  return `<div class="lever" role="radiogroup" aria-label="Mode"><span class="lever-slot" aria-hidden="true"></span>${rows}<span class="lever-handle" data-handle style="--stop:${idx}" aria-hidden="true"><i></i><i></i><i></i></span></div>`;
}
const pauseKeys = () => keysBlock(L('ring_for'), PAUSE.map(p => ({ label: L(p[1]), act: 'pause', v: p[0], aria: p[2] })));
function bayInner(kind) {
  if (kind === 'cannot') return '';
  if (kind === 'role') return mainKey(L('role_btn'), 'role');
  if (kind === 'paused') return mainKey(L('resume'), 'resume');
  if (kind === 'off' || kind === 'first') return `<p class="note">${esc(L('privacy'))}</p>`;
  return pauseKeys();
}
function notifStripH() {
  const ns = notifState();
  if (ns === 'blocked') return stripH({ title: L('notifications'), detail: L('notif_blocked'), trail: 'out', act: 'sys', v: 'asettings', aria: T.notifications + '. ' + T.notif_blocked + '. ' + T.open_settings });
  return stripH({ title: L('notifications'), detail: L(ns === 'on' ? 'notif_on' : 'notif_off'), trail: 'switch', on: ns === 'on', act: 'notif' });
}

/* ── Home ── */
function vHome() {
  const k = statusKind(), st = stats();
  return `<div class="scr home" data-k="home">
<section class="win" aria-label="Status"><button class="gear" data-act="go" data-v="settings" aria-label="Settings">${IC.gear}</button>${statusH(k)}${tilesH(st)}</section>
${leverH({ mode: S.mode, lamp: { off: lampState('off'), silence: lampState('silence'), block: lampState('block') }, locked: S.role === 'unavailable' })}
<div class="bay">${bayInner(k)}</div>
<div class="strips">${stripH({ title: L('options'), detail: excSummary(), trail: 'chev', act: 'go', v: 'options' })}${notifStripH()}</div>
</div>`;
}

/* ── Options ── */
function vOptions() {
  const intl = S.scope === 'intl';
  const scope = `<div class="sec is-first"><span class="strip-t">Who is filtered</span>${keysH([
    { label: 'Everyone', act: 'scope', v: 'all', on: !intl }, { label: 'International Only', act: 'scope', v: 'intl', on: intl }], { radio: 'Who is filtered' })}
<p class="body dim">${intl ? 'Only international callers outside your contacts.' : 'Everyone outside your contacts.'}</p></div>`;
  const i140 = S.later.india ? `<div class="strips is-closed">${stripH({ title: 'Always block 140 numbers', tag: true, detail: S.india.b140 ? 'On. Telemarketers must call from these.' : 'Off', trail: 'switch', on: S.india.b140, act: 'india', v: 'b140' })}</div>` : '';
  const repeat = `<div class="sec"><span class="strip-t">Repeat callers</span><p class="body dim">A stopped number that calls again within this time rings.</p>${keysH([
    { label: 'Off', act: 'repeat', v: 0, on: S.repeat === 0 }, { label: '5 Min', act: 'repeat', v: 5, on: S.repeat === 5, aria: '5 minutes' },
    { label: '15 Min', act: 'repeat', v: 15, on: S.repeat === 15, aria: '15 minutes' }, { label: '30 Min', act: 'repeat', v: 30, on: S.repeat === 30, aria: '30 minutes' }], { radio: 'Repeat callers' })}</div>`;
  const live = liveAllowed();
  let list = '';
  if (S.allowOn) {
    list = live.length ? live.map(e => `<div class="arow"><span class="arow-t"><span class="num">${numH(e.n)}</span><span class="strip-d">${esc(allowText(e))}</span></span><button class="arow-x" data-act="unallowRow" data-v="${esc(e.n)}" aria-label="Remove ${esc(e.n)}">${IC.x}</button></div>`).join('')
      : `<p class="body dim">No numbers yet.</p>`;
    const open = S.fieldOpen || S.field !== '' || S.fieldErr;
    list += `<input class="field${S.fieldErr ? ' is-error' : ''}" id="numField" type="tel" inputmode="tel" autocomplete="off" placeholder="Phone number" aria-label="Phone number to allow" value="${esc(S.field)}"${S.fieldErr ? ' aria-invalid="true"' : ''}>`;
    if (S.fieldErr) list += `<p class="err" role="alert"><i aria-hidden="true"></i>Enter a phone number.</p>`;
    if (open) list += keysBlock('Let this number ring', ALLOW.map(a => ({ label: a[1], act: 'allowTyped', v: a[0] == null ? 'always' : a[0], aria: a[2] })));
  }
  const allow = `<div class="sec">${stripH({ title: 'Allow list', detail: S.allowOn ? 'On' : 'Off', trail: 'switch', on: S.allowOn, act: 'allowToggle', cls: 'is-head' })}${list}</div>`;
  const i160 = S.later.india ? `<div class="strips is-closed">${stripH({ title: 'Always allow 160 numbers', tag: true, detail: S.india.a160 ? 'On. Banks, insurers and government services call from these.' : 'Off', trail: 'switch', on: S.india.a160, act: 'india', v: 'a160' })}</div>` : '';
  return `<div class="scr${S.fieldOpen ? ' is-typing' : ''}" data-k="options">${hdrH('Options')}${scope}${i140}${repeat}${allow}${i160}</div>`;
}

/* ── History ── */
function hrowH(r, o) {
  // o: {nth, allow}
  const sub = `${mark(r.a)}<span>${dotsH([r.a === 'B' ? T.blocked : T.silenced].concat(o.nth > 1 ? [ordinal(o.nth) + ' call'] : []))}</span>`;
  const al = o.allow ? `<span class="hrow-allow">${esc(o.allow.until == null ? 'On the allow list' : 'Rings until ' + fUntil(o.allow.until))}</span>` : '';
  const aria = `${fTime(r.t)}, ${r.n == null ? T.no_number : r.n}, ${r.a === 'B' ? T.blocked : T.silenced}${o.nth > 1 ? ', ' + ordinal(o.nth) + ' call that day' : ''}${o.allow ? ', ' + (o.allow.until == null ? 'on the allow list' : 'rings until ' + fUntil(o.allow.until)) : ''}`;
  return `<button class="hrow" data-act="row" data-v="${r.id}" aria-label="${esc(aria)}"><span class="hrow-time">${fTime(r.t)}</span><span class="hrow-main"><span class="num">${numH(r.n)}</span><span class="hrow-sub">${sub}</span>${al}</span></button>`;
}
function vHistory() {
  const rows = S.rows.slice().sort((a, b) => b.t - a.t);
  let body;
  if (!rows.length) body = `<p class="empty">No calls blocked or silenced yet.</p>`;
  else {
    const groups = [];
    for (const r of rows) { const d = dayNo(r.t); let g = groups[groups.length - 1]; if (!g || g.d !== d) { g = { d, t: r.t, rows: [] }; groups.push(g); } g.rows.push(r); }
    body = '<div class="hist">' + groups.map(g => {
      const seen = {}, nth = {};
      g.rows.slice().reverse().forEach(r => { if (r.n != null) { seen[r.n] = (seen[r.n] || 0) + 1; nth[r.id] = seen[r.n]; } });
      return `<h2 class="day cap">${fDay(g.t)}</h2>` + g.rows.map(r => hrowH(r, { nth: nth[r.id] || 1, allow: allowEntry(r.n) })).join('');
    }).join('') + '</div>';
    if (S.base.truncated) body += `<p class="pnote">Prototype note. The sample data ends here. The brief supplies five numbers, so older calls cannot be listed without inventing more.</p>`;
  }
  const act = rows.length ? `<button class="hdr-a" data-act="askDeleteAll">Delete All</button>` : '';
  return `<div class="scr" data-k="history">${hdrH('History', act)}${body}</div>`;
}

/* ── Number details: a sheet ── */
function sheetH(o) {
  const n = o.num, d = numDetails(n), call = o.callId != null ? S.rows.find(r => r.id === o.callId) : null, al = allowEntry(n);
  const thiscall = call ? `<div class="thiscall"><span class="thiscall-t"><span class="cap">This call</span><span class="body">${dotsH([fDay(call.t) + ', ' + fTime(call.t), call.a === 'B' ? T.blocked : T.silenced])}</span></span><button class="key key-sm" data-act="delCall" aria-label="Delete This Call">Delete</button></div>` : '';
  const when = d.total > 1 ? `<div class="kv"><span>First</span><span>${d.first ? fDateTime(d.first) : ''}</span></div><div class="kv"><span>Last</span><span>${d.last ? fDateTime(d.last) : ''}</span></div>`
    : (call ? '' : `<div class="kv"><span>Last</span><span>${d.last ? fDateTime(d.last) : ''}</span></div>`);
  const facts = `<div class="facts"><p class="lead">${calls(d.total)} stopped</p><div class="facts-split"><span class="tile-line">${mark('B')}${fNum(d.b)} blocked</span><span class="tile-line">${mark('S')}${fNum(d.s)} silenced</span></div>${when}</div>`;
  let act = '';
  if (n != null) act = al
    ? `<div class="keys-block"><span class="cap">On the allow list</span><p class="lead">${esc(allowText(al))}</p><div class="keys" style="--n:1"><button class="key" data-act="unallow">Remove From Allow List</button></div></div>`
    : keysBlock('Let this number ring', ALLOW.map(a => ({ label: a[1], act: 'allow', v: a[0] == null ? 'always' : a[0], aria: a[2] })));
  return `<div class="layer" data-k="sheet"><div class="scrim" data-act="back"></div><div class="plate sheet" role="dialog" aria-modal="true" aria-label="${esc(n == null ? T.no_number : n)}"><span class="sheet-grip" aria-hidden="true"></span><h2 class="sheet-num">${numH(n)}</h2>${thiscall}${facts}${act}</div></div>`;
}
const dialogPlate = () => `<div class="plate dialog" role="alertdialog" aria-modal="true" aria-labelledby="dlgT"><h2 class="dialog-t" id="dlgT">Delete all history?</h2><p class="body">Statistics are counted from the history, so they reset too.</p>${keysH([{ label: 'Cancel', act: 'back' }, { label: 'Delete All', act: 'deleteAll' }])}</div>`;
const dialogDeleteAll = () => `<div class="layer is-center" data-k="dialog"><div class="scrim" data-act="back"></div>${dialogPlate()}</div>`;

/* ── Statistics ── */
function drumsH(n) {
  const s = String(n).padStart(3, '0'), lead = s.length - String(n).length;
  return `<div class="drums" role="img" aria-label="${fNum(n)}">${s.split('').map((c, i) => `<span class="drum${i < lead ? ' is-lead' : ''}" aria-hidden="true">${c}</span>`).join('')}</div>`;
}
function mileH(total, reached, next) {
  const top = next || reached || 10, fill = Math.min(100, total / top * 100);
  const notch = reached && next ? `<span class="mile-notch" style="left:${(reached / next * 100).toFixed(2)}%"></span>` : '';
  return `<div class="mile" role="img" aria-label="${reached ? fNum(reached) + ' reached. ' : ''}${next ? 'Next: ' + fNum(next) : ''}"><div class="mile-bar"><span class="mile-fill" style="width:${fill.toFixed(2)}%"></span>${notch}</div><div class="mile-l"><span>${reached ? fNum(reached) + ' reached' : ''}</span><span>${next ? 'Next: ' + fNum(next) : ''}</span></div></div>`;
}
function dayChartH(daily, sel) {
  const max = Math.max.apply(null, daily.map(d => d.b + d.s)), bricks = max <= 8;
  const cols = daily.map((d, i) => {
    const date = at(i - 6, 12, 0), tot = d.b + d.s;
    let body;
    if (bricks) body = `<span class="dbricks">${'<i class="b"></i>'.repeat(d.b)}${'<i class="s"></i>'.repeat(d.s)}</span>`;
    else {
      const h = Math.round(tot / max * 101), hs = d.s ? Math.max(5, Math.round(d.s / tot * h)) : 0, hb = Math.max(0, h - hs - (d.s ? 1 : 0));
      body = `<span class="dval">${tot ? fNum(tot) : ''}</span><span class="dbar">${d.s ? `<i class="s" style="height:${hs}px"></i>` : ''}${d.b ? `<i class="b" style="height:${hb}px"></i>` : ''}</span>`;
    }
    return `<button class="dcol${i === sel ? ' is-sel' : ''}" data-act="day" data-v="${i}" role="listitem" aria-label="${WDL[date.getDay()]}: ${d.b} blocked, ${d.s} silenced"${i === sel ? ' aria-current="true"' : ''}>${body}<span class="dlab">${WD[date.getDay()]}</span></button>`;
  }).join('');
  return `<div class="days" role="list" aria-label="Day by day" style="--val-h:${bricks ? '0px' : 'calc(18px * var(--ts))'}">${cols}</div>`;
}
function weekdayChartH(wd) {
  const max = Math.max(4, Math.max.apply(null, wd));
  return `<div class="wk" role="list" aria-label="By weekday">${wd.map((v, i) => `<div class="wcol" role="listitem" aria-label="${WDL[i]}: ${v}"><span class="wval">${fNum(v)}</span><span class="wbar">${v ? `<i class="b" style="height:${Math.max(2, Math.round(v / max * 72))}px"></i>` : ''}</span><span class="dlab">${WD[i]}</span></div>`).join('')}</div>`;
}
function hourChartH(hr, sel) {
  const max = Math.max(4, Math.max.apply(null, hr));
  return `<div class="hrs" data-hrs role="list" aria-label="By hour">${hr.map((v, i) => `<span class="hbar${i === sel ? ' is-sel' : ''}" role="listitem" aria-label="${fHour(i)}: ${v}">${v ? `<i class="b" style="height:${Math.max(2, Math.round(v / max * 72))}px"></i>` : ''}</span>`).join('')}</div><div class="hax" aria-hidden="true">${[0, 6, 12, 18].map(h => `<span>${fHour(h)}</span>`).join('')}</div>`;
}
function vStatistics() {
  const st = stats(), has = st.total > 0;
  const share = has ? `<button class="hdr-b" data-act="sys" data-v="share" aria-label="Share">${IC.share}</button>` : '';
  const win = `<section class="win statwin" aria-label="All time"><div class="counter">${drumsH(st.total)}<div class="counter-t"><span class="cap">${st.total === 1 ? 'Call stopped' : 'Calls stopped'}</span><span class="tile-line">${mark('B')}${fNum(st.all.b)} blocked</span><span class="tile-line">${mark('S')}${fNum(st.all.s)} silenced</span></div></div>${mileH(st.total, st.reached, st.next)}${has ? `<p class="saved">About ${fSaved(st.total)}, at 30 seconds a call</p>` : ''}</section>`;
  if (!has) return `<div class="scr" data-k="statistics">${hdrH('Statistics', share)}${win}</div>`;
  const sel = st.daily[S.daySel], selDate = at(S.daySel - 6, 12, 0);
  const change7 = st.pct7 == null ? '' : st.pct7 < 0 ? `${-st.pct7}% fewer than the week before. ` : st.pct7 > 0 ? `${st.pct7}% more than the week before. ` : 'Same as the week before. ';
  const week = `<div class="sec is-first"><div class="sec-h"><h2 class="h2">Last 7 days</h2><span class="lead">${calls(st.weekTotal)}</span></div>${dayChartH(st.daily, S.daySel)}
<p class="body" aria-live="polite">${WD[selDate.getDay()]}: ${sel.b} blocked, ${sel.s} silenced</p>
${st.weekTotal ? `<p class="body dim">${change7}${st.busiest7 != null ? 'Busiest around ' + fHour(st.busiest7) + '.' : ''}</p>` : ''}</div>`;
  const change30 = st.pct30 == null ? '' : st.pct30 < 0 ? `${-st.pct30}% fewer than the 30 days before` : st.pct30 > 0 ? `${st.pct30}% more than the 30 days before` : 'Same as the 30 days before';
  const month = st.older ? `<div class="sec"><div class="sec-h"><h2 class="h2">Last 30 days</h2><span class="lead">${calls(st.d30)}</span></div>${change30 ? `<p class="body dim">${change30}</p>` : ''}</div>` : '';
  const period = `<div class="sec">${keysH([7, 30, 90].map(p => ({ label: p + ' Days', act: 'period', v: p, on: S.period === p })), { radio: 'Period' })}</div>`;
  const byWd = `<div class="sec is-first"><div class="sec-h"><h2 class="h2">By weekday</h2><span class="body dim">${st.busyWd != null ? 'Most on ' + WDL[st.busyWd] : ''}</span></div>${weekdayChartH(st.wd)}</div>`;
  const hourLine = S.hourSel != null ? `${fHour(S.hourSel)}: ${calls(st.hr[S.hourSel])}` : (st.busyHr != null ? 'Most around ' + fHour(st.busyHr) : '');
  const byHr = `<div class="sec is-first"><div class="sec-h"><h2 class="h2">By hour</h2><span class="body dim" aria-live="polite">${hourLine}</span></div>${hourChartH(st.hr, S.hourSel)}</div>`;
  const top = st.top.length ? `<div class="sec is-first"><h2 class="h2">Most frequent</h2><div>${st.top.map(e => {
    const al = allowEntry(e.n);
    return `<button class="frow" data-act="top" data-v="${esc(e.n)}"><span class="frow-t"><span class="num">${numH(e.n)}</span>${al ? `<span class="hrow-allow">${esc(al.until == null ? 'On the allow list' : 'Rings until ' + fUntil(al.until))}</span>` : ''}</span><span class="frow-n">${calls(e.c)}</span></button>`;
  }).join('')}</div></div>` : '';
  return `<div class="scr" data-k="statistics">${hdrH('Statistics', share)}${win}${week}${month}${period}${byWd}${byHr}${top}</div>`;
}

/* ── Settings and Licences ── */
function vSettings() {
  const blocked = S.an === 'blocked';
  const summary = `<div class="sec is-first"><div class="keys-block"><span class="strip-t">Summary notification</span><p class="body dim">A count of stopped calls, each Monday or on the 1st.</p>${blocked
    ? `<div class="strips is-closed">${stripH({ title: 'Notifications', detail: T.notif_blocked, trail: 'out', act: 'sys', v: 'asettings', aria: T.notif_blocked + '. ' + T.open_settings })}</div>`
    : keysH([['off', 'Off'], ['weekly', 'Weekly'], ['monthly', 'Monthly']].map(x => ({ label: x[1], act: 'summary', v: x[0], on: S.summary === x[0] })), { radio: 'Summary notification' })}</div></div>`;
  const links = [
    stripH({ title: 'Privacy Policy', trail: 'out', act: 'sys', v: 'policy' }),
    S.consent ? stripH({ title: 'Privacy Choices', trail: 'chev', act: 'sys', v: 'consent' }) : '',
    stripH({ title: 'Open-Source Licences', trail: 'chev', act: 'go', v: 'licences' }),
    stripH({ title: 'Contact', trail: 'out', act: 'sys', v: 'contact' }),
    stripH({ title: 'Share App', trail: 'out', act: 'sys', v: 'shareapp' }),
    stripH({ title: 'Rate App', trail: 'out', act: 'sys', v: 'rate' }),
  ];
  if (S.later.ads) {
    if (S.ad === 'removed') links.push(stripH({ title: 'Ads removed', tag: true, trail: 'none' }));
    else links.push(stripH({ title: 'Remove Ads', tag: true, detail: 'One-time purchase', trail: '₹000', act: 'sys', v: 'buy', aria: 'Remove Ads. One-time purchase. Price placeholder' }),
      stripH({ title: 'Restore Purchases', tag: true, detail: S.restoreMsg ? 'No purchase found for this Google account.' : '', trail: 'chev', act: 'sys', v: 'restore' }));
  }
  return `<div class="scr" data-k="settings">${hdrH('Settings')}${summary}<div class="strips is-closed">${links.join('')}</div><p class="stamp">v0.1.0 · 2026-10-02</p></div>`;
}
const LIBS = [
  ['Android Jetpack: Activity, Compose, Core, DataStore, Lifecycle, Navigation, Room, WorkManager', 'The Android Open Source Project', 'Apache License 2.0'],
  ['Material Components and Material Icons', 'Google LLC', 'Apache License 2.0'],
  ['Kotlin and kotlinx.coroutines', 'JetBrains s.r.o. and Kotlin Programming Language contributors', 'Apache License 2.0'],
  ['libphonenumber', 'Google LLC', 'Apache License 2.0'],
  ['Hanken Grotesk', 'The Hanken Grotesk Project Authors', 'SIL Open Font License 1.1'],
  ['Noto Sans Devanagari', 'The Noto Project Authors', 'SIL Open Font License 1.1'],
];
function vLicences() {
  return `<div class="scr" data-k="licences">${hdrH('Open-Source Licences')}<div>${LIBS.map(l => `<div class="lic"><b>${esc(l[0])}</b><span>© ${esc(l[1])}</span><span>${esc(l[2])}</span></div>`).join('')}</div>
<div class="strips is-closed">${stripH({ title: 'Read The Apache License 2.0', trail: 'out', act: 'sys', v: 'apache' })}${stripH({ title: 'Read The SIL Open Font License 1.1', trail: 'out', act: 'sys', v: 'ofl' })}</div></div>`;
}

/* ── surfaces that are not ours to draw: labelled stand-ins that only let the flow continue ── */
const STORE = 'https://play.google.com/store/apps/details?id=com.cyanharborstudios.callblock';
function standinH(o) {
  const st = stats();
  const box = (tag, title, body, btns) => `<div class="layer${o.center ? ' is-center' : ''}" data-k="sys-${o.kind}"><div class="scrim" data-act="back"></div><div class="standin" role="dialog" aria-modal="true" aria-label="${esc(title)}"><span class="standin-tag">${esc(tag)} · stand-in, not part of this design</span><h3>${esc(title)}</h3>${body}<div class="standin-btns">${btns.map(b => `<button data-act="${b[1]}"${b[2] != null ? ` data-v="${b[2]}"` : ''}>${esc(b[0])}</button>`).join('')}</div></div></div>`;
  switch (o.kind) {
    case 'role': return box('Android', 'Android’s role prompt', '<p>Android asks, in its own words, whether this app should screen calls. The lever waits at its old stop.</p>', [['User Declines', 'back'], ['User Accepts', 'roleAccept']]);
    case 'notifperm': return box('Android', 'Android’s notification permission prompt', '<p>Shown once, at the moment something that notifies is switched on.</p>', [['Don’t Allow', 'permDeny'], ['Allow', 'permAllow']]);
    case 'asettings': return box('Android', 'Android’s notification settings for this app', '<p>The user leaves the app. What they choose there is read again when they return.</p>', [['Leave Them Off', 'back'], ['Allow Notifications', 'permAllow']]);
    case 'share': return box('Android', 'Android’s share sheet', `<p>The text handed to it:</p><p class="standin-q">${esc(T.app_name)} has stopped ${calls(st.total)} from numbers outside my contacts: ${fNum(st.all.b)} blocked, ${fNum(st.all.s)} silenced.\n${STORE}</p>`, [['Close', 'back']]);
    case 'shareapp': return box('Android', 'Android’s share sheet', `<p>The text handed to it:</p><p class="standin-q">${STORE}</p>`, [['Close', 'back']]);
    case 'consent': return box('Google', 'Google’s consent form', '<p>Shown over Home on first launch where the law requires consent, and again from Privacy Choices.</p>', [['Close', 'back']]);
    case 'policy': return box('Leaves the app', 'The browser opens the privacy policy', '<p class="standin-q">cyanharborstudios.com/call-blocker/privacy/</p>', [['Back To The App', 'back']]);
    case 'contact': return box('Leaves the app', 'The email app opens a new message', '<p class="standin-q">To: contact@cyanharborstudios.com</p>', [['Back To The App', 'back']]);
    case 'rate': return box('Leaves the app', 'Google Play opens the app’s page', '', [['Back To The App', 'back']]);
    case 'apache': return box('Leaves the app', 'The browser opens the licence', '<p class="standin-q">apache.org/licenses/LICENSE-2.0</p>', [['Back To The App', 'back']]);
    case 'ofl': return box('Leaves the app', 'The browser opens the licence', '<p class="standin-q">openfontlicense.org</p>', [['Back To The App', 'back']]);
    case 'buy': return box('Google Play · Later', 'Google Play’s purchase sheet', `<p>Play draws it. The words we supply:</p><p class="standin-q"><b>Remove Ads</b>\nRemoves the ad from every screen. One-time purchase.\n₹000</p>`, [['Close', 'back'], ['Buy', 'bought']]);
    case 'restore': return box('Google Play · Later', 'Google Play checks for an earlier purchase', '', [['Nothing Found', 'restoreNone'], ['Purchase Found', 'bought']]);
  }
  return '';
}
function ncardH(o) {
  const tag = o.act ? 'button' : 'div';
  return `<${tag} class="ncard"${o.act ? ` data-act="ntap" data-v="${o.act}"` : ''}><span class="ncard-h"><span class="ncard-ic">${iconNotif(14, '#FFFFFF')}</span><span>${esc(T.app_name)} · ${esc(o.when)}</span></span><p class="ncard-t">${esc(o.title)}</p>${o.text ? `<p class="ncard-x">${o.text}</p>` : ''}${o.action ? `<span class="ncard-a">${esc(o.action)}</span>` : ''}</${tag}>`;
}
function shadeH(o) {
  const lock = o.kind === 'lock';
  const head = `<div class="shade-h"><span>${lock ? 'Android’s lock screen' : 'Android’s notification shade'}, as a stand-in. Android draws the frame; the icon and the words are ours. All three kinds are silent.</span><button data-act="back">Close</button></div>`;
  if (lock) return `<div class="shade" data-k="shade-lock" role="dialog" aria-label="Lock screen">${head}<p class="shade-clock">${fTime(NOW)}</p>${ncardH({ when: fTime(at(0, 19, 4)), title: 'Call blocked' })}${ncardH({ when: fTime(at(0, 16, 12)), title: 'Call silenced' })}<p class="ncard-note">The number is hidden while the phone is locked, and there is no action.</p></div>`;
  return `<div class="shade" data-k="shade" role="dialog" aria-label="Notifications">${head}
${ncardH({ when: fTime(at(0, 19, 4)), title: 'Call blocked', text: numH(N.a), action: 'Allow For 1 Hour', act: 'history' })}
${ncardH({ when: fTime(at(0, 16, 12)), title: 'Call silenced', text: numH(N.c), action: 'Allow For 1 Hour', act: 'history' })}
${ncardH({ when: 'Thu', title: 'Call blocked', text: T.no_number, act: 'history' })}
<p class="ncard-note">Tapping one opens History. The action is new. It lets that number ring for an hour without opening the app.</p>
${ncardH({ when: '21' + NB + 'Sep', title: '100 calls stopped', act: 'statistics' })}
${ncardH({ when: 'Mon', title: 'Last week', text: '31 blocked, 3 silenced', act: 'statistics' })}
${ncardH({ when: '1' + NB + 'Oct', title: 'Last month', text: '114 blocked, 6 silenced', act: 'statistics' })}
<p class="ncard-note">The milestone and the summaries open Statistics.</p></div>`;
}
function overlayH() {
  const o = S.overlay;
  if (!o) return '';
  if (o.type === 'sheet') return sheetH(o);
  if (o.type === 'dialog') return dialogDeleteAll();
  if (o.type === 'sys') return standinH(o);
  if (o.type === 'shade') return shadeH(o);
  return '';
}

/* ── the frame: status bar inset, the screen, the slot ── */
const slotPart = loaded => `<div class="slot" data-k="slot"${loaded ? ' role="complementary" aria-label="Advertisement"' : ' aria-hidden="true"'}><div class="slot-sill">${loaded ? `<span>${esc(L('ad'))}</span>` : ''}</div><div class="slot-ad">${loaded ? '<div class="testad"><span>Test ad</span><b>Open</b></div>' : ''}</div></div>`;
const slotH = () => S.ad === 'removed' ? '<div class="gest" data-k="gest"></div>' : slotPart(S.ad === 'loaded');
const fsOn = () => document.fullscreenElement || document.webkitFullscreenElement || null;
const fsCan = () => !!(document.fullscreenEnabled || document.webkitFullscreenEnabled);   // an iPhone has neither
const canFullScreen = () => !S.noFS && !S.shot && window.innerWidth < 600 && fsCan() && !fsOn();
const SCREENS = { home: vHome, options: vOptions, history: vHistory, statistics: vStatistics, settings: vSettings, licences: vLicences };
function appH(M) {
  const cls = ['sw', S.theme, S.motion === 'reduced' ? 'rm' : '', hindi() ? 'hi' : '', S.still ? 'still' : ''].filter(Boolean).join(' ');
  return `<div id="phone" class="${cls}" style="--ts:${S.ts};--win-h:${M.winH}px;--bay-h:${M.bayH}px;--time-w:${M.timeW}px"${hindi() ? ' lang="hi"' : ' lang="en"'}>
<div class="sb"><button class="sb-proto" data-act="panel" aria-label="Prototype controls"><i aria-hidden="true"></i>Prototype</button>${hindi() ? '<span class="sb-later">Later: Hindi</span>' : S.ad === 'removed' ? '<span class="sb-later">Later: ads removed</span>' : ''}${canFullScreen() ? '<button class="sb-fs" data-act="fs">Full Screen</button>' : `<span aria-hidden="true">${['Fri', 2 + NB + 'Oct'].join(' ')}, ${fTime(NOW)}</span>`}</div>
<div class="view" id="view">${SCREENS[S.screen]()}</div>${slotH()}<span class="gest-pill" aria-hidden="true"></span>${overlayH()}</div>`;
}
