# <App name>: compliance record

The answers this app gives Google, what it collects, and every audit and review
outcome. Keep this current: when a declaration changes in Play Console, change it
here in the same breath. (Cricket Auction Simulator's filled-in record is in the
cyan repo at `.claude/skills/play-policy-guard/references/app-record.md`.)

## The app, as Play sees it

- Package `com.cyanharborstudios.<app>`. Developer: **Cyan Harbor Studios**, an
  organization account (verified 2026-09-22).
- <One sentence: what kind of app, offline or not, accounts or not, any user
  content or sharing.>
- Monetization:
  - <ads: format, when they show, what they reward>
  - <purchases: product id, price hypothesis, what it grants and doesn't>

## Play Console answers (keep identical to what's submitted)

| Form | Answer |
|---|---|
| Sign in details | <No, nothing is restricted / describe the login> |
| Ads | <Yes, contains ads / No> |
| Content rating (IARC) | <Category. The honest answers, and the ratings received> |
| Target audience | <13–15, 16–17, 18+ keeps the Families policy out. Appeals to children: No> |
| Data safety | See `docs/launch/DATA_SAFETY.md`. Submitted by importing `docs/launch/data-safety-play-console.csv`; re-import after any change |
| Advertising ID | <Yes: advertising or marketing, analytics, fraud prevention / No> |
| Government / financial / health / news | None |
| Privacy policy | https://cyanharborstudios.com/<app-slug>/privacy/ (also linked in Settings) |
| Countries | All |
| EU trader status | Trader (the contact details become public in the EU) |
| Category | <Game › … or App › …> |
| AI asset declaration (listing) | <Don't label real captures and hand-drawn art; label realistic AI images> |
| One-time product | <`product_id` "Display name"; tax category Digital app sales; age rating; purchase option; price and bulk pricing> |
| Form factors | <Google Play Games on PC opted out until tested; ChromeOS and Android XR active> |

## What the app collects

<Only through which SDKs, for which users. The AdMob baseline: IP address
(approximate location), advertising ID and app set ID, ad interactions,
diagnostics. Say whether the app requests non-personalized ads, the content cap,
and whether consent is gathered before the SDK starts. Say where game data lives
(`allowBackup: false` keeps it off the cloud).>

## Audit log

### <date>: pre-launch audit (read-only agent) → PR #<n>, build <n>

| # | Severity | Finding | Resolution |
|---|---|---|---|
| F1 | BLOCKER | | |

### <date>: submitted to production review (build <n>, <version>)

<Every App content item complete, countries, managed publishing on/off. Checker
result on the submitted build.>

### <date>: <outcome>

## Founder account checks

Play Console:
- [ ] Android developer verification page shows the package registered
- [ ] Developer website set to `https://cyanharborstudios.com` (AdMob reads app-ads.txt there)
- [ ] EU trader contact details correct
- [ ] Every App content item shows Complete; read the pre-launch report after the first upload
- [ ] Products active; license tester added; one test purchase verified on a Play-installed copy

AdMob:
- [ ] App added and, once live, linked to the Play listing; app-ads.txt shows Verified
- [ ] App blocking controls: **max ad content rating PG**. Block Social casino games, Dating, Get rich quick, Reference to sex. Keep Gambling & betting and Alcohol blocked
- [ ] GDPR message published only **after** a build with the Privacy Choices entry point is in production
- [ ] Test devices registered; never tap live ads

## Re-audit (for a new app, a new SDK or a new monetization model)

Spawn one **read-only** agent. Give it the app facts above, the planned
declarations, the repo path and the build to inspect. Tell it to research
Google's current pages (Play policy centre, Play Console Help, AdMob Help,
developer.android.com), then report:
1. a verdict;
2. findings (BLOCKER / FIX-BEFORE-LAUNCH / LATER), each with a policy link, evidence and a concrete fix;
3. a declaration-by-declaration check;
4. founder-only account checks;
5. digest updates.

Forbid edits, Gradle, the emulator and any logged-in account. Then fix, re-run
the checker, and add the new rows to the audit log above.
