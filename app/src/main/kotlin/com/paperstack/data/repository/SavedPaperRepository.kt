package com.paperstack.data.repository

import com.paperstack.domain.model.Paper
import kotlinx.coroutines.flow.Flow

interface SavedPaperRepository {
    fun observeAll(): Flow<List<Paper>>
    fun observeIsSaved(id: String): Flow<Boolean>
    suspend fun save(paper: Paper)
    suspend fun remove(id: String)
}
