package com.nonoka.nonorecorder.shared

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.S
import android.os.Environment.DIRECTORY_MUSIC
import android.os.Environment.DIRECTORY_RECORDINGS

val exportFolder: String = if (SDK_INT >= S) DIRECTORY_RECORDINGS else DIRECTORY_MUSIC