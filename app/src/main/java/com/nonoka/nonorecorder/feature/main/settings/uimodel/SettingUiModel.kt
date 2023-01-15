package com.nonoka.nonorecorder.feature.main.settings.uimodel

import com.nonoka.nonorecorder.domain.entity.SettingCategory

sealed class SettingUiModel(
    open val category: SettingCategory,
    open val name: String,
    open val label: String,
) {
    data class SelectableSetting(
        override val category: SettingCategory,
        override val name: String,
        override val label: String,
        val options: List<SelectableSettingOption>,
        val selectedIndex: Int,
    ) : SettingUiModel(category = category, name = name, label = label)

    data class NumericalSetting(
        override val category: SettingCategory,
        override val name: String,
        override val label: String,
        val inputHint: String,
        val inputValue: String,
        val unit: String,
    ) : SettingUiModel(category = category, name = name, label = label)

    data class SwitchSetting(
        override val category: SettingCategory,
        override val name: String,
        override val label: String,
    ) : SettingUiModel(category = category, name = name, label = label)
}

sealed class SelectableSettingOption(open val id: Int, open val label: String) {
    data class IntegerSettingOption(
        override val id: Int,
        override val label: String,
        val value: Int
    ) : SelectableSettingOption(id, label)
}
