/* ───────────── words and sample data ───────────── */

// The prototype's clock is fixed, so every frame can be reproduced: Friday 2 October 2026, 19:30.
const NOW = new Date(2026, 9, 2, 19, 30);
const at = (dayOffset, h, m) => new Date(2026, 9, 2 + dayOffset, h, m);
const dt = a => new Date(a[0], a[1], a[2], a[3], a[4]);

// The five numbers of §8. Nothing else is ever shown.
const N = {
  a: '+91 140 123 4567',
  b: '+91 80 4567 8901',
  c: '+91 22 1234 5678',
  d: '+91 98765 43210',
  e: '+1 415-555-0137',
};
const keyOf = n => { for (const k in N) if (N[k] === n) return k; return n == null ? 'none' : null; };

// Final English strings. Typographic apostrophes throughout.
const T = {
  app_name: 'Call Blocker',
  off: 'Off', silence: 'Silence', block: 'Block',
  off_d: 'Every call rings.',
  silence_d: 'Callers outside your contacts don’t ring. You can still answer.',
  block_d: 'Callers outside your contacts are rejected.',
  silence_i: 'International callers outside your contacts don’t ring.',
  block_i: 'International callers outside your contacts are rejected.',
  paused: 'Paused until {t}. Every call rings.',
  tomorrow: 'tomorrow, {t}',
  resume: 'Resume',
  role1: 'Nothing is being filtered.',
  role2: 'Android isn’t sending calls to this app.',
  role_btn: 'Set As Screening App',
  cannot: 'This device can’t screen calls.',
  privacy: 'No account. Your contacts are never read. Nothing about a call leaves this phone.',
  history: 'History', statistics: 'Statistics', options: 'Options', notifications: 'Notifications', settings: 'Settings', back: 'Back',
  call_today: 'call today', calls_today: 'calls today', nothing_today: 'Nothing today',
  nothing_yet: 'Nothing yet', nothing_7: 'Nothing in 7 days',
  n_blocked: '{n} blocked', n_silenced: '{n} silenced', in7: 'in 7 days',
  ring_for: 'Let every call ring for',
  k15: '15 Min', k60: '1 Hr', k240: '4 Hr', k1440: '24 Hr',
  no_exc: 'No exceptions', repeat_ring: 'Repeat callers ring', one_allowed: '1 number allowed', n_allowed: '{n} numbers allowed',
  notif_on: 'On, for each stopped call', notif_off: 'Off', notif_blocked: 'Switched off in Android’s settings',
  open_settings: 'Open Settings',
  ad: 'Advertisement',
  blocked: 'Blocked', silenced: 'Silenced', no_number: 'No number',
};

// Draft Hindi for the one Later frame (Home). Not reviewed by a native reader.
const HI = {
  off: 'बंद', silence: 'साइलेंट', block: 'ब्लॉक',
  off_d: 'हर कॉल की घंटी बजती है।',
  silence_d: 'कॉन्टैक्ट से बाहर के नंबरों पर घंटी नहीं बजती। आप फिर भी उठा सकते हैं।',
  block_d: 'कॉन्टैक्ट से बाहर के नंबरों की कॉल काट दी जाती है।',
  silence_i: 'कॉन्टैक्ट से बाहर के विदेशी नंबरों पर घंटी नहीं बजती।',
  block_i: 'कॉन्टैक्ट से बाहर के विदेशी नंबरों की कॉल काट दी जाती है।',
  paused: '{t} तक रुका है। हर कॉल की घंटी बजेगी।',
  tomorrow: 'कल {t}',
  resume: 'फिर चालू करें',
  role1: 'कोई कॉल नहीं रोकी जा रही।',
  role2: 'Android इस ऐप को कॉल नहीं भेज रहा।',
  role_btn: 'स्क्रीनिंग ऐप बनाएँ',
  cannot: 'यह फ़ोन कॉल स्क्रीन नहीं कर सकता।',
  privacy: 'कोई खाता नहीं। आपके कॉन्टैक्ट कभी नहीं पढ़े जाते। कॉल की कोई जानकारी इस फ़ोन से बाहर नहीं जाती।',
  history: 'इतिहास', statistics: 'आँकड़े', options: 'विकल्प', notifications: 'सूचनाएँ', settings: 'सेटिंग',
  call_today: 'कॉल आज', calls_today: 'कॉल आज', nothing_today: 'आज कुछ नहीं',
  nothing_yet: 'अभी कुछ नहीं', nothing_7: '7 दिनों में कुछ नहीं',
  n_blocked: '{n} ब्लॉक', n_silenced: '{n} साइलेंट', in7: '7 दिनों में',
  ring_for: 'इतनी देर हर कॉल बजने दें',
  k15: '15 मिनट', k60: '1 घंटा', k240: '4 घंटे', k1440: '24 घंटे',
  no_exc: 'कोई छूट नहीं', repeat_ring: 'दोबारा कॉल करने वाले बजते हैं', one_allowed: '1 नंबर को छूट', n_allowed: '{n} नंबरों को छूट',
  notif_on: 'चालू, हर रोकी गई कॉल पर', notif_off: 'बंद', notif_blocked: 'Android की सेटिंग में बंद है',
  ad: 'विज्ञापन',
};

