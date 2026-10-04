# Google Play Repetitive Content policy

> Scarlet's copy of the studio's rule. The master is falcon's
> `playbook/skills/play-policy-guard/references/play-repetitive-content.md`; the
> general parts are the same here, and "App notes" below is Scarlet's own.
> Saved 2026-10-04; "Making the app better than its model" came from the founder's
> call the same day.

Every app we publish ships from one Play developer account. Google Play's Spam policy includes a Repetitive Content rule, and enforcement can escalate from removing a single app to terminating the account and every app in it. So each app is checked against the rule twice: before work starts, and before every store submission.

## What the rule says

Paraphrased from the Play Console policy page (checked 4 October 2026):

- Play does not allow an app that only reproduces an experience already available on the store. Each app has to offer its own content or service.
- Named violation 1: copying content from other apps without adding original content or value.
- Named violation 2: publishing several apps with highly similar functionality, content and user experience. Google's suggested fix for small near-duplicates is one app that combines them.

Source of truth: https://support.google.com/googleplay/android-developer/answer/9899034
(page title "Spam - Play Console Help"; the Repetitive Content section and both named
violations were re-read on 2026-10-04)

The page changes without notice. If this file is more than a few months old, re-read the page and update this file first.

## How to apply it

The rule is written in terms of experience, functionality and content, so judge by what a user sees and does: screens, flows, content and the store listing.

**Check 1: against the app it is modeled on.** Building in an existing category is fine. Shipping a copy is not. The app needs at least one difference a user would notice and could name: a feature, a use case, content, or a clearly different flow. Do not reuse another app's text, images, icons, screenshots or data.

When an app is modeled on an existing one, propose the features that would make it better and different without waiting to be asked, and record the chosen ones under that app's heading in "App notes" below. The owner wants every app to end up better than its model, not only different enough to pass.

**Check 2: against our own published apps.** If a new app would share most of its functionality, content and flow with one we already publish, it belongs inside that app as a feature, mode or content pack, not as a separate listing. Shared code and scaffolding are fine. Shared screens with swapped content are not.

**Combining apps.** Merging our own similar apps into one app is the fix the policy itself suggests, so it always satisfies Check 2. Combining features from two or more apps by other developers can be the difference for Check 1, when the combined app does a job that neither original does alone. It is not a way around the rule: copied content still fails, and a pile of unrelated features is not a difference a user would name.

## Making the app better than its model

Passing Check 1 is the floor. The founder's standard (4 October 2026) is to include
the best features of the other similar apps, so that ours is the one a user picks.
This is also what keeps us clear of the rule: a user can name what we do that the
model doesn't. Do it before the spec is signed off, and again if the category has
moved.

