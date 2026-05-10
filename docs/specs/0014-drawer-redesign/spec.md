---
status: Implemented
number: "0014"
title: Drawer Redesign
depends_on: ["0006"]
blocks: []
created: 2026-05-10
updated: 2026-05-10
owner: ""
---

# 0014 — Drawer Redesign

## Context

The current drawer uses a M3 `ModalDrawerSheet` with `NavigationDrawerItem` items, showing category names with codes inline (e.g. "Artificial Intelligence (cs.AI)"). The Figma redesign (May 2026) introduces a user profile header (avatar circle + name), a "CATEGORIES" section label, two-line category items (name + code on separate lines), a highlighted background for the active category, and a "+ Add categories" text button at the bottom.

## Goal

Update the navigation drawer to match the Figma design while keeping all existing category switching and navigation logic.

## Non-goals

- Adding user profile editing
- Adding drawer gestures or animations beyond M3 defaults
- Changing `ModalNavigationDrawer` integration in `FeedScreen`

## Functional requirements

### User header

- RF1: Top section shows a circular avatar placeholder (48dp circle, `surfaceVariant` background) with a person icon (`Icons.Outlined.PersonOutline`, 24dp, `onSurfaceVariant`) on the left, and the user's name (`titleMedium`, `FontWeight.SemiBold`) to the right.
- RF2: Header has padding: `horizontal = Spacing.md`, `vertical = Spacing.md`.

### Categories section

- RF3: Section header "CATEGORIES" — `labelMedium`, `FontWeight.SemiBold`, uppercase, letter-spacing wide (2sp), `onSurfaceVariant`. Padding `horizontal = Spacing.md`, top 24dp, bottom 12dp. A thin `HorizontalDivider` above it (separating from the user header).
- RF4: Each category row shows:
  - **Name**: `bodyLarge`, `FontWeight.Medium`, `onSurface` — e.g. "Artificial Intelligence".
  - **Code**: `bodySmall`, `onSurfaceVariant` — e.g. "(cs.AI)".
  - Both lines stacked vertically in a `Column`.
- RF5: The **active category** row has a muted background highlight (`surfaceVariant` or `secondaryContainer`) with rounded corners (`RoundedCornerShape(8.dp)`).
- RF6: Inactive rows have transparent background.
- RF7: Tapping a row calls `onCategorySelected(code)`.
- RF8: Row padding: `horizontal = Spacing.md`, `vertical = 12.dp`.
- RF9: Spacing between rows: 4dp.

### Add categories

- RF10: Below the category list, a `HorizontalDivider` is **not** shown (Figma shows none). Instead, "+ Add categories" is a text row with a `+` icon (`Icons.Filled.Add`, 20dp) and "Add categories" label (`bodyLarge`, `FontWeight.Medium`, `onSurface`). Same row padding as category items.
- RF11: Tapping calls `onAddCategories`.

### Drawer dimensions

- RF12: Drawer width: ~300dp (or `ModalDrawerSheet` default, whichever looks closer to the Figma — roughly 70% of screen width).

## Contracts

### Composable signature (unchanged)

```kotlin
@Composable
fun DrawerContent(
    settings: Settings,
    onCategorySelected: (String) -> Unit,
    onAddCategories: () -> Unit,
    modifier: Modifier = Modifier,
)
```

### Key visual mapping from Figma → Compose

| Figma / Screenshot | Compose |
|---|---|
| Circle avatar 48dp, gray bg, person icon | `Surface(shape = CircleShape, color = surfaceVariant, modifier = Modifier.size(48.dp))` + `Icon(Icons.Outlined.PersonOutline)` |
| User name beside avatar | `Text(settings.name, style = titleMedium, fontWeight = SemiBold)` |
| "CATEGORIES" section label | `Text("CATEGORIES", style = labelMedium, fontWeight = SemiBold, letterSpacing = 2.sp, color = onSurfaceVariant)` |
| Category name `font-medium` | `bodyLarge`, `FontWeight.Medium` |
| Category code `text-sm text-muted` in parens | `bodySmall`, `onSurfaceVariant`, e.g. "(cs.AI)" |
| Active row `bg-muted rounded` | `Surface(color = surfaceVariant, shape = RoundedCornerShape(8.dp))` |
| Inactive row transparent | `Modifier.clickable`, no background |
| "+ Add categories" with + icon | `Row` with `Icon(Icons.Filled.Add, size = 20.dp)` + `Text("Add categories")` |

### Category row composable

```kotlin
@Composable
private fun CategoryRow(
    name: String,
    code: String,
    isActive: Boolean,
    onClick: () -> Unit,
)
```

## Acceptance criteria

- [ ] AC1: The drawer header shows a 48dp circular avatar placeholder with a person icon and the user's name beside it.
- [ ] AC2: "CATEGORIES" section label is uppercase, semibold, letter-spacing wide, `onSurfaceVariant`.
- [ ] AC3: Each category shows name and code on separate lines.
- [ ] AC4: The active category row has a `surfaceVariant` background with rounded corners.
- [ ] AC5: Tapping a category row calls `onCategorySelected` and the drawer closes (existing behavior).
- [ ] AC6: "+ Add categories" row shows a `+` icon and "Add categories" text.
- [ ] AC7: Tapping "+ Add categories" navigates to the add-categories screen.
- [ ] AC8: No regression in drawer open/close or category switching.

## Risks

| Risk | Mitigation |
|------|-----------|
| `settings.name` might be empty or blank | Show fallback "User" if name is empty |
| `ModalDrawerSheet` default width doesn't match Figma | Override with `Modifier.width(300.dp)` or `Modifier.fillMaxWidth(0.75f)` |
| `NavigationDrawerItem` styling hard to customize | Replace with plain `Row` + `Modifier.clickable` inside `ModalDrawerSheet` for full control |

## References

- Figma screenshot: drawer panel (May 10, 2026)
- Depends on: spec 0006 (design system — `Spacing`, color tokens, typography)
- Current implementation: `ui/feed/DrawerContent.kt`
