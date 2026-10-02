# Google Play policy digest for an ad-supported offline game with a one-time purchase

> Shared across Cyan Harbor apps (master copy in the playbook). Written for
> Cricket Auction Simulator's pre-launch audit; where it says "the app" or
> names cricket, read it as the worked example.

Checked against Google's own pages by the pre-launch audit on 2026-09-24. Dates
move every year, so re-check any rule with a date before relying on it. Each rule
names the checker id that covers it, or "judgment" if no script can.

## Platform requirements (dated)

1. **Target API:** new apps and updates must target API 36 from 31 Aug 2026 (an extension to 1 Nov is available on request). Checker: PP-TARGET-SDK. https://developer.android.com/google/play/requirements/target-sdk
2. **Play Billing Library:** version 8+ is required from 31 Aug 2026; version 8 itself retires on 31 Aug 2027. Checker: PP-BILLING-LIB (`--gradle`). https://developer.android.com/google/play/billing/deprecation-faq
3. **16 KB pages:** 64-bit native libraries must support 16 KB page sizes. Checker: PP-16KB. https://developer.android.com/guide/practices/page-sizes
4. **Developer verification:** apps from participating stores that aren't registered by 30 Sep 2026 can no longer be installed on certified Android devices in select countries. Apps created in Play Console register automatically; Play Console's home confirmed it for this app ("All of your Play apps have been successfully registered", seen 2026-09-27). Keys used to sign copies outside Play, such as the upload key on hand-installed test builds, can be registered on the Android developer verification page too. Judgment (founder check). https://support.google.com/googleplay/android-developer/answer/16984799

## Privacy and data

5. **Privacy policy:** linked in Play Console **and** inside the app. Public HTML (not a PDF, not geofenced) with developer contact details, the data types and who receives them, secure handling, and retention and deletion. Checkers: PP-PRIVACY-LINK, PP-PRIVACY-CONTENT, PP-PRIVACY-LIVE. https://support.google.com/googleplay/android-developer/answer/10144311
6. **Data safety:** declare what the app and its SDKs collect. It must be consistent with the privacy policy and updated whenever SDKs change. On-device-only data, and payment data handled by Play Billing, are exempt. Checkers: PP-DATA-SAFETY, PP-PRIVACY-CONTRADICTION. https://support.google.com/googleplay/android-developer/answer/10787469
7. **AdMob's baseline for Data safety:** approximate location (from IP), app interactions, diagnostics, and device or other IDs (advertising ID, app set ID). All are collected and shared, for advertising, analytics and fraud prevention. https://developers.google.com/admob/android/privacy/play-data-disclosure
8. **Advertising ID:** declare AD_ID and complete the Advertising ID form. Use it only for ads and analytics, never link it to persistent identifiers, and honour the user's opt-out. Checker: PP-AD-ID. https://support.google.com/googleplay/android-developer/answer/6048248
9. **AdMob's own privacy rule:** the privacy policy must disclose ad-related collection, including IP addresses, identifiers and third-party collection. Checker: PP-PRIVACY-CONTENT. https://support.google.com/admob/answer/2753860
10. **EEA, UK and Switzerland:** personalized ads need a Google-certified CMP (UMP). Without a consent string, ads are limited. Whenever UMP reports privacy options REQUIRED, the app must offer a way back to the choices. Checkers: PP-UMP, PP-UMP-OPTIONS. https://developers.google.com/admob/android/privacy

## Permissions

11. **Minimum permissions:** request only what a shipped feature uses, and strip template or library extras with `android.blockedPermissions`. Checkers: PP-PERM-RESTRICTED, PP-PERM-UNEXPECTED. https://support.google.com/googleplay/android-developer/answer/9888170
12. **Declaration-gated permissions to avoid:** SMS and Call Log, MANAGE_EXTERNAL_STORAGE, QUERY_ALL_PACKAGES, REQUEST_INSTALL_PACKAGES, background location, READ_MEDIA_*, exact alarms, full-screen intents, accessibility, VPN, and **typed** foreground services (Android 14+). An untyped FOREGROUND_SERVICE from WorkManager needs no declaration. Checkers: PP-PERM-RESTRICTED, PP-FGS-TYPE. https://support.google.com/googleplay/android-developer/answer/13392821

## Ads

