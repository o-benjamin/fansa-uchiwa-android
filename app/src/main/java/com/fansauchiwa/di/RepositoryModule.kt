package com.fansauchiwa.di

import android.content.Context
import androidx.room.Room
import com.fansauchiwa.data.AdMobRepository
import com.fansauchiwa.data.AdMobRepositoryImpl
import com.fansauchiwa.data.GalleryImageDataSource
import com.fansauchiwa.data.GalleryImageLocalSource
import com.fansauchiwa.data.ImageDataSource
import com.fansauchiwa.data.ImageLocalSource
import com.fansauchiwa.data.LocalDatabaseRepository
import com.fansauchiwa.data.LocalDatabaseRepositoryImpl
import com.fansauchiwa.data.LocalImageRepository
import com.fansauchiwa.data.LocalImageRepositoryImpl
import com.fansauchiwa.data.MasterpieceDataSource
import com.fansauchiwa.data.MasterpieceLocalSource
import com.fansauchiwa.data.MasterpieceRepository
import com.fansauchiwa.data.MasterpieceRepositoryImpl
import com.fansauchiwa.data.UuidProvider
import com.fansauchiwa.data.UuidProviderImpl
import com.fansauchiwa.data.repository.AnalyticsRepository
import com.fansauchiwa.data.repository.AnalyticsRepositoryImpl
import com.fansauchiwa.data.repository.DefaultTemplateRepository
import com.fansauchiwa.data.repository.TemplateRepository
import com.fansauchiwa.data.source.FansaUchiwaDao
import com.fansauchiwa.data.source.FansaUchiwaDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMasterpieceRepository(
        masterpieceRepositoryImpl: MasterpieceRepositoryImpl
    ): MasterpieceRepository

    @Binds
    @Singleton
    abstract fun bindLocalImageRepository(
        localImageRepositoryImpl: LocalImageRepositoryImpl
    ): LocalImageRepository

    @Binds
    @Singleton
    abstract fun bindLocalDatabaseRepository(
        localDatabaseRepositoryImpl: LocalDatabaseRepositoryImpl
    ): LocalDatabaseRepository

    @Binds
    @Singleton
    abstract fun bindAdMobRepository(
        adMobRepositoryImpl: AdMobRepositoryImpl
    ): AdMobRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(
        impl: AnalyticsRepositoryImpl
    ): AnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindTemplateRepository(
        impl: DefaultTemplateRepository
    ): TemplateRepository

    @Binds
    @Singleton
    abstract fun bindUuidProvider(
        impl: UuidProviderImpl
    ): UuidProvider
}

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Binds
    @Singleton
    abstract fun bindImageDataSource(
        impl: ImageLocalSource
    ): ImageDataSource

    @Binds
    @Singleton
    abstract fun bindMasterpieceDataSource(
        impl: MasterpieceLocalSource
    ): MasterpieceDataSource

    @Binds
    @Singleton
    abstract fun bindGalleryImageDataSource(
        impl: GalleryImageLocalSource
    ): GalleryImageDataSource
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext context: Context): FansaUchiwaDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            FansaUchiwaDatabase::class.java,
            "uchiwaData.db"
        ).build()
    }

    @Provides
    fun provideTaskDao(database: FansaUchiwaDatabase): FansaUchiwaDao {
        return database.uchiwaDao()
    }
}
