package com.paperstack.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.paperstack.data.repository.SettingsRepository
import com.paperstack.domain.model.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    companion object {
        val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        val KEY_SELECTED_CATEGORIES = stringPreferencesKey("selected_categories")
        val KEY_ACTIVE_CATEGORY = stringPreferencesKey("active_category")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    override val settings: Flow<Settings?> = dataStore.data.map { prefs ->
        val name = prefs[KEY_DISPLAY_NAME] ?: return@map null
        val categoriesJson = prefs[KEY_SELECTED_CATEGORIES] ?: return@map null
        val activeCategory = prefs[KEY_ACTIVE_CATEGORY] ?: return@map null
        val onboardingCompleted = prefs[KEY_ONBOARDING_COMPLETED] ?: false

        Settings(
            displayName = name,
            selectedCategories = Json.decodeFromString(categoriesJson),
            activeCategory = activeCategory,
            onboardingCompleted = onboardingCompleted,
        )
    }

    override suspend fun completeOnboarding(name: String, categories: List<String>) {
        require(name.isNotBlank()) { "Display name must not be blank" }
        require(categories.isNotEmpty()) { "At least one category must be selected" }

        dataStore.edit { prefs ->
            prefs[KEY_DISPLAY_NAME] = name.trim()
            prefs[KEY_SELECTED_CATEGORIES] = Json.encodeToString(categories)
            prefs[KEY_ACTIVE_CATEGORY] = categories.first()
            prefs[KEY_ONBOARDING_COMPLETED] = true
        }
    }

    override suspend fun setActiveCategory(category: String) {
        dataStore.edit { prefs ->
            prefs[KEY_ACTIVE_CATEGORY] = category
        }
    }

    override suspend fun setCategories(categories: List<String>) {
        dataStore.edit { prefs ->
            val current: List<String> = prefs[KEY_SELECTED_CATEGORIES]
                ?.let { Json.decodeFromString(it) }
                ?: emptyList()
            prefs[KEY_SELECTED_CATEGORIES] = Json.encodeToString(categories.distinct())
            // If active category was removed, switch to first remaining
            val active = prefs[KEY_ACTIVE_CATEGORY]
            if (active != null && active !in categories && categories.isNotEmpty()) {
                prefs[KEY_ACTIVE_CATEGORY] = categories.first()
            }
        }
    }

    override suspend fun removeCategory(category: String) {
        dataStore.edit { prefs ->
            val current: List<String> = prefs[KEY_SELECTED_CATEGORIES]
                ?.let { Json.decodeFromString(it) }
                ?: emptyList()
            val updated = current.filter { it != category }
            prefs[KEY_SELECTED_CATEGORIES] = Json.encodeToString(updated)

            // If active category was removed, set the first remaining one
            if (prefs[KEY_ACTIVE_CATEGORY] == category && updated.isNotEmpty()) {
                prefs[KEY_ACTIVE_CATEGORY] = updated.first()
            }
        }
    }

    override suspend fun isOnboardingCompleted(): Boolean {
        return dataStore.data.firstOrNull()?.get(KEY_ONBOARDING_COMPLETED) ?: false
    }
}
