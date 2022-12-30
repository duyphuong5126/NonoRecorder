package com.nonoka.nonorecorder.feature.main.recorded.uimodel

sealed class RecordedItem {

    data class RecordedDate(val date: String) : RecordedItem()

    data class RecordedFileUiModel(
        val id: Int,
        val name: String,
        val duration: String,
        val lastModified: String
    ) : RecordedItem()
}
