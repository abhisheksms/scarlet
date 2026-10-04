/* ───────────── start ───────────── */

function fit() {
  const stage = $('#stage');
  // on the bench the frame shrinks to the window's height. On a phone the frame is real size where it fits and shrunk where it
  // does not; without the frame the app is the window.
  K = window.innerWidth >= 600 ? Math.min(1, Math.max(0.5, (stage.clientHeight - 40) / 800))
    : S.frame && !S.shot ? Math.min(1, window.innerWidth / 360, window.innerHeight / 800) : 1;
  stage.style.setProperty('--k', K);
}

function wirePanel() {
  const panel = $('#panel');
  panel.addEventListener('click', e => {
    const t = e.target.closest('button');
    if (!t) return;
    if (t.dataset.tab) { S.tab = t.dataset.tab; drawPanel(true); return; }
    if (t.dataset.sw) { setSw(t.dataset.sw, t.dataset.v); return; }
    if (t.dataset.preset) { applyPreset(t.dataset.preset); if (!window.matchMedia('(min-width:1020px)').matches) closePanel(); return; }
    if (t.id === 'panelClose') { closePanel(); return; }
    if (t.hasAttribute('data-selftest')) { runSelfTestUI(); return; }
    if (t.hasAttribute('data-fs')) {
      fullScreen(() => { const msg = $('#fsMsg'); if (msg) msg.textContent = 'This viewer does not allow full screen. Open the HTML file itself in the browser to use it.'; });
    }
  });
}

const wantsSelfTest = () => /^#selftest\b/.test(location.hash);

function boot() {
  // start in the viewer's theme and motion setting
  const rootTheme = document.documentElement.getAttribute('data-theme');
  S.theme = rootTheme === 'dark' || rootTheme === 'light' ? rootTheme : (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) S.motion = 'reduced';
  resetApp('heavy');
  wire(); wirePanel(); fit();
  if (wantsSelfTest() || !applyHash()) render({ still: true });
  const relayout = () => { fit(); M.key = ''; render({ still: true }); };
  window.addEventListener('resize', relayout);
  window.addEventListener('hashchange', () => { if (wantsSelfTest()) runSelfTestUI(); else applyHash(); });
  const onFullScreen = () => { if (fsOn()) closePanel(); relayout(); };
  document.addEventListener('fullscreenchange', onFullScreen);
  document.addEventListener('webkitfullscreenchange', onFullScreen);
  loadFonts().then(relayout);
  if (document.fonts && document.fonts.addEventListener) document.fonts.addEventListener('loadingdone', relayout);
  window.scarletPrototype = { presets: PRESETS.map(p => p[0]), apply: applyPreset, link: applyLink, state: S, metrics: M, check: checkNow, selfTest, dump: link => { applyLink(link); return layoutDump(); } };
  if (wantsSelfTest()) runSelfTestUI();
}
boot();
