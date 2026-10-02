# ADR-006: AdMob with Google's test unit ids, behind the UMP consent flow

## Status

Accepted (2026-10-02, founder's constraint in the brief). Ad formats and placement
are provisional until the founder closes gates G6 and G7 in `PLAN.md`.

## Context

The reference app earns from ads: a large native ad on each screen, which loads after
the screen has drawn and pushes content down, and a full-screen ad on the tap that
opens its history and its statistics. During the study a tap aimed at a settings row
landed on an ad that arrived under it. Google Play's ads policy forbids ads placed to
draw accidental taps and full-screen ads that appear when the user has chosen to do
something else. The studio publishes every app from one developer account.

The founder's constraints: AdMob, Google's published test ad unit ids only, plus
Google's UMP consent flow. Live ids come only from the founder.

## Decision

- **SDK**: Google Mobile Ads (`play-services-ads`) and the User Messaging Platform.
- **Ids**: Google's published test ids. The two unit ids are in `ads/AdUnits.kt`; the
  sample AdMob app id is one value at the top of `app/build.gradle.kts`, because the
  manifest needs it too. `LaunchGateTest` and `ManifestPermissionsTest` pin all three
  and fail if any drifts or belongs to a real publisher. Switching to live ids is a
  founder-supplied change to those two places and those tests, nothing else.
- **Consent first**: on each launch the app updates consent information and shows
  the UMP form if required; the ads SDK is initialised only after
  `canRequestAds()` is true. Settings shows a "Privacy Choices" entry whenever UMP
  reports privacy options as required.
- **Reserved space**: one anchored adaptive banner for the whole app, in a slot along
  the bottom edge beneath every screen. The slot has the banner's height from the
  first frame, whether or not an ad ever loads, so nothing moves when it fills. No
  ad inside lists or between controls.
- **Full-screen ad**: at most one, when the user *leaves* History or Statistics, not
  on the tap that opens them, and no more often than once every few minutes
  (`AdPlacements`). The reference's on-open behaviour is one constant away, for the
  founder to choose (G6).
- **Never**: an ad during the role request or a confirmation; an ad started from the
  screening service; the SDK initialised in `Application.onCreate`.
- Ad content rating is capped at PG in code, as in the first app (G7).

## Consequences

Positive:

- The layout cannot shift, so the accidental-tap failure seen in the reference
  cannot happen here.
- One file and one test stand between test ads and live ads.

Negative:

- Likely less ad revenue than the reference's placement. That trade is the
  founder's to make at G6, with the policy text in front of him.
- The legacy Mobile Ads SDK is in maintenance mode; Google now steers new apps to
  its "next-gen" SDK. The ads code is one small package, so moving is contained.
  Re-check when live ids are wired.

## Data safety note

AdMob collects and shares approximate location (from IP), app interactions,
diagnostics and device or other identifiers. The app itself collects nothing: its
call log is on-device only. Both statements belong in the Data safety form and the
privacy page, together.
