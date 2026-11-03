package com.fansauchiwa.di

import com.fansauchiwa.data.infra.DisplayDensityDataSource
import com.fansauchiwa.data.infra.DisplayDensityLocalSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DisplayModule {

    @Binds
    @Singleton
    abstract fun bindDisplayDensityDataSource(
        impl: DisplayDensityLocalSource
    ): DisplayDensityDataSource
}
