---
status: Approved
number: "0011"
title: PaperCard Redesign
depends_on: ["0006"]
blocks: []
created: 2026-05-10
updated: 2026-05-10
owner: ""
---

# 0011 — PaperCard Redesign

## Context

`PaperCard` is the shared composable used in the feed list, saved list, and (by reference) the detail screen navigation. The Figma redesign (May 2026) updates the card layout: outlined card (thin border, no elevation), stacked vertical layout with title + bookmark row, authors line, date badge chip, and truncated abstract.

This spec covers the visual update of the `PaperCard` composable only. It is used by both feed and saved screens.

## Goal

Update `PaperCard` styling to match the Figma design while keeping its API, data contract, and bookmark toggle behavior unchanged.

## Non-goals

- Changing `Paper` domain model
- Changing bookmark/save ViewModel logic
- Adding swipe-to-dismiss or other gestures
- Moving `PaperCard` to `ui/components/` (can be done later)

## Functional requirements

- RF1: The card uses an outlined style — thin `1.dp` border in `outlineVariant` color, `RoundedCornerShape(12.dp)`, no elevation/shadow.
- RF2: The card has internal padding of `Spacing.md` (16dp).
- RF3: Top row: paper title (`titleMedium`, `FontWeight.SemiBold`, `onSurface`, max 3 lines, ellipsis) on the left (`weight(1f)`), and a bookmark `Icon` (not `IconButton`) on the right — `Icons.Filled.Bookmark` tinted `onSurface` when saved, `Icons.Outlined.BookmarkBorder` tinted `onSurfaceVariant` when not saved. The icon is 24×24dp and is tappable via `Modifier.clickable` on the icon itself.
- RF4: Below the title row, authors text: up to 3 names + "et al." — `bodyMedium`, `onSurfaceVariant`.
- RF5: Below authors, a date badge chip showing `submittedDate.take(10)` (e.g. "2026-05-07") — `labelSmall` text inside a small rounded container (`RoundedCornerShape(4.dp)`) with `surfaceVariant` background and `onSurfaceVariant` text. No icon in the chip.
- RF6: Below the date chip, the abstract preview — `bodyMedium`, `onSurface`, max 4 lines, ellipsis. Truncated to 200 chars + "…".
- RF7: Vertical spacing between elements: 6dp between title row and authors, 8dp between authors and date chip, 12dp between date chip and abstract.
- RF8: Tapping the card (excluding the bookmark icon) calls `onClick`.
- RF9: The conference/comment badge (current `labelLarge`, primary color) is preserved if `paper.comment` is not blank — shown between the date chip and abstract.

## Contracts

### Composable signature (unchanged)

```kotlin
@Composable
fun PaperCard(
    paper: Paper,
    isSaved: Boolean,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    modifier: Modifier = Modifier,
)
```

### Key visual mapping from Figma → Compose

| Figma / Screenshot | Compose |
|---|---|
| Outlined card, thin border, rounded corners | `OutlinedCard(shape = RoundedCornerShape(12.dp))` or `Card` with `CardDefaults.outlinedCardColors()` + `CardDefaults.outlinedCardBorder()` |
| Title `font-semibold text-lg` | `titleMedium`, `FontWeight.SemiBold` |
| Bookmark icon `w-5 h-5` filled/outlined | `Icon(modifier = Modifier.size(24.dp))`, `Icons.Filled.Bookmark` / `Icons.Outlined.BookmarkBorder` |
| Authors `text-sm text-muted-foreground` | `bodyMedium`, `onSurfaceVariant` |
| Date `text-xs` in pill/chip | `labelSmall` inside `Surface(shape = RoundedCornerShape(4.dp), color = surfaceVariant)` |
| Abstract `text-sm` truncated 4 lines | `bodyMedium`, `maxLines = 4`, `TextOverflow.Ellipsis` |
| Card padding | `Modifier.padding(Spacing.md)` (16dp all sides) |

### Date chip composable

```kotlin
@Composable
private fun DateChip(date: String) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}
```

## Acceptance criteria

- [ ] AC1: The card has an outlined border (1dp, `outlineVariant` color) with 12dp rounded corners and no elevation.
- [ ] AC2: The title is `titleMedium` semibold, max 3 lines with ellipsis.
- [ ] AC3: The bookmark icon is 24×24dp, shows filled when saved (tinted `onSurface`) and outlined when not saved (tinted `onSurfaceVariant`).
- [ ] AC4: Authors show up to 3 names + "et al." in `bodyMedium`, `onSurfaceVariant`.
- [ ] AC5: The date is displayed inside a small rounded chip with `surfaceVariant` background.
- [ ] AC6: The abstract is `bodyMedium`, max 4 lines, ellipsis, truncated to 200 chars.
- [ ] AC7: Tapping the card calls `onClick`; tapping the bookmark icon calls `onToggleSave`.
- [ ] AC8: The card renders identically in both feed and saved screens.
- [ ] AC9: Conference/comment text is preserved when `paper.comment` is not blank.

## Risks

| Risk | Mitigation |
|------|-----------|
| `OutlinedCard` border color doesn't match design token | Use `CardDefaults.outlinedCardBorder(borderBrush = SolidColor(outlineVariant))` |
| Bookmark `Icon` without `IconButton` loses touch target | Add `Modifier.clickable` with minimum `48.dp` touch target via `minimumInteractiveComponentSize()` |

## References

- Figma screenshots: feed + saved screens (May 10, 2026)
- Depends on: spec 0006 (design system — `Spacing`, color tokens, shape tokens)
- Current implementation: `ui/feed/FeedScreen.kt`, `PaperCard` composable (lines 205–273)
- Used by: FeedScreen, SavedScreen
