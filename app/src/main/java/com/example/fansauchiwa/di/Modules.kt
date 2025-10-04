package com.example.fansauchiwa.di

import com.example.fansauchiwa.data.FansaUchiwaRepository
import com.example.fansauchiwa.data.FansaUchiwaRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class Modules {

    @Binds
    @Singleton
    abstract fun bindUchiwaRepository(
        fansaUchiwaRepositoryImpl: FansaUchiwaRepositoryImpl
    ): FansaUchiwaRepository
}
