package com.nonoka.nonorecorder

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED
import android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.FrameLayout
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber


class CallRecordingService : AccessibilityService() {
    private var windowManager: WindowManager? = null
    // ImageView back,home,notification,minimize;
    //WindowManager.LayoutParams params;
//    AccessibilityService service;

    // ImageView back,home,notification,minimize;
    //WindowManager.LayoutParams params;
    //    AccessibilityService service;

    private var player: MediaPlayer? = null
    private var recorder: MediaRecorder? = null

    private lateinit var file: File

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private var audioMode = AudioManager.MODE_INVALID

    private val pendingIntent: PendingIntent
        get() =
            Intent(this, RedirectActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    FLAG_IMMUTABLE
                )
            }

    private val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)

    @SuppressLint("RtlHardcoded")
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        Timber.i("start RecordingService")

        // Start listening to Audio mode change
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManager.addOnModeChangedListener({
                onAudioModeChange(audioManager.mode)
            }, this::onAudioModeChange)
        } else {
            ioScope.launch {
                doWhile(action = {
                    onAudioModeChange(audioManager.mode)
                }, checker = {
                    true
                }, interval = AUDIO_MODE_CHECK_INTERVAL)
            }
        }

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val overlayLayout = layoutInflater.inflate(R.layout.layout_recording_overlay, null)

        val params = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
            )
        } else {
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
            )
        }

        params.gravity = Gravity.RIGHT or Gravity.TOP
        windowManager.addView(overlayLayout, params)

        /*  back = new ImageView(this);
        home = new ImageView(this);
        minimize = new ImageView(this);
        notification = new ImageView(this);

        back.setImageResource(R.drawable.ic_launcher_background);
        home.setImageResource(R.drawable.ic_launcher_background);
        minimize.setImageResource(R.drawable.ic_launcher_background);
        notification.setImageResource(R.drawable.ic_launcher_background);
*/
        /*      params= new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER_VERTICAL|Gravity.RIGHT;
        params.x = 10;
        params.y = 50;

        windowManager.addView(home, params);

        params= new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER_VERTICAL|Gravity.RIGHT;
        params.x = 10;
        params.y = 100;

        windowManager.addView(minimize, params);

        params= new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER_VERTICAL|Gravity.RIGHT;
        params.x = 10;
        params.y = 150;

        windowManager.addView(notification, params);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        minimize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
*/
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification: Notification =
            NotificationCompat.Builder(this, createNotificationChannel())
                .setContentTitle(getText(R.string.waiting_for_call))
                .setSmallIcon(R.drawable.ic_standby_24dp)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.waiting_for_call))
                .setOnlyAlertOnce(true)
                .build()

        startForeground(RECORDING_NOTIFICATION_ID, notification)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        //Timber.d("Event: ${event.eventType}")
    }

    override fun onInterrupt() {
        Timber.d("Recording service is being interrupted")
    }

    override fun onUnbind(intent: Intent): Boolean {
        Timber.d("Recording service is being unbound")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        ioScope.cancel()
    }

    /*  @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d("TAG", "onServiceConnected");
    }*/


