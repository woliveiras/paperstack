package com.paperstack.ui.saved

import app.cash.turbine.test
import com.paperstack.data.repository.SavedPaperRepository
import com.paperstack.domain.model.Paper
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SavedViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val savedPaperRepository = mockk<SavedPaperRepository>()
    private val papersFlow = MutableStateFlow<List<Paper>>(emptyList())

    private val paper = Paper(
        id = "2605.00001",
        title = "Test Paper",
        authors = listOf("Alice", "Bob"),
        abstract = "Abstract text.",
        submittedDate = "2026-05-01T00:00:00Z",
        updatedDate = "2026-05-01T00:00:00Z",
        pdfUrl = "https://arxiv.org/pdf/2605.00001",
        categories = listOf("cs.AI"),
        primaryCategory = "cs.AI",
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { savedPaperRepository.observeAll() } returns papersFlow
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SavedViewModel(savedPaperRepository)

    @Nested
    inner class InitialState {

        @Test
        fun `starts in loading state`() = runTest {
            val vm = createViewModel()
            assertTrue(vm.state.value.isLoading)
        }

        @Test
        fun `emits empty list when repository returns no papers`() = runTest {
            val vm = createViewModel()
            advanceUntilIdle()
            assertFalse(vm.state.value.isLoading)
            assertTrue(vm.state.value.papers.isEmpty())
        }
    }

    @Nested
    inner class PaperList {

        @Test
        fun `reflects papers from repository`() = runTest {
            val vm = createViewModel()
            papersFlow.value = listOf(paper)
            advanceUntilIdle()
            assertEquals(listOf(paper), vm.state.value.papers)
        }

        @Test
        fun `updates when repository emits new list`() = runTest {
            val vm = createViewModel()
            vm.state.test {
                awaitItem() // loading
                papersFlow.value = listOf(paper)
                val withPaper = awaitItem()
                assertEquals(1, withPaper.papers.size)

                papersFlow.value = emptyList()
                val empty = awaitItem()
                assertTrue(empty.papers.isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class Remove {

        @Test
        fun `remove calls repository with paper id`() = runTest {
            io.mockk.coEvery { savedPaperRepository.remove(any()) } returns Unit
            val vm = createViewModel()
            vm.remove(paper)
            advanceUntilIdle()
            coVerify(exactly = 1) { savedPaperRepository.remove(paper.id) }
        }
    }
}
