package com.example.fansauchiwa.di

import android.content.Context
import androidx.room.Room
import com.example.fansauchiwa.data.GalleryImageDataSource
import com.example.fansauchiwa.data.GalleryImageLocalSource
import com.example.fansauchiwa.data.ImageDataSource
import com.example.fansauchiwa.data.ImageLocalSource
import com.example.fansauchiwa.data.LocalDatabaseRepository
import com.example.fansauchiwa.data.LocalDatabaseRepositoryImpl
import com.example.fansauchiwa.data.LocalImageRepository
import com.example.fansauchiwa.data.LocalImageRepositoryImpl
import com.example.fansauchiwa.data.MasterpieceDataSource
import com.example.fansauchiwa.data.MasterpieceLocalSource
import com.example.fansauchiwa.data.MasterpieceRepository
import com.example.fansauchiwa.data.MasterpieceRepositoryImpl
import com.example.fansauchiwa.data.source.FansaUchiwaDao
import com.example.fansauchiwa.data.source.FansaUchiwaDatabase
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
