package com.nonoka.nonorecorder.shared

import android.content.ContentValues
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Build.VERSION_CODES.S
import android.os.Environment.DIRECTORY_MUSIC
import android.os.Environment.DIRECTORY_RECORDINGS
import android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
import android.provider.MediaStore.MediaColumns.DISPLAY_NAME
import android.provider.MediaStore.MediaColumns.MIME_TYPE
import android.provider.MediaStore.MediaColumns.RELATIVE_PATH
import com.nonoka.nonorecorder.constant.FileConstants.externalRecordedFolder
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import timber.log.Timber

@Throws(Throwable::class)
fun Context.createSharedAudioFile(mp3File: File) {
    val contentValues = ContentValues()
    contentValues.put(DISPLAY_NAME, mp3File.name)
    contentValues.put(MIME_TYPE, "audio/mpeg")
    if (SDK_INT >= S) {
        contentValues.put(RELATIVE_PATH, "$DIRECTORY_RECORDINGS/$externalRecordedFolder")
    } else if (SDK_INT >= Q) {
        contentValues.put(RELATIVE_PATH, "$DIRECTORY_MUSIC/$externalRecordedFolder")
    }

    try {
        contentResolver.insert(EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
            contentResolver.openOutputStream(uri)?.let { outputStream ->
                writeFileToOutputStream(mp3File, outputStream)
                Timber.d("Recording>>> Saved file ${mp3File.name} into $uri")
            }
        }
    } catch (error: Throwable) {
        Timber.d("Recording>>> Could not insert file ${mp3File.name} to media store with error $error")
        throw error
    }
}

private fun writeFileToOutputStream(file: File, outputStream: OutputStream) {
    var length: Int
    val buffer = ByteArray(8192)
    val inputStream = FileInputStream(file)
    while (inputStream.read(buffer).also { length = it } >= 0) {
        outputStream.write(buffer, 0, length)
    }
    outputStream.flush()
    outputStream.close()
    inputStream.close()
}