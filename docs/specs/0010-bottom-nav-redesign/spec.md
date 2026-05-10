---
status: Approved
number: "0010"
title: Bottom Navigation Redesign
depends_on: ["0006"]
blocks: []
created: 2026-05-10
updated: 2026-05-10
owner: ""
---

# 0010 — Bottom Navigation Redesign

## Context

The current bottom navigation uses M3 `NavigationBar` with its default styling (elevated surface, indicator pill on selected item). The Figma design (May 2026) specifies a simpler bottom bar: card background, top border, two tabs (Feed + Saved) with filled/outlined icon variants and a small label below each icon — no pill indicator.

## Goal

Restyle the bottom navigation bar in `MainActivity` to match the Figma design while keeping all existing navigation logic intact.

## Non-goals

- Adding new tabs
- Changing navigation routes or back-stack behavior
- Replacing `NavigationBar` with a fully custom composable (only restyle if M3 theming suffices; otherwise extract)

## Functional requirements

- RF1: The bottom bar has a card/surface background (`colorScheme.surface`) with a `HorizontalDivider` (or `border-t`) as the top separator — no elevation shadow.
- RF2: Two tabs: **Feed** and **Saved**, evenly distributed (`Arrangement.SpaceAround`).
- RF3: Each tab displays a 24×24dp icon above a label (`labelSmall`, `FontWeight.Medium`).
- RF4: **Feed tab icons**: active → `Icons.Filled.Home` (filled), primary color; inactive → `Icons.Outlined.Home` (stroke only), `onSurfaceVariant` color.
- RF5: **Saved tab icons**: active → `Icons.Filled.Bookmark` (filled), primary color; inactive → `Icons.Outlined.BookmarkBorder` (stroke only), `onSurfaceVariant` color.
- RF6: Active tab text is `primary` color; inactive tab text is `onSurfaceVariant`.
- RF7: No pill/indicator highlight on the selected tab — color change on icon + label is the only visual differentiation.
- RF8: The bar uses horizontal padding of `Spacing.md` (24dp per the Figma `px-6`) and vertical padding of 8dp (`py-2`).
- RF9: All existing navigation behavior (`popUpTo`, `launchSingleTop`, `restoreState`, `saveState`) is unchanged.
- RF10: The bar is visible only on `feed` and `saved` routes (current behavior preserved).

## Contracts

### Key visual mapping from Figma → Compose

| Figma / Tailwind | Compose |
|---|---|
| `bg-card border-t border-border` | `Surface(color = colorScheme.surface)` + `HorizontalDivider()` on top |
| `px-6 py-2` | `Modifier.padding(horizontal = Spacing.md, vertical = 8.dp)` |
| `flex items-center justify-around` | `Row(horizontalArrangement = Arrangement.SpaceAround)` |
| `flex-col items-center gap-1` per tab | `Column(horizontalAlignment = CenterHorizontally, verticalArrangement = spacedBy(4.dp))` |
| `Home` icon `w-6 h-6 fill-current` (active) | `Icons.Filled.Home`, tint `primary`, `Modifier.size(24.dp)` |
| `Home` icon `w-6 h-6` strokeWidth=2 (inactive) | `Icons.Outlined.Home`, tint `onSurfaceVariant` |
| `Bookmark` icon `w-6 h-6 fill-current` (active) | `Icons.Filled.Bookmark`, tint `primary` |
| `Bookmark` icon `w-6 h-6` strokeWidth=2 (inactive) | `Icons.Outlined.BookmarkBorder`, tint `onSurfaceVariant` |
| `text-xs font-medium` label | `labelSmall`, `FontWeight.Medium` |
| `text-primary` active label | `primary` color |
| `text-muted-foreground` inactive label | `onSurfaceVariant` color |

### Implementation approach

Option A — Restyle M3 `NavigationBar` + `NavigationBarItem` via `NavigationBarDefaults.colors()` and `NavigationBarItemDefaults.colors()` to remove the indicator and match the color scheme.

Option B — Replace with a custom `Row`-based composable if M3 indicator cannot be fully suppressed.

Prefer Option A for simplicity; fall back to Option B if the M3 indicator pill is unavoidable.

## Acceptance criteria

- [ ] AC1: The bottom bar has a flat surface background with a top `HorizontalDivider` — no elevation shadow.
- [ ] AC2: No pill/indicator highlight appears on the selected tab.
- [ ] AC3: Active tab shows filled icon + primary-colored label; inactive tab shows outlined icon + `onSurfaceVariant` label.
- [ ] AC4: Feed uses `Home`/`Home` outlined; Saved uses `Bookmark`/`BookmarkBorder`.
- [ ] AC5: Tapping each tab navigates correctly — no regression in `popUpTo`, `saveState`, `restoreState`.
- [ ] AC6: The bar is hidden on `onboarding`, `detail`, and `add-categories` routes.
- [ ] AC7: Icon size is 24×24dp, labels use `labelSmall` + `FontWeight.Medium`.

## Risks

| Risk | Mitigation |
|------|-----------|
| M3 `NavigationBarItem` indicator pill cannot be removed via theming alone | Fall back to custom `Row` + `Column` composable with `clickable` modifier |
| `Icons.Filled.Bookmark` not available in default icon set | Use `Icons.Filled.Bookmarks` (current) or add `material-icons-extended` dependency |

## References

- Figma React source: `BottomNav` component (May 2026)
- Depends on: spec 0006 (design system — `Spacing`, color tokens, typography)
- Current implementation: `MainActivity.kt`, `NavigationBar` block (lines 62–100)
