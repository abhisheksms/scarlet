# Launch plan: the call blocker

Goal: **live on Google Play, published by Cyan Harbor Studios.**

Every step between today's build and a live listing, in the order it can happen, with
who does it and what it waits for. This is the app's copy of the studio's launch plan.
The template is falcon's `playbook/skills/play-launch/references/launch-plan-template.md`,
the worked example is the first app's `docs/launch/LAUNCH_PLAN.md` (the cyan repo), and
the clicks and answers are in falcon's `playbooks/google-play-launch-playbook.md`. Where
this app needs a different answer from the first one, the row says so.

**Where things stand (9 October 2026).** The app is built and runs on the founder's phone
as a test build (the build of 9 October, `cac5ee3`; 230 unit tests, 26 emulator checks).
Nothing of the launch itself has started: no listing text, no store graphics, no privacy
page, no upload key for this app, no release build since the smoke test of 2 October, no
app in Play Console or AdMob. The studio's side was done once for the first app and is not
repeated. Three things that are not paperwork stand between this build and a public
listing: the founder's list of text changes and fixes (2.1), the name and the package
(1.1), and the last of the three differences Play's Repetitive Content rule asks for
(1.3, 2.2).

When a row is done, put ✅ and the date at the front of its step, as the first app's plan
does. When a fact changes, add a dated paragraph above this one and leave the old rows
readable.

## Next three (as of 9 October 2026)

1. **You:** your list of text changes and fixes (2.1), and the decisions in stage 1. The
   name (1.1) and the last difference (1.3) hold the most up.
2. **Claude, once you say the launch starts:** the rows that wait for nothing: 2.5, 3.2,
   3.4, 3.9, 3.10, and drafts of 3.1 and 3.5 under the working name.
3. **Then:** the site deployed (3.6, yours), the release build (stage 4, on your ask) and
   Play Console (stage 6, yours).

## The route

- [ ] **1 · Decisions.** You. Waits for nothing.
- [ ] **2 · Finish the app.** Claude. Waits for your list and for 1.1, 1.3, 1.4.
- [ ] **3 · The launch kit:** the listing's words, the graphics, Data safety, the privacy
      page. Claude writes, you approve and deploy the site. Waits for 1.1.
- [ ] **4 · The release build.** Claude, only on your ask. Waits for 1.2 and the privacy
      page being live.
- [ ] **5 · AdMob:** the app and its ad units. You, then Claude. Waits for 1.1 and 1.4.
- [ ] **6 · Play Console:** the app, its forms, internal testing, the three products, the
      test purchases. You. Waits for stages 3 and 4.
- [ ] **7 · Production.** Claude checks, you submit. Waits for stages 2, 5 and 6.
- [ ] **8 · After submission.** Both. Waits for Google's review.
- [ ] **9 · Money.** You. Studio-wide; before the first payout, not before launch.

Stages 2 and 3 run side by side, and so can 5. Internal testing (stage 6) does not wait
for the last difference; production (stage 7) does.

**Your clicks come to about three and a half hours in all**, in a few sittings, going by
the first app's times. Google's review of a new app takes a few days to a week; the first
app's took about seven days. The studio's paperwork, the first app's long pole at six and
a half weeks, is already done.

**Four rows are marked Stop**, where a wrong click cannot be undone or costs a review:
the first upload fixes the package name for good (6.6); a product's id can never be
changed or used again (6.7); uninstalling the test build deletes the app's history on
your phone (6.9); and a release left out of Publishing overview gets the listing reviewed
with no app behind it (7.7).

---

## Stage 1 · Decisions (yours)

Each has a recommendation. An answer can be one word.

