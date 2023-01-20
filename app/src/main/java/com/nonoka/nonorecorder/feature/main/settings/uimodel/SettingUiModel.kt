package com.nonoka.nonorecorder.feature.main.settings.uimodel

import com.nonoka.nonorecorder.domain.entity.SettingCategory

sealed class SettingUiModel(
    open val category: SettingCategory,
    open val name: String,
) {
    data class SelectableSetting(
        override val category: SettingCategory,
        override val name: String,
        val label: String,
        val options: List<SelectableSettingOption>,
        val selectedIndex: Int,
    ) : SettingUiModel(category = category, name = name)

    data class SwitchSetting(
        override val category: SettingCategory,
        override val name: String,
        val value: Boolean,
    ) : SettingUiModel(category = category, name = name)
}

sealed class SelectableSettingOption(open val id: Int, open val label: String) {
    data class IntegerSettingOption(
        override val id: Int,
        override val label: String,
        val value: Int
    ) : SelectableSettingOption(id, label)
}
