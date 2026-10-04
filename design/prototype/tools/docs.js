// Writes SPEC.md and HANDBACK.md from the prototype's own panel, so the Markdown says what the prototype says.
//
//   node docs.js      run after a build; the text itself lives in src/60-panel.js
//
// SPEC.md is the Values, TalkBack and Components tabs. HANDBACK.md is the Notes and Words tabs.
const fs = require('fs');
const path = require('path');
const { launch, open } = require('./lib');

// runs in the page: one tab of the panel as Markdown
const TAB_TO_MD = `(tab, depth) => {
  document.querySelector('#tabs [data-tab="' + tab + '"]').click();
  const root = document.querySelector('#panelBody .pb');
  const clean = s => s.replace(/\\s+/g, ' ');
  const inline = node => {
    let out = '';
    for (const n of node.childNodes) {
      if (n.nodeType === 3) { out += clean(n.nodeValue); continue; }
      if (n.nodeType !== 1) continue;
      const t = n.tagName.toLowerCase();
      if (t === 'b' || t === 'strong') out += '**' + inline(n).trim() + '**';
      else if (t === 'code') out += '\`' + n.textContent + '\`';
      else if (t === 'i' && n.parentElement.classList.contains('swatch')) continue;   // the colour chip beside a hex value
      else if (t === 'br') out += ' ';
      else if (t === 'small') out += ' (' + inline(n).trim() + ')';
      else out += inline(n);
    }
    return out;
  };
  const cell = td => inline(td).trim().replace(/\\|/g, '\\\\|') || ' ';
  const h = n => '#'.repeat(n + depth);
  const lines = [];
  const block = el => {
    const t = el.tagName.toLowerCase();
    if (t === 'h2') lines.push(h(1) + ' ' + inline(el).trim(), '');
    else if (t === 'h3') lines.push(h(2) + ' ' + inline(el).trim(), '');
    else if (t === 'p') { const s = inline(el).trim(); if (s) lines.push(s, ''); }
    else if (t === 'ul' || t === 'ol') { let i = 0; for (const li of el.children) lines.push((t === 'ol' ? ++i + '. ' : '- ') + inline(li).trim()); lines.push(''); }
    else if (el.classList.contains('tw')) {
      const rows = [...el.querySelectorAll('tr')].map(tr => [...tr.children].map(cell));
      lines.push('| ' + rows[0].join(' | ') + ' |', '|' + rows[0].map(() => ' --- |').join(''));
      for (const r of rows.slice(1)) lines.push('| ' + r.join(' | ') + ' |');
      lines.push('');
    }
    else if (el.classList.contains('inv')) {
      for (const item of el.children) {
        lines.push(h(1) + ' ' + inline(item.querySelector('h3')).trim(), '', inline(item.querySelector('p')).trim(), '');
        const caps = [...item.querySelectorAll('figcaption')].map(c => c.textContent.trim());
        if (caps.length) lines.push('Drawn in the Components tab: ' + caps.join(', ') + '.', '');
      }
    }
    else if (el.classList.contains('icons')) lines.push('The drawings are in the Values tab: ' + [...el.querySelectorAll('figcaption')].map(c => c.textContent.trim()).join(', ') + '.', '');
    else for (const c of el.children) block(c);
  };
  for (const c of root.children) block(c);
  return lines.join('\\n');
}`;

(async () => {
  const { browser } = await launch();
  const { page, errors } = await open(browser, { width: 1280, height: 900, hash: 'home-block.light.100.24h' });
  const md = (tab, depth) => page.evaluate(`(${TAB_TO_MD})(${JSON.stringify(tab)}, ${depth})`);
  const note = tabs => `<!-- Written by tools/docs.js from the prototype's ${tabs} tabs. Change the text in src/60-panel.js, run build.py, then run docs.js again. -->`;
  const spec = [
    '# Scarlet design spec: Switchboard', '',
    note('Values, TalkBack and Components'), '',
    'Every final value of the Stage 3 prototype, `scarlet-prototype.html`. Where this page and the prototype differ, the prototype is right and this page is stale.', '',
    '## Values', '', await md('values', 2),
    '## TalkBack order', '', await md('talkback', 2),
    '## Components', '', await md('components', 2),
  ].join('\n');
  const handback = [
    '# Scarlet Stage 3: what comes back with the prototype', '',
    note('Notes and Words'), '',
    'The brief asks for four things with every delivery: the decisions it did not dictate, every wording change with old beside new, anything in it that is wrong for the user, and anything that could not be opened or done.', '',
    await md('notes', 1),
    '## Words', '', await md('words', 2),
  ].join('\n');
  await browser.close();
  const tidy = s => s.replace(/\n{3,}/g, '\n\n').replace(/[ \t]+\n/g, '\n').trim() + '\n';
  const dir = path.resolve(__dirname, '..');
  fs.writeFileSync(path.join(dir, 'SPEC.md'), tidy(spec));
  fs.writeFileSync(path.join(dir, 'HANDBACK.md'), tidy(handback));
  for (const e of errors) console.log('  ' + e);
  console.log(`SPEC.md ${tidy(spec).split('\n').length} lines, HANDBACK.md ${tidy(handback).split('\n').length} lines`);
  process.exit(errors.length ? 1 : 0);
})().catch(e => { console.error(e); process.exit(2); });
