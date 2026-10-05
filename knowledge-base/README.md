# scarlet knowledge base

The authoritative spec for the app. `PLAN.md` at the repo root is the spine; these
documents elaborate it and may not contradict it. Docs describe what the app does
now, not what it might do. A hard-to-reverse decision is an ADR before it is code.

| Document | What it settles |
|---|---|
| [`docs/00-product-vision.md`](docs/00-product-vision.md) | what the app is, for whom, why it wins |
| [`docs/01-v1-scope.md`](docs/01-v1-scope.md) | must / later / never for phase one |
| [`docs/02-rules-engine.md`](docs/02-rules-engine.md) | the rule list: facts, conditions, order, how a new rule is added |
| [`docs/03-screening-flow.md`](docs/03-screening-flow.md) | what happens between Android handing over a call and the phone ringing or not |
| [`docs/04-application-architecture.md`](docs/04-application-architecture.md) | modules, storage, dependencies and why each is there |
| [`docs/05-design-workflow.md`](docs/05-design-workflow.md) | how the design is made: the brief, the boards, the pick and the handoff |
| [`adr/`](adr/) | ADR-001 stack · ADR-002 call-screening role, no contacts · ADR-003 no call-log permission · ADR-004 rules as data · ADR-005 SDK levels · ADR-006 ads · ADR-007 call-backs, dialled numbers kept a day · ADR-008 a day's pause after an emergency call |

Outside this folder: [`../FEATURES.md`](../FEATURES.md) is the feature inventory
with sources and verification; [`../docs/reference/notes.md`](../docs/reference/notes.md)
is the raw observation of the reference app (research, behind the firewall);
[`../docs/SECURITY_CHECKLIST.md`](../docs/SECURITY_CHECKLIST.md) is the pre-merge gate.

## Maintenance

- When behaviour changes, change the document that owns it in the same PR.
- A founder decision is recorded where the topic lives, with its date, and the ADR
  is added or amended.
- Deprecated ideas are marked, not deleted.
