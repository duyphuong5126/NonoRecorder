package com.nonoka.nonorecorder.domain.entity

enum class SettingCategory(val id: String) {
    AUDIO_CHANNELS("audio_channels"),
    SAMPLING_RATE("sampling_rate"),
    ENCODING_BITRATE("encoding_bitrate"),

    DARK_THEME("dark_theme"),

    USE_SHARED_STORAGE("use_shared_storage")
    ;
}