/*    @Override
    public void onServiceConnected() {
        // Set the type of events that this service wants to listen to. Others
        // won't be passed to this service.

        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
                AccessibilityEvent.TYPE_VIEW_FOCUSED;

        // If you only want this service to work with specific applications, set their
        // package names here. Otherwise, when the service is activated, it will listen
        // to events from all applications.
        info.packageNames = new String[]
                {"nisarg.app.demo"};

        // Set the type of feedback your service will provide.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;

        // Default services are invoked only if no package-specific ones are present
        // for the type of AccessibilityEvent generated. This service *is*
        // application-specific, so the flag isn't necessary. If this was a
        // general-purpose service, it would be worth considering setting the
        // DEFAULT flag.

        // info.flags = AccessibilityServiceInfo.DEFAULT;

        info.notificationTimeout = 100;

        this.setServiceInfo(info);


    }*/


    /*  @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d("TAG", "onServiceConnected");
    }*/
    /*    @Override
    public void onServiceConnected() {
        // Set the type of events that this service wants to listen to. Others
        // won't be passed to this service.

        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
                AccessibilityEvent.TYPE_VIEW_FOCUSED;

        // If you only want this service to work with specific applications, set their
        // package names here. Otherwise, when the service is activated, it will listen
        // to events from all applications.
        info.packageNames = new String[]
                {"nisarg.app.demo"};

        // Set the type of feedback your service will provide.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;

        // Default services are invoked only if no package-specific ones are present
        // for the type of AccessibilityEvent generated. This service *is*
        // application-specific, so the flag isn't necessary. If this was a
        // general-purpose service, it would be worth considering setting the
        // DEFAULT flag.

        // info.flags = AccessibilityServiceInfo.DEFAULT;

        info.notificationTimeout = 100;

        this.setServiceInfo(info);


    }*/
    @SuppressLint("ClickableViewAccessibility")
    override fun onServiceConnected() {
        Timber.d("onServiceConnected")

        //==============================Record Audio while Call received===============//
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val layout = FrameLayout(this)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP
        windowManager.addView(layout, params)
        layout.setOnTouchListener { _, _ -> //You can either get the information here or on onAccessibilityEvent
            //Timber.d("Window view touched........:")
            true
        }

        //==============To Record Audio wile Call received=================
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        info.notificationTimeout = 100
        info.packageNames = null
        serviceInfo = info
        try {
            //startRecording();
            startCallRecording()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startPlaying() {
        player = MediaPlayer()
        try {
            player?.setDataSource(file.absolutePath)
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

    private fun startCallRecording() {
        @Suppress("DEPRECATION")
        initOutputFile()
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }
        // This must be needed source
        recorder?.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder?.setOutputFile(file.absolutePath)
        //recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)
        try {
            recorder?.prepare()
            recorder?.start()
            recorder?.setOnInfoListener { _, what, extra ->
                val whatMessage = when (what) {
                    MEDIA_RECORDER_INFO_MAX_DURATION_REACHED -> "Max duration reached"
                    MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED -> "Max file size reached"
                    else -> "Unknown"
                }
                Timber.d("Warning: $whatMessage, extra=$extra")
            }
        } catch (e: Throwable) {
            Timber.e("prepare() failed with error $e")
        }
        Timber.d("recording started")

        NotificationCompat.Builder(this, createNotificationChannel())
            .setContentTitle(getText(R.string.recording_call))
            .setSmallIcon(R.drawable.ic_mic_24dp)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.recording_call))
            .setOnlyAlertOnce(true)
            .build()
            .let {
                val notificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(RECORDING_NOTIFICATION_ID, it)
            }
    }


    private fun stopCallRecording() {
        recorder?.stop()
        recorder?.release()
        recorder = null
        Timber.d("recording stopped")

        NotificationCompat.Builder(this, createNotificationChannel())
            .setContentTitle(getText(R.string.waiting_for_call))
            .setSmallIcon(R.drawable.ic_standby_24dp)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.waiting_for_call))
            .setOnlyAlertOnce(true)
            .build()
            .let {
                val notificationManager =
                    getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(RECORDING_NOTIFICATION_ID, it)
            }
    }

    private fun createNotificationChannel(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_ID, NotificationManager.IMPORTANCE_NONE
            )
            channel.lightColor = ContextCompat.getColor(this, R.color.primary)
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(channel)
        }
        return CHANNEL_ID
    }


    //=================================================Added code start==========

    //=================================================Added code start==========
    private var mRecorder: MediaRecorder? = null
    private var isStarted = false


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
            mRecorder!!.setOutputFile(file.absolutePath)
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


    //=================================End================================

    //=================================End================================

    // Audio mode listening area - Start
    private fun onAudioModeChange(newMode: Int) {
        when (newMode) {
            AudioManager.MODE_IN_CALL -> {
                Timber.d("AudioMode>>> In a call")
                if (audioMode != AudioManager.MODE_IN_COMMUNICATION && audioMode != AudioManager.MODE_IN_CALL) {
                    startCallRecording()
                }
            }
            AudioManager.MODE_IN_COMMUNICATION -> {
                Timber.d("AudioMode>>> In a VOIP call")
                if (audioMode != AudioManager.MODE_IN_COMMUNICATION && audioMode != AudioManager.MODE_IN_CALL) {
                    startCallRecording()
                }
            }
            AudioManager.MODE_RINGTONE -> {
                Timber.d("AudioMode>>> In a ringtone")
            }
            AudioManager.MODE_CALL_SCREENING -> {
                Timber.d("AudioMode>>> In a call screening")
            }
            AudioManager.MODE_INVALID -> {
                Timber.d("AudioMode>>> Invalid")
            }
            AudioManager.MODE_CURRENT -> {
                Timber.d("AudioMode>>> Current")
            }
            AudioManager.MODE_NORMAL -> {
                Timber.d("AudioMode>>> Normal")
                if (audioMode == AudioManager.MODE_IN_COMMUNICATION || audioMode == AudioManager.MODE_IN_CALL) {
                    stopCallRecording()
                }
            }
            AudioManager.MODE_CALL_REDIRECT -> {
                Timber.d("AudioMode>>> Call redirect")
            }
            AudioManager.MODE_COMMUNICATION_REDIRECT -> {
                Timber.d("AudioMode>>> VOIP call redirect")
            }
        }
        audioMode = newMode
    }
    // Audio mode listening area - End

    private fun initOutputFile() {
        val recordDir = File(filesDir.path, "recorded")
        if (!recordDir.exists()) {
            recordDir.mkdir()
        }
        file = File(recordDir, "${dateFormat.format(Date())}.mp4")
        file.createNewFile()
    }

    companion object {
        private const val CHANNEL_ID = "RecordingChannel"

        private const val RECORDING_NOTIFICATION_ID = 11
        private const val AUDIO_MODE_CHECK_INTERVAL = 1000L

        @JvmStatic
        fun start(context: Context) {
            context.startService(Intent(context, CallRecordingService::class.java))
        }
    }
}