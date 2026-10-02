# Privacy policy — where it lives and what it must cover

> Template, filled in for Cricket Auction Simulator (the first Cyan Harbor app).
> Copy it to the new app's `docs/launch/` and replace every app-specific line.

The live policy is **`site/cricket-auction/privacy/index.html`**, served at
https://cyanharborstudios.com/cricket-auction/privacy/. Edit that file. This page
used to hold a draft that drifted from the live one: the draft said the app
collects no location, while Data safety declares approximate location. Keep one
source.

Google Play (User Data policy, answer 10144311) and AdMob (answer 2753860)
require the live page to:

- name the developer and give a privacy contact (contact@cyanharborstudios.com)
- list every data type the app or its SDKs collect, and who receives it. Today
  that's the Google Mobile Ads SDK: IP address (approximate location), advertising
  ID and app set ID, ad interactions and diagnostics, shared with Google for ads,
  measurement and fraud prevention
- **match the Data safety form** (`DATA_SAFETY.md`). Change both together
- describe secure handling (encrypted in transit; no servers of ours)
- give the retention and deletion policy (nothing held by us; on-device data goes
  on uninstall; the ad ID can be reset; Google's retention applies to ad data)
- be public HTML, not a PDF, and not geofenced

The app links to it from Settings (`src/links.ts`), as Play requires. Updating
the site: zip `site/` and upload it as a new deployment of the Cloudflare Worker
`bold-sunset-1149` (see LAUNCH_PLAN A3). Last rewrite: 2026-09-24, after the
pre-launch policy audit; live since the founder redeployed on 2026-09-25. The
policy checker's `--online` step compares the live page with this repo's copy.
