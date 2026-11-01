package com.fansauchiwa.di

import com.fansauchiwa.data.TemplateCandidates
import com.fansauchiwa.data.templateCandidateList
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object TemplateCandidateModule {

    @Provides
    fun provideTemplateCandidates(): TemplateCandidates = TemplateCandidates(templateCandidateList)
}