const MON = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
const WD = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
const WDL = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
const LADDER = [10, 25, 50, 100, 250, 500, 1000, 2500, 5000, 10000, 25000, 50000, 100000];

const HR = o => { const a = new Array(24).fill(0); for (const k in o) a[+k] = o[k]; return a; };

// Rows: [day offset, hour, minute, number key or null, B or S]
const ROWS_HEAVY = [
  // Today, exactly as §8 gives it
  [0, 19, 4, 'a', 'B'], [0, 17, 41, 'b', 'B'], [0, 16, 12, 'c', 'S'], [0, 13, 30, 'a', 'B'], [0, 11, 26, 'd', 'B'], [0, 10, 2, 'e', 'B'],
  // Yesterday and the day before: derived, drawn only from the same five numbers and one caller with no number
  [-1, 18, 22, 'a', 'B'], [-1, 15, 47, null, 'B'], [-1, 11, 9, 'b', 'B'], [-1, 9, 38, 'c', 'B'],
  [-2, 17, 15, 'a', 'B'], [-2, 16, 40, 'd', 'B'], [-2, 12, 3, 'b', 'B'], [-2, 11, 31, 'a', 'B'], [-2, 10, 20, 'c', 'S'],
];
const ROWS_THREE = [[0, 17, 41, 'b', 'B'], [0, 16, 12, 'c', 'S'], [0, 11, 26, 'd', 'B']];

/* Targets are the figures the screens must show. §8 supplies the heavy user's; the rest are derived to agree with them.
   wd is Sunday to Saturday. daily is the last seven days, oldest first, as [blocked, silenced]. */
const DATA = {
  new: { rows: [], t: null },
  three: { rows: ROWS_THREE, t: null },
  heavy: {
    rows: ROWS_HEAVY, truncated: true,
    t: {
      all: [148, 8],
      daily: [[3, 0], [1, 0], [6, 1], [8, 0], [4, 1], [4, 0], [5, 1]],
      d30: 121, pct7: -12, prev7: 39, pct30: 9, prev30: 111,
      wd: { 7: [1, 7, 8, 5, 4, 6, 3], 30: [6, 22, 25, 20, 19, 18, 11], 90: [8, 28, 32, 26, 25, 23, 14] },
      hr: {
        7: HR({ 8: 1, 9: 2, 10: 4, 11: 6, 12: 3, 13: 2, 14: 2, 15: 2, 16: 3, 17: 4, 18: 2, 19: 2, 20: 1 }),
        30: HR({ 7: 1, 8: 2, 9: 7, 10: 11, 11: 16, 12: 10, 13: 8, 14: 7, 15: 9, 16: 11, 17: 14, 18: 10, 19: 7, 20: 5, 21: 2, 22: 1 }),
        90: HR({ 7: 1, 8: 3, 9: 9, 10: 14, 11: 21, 12: 13, 13: 10, 14: 9, 15: 12, 16: 14, 17: 18, 18: 13, 19: 9, 20: 6, 21: 3, 22: 1 }),
      },
      top: { 7: { a: 5, b: 3, c: 3, d: 2, e: 1 }, 30: { a: 9, b: 6, c: 4, d: 3, e: 2 }, 90: { a: 11, b: 7, c: 5, d: 3, e: 2 } },
      num: {
        a: { b: 11, s: 0, first: [2026, 7, 26, 10, 14] }, b: { b: 7, s: 0, first: [2026, 7, 29, 16, 5] },
        c: { b: 3, s: 2, first: [2026, 8, 5, 12, 40] }, d: { b: 3, s: 0, first: [2026, 8, 14, 11, 52] },
        e: { b: 2, s: 0, first: [2026, 8, 21, 9, 17] }, none: { b: 1, s: 0, first: null },
      },
    },
  },
  thousands: {
    rows: ROWS_HEAVY, truncated: true,
    t: {
      all: [3180, 232],
      daily: [[20, 1], [9, 0], [35, 3], [42, 2], [29, 2], [25, 1], [33, 2]],
      d30: 846, pct7: 6, prev7: 192, pct30: -4, prev30: 881,
      wd: { 7: [9, 38, 44, 31, 26, 35, 21], 30: [41, 148, 166, 139, 131, 127, 94], 90: [122, 437, 489, 411, 388, 376, 274] },
      hr: {
        7: HR({ 8: 4, 9: 12, 10: 19, 11: 27, 12: 17, 13: 14, 14: 12, 15: 15, 16: 19, 17: 24, 18: 17, 19: 12, 20: 8, 21: 3, 22: 1 }),
        30: HR({ 7: 6, 8: 19, 9: 52, 10: 79, 11: 108, 12: 71, 13: 58, 14: 52, 15: 63, 16: 78, 17: 96, 18: 68, 19: 47, 20: 31, 21: 13, 22: 5 }),
        90: HR({ 7: 18, 8: 56, 9: 153, 10: 233, 11: 319, 12: 210, 13: 171, 14: 153, 15: 186, 16: 230, 17: 283, 18: 201, 19: 139, 20: 91, 21: 38, 22: 16 }),
      },
      top: { 7: { a: 9, b: 6, c: 5, d: 3, e: 2 }, 30: { a: 38, b: 22, c: 15, d: 9, e: 6 }, 90: { a: 97, b: 61, c: 40, d: 22, e: 13 } },
      num: {
        a: { b: 141, s: 0, first: [2025, 1, 14, 10, 14] }, b: { b: 88, s: 0, first: [2025, 2, 2, 16, 5] },
        c: { b: 49, s: 8, first: [2025, 2, 9, 12, 40] }, d: { b: 30, s: 0, first: [2025, 3, 27, 11, 52] },
        e: { b: 17, s: 0, first: [2025, 5, 18, 9, 17] }, none: { b: 64, s: 0, first: [2025, 1, 21, 15, 47] },
      },
    },
  },
};

