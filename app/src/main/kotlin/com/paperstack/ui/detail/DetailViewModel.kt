package com.paperstack.ui.detail

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.paperstack.data.repository.SavedPaperRepository
import com.paperstack.domain.model.Paper
import com.paperstack.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val savedPaperRepository: SavedPaperRepository,
    @ApplicationContext private val appContext: Context,
    private val okHttpClient: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state.asStateFlow()

    init {
        val paperJson: String = checkNotNull(savedStateHandle["paperJson"])
        val paper: Paper = Json.decodeFromString(paperJson)
        _state.update { it.copy(paper = paper) }

        savedPaperRepository.observeIsSaved(paper.id)
            .onEach { isSaved -> _state.update { it.copy(isSaved = isSaved) } }
            .launchIn(viewModelScope)

        checkIfAlreadyDownloaded(paper)
    }

    private fun checkIfAlreadyDownloaded(paper: Paper) {
        val file = localFile(paper.id)
        if (file.exists()) {
            _state.update { it.copy(downloadState = DownloadState.Downloaded) }
        }
    }

    fun toggleSave() {
        val paper = _state.value.paper ?: return
        if (_state.value.isTogglingSave) return
        _state.update { it.copy(isTogglingSave = true) }
        viewModelScope.launch {
            try {
                if (_state.value.isSaved) {
                    savedPaperRepository.remove(paper.id)
                } else {
                    savedPaperRepository.save(paper)
                }
            } finally {
                _state.update { it.copy(isTogglingSave = false) }
            }
        }
    }

    fun download(paper: Paper) {
        if (_state.value.downloadState is DownloadState.Downloading) return
        _state.update { it.copy(downloadState = DownloadState.Downloading(0f)) }
        viewModelScope.launch {
            try {
                val file = localFile(paper.id)
                file.parentFile?.mkdirs()
                var success = false
                withContext(ioDispatcher) {
                    val request = Request.Builder().url(paper.pdfUrl).build()
                    okHttpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            _state.update { it.copy(downloadState = DownloadState.Error("Server error: ${response.code}")) }
                            return@withContext
                        }
                        val body = response.body
                            ?: run {
                                _state.update { it.copy(downloadState = DownloadState.Error("Empty response")) }
                                return@withContext
                            }
                        val contentLength = body.contentLength()
                        var downloaded = 0L
                        file.outputStream().use { out ->
                            body.byteStream().use { input ->
                                val buffer = ByteArray(8192)
                                var read: Int
                                while (input.read(buffer).also { read = it } != -1) {
                                    out.write(buffer, 0, read)
                                    downloaded += read
                                    if (contentLength > 0) {
                                        val progress = downloaded.toFloat() / contentLength.toFloat()
                                        _state.update { it.copy(downloadState = DownloadState.Downloading(progress)) }
                                    }
                                }
                            }
                        }
                        success = true
                    }
                }
                if (success) {
                    _state.update { it.copy(downloadState = DownloadState.Downloaded) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(downloadState = DownloadState.Error(e.message ?: "Download failed")) }
            }
        }
    }

    fun openPdf(paper: Paper, context: Context) {
        val file = localFile(paper.id)
        if (!file.exists()) return
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun localFile(paperId: String): File =
        File(appContext.filesDir, "papers/$paperId.pdf")
}
