package com.paperstack.data.local.db

import com.paperstack.data.repository.SavedPaperRepository
import com.paperstack.domain.model.Paper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomSavedPaperRepository @Inject constructor(
    private val dao: SavedPaperDao,
) : SavedPaperRepository {

    override fun observeAll(): Flow<List<Paper>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeIsSaved(id: String): Flow<Boolean> =
        dao.observeIsSaved(id)

    override suspend fun save(paper: Paper) =
        dao.insert(paper.toEntity())

    override suspend fun remove(id: String) =
        dao.deleteById(id)

    private fun Paper.toEntity() = SavedPaperEntity(
        id = id,
        title = title,
        authors = Json.encodeToString(authors),
        abstract = abstract,
        submittedDate = submittedDate,
        updatedDate = updatedDate,
        pdfUrl = pdfUrl,
        categories = Json.encodeToString(categories),
        primaryCategory = primaryCategory,
        comment = comment,
    )

    private fun SavedPaperEntity.toDomain() = Paper(
        id = id,
        title = title,
        authors = Json.decodeFromString(authors),
        abstract = abstract,
        submittedDate = submittedDate,
        updatedDate = updatedDate,
        pdfUrl = pdfUrl,
        categories = Json.decodeFromString(categories),
        primaryCategory = primaryCategory,
        comment = comment,
    )
}
