---
status: Implemented
number: "0008"
title: Category Selection Screen Redesign
depends_on: ["0006"]
blocks: []
created: 2026-05-10
updated: 2026-05-10
owner: ""
---

# 0008 — Category Selection Screen Redesign

## Context

Spec 0001 implemented category selection as a chip-grid. The Figma design refresh (May 2026) replaces chips with a scrollable list of row items — each row shows the full category name, the arXiv code below it, and a checkbox on the right. This layout handles long category names gracefully (e.g. "Computational Engineering, Finance, and Science") and scales to any number of categories.

This spec applies to both the onboarding step and the "Add categories" screen (both use `CategoriesStep`).

## Goal

Visually update the category selection composable to match the Figma list design while keeping all existing ViewModel logic, navigation, and validation intact.

## Non-goals

- Changing `OnboardingViewModel`, `SettingsRepository`, or any data layer
- Adding search/filter to the category list (v2)
- Grouping categories by arXiv family beyond the section header already in the design

## Functional requirements

- RF1: A sticky header section shows the title "Choose your topics" (`headlineMedium`, semibold) and the subtitle "Select at least one category to follow." (`bodyMedium`, `onSurfaceVariant`), separated from the list by a `HorizontalDivider`.
- RF2: The category list is scrollable (`LazyColumn`) and fills the available space between the header and the bottom bar.
- RF3: Each list item is a full-width row containing:
  - Left side: category name (`bodyLarge`, semibold, `onSurface`) and arXiv code below it (`bodySmall`, `onSurfaceVariant`).
  - Right side: a 24×24dp checkbox — unchecked shows an outlined square (`border-2`, `onSurfaceVariant/40`); checked shows a filled square with primary background and a white checkmark.
- RF4: Tapping anywhere on the row toggles selection — identical behavior to the current `toggleCategory` ViewModel call.
- RF5: A sticky bottom bar contains the "Get Started" / "Save categories" pill button (full-width, `CircleShape`, primary when ≥1 category selected, `surfaceVariant` + `onSurfaceVariant` when disabled).
- RF6: When at least one category is selected, a counter label appears below the button: `"N category selected"` / `"N categories selected"` (`bodySmall`, `onSurfaceVariant`, centered).
- RF7: The bottom bar is separated from the list by a `HorizontalDivider`.
- RF8: The composable signature (`CategoriesStep`) and its parameters remain unchanged so that `OnboardingScreen` and `add-categories` route require no navigation changes.

## Contracts

### Composable signature (unchanged)

```kotlin
@Composable
fun CategoriesStep(
    state: OnboardingState,
    onToggleCategory: (String) -> Unit,
    onConfirm: () -> Unit,
    confirmLabel: String = "Get Started",
)
```

### Category row item

```kotlin
@Composable
private fun CategoryRow(
    category: ArxivCategory,
    isSelected: Boolean,
    onToggle: () -> Unit,
)
```

### Key visual mapping from Figma → Compose

| Figma / Tailwind | Compose |
|---|---|
| `text-2xl font-semibold` title | `MaterialTheme.typography.headlineMedium`, `FontWeight.SemiBold` |
| `text-sm text-muted-foreground` subtitle | `MaterialTheme.typography.bodyMedium`, `onSurfaceVariant` |
| `border-b border-border` header separator | `HorizontalDivider()` |
| `font-medium text-foreground` category name | `MaterialTheme.typography.bodyLarge`, `FontWeight.Medium` |
| `text-sm text-muted-foreground` code | `MaterialTheme.typography.bodySmall`, `onSurfaceVariant` |
| `w-6 h-6 rounded border-2` checkbox | `Box(Modifier.size(24.dp))` + `RoundedCornerShape(4.dp)` + border |
| Checked: `bg-primary border-primary` + checkmark | Filled `Box` + `Icons.Filled.Check` tinted white, size 16dp |
| `hover:bg-muted` row hover | `Modifier.clickable` (ripple provides feedback on Android) |
| `rounded-full` CTA button | `Button(shape = RoundedCornerShape(50))` |
| `bg-muted text-muted-foreground` disabled button | `enabled = state.selectedCategories.isNotEmpty()` |
| `border-t border-border` bottom separator | `HorizontalDivider()` above bottom bar |
| `"N categories selected"` counter | `Text` below button, visible when `selectedCategories.isNotEmpty()` |
| `px-6 py-6` header padding | `Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md)` |
| `px-4 py-3` row padding | `Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm + 4.dp)` |

## Acceptance criteria

- [ ] AC1: The header shows "Choose your topics" in `headlineMedium` semibold and the subtitle in `bodyMedium` `onSurfaceVariant`.
- [ ] AC2: The header and bottom bar are separated from the list by `HorizontalDivider`.
- [ ] AC3: Each row displays the category full name (semibold) and arXiv code below it (`onSurfaceVariant`).
- [ ] AC4: Unchecked rows show an outlined 24×24dp square; checked rows show the square filled with primary color and a white checkmark.
- [ ] AC5: Tapping any row calls `onToggleCategory` with the correct category code — no change to ViewModel behavior.
- [ ] AC6: The CTA button is disabled (visual + non-interactive) when no category is selected.
- [ ] AC7: When ≥1 category is selected, the counter label "N category/categories selected" appears below the button.
- [ ] AC8: The CTA label uses `confirmLabel` parameter ("Get Started" in onboarding, "Save categories" in add-categories screen).
- [ ] AC9: The list scrolls independently without scrolling the header or bottom bar.
- [ ] AC10: No regression in onboarding navigation or add-categories flow.

## Risks

| Risk | Mitigation |
|------|-----------|
| Custom checkbox conflicts with M3 `Checkbox` styling | Use M3 `Checkbox` composable with `CheckboxDefaults.colors(checkedColor = primary)` instead of custom `Box` if simpler |
| Long category names wrapping and misaligning the checkbox | Use `Row` with `weight(1f)` on the text column and fixed-size checkbox box |
| `LazyColumn` inside a `Column` with fixed height causes scroll conflict | Use `Scaffold` with `bottomBar` slot + `LazyColumn` fills `fillMaxSize` |

## References

- Figma React source: `OnboardingCategories` component (May 2026)
- Screenshot: category selection screen (May 10, 2026)
- Depends on: spec 0006 (design system — `Spacing`, color tokens, typography)
- Existing implementation: `ui/onboarding/OnboardingScreen.kt`, `CategoriesStep` composable
- Used in: onboarding flow + `add-categories` route in `MainActivity`
