---
status: Implemented
number: "0007"
title: Onboarding Screen Redesign
depends_on: ["0006"]
blocks: []
created: 2026-05-10
updated: 2026-05-10
owner: ""
---

# 0007 — Onboarding Screen Redesign

## Context

Spec 0001 implemented a functional onboarding flow (name + category selection). The Figma design refresh (May 2026) defines a new visual language for the name-input step: a large stacked-layers logo mark, prominent serif welcome headline, borderless input with focus ring, and a pill-shaped CTA pinned to the bottom.

This spec translates the Figma/React reference component (`OnboardingName`) to Jetpack Compose.

## Goal

Visually update the onboarding name step to match the Figma design while keeping all existing behavior (validation, navigation) intact.

## Non-goals

- Changing the category-selection step (covered by a future spec)
- Changing ViewModel or repository logic
- Animations or transitions (v2)

## Functional requirements

- RF1: The screen shows a `Layers`-style stacked-paper icon (64×64dp) at the top-center of the content area.
- RF2: Below the icon, a large headline reads "Welcome to Paperstack" — `headlineLarge` style, semibold, Inter (via the design system token).
- RF3: Below the headline, a subtitle reads "What should we call you?" — `bodyLarge`, `onSurfaceVariant` color.
- RF4: A text input field spans the full width with rounded corners (`12.dp`), placeholder "Your name", and highlights its border with the primary color on focus.
- RF5: A full-width pill-shaped button ("Continue") is pinned to the bottom of the screen with `md` (16dp) horizontal and `lg` (24dp) bottom padding.
- RF6: The "Continue" button is disabled (or ignores taps) when the name field is empty or blank — identical to current behavior.
- RF7: The layout uses `Column` with `verticalArrangement = Arrangement.SpaceBetween` so the icon+form group is centered and the button stays at the bottom regardless of keyboard state.
- RF8: The icon uses `Icons.Outlined.Layers` (or equivalent stacked-paper icon from Material Symbols) tinted with `MaterialTheme.colorScheme.primary`.

## Contracts

### Composable signature (unchanged)

```kotlin
@Composable
fun NameStep(
    state: OnboardingState,
    onNameChange: (String) -> Unit,
    onNext: () -> Unit,
)
```

### Key visual mapping from Figma → Compose

| Figma / Tailwind | Compose |
|---|---|
| `text-4xl font-semibold` | `MaterialTheme.typography.headlineLarge` (Inter, SemiBold) |
| `text-lg text-muted-foreground` | `MaterialTheme.typography.bodyLarge`, `onSurfaceVariant` |
| `border-2 border-border rounded-lg focus:border-primary` | `OutlinedTextField` with `focusedBorderColor = primary` |
| `rounded-full` button | `Button` with `shape = CircleShape` (or `RoundedCornerShape(50%)`) |
| `w-16 h-16` icon | `Icon(modifier = Modifier.size(64.dp))` |
| `py-12` outer padding | `PaddingValues(vertical = Spacing.xl)` |
| `px-6` horizontal padding | `PaddingValues(horizontal = Spacing.md)` |
| Sticky bottom button | `Column(verticalArrangement = Arrangement.SpaceBetween)` |

### Icon

Use `Icons.AutoMirrored.Outlined.LibraryBooks` or `Icons.Outlined.Layers` if available via `compose.material.icons.extended`. Fallback: draw a custom `Layers`-style icon using `Canvas` or use an SVG asset in `res/drawable`.

## Acceptance criteria

- [ ] AC1: The stacked-paper icon is displayed at 64×64dp, tinted primary color, centered above the headline.
- [ ] AC2: The headline "Welcome to Paperstack" uses `headlineLarge` typography (Inter SemiBold via the design system).
- [ ] AC3: The subtitle "What should we call you?" is displayed in `bodyLarge` with `onSurfaceVariant` tint.
- [ ] AC4: The text field has rounded corners, shows "Your name" placeholder, and its border turns primary color when focused.
- [ ] AC5: The "Continue" button is pill-shaped (high border radius), full-width, primary background, white text.
- [ ] AC6: The "Continue" button is disabled when the name field is empty.
- [ ] AC7: The button is pinned to the bottom — it does not scroll with the content.
- [ ] AC8: Existing `OnboardingViewModel` and navigation behavior are unchanged — no regression on the onboarding flow.

## Risks

| Risk | Mitigation |
|------|-----------|
| `Icons.Outlined.Layers` not in Material Symbols set | Use `LibraryBooks` or bundle SVG as `VectorDrawable` in `res/drawable` |
| Pill button shape conflicts with M3 `Button` defaults | Override `shape` parameter: `Button(shape = RoundedCornerShape(50))` |
| Keyboard pushes button off screen | Wrap in `imePadding()` modifier and use `SpaceBetween` layout |

## References

- Figma React source: `OnboardingName` component (May 2026)
- Depends on: spec 0006 (design system tokens — Inter font, `Spacing`, color scheme)
- Existing implementation: `ui/onboarding/OnboardingScreen.kt`, `NameStep` composable
