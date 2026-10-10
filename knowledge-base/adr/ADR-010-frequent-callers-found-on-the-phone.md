# ADR-010: Frequent callers are found on the phone, in the app's own record, with no backend

## Status

Accepted (2026-10-10). The founder's ask of that day, in his words: "a mini brain on an app
that runs on its own that basically figures out, based on the blocked call logs, which are
the most common ones and suggests a toggle, a feature which can block only these pesky
numbers or the numbers which are starting with certain prefix or which have appeared a lot
in the call logs", "allowing the other numbers which could be important, let's say from a
recruiter"; and, asked what he meant: "not a number but a class of numbers which look very
similar and appear every day or appear very commonly". On the backend: "figure out which
is more feasible". The plan it is sold in is ADR-011.

## Context

A blocker that stops every unknown number costs its user the recruiter, the clinic and the
courier, and one that stops nothing costs them the loan desk that calls from forty numbers
of one block. The pests on a phone in India are classes: a call centre is given a block of
a thousand numbers and rotates through it; a bank's service desk has its lines on one
exchange. Google's own Phone app warns of callers its servers know about; Truecaller
blocks from a database its users feed, which needs the user's contacts and an account.
Neither knows which classes keep calling *this* phone.

The app already sees every call Android screens: the number and the moment (ADR-002), and
keeps its own record of the calls it stopped (ADR-003). What it has never kept is the
calls it let ring.

## Decision

- **The frequent callers are found on the phone, by arithmetic over the app's own record.**
  `core/frequent/FrequentCallers.kt`, pure Kotlin with tests: a class is a set of numbers
  that differ only in their last three digits (a thousand numbers), or their last four where
  the callers spread wider, with at least three different numbers, six calls and three
  different days in the last sixty days; a single number that calls on three different days
  is listed on its own. What the user has dealt with is left out: a number they called or
  allowed, anything one of their number rules covers, a class they have blocked. India's
  reserved series are left out: the 140 series has its own switch and the 160 series always
  rings. The thresholds are constants, in one place.
- **No backend, no account, no upload.** Nothing a server would add is worth what it costs:
  a list shared across users would need every user's calls sent off the phone, consent for
  it, a Data safety declaration, the law on personal data in India, a server bill, and an
  account to tie a subscription to. The one thing a backend would be for, verifying a
  purchase, Google Play already does for a client-only app (ADR-009). If a shared list is
  ever wanted it is a new product decision with a new ADR, not an extension of this one.
- **The app now notes every call it is asked about while it is on**, allowed or not, in a
  second table, `seen_calls`: the number's key and the moment, nothing else. Nothing is
  written while the mode in effect is Off, a pause included: at Off the app is out of the
  way. The record is trimmed to sixty days with every call written; History never shows it;
  Delete All in History clears it; it is not backed up. This amends ADR-003, whose "calls it
  allows are not recorded" held for the history and still does.
- **A blocked frequent caller is a rule about how a number starts**, placed by length among
  the user's own number rules and the series (`RuleBook.startRules`), and it gets the
  lever's action: silenced at Silence, cut off at Block. No automatic pass lets it through.
  The user's own number rule comes first at the same length, and a longer Always Ring rule
  inside a blocked class lets that one number through.
- **A third scope, Frequent Only**: the lever's stop applies to the blocked frequent callers
  alone, and every other unknown number rings (`RuleBook.OTHERS_OUT_OF_SCOPE`). This is the
  "toggle" of the ask: Block, with the scope at Frequent Only, is "block only these".
- **Nothing is blocked without the user's word** unless they switch Auto-block on, which is
  off as installed. A newly found caller is announced once, in a quiet notification that
  opens the list; with Auto-block on it is blocked at that moment and the notification says
  so. A caller announced once is never announced again.
- **The list is every plan's to read.** Blocking one, Auto-block and the scope are Plus's
  (ADR-011). Without Plus the blocked list is as if empty and the scope reads as all unknown
  numbers, the lever's plain meaning; nothing stored is thrown away.
- **The words.** "Frequent callers", never "spam": the app only counts, and TRAI forbids
  tagging the reserved series. A row is the number or the shared start with an ellipsis,
  and its figures: calls, numbers, days.

## Consequences

Positive:

- The difference a user can name, for Play's Repetitive Content rule: the app blocks the
  callers that keep calling *them*, from their own phone's record, and lets the rest ring.
- No new permission, no new Data safety row, no server, and the privacy claim stays
  literally true.
- The finder is pure and tested; the thresholds can be changed in one place after launch
  data.

Negative:

- **It counts; it does not know.** A recruiter's agency and a loan desk look the same in
  the record. The user decides, with the figures in front of them, and the undo is one
  press; Auto-block is their choice.
- **Cold start.** Nothing is listed until three days of calls from one class have been seen,
  and nothing at all for a user who keeps the lever at Off, since nothing is noted then.
- **A class covers a thousand numbers.** A wanted caller inside a blocked class is stopped;
  History shows the call with its reason, and the allow list or an Always Ring rule brings
  it back.
- **The record is wider than before**: numbers that rang are now kept for sixty days. The
  privacy page must say so (launch plan 3.5).
- **Outside India** the class widths are the same three and four digits; whether that suits
  other numbering plans is unknown.
