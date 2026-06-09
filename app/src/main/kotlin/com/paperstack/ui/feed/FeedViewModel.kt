package com.paperstack.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paperstack.data.remote.ArxivApiService
import com.paperstack.data.remote.FetchPapersParams
import com.paperstack.data.remote.SortOrder
import com.paperstack.data.repository.SavedPaperRepository
import com.paperstack.data.repository.SettingsRepository
import com.paperstack.domain.model.Paper
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
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private const val PAGE_SIZE = 30
private const val VISIBLE_SIZE = 15

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val arxivApiService: ArxivApiService,
    private val settingsRepository: SettingsRepository,
    private val savedPaperRepository: SavedPaperRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(FeedState())
    val state: StateFlow<FeedState> = _state.asStateFlow()

    private val cache = mutableMapOf<String, FeedState>()
    private var currentCategory: String = ""

    init {
        savedPaperRepository.observeAll()
            .onEach { saved -> _state.update { it.copy(savedIds = saved.map { p -> p.id }.toSet()) } }
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

        val cached = cache[category]
        if (cached != null) {
            _state.value = cached.copy(savedIds = _state.value.savedIds)
        } else {
            _state.value = FeedState(
                isLoading = true,
                savedIds = _state.value.savedIds,
            )
            fetchInitial(category)
        }
    }

    private fun filterByDate(papers: List<Paper>, from: LocalDate?, to: LocalDate?): List<Paper> {
        if (from == null && to == null) return papers
        return papers.filter { paper ->
            val paperDate = try {
                ZonedDateTime.parse(paper.submittedDate, DateTimeFormatter.ISO_DATE_TIME).toLocalDate()
            } catch (_: Exception) {
                return@filter true
            }
            val afterFrom = from == null || !paperDate.isBefore(from)
            val beforeTo = to == null || !paperDate.isAfter(to)
            afterFrom && beforeTo
        }
    }

    private fun fetchInitial(category: String) {
        val state = _state.value
        viewModelScope.launch {
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
                    val filtered = filterByDate(fetched.papers, state.fromDate, state.toDate)
                    val visible = filtered.take(VISIBLE_SIZE)
                    val buffer = filtered.drop(VISIBLE_SIZE)
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
                    val filtered = filterByDate(fetched.papers, current.fromDate, current.toDate)
                    val newBuffer = filtered.take(VISIBLE_SIZE)
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
        _state.update {
            FeedState(
                isLoading = true,
                savedIds = dates.savedIds,
                fromDate = dates.fromDate,
                toDate = dates.toDate,
            )
        }
        fetchInitial(category)
    }

    fun setDateFilter(fromDate: LocalDate?, toDate: LocalDate?) {
        val current = _state.value
        if (current.fromDate == fromDate && current.toDate == toDate) return

        cache.remove(currentCategory)
        _state.update {
            FeedState(
                isLoading = true,
                savedIds = it.savedIds,
                fromDate = fromDate,
                toDate = toDate,
                sortOrder = it.sortOrder,
            )
        }
        fetchInitial(currentCategory)
    }

    fun clearDateFilter() {
        val current = _state.value
        if (current.fromDate == null && current.toDate == null) return

        cache.remove(currentCategory)
        _state.update {
            FeedState(
                isLoading = true,
                savedIds = it.savedIds,
                fromDate = null,
                toDate = null,
                sortOrder = it.sortOrder,
            )
        }
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
            if (_state.value.savedIds.contains(paper.id)) {
                savedPaperRepository.remove(paper.id)
            } else {
                savedPaperRepository.save(paper)
            }
        }
    }
}
