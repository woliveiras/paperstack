package com.paperstack.ui.feed

import com.paperstack.domain.model.Paper

data class FeedState(
    val visiblePapers: List<Paper> = emptyList(),
    val buffer: List<Paper> = emptyList(),
    val nextStart: Int = 0,
    val totalResults: Int = 0,
    val isLoading: Boolean = false,
    val isPrefetching: Boolean = false,
    val error: String? = null,
) {
    val hasMore: Boolean
        get() = visiblePapers.size + buffer.size < totalResults ||
            (totalResults == 0 && visiblePapers.isEmpty())

    val canShowLoadMore: Boolean
        get() = !isLoading &&
            (buffer.isNotEmpty() || visiblePapers.size < totalResults)
}
