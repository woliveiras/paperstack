# Category Navigation: Drawer with Per-Category Feeds

* Status: Accepted
* Date: 2026-05-09
* Deciders: William Oliveira

## Context and Problem Statement

Users can follow multiple arXiv categories. The app must let them switch between categories while reading. How should the category navigation be structured so the UX is fluid and the pattern is familiar?

## Decision Drivers

* Users may follow many categories (no upper limit) — a tab bar overflows at 5+ items.
* Switching categories should not disrupt the reading flow.
* The pattern should be familiar to users of similar apps (Feedly, Reeder, Reddit).
* Gesture-based access (swipe right) is expected on mobile.

## Considered Options

* **Option A — Tab bar:** One tab per category at the bottom. Familiar, but breaks at 4+ categories.
* **Option B — Dropdown/picker in header:** Compact, but hidden behind a tap; not discoverable via gestures.
* **Option C — Drawer (side menu):** Slides in from the left on swipe-right or menu icon tap. Scales to any number of categories. Last item is always "+ Add categories".

## Decision Outcome

Chosen option: **Option C — Drawer**, because it scales to unlimited categories, supports gesture navigation, and is a well-established pattern for multi-feed apps. Options A and B either break at scale or sacrifice discoverability.

### Positive Consequences

* No limit on how many categories a user can follow — the drawer scrolls.
* "+ Add categories" at the bottom of the drawer provides a natural, always-accessible entry point to manage subscriptions.
* Swipe-right is a familiar gesture on mobile readers.

### Negative Consequences

* The drawer pattern adds one layer of navigation compared to tabs.
* Category switching requires `feedStore.reset()` + `feedStore.fetchInitial()` — the feed re-loads when switching (no per-category cache in v1).

## Pros and Cons of the Options

### Option A — Tab bar

* Good, because always visible; zero taps to switch
* Bad, because overflows at 4+ categories; Material 3 tab bars don't handle many items well

### Option B — Dropdown/picker in header

* Good, because compact; single tap to open
* Bad, because not gesture-accessible; feels out of place in a reading app

### Option C — Drawer

* Good, because scales to any number of categories
* Good, because swipe-right is discoverable and familiar
* Good, because "+ Add categories" slot is always reachable without leaving the feed
* Bad, because one extra step vs. always-visible tabs

## References

- Spec 0002: [docs/specs/0002-paper-feed/spec.md](../../specs/0002-paper-feed/spec.md)
- PRD: [docs/prds/0001-browse-by-category.md](../../prds/0001-browse-by-category.md)
