package com.paperstack.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.paperstack.data.local.datastore.SettingsDataStore
import com.paperstack.data.local.db.PaperStackDatabase
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

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
    fun provideDatabase(@ApplicationContext context: Context): PaperStackDatabase =
        Room.databaseBuilder(context, PaperStackDatabase::class.java, "paperstack.db")
            .build()

    @Provides
    @Singleton
    fun provideSavedPaperDao(db: PaperStackDatabase): SavedPaperDao = db.savedPaperDao()
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "PaperStack/1.0 (contact@paperstack.app)")
                .build()
            chain.proceed(request)
        }
        .build()

    @Provides
    @Singleton
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
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
