package com.nonoka.nonorecorder

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.nonoka.nonorecorder.di.qualifier.GeneralSetting
import com.nonoka.nonorecorder.domain.entity.SettingCategory.DARK_THEME
import com.nonoka.nonorecorder.infrastructure.ConfigDataSource
import com.nonoka.nonorecorder.theme.NightMode
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import timber.log.Timber

@HiltAndroidApp
class App : Application() {
    @Inject
    @GeneralSetting
    lateinit var generalConfigDataSource: ConfigDataSource

    var nightModeSetting by mutableStateOf(NightMode.System)

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        generalConfigDataSource.getInt(DARK_THEME.name)?.let {
            nightModeSetting = NightMode.fromId(it)
        }
    }
}