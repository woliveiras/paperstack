package com.paperstack.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_papers")
data class SavedPaperEntity(
    @PrimaryKey val id: String,
    val title: String,
    val authors: String,       // JSON list
    val abstract: String,
    val submittedDate: String,
    val updatedDate: String,
    val pdfUrl: String,
    val categories: String,    // JSON list
    val primaryCategory: String,
    val comment: String?,
    val savedAt: Long = System.currentTimeMillis(),
)
