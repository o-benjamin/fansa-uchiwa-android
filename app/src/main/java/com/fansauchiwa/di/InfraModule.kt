package com.fansauchiwa.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.fansauchiwa.data.infra.AnalyticsDataSource
import com.fansauchiwa.data.infra.EventDataSource
import com.fansauchiwa.data.infra.EventLocalSource
import com.fansauchiwa.data.infra.FirebaseAnalyticsRemoteSource
import com.fansauchiwa.data.infra.ImageProcessingDataSource
import com.fansauchiwa.data.infra.ImageProcessingLocalSource
import com.fansauchiwa.data.infra.SettingsDataSource
import com.fansauchiwa.data.infra.SettingsLocalSource
import com.fansauchiwa.data.repository.ImageProcessingRepository
import com.fansauchiwa.data.repository.ImageProcessingRepositoryImpl
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsDataSource(
        impl: FirebaseAnalyticsRemoteSource
    ): AnalyticsDataSource

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAnalytics(
            @ApplicationContext context: Context
        ): FirebaseAnalytics {
            return FirebaseAnalytics.getInstance(context)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ImageProcessingModule {

    @Binds
    @Singleton
    abstract fun bindImageProcessingDataSource(
        impl: ImageProcessingLocalSource
    ): ImageProcessingDataSource

    @Binds
    @Singleton
    abstract fun bindImageProcessingRepository(
        impl: ImageProcessingRepositoryImpl
    ): ImageProcessingRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    @Singleton
    abstract fun bindSettingsDataSource(
        impl: SettingsLocalSource
    ): SettingsDataSource

    @Binds
    @Singleton
    abstract fun bindEventDataSource(
        impl: EventLocalSource
    ): EventDataSource

    companion object {
        @Provides
        @Singleton
        fun provideDataStore(
            @ApplicationContext context: Context
        ): DataStore<Preferences> {
            return context.dataStore
        }
    }
}
