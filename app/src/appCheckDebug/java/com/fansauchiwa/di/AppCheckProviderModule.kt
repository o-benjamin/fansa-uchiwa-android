package com.fansauchiwa.di

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * debug と beta の App Check provider（#337）。ストアを通らないビルドは Play Integrity を通らないため、デバッグ用を使う。
 *
 * 初めてトークンを取るときに、デバッグ用のトークンが logcat に出る（`DebugAppCheckProvider` のタグ）。
 * Firebase コンソールの App Check →「デバッグトークンを管理」に登録すると、サーバーを呼べるようになる
 */
@Module
@InstallIn(SingletonComponent::class)
object AppCheckProviderModule {

    @Provides
    @Singleton
    fun provideAppCheckProviderFactory(): AppCheckProviderFactory =
        DebugAppCheckProviderFactory.getInstance()
}
