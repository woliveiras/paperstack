---
status: Approved
number: "0001"
title: Onboarding
depends_on: []
blocks: ["0002"]
created: 2026-05-09
updated: 2026-05-09
owner: ""
---

# 0001 — Onboarding

## Context

When a user opens Paperstack for the first time, there is no stored preference for display name or category. The app cannot show a meaningful feed without at least one category selected. Onboarding must be completed before any other screen is accessible.

Driven by PRD: [docs/prds/0001-browse-by-category.md](../../prds/0001-browse-by-category.md)

## Goal

Guide a first-time user through entering their display name and selecting one or more arXiv categories, then persist both to local storage so the feed can load.

## Non-goals

- Editing name or category after onboarding (accessible via Drawer → "+ Add categories")
- Any form of account, login, or remote sync

## Functional requirements

- RF1: On first launch (no stored settings), the app renders the onboarding screen instead of the feed.
- RF2: Onboarding step 1 — the user enters a display name (required, 1–50 characters, trimmed).
- RF3: Onboarding step 2 — the user sees the full list of arXiv categories and selects one or more using checkboxes. There is no upper limit. Minimum: 1.
- RF4: The category list is hardcoded in the app (no API call needed); it reflects the official arXiv category taxonomy, grouped by domain.
- RF5: On completion, display name, selected categories array, and active category (first selected) are persisted to `SettingsRepository` → DataStore Preferences.
- RF6: After completion, the app navigates to the feed screen. Onboarding must never appear again for this device.
- RF7: The "Continue" button on step 1 is disabled until a valid name is entered.
- RF8: The "Confirm" button on step 2 is disabled until at least one category is selected.

## Contracts

### Settings data model

```kotlin
// domain/model/Settings.kt
data class Settings(
    val displayName: String,              // 1–50 chars, trimmed
    val selectedCategories: List<String>, // arXiv codes, e.g. ["cs.AI", "cs.PL"]
    val activeCategory: String,           // currently viewed category code
    val onboardingCompleted: Boolean,
)
```

### SettingsRepository

```kotlin
// data/repository/SettingsRepository.kt
interface SettingsRepository {
    val settings: Flow<Settings?>
    suspend fun completeOnboarding(name: String, categories: List<String>)
    suspend fun setActiveCategory(category: String)
    suspend fun addCategories(categories: List<String>)
    suspend fun removeCategory(category: String)
    suspend fun isOnboardingCompleted(): Boolean
}
```

### Navigation contract

- First launch: root `NavHost` checks `isOnboardingCompleted()` → navigates to `OnboardingScreen` if false
- After `completeOnboarding()`: navigate to `FeedScreen`

### Category list

Hardcoded constant in `domain/model/ArxivCategory.kt`:

```kotlin
data class ArxivCategory(
    val code: String,   // e.g. "cs.AI"
    val name: String,   // e.g. "Artificial Intelligence"
    val group: String,  // e.g. "Computer Science"
)

// Companion object or top-level val in constants
val ARXIV_CATEGORIES: List<ArxivCategory> = listOf(
    // full list at implementation time
)
```

## Acceptance criteria

- [ ] AC1: On fresh install, the onboarding screen is shown before the feed.
- [ ] AC2: Submitting an empty or whitespace-only name is not allowed (button disabled).
- [ ] AC3: A name longer than 50 characters is not allowed.
- [ ] AC4: The category list renders all arXiv categories grouped by domain with checkboxes.
- [ ] AC5: The "Confirm" button is disabled until at least one category is checked.
- [ ] AC6: Multiple categories can be selected simultaneously with no upper limit.
- [ ] AC7: After completing onboarding, `settingsStore.settings.onboardingCompleted` is `true`.
- [ ] AC8: After completing onboarding, `settingsStore.settings.selectedCategories` contains all checked categories.
- [ ] AC9: `settingsStore.settings.activeCategory` is set to the first category in `selectedCategories`.
- [ ] AC10: After completing onboarding, reopening the app goes directly to the feed — not onboarding.
- [ ] AC11: `SettingsRepository` is covered by unit tests for `completeOnboarding`, `addCategories`, `removeCategory`, and `isOnboardingCompleted`.

## Risks

| Risk | Mitigation |
|------|-----------|
| Category list becomes outdated | Document update process; list is versioned in `domain/model/ArxivCategory.kt` |
| User force-quits mid-onboarding | Persist only on final confirmation — partial state is discarded on next launch |

## References

- PRD: [docs/prds/0001-browse-by-category.md](../../prds/0001-browse-by-category.md)
- Blocks: spec 0002 (paper feed)
