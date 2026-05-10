package com.paperstack.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SavedPaperEntity::class], version = 1, exportSchema = true)
abstract class PaperStackDatabase : RoomDatabase() {
    abstract fun savedPaperDao(): SavedPaperDao
}
