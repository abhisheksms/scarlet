# Play Console — Data safety form answers

> Template, filled in for Cricket Auction Simulator (the first Cyan Harbor app).
> Copy it to the new app's `docs/launch/` and replace every app-specific line.

Checked 2026-09-23 against Google's own disclosure for the Mobile Ads SDK
(https://developers.google.com/admob/android/privacy/play-data-disclosure).
The app itself collects nothing — no account, no analytics, no server. The four
rows below are what the AdMob SDK collects for players who haven't bought ad
removal. Purchases go through Google Play Billing, which you don't declare.
If you ever add Firebase Analytics/Crashlytics, revisit this.

**Submitted 2026-09-25** by importing [`data-safety-play-console.csv`](data-safety-play-console.csv)
(Play's own export format) on the Data safety page. After any change, edit the
CSV and this file together and re-import it.

## Data collection and security
- **Does your app collect or share any of the required user data types?** Yes
- **Is all of the user data collected by your app encrypted in transit?** Yes
- **Do you provide a way for users to request that their data be deleted?** No —
  there is no account and no server-side data. Contact: contact@cyanharborstudios.com

## Data types — tick exactly these four, nothing else
For each: **Collected** and **Shared**, **not** processed ephemerally,
**required** (users can't switch it off), purposes **Advertising or marketing**,
**Analytics**, **Fraud prevention, security, and compliance**.

| Section | Data type | What it is |
|---|---|---|
| Location | Approximate location | Derived from the IP address |
| App activity | App interactions | Ad views and taps |
| App info and performance | Diagnostics | Ad-performance diagnostics |
| Device or other IDs | Device or other IDs | Advertising ID, app set ID |

## Related App content answers
- **Ads:** Yes, my app contains ads (Google AdMob, rewarded format only).
- **Advertising ID:** Yes, the app uses it — purposes: Advertising or marketing,
  Analytics, and Fraud prevention, security, and compliance (the same three as
  the Device or other IDs row, so the two forms agree).
