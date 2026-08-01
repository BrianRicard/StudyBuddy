package com.studybuddy.core.common.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.datetime.Clock

/**
 * Injecting the clock keeps "is this card due?" honest: use cases read the time
 * per emission instead of freezing it when a flow is built, and tests can pin it.
 */
@Module
@InstallIn(SingletonComponent::class)
object ClockModule {

    @Provides
    fun provideClock(): Clock = Clock.System
}
