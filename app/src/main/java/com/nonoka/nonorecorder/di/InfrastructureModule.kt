package com.nonoka.nonorecorder.di

import android.content.Context
import com.nonoka.nonorecorder.di.qualifier.GeneralSetting
import com.nonoka.nonorecorder.di.qualifier.RecordingSetting
import com.nonoka.nonorecorder.infrastructure.ConfigDataSource
import com.nonoka.nonorecorder.infrastructure.ConfigDataSource.Companion.generalSetting
import com.nonoka.nonorecorder.infrastructure.ConfigDataSource.Companion.recordingSetting
import com.nonoka.nonorecorder.infrastructure.ConfigDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class InfrastructureModule {
    @Provides
    @Singleton
    @RecordingSetting
    fun providesRecordingConfigDataSource(@ApplicationContext appContext: Context): ConfigDataSource {
        return ConfigDataSourceImpl(appContext, recordingSetting)
    }

    @Provides
    @Singleton
    @GeneralSetting
    fun providesGeneralConfigDataSource(@ApplicationContext appContext: Context): ConfigDataSource {
        return ConfigDataSourceImpl(appContext, generalSetting)
    }
}