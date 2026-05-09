package com.paperstack.ui.detail

import android.content.Context
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val savedPaperRepository = mockk<SavedPaperRepository>()
    private val isSavedFlow = MutableStateFlow(false)
    private val mockWebServer = MockWebServer()
    private val okHttpClient = OkHttpClient()
    private val appContext = mockk<Context>(relaxed = true)

    @TempDir
    lateinit var tempDir: File

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
        every { appContext.filesDir } returns tempDir
        every { appContext.packageName } returns "com.paperstack"
        mockWebServer.start()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        mockWebServer.shutdown()
    }

    private fun createViewModel(overridePaper: Paper = paper): DetailViewModel {
        val handle = SavedStateHandle(mapOf("paperJson" to Json.encodeToString(overridePaper)))
        return DetailViewModel(handle, savedPaperRepository, appContext, okHttpClient, testDispatcher)
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

        @Test
        fun `downloadState is Idle when no local file exists`() = runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertEquals(DownloadState.Idle, viewModel.state.value.downloadState)
        }

        @Test
        fun `downloadState is Downloaded when local file already exists`() = runTest {
            val papersDir = File(tempDir, "papers").also { it.mkdirs() }
            File(papersDir, "${paper.id}.pdf").writeBytes(ByteArray(10))
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertEquals(DownloadState.Downloaded, viewModel.state.value.downloadState)
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

    @Nested
    inner class `download` {

        @Test
        fun `transitions to Downloaded on successful download`() = runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody("PDF bytes")
                    .addHeader("Content-Type", "application/pdf"),
            )
            val serverPaper = paper.copy(pdfUrl = mockWebServer.url("/pdf/${paper.id}").toString())
            val viewModel = createViewModel(overridePaper = serverPaper)
            advanceUntilIdle()

            viewModel.download(serverPaper)
            advanceUntilIdle()

            assertEquals(DownloadState.Downloaded, viewModel.state.value.downloadState)
        }

        @Test
        fun `transitions to Error on server error`() = runTest {
            mockWebServer.enqueue(MockResponse().setResponseCode(503))
            val serverPaper = paper.copy(pdfUrl = mockWebServer.url("/pdf/${paper.id}").toString())
            val viewModel = createViewModel(overridePaper = serverPaper)
            advanceUntilIdle()

            viewModel.download(serverPaper)
            advanceUntilIdle()

            assertTrue(viewModel.state.value.downloadState is DownloadState.Error)
        }

        @Test
        fun `concurrent download calls do not start second download`() = runTest {
            mockWebServer.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody("PDF bytes"),
            )
            val serverPaper = paper.copy(pdfUrl = mockWebServer.url("/pdf/${paper.id}").toString())
            val viewModel = createViewModel(overridePaper = serverPaper)
            advanceUntilIdle()

            viewModel.download(serverPaper)
            viewModel.download(serverPaper) // second call ignored
            advanceUntilIdle()

            assertEquals(1, mockWebServer.requestCount)
        }
    }
}
