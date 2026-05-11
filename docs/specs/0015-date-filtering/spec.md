---
status: Draft
number: "0015"
title: Date filtering for papers
depends_on: []
blocks: []
created: 2026-05-11
updated: 2026-05-11
owner: ""
---

# 0015 — Date filtering for papers

## Context

Users need to filter papers by date range to find recent papers. Currently, the app shows all papers sorted by submission date with no date filtering capability.

## Goal

Allow users to filter papers by date range using a date picker, with a default of showing papers from the last 3 days.

## Non-goals

- Client-side filtering (use arXiv API's native date filters)
- Multiple date range presets beyond custom range
- Filtering saved papers (out of scope)

## Functional requirements

- RF1: Users can set a "from" date to see papers submitted on or after that date
- RF2: Users can set a "to" date to see papers submitted on or before that date
- RF3: Default date range is today minus 3 days (last 3 days)
- RF4: Filter UI is hidden behind a filter icon in the top bar
- RF5: Changing the date filter refreshes the feed with new results
- RF6: Date filter is **per-category** — each category remembers its own date range
- RF7: Users can clear the date filter to show all papers (no date limit)

## Contracts

### Data model

```kotlin
// FetchPapersParams (data/remote/ArxivApiService.kt)
data class FetchPapersParams(
    val category: String,
    val start: Int,
    val pageSize: Int = 30,
    val fromDate: String? = null,  // YYYY-MM-DD format
    val toDate: String? = null,
)

// FeedState (ui/feed/FeedState.kt)
data class FeedState(
    // ... existing fields ...
    val fromDate: LocalDate? = null,  // null means "no filter" (show all)
    val toDate: LocalDate? = null,
)
```

### API query

```
GET https://export.arxiv.org/api/query?search_query=cat:cs.AI+date-from:2026-05-08&sortBy=submittedDate&sortOrder=descending&start=0&max_results=30
```

### UI layout

```
+------------------------------------------+
| [Menu] PaperStack / cs.AI       [Filter] |
+------------------------------------------+
|                                          |
|  <Paper cards...>                        |
|                                          |
+------------------------------------------+

[Filter] click opens DatePickerDialog:
+------------------------------------------+
|  Filter by date                   [X]    |
+------------------------------------------+
|  From:  [    Date Picker    ]            |
|  To:    [    Date Picker    ]            |
+------------------------------------------+
|  [Clear]                    [Cancel]     |
|  [Apply]                                 |
+------------------------------------------+
```

**Note**: "Clear" button resets both dates to null (show all papers). "Apply" uses default (last 3 days) if both dates are null.

## Acceptance criteria

- [ ] AC1: Feed shows papers from last 3 days by default on app launch
- [ ] AC2: Tapping filter icon opens date picker dialog
- [ ] AC3: Setting "from" date filters papers submitted on or after that date
- [ ] AC4: Setting "to" date filters papers submitted on or before that date
- [ ] AC5: "Apply" button triggers a refresh of the feed with filtered results
- [ ] AC6: Changing dates clears any cached results for that category
- [ ] AC7: Date picker uses Material 3 components
- [ ] AC8: "Clear" button removes date filter and shows all papers
- [ ] AC9: Each category has its own date filter state

## Risks

| Risk | Mitigation |
|------|-----------|
| arXiv API date format may differ | Use YYYY-MM-DD format which matches API spec |
| Invalid date range (from > to) | Let API return empty results (natural behavior) |
| Cached results don't account for date filter | Clear category cache when filter changes |
| Default (last 3 days) shows no papers on fresh install | Handle empty state gracefully |

## References

- arXiv API query documentation
- Existing FeedScreen implementation
- Material 3 DatePicker component