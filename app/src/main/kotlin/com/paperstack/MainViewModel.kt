package com.paperstack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paperstack.data.repository.SettingsRepository
import com.paperstack.domain.model.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<Settings?> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    /** Null while loading; then "onboarding" or "feed". */
    val startDestination: StateFlow<String?> = settingsRepository.settings
        .map { s ->
            if (s?.onboardingCompleted == true) "feed" else "onboarding"
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    fun setActiveCategory(category: String) {
        viewModelScope.launch {
            settingsRepository.setActiveCategory(category)
        }
    }
}
