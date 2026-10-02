---
name: play-launch
description: Launching a new Cyan Harbor app on Google Play from the studio's existing organization account - what is already set up once for the studio (org account, D-U-N-S, domain, site hosting, AdMob publisher, merchant profile), the per-app launch plan in dependency order (store assets, Play Console app and forms, in-app product, internal testing, licence-test purchase, production, after-submission), the policy posture that makes an app clean for review, and copy-paste templates for the store listing, Data safety answers and CSV, and the privacy policy page. Use it when starting a new app's launch, filling any Play Console or AdMob form, writing listing copy, adding a privacy page to the studio site, or when the founder asks "what's left to launch", "how do I put this on Play" or "what do I answer here".
---

# Play launch kit

The first app went through all of this between August and October 2026. The
studio-level work (Epic A) is done once and stays done; a new app starts at
Epic B. The sequence, with who does what and how long each step takes, is in
`references/launch-plan-template.md`. What already exists at studio level is in
`references/studio-account.md`. The ready-to-paste files are in `templates/`,
filled in for Cricket Auction Simulator so the shape and tone are concrete.

## The order for a new app

1. **Identity, in the repo:** Play listing title by category keyword, launcher
   label, `android.package` = `com.cyanharborstudios.<app>` (permanent after the
   first upload), privacy URL `https://cyanharborstudios.com/<app-slug>/privacy/`.
   The `founder-taste` skill §9 has the naming rule.
2. **Store assets:** 512×512 icon with alpha, 1024×500 feature graphic with no
   alpha, 2–8 phone screenshots within 2:1 (capture on the emulator at
   1080×1920; a 20:9 phone capture is rejected). The `android-release` skill has
   the capture commands.
3. **Privacy page on the studio site**, from `templates/privacy-policy.html`,
   committed to the site and deployed by the founder (Cloudflare Worker, see
   `studio-account.md`). It must match the Data safety answers. Link it from the
   app's Settings.
4. **Play Console** (founder's clicks; give numbered steps and read screenshots):
   create the app → App content (privacy URL, app access, ads, advertising ID,
   content rating, target audience, Data safety by importing the CSV) → store
   listing with website `https://cyanharborstudios.com` → internal testing:
   upload the AAB, accept Play App Signing, add the founder's Gmail as tester →
   create the in-app product(s) with the exact ids the code queries → licence
   testing: add the Gmail, make one free test purchase on the Play-installed copy
   → promote the same release to production. An organization account skips the
   12-testers / 14-days rule.
5. **AdMob:** add the app, create the ad unit(s), wire the ids into the one file
   that holds them, and pin them with a launch-gate test. AdMob only finds the app
   for linking once it is live in production; `app-ads.txt` at the studio domain
   already carries the publisher line.
6. **After submission:** Google's review takes a few days to a week for a new app.
   Then link the app in AdMob, publish the GDPR message if a build with the
   Privacy Choices entry point is live, fix any price rounding Play applied, and
   check the test order shows "Processed" in Order management.

Run the `play-policy-guard` checker with `--apk --aab --online --gradle` before
the first upload and every one after.

## Policy posture (keep every app this clean)

- Not gambling: no real-money wagering, no chance-based rewards, no loot boxes.
- No pay-to-win: a purchase grants zero gameplay or competitive advantage.
- Calm ads: rewarded-only, user-initiated, never during play, never triggered by a
  loss or scarcity.
- Fictional identities only, or documented rights.
- Listing claims literally true.

## Templates

| File | Use |
|---|---|
| `templates/STORE_LISTING.md` | Title (≤30), short (≤80), full (≤4000, one line per paragraph), category, tags, IAP blurb, content-rating answers, graphics spec. Keep it in the app at `docs/launch/STORE_LISTING.md`; the policy checker reads it. |
| `templates/DATA_SAFETY.md` | The Data safety answers for an app whose only collection is the AdMob SDK, and why. Keep at `docs/launch/DATA_SAFETY.md`. |
| `templates/data-safety-play-console.csv` | The same answers in Play's import/export format. Import it on the Data safety page; edit both files together. It is app-agnostic for an AdMob-only app. |
| `templates/PRIVACY_POLICY.md` | What the live privacy page must cover, and the rule that the site copy is the only source. |
| `templates/privacy-policy.html` | The live page itself, for the studio site at `site/<app-slug>/privacy/index.html`. Replace the app name, slug, effective date and the data section if the SDKs differ. |

Each app also keeps `docs/launch/LAUNCH_PLAN.md` (its copy of the template, with
status per row) and `docs/launch/APP_RECORD.md` (from the policy guard's
template). Money matters (bank account, tax, GST, cross-border KYC) are
studio-level and tracked once, in the first app's launch plan, Epic D.

## Founder-only

Anything inside Play Console, AdMob, Cloudflare, Google payments or banking;
credentials; agreements; permanent deletions. Verify outcomes from outside
where possible: fetch the live privacy page, `app-ads.txt`, the public listing.
