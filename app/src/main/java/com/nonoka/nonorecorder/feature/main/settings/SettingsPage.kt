@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)

package com.nonoka.nonorecorder.feature.main.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nonoka.nonorecorder.R
import com.nonoka.nonorecorder.constant.Colors
import com.nonoka.nonorecorder.constant.Dimens
import com.nonoka.nonorecorder.constant.titleAppBar
import com.nonoka.nonorecorder.feature.main.settings.uimodel.SelectableSettingOption
import com.nonoka.nonorecorder.feature.main.settings.uimodel.SettingUiModel
import com.nonoka.nonorecorder.feature.main.settings.uimodel.SettingUiModel.SelectableSetting
import com.nonoka.nonorecorder.feature.main.settings.uimodel.SettingUiModel.SwitchSetting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(settingsViewModel: SettingsViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings_page_title),
                        style = MaterialTheme.typography.titleAppBar,
                    )
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .padding(horizontal = Dimens.normalSpace),
            verticalArrangement = Arrangement.Top,
        ) {
            item {
                Text(
                    text = stringResource(id = R.string.video_recording_settings_area),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            item {
                Box(modifier = Modifier.height(Dimens.mediumSpace))
            }

            itemsIndexed(
                items = settingsViewModel.recordingSettings,
                key = { _, setting: SettingUiModel ->
                    setting.category.name
                }) { index, setting ->
                when (index) {
                    0 -> {
                        Column(
                            modifier = Modifier
                                .clip(
                                    shape = RoundedCornerShape(
                                        topStart = Dimens.normalSpace,
                                        topEnd = Dimens.normalSpace,
                                    )
                                )
                                .background(color = MaterialTheme.colorScheme.surface)
                        ) {
                            SettingItem(
                                settingsViewModel = settingsViewModel,
                                setting = setting,
                            )
                        }
                    }
                    settingsViewModel.recordingSettings.size - 1 -> {
                        Column(
                            modifier = Modifier
                                .clip(
                                    shape = RoundedCornerShape(
                                        bottomStart = Dimens.normalSpace,
                                        bottomEnd = Dimens.normalSpace,
                                    )
                                )
                                .background(color = MaterialTheme.colorScheme.surface)
                        ) {
                            SettingItemDivider()

                            SettingItem(
                                settingsViewModel = settingsViewModel,
                                setting = setting,
                            )
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)
                        ) {
                            SettingItemDivider()

                            SettingItem(
                                settingsViewModel = settingsViewModel,
                                setting = setting,
                            )
                        }
                    }
                }
            }

            item {
                Box(modifier = Modifier.height(Dimens.largeSpace))
            }

            item {
                Text(
                    text = stringResource(id = R.string.display_settings_area),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            item {
                Box(modifier = Modifier.height(Dimens.mediumSpace))
            }

            item {
                Column(
                    modifier = Modifier
                        .clip(
                            shape = RoundedCornerShape(Dimens.normalSpace)
                        )
                        .background(color = MaterialTheme.colorScheme.surface)
                ) {
                    val setting = settingsViewModel.displaySettings.first()
                    SettingItem(
                        settingsViewModel = settingsViewModel,
                        setting = setting,
                    )
                }
            }

            val storageLocationSetting = settingsViewModel.storageLocationSetting
            if (storageLocationSetting != null) {
                item {
                    Box(modifier = Modifier.height(Dimens.largeSpace))
                }

                item {
                    Text(
                        text = stringResource(id = R.string.storage_settings_area),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                item {
                    Box(modifier = Modifier.height(Dimens.mediumSpace))
                }

                item {
                    Column(
                        modifier = Modifier
                            .clip(
                                shape = RoundedCornerShape(Dimens.normalSpace)
                            )
                            .background(color = MaterialTheme.colorScheme.surface)
                    ) {
                        SettingItem(
                            settingsViewModel = settingsViewModel,
                            setting = storageLocationSetting,
                        )
                    }
                }
            }

            item {
                Box(modifier = Modifier.height(Dimens.ultraLargeSpace))
            }
        }
    }
}

@Composable
private fun SettingItem(
    settingsViewModel: SettingsViewModel,
    setting: SettingUiModel,
) {
    when (setting) {
        is SelectableSetting -> SelectableSettingItem(
            settingsViewModel = settingsViewModel,
            setting = setting,
        )
        is SwitchSetting -> SwitchSettingItem(
            settingsViewModel = settingsViewModel,
            setting = setting,
        )
    }
}

@Composable
private fun SelectableSettingItem(
    settingsViewModel: SettingsViewModel,
    setting: SelectableSetting,
) {
    val dialogModel = remember { mutableStateOf<SelectableSetting?>(null) }
    val settingModel = dialogModel.value
    if (settingModel != null) {
        MultipleChoicesSettingDialog(
            selectableSetting = settingModel,
            onDismiss = {
                dialogModel.value = null
            },
            onSelectOption = {
                settingsViewModel.onSelectRecordingSettingOption(
                    settingOption = it,
                    category = setting.category,
                )
                dialogModel.value = null
            }
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    dialogModel.value = setting
                },
            )
            .padding(Dimens.normalSpace),
    ) {
        Column {
            Text(
                text = setting.name,
                style = MaterialTheme.typography.bodyLarge,
            )

            Box(modifier = Modifier.height(Dimens.smallSpace))

            Text(
                text = setting.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun SwitchSettingItem(
    settingsViewModel: SettingsViewModel,
    setting: SwitchSetting,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.normalSpace),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = setting.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(end = Dimens.mediumSpace)
                    .weight(1f),
            )

            Switch(
                checked = setting.value,
                onCheckedChange = {
                    settingsViewModel.onSwitchStateChange(setting, it)
                },
                colors = SwitchDefaults.colors(
                    uncheckedThumbColor = Colors.white,
                    uncheckedBorderColor = Colors.switchUnselectedColor,
                    uncheckedTrackColor = Colors.switchUnselectedColor
                ),
                modifier = Modifier.padding(end = Dimens.mediumSpace)
            )
        }
    }
}

@Composable
private fun SettingItemDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = Dimens.normalSpace)
            .height(Dimens.tinySpace)
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.outline)
    )
}

