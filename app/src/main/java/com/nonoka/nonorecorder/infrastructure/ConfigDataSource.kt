package com.nonoka.nonorecorder.infrastructure

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import timber.log.Timber

interface ConfigDataSource {
    suspend fun saveInt(key: String, value: Int)
    suspend fun saveBoolean(key: String, value: Boolean)
    fun getInt(key: String, defaultValue: Int): Int
    fun getInt(key: String): Int?
    fun getBoolean(key: String, defaultValue: Boolean): Boolean

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

    override suspend fun saveBoolean(key: String, value: Boolean) {
        val result = sharedPreferences.edit().putBoolean(key, value).commit()
        Timber.d("Result of saving config ($key, $value): $result")
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        val result = sharedPreferences.getInt(key, defaultValue)
        Timber.d("Config $key, value: $result")
        return result
    }

    override fun getInt(key: String): Int? {
        val result = sharedPreferences.getInt(key, Int.MIN_VALUE)
        Timber.d("Config $key, value: $result")
        return if (result > Int.MIN_VALUE) result else null
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        val result = sharedPreferences.getBoolean(key, defaultValue)
        Timber.d("Config $key, value: $result")
        return result
    }
}