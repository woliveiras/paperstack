# Feed Pagination: Prefetch Buffer Strategy

* Status: Accepted
* Date: 2026-05-09
* Deciders: William Oliveira

## Context and Problem Statement

The feed screen loads arXiv papers in pages. With a naive "fetch on demand" approach, tapping "Load more" triggers a network request and the user waits for the response before seeing new content. Given the arXiv API latency (variable, typically 500ms–2s), this creates a noticeable pause that hurts the perceived performance.

How should we paginate the feed so that "Load more" feels instant to the user?

## Decision Drivers

* The arXiv API enforces a 1 request per 3 seconds rate limit — we cannot fire requests freely.
* Perceived performance matters more than actual latency for a reading app.
* The solution must remain simple enough to unit test reliably.

## Considered Options

* **Option A — Fetch on demand (naive):** Fetch 15 papers each time "Load more" is tapped. User waits for each fetch.
* **Option B — Prefetch buffer:** Fetch 30 papers at a time. Show the first 15 immediately; buffer the next 15. On "Load more", flush the buffer to the screen instantly and trigger the next background fetch.
* **Option C — Infinite scroll with prefetch:** Same as B but triggered by scroll position rather than a button.

## Decision Outcome

Chosen option: **Option B — Prefetch buffer**, because it eliminates perceived latency on "Load more" while keeping a predictable, button-triggered UX. Option A fails on perceived performance. Option C (infinite scroll) was rejected because auto-triggered fetches are harder to control within the rate limit and can fire unexpectedly.

### Positive Consequences

* "Load more" shows content instantly — no spinner, no wait.
* Background fetch is decoupled from the user interaction — rate limit is easy to enforce.
* The buffer is a simple `Paper[]` slice, straightforward to test.

### Negative Consequences

* The first load fetches 30 papers instead of 15 — slightly more data on startup.
* Store logic is more complex: `visiblePapers` + `buffer` + `nextStart` instead of a single `papers` array.
* A failed background prefetch must be handled silently and retried on next "Load more".

## Pros and Cons of the Options

### Option A — Fetch on demand

* Good, because simplest store model
* Bad, because every "Load more" tap has visible latency

### Option B — Prefetch buffer

* Good, because "Load more" is instant (buffer flush)
* Good, because background fetches are easy to rate-limit
* Bad, because store has two lists to manage (`visiblePapers` + `buffer`)

### Option C — Infinite scroll with prefetch

* Good, because no button needed
* Bad, because scroll-position triggers are harder to debounce within the 1 req/3s limit
* Bad, because auto-fetch on scroll can interfere with reading flow

## References

- Spec 0002: [docs/specs/0002-paper-feed/spec.md](../../specs/0002-paper-feed/spec.md)
