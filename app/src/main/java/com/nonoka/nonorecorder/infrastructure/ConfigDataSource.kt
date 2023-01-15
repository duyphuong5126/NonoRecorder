package com.nonoka.nonorecorder.infrastructure

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import timber.log.Timber

interface ConfigDataSource {
    suspend fun saveInt(key: String, value: Int)
    fun getInt(key: String, defaultValue: Int): Int

    companion object {
        const val recordingSetting = "recording_setting"
        const val generalSetting = "general_setting"
    }
}

class ConfigDataSourceImpl @Inject constructor(
    @ApplicationContext context: Context,
    groupName: String,
) : ConfigDataSource {
    private val sharedPreferences =
        context.getSharedPreferences(groupName, Context.MODE_PRIVATE)

    override suspend fun saveInt(key: String, value: Int) {
        val result = sharedPreferences.edit().putInt(key, value).commit()
        Timber.d("Result of saving config ($key, $value): $result")
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        val result = sharedPreferences.getInt(key, defaultValue)
        Timber.d("Config $key, value: $result")
        return result
    }
}