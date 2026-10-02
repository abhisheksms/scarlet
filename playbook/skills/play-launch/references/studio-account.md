# What the studio already has (as of 2 October 2026)

Done once, for every app. Personal and legal details (the registered address,
the proprietor's name as on PAN, payments-profile numbers, D-U-N-S) are
deliberately not in this shared file: they are in the cyan repo's
`docs/launch/LAUNCH_PLAN.md` under "Identity strings", and must be used verbatim
wherever Google or D&B ask, because they match on strings.

## Google Play

- **Developer account:** Cyan Harbor Studios, an **organization** account,
  verified 2026-09-22 against the Udyam (MSME) certificate and a D-U-N-S number
  issued 2026-09-21. Organization accounts skip the "12 testers for 14 days"
  rule for new apps.
- **Developer website:** `https://cyanharborstudios.com`. AdMob reads
  `app-ads.txt` there, so every app's listing uses this site.
- **EU trader status:** trader. The contact details become public in the EU.
- **Payments / merchant profile:** exists in the studio's name (INR). It is what
  lets Play sell one-time products. The 15% service-fee tier was enrolled
  2026-09-25 through an account group. **Cross-border sales** stay blocked until
  the BillDesk KYC (RBI PA-CB) is done; until then only buyers in India can pay,
  ads are unaffected. A bank account for payouts is still to be added.
- **Android developer verification:** apps created in Play Console register
  automatically; the first app's registration was confirmed 2026-09-25.
- **Package prefix:** `com.cyanharborstudios.<app>` (founder's decision,
  2026-08-04). Permanent after an app's first upload.
- **First app:** Cricket Auction Simulator, `com.cyanharborstudios.cricketauction`,
  submitted 2026-09-25, live 2026-10-02 (about seven days in review, approved as
  submitted).

## AdMob

- **Account approved** 2026-09-26, publisher id `pub-8927071309083589` (public,
  not a secret). Attached to the studio's organization payments profile.
- **`app-ads.txt`** live at `https://cyanharborstudios.com/app-ads.txt`:
  `google.com, pub-8927071309083589, DIRECT, f08c47fec0942fa0`. Same line for
  every app; nothing to add per app.
- **Test device:** the founder's phone is registered (AdMob → Settings → Test
  devices). Registered devices get test ads from live unit ids; unregistered
  taps are invalid traffic.
- Per app: add the app in AdMob, create its ad unit(s), set max ad content rating
  PG and the blocked categories, link the app to the Play listing once it is live.
- Payouts: nothing is configured until the first app earns; then a wire-transfer
  payment method whose holder name matches the payee name exactly, W-8BEN with
  the India treaty benefit, and the postal PIN.

## Domain, site and email

- **`cyanharborstudios.com`** on Cloudflare Registrar (since 2026-08-07),
  verified in Google Search Console (Cloudflare adds the TXT record).
- **Site:** plain HTML/CSS, no build, kept in the cyan repo under `site/`
  (`index.html`, `style.css`, `app-ads.txt`, one folder per app with
  `index.html` and `privacy/index.html`). Hosted as a Cloudflare **Worker with
  static assets** named `bold-sunset-1149`, not a Pages project. To deploy:
  `cd site && zip -r -X ../site.zip . -x '.*'`, then the founder uploads it
  under Workers & Pages → `bold-sunset-1149` → New deployment. Verify from
  outside: fetch `/app-ads.txt` and `/<app-slug>/privacy/`. `www` is not yet a
  second custom domain.
- **Email:** Cloudflare Email Routing forwards `contact@cyanharborstudios.com`
  to the founder's Gmail (test-confirmed 2026-09-22). It is the public contact
  on Play, in every privacy policy and on the site.
- Moving the site out of the first app's repo into this playbook (or its own
  repo) is sensible once a second app needs a page; until then, add new app
  pages under `site/` in the cyan repo and keep each app's `play-policy-config.json`
  pointing at its own privacy page source.

## The founder's Mac (where builds happen)

- JDK 17 (`/usr/libexec/java_home -v17`), Android command-line tools at
  `/opt/homebrew/share/android-commandlinetools` (`ANDROID_HOME`), the API 36
  Google Play arm64 system image, and the AVD `budget_phone` (Pixel 4a profile,
  1080×2340 at 440 dpi, 2 GB RAM).
- The phone (ColorOS, with a work profile) is on USB debugging; install with
  `adb install --user 0 -r`.
- `~/CyanHarbor/keystore/` holds the upload keystores (one per app) and their
  password files; `~/CyanHarbor/builds/<date>-build<N>/` the artefacts, with
  `superseded/` for old ones; `~/CyanHarbor/play-upload/` stages the AAB.
  `~/CyanHarbor/playbooks/google-play-launch-playbook.md` is the founder's own
  launch notes, written outside any repo; it belongs in this playbook.
- Releases are built locally with Gradle. EAS profiles exist in the first app's
  `eas.json` but are not used for releases; the EAS owner is `cyan-harbor`.
