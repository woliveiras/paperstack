package com.paperstack.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paperstack.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun setName(name: String) {
        _state.update { it.copy(displayName = name) }
    }

    fun nextStep() {
        val current = _state.value
        if (current.step == OnboardingStep.Name && current.isNameValid) {
            _state.update { it.copy(step = OnboardingStep.Categories) }
        }
    }

    fun toggleCategory(code: String) {
        _state.update { state ->
            val updated = if (state.selectedCategories.contains(code)) {
                state.selectedCategories - code
            } else {
                state.selectedCategories + code
            }
            state.copy(selectedCategories = updated)
        }
    }

    fun completeOnboarding() {
        val current = _state.value
        _state.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                settingsRepository.completeOnboarding(
                    name = current.displayName.trim(),
                    categories = current.selectedCategories.toList(),
                )
                _state.update { it.copy(isSaving = false, isDone = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    /** Pre-fills the selected categories for the "add categories" flow. */
    fun initForAddCategories(existingCategories: List<String>) {
        _state.update {
            it.copy(
                step = OnboardingStep.Categories,
                selectedCategories = existingCategories.toSet(),
            )
        }
    }

    /** Saves the full updated category selection (add + remove). */
    fun saveAddedCategories() {
        val current = _state.value
        _state.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                settingsRepository.setCategories(current.selectedCategories.toList())
                _state.update { it.copy(isSaving = false, isDone = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
