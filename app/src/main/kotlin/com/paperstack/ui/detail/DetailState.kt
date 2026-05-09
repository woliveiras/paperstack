package com.paperstack.ui.detail

import com.paperstack.domain.model.Paper

data class DetailState(
    val paper: Paper? = null,
    val isSaved: Boolean = false,
    val isTogglingSave: Boolean = false,
    val error: String? = null,
)
