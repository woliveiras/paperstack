---
status: Approved
number: "0013"
title: Detail Screen Redesign
depends_on: ["0006"]
blocks: []
created: 2026-05-10
updated: 2026-05-10
owner: ""
---

# 0013 — Detail Screen Redesign

## Context

Spec 0003 and 0005 built the detail screen with a `TopAppBar`, inline metadata text, a horizontal row of Save + Read Online buttons, and the download/open PDF flow. The Figma redesign (May 2026) restructures the layout: a custom top bar (back + bookmark), serif title, metadata chips row, an "ABSTRACT" section header, full-width stacked action buttons (filled Save, outlined Read Online, outlined Download PDF with distinct download/progress/open/error states).

## Goal

Visually update the detail screen to match the Figma design while preserving all existing ViewModel logic, download states, and navigation.

## Non-goals

- Changing `DetailViewModel`, `DetailState`, or `DownloadState` logic
- Changing the PDF download/open mechanism
- Adding share functionality (v2)

## Functional requirements

### Top bar

- RF1: Custom top bar (not `TopAppBar`) — `Row` with back arrow (`Icons.AutoMirrored.Filled.ArrowBack`) on the left and bookmark icon on the right, both as `IconButton`. Surface/card background + `HorizontalDivider` below.
- RF2: Bookmark icon: `Icons.Filled.Bookmark` tinted `onSurface` when saved, `Icons.Outlined.BookmarkBorder` tinted `onSurfaceVariant` when not saved. 24×24dp.

### Content area

- RF3: Title uses `headlineMedium` (Playfair Display serif, bold/700 from design system). No max lines — full title shown.
- RF4: Authors line below title — `bodyMedium`, `onSurface` at 80% opacity (`copy(alpha = 0.8f)`), max 2 lines, ellipsis.
- RF5: Metadata chips in a `FlowRow` (or horizontal `Row` with wrapping) with 8dp gaps:
  - **Date chip**: `labelSmall` inside `Surface(color = surfaceVariant, shape = RoundedCornerShape(4.dp))` — e.g. "2026-05-07".
  - **Category chip**: same style as date chip — e.g. "cs.CL".
  - **Conference chip** (optional, shown when `paper.comment` is not blank): `labelSmall`, `FontWeight.Medium`, inside `Surface(color = primary.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp))`, text tinted `primary` — e.g. "ICML 2026".
- RF6: "ABSTRACT" section header: `labelMedium`, `FontWeight.SemiBold`, uppercase, letter-spacing `wide` (2sp), `onSurfaceVariant` color. No `HorizontalDivider` between metadata and abstract (Figma shows none).
- RF7: Abstract body: `bodyMedium`, `onSurface` at 90% opacity, `lineHeight` relaxed. Full text shown (no truncation).

### Action buttons (stacked, full-width)

- RF8: **Save/Saved button** — filled, full-width, rounded-lg (`RoundedCornerShape(8.dp)`), dark background (`onSurface` or `primary`). Label: "Save" when not saved, "Saved" when saved. White text, `FontWeight.Medium`.
- RF9: **Read online button** — outlined, full-width, rounded-lg, `border-2 border-border`. Icon `ExternalLink` (`Icons.Filled.OpenInNew`, 16dp) + "Read online" label. Text tinted `onSurface`.
- RF10: **Download/Open PDF button** — outlined, full-width, rounded-lg, with 4 states:
  - **Idle**: icon `Download` (`Icons.Filled.Download`, 16dp) + "Download PDF". Border `border-border`.
  - **Downloading**: border changes to `primary`. Row with "Downloading..." on left + "N%" on right (both `bodySmall`, `FontWeight.Medium`, `primary`). Below the text: a `LinearProgressIndicator` filling the width inside the button, track color `surfaceVariant`, indicator color `primary`, height 8dp, rounded.
  - **Downloaded**: icon `FileText` (`Icons.Filled.Description`, 16dp) + "Open PDF". Background `primary.copy(alpha = 0.1f)`, border `primary`, text `primary`.
  - **Error**: icon `RefreshCw` (`Icons.Filled.Refresh`, 16dp) + "Retry". Background `error.copy(alpha = 0.1f)`, border `error`, text `error`.
- RF11: Spacing between action buttons: 12dp (`spacedBy(12.dp)`).
- RF12: Bottom padding of 24dp after the last button.

## Contracts

### Key visual mapping from Figma → Compose

