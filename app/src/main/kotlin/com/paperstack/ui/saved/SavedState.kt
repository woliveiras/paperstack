package com.paperstack.ui.saved

import com.paperstack.domain.model.Paper

data class SavedState(
    val papers: List<Paper> = emptyList(),
    val isLoading: Boolean = true,
)