1. **Survey the category, not only the model.** Take the model plus the next two to
   four apps for the same job (from the store's search suggestions and ranking). For
   each, read the listing's feature list and the 1 to 3 star review themes: what
   users miss, what makes them uninstall.
2. **Lay out a feature matrix:** one row per feature, one column per app.
   - Everyone has it: table stakes. We match it, our own way.
   - One app does it notably well: adopt the behaviour, with our own execution.
   - Nobody does it, or reviews keep asking for it: a candidate difference.
3. **Choose the differences.** A good one does one of these:
   - fixes a complaint theme from the reviews;
   - brings a best-in-class feature from another app into the model's job, so that
     one app does what two did;
   - fits the local market (Scarlet's India number rules).

   Aim for three headline differences in the first public release, each with a one-line
   reason, and park the rest as improvements (the Pro candidates).
4. **Guard the choices.**
   - Take behaviours and ideas, never content, text, art, names or data.
   - A feature that needs a restricted permission (call log, SMS, accessibility) or a
     server the app doesn't have is usually not worth the policy risk. Write it under
     "Not planned" with the reason, as Scarlet's entry does.
   - A pile of unrelated extras is not a difference, and founder taste says subtract:
     every feature has to serve the app's one job.
   - Grade the evidence. A feature read on the app's own listing or seen on a phone is
     solid; a review claim is a lead to confirm; a blog "best apps" list is not enough
     to choose on.
5. **Record it** under the app's heading in "App notes": the chosen differences, which
   app or review theme each came from, and what was left out. The teardown playbook's
   inventory and "what we do better" note already hold this evidence.

## Done means

The app's README contains both of these before the first store submission:

1. One sentence naming the closest existing Play app and the difference a user would notice.
2. One sentence confirming that none of our published apps shares its core functionality, content and flow, or naming the app it was merged into.

## When a check fails or is unclear

Stop before building further or submitting, and ask the repo owner. State which check failed and what the overlap is. Changing only names, colors or wording does not fix an overlap, because the experience stays the same.

## App notes

### Scarlet (call blocker)

**Modeled on:** Block Unknown Callers by Life Software Lab. From its listing: one switch that blocks or silences calls from numbers not in contacts, an optional notification, a history with per-number details, a temporary allow, no login, no contacts access, and ads.

**README difference sentence (draft):** Closest Play app: Block Unknown Callers (Life Software Lab). Difference: Scarlet knows Indian number series, so bank and government service calls always ring and marketing calls never do; it lets an urgent repeat caller through; and it shows the rule behind every decision.

**Release gate.** The build order in the Scarlet brief still holds: reach parity first, then add the items below. What changes is the release: a parity-only build stays on internal testing, and nothing goes to a public Play track until differences 1 to 3 are in.

**Differences (in the first public release):**

1. India number rules. Calls from the 1600 series always ring. TRAI reserves that series for service and transactional calls from banks, insurers, mutual funds, brokers and government bodies. Calls from the 140 series, which TRAI reserves for promotional calls, are always blocked. Both rules are on by default and each has its own switch. Why: blocking every unknown number also blocks the bank's fraud-check call, and that missed call is what makes people switch a blocker off.
2. Repeat-caller pass. An unknown number that calls again within a short window rings through. Default: a second call within 3 minutes. The window is adjustable and the rule can be turned off. Why: a person with an urgent reason calls back straight away.
3. A reason on every handled call. History shows which rule decided each call, for example "Blocked: 140 promotional", "Allowed: bank or government 1600", "Allowed: called twice" or "Blocked: not in contacts". Each entry has one-tap "always allow" and "always block", stored in the app's own lists. Why: a blocker that cannot explain a missed call gets uninstalled after the first one. The ordered rule list in the brief already knows which rule fired.

**Improvements (after the first release; candidates for the one-time Pro unlock):**

4. International-call rule: block or silence unknown callers from outside India, with an allow list of countries for people with family abroad.
5. Schedules: block unknown callers only during chosen hours, such as night or work hours, and let them ring otherwise.
6. Custom prefix rules: user-defined "numbers starting with" block and allow rules.
7. Quick pause: a Quick Settings tile and a notification action that let unknown calls ring for 30 minutes, 2 hours or until tonight, for deliveries and cabs. The model app already has a temporary allow, so this is parity done faster, not a difference on its own.
8. Weekly summary: one notification a week showing what was blocked and why. A blocker works silently, so users forget it is doing anything, and the summary is the natural place for the Pro prompt.
9. Hindi interface, then other Indian languages, reviewed by a fluent speaker before release.

**Not planned, and why:**

- Caller-name lookup or a spam-number database. It needs a server, user data and scale, it is the big caller-ID apps' ground, and it breaks the no-login, no-contacts privacy pitch.
- Anything that needs call-log, SMS or contacts permissions. Call-log and SMS permissions are restricted on Play, and contacts access would break the privacy pitch.
- Per-SIM rules. As far as the Android docs describe it, a screening service is told a call's number and time, not which SIM it arrived on. Confirm in the docs before revisiting.

**Verify before building each rule:**

- How 1600-series and 140-series numbers actually arrive as the incoming number: with or without +91, and how many digits. Use documented examples or real calls, then normalize before matching.
- TRAI's current rules for both series. They come from TRAI directions with compliance deadlines in early 2026, and they can change.
- The exact fields a screening service receives about a call. Every rule has to work from those plus the app's own history of screened calls.

**Done for Scarlet's differences:** with simulated calls on the emulator, a 1600 number rings, a 140 number is blocked, a second call inside the window rings, and each of the three shows its reason in history. The README has the two sentences from "Done means".

**Stop and ask the owner if** the model app turns out to already have any of differences 1 to 3, because the difference has to be real.

## CLAUDE.md pointer

Add this line to the app's CLAUDE.md once, so every session picks the rule up (the
`new-app-setup` CLAUDE.md template already carries it):

> Play policy: before starting a new app or submitting a build, run the checks in docs/play-repetitive-content.md. A violation can cost the whole developer account.
