package com.nonoka.nonorecorder.recorder

import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.nonoka.nonorecorder.constant.AppConstants.DEFAULT_CHANNELS
import com.nonoka.nonorecorder.constant.AppConstants.DEFAULT_ENCODING_BITRATE
import com.nonoka.nonorecorder.constant.AppConstants.DEFAULT_SAMPLING_RATE
import com.nonoka.nonorecorder.constant.FileConstants.mp3FileExt
import com.nonoka.nonorecorder.constant.FileConstants.mp4FileExt
import com.nonoka.nonorecorder.constant.FileConstants.recordedFolder
import com.nonoka.nonorecorder.constant.IntentConstants.actionFinishedRecording
import com.nonoka.nonorecorder.constant.IntentConstants.extraDirectory
import com.nonoka.nonorecorder.di.qualifier.GeneralSetting
import com.nonoka.nonorecorder.di.qualifier.RecordingSetting
import com.nonoka.nonorecorder.domain.entity.SettingCategory.AUDIO_CHANNELS
import com.nonoka.nonorecorder.domain.entity.SettingCategory.ENCODING_BITRATE
import com.nonoka.nonorecorder.domain.entity.SettingCategory.SAMPLING_RATE
import com.nonoka.nonorecorder.domain.entity.SettingCategory.USE_SHARED_STORAGE
import com.nonoka.nonorecorder.infrastructure.ConfigDataSource
import com.nonoka.nonorecorder.shared.createSharedAudioFile
import com.nonoka.nonorecorder.shared.exportFolder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber


