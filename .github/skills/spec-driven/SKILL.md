---
name: spec-driven
description: Spec-Driven Development workflow. Use when user asks to "create spec", "new spec", "approve spec", "implement spec", "/spec", or when starting work on any non-trivial change in a repository that uses docs/specs/. Enforces immutable numbering, status lifecycle (Draft → Approved → In Progress → Implemented → Deprecated), and index synchronization.
---

# Spec-Driven Development (SDD)

Specs are **executable contracts**, not documentation. They are written
**before** code, reviewed like code, and must stay in sync with reality.

## When to use this skill

ALWAYS load this skill when:
- User asks to create, approve, implement, or deprecate a spec
- User says "/spec", "new spec", "draft spec", "spec for X"
- Starting non-trivial work in a repo that has `docs/specs/`
- Modifying spec status or updating the spec index

DO NOT use for:
- Trivial fixes (typos, single-line changes)
- Pure research/exploration tasks (use proposals/ instead)
- Code that does not change observable behavior

## Core principles

1. **Numbering is immutable.** Once `0042` exists, never reuse, never renumber.
2. **Index is the source of truth.** Every spec MUST appear in `docs/specs/README.md`.
3. **Status drives action.** Implementation only starts when status is `Approved`.
4. **Acceptance criteria must be binary.** Each AC must be verifiable as pass/fail.
5. **Dependencies are explicit.** A spec lists `depends_on` and `blocks` in its header.
6. **Specs ≠ Proposals.** Proposals analyze; specs commit.

## Status lifecycle

```
Draft ──review──▶ Approved ──work──▶ In Progress ──merge──▶ Implemented
                                                              │
                                                              ▼
                                                         Deprecated
```

- **Draft:** being written; safe to change freely
- **Approved:** contract locked; ACs are binary; work can start
- **In Progress:** at least one PR open implementing it
- **Implemented:** all ACs verified; PRs merged; index updated
- **Deprecated:** superseded; kept for history; index marks it

## Workflow: Creating a new spec

1. **Read the index** at `docs/specs/README.md` to find the next available number
   - Look at the highest number in the index across ALL families
   - Reserve number gaps for related work (e.g., reserve 0010-0019 for a family)
2. **Copy the template** from `docs/specs/_template/` to `docs/specs/NNNN-slug/`
   - Slug is kebab-case, ≤4 words, descriptive
3. **Fill `spec.md`** in this order:
   - Header (status: Draft, depends_on, blocks)
   - Context: WHY this exists (link proposals/incidents)
   - Goal: ONE sentence
   - Non-goals: prevent scope creep
   - Functional requirements (RF1, RF2, ...)
   - Contracts: API/UI — concrete, copy-pasteable
   - Acceptance criteria: binary checklist
   - Risks
4. **Fill `plan.md`** if implementation spans >1 file or >1 screen
5. **Fill `tasks.md`** if implementation has >3 discrete steps
6. **Update `docs/specs/README.md`** index:
   - Add row to the appropriate family table with status `Draft`
   - If new family, add a new section
7. **Confirm with user** before moving status to `Approved`

## Workflow: Approving a spec

Before flipping status to `Approved`, verify:

- [ ] Acceptance criteria are testable (no "should be fast"; use specific conditions)
- [ ] Contracts (API shapes, UI wireframe, data model) are concrete
- [ ] `depends_on` lists specs that must reach `Implemented` first
- [ ] No circular dependencies in the chain
- [ ] Owner is assigned and committed to a rough timeline
- [ ] Risks have mitigations (not just listed)

If any item fails: keep as `Draft`, list gaps, ask user.

## Workflow: Implementing a spec

1. Verify all `depends_on` specs are `Implemented`
   - If not: STOP. Surface the blocker to the user.
2. Update spec status to `In Progress` and `updated:` date
3. Update index in `docs/specs/README.md`
4. Work through `tasks.md` checklist in order
5. Each task gets its own commit/PR when possible
6. Tests cover every acceptance criterion
7. On merge, update spec to `Implemented`, link PRs in footer
8. Update index
9. If new follow-up work emerges: create new spec, do NOT expand scope of current one

## Workflow: Deprecating a spec

- Set status to `Deprecated`
- Add header field `superseded_by: NNNN` if applicable
- Add reason at top of spec body
- Keep the file (numbering is immutable)
- Update index

## Anti-patterns to refuse

- ❌ Renumbering specs to "make it tidy"
- ❌ Implementing a `Draft` spec ("we'll approve it later")
- ❌ Creating a spec without updating the index
- ❌ Vague ACs like "must be performant" or "should work well"
- ❌ Specs that describe how but not why
- ❌ Inflating one spec to cover multiple unrelated changes (one spec = one focused contract)
- ❌ Treating specs as documentation written after code

## When in doubt

Ask the user. Specs are commitments — better to clarify than to commit incorrectly.

## Reference files in this repo

- Index: `docs/specs/README.md`
- Template: `docs/specs/_template/`
