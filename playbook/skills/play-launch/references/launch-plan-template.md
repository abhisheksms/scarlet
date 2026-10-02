# Launch plan — <App name>

Goal: **live on Google Play, published by Cyan Harbor Studios.**

Ticket-shaped, dependency-ordered. Epic A (studio identity: Udyam, domain, site,
D-U-N-S, organization account, payments profile) was done once in 2026 and is
not repeated; see `studio-account.md`. Copy this file to the app repo as
`docs/launch/LAUNCH_PLAN.md`, keep status per row, and write the date on every
tick. Cricket Auction Simulator's filled-in plan (cyan repo,
`docs/launch/LAUNCH_PLAN.md`) shows what a finished one looks like.

## EPIC B — Store assets (critical path)

| # | Task | Who | Time | Notes |
|---|---|---|---|---|
| B1 | App icon 512×512 (32-bit PNG with alpha) + adaptive foreground/background | Claude | — | Check it on the emulator's home screen, not just the PNG |
| B2 | Icon wired into `app.json`; splash icon | Claude | — | |
| B3 | Feature graphic 1024×500, 24-bit, no alpha | Claude | — | `docs/launch/play-feature-graphic-1024x500.png` |
| B4 | 2–8 screenshots, 1080×1920, from the release build on the emulator | Claude | — | Not off a phone: 20:9 breaks Play's 2:1 limit |

## EPIC C — Play Console (founder's clicks; Claude writes every field)

| # | Task | Who | Time | Notes |
|---|---|---|---|---|
| C1 | **Create app**: `<Listing title>`, Game/App, Free | You | 5 min | |
| C2 | **Store listing**: paste from `STORE_LISTING.md`; website `https://cyanharborstudios.com` | You | 20 min | B1–B4 |
| C3 | **Content rating** questionnaire | You | 10 min | Answers in `STORE_LISTING.md` |
| C4 | **Data safety**: import `data-safety-play-console.csv` | You | 15 min | Must match the privacy page |
| C5 | **Target audience + ads declaration + advertising ID + privacy policy URL** | You | 10 min | 13+ keeps the Families policy out |
| C6 | **Upload AAB** to Internal testing; accept Play App Signing; add your Gmail as tester | You | 10 min | Newest dated folder in `~/CyanHarbor/builds/`, never `superseded/` |
| C7 | **Create in-app product(s)**: id exactly as the code queries; price; bulk-apply then check India's rounding | You | 10 min | Needs C6 first |
| C8 | **License testers**: add your Gmail; make one free test purchase on the Play-installed copy; Restore finds it | You | 10 min | A sideloaded APK can't test billing |
| C9 | **Android developer verification** shows no issues | You | 1 min | Sidebar |
| C10 | **Roll out to internal testing**; install from Play; check every screen | You | 30 min | |

## EPIC D — AdMob (per app)

| # | Task | Who | Notes |
|---|---|---|---|
| D1 | Add the app in AdMob; create the ad unit(s); send Claude the ids | You | Ids are public, not secrets |
| D2 | Wire the ids; flip the launch-gate test to pin them; rebuild | Claude | Never tap a live ad |
| D3 | App blocking controls: max ad content rating PG; block Social casino, Dating, Get rich quick, Reference to sex; keep Gambling and Alcohol blocked | You | |
| D4 | After the app is live: link it to the Play listing; `app-ads.txt` shows Verified | You | AdMob's store search only finds live apps |
| D5 | GDPR message, only after a build with the Privacy Choices entry point is in production | You | |

## EPIC E — Production

| # | Task | Notes |
|---|---|---|
| E1 | Policy checker `--apk --aab --online --gradle`: 0 errors on the exact files uploaded | |
| E2 | Promote the internal-testing release to production; countries: all; managed publishing off | Same AAB, no rebuild. EU trader status: trader |

## EPIC F — After submission

| # | Task | Owner | Notes |
|---|---|---|---|
| F1 | Google's review (a few days to a week for a new app) | — | Check the public listing URL from outside; the email may not be readable from a session |
| F2 | Prices Play rounded (India was rounded ₹149 → ₹150 on the first app) | You | Price changes need no review |
| F3 | The test order shows "Processed" in Order management | You | Unacknowledged orders are refunded in minutes |
| F4 | D4, D5 | You | |
| F5 | First update: internal testing → phone check → promote | Claude, then you | `android-release` skill |
| F6 | Read Play Console's "For your next release" notes; most are advisory | Claude | |
| F7 | Google Play Games on PC: opt in once tried on a PC | Later | |