const mkRows = spec => spec.map((r, i) => ({ id: i + 1, t: at(r[0], r[1], r[2]), n: r[3] ? N[r[3]] : null, a: r[4] }));
const dayIndex = d => 6 - Math.round((new Date(NOW.getFullYear(), NOW.getMonth(), NOW.getDate()) - new Date(d.getFullYear(), d.getMonth(), d.getDate())) / 864e5);

// base = target minus what the listed rows already account for, so deleting a row moves every figure that counts it
function mkBase(kind) {
  const D = DATA[kind], rows = mkRows(D.rows);
  const z = { all: [0, 0], daily: [0, 0, 0, 0, 0, 0, 0].map(() => [0, 0]), d30: 0, prev7: 0, prev30: 0, pct7: null, pct30: null, week0: 0, d300: 0,
    wd: { 7: new Array(7).fill(0), 30: new Array(7).fill(0), 90: new Array(7).fill(0) },
    hr: { 7: new Array(24).fill(0), 30: new Array(24).fill(0), 90: new Array(24).fill(0) },
    top: { 7: {}, 30: {}, 90: {} }, num: {}, truncated: !!D.truncated };
  if (!D.t) return { rows, base: z };
  const t = D.t, b = z;
  b.all = t.all.slice(); b.daily = t.daily.map(x => x.slice()); b.d30 = t.d30;
  b.prev7 = t.prev7; b.prev30 = t.prev30; b.pct7 = t.pct7; b.pct30 = t.pct30;
  b.week0 = t.daily.reduce((n, x) => n + x[0] + x[1], 0); b.d300 = t.d30;
  for (const p of [7, 30, 90]) { b.wd[p] = t.wd[p].slice(); b.hr[p] = t.hr[p].slice(); b.top[p] = Object.assign({}, t.top[p]); }
  for (const k in t.num) b.num[k] = { b: t.num[k].b, s: t.num[k].s, first: t.num[k].first ? dt(t.num[k].first) : null };
  for (const r of rows) {
    const i = r.a === 'B' ? 0 : 1, k = keyOf(r.n);
    b.all[i]--; b.daily[dayIndex(r.t)][i]--; b.d30--;
    for (const p of [7, 30, 90]) { b.wd[p][r.t.getDay()]--; b.hr[p][r.t.getHours()]--; if (k !== 'none') b.top[p][k]--; }
    b.num[k][r.a === 'B' ? 'b' : 's']--;
  }
  return { rows, base: b };
}