| # | Decision | Recommendation | Holds up |
|---|---|---|---|
| 1.1 | **The name and the package** (`PLAN.md` G3): the Play title (30 characters at most), the label under the icon, the package, and the short word in the privacy page's address | Keep the package `com.cyanharborstudios.callblock`: it names the job, not a brand. Choose a title that carries the words people search and is more distinctive than "Call Blocker". Claude brings candidates with Play's own search suggestions behind each; those are read in the Play Store app on your phone, at your say-so (`TASKS.md` open 9) | 3.1, 3.3, 3.5, 4.1, 5.1, 6.1 |
| 1.2 | **Whether the release build is shrunk** (R8; `TASKS.md` open 13). Your rule of 3 October: size optimizations need your OK first | Keep it on, and prove it on the copy installed from Play before anything is promoted (6.9). It is Android's default for a native app, the first app's blank screen was a React Native build, and a shrunk build of this app passed a smoke test on 2 October, before the Switchboard screens. Row 4.4 puts both sizes in front of you first | 4.5 |
| 1.3 | **The last difference** (`PLAN.md` decision 12 and G9): a reason on every stopped call, with Always Allow and Always Block. Build it now from the Switchboard parts, or wait for Claude Design to draw it in round 2 | Build it now, as How It Works was on 5 October; round 2 can redraw it. It is the only thing left to build that a public listing waits for | 2.2, and through it stage 7 |
| 1.4 | **Full-screen ads** (`PLAN.md` G6): (b) as built, one when leaving History or Statistics, at most once every few minutes; or (c) none | (b), as `PLAN.md` recommends. Google's rules were read again on 9 October (below): an ad between two pages is allowed and one at the start of a page is not, so the reference's way, (a), is out | 5.1, 5.2 |
| 1.5 | **Ad personalisation, the content cap and the audience** (`PLAN.md` G7) | As the first app: personalised only where the consent form allows it; ad content capped at PG; target audience 13–15, 16–17 and 18 and over, with no under-13 group, which keeps the Families policy out | 5.2, 6.2 |
| 1.6 | **Countries** | All, as the first app. The India rules act only on numbers with India's country code, so nothing misfires abroad. Until 9.2 is done only buyers in India can pay; elsewhere the app shows no price and no Buy key | 7.5 |
| 1.7 | **Languages** (`PLAN.md` G5, `FEATURES.md` F-44) | English only for the first release. Hindi next, read by a fluent speaker before it ships | 3.1 |
| 1.8 | **The plans' names and prices** (`PLAN.md` G12): No Ads ₹99, Pro ₹199, the upgrade ₹100 | Taken as agreed on 6 October ("add pricing"). Say so only if a name or a number changes | 6.7 |
| 1.9 | **The category** | App › Tools, where one-job call blockers are listed. The other candidate is Communication, where the caller-ID apps are | 6.4 |

## Stage 2 · Finish the app (Claude)

| # | Step | Needs | Notes |
|---|---|---|---|
| 2.1 | Your text changes and fixes | your list | `TASKS.md` open 15. Nothing is guessed before it arrives. Put in the same list anything you want cut (`PLAN.md` G5), the time-saved figure if it should go (G8), and whether a sheet's grip drags or goes (`TASKS.md` open 14). No word on those means they stay as built |
| 2.2 | The last difference: the reason line in History and on the number's sheet, Always Block beside the allow keys, a block list in Options, its rule in the engine | 1.3 | `FEATURES.md` D-02. The copy never says "spam". New emulator checks come with it |
| 2.3 | Stage 1 applied: the name in `strings.xml`, `play-policy-config.json` and the listing; the full-screen ad's switch | 1.1, 1.4 | If the package changes, `LaunchGateTest` changes in the same commit |
| 2.4 | A read-only audit before submission: one pass over the code, one over policy against Google's current pages | best after 2.1 and 2.2 | The first app's audit found a blocker the day before it was submitted (falcon, launch lesson 15). Code: the brief in `TASKS.md` open 5. Policy: "Re-audit" in falcon's app-record template. A finding is fixed, or written down with why not |
| 2.5 | What no device has shown yet, on the emulator: the consent form with the test geography set to Europe, TalkBack's reading order, the two licence links, the Open Settings strip, dated headings in History | nothing | The list is `docs/verification/README.md`, "Not yet exercised on a device" |
| 2.6 | The same on your phone, at your say-so: Silence with a real call, the Quick Settings tile on the real panel, a call back from a number you rang, a 1600 or 140 call | you | `TASKS.md` open 1 and 3. None of them blocks the launch |
| 2.7 | TRAI's rules on the 160 and 140 series read again | before stage 7 | The record asks for it before the first public release (`docs/play-repetitive-content.md`) |
| 2.8 | The version: name 1.0.0 (0.1.0 today); code 1 for the first upload and one higher for each upload after | nothing | Play refuses a version code it has already accepted |
| 2.9 | Every emulator check on the final test build, old and new; every changed screen at 360 dp, light and dark, at 100% and 135% text | 2.1 to 2.3 | Recorded in `docs/verification/`, as each round has been |

