package com.studybuddy.core.data.di

import com.studybuddy.core.data.network.PoetryDbClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun providePoetryDbClient(): PoetryDbClient = PoetryDbClient()
}
