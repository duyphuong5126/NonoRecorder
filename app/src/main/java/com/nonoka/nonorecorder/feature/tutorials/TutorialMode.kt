package com.nonoka.nonorecorder.feature.tutorials

enum class TutorialMode(val modeId: Int) {
    AppearsOnTop(1), Recording(2), Accessibility(3), CallAppPermissions(4);

    companion object {
        fun fromModeId(modeId: Int): TutorialMode {
            return values().firstOrNull {
                it.modeId == modeId
            } ?: throw IllegalStateException("Unsupported mode ID $modeId")
        }
    }
}