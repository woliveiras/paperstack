package com.paperstack.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Paper(
    val id: String,
    val title: String,
    val authors: List<String>,
    val abstract: String,
    val submittedDate: String,
    val updatedDate: String,
    val pdfUrl: String,
    val categories: List<String>,
    val primaryCategory: String,
    val comment: String? = null,
)
