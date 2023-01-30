package com.nonoka.nonorecorder.recorder

import android.Manifest.permission.RECORD_AUDIO
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder.AudioSource.VOICE_RECOGNITION
import androidx.core.content.ContextCompat.checkSelfPermission
import com.nonoka.nonorecorder.constant.FileConstants.pcmFileExt
import com.nonoka.nonorecorder.constant.FileConstants.recordedFolder
import com.nonoka.nonorecorder.constant.FileConstants.wavFileExt
import com.nonoka.nonorecorder.constant.IntentConstants
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


class AudioCallRecorder : CallRecorder {
    private lateinit var recordedAudioFile: File
    private var audioRecorder: AudioRecord? = null

    private var sampleRateInHz: Int = 44100
    private var channelConfig: Int = AudioFormat.CHANNEL_IN_STEREO
    private var audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
    private var bufferSizeInBytes: Int =
        AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)

    private val isRecordingAudio = AtomicBoolean(false)

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)

    override val isRecording: Boolean
        get() = isRecordingAudio.get()

    override fun startCallRecording(context: Context, audioSource: Int) {
        Timber.d("Recording>>> starting audio recorder")
        if (checkSelfPermission(
                context,
                RECORD_AUDIO
            ) != PERMISSION_GRANTED || isRecordingAudio.get()
        ) {
            return
        }
        try {
            audioRecorder = AudioRecord(
                VOICE_RECOGNITION,
                sampleRateInHz,
                channelConfig,
                audioFormat,
                bufferSizeInBytes
            )

            initAudioOutputFile(context)
            if (audioRecorder?.state != AudioRecord.STATE_INITIALIZED) {
                Timber.e("Recording>>> failed to init AudioRecord")
                return
            }
            audioRecorder?.startRecording()
            isRecordingAudio.compareAndSet(false, true)
            ioScope.launch {
                updateAudioOutputFile()
            }
        } catch (error: Throwable) {
            Timber.d("Recording>>> error in starting video recorder: $error")
        }
        Timber.d("Recording>>> audio recorder started")
    }

    override fun stopCallRecording(context: Context) {
        Timber.d("Recording>>> stopping audio recorder")
        try {
            if (!isRecordingAudio.get()) {
                return
            }
            audioRecorder?.stop()
            audioRecorder?.release()
            audioRecorder = null
            isRecordingAudio.compareAndSet(true, false)

            convertPcmToWav(context)
        } catch (error: Throwable) {
            Timber.d("Recording>>> error in stopping audio recorder: $error")
        }
        Timber.d("Recording>>> audio recorder stopped")

        ioScope.launch {
            delay(5000)
            context.sendBroadcast(Intent(IntentConstants.actionFinishedRecording).apply {
                putExtra(IntentConstants.extraDirectory, recordedAudioFile.parentFile?.absolutePath)
            })
        }
    }

    override fun destroy() {
        ioScope.cancel()
    }

    private fun initAudioOutputFile(context: Context) {
        val recordDir = File(context.filesDir.absolutePath, recordedFolder)
        if (!recordDir.exists()) {
            recordDir.mkdir()
        }
        recordedAudioFile = File(recordDir, "${dateFormat.format(Date())}$pcmFileExt")
        recordedAudioFile.createNewFile()
    }

    private fun updateAudioOutputFile() {
        val data = ByteArray(bufferSizeInBytes / 2)
        val outputStream: FileOutputStream?
        try {
            outputStream = FileOutputStream(recordedAudioFile)
        } catch (e: FileNotFoundException) {
            return
        }
        while (isRecordingAudio.get()) {
            val read = audioRecorder!!.read(data, 0, data.size)
            if (read != AudioRecord.ERROR_INVALID_OPERATION) {
                try {
                    outputStream.write(data, 0, read)
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
        try {
            outputStream.flush()
            outputStream.close()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun convertPcmToWav(context: Context) {
        try {
            val recordDir = File(context.filesDir.absolutePath, recordedFolder)
            if (!recordDir.exists()) {
                recordDir.mkdir()
            }
            val wawOutputFile =
                File(recordDir, recordedAudioFile.name.replace(pcmFileExt, wavFileExt))
            AudioConverter.pcm2Wav(
                input = recordedAudioFile,
                output = wawOutputFile,
                channelCount = 2,
                sampleRate = sampleRateInHz,
                bitsPerSample = 16
            )
            recordedAudioFile.delete()
            Timber.d("Converted file ${wawOutputFile.name}")
        } catch (error: Throwable) {
            Timber.d("Failed to convert file ${recordedAudioFile.name} with error $error")
        }
    }
}