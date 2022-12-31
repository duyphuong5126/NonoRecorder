package com.nonoka.nonorecorder.feature.main.recorded

import android.media.MediaMetadataRetriever
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonoka.nonorecorder.feature.main.recorded.uimodel.RecordedItem
import com.nonoka.nonorecorder.feature.main.recorded.uimodel.RecordedItem.RecordedDate
import com.nonoka.nonorecorder.feature.main.recorded.uimodel.RecordedItem.RecordedFileUiModel
import com.nonoka.nonorecorder.feature.main.recorded.uimodel.RecordedItem.BrokenRecordedFileUiModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.lang3.time.DurationFormatUtils
import timber.log.Timber

class RecordedListViewModel : ViewModel() {
    var recordedList by mutableStateOf<List<RecordedItem>>(emptyList())
        private set

    private val dateFormat = SimpleDateFormat("E, MMM dd yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("E, MMM dd yyyy HH:mm:ss", Locale.getDefault())
    private val durationFormat = "HH:mm:ss"

    fun initialize(generalFileDirPath: String) {
        refresh(File(generalFileDirPath, "recorded").absolutePath)
    }

    fun refresh(recordedDirectoryPath: String) {
        Timber.d("Test>>> recordedDirectoryPath=$recordedDirectoryPath")
        try {
            viewModelScope.launch(Dispatchers.IO) {
                val recordedDirectory = File(recordedDirectoryPath)
                val retriever = MediaMetadataRetriever()
                var lastDateModified = ""
                val recordedFiles = arrayListOf<RecordedItem>()
                recordedDirectory.listFiles()?.apply {
                    sortByDescending { file ->
                        file.lastModified()
                    }
                }?.forEachIndexed { index, file ->
                    val dateModified = dateFormat.format(file.lastModified())
                    if (lastDateModified != dateModified) {
                        recordedFiles.add(RecordedDate(dateModified))
                        lastDateModified = dateModified
                    }

                    try {
                        retriever.setDataSource(file.absolutePath)
                        val title =
                            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                                ?: file.name ?: "Unknown"
                        val durationMillis =
                            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                ?.toLong() ?: 0L
                        recordedFiles.add(
                            RecordedFileUiModel(
                                id = index,
                                name = title,
                                duration = DurationFormatUtils.formatDuration(
                                    durationMillis,
                                    durationFormat
                                ),
                                lastModified = dateTimeFormat.format(file.lastModified())
                            )
                        )
                    } catch (error: Throwable) {
                        recordedFiles.add(
                            BrokenRecordedFileUiModel(
                                id = index,
                                name = file.name,
                                lastModified = dateTimeFormat.format(file.lastModified())
                            )
                        )
                    }
                }
                recordedList = recordedFiles
            }
        } catch (error: SecurityException) {
            Timber.e(error)
        }
    }
}