---
status: Approved
number: "0009"
title: Feed Screen Redesign
depends_on: ["0006"]
blocks: []
created: 2026-05-10
updated: 2026-05-10
owner: ""
---

# 0009 — Feed Screen Redesign

## Context

Spec 0002 implemented the feed screen with a `TopAppBar`, drawer toggle, and a `LazyColumn` of `PaperCard` items. The Figma design refresh (May 2026) introduces a custom top bar with centered title + subtitle layout, and a text-only "Load more papers" button at the end of the list.

This spec covers the feed screen **chrome** (top bar + list container + load-more button). The `PaperCard` visual redesign is covered by a separate spec.

## Goal

Update the feed screen layout and top bar to match the Figma design while preserving all existing ViewModel behavior, navigation, drawer interaction, and bottom navigation integration.

## Non-goals

- Redesigning `PaperCard` internals (separate spec)
- Changing `FeedViewModel`, `FeedState`, or `FeedRepository` logic
- Adding pull-to-refresh changes (already implemented)
- Adding search or filter functionality

## Functional requirements

- RF1: The top bar is a custom composable (not `TopAppBar`) with a fixed height, white/card background, and a bottom border (`HorizontalDivider`).
- RF2: Top bar layout is a `Row` with three slots:
  - **Left**: a hamburger icon button (`Icons.Filled.Menu`, 24×24dp) wrapped in a circular touch target. Opens the `ModalNavigationDrawer`.
  - **Center** (weight 1f, centered): Title "Paperstack" (`titleMedium`, `FontWeight.SemiBold`) and the current category name below it (`labelSmall`, `onSurfaceVariant`).
  - **Right**: an invisible `Spacer(Modifier.size(40.dp))` to balance the row so the title stays centered.
- RF3: The paper list is a `LazyColumn` filling the remaining space (`Modifier.weight(1f)` or `fillMaxSize` in the Scaffold content area), with `contentPadding = PaddingValues(Spacing.md)` and `verticalArrangement = spacedBy(Spacing.md)`.
- RF4: At the end of the list, a text-only "Load more papers" button appears when `state.canShowLoadMore` is true — styled as full-width, `bodySmall`, `FontWeight.Medium`, primary-colored text, no background, rounded-lg ripple. Tapping calls `onLoadMore`.
- RF5: The existing `PullToRefreshBox` behavior is preserved.
- RF6: The bottom navigation bar (`NavigationBar` in `MainActivity`) remains unchanged — the feed screen content renders above it.

## Contracts

### Top bar composable

```kotlin
@Composable
private fun FeedTopBar(
    currentCategory: String,
    onMenuClick: () -> Unit,
)
```

### Key visual mapping from Figma → Compose

| Figma / Tailwind | Compose |
|---|---|
| `bg-card border-b border-border` top bar | `Surface(color = colorScheme.surface)` + `HorizontalDivider()` below |
| `px-4 py-4` top bar padding | `Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md)` |
| `p-2 hover:bg-muted rounded-full` menu button | `IconButton` (built-in circular ripple) |
| `Menu` icon `w-6 h-6` | `Icon(Icons.Filled.Menu, Modifier.size(24.dp))` |
| `font-semibold text-lg` title | `MaterialTheme.typography.titleMedium`, `FontWeight.SemiBold` |
| `text-xs text-muted-foreground` subtitle | `MaterialTheme.typography.labelSmall`, `onSurfaceVariant` |
| `w-10` right spacer | `Spacer(Modifier.size(40.dp))` |
| `p-4 space-y-4` list container | `contentPadding = PaddingValues(Spacing.md)`, `spacedBy(Spacing.md)` |
| `text-sm text-primary font-medium` load more | `TextButton` with `bodySmall`, `FontWeight.Medium`, primary color |
| `hover:bg-primary/5 rounded-lg` load more | Default `TextButton` ripple + `RoundedCornerShape(8.dp)` |

## Acceptance criteria

- [ ] AC1: The top bar shows the hamburger icon on the left and "Paperstack" centered with the current category name below.
- [ ] AC2: The right side of the top bar has a 40dp spacer to visually center the title.
- [ ] AC3: A `HorizontalDivider` separates the top bar from the list.
- [ ] AC4: The paper list uses `Spacing.md` (16dp) padding and 16dp spacing between items.
- [ ] AC5: "Load more papers" appears as a full-width text button at the end of the list when more results are available.
- [ ] AC6: The hamburger icon opens the `ModalNavigationDrawer`.
- [ ] AC7: Pull-to-refresh continues to work.
- [ ] AC8: Bottom navigation bar remains visible on the feed screen.
- [ ] AC9: No regression in feed loading, prefetching, or category switching.

## Risks

| Risk | Mitigation |
|------|-----------|
| Custom top bar doesn't integrate with `Scaffold` `topBar` slot | Use `Column` layout: custom bar on top, scrollable content below — or pass custom bar as `topBar` content |
| Center alignment breaks when category name is very long | Use `maxLines = 1` + `overflow = TextOverflow.Ellipsis` on the category subtitle |
| `PullToRefreshBox` wrapping `LazyColumn` conflicts with custom layout | Keep `PullToRefreshBox` wrapping only the `LazyColumn`, below the custom top bar |

## References

- Figma React source: `FeedScreen` component (May 2026)
- Depends on: spec 0006 (design system — `Spacing`, color tokens, typography)
- Existing implementation: `ui/feed/FeedScreen.kt`
- `PaperCard` redesign: covered by a future spec
