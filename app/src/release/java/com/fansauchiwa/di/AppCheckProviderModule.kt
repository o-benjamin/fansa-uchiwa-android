package com.fansauchiwa.di

import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 製品版の App Check provider（#337）。Play Integrity で、ストアから入れた本物のアプリかを確かめる。
 *
 * 動かすには、Play Console で Play Integrity API を Firebase のプロジェクトにつなぎ、
 * Firebase コンソールの App Check にアプリを登録する。登録する SHA-256 は Play Console の「アプリの署名」にある
 * アプリ署名鍵のもの（Google Play が署名し直すため、アップロード鍵のものでは通らない）
 */
@Module
@InstallIn(SingletonComponent::class)
object AppCheckProviderModule {

    @Provides
    @Singleton
    fun provideAppCheckProviderFactory(): AppCheckProviderFactory =
        PlayIntegrityAppCheckProviderFactory.getInstance()
}
