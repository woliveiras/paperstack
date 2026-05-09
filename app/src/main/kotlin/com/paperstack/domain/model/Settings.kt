package com.paperstack.domain.model

data class Settings(
    val displayName: String,
    val selectedCategories: List<String>,
    val activeCategory: String,
    val onboardingCompleted: Boolean,
)
