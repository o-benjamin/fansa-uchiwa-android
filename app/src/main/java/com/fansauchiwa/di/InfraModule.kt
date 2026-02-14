package com.fansauchiwa.di

import android.content.Context
import com.fansauchiwa.data.infra.AnalyticsDataSource
import com.fansauchiwa.data.infra.FirebaseAnalyticsRemoteSource
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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


