# Specs

Feature specifications for Paperstack.

## Index

| # | Title | Status | Depends on | Blocks |
|---|-------|--------|------------|--------|
| [0001](0001-onboarding/spec.md) | Onboarding | Draft | — | 0002 |
| [0002](0002-paper-feed/spec.md) | Paper Feed | Draft | 0001 | 0003, 0004, 0005 |
| [0003](0003-paper-detail/spec.md) | Paper Detail | Draft | 0002 | — |
| [0004](0004-saved-papers/spec.md) | Saved Papers | Draft | 0002 | — |
| [0005](0005-pdf-download/spec.md) | PDF Download | Draft | 0002 | — |

## Families

### v1 — Browse by Category (PRD: [0001-browse-by-category](../prds/0001-browse-by-category.md))

```
0001-onboarding
  └─▶ 0002-paper-feed
        ├─▶ 0003-paper-detail
        ├─▶ 0004-saved-papers
        └─▶ 0005-pdf-download
```

---

To create a new spec, use the `/spec` skill.
