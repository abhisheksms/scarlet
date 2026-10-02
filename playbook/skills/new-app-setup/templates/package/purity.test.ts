import { readdirSync, readFileSync, statSync } from "node:fs";
import { join } from "node:path";

/**
 * ADR-003 is enforced here rather than by convention. The engine is a seeded,
 * deterministic reducer: same seed plus same actions always yields the same
 * auction. Save/resume and season payoff both depend on it, and a single
 * `Date.now()` or `Math.random()` slipped into a reducer breaks them silently —
 * the auction still runs, it just stops being reproducible, and the failure
 * surfaces as a corrupted save days later.
 *
 * A grep is the right shape of test for this: the rule is about what the source
 * may mention at all, not about what any one function returns.
 */

const SRC = join(__dirname);

function engineSources(dir: string): string[] {
  return readdirSync(dir).flatMap((entry) => {
    const path = join(dir, entry);
    if (statSync(path).isDirectory()) return engineSources(path);
    if (!path.endsWith(".ts") || path.endsWith(".test.ts")) return [];
    return [path];
  });
}

const FILES = engineSources(SRC);

/** Comments are stripped before scanning: a docstring that says "never use
 * `Math.random`" is the rule being documented, not a breach of it. */
function code(file: string): string {
  return readFileSync(file, "utf8")
    .replace(/\/\*[\s\S]*?\*\//g, "")
    .replace(/\/\/.*$/gm, "");
}

/** Each rule is a pattern that must not appear in engine source, and the reason
 * a reader needs in order to understand why their change was rejected. */
const FORBIDDEN: { name: string; pattern: RegExp; why: string }[] = [
  { name: "Date.now", pattern: /\bDate\.now\b/, why: "reads the clock — the same seed must replay identically" },
  { name: "new Date", pattern: /\bnew Date\b/, why: "reads the clock — the same seed must replay identically" },
  { name: "Math.random", pattern: /\bMath\.random\b/, why: "unseeded randomness — all randomness goes through src/rng" },
  { name: "performance.now", pattern: /\bperformance\.now\b/, why: "reads the clock" },
  { name: "setTimeout / setInterval", pattern: /\bset(Timeout|Interval)\b/, why: "the engine names timing intent; the UI owns the clock" },
  { name: "fetch / XMLHttpRequest", pattern: /\b(fetch|XMLHttpRequest)\s*\(/, why: "the engine is offline and side-effect free" },
  { name: "localStorage / AsyncStorage", pattern: /\b(localStorage|AsyncStorage)\b/, why: "the engine never touches storage" },
  { name: "process.env", pattern: /\bprocess\.env\b/, why: "environment access makes a run irreproducible" },
];

/** Packages the engine may never import — React, storage, ads, billing, or any
 * sibling workspace package (the engine sits at the bottom of the graph). */
const FORBIDDEN_IMPORTS =
  /from\s+["'](react|react-native|expo[-/]?[^"']*|@cyan\/[^"']+|node:fs|fs|node:https?|https?)["']/;

describe("engine purity (ADR-003)", () => {
  it("scans a real, non-trivial set of engine sources", () => {
    // Guards the guard: if the walker silently returned nothing, every
    // assertion below would pass while checking absolutely nothing.
    expect(FILES.length).toBeGreaterThan(15);
    expect(FILES.some((f) => f.includes("reducer.ts"))).toBe(true);
  });

  it.each(FORBIDDEN)("never uses $name — $why", ({ pattern }) => {
    const offenders = FILES.filter((file) => pattern.test(code(file)));
    expect(offenders).toEqual([]);
  });

  it("never imports React, storage, ads, billing, or a sibling package", () => {
    const offenders = FILES.filter((file) => FORBIDDEN_IMPORTS.test(code(file)));
    expect(offenders).toEqual([]);
  });
});

describe("money is integer lakhs, never floats", () => {
  it("keeps every bid-increment result a whole number of lakhs", () => {
    // Floats in money are how a purse ends up at ₹0.0000001 and a legal bid
    // gets rejected. The tiered ladder is the place fractions would creep in.
    const { bidIncrement, nextBid } = require("./money/bid-increment");
    const { lakhs } = require("./money/money");
    for (let bid = 0; bid <= 3000; bid += 5) {
      expect(Number.isInteger(bidIncrement(lakhs(bid)))).toBe(true);
      expect(Number.isInteger(nextBid(lakhs(bid)))).toBe(true);
    }
  });
});
