package com.nonoka.nonorecorder.feature.main.settings

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonoka.nonorecorder.constant.AppConstants.DEFAULT_CHANNELS
import com.nonoka.nonorecorder.constant.AppConstants.DEFAULT_ENCODING_BITRATE
import com.nonoka.nonorecorder.constant.AppConstants.DEFAULT_SAMPLING_RATE
import com.nonoka.nonorecorder.di.qualifier.RecordingSetting
import com.nonoka.nonorecorder.domain.entity.SettingCategory
import com.nonoka.nonorecorder.domain.entity.SettingCategory.AUDIO_CHANNELS
import com.nonoka.nonorecorder.domain.entity.SettingCategory.DARK_THEME
import com.nonoka.nonorecorder.domain.entity.SettingCategory.SAMPLING_RATE
import com.nonoka.nonorecorder.domain.entity.SettingCategory.ENCODING_BITRATE
import com.nonoka.nonorecorder.feature.main.settings.uimodel.SelectableSettingOption
import com.nonoka.nonorecorder.feature.main.settings.uimodel.SelectableSettingOption.IntegerSettingOption
import com.nonoka.nonorecorder.feature.main.settings.uimodel.SettingUiModel
import com.nonoka.nonorecorder.feature.main.settings.uimodel.SettingUiModel.SelectableSetting
import com.nonoka.nonorecorder.feature.main.settings.uimodel.SettingUiModel.NumericalSetting
import com.nonoka.nonorecorder.infrastructure.ConfigDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @RecordingSetting private val recordingConfigDataSource: ConfigDataSource
) : ViewModel() {
    val recordingSettings = mutableStateListOf<SettingUiModel>()
    val storageSettings = mutableStateListOf<SettingUiModel>()
    val displaySettings = mutableStateListOf<SettingUiModel>()

    fun init() {
        initRecordingSettings()
        initDisplaySettings()
        initGeneralSettings()
    }

    fun onSelectSettingOption(
        settingOption: SelectableSettingOption,
        category: SettingCategory
    ) {
        recordingSettings.indexOfFirst {
            it.category == category
        }.let { settingIndex ->
            if (settingIndex >= 0) {
                val selectableSetting = (recordingSettings[settingIndex] as SelectableSetting)
                selectableSetting.options.indexOfFirst {
                    it.id == settingOption.id
                }.let { optionIndex ->
                    if (optionIndex >= 0) {
                        val newOption = selectableSetting.options[optionIndex]
                        recordingSettings[settingIndex] = selectableSetting.copy(
                            selectedIndex = optionIndex,
                            label = newOption.label,
                        )
                        if (newOption is IntegerSettingOption) {
                            viewModelScope.launch(Dispatchers.IO) {
                                recordingConfigDataSource.saveInt(category.name, newOption.value)
                            }
                        }
                    }
                }
            }
        }
    }

    fun onChangeNumericalSetting(
        category: SettingCategory,
        rawValue: String,
    ) {
        recordingSettings.indexOfFirst {
            it.category == category
        }.let { settingIndex ->
            if (settingIndex >= 0) {
                val numericalSetting = (recordingSettings[settingIndex] as NumericalSetting)
                try {
                    val value = if (rawValue.isBlank()) 0 else rawValue.toInt()
                    recordingSettings[settingIndex] = numericalSetting.copy(
                        inputValue = rawValue,
                        label = generateNumericLabel(
                            value = value,
                            unit = numericalSetting.unit,
                            zeroValue = "Default"
                        )
                    )
                } catch (error: Throwable) {
                    Timber.d("Error in updating value $rawValue to $category: $error")
                }
            }
        }
    }

    private fun initRecordingSettings() {
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
            recordingConfigDataSource.getInt(SAMPLING_RATE.name, DEFAULT_SAMPLING_RATE)
        recordingSettings.add(
            SelectableSetting(
                category = SAMPLING_RATE,
                name = "Sampling rate",
                options = samplingRateList,
                label = samplingRateList[0].label,
                selectedIndex = samplingRateList.indexOfFirst {
                    it.value == storedSamplingRate
                }
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
            recordingConfigDataSource.getInt(ENCODING_BITRATE.name, DEFAULT_ENCODING_BITRATE)
        recordingSettings.add(
            SelectableSetting(
                category = ENCODING_BITRATE,
                name = "Encoding bitrate",
                options = bitrateList,
                label = bitrateList[0].label,
                selectedIndex = bitrateList.indexOfFirst {
                    it.value == storedEncodingBitrate
                }
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
            recordingConfigDataSource.getInt(AUDIO_CHANNELS.name, DEFAULT_CHANNELS)
        recordingSettings.add(
            SelectableSetting(
                category = AUDIO_CHANNELS,
                name = "Audio channels",
                options = audioChannelList,
                label = audioChannelList[0].label,
                selectedIndex = audioChannelList.indexOfFirst {
                    it.value == storedAudioChannelCount
                }
            )
        )
    }

    private fun initDisplaySettings() {
        val themeOptionList = arrayListOf<SelectableSettingOption>()
        themeOptionList.add(
            IntegerSettingOption(
                id = THEME_GROUP_ID + 1,
                label = "System",
                value = 0
            )
        )
        themeOptionList.add(
            IntegerSettingOption(
                id = THEME_GROUP_ID + 2,
                label = "Light",
                value = 1
            )
        )
        themeOptionList.add(
            IntegerSettingOption(
                id = THEME_GROUP_ID + 3,
                label = "Dark",
                value = 3
            )
        )
        displaySettings.add(
            SelectableSetting(
                category = DARK_THEME,
                name = "Dark theme",
                options = themeOptionList,
                label = themeOptionList[0].label,
                selectedIndex = 0
            )
        )
    }

    private fun initGeneralSettings() {

    }

    private fun generateNumericLabel(value: Int, unit: String, zeroValue: String? = null): String {
        return if (value == 0 && zeroValue != null) zeroValue else "$value ($unit)"
    }

    companion object {
        private const val SAMPLING_GROUP_ID = 10
        private const val AUDIO_CHANNELS_GROUP_ID = 20
        private const val BITRATE_GROUP_ID = 30
        private const val THEME_GROUP_ID = 40
    }
}