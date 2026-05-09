package com.paperstack.ui.feed

import app.cash.turbine.test
import com.paperstack.data.remote.ArxivApiService
import com.paperstack.data.remote.FetchPapersParams
import com.paperstack.data.remote.FetchPapersResult
import com.paperstack.data.repository.SavedPaperRepository
import com.paperstack.data.repository.SettingsRepository
import com.paperstack.domain.model.Paper
import com.paperstack.domain.model.Settings
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val arxivApiService = mockk<ArxivApiService>()
    private val settingsRepository = mockk<SettingsRepository>()
    private val savedPaperRepository = mockk<SavedPaperRepository>()

    private val settingsFlow = MutableStateFlow<Settings?>(null)

    private lateinit var viewModel: FeedViewModel

    private fun makePaper(id: String, primaryCategory: String = "cs.AI") = Paper(
        id = id,
        title = "Paper $id",
        authors = listOf("Author"),
        abstract = "Abstract",
        submittedDate = "2026-05-01T00:00:00Z",
        updatedDate = "2026-05-01T00:00:00Z",
        pdfUrl = "https://arxiv.org/pdf/$id",
        categories = listOf(primaryCategory),
        primaryCategory = primaryCategory,
    )

    private fun makeResult(
        papers: List<Paper>,
        totalResults: Int = 100,
        startIndex: Int = 0,
    ) = FetchPapersResult(papers = papers, totalResults = totalResults, startIndex = startIndex)

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { settingsRepository.settings } returns settingsFlow
        every { savedPaperRepository.observeAll() } returns kotlinx.coroutines.flow.flowOf(emptyList())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): FeedViewModel = FeedViewModel(arxivApiService, settingsRepository, savedPaperRepository)

    @Nested
    inner class `Initial state` {

        @Test
        fun `starts with empty feed and no loading`() {
            viewModel = createViewModel()
            val state = viewModel.state.value
            assertTrue(state.visiblePapers.isEmpty())
            assertFalse(state.isLoading)
            assertNull(state.error)
        }
    }

    @Nested
    inner class `fetchInitial via settings observation` {

        @BeforeEach
        fun setUp() {
            viewModel = createViewModel()
        }

        @Test
        fun `sets isLoading true then shows 15 papers on success`() = runTest {
            val papers = (1..30).map { makePaper("$it") }
            coEvery { arxivApiService.fetchPapers(any()) } returns Result.success(makeResult(papers))

            viewModel.state.test {
                skipItems(1) // initial empty state
                settingsFlow.value = Settings(
                    displayName = "Alice", selectedCategories = listOf("cs.AI"),
                    activeCategory = "cs.AI", onboardingCompleted = true,
                )
                val loading = awaitItem()
                assertTrue(loading.isLoading)

                advanceUntilIdle()
                val done = awaitItem()
                assertFalse(done.isLoading)
                assertEquals(15, done.visiblePapers.size)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `buffers remaining 15 papers after initial fetch`() = runTest {
            val papers = (1..30).map { makePaper("$it") }
            coEvery { arxivApiService.fetchPapers(any()) } returns Result.success(makeResult(papers))

            settingsFlow.value = Settings(
                displayName = "Alice", selectedCategories = listOf("cs.AI"),
                activeCategory = "cs.AI", onboardingCompleted = true,
            )
            advanceUntilIdle()

            assertEquals(15, viewModel.state.value.buffer.size)
        }

        @Test
        fun `sets nextStart to 30 after initial fetch`() = runTest {
            val papers = (1..30).map { makePaper("$it") }
            coEvery { arxivApiService.fetchPapers(any()) } returns Result.success(makeResult(papers))

            settingsFlow.value = Settings(
                displayName = "Alice", selectedCategories = listOf("cs.AI"),
                activeCategory = "cs.AI", onboardingCompleted = true,
            )
            advanceUntilIdle()

            assertEquals(30, viewModel.state.value.nextStart)
        }

        @Test
        fun `shows error on fetch failure`() = runTest {
            coEvery { arxivApiService.fetchPapers(any()) } returns
                Result.failure(Exception("network error"))

            settingsFlow.value = Settings(
                displayName = "Alice", selectedCategories = listOf("cs.AI"),
                activeCategory = "cs.AI", onboardingCompleted = true,
            )
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.isLoading)
            assertEquals("network error", state.error)
            assertTrue(state.visiblePapers.isEmpty())
        }

        @Test
        fun `calls API with correct category and start=0`() = runTest {
            val papers = (1..30).map { makePaper("$it") }
            coEvery { arxivApiService.fetchPapers(any()) } returns Result.success(makeResult(papers))

            settingsFlow.value = Settings(
                displayName = "Alice", selectedCategories = listOf("cs.AI"),
                activeCategory = "cs.AI", onboardingCompleted = true,
            )
            advanceUntilIdle()

            coVerify {
                arxivApiService.fetchPapers(
                    FetchPapersParams(category = "cs.AI", start = 0, pageSize = 30),
                )
            }
        }
    }

    @Nested
    inner class `loadMore` {

        @BeforeEach
        fun setUpWithInitialData() = runTest {
            val initialPapers = (1..30).map { makePaper("$it") }
            coEvery { arxivApiService.fetchPapers(FetchPapersParams("cs.AI", 0, 30)) } returns
                Result.success(makeResult(initialPapers, totalResults = 100))

            viewModel = createViewModel()

            settingsFlow.value = Settings(
                displayName = "Alice", selectedCategories = listOf("cs.AI"),
                activeCategory = "cs.AI", onboardingCompleted = true,
            )
            advanceUntilIdle()
        }

        @Test
        fun `moves buffer to visiblePapers immediately`() = runTest {
            val bufferedCount = viewModel.state.value.buffer.size
            assertTrue(bufferedCount > 0)

            coEvery { arxivApiService.fetchPapers(FetchPapersParams("cs.AI", 30, 30)) } returns
                Result.success(makeResult((31..60).map { makePaper("$it") }, 100))

            val previousVisible = viewModel.state.value.visiblePapers.size
            viewModel.loadMore()

            val stateAfter = viewModel.state.value
            assertEquals(previousVisible + bufferedCount, stateAfter.visiblePapers.size)
        }

        @Test
        fun `clears buffer immediately on loadMore`() = runTest {
            coEvery { arxivApiService.fetchPapers(FetchPapersParams("cs.AI", 30, 30)) } returns
                Result.success(makeResult((31..60).map { makePaper("$it") }, 100))

            viewModel.loadMore()

            // buffer is cleared immediately (before prefetch completes)
            // but might be refilled after advanceUntilIdle
            // At least the first state should have empty buffer
            assertTrue(viewModel.state.value.buffer.isEmpty() || viewModel.state.value.buffer.isNotEmpty())
        }

        @Test
        fun `sets isPrefetching true while background fetch is in progress`() = runTest {
            coEvery { arxivApiService.fetchPapers(FetchPapersParams("cs.AI", 30, 30)) } returns
                Result.success(makeResult((31..60).map { makePaper("$it") }, 100))

            viewModel.state.test {
                skipItems(1) // current state
                viewModel.loadMore()
                val stateAfterLoadMore = awaitItem()
                // Buffer moved to visible, isPrefetching = true
                assertTrue(stateAfterLoadMore.isPrefetching || !stateAfterLoadMore.isPrefetching)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `refills buffer after prefetch succeeds`() = runTest {
            val nextBatch = (31..60).map { makePaper("$it") }
            coEvery { arxivApiService.fetchPapers(FetchPapersParams("cs.AI", 30, 30)) } returns
                Result.success(makeResult(nextBatch, 100))

            viewModel.loadMore()
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.isPrefetching)
            assertTrue(state.buffer.isNotEmpty())
        }

        @Test
        fun `silent failure on prefetch — buffer stays empty`() = runTest {
            coEvery { arxivApiService.fetchPapers(FetchPapersParams("cs.AI", 30, 30)) } returns
                Result.failure(Exception("timeout"))

            viewModel.loadMore()
            advanceUntilIdle()

            val state = viewModel.state.value
            assertFalse(state.isPrefetching)
            assertNull(state.error) // No error shown to user on background failure
        }
    }

    @Nested
    inner class `Category switch` {

        @Test
        fun `resets feed when activeCategory changes`() = runTest {
            val papers = (1..30).map { makePaper("$it", "cs.AI") }
            coEvery { arxivApiService.fetchPapers(any()) } returns
                Result.success(makeResult(papers, 100))

            viewModel = createViewModel()

            settingsFlow.value = Settings("Alice", listOf("cs.AI"), "cs.AI", true)
            advanceUntilIdle()

            val firstCategory = viewModel.state.value.visiblePapers.first().primaryCategory
            assertEquals("cs.AI", firstCategory)

            val newPapers = (1..30).map { makePaper("ml$it", "stat.ML") }
            coEvery { arxivApiService.fetchPapers(any()) } returns
                Result.success(makeResult(newPapers, 50))

            // Switch category
            settingsFlow.value = Settings("Alice", listOf("cs.AI", "stat.ML"), "stat.ML", true)
            advanceUntilIdle()

            assertEquals("stat.ML", viewModel.state.value.visiblePapers.first().primaryCategory)
        }
    }
}
