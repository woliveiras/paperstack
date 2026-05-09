---
status: Approved
number: "0002"
title: Paper Feed
depends_on: ["0001"]
blocks: ["0003", "0004", "0005"]
created: 2026-05-09
updated: 2026-05-09
owner: ""
---

# 0002 — Paper Feed

## Context

Once onboarding is complete, the user's selected arXiv category is known. The feed screen fetches the latest papers from that category via the arXiv public API and presents them as a scrollable list of cards.

Driven by PRD: [docs/prds/0001-browse-by-category.md](../../prds/0001-browse-by-category.md)

## Goal

Display a paginated list of the latest arXiv papers for the user's active category, with a Drawer menu to switch between selected categories and a "Load more" button for pagination.

## Non-goals

- A single merged feed across all selected categories
- Search or filtering within the feed
- Offline caching of feed results

## Functional requirements

- RF1: The feed screen fetches papers from the arXiv API using `settingsStore.settings.activeCategory`.
- RF2: On initial load, **30 papers** are fetched. The first 15 are displayed immediately; the remaining 15 are held in a prefetch buffer.
- RF3: Each paper card displays: title, authors (up to 3, then "et al."), submission date, abstract excerpt (max 200 chars), and conference/journal info when available (`arxiv:comment`).
- RF4: When the user taps "Load more", the 15 buffered papers are appended to the visible list instantly. Simultaneously, the next 30 papers are fetched in the background to refill the buffer.
- RF5: The "Load more" button shows a subtle background-loading indicator only while the next batch is being fetched. It remains tappable — the buffered papers are available immediately.
- RF6: If the initial fetch fails, a non-blocking error message is shown with a "Retry" button.
- RF7: If the background prefetch fails, the buffer is empty. The next "Load more" tap triggers a fresh fetch (with a loading state) instead of showing an error proactively.
- RF8: All API calls go through `services/arxiv.ts`. Max 1 request per 3 seconds. `User-Agent` header is set.
- RF9: Tapping a paper card navigates to the paper detail screen (spec 0003).
- RF10: The "Load more" button is hidden when `visiblePapers.length + buffer.length >= totalResults` and the buffer is exhausted.
- RF11: A Drawer slides in from the left (swipe right or tap menu icon). It lists all categories from `settingsStore.settings.selectedCategories`. The active one is highlighted.
- RF12: Tapping a category in the Drawer sets it as `activeCategory` in `settingsStore` and resets the feed for that category.
- RF13: The last item in the Drawer is always "+ Add categories", which navigates to the category selection screen with existing selections pre-checked.
- RF14: The category name of the active category is shown in the feed screen header.

## Contracts

### Paper data model

```ts
// types/paper.ts
export interface Paper {
  id: string           // e.g. "2605.06667"
  title: string
  authors: string[]
  abstract: string
  submittedDate: string  // ISO 8601
  updatedDate: string    // ISO 8601
  pdfUrl: string         // e.g. "https://arxiv.org/pdf/2605.06667"
  categories: string[]   // e.g. ["cs.AI", "cs.LG"]
  primaryCategory: string
  comment?: string       // conference/journal info
}
```

### arXiv service

```ts
// services/arxiv.ts
interface FetchPapersParams {
  category: string
  start: number      // offset (0-based)
  pageSize: number   // always 30 (fetched in pairs of visible + buffer)
}

interface FetchPapersResult {
  papers: Paper[]
  totalResults: number
  startIndex: number
}

export async function fetchPapers(params: FetchPapersParams): Promise<FetchPapersResult>
```

### feedStore

```ts
// store/feedStore.ts
interface FeedStore {
  visiblePapers: Paper[]     // papers shown in the list
  buffer: Paper[]            // prefetched, not yet shown
  nextStart: number          // next API offset to fetch
  totalResults: number
  isLoading: boolean         // initial load
  isPrefetching: boolean     // background prefetch in progress
  error: string | null
  fetchInitial: () => Promise<void>  // uses activeCategory from settingsStore
  loadMore: () => Promise<void>
  reset: () => void          // called when activeCategory changes
}
```

**`loadMore` logic:**
1. Move `buffer` → append to `visiblePapers`; clear `buffer`
2. Fire background fetch for next 30 at `nextStart` (no await — user sees content immediately)
3. On background fetch success: split into first 15 → visible append, next 15 → buffer; advance `nextStart`

**Category switch logic:**
1. `settingsStore.setActiveCategory(code)` updates `activeCategory`
2. `feedStore.reset()` clears `visiblePapers`, `buffer`, `nextStart`, `totalResults`
3. `feedStore.fetchInitial()` fires for the new category

## Acceptance criteria

- [ ] AC1: The feed screen renders on first load with 15 paper cards (given network access).
- [ ] AC2: Each card shows title, at least one author, date, and abstract excerpt.
- [ ] AC3: Cards with `arxiv:comment` show the conference/journal info.
- [ ] AC4: Tapping "Load more" shows the buffered 15 papers instantly (no loading delay).
- [ ] AC5: After "Load more", a background fetch starts to refill the buffer.
- [ ] AC6: "Load more" button is hidden when `visiblePapers.length >= totalResults` and the buffer is empty.
- [ ] AC7: A loading spinner is shown on initial fetch; 15 cards are shown on success.
- [ ] AC8: A retry button appears when the initial fetch fails.
- [ ] AC9: A silent background prefetch failure does not show an error; the next "Load more" triggers a fresh fetch with a loading state.
- [ ] AC10: `services/arxiv.ts` — `fetchPapers` is covered by unit tests with mocked `fetch` (success, empty result, 503 retry).
- [ ] AC11: `feedStore` — `fetchInitial` and `loadMore` buffer logic are covered by unit tests.
- [ ] AC12: The Drawer opens on swipe-right and on tapping the menu icon.
- [ ] AC13: The Drawer lists all categories from `settingsStore.settings.selectedCategories`; the active one is visually highlighted.
- [ ] AC14: Tapping a category in the Drawer updates `activeCategory`, closes the Drawer, resets the feed, and loads papers for the new category.
- [ ] AC15: The active category name is shown in the feed header.
- [ ] AC16: Tapping "+ Add categories" in the Drawer opens the category selection screen with existing selections pre-checked.

## Risks

| Risk | Mitigation |
|------|-----------|
| arXiv rate limit hit on rapid "Load more" taps | Debounce `loadMore`; background prefetch respects the 1 req/3s rule |
| Buffer out of sync with `totalResults` | Recalculate remaining count from `totalResults - nextStart` on each fetch |
| XML parsing breaks on malformed response | Unit test with fixture XMLs including edge cases |

## References

- PRD: [docs/prds/0001-browse-by-category.md](../../prds/0001-browse-by-category.md)
- ADR: [docs/adrs/0001-feed-prefetch-buffer](../../adrs/0001-feed-prefetch-buffer/adr.md)
- ADR: [docs/adrs/0002-drawer-category-navigation](../../adrs/0002-drawer-category-navigation/adr.md)
- Depends on: spec 0001 (onboarding)
- Blocks: spec 0003 (paper detail), spec 0004 (saved papers), spec 0005 (pdf download)
- arXiv API skill: [.github/skills/arxiv-api/SKILL.md](../../../.github/skills/arxiv-api/SKILL.md)
