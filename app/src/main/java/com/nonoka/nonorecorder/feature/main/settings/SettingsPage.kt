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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nonoka.nonorecorder.R
import com.nonoka.nonorecorder.constant.Colors
import com.nonoka.nonorecorder.constant.Dimens
import com.nonoka.nonorecorder.constant.titleAppBar
import com.nonoka.nonorecorder.feature.main.settings.uimodel.SettingUiModel
import com.nonoka.nonorecorder.feature.main.settings.uimodel.SettingUiModel.SelectableSetting
import com.nonoka.nonorecorder.feature.main.settings.uimodel.SettingUiModel.NumericalSetting
import com.nonoka.nonorecorder.feature.main.settings.uimodel.SettingUiModel.SwitchSetting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(settingsViewModel: SettingsViewModel) {
    MaterialTheme(colorScheme = Colors.getColorScheme()) {
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
                                    settingsViewModel = settingsViewModel, setting = setting,
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
                                    settingsViewModel = settingsViewModel, setting = setting,
                                )
                            }
                        }
                        else -> {
                            Column(
                                modifier = Modifier.background(color = MaterialTheme.colorScheme.surface)
                            ) {
                                SettingItemDivider()

                                SettingItem(
                                    settingsViewModel = settingsViewModel, setting = setting,
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
                        SettingItem(
                            settingsViewModel = settingsViewModel,
                            setting = settingsViewModel.displaySettings.first(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingItem(
    settingsViewModel: SettingsViewModel, setting: SettingUiModel
) {
    when (setting) {
        is SelectableSetting -> DefaultSettingItem(
            settingsViewModel = settingsViewModel, setting = setting
        )
        is NumericalSetting -> DefaultSettingItem(
            settingsViewModel = settingsViewModel, setting = setting
        )
        is SwitchSetting -> DefaultSettingItem(
            settingsViewModel = settingsViewModel, setting = setting
        )
    }
}


@Composable
private fun DefaultSettingItem(
    settingsViewModel: SettingsViewModel, setting: SettingUiModel
) {
    val dialogModel = remember { mutableStateOf<SettingUiModel?>(null) }
    if (dialogModel.value != null) {
        val settingModel = dialogModel.value
        if (settingModel is SelectableSetting) {
            MultipleChoicesSettingDialog(
                settingsViewModel = settingsViewModel,
                selectableSetting = settingModel,
                onDismiss = {
                    dialogModel.value = null
                },
            )
        } else if (setting is NumericalSetting) {
            NumericalSettingDialog(
                settingsViewModel = settingsViewModel,
                numericalSetting = setting,
                onDismiss = {
                    dialogModel.value = null
                },
            )
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    if (setting is SelectableSetting || setting is NumericalSetting) {
                        dialogModel.value = setting
                    }
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
    settingsViewModel: SettingsViewModel,
    selectableSetting: SelectableSetting,
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
                                settingsViewModel.onSelectSettingOption(
                                    settingOption = settingOption,
                                    category = selectableSetting.category,
                                )
                                onDismiss()
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

@Composable
private fun NumericalSettingDialog(
    settingsViewModel: SettingsViewModel,
    numericalSetting: NumericalSetting,
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
            Column {
                Text(
                    text = numericalSetting.name,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(
                            vertical = Dimens.normalSpace,
                            horizontal = Dimens.largeSpace,
                        )
                        .fillMaxWidth()
                )

                val editText = remember { mutableStateOf(numericalSetting.inputValue) }
                OutlinedTextField(
                    value = editText.value,
                    onValueChange = {
                        editText.value = it
                    },
                    placeholder = {
                        Text(text = numericalSetting.inputHint)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            settingsViewModel.onChangeNumericalSetting(
                                numericalSetting.category,
                                editText.value,
                            )
                            onDismiss()
                        },
                    ),
                    maxLines = 1,
                    modifier = Modifier
                        .padding(horizontal = Dimens.normalSpace)
                        .fillMaxWidth()
                )

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
