---
status: Implemented
number: "0004"
title: Saved Papers
depends_on: ["0002"]
blocks: []
created: 2026-05-09
updated: 2026-05-09
owner: ""
---

# 0004 — Saved Papers

## Context

Users want to bookmark papers they find interesting in the feed to revisit later. Saved papers must persist across app restarts, stored entirely on-device with no backend.

Driven by PRD: [docs/prds/0001-browse-by-category.md](../../prds/0001-browse-by-category.md)

## Goal

Allow users to save and unsave papers, and view all saved papers in a dedicated screen.

## Non-goals

- Syncing saved papers across devices
- Organizing saved papers into folders or tags
- Exporting the saved list

## Functional requirements

- RF1: Any paper can be saved from the feed card (long-press or dedicated button) or from the paper detail screen.
- RF2: Saved papers are persisted via `SavedPaperRepository` backed by Room (SQLite).
- RF3: A bottom nav item "Saved" shows the full list of saved papers.
- RF4: The saved list renders the same card component as the feed.
- RF5: Unsaving a paper from the saved list removes it immediately (optimistic update).
- RF6: If the saved list is empty, an empty state message is shown ("No saved papers yet").
- RF7: Tapping a saved paper card navigates to the paper detail screen (spec 0003).
- RF8: The save state is consistent across all screens — saving in the detail screen reflects immediately in the feed card and saved list.

## Contracts

### SavedPaperRepository

```kotlin
// data/repository/SavedPaperRepository.kt
interface SavedPaperRepository {
    fun getAll(): Flow<List<Paper>>
    suspend fun save(paper: Paper)
    suspend fun unsave(id: String)
    fun isSaved(id: String): Flow<Boolean>
}
```

Persistence: Room table `saved_papers` (see android skill for `SavedPaperEntity` definition)

## Acceptance criteria

- [ ] AC1: Saving a paper from the feed card adds it to `savedStore.savedPapers`.
- [ ] AC2: Saving a paper from the detail screen adds it to `savedStore.savedPapers`.
- [ ] AC3: Saved papers persist after the app is closed and reopened.
- [ ] AC4: The saved tab shows all saved papers.
- [ ] AC5: Unsaving a paper from the saved list removes it from the list immediately.
- [ ] AC6: The saved/unsaved state is consistent between the feed, detail screen, and saved list.
- [ ] AC7: An empty state is shown when no papers are saved.
- [ ] AC8: `SavedPaperRepository` — `save`, `unsave`, and `isSaved` are covered by unit tests with a mocked DAO.
- [ ] AC9: Room DAO (`SavedPaperDao`) is covered by an instrumented test with an in-memory database.

## Risks

| Risk | Mitigation |
|------|-----------|
| Stale paper data (saved paper's metadata becomes outdated) | Accept stale data in v1; refresh on open is v2 |
| Room database size | Acceptable for v1 paper count; monitor if needed |

## References

- PRD: [docs/prds/0001-browse-by-category.md](../../prds/0001-browse-by-category.md)
- Depends on: spec 0002 (paper feed)
