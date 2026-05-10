# Specs

Feature specifications for Paperstack.

## Index

| # | Title | Status | Depends on | Blocks |
|---|-------|--------|------------|--------|
| [0001](0001-onboarding/spec.md) | Onboarding | Implemented | — | 0002 |
| [0002](0002-paper-feed/spec.md) | Paper Feed | Implemented | 0001 | 0003, 0004, 0005 |
| [0003](0003-paper-detail/spec.md) | Paper Detail | Implemented | 0002 | — |
| [0004](0004-saved-papers/spec.md) | Saved Papers | Implemented | 0002 | — |
| [0005](0005-pdf-download/spec.md) | PDF Download | Implemented | 0002 | — |
| [0006](0006-design-system/spec.md) | Design System | Implemented | — | — |
| [0007](0007-onboarding-redesign/spec.md) | Onboarding Screen Redesign | Implemented | 0006 | — |
| [0008](0008-category-selection-redesign/spec.md) | Category Selection Screen Redesign | Implemented | 0006 | — |
| [0009](0009-feed-screen-redesign/spec.md) | Feed Screen Redesign | Approved | 0006 | — |
| [0010](0010-bottom-nav-redesign/spec.md) | Bottom Navigation Redesign | Approved | 0006 | — |
| [0011](0011-paper-card-redesign/spec.md) | PaperCard Redesign | Approved | 0006 | — |
| [0012](0012-saved-screen-redesign/spec.md) | Saved Screen Redesign | Approved | 0006, 0011 | — |
| [0013](0013-detail-screen-redesign/spec.md) | Detail Screen Redesign | Approved | 0006 | — |
| [0014](0014-drawer-redesign/spec.md) | Drawer Redesign | Approved | 0006 | — |

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
