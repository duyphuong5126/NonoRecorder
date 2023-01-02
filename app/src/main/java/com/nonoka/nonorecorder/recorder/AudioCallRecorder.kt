package com.nonoka.nonorecorder.recorder

import android.Manifest.permission.RECORD_AUDIO
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder.AudioSource.VOICE_RECOGNITION
import androidx.core.content.ContextCompat.checkSelfPermission
import com.nonoka.nonorecorder.constant.IntentConstants
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
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

    override fun startCallRecording(context: Context) {
        if (checkSelfPermission(
                context,
                RECORD_AUDIO
            ) != PERMISSION_GRANTED || isRecordingAudio.get()
        ) {
            return
        }

        audioRecorder = AudioRecord(
            VOICE_RECOGNITION,
            sampleRateInHz,
            channelConfig,
            audioFormat,
            bufferSizeInBytes
        )

        initAudioOutputFile(context)
        if (audioRecorder?.state != AudioRecord.STATE_INITIALIZED) {
            Timber.e("error initializing AudioRecord")
            return
        }
        try {
            audioRecorder?.startRecording()
            isRecordingAudio.compareAndSet(false, true)
            ioScope.launch {
                updateAudioOutputFile()
            }
        } catch (e: Throwable) {
            Timber.e("prepare() failed with error $e")
        }
        Timber.d("recording started")
    }

    override fun stopCallRecording(context: Context) {
        if (!isRecordingAudio.get()) {
            return
        }
        audioRecorder?.stop()
        audioRecorder?.release()
        audioRecorder = null
        isRecordingAudio.compareAndSet(true, false)

        convertPcmToWav(context)

        Timber.d("recording stopped")

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
        val recordDir = File(context.filesDir.absolutePath, "recorded")
        if (!recordDir.exists()) {
            recordDir.mkdir()
        }
        recordedAudioFile = File(recordDir, "${dateFormat.format(Date())}.pcm")
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
            try {
                outputStream.write(data, 0, read)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        try {
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun convertPcmToWav(context: Context) {
        try {
            val recordDir = File(context.filesDir.absolutePath, "recorded")
            if (!recordDir.exists()) {
                recordDir.mkdir()
            }
            val wawOutputFile =
                File(recordDir, "${recordedAudioFile.name.replace(".pcm", "")}.wav")
            AudioConverter.pcm2Wav(
                input = recordedAudioFile,
                output = wawOutputFile,
                channelCount = 2,
                sampleRate = sampleRateInHz,
                bitsPerSample = 16
            )
            recordedAudioFile.delete()
            Timber.d("Converted file ${wawOutputFile.name}")
        } catch (error: SecurityException) {
            Timber.d("Failed to convert file ${recordedAudioFile.name} with error $error")
        }
    }
}