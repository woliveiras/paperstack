package com.paperstack.data.local.db

import app.cash.turbine.test
import com.paperstack.domain.model.Paper
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoomSavedPaperRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private val dao = mockk<SavedPaperDao>()

    private val paper = Paper(
        id = "2605.00001",
        title = "Test Paper",
        authors = listOf("Alice", "Bob"),
        abstract = "Abstract text.",
        submittedDate = "2026-05-01T00:00:00Z",
        updatedDate = "2026-05-01T00:00:00Z",
        pdfUrl = "https://arxiv.org/pdf/2605.00001",
        categories = listOf("cs.AI", "cs.LG"),
        primaryCategory = "cs.AI",
        comment = "Accepted at ICML 2026",
    )

    private val entity = SavedPaperEntity(
        id = paper.id,
        title = paper.title,
        authors = Json.encodeToString(paper.authors),
        abstract = paper.abstract,
        submittedDate = paper.submittedDate,
        updatedDate = paper.updatedDate,
        pdfUrl = paper.pdfUrl,
        categories = Json.encodeToString(paper.categories),
        primaryCategory = paper.primaryCategory,
        comment = paper.comment,
        savedAt = 0L,
    )

    private lateinit var repository: RoomSavedPaperRepository

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = RoomSavedPaperRepository(dao)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class ObserveAll {

        @Test
        fun `maps entity list to domain papers`() = runTest {
            every { dao.observeAll() } returns flowOf(listOf(entity))

            repository.observeAll().test {
                val papers = awaitItem()
                assertEquals(1, papers.size)
                with(papers[0]) {
                    assertEquals(paper.id, id)
                    assertEquals(paper.title, title)
                    assertEquals(paper.authors, authors)
                    assertEquals(paper.categories, categories)
                    assertEquals(paper.comment, comment)
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `returns empty list when dao has no entries`() = runTest {
            every { dao.observeAll() } returns flowOf(emptyList())

            repository.observeAll().test {
                assertTrue(awaitItem().isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class ObserveIsSaved {

        @Test
        fun `delegates to dao with same id`() = runTest {
            every { dao.observeIsSaved("2605.00001") } returns flowOf(true)

            repository.observeIsSaved("2605.00001").test {
                assertTrue(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `returns false when not saved`() = runTest {
            every { dao.observeIsSaved("2605.00001") } returns flowOf(false)

            repository.observeIsSaved("2605.00001").test {
                assertFalse(awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class Save {

        @Test
        fun `inserts entity with correct field mapping`() = runTest {
            val captured = slot<SavedPaperEntity>()
            io.mockk.coEvery { dao.insert(capture(captured)) } returns Unit

            repository.save(paper)

            coVerify(exactly = 1) { dao.insert(any()) }
            with(captured.captured) {
                assertEquals(paper.id, id)
                assertEquals(paper.title, title)
                assertEquals(Json.encodeToString(paper.authors), authors)
                assertEquals(Json.encodeToString(paper.categories), categories)
                assertEquals(paper.primaryCategory, primaryCategory)
                assertEquals(paper.comment, comment)
            }
        }

        @Test
        fun `encodes null comment as null`() = runTest {
            val paperNoComment = paper.copy(comment = null)
            val captured = slot<SavedPaperEntity>()
            io.mockk.coEvery { dao.insert(capture(captured)) } returns Unit

            repository.save(paperNoComment)

            assertNull(captured.captured.comment)
        }
    }

    @Nested
    inner class Remove {

        @Test
        fun `calls deleteById with the given id`() = runTest {
            io.mockk.coEvery { dao.deleteById(any()) } returns Unit

            repository.remove(paper.id)

            coVerify(exactly = 1) { dao.deleteById(paper.id) }
        }
    }
}
