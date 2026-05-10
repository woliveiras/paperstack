# PRD: Browse by Category

## Status

Approved

## Problem statement

Researchers, students, professors, and industry professionals who follow scientific literature have no simple, mobile-friendly way to view the latest arXiv papers filtered by the categories they care about. The arXiv website is not optimized for mobile and mixes all categories by default.

## Design principle: local-first

Paperstack is **fully local-first**. There are no accounts, no login, no backend, and no third-party auth integrations. All user preferences and saved data are stored on-device (AsyncStorage). The only external communication is read-only fetches to the arXiv public API.

## Goals

- [ ] The user completes onboarding (name + categories) and lands on the feed without friction
- [ ] The user can select one or more arXiv categories during onboarding
- [ ] The user can switch between their selected categories via a Drawer menu
- [ ] The user can add or remove categories at any time from the category selection screen
- [ ] The user can browse the latest papers from the active category
- [ ] The user can read a paper's abstract directly in the app
- [ ] The user can save papers they find interesting to revisit later
- [ ] The user can download a paper's PDF for offline reading
- [ ] The experience works without errors from first access onwards

## Non-goals

- Out of scope: authentication, accounts, or any login flow
- Out of scope: any server-side backend or cloud sync
- Out of scope: search by text or author
- Out of scope: a single merged feed across all selected categories
- Out of scope: push notifications

## Personas

| Persona | Profile |
|---------|---------|
| Researcher | Follows one or more arXiv categories daily to stay up to date |
| Industry professional | Wants to keep up with scientific studies without navigating the arXiv website |
| Student / Professor | Uses arXiv as a reference source and wants quick access to the latest papers |

## User stories

1. As a first-time user, I want to tell the app my name and pick my arXiv categories during onboarding so that my feed is immediately relevant.
2. As a researcher, I want to switch between my saved categories using a Drawer menu so that I can read each topic separately.
3. As a user, I want to add or remove categories at any time so that my reading list stays relevant as my interests evolve.
4. As a researcher, I want to see the latest papers from the active category so that I can stay up to date quickly.
5. As a user, I want to read a paper's abstract before opening it so that I can decide if the full read is worth it.
6. As a user, I want to save a paper to my reading list so that I can come back to it later.
7. As a user, I want to download a paper's PDF to my device so that I can read it offline at any time.
8. As a user, I want the app to work without errors from start to finish so that I can trust it as a tool.

## Proposed solution

### User journey (v1)

```
First launch
  └─▶ Onboarding
        ├─ Enter display name
        └─ Pick one or more arXiv categories (checkboxes, no limit, min 1)
              └─▶ Feed screen (active category)
                    ├─ [☰] Drawer menu — swipe or tap to open
                    │       ├─ cs.AI  ← active (highlighted)
                    │       ├─ cs.PL
                    │       ├─ cs.SE
                    │       └─ [+ Add categories] → category selection screen
                    │                                 (pre-checks existing selections)
                    ├─ List of latest 15 papers ("Load more" for next page)
                    ├─ Each card: title, authors, date, abstract excerpt,
                    │            conference/journal when available
                    └─ Tap paper
                          ├─ Full abstract view
                          ├─ Save to reading list
                          └─ Download PDF to device
```

### Local-first storage

All data lives on-device:
- Display name and selected categories → DataStore Preferences via `SettingsDataStore`
- Active category (last viewed) → DataStore Preferences via `SettingsDataStore`
- Saved papers → Room database via `SavedPaperRepository`
- Downloaded PDFs → device file system via `DownloadManager` / `ContentResolver`

No backend. No sync. No account required.

### Feed and Drawer

The feed fetches from the arXiv API using the currently active category. The user switches categories via a Drawer that slides in from the left — accessible by swiping right from the feed or tapping the menu icon. The Drawer lists all selected categories; the last item is always "+ Add categories", which opens the category selection screen with previously chosen categories pre-selected. Switching categories resets the feed for the new category.

### v2 note

In v2, the user will be able to select **multiple categories**. The feed will merge and sort results by submission date. This is explicitly out of scope for v1.

## Success metrics

| Metric | Criterion |
|--------|-----------|
| Zero crashes | No crash from first access through downloading a PDF |
| Onboarding completed | User reaches the feed after onboarding with no errors |
| Papers loaded | List displays ≥1 paper from the category within 5 seconds on a normal connection |
| Save works | Paper appears in reading list immediately after saving |
| PDF downloaded | PDF file is saved to device and accessible offline |

## Open questions

All resolved. ✅

| Question | Decision |
|----------|----------|
| Papers per page? | 15 |
| Default category? | None — user chooses on first launch (mandatory onboarding) |
| Pagination or infinite scroll? | "Load more" button |
| Conference/journal on card? | Yes, whenever available in the `arxiv:comment` field |

## References

- arXiv API skill: [.github/skills/arxiv-api/SKILL.md](../../.github/skills/arxiv-api/SKILL.md)
- arXiv API docs: https://info.arxiv.org/help/api/index.html

