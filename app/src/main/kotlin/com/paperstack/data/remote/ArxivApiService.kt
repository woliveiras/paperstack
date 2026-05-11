package com.paperstack.data.remote

import com.paperstack.domain.model.Paper

data class FetchPapersParams(
    val category: String,
    val start: Int,
    val pageSize: Int = 30,
    val fromDate: String? = null,
    val toDate: String? = null,
)

data class FetchPapersResult(
    val papers: List<Paper>,
    val totalResults: Int,
    val startIndex: Int,
)

interface ArxivApiService {
    suspend fun fetchPapers(params: FetchPapersParams): Result<FetchPapersResult>
}