## Stage 3 · The launch kit

The files live in `docs/launch/`, at the paths `play-policy-config.json` names; the site's
pages live with the site (3.5). falcon's `play-launch` skill has the templates.

| # | Step | Who | Needs | Notes |
|---|---|---|---|---|
| 3.1 | `STORE_LISTING.md`: the title, the short description (80 characters), the full description (4,000, one line per paragraph), tags, the three products' names and descriptions, the content-rating answers, "What's new" | Claude writes, you approve | 1.1, 1.7 | Every claim literally true: no caller ID and no spam detection (the app has neither), not "works offline" (ads and buying need a connection), no other app's name. The policy checker reads this file |
| 3.2 | The Play icon: 512×512, 32-bit PNG with alpha, drawn by a script from the launcher icon's own vector | Claude, your eye | nothing | The first app's icon was redrawn after "juvenile". Looked at on the emulator's home screen as well as in the file |
| 3.3 | The feature graphic: 1024×500, 24-bit, no alpha | Claude, your eye | 1.1 | |
| 3.4 | `DATA_SAFETY.md` and `data-safety-play-console.csv` | Claude | nothing | falcon's template as it is: the only data that leaves the phone is what Google's ads code sends, the same four types as the first app. What stays on the phone is exempt from the form, and is said on the privacy page |
| 3.5 | The privacy page and the app's own page for the studio site, and a card on the site's front page | Claude | 1.1 | The site is one folder in the first app's repo, and one upload replaces all of it: the pages go into that folder through a pull request there, never into a zip of their own. The privacy page says what the ads code collects (the Data safety list, word for word), what stays on the phone and never leaves it (stopped calls, the allow list, numbers you called in the last day, your rules and settings), that Google Play handles purchases, that there is no account, how to delete, children, contact, and an effective date |
| 3.6 | The site deployed | You, 10 min | 3.5 | Claude hands you one zip of the whole site. The clicks are in falcon's launch playbook, part 3 |
| 3.7 | Checked from outside: this app's privacy address answers with the new date, the first app's still answers, `app-ads.txt` still carries the publisher line | Claude | 3.6 | The first app's live page was a month behind its repo until someone looked |
| 3.8 | The two switches in `ui/Links.kt` on: `PRIVACY_PAGE_LIVE` (About's Privacy Policy row) and `STORE_PAGE_LIVE` (Share App, Rate App, the link in shared statistics). Emulator check 13 then taps the rows | Claude | 3.7 | Play requires the privacy row in every build uploaded to it. `LaunchGateTest` refuses live ad ids while either switch is off |
| 3.9 | `APP_RECORD.md`: every answer given to Google, what the app collects, the audit log, your account checks | Claude | nothing | From falcon's policy-guard template. Kept identical to what is submitted |
| 3.10 | The policy checker made to read a Kotlin app | Claude, a falcon pull request | nothing | `TASKS.md` open 7. Today it looks for the in-app privacy link in TypeScript and reports an error this app cannot clear, and an error blocks an upload |
| 3.11 | `play-policy-config.json` pointed at the real files | Claude | 3.1 to 3.5 | Today it names a site folder this repo does not have |

## Stage 4 · The release build (Claude, only on your ask)

falcon's `android-release` skill is the procedure. What changes for a native app: no Expo
step, and Gradle runs from the repo root.

| # | Step | Who | Needs | Notes |
|---|---|---|---|---|
| 4.1 | An upload key for this app in `~/CyanHarbor/keystore/`, with its password in a file beside it | Claude makes it, you back it up | 1.1, your ask | One key per app (falcon's rule); the first app's stays its own. The password is never printed and nothing of it enters the repo. Keep a copy off the laptop: Google holds the real signing key, but a lost upload key means a reset request to Google |
| 4.2 | `.gitignore` gains `*.jks` | Claude | nothing | It lists `*.keystore` today, and the key is a `.jks` |
| 4.3 | The signed build from `main` with CI green: `:app:bundleRelease :app:assembleRelease`, the key passed on the command line | Claude | 3.8, 4.1 | Nothing secret goes into a tracked file. Not yet run with a real key on this repo: the first build proves it |
| 4.4 | The same commit built shrunk and unshrunk, once: both sizes and both cold-start times in front of you | Claude | 4.3 | For 1.2 |
| 4.5 | The files checked, not the config: package, version code and target API 36 (`aapt2 dump badging`); the upload key's SHA-1 on the AAB and on the APK; not debuggable; the merged permissions equal to the allowlist; both native libraries 16 KB-aligned (`check-aab.py`); the Billing library at 8 or later; shrinking as decided in 1.2 | Claude | 1.2 | The four ABIs the libraries ship stay: dropping one is a size optimization too |
| 4.6 | The policy checker on the exact files, with `--apk --aab --online --gradle`: 0 errors | Claude | 3.10, 3.11 | Never while a build or the emulator is running |
| 4.7 | The release build on the emulator: a fresh install, the role granted, a call rejected in Block and silent in Silence as Android's own records show it, every screen opened, no price and no Buy key where Play has nothing on sale, no crash | Claude | 4.5 | The 26 scripted checks read a test build's own log line and its plan keys, so they cannot run on a release build. A shorter pass is written for it |
| 4.8 | The files kept: `~/CyanHarbor/builds/<date>-build<N>/`, older ones moved to `superseded/`, both on Drive with each copy checked by `cmp`, the AAB staged in a folder of this app's own under `~/CyanHarbor/play-upload/` | Claude | 4.5 | The first app's build 9 is still staged in `play-upload/`: two apps' files must not sit side by side there |
| 4.9 | The store screenshots: 2 to 8, 1080×1920, from this build on the emulator, made-up numbers only | Claude, your eye | 4.7 | A phone's own 20:9 capture breaks Play's limit (the long side at most twice the short). Made-up numbers: `NOTES.md` N-19 |
| 4.10 | `RELEASE_CHECKLIST.md` for this app, written from what the first build really took | Claude | 4.8 | The first app's is the model |

## Stage 5 · AdMob

Any time after 1.1 and 1.4. The live ids have to be in the build that goes to production
(7.2); the first upload to internal testing may still carry Google's test ids.

| # | Step | Who | Needs | Notes |
|---|---|---|---|---|
| 5.1 | The app added in AdMob (Apps → Add app → Android → not published yet); one banner unit, and one interstitial unit if 1.4 keeps the full-screen ad; the ids sent to Claude | You, 10 min | 1.1, 1.4 | The ids are public, not secrets |
| 5.2 | The ids wired: the app id at the top of `app/build.gradle.kts`, the units in `ads/AdUnits.kt`, `LaunchGateTest` changed in the same commit to pin them | Claude | 5.1, 3.8 | ADR-006 says to look again at Google's ads library at this point: Google steers new apps to a newer one |
| 5.3 | Blocking controls looked at for this app: ad content rating PG; Dating, Get Rich Quick, References to Sex and Social Casino Games blocked; Alcohol and Gambling stay blocked | You, 5 min | 5.1 | falcon's launch playbook, part 9 |
| 5.4 | Your phone is already a registered test device, which covers every app in the account. Even so, nobody taps a live ad | | | |
| 5.5 | Once the app is live: the app linked to its Play listing, and `app-ads.txt` reads Verified | You | 8.2 | AdMob's search finds only live apps, and a new one can take a day or two to appear |
| 5.6 | The European consent message published for this app (Privacy & messaging) | You | 8.2 | Only once a build with Privacy Choices is in production; 1.0.0 has it. Without the message Europe gets fewer ads and nothing breaks |

## Stage 6 · Play Console (your clicks)

When this stage starts Claude makes the page for it, as for the first app: every field
with a Copy button, and a red Stop where a click cannot be taken back. Claude never
drives the console; it reads your screenshots. The times are the first app's.

| # | Step | Time | Needs | Notes |
|---|---|---|---|---|
| 6.1 | **Create app**: the title from 1.1, English (United States), **App**, **Free**, the three declarations ticked | 5 min | 1.1 | The first app was a Game. Free can never become paid |
| 6.2 | **App content**, top to bottom: the privacy policy's address; sign in details (below); ads: Yes; content rating; target audience (1.5); Data safety by importing the CSV (3.4); government, financial, health, news: none | 30 min | 3.4, 3.7 | Content rating: the category for utilities; No to everything except digital purchases; expect Everyone, PEGI 3, IARC 3+ |
| 6.3 | **Advertising ID** (Policy and programs › App content; it is not on the Dashboard): Yes, for Advertising or marketing, Analytics, and Fraud prevention, security, and compliance | 2 min | 6.1 | Play rolls out nothing, internal testing included, until this is answered |
| 6.4 | **Category and contact**: App › Tools (1.9), tags from the ones Play offers, the studio's contact email and website, the phone left blank | 5 min | 1.9 | |
| 6.5 | **Store listing**: pasted from `STORE_LISTING.md`; the icon, the feature graphic, the screenshots in order; AI asset declaration: Don't label | 20 min | 3.1 to 3.3, 4.9 | |
| 6.6 | **Internal testing**: tick the tester list made for the first app (make it again if Play does not offer it) → Create new release → keep Google's recommended signing → upload the AAB from 4.8 → release notes → Start rollout | 10 min | 4.8, 6.3 | **Stop: this upload makes the package name permanent.** No review. A new app can take from minutes to a few hours to reach the phone. Until the production review the title shows as the package name with "(unreviewed)" and a plain icon: cosmetic |
| 6.7 | **The three products** (Monetize with Play › Products › One-time products): ids exactly `no_ads`, `pro`, `pro_upgrade`; each with one purchase option of type Buy, Backwards compatible on; ₹99, ₹199 and ₹100 applied to all countries, then India's row checked on all three; Activate | 25 min | 6.6, 1.8 | **Stop: a product's id can never be changed or used again.** The bulk price edit rounded India's price on the first app. The upgrade stays at Pro's price less No Ads': Play does not know the three are related |
| 6.8 | **Licence testing** (Settings, account level, set up for the first app): the tester list ticked, the response RESPOND_NORMALLY | 2 min | | A look, not a redo |
| 6.9 | **The Play copy on your phone**: uninstall the test build → the tester link → Accept invite → install from Play → open it → move the lever to Block and accept Android's prompt. The phone on USB for the first launch | 15 min | 6.6 | **Stop: uninstalling deletes the app's history, allow list, rules and settings on your phone, and nothing can carry them over.** The two copies are signed with different keys, so neither can update the other. This first launch is where a shrunk build is proven (1.2): if it opens blank, do not reopen it before the phone is on USB |
| 6.10 | Every screen on the Play copy, and one real unknown call stopped | 15 min | 6.9 | You look. Over USB Claude reads the app's own test tags, as before, and nothing else on the phone |
| 6.11 | **The test purchases** (below), about 30 minutes after 6.7 | 30 min, and a look three days later | 6.7, 6.9 | Only a copy installed from Play can buy |
| 6.12 | **Pre-launch report** (Test and release): no red crash | 2 min | 6.6 | Claude reads your screenshot |
| 6.13 | **Android developer verification**: the banner on Play Console's home says every app is registered | 1 min | 6.6 | Apps made in Play Console register themselves |

**Sign in details (row 6.2).** The app has no login, and the first app answered that
nothing is restricted, because its purchase only removed ads. Here three features sit
behind Pro. Google's rule, read on 9 October (below), asks for a way in for its reviewers
when functions sit behind a paywall; it names subscriptions and is silent on a one-time
purchase. Recommended: answer that some functionality is restricted, and paste Claude's
instructions: no login, what Pro holds, how to open it. The how is the open part. A promo
code for `pro` is Play's own route, but a code works once, and it may not redeem outside
India until 9.2 is done. Try one on your phone in 6.11 before relying on it.

**The purchases to try (row 6.11).** Written from the code and Google's documents; none
has been done in Play Console yet. Close the app fully and reopen it first. Google's
sheet must show Play's own price and "Test card, always approves". If it shows your real
card or UPI, that account is not a licence tester: back out.

1. Buy No Ads. The ad tray goes, and the app is on No Ads.
2. Buy the upgrade. The app ends on Pro: the Timer, the Schedule and Number rules open.
3. Clear the app's data. The plan is back at the next launch, and Restore Purchases says so.
4. Pro bought directly: refund both test orders in Order management with the entitlement
   removed, then buy Pro. A second tester account does the same job.
5. A slow test card: nothing is granted while the payment waits; the plan arrives when it
   clears.
6. A refund: the plan is gone the next time the app comes to the front.
7. One promo code for `pro`, redeemed in the Play Store: the app picks it up. This also
   settles the sign in details question.
8. Three days later: every order that was kept reads Processed. Refunded there means the
   app never acknowledged the purchase.

The purchases can be tried on the emulator instead, if you sign it in to Play yourself
with a tester account (one made for testing is cleaner than your own). That leaves your
phone's history alone until 7.2. The first launch of the Play copy on a real phone is
still owed before anything is promoted.

## Stage 7 · Production

| # | Step | Who | Needs | Notes |
|---|---|---|---|---|
| 7.1 | The Repetitive Content check run again; all three differences in the build; the README's two sentences true of it | Claude | 2.2 | `docs/play-repetitive-content.md`. One account holds every app, so a strike here reaches the first app too |
| 7.2 | The candidate: all of stage 2, the live ad ids (5.2), version 1.0.0; through 4.3 to 4.8 again; uploaded to internal testing; opened from Play on your phone; the screens and one purchase looked at again | Claude, then you | stages 2 and 6, 5.2 | A higher version code than 6.6's |
| 7.3 | The policy checker on the exact files: 0 errors, 0 warnings, the live privacy page equal to the repo's | Claude | 7.2 | |
| 7.4 | Play Console: every "Set up your app" task ticked, App content all Complete, the three products Active, the pre-launch report clean | You, 5 min | 7.2 | |
| 7.5 | Countries: Production → Countries / regions → all | You, 2 min | 1.6 | That adds "rest of world" too |
| 7.6 | The same release promoted from internal testing to production, with release notes | You, 5 min | 7.3, 7.4 | Add from library. No new upload, no rebuild |
| 7.7 | Publishing overview: the release's own line is in the list before Submit | You, 2 min | 7.6 | **Stop: without that line Google reviews the listing and publishes no app.** Managed publishing off, so it goes live when approved. If asked for EU trader status: Trader |

## Stage 8 · After submission

| # | Step | Who | Notes |
|---|---|---|---|
| 8.1 | Google's review | Google | A few days to a week. No email at "in review", only at the result, so Claude checks the public listing's address from outside. Nobody presses "Remove changes" or edits the listing meanwhile. A rejection says what to change |
| 8.2 | Live: the listing read from outside (version, Contains ads, In-app purchases, the rating); Share App and Rate App open it | Claude | |
| 8.3 | India's price on all three products still ₹99, ₹199 and ₹100 | You | A price change needs no review |
| 8.4 | The test orders read Processed in Order management | You | |
| 8.5 | AdMob: 5.5 and 5.6 | You | "No ad available" until then is expected |
| 8.6 | The app's page on the studio site gets its Play link; the site deployed again | Claude, then you | |
| 8.7 | Play Console's "For your next release" notes | You paste, Claude reads | Most are advisory |
| 8.8 | Crashes and reviews: Android vitals and the first reviews, weekly at first | You paste, Claude reads | The app has no crash reporter, by decision (`PLAN.md` decision 7), so these are the only signals |
| 8.9 | The first update: internal testing → the Play copy on your phone → the same release promoted | Claude, then you | `RELEASE_CHECKLIST.md` (4.10) |
| 8.10 | Prices looked at after three to four weeks: buyers against installs | Both | Discount before raising |
| 8.11 | The record: this file, `APP_RECORD.md`, `PLAN.md`, `README.md`; what the launch taught goes into falcon's `launch-lessons.md` | Claude | |

## Stage 9 · Money (studio-wide)

Tracked in the first app's plan, Epic D and row F4. Listed here because 9.2 decides where
this app can sell.

| # | Step | Who | Notes |
|---|---|---|---|
| 9.1 | A current account in the studio's exact payee name; then AdMob's payment method and the US tax form; then Play's payout account | You | Before the first payout, not before launch. Google holds the money until then |
| 9.2 | The cross-border sales verification (BillDesk) | You | Needs 9.1. Until it is done only buyers in India can buy a plan; ads are unaffected |
| 9.3 | The CA's read on GST and the registered address | You | As the first app's plan records it |

---

## Checked on Google's pages (9 October 2026)

Three answers here are not the first app's, so each was read on Google's own page. The
pages change: read them again before relying on a row.

- **Full-screen ads (1.4).** Play's Ads policy does not allow a full-screen ad that
  appears unexpectedly when the user has chosen to do something else, or at the start of a
  content segment. AdMob's own page keeps interstitials to logical breaks between pages of
  the app, never on opening or leaving the app, and not after every action, Back included.
  So an ad on the tap that opens History is out, and one on the way back, capped, is a
  break between pages. A reviewer's eye is still a judgment.
  https://support.google.com/googleplay/android-developer/answer/9857753 ·
  https://support.google.com/admob/answer/6201362
- **Sign in details (6.2).** Google's page on sign in details for review says an app with
  no sign-in whose functions sit behind a subscription paywall must give instructions or
  access that let reviewers reach all of it "fully and freely". It does not mention a
  one-time purchase.
  https://support.google.com/googleplay/android-developer/answer/15748846
- **Promo codes (6.11).** One-time-use codes can be made for a one-time product with an
  active Buy option, up to 500 a quarter for an app, under Monetize with Play › Promo
  codes. The app has to pick up a purchase made outside it; this one asks Google Play what
  is owned each time it comes to the front (ADR-009). Not tried yet.
  https://support.google.com/googleplay/android-developer/answer/6321495

## What the first app's launch taught, and where this plan carries it

| What bit the first app | Row here |
|---|---|
| A privacy page on the live site a month behind the repo, and one that contradicted Data safety | 3.4, 3.5, 3.7, 7.3 |
| The Advertising ID answer missing, which blocks every rollout | 6.3 |
| Play's bulk price edit rounding India's price | 6.7, 8.3 |
| A release left out of Publishing overview | 7.7 |
| A shrunk build that passed every check and opened blank from Play | 1.2, 4.4, 6.9 |
| A hand-installed copy that could not see a purchase | 6.9, 6.11 |
| Phone screenshots at 20:9 | 4.9 |
| A purchase tried before the product had spread through Play | 6.11 |
| A tester address that was not a Google account | 6.6 |
| An old AAB within reach of an upload | 4.8 |
| A build handed over without a screen having been looked at | 2.9, 4.7, 6.10 |
| An icon that read as juvenile | 3.2 |
| Tasks that lived only in a chat | this file |

## What is different from the first app

- **An app, not a game:** the category, the content-rating questionnaire, and no Play
  Games on PC to opt out of.
- **Three products, and features behind one of them:** more purchases to try (6.11), and
  a different answer on sign in details (6.2).
- **A banner and a full-screen ad, not rewarded ads:** the Ads policy has to be read for
  them (1.4), where the first app's opt-in format needed no reading.
- **Native Kotlin:** no Expo step; two small native libraries; shrinking is the
  platform's default (1.2).
- **It screens the founder's real calls:** moving his phone to the Play copy costs the
  app's history there (6.9).
- **It rebuilds another app's feature set:** nothing goes to a public track before the
  three differences are in (7.1).
- **This repo is public:** the studio's identity strings stay in the first app's plan and
  are never copied here.

## Done once for the studio, not repeated

The organization developer account (so no "12 testers for 14 days"), the domain, the site
and the contact address, the payments and merchant profile, the 15% service-fee tier, the
AdMob account and `app-ads.txt`, the registered test device, the tester list and licence
testing, and the Mac's build tools. The state of each is in falcon,
`playbook/skills/play-launch/references/studio-account.md`.

## Never

- Dial an emergency number to test anything, on a phone or on the emulator.
- Tap a live ad on a real device.
- Commit the upload key, or print its password.
- Upload anything from `superseded/`, or send Google's test ad ids to production.
- Turn a size optimization on or off without the founder's OK.
- Let Claude drive Play Console, AdMob, Cloudflare, payments or banking.
- Put the reference app's name, or its developer's, in the listing, the product or the
  code.
- Put an identity string, a serial or a password in this repo: it is public.

## Sources

- The first app's plan and the files beside it: the cyan repo, `docs/launch/`
  (`LAUNCH_PLAN.md`, `RELEASE_CHECKLIST.md`, `STORE_LISTING.md`).
- falcon: `playbooks/google-play-launch-playbook.md`, `playbooks/launch-lessons.md`, and
  the skills `play-launch`, `android-release` and `play-policy-guard` under `playbook/skills/`.
- This repo: `PLAN.md` (the gates), `TASKS.md`, `FEATURES.md`, `docs/SECURITY_CHECKLIST.md`,
  `docs/play-repetitive-content.md`, `docs/verification/README.md`, ADR-006 and ADR-009.
