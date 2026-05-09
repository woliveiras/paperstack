package com.paperstack.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.paperstack.data.local.datastore.SettingsDataStore
import com.paperstack.data.local.db.PaperstackDatabase
import com.paperstack.data.local.db.RoomSavedPaperRepository
import com.paperstack.data.local.db.SavedPaperDao
import com.paperstack.data.remote.ArxivApiService
import com.paperstack.data.remote.ArxivApiServiceImpl
import com.paperstack.data.repository.SavedPaperRepository
import com.paperstack.data.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.settingsDataStore
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PaperstackDatabase =
        Room.databaseBuilder(context, PaperstackDatabase::class.java, "paperstack.db")
            .build()

    @Provides
    @Singleton
    fun provideSavedPaperDao(db: PaperstackDatabase): SavedPaperDao = db.savedPaperDao()
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Paperstack/1.0 (contact@paperstack.app)")
                .build()
            chain.proceed(request)
        }
        .build()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsDataStore): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindArxivApiService(impl: ArxivApiServiceImpl): ArxivApiService

    @Binds
    @Singleton
    abstract fun bindSavedPaperRepository(impl: RoomSavedPaperRepository): SavedPaperRepository
}