@Composable
private fun MultipleChoicesSettingDialog(
    selectableSetting: SelectableSetting,
    onSelectOption: (settingOption: SelectableSettingOption) -> Unit,
    onDismiss: () -> Unit,
    properties: DialogProperties = DialogProperties(),
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = properties.let {
            DialogProperties(
                dismissOnBackPress = it.dismissOnBackPress,
                dismissOnClickOutside = it.dismissOnClickOutside,
                securePolicy = it.securePolicy,
                usePlatformDefaultWidth = false
            )
        },
    ) {
        Surface(
            modifier = Modifier
                .padding(Dimens.normalSpace)
                .fillMaxWidth(),
            shape = RoundedCornerShape(Dimens.largeSpace),
        ) {
            LazyColumn {
                item {
                    Text(
                        text = selectableSetting.name,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .padding(
                                vertical = Dimens.normalSpace,
                                horizontal = Dimens.largeSpace,
                            )
                            .fillMaxWidth()
                    )
                }

                itemsIndexed(items = selectableSetting.options, key = { _, settingOption ->
                    settingOption.id
                }) { index, settingOption ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectOption(settingOption)
                            }
                            .padding(
                                horizontal = Dimens.largeSpace, vertical = Dimens.normalSpace
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selectableSetting.selectedIndex == index, onClick = null
                        )

                        Box(modifier = Modifier.width(Dimens.mediumSpace))

                        Text(
                            text = settingOption.label,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                item {
                    TextButton(
                        modifier = Modifier
                            .padding(
                                horizontal = Dimens.normalSpace,
                                vertical = Dimens.mediumSpace,
                            )
                            .fillMaxWidth(),
                        onClick = onDismiss,
                    ) {
                        Text(
                            text = stringResource(id = R.string.action_cancel),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}
