// Every piece of text the app shows, and the copy rules of the brief's §7 that a script can check.
//
//   node words.js      prints what it found; writes shots/words.txt, shots/labels.txt and shots/spoken.txt for reading
//
// Checked: none of the words §7 rules out; every word capitalised on buttons and actions; no exclamation marks.
// Not checked, because only a reader can: tone, urgency, narration, filler.
const fs = require('fs');
const path = require('path');
const { launch, open } = require('./lib');

(async () => {
  const { browser } = await launch();
  const { page, errors } = await open(browser, { width: 1280, height: 900 });
  const got = await page.evaluate(() => {
    const P = window.scarletPrototype, text = new Set(), labels = new Set(), spoken = new Set(), hindi = new Set();
    for (const id of P.presets) for (const clock of ['24h', '12h']) {
      P.link([id, 'light', '100', clock].join('.'));
      const phone = document.querySelector('#phone'), hi = phone.classList.contains('hi');
      const walk = document.createTreeWalker(phone, NodeFilter.SHOW_TEXT);
      while (walk.nextNode()) {
        const s = walk.currentNode.nodeValue.replace(/\s+/g, ' ').trim();
        if (!s || walk.currentNode.parentElement.closest('.sb')) continue;   // the status bar is the prototype's own
        (hi ? hindi : text).add(s);
      }
      // buttons and actions: keys, header actions, the notification's action. A stand-in's buttons are Android's or Google's.
      if (!hi) for (const b of phone.querySelectorAll('.key,.key-main,.hdr-a,.ncard-a')) if (!b.closest('.standin')) labels.add(b.textContent.replace(/\s+/g, ' ').trim());
      for (const b of phone.querySelectorAll('[aria-label]')) spoken.add(b.getAttribute('aria-label').replace(/\s+/g, ' ').trim());
    }
    return { text: [...text].sort(), labels: [...labels].sort(), spoken: [...spoken].sort(), hindi: [...hindi].sort() };
  });
  await browser.close();

  const problems = errors.slice();
  const ruledOut = /\b(protect\w*|safe\w*|secur\w*|threat\w*|spam\w*|scam\w*)\b/i;
  for (const s of got.text.concat(got.spoken)) {
    const m = s.match(ruledOut);
    if (m) problems.push(`“${m[0]}” in: ${s}`);
    if (/\bAI\b/.test(s)) problems.push(`“AI” in: ${s}`);
    if (s.includes('!')) problems.push(`an exclamation mark in: ${s}`);
  }
  for (const l of got.labels) if (/(^|[\s-])[a-z]/.test(l)) problems.push(`a button or action with a word in lower case: ${l}`);

  const out = path.resolve(__dirname, 'shots');
  fs.mkdirSync(out, { recursive: true });
  fs.writeFileSync(path.join(out, 'words.txt'), got.text.join('\n') + '\n\n— Hindi (Later) —\n' + got.hindi.join('\n') + '\n');
  fs.writeFileSync(path.join(out, 'labels.txt'), got.labels.join('\n') + '\n');
  fs.writeFileSync(path.join(out, 'spoken.txt'), got.spoken.join('\n') + '\n');
  console.log(`${got.text.length} pieces of text, ${got.labels.length} button and action labels, ${got.spoken.length} spoken labels, ${got.hindi.length} in Hindi. Written to tools/shots/.`);
  for (const p of problems) console.log('  ' + p);
  console.log(problems.length ? `PROBLEMS: ${problems.length}` : 'NO PROBLEMS');
  process.exit(problems.length ? 1 : 0);
})().catch(e => { console.error(e); process.exit(2); });
