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

Checked on 4 October 2026 against `FEATURES.md` (what the reference app has, row by row)
and against TRAI's own releases on India's number series. Two of the three differences
first proposed that day turned out to be parity, so this is the corrected record; the
first draft is in this repo's history (PR #14).

**Modeled on:** Block Unknown Callers by Life Software Lab, studied on the founder's phone
on 2 October 2026 (`docs/reference/notes.md`). From its listing and its screens: one switch
that blocks or silences calls from numbers not in contacts, an optional notification, a
filter scope (everyone, or international callers only), a pause, a repeat-caller pass, an
allow list, a history with per-number details, statistics with charts and milestones, a
weekly or monthly summary, no login, no contacts access, and ads. All of that is parity,
built (`FEATURES.md`, F-01 to F-43).

**README difference sentence:** Closest Play app: Block Unknown Callers (Life Software
Lab). Difference: this app knows India's number series, so service calls from banks,
insurers and government bodies (the 1600 and 1601 series) always ring and one switch
blocks every 140-series promotional call; it pauses from Quick Settings with one tap; and
it shows the rule behind every call it stopped, with Always Allow and Always Block one tap
away.

**Release gate.** Parity first (done); then the differences. A parity-only build stays on
internal testing, and nothing goes to a public Play track until the differences below are
in (`PLAN.md`, gate G9).

**Differences (in the first public release):**

1. **India's number series.** Built 4 October 2026 in the rule engine
   (`core/…/rules/RuleBook.kt`): calls from the 160 series always ring while the lever is on
   (1600: banks, insurers and other regulated financial entities, government bodies; 1601:
   utilities, couriers and logistics); calls from the 140 series (promotional calls from
   registered telemarketers) are blocked once the user switches that on in Options. Why
   those defaults: TRAI's third amendment to the TCCCPR (18 September 2026) prohibits
   call-management apps from blanket blocking, filtering or tagging the 1600, 1601 and 140
   series, and keeps the consumer's "full freedom to block, or filter calls on their own
   devices". So the 160 rule has no switch, and 140 blocking is the user's own choice, off
   as installed. A blocker that rejects every unknown number is blocking the bank's own
   fraud-check call; that missed call is what makes people switch a blocker off. Verified:
   unit tests and emulator check 11. Not yet seen: a real 1600 or 140 call on an Indian SIM.
2. **A reason on every handled call, with Always Allow and Always Block one tap away.** The
   engine already stores the deciding rule with every call (`handled_calls.rule_id`). What is
   missing is the surface: the reason line on each History row and on the number's sheet
   ("not in contacts", "promotional 140 number", "allowed for an hour" …), an Always Block
   key on the sheet beside the allow keys, and a block list in Options like the allow list.
   The reference app has neither reasons nor a block list. Goes to Claude Design as a
   round-2 note (`knowledge-base/docs/05-design-workflow.md`); built once drawn. The copy
   says "promotional", never "spam": TRAI forbids tagging these calls as spam.
3. **A Quick Settings tile.** The repeat-caller pass proposed as difference 2 is parity:
   the reference app has it (F-13), as it has the international-call rule (F-11) and the
   weekly summary (F-09) listed below as improvements. Of the candidates the reference
   lacks (checked on the founder's phone on 2 October: no widget, no Quick Settings tile,
   no shortcuts), the founder picked the tile on 4 October ("go with the quick settings
   tile"), and it was built the same day: lit while calls are filtered; one tap pauses
   filtering for an hour, the next resumes it; a tap when nothing is filtered opens the
   app. Why: the courier case from the lock screen, without opening the app. Verified:
   unit tests and emulator check 12. Not yet seen on a real phone's panel.

**Improvements (after the first release; candidates for the one-time Pro unlock):**

4. International-call rule: the scope exists (F-11); the addition is an allow list of
   countries for people with family abroad.
5. Schedules: block unknown callers only during chosen hours, such as night or work hours,
   and let them ring otherwise. **Built 5 October 2026, in Pro**, with a timer that holds
   Off, Silence or Block for a while (`FEATURES.md` A-11, A-12). The reference app has
   neither. Five of 34 call blockers surveyed that day sell a schedule
   (`docs/premium-research.md`); they are features of the paid plan, so the three
   differences above, which every user has, remain the ones this record rests on.
6. Custom prefix rules: user-defined "numbers starting with" block and allow rules.
7. Quick pause: a Quick Settings tile and a notification action that let unknown calls ring
   for a while, for deliveries and cabs. The pause itself is parity (F-12); the tile is not.
8. Weekly summary: exists (F-09); the addition is "and why", once reasons are shown.
9. Hindi interface, then other Indian languages, reviewed by a fluent speaker before
   release.

**Not planned, and why:**

- Caller-name lookup or a spam-number database. It needs a server, user data and scale, it
  is the big caller-ID apps' ground, and it breaks the no-login, no-contacts privacy pitch.
- Tagging or labelling any call as spam, or a spam report inside the app. TRAI's amendment
  forbids tagging the designated series and requires any in-app spam report to reach the
  operators' DLT platform. This app labels a stopped call with the rule that stopped it,
  nothing more.
- Anything that needs call-log, SMS or contacts permissions. Call-log and SMS permissions
  are restricted on Play, and contacts access would break the privacy pitch.
- Per-SIM rules. A screening service is told a call's number and time, not which SIM it
  arrived on.

**Verified before building the series rules (4 October 2026):**

- How the numbers arrive: the engine reads "1401234567", "01401234567" and
  "+91 140 123 4567" into one key (unit test), so the rule holds however an Indian network
  presents the number. A real 1600 or 140 call on an Indian SIM has not been seen yet.
- TRAI's rules, read on the government's and TRAI's own pages: the 140 series stays for
  promotional calls and the 1600 series is for service and transactional calls (PIB, 12 Feb
  2025); adoption deadlines for banks, NBFCs, mutual funds, brokers, pension bodies (PIB,
  19 Nov 2025) and insurers (PIB, 17 Dec 2025), all passed by 15 March 2026; "any tagging,
  blocking or filtering of the calls originating from 1600 series numbers is not permitted"
  and the customer blocks 140 calls through the DND registry (TRAI press release 91/2026,
  10 Jul 2026); the third amendment's prohibition on blanket blocking by call-management
  apps, with the consumer's own freedom kept (TRAI press release 119/2026, 18 Sep 2026).
  These rules can change; re-read them before the first public release.
- What a screening service receives: the number and the time (`Call.Details`), which is
  all the rules use.

**Done for Scarlet's differences:** differences 1 and 3 are done on the emulator (check
11: a 1600 number rings, a 140 number is silenced like any unknown caller as installed and
blocked once the switch is on; check 12: one tap on the tile pauses filtering, the next
resumes it). Difference 2 is open. The README has the two sentences from "Done means".

**The stop happened.** This file says to stop and ask the owner if the model app turns out
to have one of the differences. It has the repeat-caller pass, so the third difference is
the founder's pick (G10); the other two stand.

## CLAUDE.md pointer

Add this line to the app's CLAUDE.md once, so every session picks the rule up (the
`new-app-setup` CLAUDE.md template already carries it):

> Play policy: before starting a new app or submitting a build, run the checks in docs/play-repetitive-content.md. A violation can cost the whole developer account.
