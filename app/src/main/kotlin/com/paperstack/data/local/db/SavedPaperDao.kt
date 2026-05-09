package com.paperstack.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedPaperDao {

    @Query("SELECT * FROM saved_papers ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<SavedPaperEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_papers WHERE id = :id)")
    fun observeIsSaved(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(paper: SavedPaperEntity)

    @Query("DELETE FROM saved_papers WHERE id = :id")
    suspend fun deleteById(id: String)
}
