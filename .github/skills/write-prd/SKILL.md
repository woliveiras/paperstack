---
name: write-prd
description: >
  Product Requirements Document (PRD) writing workflow. Use when the user wants
  to define a new feature, product area, or user journey before writing specs.
  Trigger phrases: "write prd", "product requirements", "new feature brief",
  "define feature", "/write-prd".
---

# Write PRD

A PRD defines **what** and **why** — not how. It is the input to the spec
(`/spec`) and, when a significant architecture choice is involved, to an ADR (`/madr`).

## When to use

ALWAYS load this skill when:
- User says "/write-prd", "write prd", "product requirements"
- User wants to define a new feature before creating specs
- User has a feature idea and needs to structure it before implementation

DO NOT use for:
- Detailed implementation planning → use `/spec`
- Architecture decisions → use `/madr`
- Bug fixes or trivial changes

## PRD template

```markdown
# PRD: [Feature Name]

## Status
Draft | Review | Approved

## Problem statement
[1–3 sentences. What user pain or opportunity does this address?]

## Goals
- [ ] Goal 1
- [ ] Goal 2

## Non-goals
- Not in scope: X
- Not in scope: Y

## User stories
As a [persona], I want to [action] so that [benefit].

1. As a reader, I want to filter papers by category so that I only see relevant content.
2. ...

## Proposed solution
[High-level description. No implementation details. Wireframe link if available.]

## Success metrics
| Metric | Target |
|--------|--------|
| ... | ... |

## Open questions
- [ ] Question 1?
- [ ] Question 2?

## References
- Related spec: [docs/specs/NNNN-slug](../specs/NNNN-slug/spec.md)
- Related ADR: [docs/adrs/NNNN-slug](../adrs/NNNN-slug/adr.md)
```

## Workflow

1. **Interview the user** to collect:
   - What problem are we solving?
   - Who is the user / persona?
   - What does success look like?
   - What is explicitly out of scope?
2. **Draft the PRD** using the template above
3. **Review open questions** — surface any that block the spec
4. **When approved**, create a spec with `/spec` that references this PRD
5. Store PRD in `docs/prds/` if the file is worth keeping; otherwise inline in the spec's context section

## PRD vs Spec

| PRD | Spec |
|-----|------|
| Defines what & why | Defines the contract (how to verify) |
| Written by product owner | Written by implementer |
| Prose + user stories | Requirements + ACs + contracts |
| No AC or implementation | Binary ACs, specific contracts |
| Input to specs | Output drives code |

## Anti-patterns

- ❌ Writing implementation details in a PRD
- ❌ Creating a spec before the PRD's goals are agreed
- ❌ Vague success metrics ("users should like it")
- ❌ Non-goals that are actually in scope
