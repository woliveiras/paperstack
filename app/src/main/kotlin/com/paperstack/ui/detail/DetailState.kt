package com.paperstack.ui.detail

import com.paperstack.domain.model.Paper

sealed interface DownloadState {
    data object Idle : DownloadState
    data class Downloading(val progress: Float) : DownloadState
    data object Downloaded : DownloadState
    data class Error(val message: String) : DownloadState
}

data class DetailState(
    val paper: Paper? = null,
    val isSaved: Boolean = false,
    val isTogglingSave: Boolean = false,
    val error: String? = null,
    val downloadState: DownloadState = DownloadState.Idle,
)
