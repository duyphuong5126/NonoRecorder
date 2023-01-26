package com.nonoka.nonorecorder.feature.main.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonoka.nonorecorder.BuildConfig
import com.nonoka.nonorecorder.constant.AppConstants.DEFAULT_CHANNELS
import com.nonoka.nonorecorder.constant.AppConstants.DEFAULT_ENCODING_BITRATE
import com.nonoka.nonorecorder.constant.AppConstants.DEFAULT_SAMPLING_RATE
import com.nonoka.nonorecorder.di.qualifier.GeneralSetting
import com.nonoka.nonorecorder.di.qualifier.RecordingSetting
import com.nonoka.nonorecorder.domain.entity.SettingCategory
import com.nonoka.nonorecorder.domain.entity.SettingCategory.AUDIO_CHANNELS
import com.nonoka.nonorecorder.domain.entity.SettingCategory.DARK_THEME
import com.nonoka.nonorecorder.domain.entity.SettingCategory.SAMPLING_RATE
import com.nonoka.nonorecorder.domain.entity.SettingCategory.ENCODING_BITRATE
import com.nonoka.nonorecorder.domain.entity.SettingCategory.USE_SHARED_STORAGE
import com.nonoka.nonorecorder.feature.main.settings.uimodel.SelectableSettingOption
import com.nonoka.nonorecorder.feature.main.settings.uimodel.SelectableSettingOption.IntegerSettingOption
import com.nonoka.nonorecorder.feature.main.settings.uimodel.SettingUiModel
import com.nonoka.nonorecorder.feature.main.settings.uimodel.SettingUiModel.SelectableSetting
import com.nonoka.nonorecorder.feature.main.settings.uimodel.SettingUiModel.SwitchSetting
import com.nonoka.nonorecorder.infrastructure.ConfigDataSource
import com.nonoka.nonorecorder.shared.exportFolder
import com.nonoka.nonorecorder.theme.NightMode
import com.nonoka.nonorecorder.theme.NightMode.Dark
import com.nonoka.nonorecorder.theme.NightMode.Light
import com.nonoka.nonorecorder.theme.NightMode.System
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @RecordingSetting private val recordingConfigDataSource: ConfigDataSource,
    @GeneralSetting private val generalConfigDataSource: ConfigDataSource
) : ViewModel() {
    val recordingSettings = mutableStateListOf<SettingUiModel>()
    val displaySettings = mutableStateListOf<SettingUiModel>()

    var storageLocationSetting by mutableStateOf<SwitchSetting?>(null)

    private val _nightMode: MutableSharedFlow<NightMode> = MutableSharedFlow()
    val nightMode: SharedFlow<NightMode> = _nightMode

    fun init() {
        initRecordingSettings()
        initDisplaySettings()
        if (BuildConfig.DEBUG) {
            initStorageSettings()
        }
    }

    fun onSelectRecordingSettingOption(
        settingOption: SelectableSettingOption,
        category: SettingCategory
    ) {
        val targetSettingList = if (category == DARK_THEME) displaySettings else recordingSettings
        targetSettingList.indexOfFirst {
            it.category == category
        }.let { settingIndex ->
            if (settingIndex >= 0) {
                val selectableSetting = (targetSettingList[settingIndex] as SelectableSetting)
                selectableSetting.options.indexOfFirst {
                    it.id == settingOption.id
                }.let { optionIndex ->
                    if (optionIndex >= 0) {
                        val newOption = selectableSetting.options[optionIndex]
                        targetSettingList[settingIndex] = selectableSetting.copy(
                            selectedIndex = optionIndex,
                            label = newOption.label,
                        )
                        if (newOption is IntegerSettingOption) {
                            viewModelScope.launch(Dispatchers.IO) {
                                recordingConfigDataSource.saveInt(category.id, newOption.value)
                            }
                            if (category == DARK_THEME) {
                                viewModelScope.launch(Dispatchers.Default) {
                                    _nightMode.emit(NightMode.fromId(newOption.value))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun onSwitchStateChange(switchSetting: SwitchSetting, newState: Boolean) {
        if (switchSetting.category == USE_SHARED_STORAGE) {
            storageLocationSetting?.let {
                storageLocationSetting = it.copy(value = newState)
                viewModelScope.launch {
                    generalConfigDataSource.saveBoolean(USE_SHARED_STORAGE.id, newState)
                }
            }
        }
    }

    private fun initRecordingSettings() {
        recordingSettings.clear()
        val samplingRateList = arrayListOf<IntegerSettingOption>()
        samplingRateList.add(
            IntegerSettingOption(
                id = SAMPLING_GROUP_ID + 1,
                label = "44.1 kHz",
                value = 44100
            )
        )
        samplingRateList.add(
            IntegerSettingOption(
                id = SAMPLING_GROUP_ID + 2,
                label = "48 kHz",
                value = 48000
            )
        )

        val storedSamplingRate =
            recordingConfigDataSource.getInt(SAMPLING_RATE.id, DEFAULT_SAMPLING_RATE)
        val storedSamplingRateIndex = samplingRateList.indexOfFirst {
            it.value == storedSamplingRate
        }
        recordingSettings.add(
            SelectableSetting(
                category = SAMPLING_RATE,
                name = "Sampling rate",
                options = samplingRateList,
                label = samplingRateList[storedSamplingRateIndex].label,
                selectedIndex = storedSamplingRateIndex
            )
        )

        val bitrateList = arrayListOf<IntegerSettingOption>()
        bitrateList.add(
            IntegerSettingOption(
                id = BITRATE_GROUP_ID + 1,
                label = "96 kbps",
                value = 96000
            )
        )
        bitrateList.add(
            IntegerSettingOption(
                id = BITRATE_GROUP_ID + 2,
                label = "128 kbps",
                value = 128000
            )
        )
        bitrateList.add(
            IntegerSettingOption(
                id = BITRATE_GROUP_ID + 3,
                label = "192 kbps",
                value = 192000
            )
        )
        bitrateList.add(
            IntegerSettingOption(
                id = BITRATE_GROUP_ID + 4,
                label = "256 kbps",
                value = 256000
            )
        )
        bitrateList.add(
            IntegerSettingOption(
                id = BITRATE_GROUP_ID + 5,
                label = "320 kbps",
                value = 320000
            )
        )
        val storedEncodingBitrate =
            recordingConfigDataSource.getInt(ENCODING_BITRATE.id, DEFAULT_ENCODING_BITRATE)
        val storedBitrateIndex = bitrateList.indexOfFirst {
            it.value == storedEncodingBitrate
        }
        recordingSettings.add(
            SelectableSetting(
                category = ENCODING_BITRATE,
                name = "Encoding bitrate",
                options = bitrateList,
                label = bitrateList[storedBitrateIndex].label,
                selectedIndex = storedBitrateIndex
            )
        )

        val audioChannelList = arrayListOf<IntegerSettingOption>()
        audioChannelList.add(
            IntegerSettingOption(
                id = AUDIO_CHANNELS_GROUP_ID + 1,
                "Mono",
                value = 1
            )
        )
        audioChannelList.add(
            IntegerSettingOption(
                id = AUDIO_CHANNELS_GROUP_ID + 2,
                "Stereo",
                value = 2
            )
        )
        val storedAudioChannelCount =
            recordingConfigDataSource.getInt(AUDIO_CHANNELS.id, DEFAULT_CHANNELS)
        val storedChannelCountIndex = audioChannelList.indexOfFirst {
            it.value == storedAudioChannelCount
        }
        recordingSettings.add(
            SelectableSetting(
                category = AUDIO_CHANNELS,
                name = "Audio channels",
                options = audioChannelList,
                label = audioChannelList[storedChannelCountIndex].label,
                selectedIndex = storedChannelCountIndex
            )
        )
    }

    private fun initDisplaySettings() {
        displaySettings.clear()
        val themeOptionList = arrayListOf<IntegerSettingOption>()
        themeOptionList.add(
            IntegerSettingOption(
                id = THEME_GROUP_ID + 1,
                label = "System",
                value = System.id
            )
        )
        themeOptionList.add(
            IntegerSettingOption(
                id = THEME_GROUP_ID + 2,
                label = "Light",
                value = Light.id
            )
        )
        themeOptionList.add(
            IntegerSettingOption(
                id = THEME_GROUP_ID + 3,
                label = "Dark",
                value = Dark.id
            )
        )
        val selectedNightMode = generalConfigDataSource.getInt(DARK_THEME.id, System.id)
        val selectedNightModeIndex = themeOptionList.indexOfFirst {
            it.value == selectedNightMode
        }
        displaySettings.add(
            SelectableSetting(
                category = DARK_THEME,
                name = "Dark theme",
                options = themeOptionList,
                label = themeOptionList[selectedNightModeIndex].label,
                selectedIndex = selectedNightModeIndex
            )
        )
    }

    private fun initStorageSettings() {
        storageLocationSetting = SwitchSetting(
            category = USE_SHARED_STORAGE,
            name = "Use a public folder",
            details = "Recorded files will be stored in the $exportFolder folder, which is visible to other apps.",
            value = generalConfigDataSource.getBoolean(USE_SHARED_STORAGE.id, false)
        )
    }

    companion object {
        private const val SAMPLING_GROUP_ID = 10
        private const val AUDIO_CHANNELS_GROUP_ID = 20
        private const val BITRATE_GROUP_ID = 30
        private const val THEME_GROUP_ID = 40
    }
}