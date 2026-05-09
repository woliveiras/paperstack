package com.paperstack.ui.onboarding

enum class OnboardingStep { Name, Categories }

data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.Name,
    val displayName: String = "",
    val selectedCategories: Set<String> = emptySet(),
    val isSaving: Boolean = false,
    val isDone: Boolean = false,
    val error: String? = null,
) {
    val isNameValid: Boolean get() = displayName.trim().length in 1..50
    val canProceedFromCategories: Boolean get() = selectedCategories.isNotEmpty()
}
