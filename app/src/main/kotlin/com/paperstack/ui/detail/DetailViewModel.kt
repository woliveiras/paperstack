package com.paperstack.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paperstack.data.repository.SavedPaperRepository
import com.paperstack.domain.model.Paper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val savedPaperRepository: SavedPaperRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state.asStateFlow()

    init {
        val paperJson: String = checkNotNull(savedStateHandle["paperJson"])
        val paper: Paper = Json.decodeFromString(paperJson)
        _state.update { it.copy(paper = paper) }

        savedPaperRepository.observeIsSaved(paper.id)
            .onEach { isSaved -> _state.update { it.copy(isSaved = isSaved) } }
            .launchIn(viewModelScope)
    }

    fun toggleSave() {
        val paper = _state.value.paper ?: return
        if (_state.value.isTogglingSave) return
        _state.update { it.copy(isTogglingSave = true) }
        viewModelScope.launch {
            try {
                if (_state.value.isSaved) {
                    savedPaperRepository.remove(paper.id)
                } else {
                    savedPaperRepository.save(paper)
                }
            } finally {
                _state.update { it.copy(isTogglingSave = false) }
            }
        }
    }
}