13. **No deceptive or disruptive ads:** no surprise full-screen ads, none at level start or during play, and every ad closable. Opt-in rewarded ads are the safe format. Judgment. https://support.google.com/googleplay/android-developer/answer/9857753
14. **Ad content matches the app's rating:** cap `maxAdContentRating` in code, which overrides the AdMob UI, and block sensitive categories in AdMob. Social casino is allowed by default. Checker: PP-AD-RATING; the AdMob blocks are a founder check. https://support.google.com/admob/answer/7562142 and https://support.google.com/admob/answer/3150953
15. **Gambling ads:** none at all if the target audience includes anyone under 18 (it does: 13–17). The PG cap covers this. https://support.google.com/googleplay/android-developer/answer/9877032
16. **Rewarded ads:**
    - explicit opt-in, with the action and the reward stated before the ad;
    - skippable, and the reward always delivered;
    - the reward is non-monetary and non-transferable;
    - declining never blocks normal play.

    Judgment. https://support.google.com/admob/answer/7313578
17. **Invalid traffic:** never tap your own ads or ask anyone to. Use test ads, or register devices as test devices. Judgment. https://support.google.com/admob/answer/2753860

## Money

18. **Payments:** digital goods, including ad removal, go through Play Billing only. No links, buttons or copy steering players to UPI, the web or any other method, unless enrolled in an alternative-billing programme. In-app prices must match Play's. Checker: PP-PAYMENT-STEERING; the price shown comes from Play's `formattedPrice`. https://support.google.com/googleplay/android-developer/answer/9858738
19. **Real-money play:** real-money gambling, fantasy sports and prize contests need licences. Never let a paid item be staked for a real-value prize. Judgment. https://support.google.com/googleplay/android-developer/answer/9877032

## Audience and rating

20. **Target audience:** with no under-13 group, the Families policy doesn't apply, but child-appealing art, characters or wording can still get the app rejected or reclassified. Judgment. https://support.google.com/googleplay/android-developer/answer/9867159
21. **Content rating:** answer IARC accurately and again whenever content changes. Bidding with fictional money is not simulated gambling because nothing is staked on chance; casino-style or betting-style play would be. Judgment. https://support.google.com/googleplay/android-developer/answer/9898843

## Listing and identity

22. **Metadata:**
    - title 30 characters or fewer;
    - no emoji, ALL CAPS or repeated symbols;
    - no ranking, price or promo claims in the title, icon or developer name;
    - no keyword stuffing, and no unattributed testimonials.

    Checkers: PP-LISTING-*. https://support.google.com/googleplay/android-developer/answer/9898842
23. **Intellectual property:** no real league or team logos or marks, and no player names or likenesses, without a licence. "IPL", "BCCI" and real franchise names stay off every surface a player or reviewer sees. Checkers: PP-TRADEMARK, PP-REAL-PERSON. https://support.google.com/googleplay/android-developer/answer/9888072
24. **Impersonation:** never imply an affiliation or "official" status. Developer identity and contact details must be accurate. Checker: PP-LISTING-CLAIM (the word "official"). https://support.google.com/googleplay/android-developer/answer/9888374
25. **An honest, stable app:** listing text, screenshots and claims must match the real app, and it must not crash or freeze. Checkers: PP-LISTING-CLAIM; the rest is emulator verification. https://support.google.com/googleplay/android-developer/answer/9888077 and https://support.google.com/googleplay/android-developer/answer/9898783

## Graphics spec

Play icon: 512×512, 32-bit PNG with alpha, up to 1 MB. Feature graphic:
1024×500, JPEG or 24-bit PNG with no alpha. Phone screenshots: 2 to 8, each side
between 320 and 3,840 px, and the long side at most twice the short side.
Checkers: PP-ICON, PP-FEATURE-GRAPHIC, PP-SCREENSHOTS.
https://support.google.com/googleplay/android-developer/answer/9866151

## Not Play policy, but on the radar

Texas's app-store age law is in force, and Play offers an Age Signals API that it
"doesn't mandate". With 13–17-year-olds in the audience, get a quick legal read
before the US matters. Play Console already asks for a product age rating for
Texas purchase approvals; `remove_ads_lifetime` is set to All ages. https://support.google.com/googleplay/android-developer/answer/16569691
