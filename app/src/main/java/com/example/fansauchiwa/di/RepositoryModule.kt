package com.example.fansauchiwa.di

import android.content.Context
import androidx.room.Room
import com.example.fansauchiwa.data.FansaUchiwaRepository
import com.example.fansauchiwa.data.FansaUchiwaRepositoryImpl
import com.example.fansauchiwa.data.ImageDataSource
import com.example.fansauchiwa.data.ImageLocalSource
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
    abstract fun bindFansaUchiwaRepository(
        fansaUchiwaRepositoryImpl: FansaUchiwaRepositoryImpl
    ): FansaUchiwaRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Binds
    @Singleton
    abstract fun bindImageDataSource(
        impl: ImageLocalSource
    ): ImageDataSource
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
