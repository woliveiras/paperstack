package com.paperstack.ui.saved

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
import javax.inject.Inject

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val savedPaperRepository: SavedPaperRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SavedState())
    val state: StateFlow<SavedState> = _state.asStateFlow()

    init {
        savedPaperRepository.observeAll()
            .onEach { papers ->
                _state.update { it.copy(papers = papers, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun remove(paper: Paper) {
        viewModelScope.launch {
            savedPaperRepository.remove(paper.id)
        }
    }
}
