package com.paperstack.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paperstack.data.remote.ArxivApiService
import com.paperstack.data.remote.FetchPapersParams
import com.paperstack.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 30
private const val VISIBLE_SIZE = 15

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val arxivApiService: ArxivApiService,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FeedState())
    val state: StateFlow<FeedState> = _state.asStateFlow()

    private val cache = mutableMapOf<String, FeedState>()
    private var currentCategory: String = ""

    init {
        settingsRepository.settings
            .filterNotNull()
            .map { it.activeCategory }
            .onEach { category -> switchCategory(category) }
            .launchIn(viewModelScope)
    }

    private fun switchCategory(category: String) {
        if (category == currentCategory) return
        // Persist current state before switching
        if (currentCategory.isNotEmpty()) cache[currentCategory] = _state.value
        currentCategory = category

        val cached = cache[category]
        if (cached != null) {
            _state.value = cached
        } else {
            _state.value = FeedState(isLoading = true)
            fetchInitial(category)
        }
    }

    private fun fetchInitial(category: String) {
        viewModelScope.launch {
            val result = arxivApiService.fetchPapers(
                FetchPapersParams(category = category, start = 0, pageSize = PAGE_SIZE),
            )
            result.fold(
                onSuccess = { fetched ->
                    val visible = fetched.papers.take(VISIBLE_SIZE)
                    val buffer = fetched.papers.drop(VISIBLE_SIZE)
                    _state.update {
                        it.copy(
                            visiblePapers = visible,
                            buffer = buffer,
                            nextStart = PAGE_SIZE,
                            totalResults = fetched.totalResults,
                            isLoading = false,
                            error = null,
                        )
                    }
                    cache[category] = _state.value
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(isLoading = false, error = e.message ?: "Failed to load papers")
                    }
                },
            )
        }
    }

    fun loadMore() {
        val current = _state.value
        if (current.isLoading || current.buffer.isEmpty() && current.nextStart >= current.totalResults) return

        // Show buffered papers immediately
        _state.update { state ->
            state.copy(
                visiblePapers = state.visiblePapers + state.buffer,
                buffer = emptyList(),
                isPrefetching = true,
            )
        }

        // Prefetch next batch in background
        val category = _state.value.visiblePapers
            .firstOrNull()?.primaryCategory ?: return

        viewModelScope.launch {
            val result = arxivApiService.fetchPapers(
                FetchPapersParams(category = category, start = current.nextStart, pageSize = PAGE_SIZE),
            )
            result.fold(
                onSuccess = { fetched ->
                    val newBuffer = fetched.papers.take(VISIBLE_SIZE)
                    _state.update { state ->
                        state.copy(
                            buffer = newBuffer,
                            nextStart = state.nextStart + PAGE_SIZE,
                            totalResults = fetched.totalResults,
                            isPrefetching = false,
                        )
                    }
                    cache[currentCategory] = _state.value
                },
                onFailure = {
                    // Silent failure — next loadMore will retry
                    _state.update { it.copy(isPrefetching = false, buffer = emptyList()) }
                },
            )
        }
    }

    fun retry(category: String) {
        cache.remove(category)
        _state.update { FeedState(isLoading = true) }
        fetchInitial(category)
    }

    fun refresh(category: String) {
        if (_state.value.isLoading) return
        cache.remove(category)
        _state.update { FeedState(isLoading = true) }
        fetchInitial(category)
    }
}
