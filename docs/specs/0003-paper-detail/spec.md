---
status: Approved
number: "0003"
title: Paper Detail
depends_on: ["0002"]
blocks: []
created: 2026-05-09
updated: 2026-05-09
owner: ""
---

# 0003 — Paper Detail

## Context

When a user taps a paper card in the feed, they need to see the full abstract and take actions on that paper (save, download). This screen is the central hub for per-paper actions.

Driven by PRD: [docs/prds/0001-browse-by-category.md](../../prds/0001-browse-by-category.md)

## Goal

Display the full details of a paper and provide entry points to save it and download its PDF.

## Non-goals

- Rendering the PDF inline (PDF opens in external browser/viewer)
- Sharing or exporting paper metadata
- Comments or annotations

## Functional requirements

- RF1: The detail screen receives the `Paper` object via navigation params — no additional API call is made.
- RF2: The screen displays: title, all authors, submission date, primary category, all categories, full abstract, and conference/journal info when available.
- RF3: A "Save" button toggles the paper's saved state. If already saved, the button reads "Saved" (filled icon); otherwise "Save" (outline icon).
- RF4: A "Download PDF" button initiates the PDF download (delegates to spec 0005).
- RF5: A "Read online" button opens the arXiv abstract URL (`https://arxiv.org/abs/<id>`) in the system browser via `Intent.ACTION_VIEW`.
- RF6: The "Save" button state reflects the current `SavedPaperRepository.isSaved()` Flow on mount.

## Contracts

### Navigation params

```kotlin
// Navigation Compose route: "detail/{paperId}"
// Paper passed via NavBackStackEntry arguments or ViewModel SavedStateHandle
// DetailViewModel fetches Paper from SavedPaperRepository or feed cache by id
```

### Screen layout (logical)

```
[ Back button ]
[ Title ]
[ Authors ]
[ Date · Primary category ]
[ Conference/journal — if present ]
[ Abstract (full) ]
[ Divider ]
[ Save button ]  [ Download PDF button ]
[ Read online button ]
```

## Acceptance criteria

- [ ] AC1: The full abstract is visible without truncation.
- [ ] AC2: All authors are listed (no "et al." truncation on this screen).
- [ ] AC3: Conference/journal info is displayed when `comment` is present.
- [ ] AC4: The "Save" button shows the correct saved state on mount.
- [ ] AC5: Tapping "Save" on an unsaved paper saves it and updates the button to "Saved".
- [ ] AC6: Tapping "Saved" on a saved paper removes it and updates the button to "Save".
- [ ] AC7: Tapping "Read online" opens `https://arxiv.org/abs/<id>` in the system browser.
- [ ] AC8: Tapping "Download PDF" triggers the download flow (spec 0005).

## Risks

| Risk | Mitigation |
|------|-----------|
| Navigation param serialization limits | Keep `Paper` model lean; no binary data in params |

## References

- PRD: [docs/prds/0001-browse-by-category.md](../../prds/0001-browse-by-category.md)
- Depends on: spec 0002 (paper feed)
