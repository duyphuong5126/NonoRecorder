package com.nonoka.nonorecorder.feature.main.recorded.uimodel

data class StartPlayingList(
    val filePathList: List<String>,
    val startPosition: Int
)
