package com.paperstack.data.repository

import com.paperstack.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<Settings?>
    suspend fun completeOnboarding(name: String, categories: List<String>)
    suspend fun setActiveCategory(category: String)
    suspend fun addCategories(categories: List<String>)
    suspend fun removeCategory(category: String)
    suspend fun isOnboardingCompleted(): Boolean
}
