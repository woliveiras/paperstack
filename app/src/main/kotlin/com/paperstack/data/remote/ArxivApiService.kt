package com.paperstack.data.remote

import com.paperstack.domain.model.Paper

enum class SortOrder {
    RELEVANCE,
    SUBMITTED_DATE,
    LAST_UPDATED_DATE,
}

data class FetchPapersParams(
    val category: String,
    val start: Int,
    val pageSize: Int = 30,
    val fromDate: String? = null,
    val toDate: String? = null,
    val sortOrder: SortOrder = SortOrder.SUBMITTED_DATE,
)

data class FetchPapersResult(
    val papers: List<Paper>,
    val totalResults: Int,
    val startIndex: Int,
)

interface ArxivApiService {
    suspend fun fetchPapers(params: FetchPapersParams): Result<FetchPapersResult>
}
