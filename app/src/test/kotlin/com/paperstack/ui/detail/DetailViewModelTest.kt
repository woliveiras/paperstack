package com.paperstack.ui.detail

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val savedPaperRepository = mockk<SavedPaperRepository>()
    private val isSavedFlow = MutableStateFlow(false)

    private val paper = Paper(
        id = "2605.00001",
        title = "Test Paper",
        authors = listOf("Alice", "Bob", "Carol"),
        abstract = "A long abstract.",
        submittedDate = "2026-05-01T00:00:00Z",
        updatedDate = "2026-05-01T00:00:00Z",
        pdfUrl = "https://arxiv.org/pdf/2605.00001",
        categories = listOf("cs.AI", "cs.LG"),
        primaryCategory = "cs.AI",
        comment = "Accepted at ICML 2026",
    )

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { savedPaperRepository.observeIsSaved(any()) } returns isSavedFlow
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): DetailViewModel {
        val handle = SavedStateHandle(mapOf("paperJson" to Json.encodeToString(paper)))
        return DetailViewModel(handle, savedPaperRepository)
    }

    @Nested
    inner class `Initial state` {

        @Test
        fun `loads paper from SavedStateHandle`() = runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertNotNull(viewModel.state.value.paper)
            assertEquals(paper.id, viewModel.state.value.paper?.id)
        }

        @Test
        fun `isSaved starts false`() = runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertFalse(viewModel.state.value.isSaved)
        }

        @Test
        fun `isSaved reflects repository flow`() = runTest {
            val viewModel = createViewModel()
            viewModel.state.test {
                skipItems(1)
                isSavedFlow.value = true
                advanceUntilIdle()
                val state = awaitItem()
                assertTrue(state.isSaved)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `toggleSave` {

        @Test
        fun `calls save when paper is not saved`() = runTest {
            io.mockk.coEvery { savedPaperRepository.save(any()) } returns Unit
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.toggleSave()
            advanceUntilIdle()

            coVerify { savedPaperRepository.save(paper) }
        }

        @Test
        fun `calls remove when paper is already saved`() = runTest {
            io.mockk.coEvery { savedPaperRepository.remove(any()) } returns Unit
            isSavedFlow.value = true
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.toggleSave()
            advanceUntilIdle()

            coVerify { savedPaperRepository.remove(paper.id) }
        }
    }
}
