# Cyan Harbor playbook

How Cyan Harbor builds and ships Android apps, extracted from the first two
projects so the next one starts from it instead of re-learning it: **cyan**
(Cricket Auction Simulator, live on Google Play since 2 October 2026) for the
stack, the policy guard, the release and launch procedures and the founder's
taste; **turqoise** (an interval timer, stopped at the design stage in August
2026) for the written-down method, the design pipeline and the security
additions. It is a Claude Code plugin: six skills that load into any repo that
enables it.

| Skill | Fires when | Holds |
|---|---|---|
| `project-method` | starting or re-planning a project; "what phase are we in"; writing PLAN.md; briefing Claude Design; design review | the phases and exit criteria, the founder's gates, PLAN.md / design brief / PRODUCT.md / build-prompt templates, the design workflow with Impeccable, the monetization gate, turqoise's and cyan's documents as examples |
| `founder-taste` | any UI, copy, pricing or monetization work; "looks like AI slop" | `FOUNDER_TASTE.md`, the founder's own product calls (master copy) |
| `play-policy-guard` | before every Play upload; any change to ads, purchases, permissions, SDKs, data, listing, names | the policy checker (`play_policy_insights.py`, Google's insights-lint runner), the policy digest, the per-app config example, the app-record template |
| `android-release` | build, ship, screenshot, emulator, versionCode | the release checklist, the signed Gradle build, AAB checks, emulator and screenshot procedure, `check-aab.py`, `adb_drive.py` |
| `play-launch` | launching a new app; any Play Console or AdMob form | what the studio already has, the per-app launch plan, listing / Data safety / privacy templates and the Play CSV |
| `new-app-setup` | "new app", "same stack", "clone X" | the verified tech stack, testing patterns, the security checklist, and templates: CLAUDE.md, AGENTS.md, CI, workspace and Expo configs, test harness, purity test, knowledge base with ADRs |

## Where it lives

In the `scarlet` repo (the third Cyan Harbor project) under `playbook/`, with
the marketplace manifest at the repo root. Sessions in scarlet load the skills
through `.claude/skills/`, which symlinks each one; scarlet's own project skills
can sit beside them. If the playbook ever moves to its own repo, the only thing
that changes is the repo name in the settings below.

## Use it in another repo

Commit this to the app repo as `.claude/settings.json` (the template is in
`playbook/skills/new-app-setup/templates/claude-settings.json`):

```json
{
  "extraKnownMarketplaces": {
    "cyan-harbor-playbook": { "source": { "source": "github", "repo": "abhisheksms/scarlet" } }
  },
  "enabledPlugins": { "cyan-harbor@cyan-harbor-playbook": true }
}
```

Every Claude Code session in that repo then has the skills (as
`/cyan-harbor:founder-taste` and so on). To install it once for every project on
a machine instead:

```bash
claude plugin marketplace add abhisheksms/scarlet
claude plugin install cyan-harbor@cyan-harbor-playbook
```

A cloud session that attaches the scarlet repo directly also gets the skills,
through the `.claude/skills/` symlinks.

## Rules

- **This is the master copy of everything generic.** When an app teaches
  something that the next app would need too (a policy check, a build gotcha, a
  taste call, a template fix), change it here and let the apps pick it up.
- **App-specific things stay in the app:** its codebase-expert skill, knowledge
  base, launch plan and app record, listing, graphics, ad and product ids,
  `play-policy-config.json`, site pages.
- **No secrets, no personal data.** AdMob ids are public; keystores, passwords,
  the registered address and payments-profile numbers never come here.
- Scan new Pine or config files for a 64-hex default before merging anywhere.

## Layout

```
<repo root>/.claude-plugin/marketplace.json   the repo as a marketplace with one plugin
<repo root>/.claude/skills/<skill> -> ../../playbook/skills/<skill>
playbook/
  .claude-plugin/plugin.json
  README.md                 this file
  skills/
    project-method/       SKILL.md, templates/, references/, examples/
    founder-taste/        SKILL.md, FOUNDER_TASTE.md
    play-policy-guard/    SKILL.md, policy-config.example.json, scripts/, references/
    android-release/      SKILL.md, scripts/, references/build-release-ops.md
    play-launch/          SKILL.md, references/, templates/
    new-app-setup/        SKILL.md, references/, templates/
```

## Provenance

Extracted on 2 October 2026 from `abhisheksms/cyan` at commit `922adad` and
`abhisheksms/turqoise` at `d4fb6b4`. Copied files carry a note at the top
saying so. The cyan repo still has its own copies of the policy guard, founder
taste and launch docs; switching it to this plugin (and deleting the duplicates
there) is the next step, so the two can't drift. Turqoise's timer-specific
spec, engine docs and spike harness stay in its repo for a timer-like product
to pick up.
