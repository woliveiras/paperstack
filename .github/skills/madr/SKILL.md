---
name: madr
description: >
  MADR (Markdown Any Decision Records) workflow. Use when recording an architecture
  or significant technical decision. Trigger phrases: "new adr", "architecture decision",
  "record decision", "write adr", "/madr".
---

# MADR — Markdown Any Decision Records

ADRs record **significant technical decisions** and the reasoning behind them.
They are not specs (which define features) — they answer: "Why did we choose X over Y?"

Reference: [MADR](https://adr.github.io/madr/)

## When to use

ALWAYS load this skill when:
- User says "/madr", "new adr", "architecture decision", "write adr"
- A significant technical choice must be recorded (library, pattern, infrastructure)
- A decision will be hard to reverse or will affect many files

DO NOT use for:
- Feature requirements → use `/spec`
- Product decisions → use `/write-prd`
- Trivial choices (naming, minor refactors)

## ADR numbering

- ADRs are stored in `docs/adrs/NNNN-slug/adr.md`
- Numbering is **immutable** — once NNNN exists, never reuse or renumber
- Read `docs/adrs/README.md` to find the next available number

## Status lifecycle

```
Proposed ──review──▶ Accepted ──supersede──▶ Deprecated
              │
              └─── Rejected
```

## MADR template

```markdown
# [Short title of the decision]

* Status: Proposed | Accepted | Rejected | Deprecated | Superseded by [ADR-NNNN](../NNNN-slug/adr.md)
* Date: YYYY-MM-DD
* Deciders: [names or roles]

## Context and Problem Statement

[Describe the context and the problem in 2–4 sentences. Frame as a question if helpful.]

## Decision Drivers

* [Driver 1 — a force or concern]
* [Driver 2]

## Considered Options

* Option A — [one-line description]
* Option B — [one-line description]
* Option C — [one-line description]

## Decision Outcome

Chosen option: **Option A**, because [brief justification — which driver it satisfies].

### Positive Consequences

* [consequence 1]

### Negative Consequences

* [consequence 1]

## Pros and Cons of the Options

### Option A

* Good, because [argument]
* Bad, because [argument]

### Option B

* Good, because [argument]
* Bad, because [argument]
```

## Workflow

1. **Read the index** at `docs/adrs/README.md` to find the next number
2. **Create** `docs/adrs/NNNN-slug/adr.md` from the template
3. **Fill in** context, drivers, options, and outcome
4. **Update the index** in `docs/adrs/README.md` with status `Proposed`
5. **Confirm with user** before moving to `Accepted`
6. **Update `AGENTS.md`** if the decision changes a project-wide convention

## Anti-patterns

- ❌ ADR without considered alternatives (no tradeoffs = no decision record)
- ❌ Recording trivial decisions ("we used camelCase")
- ❌ Renumbering ADRs
- ❌ Creating an ADR after the fact without documenting the actual reasoning
- ❌ Vague status ("done", "approved by team")

## Reference files in this repo

- Index: `docs/adrs/README.md`
- Template: `docs/adrs/_template/adr.md`
