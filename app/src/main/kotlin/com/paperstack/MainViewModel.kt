package com.paperstack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paperstack.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {

    /** Null while loading; then "onboarding" or "feed". */
    val startDestination = settingsRepository.settings
        .map { settings ->
            if (settings?.onboardingCompleted == true) "feed" else "onboarding"
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )
}
