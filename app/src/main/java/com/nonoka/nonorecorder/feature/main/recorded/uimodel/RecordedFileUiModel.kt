package com.nonoka.nonorecorder.feature.main.recorded.uimodel

sealed class RecordedItem {
    abstract val id: String

    data class RecordedDate(val date: String) : RecordedItem() {
        override val id: String
            get() = toString()
    }

    data class RecordedFileUiModel(
        val name: String,
        val nameWithoutExtension: String,
        val duration: String,
        val lastModified: String,
        val filePath: String
    ) : RecordedItem() {
        override val id: String
            get() = toString()
    }

    data class BrokenRecordedFileUiModel(
        val name: String,
        val filePath: String,
        val lastModified: String
    ) : RecordedItem() {
        override val id: String
            get() = toString()
    }

    object FirstBannerAdUiModel : RecordedItem() {
        override val id: String
            get() = toString()
    }
}
