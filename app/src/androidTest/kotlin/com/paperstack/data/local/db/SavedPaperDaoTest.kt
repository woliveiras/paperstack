package com.paperstack.data.local.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class SavedPaperDaoTest {

    private lateinit var db: PaperstackDatabase
    private lateinit var dao: SavedPaperDao

    private val entity = SavedPaperEntity(
        id = "2605.00001",
        title = "Test Paper",
        authors = """["Alice","Bob"]""",
        abstract = "Abstract.",
        submittedDate = "2026-05-01T00:00:00Z",
        updatedDate = "2026-05-01T00:00:00Z",
        pdfUrl = "https://arxiv.org/pdf/2605.00001",
        categories = """["cs.AI"]""",
        primaryCategory = "cs.AI",
        comment = null,
        savedAt = 1000L,
    )

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PaperstackDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.savedPaperDao()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun insertAndObserveAll() = runTest {
        dao.insert(entity)
        dao.observeAll().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("2605.00001", list[0].id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeIsSaved_falseWhenNotInserted() = runTest {
        dao.observeIsSaved("2605.00001").test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeIsSaved_trueAfterInsert() = runTest {
        dao.observeIsSaved("2605.00001").test {
            assertFalse(awaitItem())
            dao.insert(entity)
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteById_removesEntity() = runTest {
        dao.insert(entity)
        dao.deleteById("2605.00001")
        dao.observeAll().test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun replaceOnConflict_updatesExisting() = runTest {
        dao.insert(entity)
        val updated = entity.copy(title = "Updated Title")
        dao.insert(updated)
        dao.observeAll().test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("Updated Title", list[0].title)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