class VideoCallRecorder(
    @RecordingSetting
    private val configDataSource: ConfigDataSource,
    @GeneralSetting
    private val generalConfigDataSource: ConfigDataSource
) : CallRecorder {
    private var videoRecorder: MediaRecorder? = null
    private lateinit var recordedVideo: File
    private var pendingExtraOutputFile: File? = null

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)

    private val isRecordingAudio = AtomicBoolean(false)

    private val useSharedStorage = AtomicBoolean(false)

    private val filePartIndex = AtomicInteger(0)

    override val isRecording: Boolean
        get() = isRecordingAudio.get()

    override fun startCallRecording(context: Context, audioSource: Int) {
        Timber.d("Recording>>> starting video recorder")
        filePartIndex.getAndSet(0)
        useSharedStorage.compareAndSet(
            false,
            generalConfigDataSource.getBoolean(USE_SHARED_STORAGE.id, false)
        )
        try {
            @Suppress("DEPRECATION")
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }
            initMediaOutputFile(context)
            // This must be needed source
            recorder.setAudioSource(audioSource)

            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setOutputFile(recordedVideo.absolutePath)
            //recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)

            configDataSource.getInt(SAMPLING_RATE.id, DEFAULT_SAMPLING_RATE)
                .let(recorder::setAudioSamplingRate)
            configDataSource.getInt(ENCODING_BITRATE.id, DEFAULT_ENCODING_BITRATE)
                .let(recorder::setAudioEncodingBitRate)
            configDataSource.getInt(AUDIO_CHANNELS.id, DEFAULT_CHANNELS)
                .let(recorder::setAudioChannels)

            recorder.prepare()
            recorder.start()
            isRecordingAudio.compareAndSet(false, true)
            videoRecorder = recorder
            videoRecorder?.setOnInfoListener { _, what, extra ->
                val whatMessage = when (what) {
                    MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_APPROACHING -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            videoRecorder?.setNextOutputFile(getNextOutputFile(context))
                        }
                        "Max file size approaching"
                    }
                    MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED -> "Max duration reached"
                    MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED -> "Max file size reached"
                    MediaRecorder.MEDIA_RECORDER_INFO_NEXT_OUTPUT_FILE_STARTED -> {
                        pendingExtraOutputFile = null
                        "Next output file started"
                    }
                    else -> "Unknown"
                }
                Timber.d("Recording>>> warning: $whatMessage, extra=$extra")
            }
        } catch (error: Throwable) {
            Timber.d("Recording>>> error in starting video recorder: $error")
        }
        Timber.d("Recording>>> video recorder started")
    }

    override fun stopCallRecording(context: Context) {
        Timber.d("Recording>>> stopping video recorder")
        val handler = Handler(Looper.getMainLooper())
        try {
            videoRecorder?.stop()
            videoRecorder?.release()
            videoRecorder = null
            val outputMp3File =
                File(getRecordDir(context), "${dateFormat.format(Date())}$mp3FileExt")
            VideoAudioExtractor.convertMedia(
                srcPath = recordedVideo.absolutePath,
                dstPath = outputMp3File.absolutePath,
                useVideo = false,
                useAudio = true
            )
            if (useSharedStorage.get()) {
                try {
                    context.createSharedAudioFile(outputMp3File)
                    handler.post {
                        Toast.makeText(
                            context,
                            "Saved file ${outputMp3File.name} to $exportFolder folder",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (error: Throwable) {
                    handler.post {
                        Toast.makeText(
                            context,
                            "Could not save mp3 file ${outputMp3File.nameWithoutExtension}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Timber.d("Recording>>> could not save mp3 file ${outputMp3File.nameWithoutExtension} with error $error")
                }
            }
            val deletedMp4File = recordedVideo.delete()
            Timber.d("Recording>>> deleted mp4 file=$deletedMp4File")
            pendingExtraOutputFile?.delete()?.let {
                Timber.d("Recording>>> deleted extra file=$it")
            }
            isRecordingAudio.compareAndSet(true, false)
        } catch (error: Throwable) {
            Timber.d("Recording>>> error in stopping video recorder: $error")
        }
        Timber.d("Recording>>> video recorder stopped")

        ioScope.launch {
            delay(3000)
            Timber.d("Recording>>> send stop recording broadcast")
            context.sendBroadcast(Intent(actionFinishedRecording).apply {
                putExtra(extraDirectory, recordedVideo.parentFile?.absolutePath)
            })
        }
    }

    override fun destroy() {
        ioScope.cancel()
    }

    private fun initMediaOutputFile(context: Context) {
        recordedVideo = File(getRecordDir(context), "${dateFormat.format(Date())}$mp4FileExt")
        recordedVideo.createNewFile()
    }

    private fun getNextOutputFile(context: Context): File {
        val nextOutputFileName =
            "${recordedVideo.nameWithoutExtension}_${filePartIndex.incrementAndGet()}"
        val file = File(getRecordDir(context), "$nextOutputFileName$mp4FileExt")
        file.createNewFile()
        pendingExtraOutputFile = file
        return file
    }

    private fun getRecordDir(context: Context): File {
        val recordDir = File(context.filesDir.absolutePath, recordedFolder)
        if (!recordDir.exists()) {
            recordDir.mkdir()
        }
        return recordDir
    }

    /*
    //=================================================Added code start==========

    //=================================================Added code start==========
    private var mRecorder: MediaRecorder? = null
    private var isStarted = false

    private var player: MediaPlayer? = null

    fun startRecording() {
        try {

            /*   String timestamp = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss", Locale.US).format(new Date());
            String fileName =timestamp+".3gp";
            mediaSaver = new MediaSaver(context).setParentDirectoryName("Accessibility").

                    setFileNameKeepOriginalExtension(fileName).
                    setExternal(MediaSaver.isExternalStorageReadable());*/
            //String selectedPath = Environment.getExternalStorageDirectory() + "/Testing";
            //String selectedPath = Environment.getExternalStorageDirectory().getAbsolutePath() +"/Android/data/" + packageName + "/system_sound";
            @Suppress("DEPRECATION")
            mRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                MediaRecorder()
            }
            //            mRecorder.reset();

            //android.permission.MODIFY_AUDIO_SETTINGS
            val mAudioManager: AudioManager =
                getSystemService(Context.AUDIO_SERVICE) as AudioManager
            //turn on speaker
            mAudioManager.mode =
                AudioManager.MODE_IN_COMMUNICATION //MODE_IN_COMMUNICATION | MODE_IN_CALL
            // mAudioManager.setSpeakerphoneOn(true);
            // mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0); // increase Volume
            hasWiredHeadset(mAudioManager)

            //android.permission.RECORD_AUDIO
            val manufacturer = Build.MANUFACTURER
            Timber.d(manufacturer)
            /* if (manufacturer.toLowerCase().contains("samsung")) {
                mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
            } else {
                mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
            }*/
            /*
            VOICE_CALL is the actual call data being sent in a call, up and down (so your side and their side). VOICE_COMMUNICATION is just the microphone, but with codecs and echo cancellation turned on for good voice quality.
            */
            mRecorder!!.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION) //MIC | VOICE_COMMUNICATION (Android 10 release) | VOICE_RECOGNITION | (VOICE_CALL = VOICE_UPLINK + VOICE_DOWNLINK)
            mRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP) //THREE_GPP | MPEG_4
            mRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB) //AMR_NB | AAC
            mRecorder!!.setOutputFile(recordedMedia.absolutePath)
            mRecorder!!.prepare()
            mRecorder!!.start()
            isStarted = true
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        if (isStarted && mRecorder != null) {
            mRecorder!!.stop()
            mRecorder!!.reset() // You can reuse the object by going back to setAudioSource() step
            mRecorder!!.release()
            mRecorder = null
            isStarted = false
        }
    }

    private fun startPlaying() {
        player = MediaPlayer()
        try {
            player?.setDataSource(recordedMedia.absolutePath)
            player?.prepare()
            player?.start()
        } catch (e: IOException) {
            Timber.d("prepare() failed")
        }
    }

    private fun stopPlaying() {
        player?.release()
        player = null
    }

    // To detect the connected other device like headphone, wifi headphone, usb headphone etc
    private fun hasWiredHeadset(mAudioManager: AudioManager): Boolean {
        val devices = arrayListOf<AudioDeviceInfo>().apply {
            addAll(mAudioManager.getDevices(AudioManager.GET_DEVICES_INPUTS))
            addAll(mAudioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS))
        }
        for (device: AudioDeviceInfo in devices) {
            when (device.type) {
                AudioDeviceInfo.TYPE_WIRED_HEADSET -> {
                    Timber.d("hasWiredHeadset: found wired headset")
                    return true
                }
                AudioDeviceInfo.TYPE_USB_DEVICE -> {
                    Timber.d("hasWiredHeadset: found USB audio device")
                    return true
                }
                AudioDeviceInfo.TYPE_TELEPHONY -> {
                    Timber.d("hasWiredHeadset: found audio signals over the telephony network")
                    return true
                }
            }
        }
        return false
    }

    /**
     * Area of MediaRecorder - Ending
     */
     */
}