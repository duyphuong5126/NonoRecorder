package com.nonoka.nonorecorder.feature.main.recorded

import android.media.MediaMetadataRetriever
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nonoka.nonorecorder.constant.FileConstants.recordedFolder
import com.nonoka.nonorecorder.feature.main.recorded.uimodel.RecordedItem
import com.nonoka.nonorecorder.feature.main.recorded.uimodel.RecordedItem.RecordedDate
import com.nonoka.nonorecorder.feature.main.recorded.uimodel.RecordedItem.RecordedFileUiModel
import com.nonoka.nonorecorder.feature.main.recorded.uimodel.RecordedItem.BrokenRecordedFileUiModel
import com.nonoka.nonorecorder.feature.main.recorded.uimodel.RecordedItem.FirstBannerAdUiModel
import com.nonoka.nonorecorder.feature.main.recorded.uimodel.StartPlayingList
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.apache.commons.lang3.time.DurationFormatUtils
import timber.log.Timber

class RecordedListViewModel : ViewModel() {
    val recordedList = mutableStateListOf<RecordedItem>()
    var isProcessingAudio by mutableStateOf(false)
    var exportingFile by mutableStateOf<String?>(null)

    private val _startPlayingList: MutableSharedFlow<StartPlayingList> = MutableSharedFlow()
    val startPlayingList: SharedFlow<StartPlayingList> = _startPlayingList

    private val _toastMessage: MutableSharedFlow<String> = MutableSharedFlow()
    val toastMessage: SharedFlow<String> = _toastMessage
    private val _exportingFileChallenge: MutableSharedFlow<String?> = MutableSharedFlow()
    val exportingFileChallenge: SharedFlow<String?> = _exportingFileChallenge

    private val exportingFileChallengeRequested = AtomicBoolean(false)
    private val isExportingChallengeAvailable = AtomicBoolean(false)

    private val dateFormat = SimpleDateFormat("E, MMM dd yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("E, MMM dd yyyy HH:mm:ss", Locale.getDefault())
    private val durationFormat = "HH:mm:ss"

    fun initialize(generalFileDirPath: String) {
        refresh(File(generalFileDirPath, recordedFolder).absolutePath)
    }

    fun refresh(recordedDirectoryPath: String) {
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
                                lastModified = dateTimeFormat.format(file.lastModified()),
                                filePath = file.absolutePath,
                                nameWithoutExtension = file.nameWithoutExtension,
                            )
                        )
                    } catch (error: Throwable) {
                        recordedFiles.add(
                            BrokenRecordedFileUiModel(
                                id = index,
                                name = file.name,
                                lastModified = dateTimeFormat.format(file.lastModified()),
                                filePath = file.absolutePath
                            )
                        )
                    }
                    if (index == FIRST_BANNER_AD_INDEX) {
                        recordedFiles.add(FirstBannerAdUiModel)
                    }
                }
                recordedList.clear()
                recordedList.addAll(recordedFiles)
            }
        } catch (error: SecurityException) {
            Timber.e(error)
        }
    }

    fun generatePlayingList(startFromItem: RecordedFileUiModel) {
        viewModelScope.launch(Dispatchers.Default) {
            recordedList
                .filterIsInstance(RecordedFileUiModel::class.java)
                .map(RecordedFileUiModel::filePath)
                .let {
                    val startPosition = it.indexOfFirst { filePath ->
                        filePath == startFromItem.filePath
                    }
                    _startPlayingList.emit(
                        StartPlayingList(
                            it,
                            if (startPosition >= 0) startPosition else 0
                        )
                    )
                }
        }
    }

    fun deleteFile(filePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val deleted: Boolean = recordedList.firstOrNull {
                (it is RecordedFileUiModel && it.filePath == filePath) || (it is BrokenRecordedFileUiModel && it.filePath == filePath)
            }?.let {
                return@let if (it is RecordedFileUiModel) it.filePath else if (it is BrokenRecordedFileUiModel) it.filePath else null
            }?.let {
                val targetFile = File(it)
                try {
                    targetFile.delete()
                } catch (error: Throwable) {
                    Timber.d("Can not delete file ${targetFile.absolutePath} with error $error")
                    false
                }
            } ?: false
            if (deleted) {
                var deletedItemDateTime = ""
                recordedList.removeAll {
                    if (it is RecordedFileUiModel && it.filePath == filePath) {
                        deletedItemDateTime = it.lastModified
                        true
                    } else if (it is BrokenRecordedFileUiModel && it.filePath == filePath) {
                        deletedItemDateTime = it.lastModified
                        true
                    } else false
                }
                val remainedDateTimeList = arrayListOf<String>()
                recordedList.forEach {
                    if (it is RecordedFileUiModel) {
                        remainedDateTimeList.add(it.lastModified)
                    } else if (it is BrokenRecordedFileUiModel) {
                        remainedDateTimeList.add(it.lastModified)
                    }
                }
                if (deletedItemDateTime.isNotBlank()) {
                    recordedList.removeAll {
                        it is RecordedDate && deletedItemDateTime.contains(it.date) && remainedDateTimeList.none { dateTime ->
                            dateTime.contains(it.date)
                        }
                    }
                }
            } else {
                _toastMessage.emit("Cannot delete file $filePath")
            }
        }
    }

    fun renameFile(filePath: String, newFileName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val renamedFile: File? = recordedList.firstOrNull {
                it is RecordedFileUiModel && it.filePath == filePath
            }?.let {
                File((it as RecordedFileUiModel).filePath)
            }?.let {
                try {
                    val newFile = File(it.parentFile, "$newFileName.${it.extension}")
                    it.renameTo(newFile)
                    newFile
                } catch (error: Throwable) {
                    Timber.d("Cannot rename to $newFileName with error $error")
                    _toastMessage.emit("Cannot rename file to $newFileName")
                    null
                }
            }
            Timber.d("renamedFile=$renamedFile")
            if (renamedFile != null) {
                for (index in recordedList.indices) {
                    val recordedItem = recordedList[index]
                    if (recordedItem is RecordedFileUiModel && recordedItem.filePath == filePath) {
                        recordedList[index] = recordedItem.copy(
                            name = renamedFile.name,
                            nameWithoutExtension = renamedFile.nameWithoutExtension
                        )
                    }
                }
            } else {
                _toastMessage.emit("Cannot rename file to $newFileName")
            }
        }
    }

    fun requestExportingFile(filePath: String?) {
        if (isExportingChallengeAvailable.get() && !exportingFileChallengeRequested.get()) {
            viewModelScope.launch {
                _exportingFileChallenge.emit(filePath)
            }
        } else {
            exportingFile = filePath
        }
    }

    fun onFinishExportingFileChallenge() {
        viewModelScope.launch {
            _exportingFileChallenge.emit(null)
        }
        exportingFileChallengeRequested.compareAndSet(false, true)
    }

    fun setChallengeAvailability(challengeAvailable: Boolean) {
        isExportingChallengeAvailable.getAndSet(challengeAvailable)
    }

    companion object {
        private const val FIRST_BANNER_AD_INDEX = 9
    }
}