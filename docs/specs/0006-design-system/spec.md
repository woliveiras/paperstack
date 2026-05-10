---
status: Implemented
number: "0006"
title: Design System
depends_on: []
blocks: []
created: 2026-05-10
updated: 2026-05-10
owner: ""
---

# 0006 — Design System

## Context

The app was built screen by screen using raw Material 3 components with ad-hoc values (colors, spacing, typography) repeated inline across `FeedScreen`, `SavedScreen`, `DetailScreen`, and `OnboardingScreen`. A Figma reference now exists defining a cohesive visual language: deep indigo primary, Playfair Display serif for titles, Inter for UI chrome, and a structured token set.

Without a design system, inconsistencies compound as new screens are added, theming is impossible to change globally, and dark mode requires manual auditing of every composable.

## Goal

Introduce a centralized Compose design system (`ui/theme/`) that encodes all brand tokens — color, typography, spacing, shape — so every screen consumes named tokens instead of hardcoded values.

## Non-goals

- Custom component library (shadowing Material 3 widgets) — use M3 as-is, only theme it
- Animation tokens or motion system
- Multi-brand or white-label theming
- Figma–code sync tooling

## Functional requirements

- RF1: A `PaperStackTheme` wrapper applies the brand color scheme (light + dark) to all screens via `MaterialTheme`.
- RF2: Primary color is `#3D5AFE` (deep indigo); surface is warm off-white `#FFFDF6` in light, deep navy `#0D0F1A` in dark.
- RF3: Paper titles across the app use a serif typeface (Playfair Display); all other text uses Inter (sans-serif).
- RF4: A `PaperStackTypography` object maps semantic roles (`titleLarge`, `bodyMedium`, etc.) to the correct font family, weight, and size.
- RF5: A `PaperStackShapes` object defines corner radii — cards use `12.dp` rounded corners.
- RF6: A `Spacing` object exposes named spacing constants (`xs = 4.dp`, `sm = 8.dp`, `md = 16.dp`, `lg = 24.dp`, `xl = 32.dp`) used in padding/margin throughout the app.
- RF7: Dark theme is applied automatically based on system setting (`isSystemInDarkTheme()`).
- RF8: All existing screens are updated to remove hardcoded color/spacing literals and consume theme tokens instead.
- RF9: The Google Fonts Compose dependency is used to load Playfair Display and Inter at runtime.

## Contracts

### Theme entry point

```kotlin
// ui/theme/Theme.kt
@Composable
fun PaperStackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
)
```

### Color tokens

```kotlin
// ui/theme/Color.kt
val Indigo500 = Color(0xFF3D5AFE)
val Indigo700 = Color(0xFF0031CA)
val NavyDark  = Color(0xFF0D0F1A)
val SurfaceWarm = Color(0xFFFFFDF6)
// + full M3 light/dark ColorScheme definitions
```

### Typography tokens

```kotlin
// ui/theme/Type.kt
val PaperStackTypography = Typography(
    titleLarge  = TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = PlayfairDisplay, fontWeight = FontWeight.Medium,   fontSize = 16.sp),
    bodyLarge   = TextStyle(fontFamily = Inter,           fontWeight = FontWeight.Normal,   fontSize = 16.sp),
    bodyMedium  = TextStyle(fontFamily = Inter,           fontWeight = FontWeight.Normal,   fontSize = 14.sp),
    labelLarge  = TextStyle(fontFamily = Inter,           fontWeight = FontWeight.Medium,   fontSize = 14.sp),
    // ... remaining M3 roles
)
```

### Spacing tokens

```kotlin
// ui/theme/Spacing.kt
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
}
```

### Shape tokens

```kotlin
// ui/theme/Shape.kt
val PaperStackShapes = Shapes(
    small  = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(12.dp),
    large  = RoundedCornerShape(16.dp),
)
```

### Gradle dependency

```kotlin
// build.gradle.kts
implementation(libs.compose.ui.google.fonts)
```

```toml
# libs.versions.toml
compose-ui-google-fonts = { group = "androidx.compose.ui", name = "ui-text-google-fonts", version.ref = "compose-bom" }
```

## Acceptance criteria

- [ ] AC1: `PaperStackTheme` is the single theme wrapper — `MaterialTheme` is not called directly anywhere outside `Theme.kt`.
- [ ] AC2: Paper titles render in Playfair Display on feed cards, saved cards, and the detail screen.
- [ ] AC3: All body and UI text renders in Inter.
- [ ] AC4: Switching the device to dark mode applies the dark color scheme without any code change.
- [ ] AC5: No hardcoded `Color(...)` literals remain in screen composables — only `MaterialTheme.colorScheme.*` references.
- [ ] AC6: No hardcoded `dp` spacing literals remain in screen composables — only `Spacing.*` constants.
- [ ] AC7: `PaperStackTheme` renders correctly in Compose Preview (`@Preview`) for both light and dark themes.
- [ ] AC8: Google Fonts load with a fallback font defined — the app does not crash if fonts fail to load offline.

## Risks

| Risk | Mitigation |
|------|-----------|
| Google Fonts requires network on first launch | Configure `FontRequest` with a local fallback font (system serif/sans-serif) |
| Playfair Display not available via Google Fonts Compose | Verify availability before implementation; bundle as asset if needed |
| Replacing hardcoded values breaks existing visual layout | Do screen-by-screen review; run on emulator after each screen migrated |

## References

- Figma reference: design produced from the Figma prompt (May 10, 2026)
- All existing screens: `FeedScreen`, `SavedScreen`, `DetailScreen`, `OnboardingScreen`
- Material 3 theming: https://m3.material.io/styles/color/the-color-system
