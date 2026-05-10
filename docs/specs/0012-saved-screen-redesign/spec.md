---
status: Implemented
number: "0012"
title: Saved Screen Redesign
depends_on: ["0006", "0011"]
blocks: []
created: 2026-05-10
updated: 2026-05-10
owner: ""
---

# 0012 — Saved Screen Redesign

## Context

Spec 0004 implemented the saved papers screen with a M3 `TopAppBar`, `LazyColumn`, and a basic empty state. The Figma redesign (May 2026) simplifies the layout: a plain text title (no `TopAppBar`), the same `PaperCard` as the feed (spec 0011), and a refined empty state with a large `BookOpen` icon, descriptive headline, and helper text.

## Goal

Update the saved screen layout and empty state to match the Figma design while preserving all existing ViewModel behavior, removal dialog, and navigation.

## Non-goals

- Changing `SavedViewModel` or `SavedPaperRepository`
- Redesigning the removal confirmation dialog (keep current `AlertDialog`)
- Adding search or sorting to the saved list

## Functional requirements

- RF1: The screen header is a simple `Text` composable — "Saved" in `titleLarge`, `FontWeight.SemiBold` — inside a `Surface` (card/surface background) with `HorizontalDivider` below. Padding: `horizontal = Spacing.md` (24dp per Figma `px-6`), `vertical = Spacing.md` (16dp).
- RF2: No `TopAppBar` or `Scaffold` topBar slot — the header is part of the `Column` layout.
- RF3: The paper list uses `LazyColumn` with `contentPadding = PaddingValues(Spacing.md)` and `verticalArrangement = spacedBy(Spacing.md)`, filling the remaining space.
- RF4: Each card is the redesigned `PaperCard` from spec 0011 — bookmark icon always filled (paper is saved). Tapping the bookmark triggers the removal confirmation dialog.
- RF5: **Empty state** — centered vertically in the available space:
  - A `BookOpen`-style icon (`Icons.AutoMirrored.Outlined.MenuBook` or similar), 96×96dp, tinted `onSurfaceVariant` at 40% alpha (`copy(alpha = 0.4f)`), strokeWidth ~1.5.
  - Below: "No saved papers yet" — `titleMedium`, `FontWeight.SemiBold`, centered.
  - Below: "Papers you bookmark will appear here for easy access" — `bodySmall`, `onSurfaceVariant`, centered, max width ~280dp.
  - Spacing: 24dp between icon and headline, 8dp between headline and body text.
- RF6: The bottom navigation bar is rendered by `MainActivity` — the saved screen content sits above it.

## Contracts

### Key visual mapping from Figma → Compose

| Figma / Tailwind | Compose |
|---|---|
| `bg-card border-b border-border` header | `Surface(color = colorScheme.surface)` + `HorizontalDivider()` |
| `px-6 py-4` header padding | `Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md)` |
| `font-semibold text-xl` title | `titleLarge`, `FontWeight.SemiBold` |
| `p-4 space-y-4` list | `contentPadding = PaddingValues(Spacing.md)`, `spacedBy(Spacing.md)` |
| `BookOpen` icon `w-24 h-24 text-muted-foreground/40 stroke-[1.5]` | `Icon(Modifier.size(96.dp))`, `onSurfaceVariant.copy(alpha = 0.4f)` |
| `text-xl font-semibold` empty title | `titleMedium`, `FontWeight.SemiBold` |
| `text-sm text-muted-foreground` empty body | `bodySmall`, `onSurfaceVariant` |
| `max-w-[280px]` body constraint | `Modifier.widthIn(max = 280.dp)` |
| `mb-6` icon margin bottom | `Spacer(Modifier.height(24.dp))` |

### Empty state composable

```kotlin
@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "No saved papers yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Papers you bookmark will appear here for easy access",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp),
        )
    }
}
```

## Acceptance criteria

- [ ] AC1: The header shows "Saved" in `titleLarge` semibold with a `HorizontalDivider` below — no `TopAppBar`.
- [ ] AC2: The paper list uses 16dp padding and 16dp spacing between cards.
- [ ] AC3: Cards use the redesigned `PaperCard` (spec 0011) with the bookmark always filled.
- [ ] AC4: Empty state shows a 96dp `MenuBook` icon at 40% opacity, centered.
- [ ] AC5: Empty state headline is "No saved papers yet" in `titleMedium` semibold.
- [ ] AC6: Empty state body text is "Papers you bookmark will appear here for easy access" in `bodySmall`, centered, max 280dp wide.
- [ ] AC7: The removal confirmation `AlertDialog` still works — no regression.
- [ ] AC8: Bottom navigation bar remains visible on the saved screen.

## Risks

| Risk | Mitigation |
|------|-----------|
| `Icons.AutoMirrored.Outlined.MenuBook` not available in default icon set | Use `Icons.Outlined.MenuBook` or add `material-icons-extended` dependency |
| Empty state competes with bottom nav for vertical space on small screens | Use `fillMaxSize` on the empty state so it centers within the available area above the nav |

## References

- Figma React source: `SavedScreen` component (May 2026)
- Figma screenshots: saved screen empty + populated (May 10, 2026)
- Depends on: spec 0006 (design system), spec 0011 (`PaperCard` redesign)
- Current implementation: `ui/saved/SavedScreen.kt`
