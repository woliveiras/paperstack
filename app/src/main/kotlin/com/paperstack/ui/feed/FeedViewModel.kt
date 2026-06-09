package com.paperstack.ui.feed

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paperstack.data.remote.ArxivApiService
import com.paperstack.data.remote.FetchPapersParams
import com.paperstack.data.remote.SortOrder
import com.paperstack.data.repository.SavedPaperRepository
import com.paperstack.data.repository.SettingsRepository
import com.paperstack.domain.model.Paper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

private const val PAGE_SIZE = 30
private const val VISIBLE_SIZE = 15
@HiltViewModel
class FeedViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val arxivApiService: ArxivApiService,
    private val settingsRepository: SettingsRepository,
    private val savedPaperRepository: SavedPaperRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FeedState())
    val state: StateFlow<FeedState> = _state.asStateFlow()

    private val _savedIds = MutableStateFlow<Set<String>>(emptySet())
    val savedIds: StateFlow<Set<String>> = _savedIds.asStateFlow()

    private val cache = mutableMapOf<String, FeedState>()
    private var currentCategory: String
        get() = savedStateHandle["currentCategory"] ?: ""
        set(value) { savedStateHandle["currentCategory"] = value }
    private var fetchJob: Job? = null
    private var prefetchJob: Job? = null

    init {
        savedPaperRepository.observeAll()
            .onEach { saved -> _savedIds.value = saved.map { p -> p.id }.toSet() }
            .launchIn(viewModelScope)

        settingsRepository.settings
            .filterNotNull()
            .map { it.activeCategory }
            .onEach { category -> switchCategory(category) }
            .launchIn(viewModelScope)
    }

    private fun switchCategory(category: String) {
        if (category == currentCategory) return
        if (currentCategory.isNotEmpty()) cache[currentCategory] = _state.value
        currentCategory = category

        fetchJob?.cancel()
        fetchJob = null

        val cached = cache[category]
        if (cached != null) {
            _state.value = cached
        } else {
            _state.value = FeedState(
                isLoading = true,
            )
            fetchInitial(category)
        }
    }

    private fun fetchInitial(category: String) {
        val state = _state.value
        fetchJob = viewModelScope.launch {
            val result = arxivApiService.fetchPapers(
                FetchPapersParams(
                    category = category,
                    start = 0,
                    pageSize = PAGE_SIZE,
                    fromDate = state.fromDate?.toString(),
                    toDate = state.toDate?.toString(),
                    sortOrder = state.sortOrder,
                ),
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
        if (current.isLoading || current.isPrefetching ||
            current.buffer.isEmpty() && current.nextStart >= current.totalResults
        ) {
            return
        }

        _state.update { state ->
            state.copy(
                visiblePapers = state.visiblePapers + state.buffer,
                buffer = emptyList(),
                isPrefetching = true,
            )
        }

        val category = _state.value.visiblePapers.firstOrNull()?.primaryCategory ?: return

        prefetchJob?.cancel()
        prefetchJob = viewModelScope.launch {
            val result = arxivApiService.fetchPapers(
                FetchPapersParams(
                    category = category,
                    start = current.nextStart,
                    pageSize = PAGE_SIZE,
                    fromDate = current.fromDate?.toString(),
                    toDate = current.toDate?.toString(),
                ),
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
        val dates = _state.value
        _state.update { FeedState(isLoading = true, fromDate = dates.fromDate, toDate = dates.toDate) }
        fetchInitial(category)
    }

    fun refresh(category: String) {
        if (_state.value.isLoading) return
        cache.remove(category)
        val dates = _state.value
        _state.value = FeedState(
            isLoading = true,
            fromDate = dates.fromDate,
            toDate = dates.toDate,
        )
        fetchInitial(category)
    }

    fun setDateFilter(fromDate: LocalDate?, toDate: LocalDate?) {
        val current = _state.value
        if (current.fromDate == fromDate && current.toDate == toDate) return

        cache.remove(currentCategory)
        _state.value = FeedState(
            isLoading = true,
            fromDate = fromDate,
            toDate = toDate,
            sortOrder = _state.value.sortOrder,
        )
        fetchInitial(currentCategory)
    }

    fun clearDateFilter() {
        val current = _state.value
        if (current.fromDate == null && current.toDate == null) return

        cache.remove(currentCategory)
        _state.value = FeedState(
            isLoading = true,
            fromDate = null,
            toDate = null,
            sortOrder = _state.value.sortOrder,
        )
        fetchInitial(currentCategory)
    }

    fun setSortOrder(sortOrder: SortOrder) {
        val current = _state.value
        if (current.sortOrder == sortOrder) return

        cache.remove(currentCategory)
        _state.update {
            it.copy(
                isLoading = true,
                sortOrder = sortOrder,
            )
        }
        fetchInitial(currentCategory)
    }

    fun toggleSave(paper: Paper) {
        viewModelScope.launch {
            if (_savedIds.value.contains(paper.id)) {
                savedPaperRepository.remove(paper.id)
            } else {
                savedPaperRepository.save(paper)
            }
        }
    }
}