| Figma / Tailwind | Compose |
|---|---|
| `bg-card border-b` top bar | `Surface(color = surface)` + `HorizontalDivider()` |
| `ArrowLeft` 24px | `Icons.AutoMirrored.Filled.ArrowBack`, `Modifier.size(24.dp)` |
| `text-2xl` title, Playfair Display 700 | `headlineMedium` (serif from design system) |
| `text-sm text-foreground/80` authors | `bodyMedium`, `onSurface.copy(alpha = 0.8f)` |
| `text-xs px-2 py-1 bg-muted rounded` chip | `labelSmall` in `Surface(shape = RoundedCornerShape(4.dp), color = surfaceVariant)`, padding `horizontal = 8.dp, vertical = 4.dp` |
| `bg-primary/10 text-primary` conference chip | `Surface(color = primary.copy(alpha = 0.1f))`, text `primary` |
| `text-sm font-semibold uppercase tracking-wide text-muted-foreground` | `labelMedium`, `FontWeight.SemiBold`, `letterSpacing = 2.sp`, `onSurfaceVariant` |
| `text-sm leading-relaxed text-foreground/90` abstract | `bodyMedium`, `onSurface.copy(alpha = 0.9f)` |
| `bg-primary text-primary-foreground rounded-lg` save button | `Button(colors = ButtonDefaults.buttonColors(containerColor = onSurface), shape = RoundedCornerShape(8.dp))` |
| `border-2 border-border rounded-lg` outlined buttons | `OutlinedButton(border = BorderStroke(2.dp, outline), shape = RoundedCornerShape(8.dp))` |
| `border-2 border-primary` downloading state | `OutlinedButton(border = BorderStroke(2.dp, primary))` |
| `h-2 bg-muted rounded-full` progress track | `LinearProgressIndicator(trackColor = surfaceVariant, modifier = Modifier.height(8.dp).clip(RoundedCornerShape(4.dp)))` |
| `bg-primary/10 border-primary` downloaded button | `OutlinedButton(colors = ...(containerColor = primary.copy(0.1f)), border = BorderStroke(2.dp, primary))` |
| `bg-destructive/10 border-destructive` error button | `OutlinedButton(colors = ...(containerColor = error.copy(0.1f)), border = BorderStroke(2.dp, error))` |
| `space-y-3` button spacing | `Column(verticalArrangement = spacedBy(12.dp))` |
| `px-6 py-6` content padding | `Modifier.padding(horizontal = Spacing.md, vertical = Spacing.md)` |

## Acceptance criteria

- [ ] AC1: Top bar shows back arrow (left) and bookmark icon (right) with `HorizontalDivider` below — no `TopAppBar`.
- [ ] AC2: Title uses the serif font from the design system (`headlineMedium`, bold).
- [ ] AC3: Authors text is `bodyMedium` at 80% opacity, max 2 lines.
- [ ] AC4: Date and category are displayed as individual chips with `surfaceVariant` background.
- [ ] AC5: Conference/comment chip (when present) uses primary tint at 10% opacity with primary text.
- [ ] AC6: "ABSTRACT" section header is uppercase, semibold, wide letter-spacing, `onSurfaceVariant`.
- [ ] AC7: Save/Saved button is full-width, filled dark, rounded-lg — toggles label.
- [ ] AC8: Read online button is outlined, full-width, with `OpenInNew` icon.
- [ ] AC9: Download PDF button (idle) is outlined with `Download` icon.
- [ ] AC10: Downloading state shows "Downloading... N%" text + `LinearProgressIndicator` inside a primary-bordered container.
- [ ] AC11: Downloaded state shows "Open PDF" with `Description` icon, primary-tinted background and border.
- [ ] AC12: Error state shows "Retry" with `Refresh` icon, error-tinted background and border.
- [ ] AC13: No regression in download/open/save ViewModel behavior.

## Risks

| Risk | Mitigation |
|------|-----------|
| `LinearProgressIndicator` inside an `OutlinedButton` may not layout properly | Use a `Surface` or `OutlinedCard` with `clickable` instead of `OutlinedButton` for the downloading state |
| Conference chip detection currently uses `paper.comment` — may include non-conference text | Keep current behavior; refine conference detection in a future spec |
| Serif font (Playfair Display) not yet available if spec 0006 not implemented | Implement 0006 first; fallback to default serif |

## References

- Figma React source: `PaperDetail` component (May 2026)
- Figma screenshots: detail screen (saved, unsaved, downloading states) (May 10, 2026)
- Depends on: spec 0006 (design system — serif font, `Spacing`, color tokens)
- Current implementation: `ui/detail/DetailScreen.kt`
