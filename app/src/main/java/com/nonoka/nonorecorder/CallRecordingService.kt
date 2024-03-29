package com.nonoka.nonorecorder

import android.Manifest.permission.RECORD_AUDIO
import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.PixelFormat
import android.media.AudioManager.MODE_CALL_REDIRECT
import android.media.AudioManager.MODE_CALL_SCREENING
import android.media.AudioManager.MODE_COMMUNICATION_REDIRECT
import android.media.AudioManager.MODE_CURRENT
import android.media.AudioManager.MODE_INVALID
import android.media.AudioManager.MODE_IN_CALL
import android.media.AudioManager.MODE_IN_COMMUNICATION
import android.media.AudioManager.MODE_NORMAL
import android.media.AudioManager.MODE_RINGTONE
import android.media.MediaRecorder.AudioSource.VOICE_CALL
import android.media.MediaRecorder.AudioSource.VOICE_RECOGNITION
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.BigTextStyle
import androidx.core.content.ContextCompat
import com.nonoka.nonorecorder.constant.AppConstants.DEFAULT_CHANNELS
import com.nonoka.nonorecorder.constant.AppConstants.DEFAULT_ENCODING_BITRATE
import com.nonoka.nonorecorder.constant.AppConstants.DEFAULT_SAMPLING_RATE
import com.nonoka.nonorecorder.di.qualifier.GeneralSetting
import com.nonoka.nonorecorder.di.qualifier.RecordingSetting
import com.nonoka.nonorecorder.domain.entity.SettingCategory.AUDIO_CHANNELS
import com.nonoka.nonorecorder.domain.entity.SettingCategory.ENCODING_BITRATE
import com.nonoka.nonorecorder.domain.entity.SettingCategory.SAMPLING_RATE
import com.nonoka.nonorecorder.infrastructure.ConfigDataSource
import com.nonoka.nonorecorder.recorder.CallRecorder
import com.nonoka.nonorecorder.recorder.VideoCallRecorder
import com.nonoka.nonorecorder.shared.audioManager
import com.nonoka.nonorecorder.shared.doWhile
import com.nonoka.nonorecorder.shared.isBluetoothConnected
import com.nonoka.nonorecorder.shared.notificationManager
import dagger.hilt.android.AndroidEntryPoint
import java.text.DecimalFormat
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class CallRecordingService : AccessibilityService() {
    @Inject
    @RecordingSetting
    lateinit var recordingConfigDataSource: ConfigDataSource

    @Inject
    @GeneralSetting
    lateinit var generalConfigDataSource: ConfigDataSource

    private var windowManager: WindowManager? = null
    // ImageView back,home,notification,minimize;
    //WindowManager.LayoutParams params;
//    AccessibilityService service;

    // ImageView back,home,notification,minimize;
    //WindowManager.LayoutParams params;
    //    AccessibilityService service;

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private lateinit var callRecorder: CallRecorder

    private var _audioMode = AtomicInteger(MODE_INVALID)
    private val audioMode: Int get() = _audioMode.get()

    private val pendingIntent: PendingIntent
        get() =
            Intent(this, RedirectActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    this, 0, notificationIntent,
                    FLAG_IMMUTABLE
                )
            }

    private val isRecordingPermissionGranted: Boolean
        get() = ActivityCompat.checkSelfPermission(this, RECORD_AUDIO) == PERMISSION_GRANTED

    private val isAccessibilitySettingsOn: Boolean
        get() {
            var accessibilityEnabled = 0
            val packageName = packageName
            val codePackageName = packageName.replace(".debug", "")
            val service =
                "$packageName/$codePackageName.${CallRecordingService::class.java.simpleName}"
            val accessibilityFound = false
            try {
                accessibilityEnabled = Settings.Secure.getInt(
                    applicationContext.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED
                )
                Timber.d("accessibilityEnabled = $accessibilityEnabled")
            } catch (e: Settings.SettingNotFoundException) {
                Timber.e("Error finding setting, default accessibility to not found: ${e.localizedMessage}")
            }
            val mStringColonSplitter = TextUtils.SimpleStringSplitter(':')
            if (accessibilityEnabled == 1) {
                Timber.d("***ACCESSIBILITY IS ENABLED*** -----------------")
                val settingValue: String = Settings.Secure.getString(
                    applicationContext.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
                mStringColonSplitter.setString(settingValue)
                while (mStringColonSplitter.hasNext()) {
                    val accessibilityService = mStringColonSplitter.next()
                    Timber.d("-------------- > accessibilityService :: $accessibilityService")
                    if (accessibilityService.equals(service, ignoreCase = true)) {
                        Timber.d("We've found the correct setting - accessibility is switched on!")
                        return true
                    }
                }
            } else {
                Timber.d("***ACCESSIBILITY IS DISABLED***")
            }
            return accessibilityFound
        }

    private val arePermissionsReady: Boolean
        get() =
            Settings.canDrawOverlays(this@CallRecordingService) &&
                    isRecordingPermissionGranted &&
                    isAccessibilitySettingsOn

    private val lastTimePermissionsReady = AtomicBoolean(false)

    private var cancelRecordingTask: Job? = null

    private val numberFormat = DecimalFormat("#,###.###")

    private val standbyNotificationContentText: String
        get() {
            val samplingRateText = numberFormat.format(
                recordingConfigDataSource.getInt(SAMPLING_RATE.id, DEFAULT_SAMPLING_RATE)
            )
            val audioChannelsText = numberFormat.format(
                recordingConfigDataSource.getInt(AUDIO_CHANNELS.id, DEFAULT_CHANNELS)
            )
            val encodingBitrateText = numberFormat.format(
                recordingConfigDataSource.getInt(ENCODING_BITRATE.id, DEFAULT_ENCODING_BITRATE)
            )
            return getString(
                R.string.recording_configs,
                samplingRateText,
                encodingBitrateText,
                audioChannelsText
            )
        }

    @SuppressLint("RtlHardcoded")
    override fun onCreate() {
        super.onCreate()
        Timber.i("start RecordingService")
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Start listening to Audio mode change
        val audioManager = this.audioManager
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

        lastTimePermissionsReady.compareAndSet(false, arePermissionsReady)
        val lastTimeReady = lastTimePermissionsReady.get()
        val notification: Notification =
            NotificationCompat.Builder(this, createNotificationChannel())
                .setContentTitle(getText(if (lastTimeReady) R.string.waiting_for_call else R.string.permissions_not_enough_title))
                .apply {
                    if (BuildConfig.DEBUG && lastTimeReady) {
                        setStyle(BigTextStyle().bigText(standbyNotificationContentText))
                    }
                }
                .setSmallIcon(if (lastTimeReady) R.drawable.ic_standby_24dp else R.drawable.ic_info_24dp)
                .setContentIntent(pendingIntent)
                .setTicker(getText(if (lastTimeReady) R.string.waiting_for_call else R.string.permissions_not_enough_title))
                .setOnlyAlertOnce(true)
                .apply {
                    if (!lastTimeReady) {
                        setContentText(getString(R.string.permissions_not_enough_message))
                    }
                }
                .build()

        startForeground(RECORDING_NOTIFICATION_ID, notification)

        ioScope.launch {
            doWhile(action = {
                val permissionsReady = arePermissionsReady
                if (permissionsReady != lastTimePermissionsReady.get()) {
                    if (permissionsReady) {
                        onPermissionReady()
                    } else {
                        if (callRecorder.isRecording) {
                            callRecorder.stopCallRecording(this@CallRecordingService)
                        }
                        onPermissionRemoval()
                    }
                }
                lastTimePermissionsReady.compareAndSet(
                    lastTimePermissionsReady.get(),
                    permissionsReady
                )
            }, checker = {
                true
            }, interval = PERMISSIONS_CHECK_INTERVAL)
        }

        callRecorder = VideoCallRecorder(recordingConfigDataSource, generalConfigDataSource)

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
        callRecorder.destroy()
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

        /*//==============================Record Audio while Call received===============//
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
            Timber.d("Window view touched........:")
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
        }*/
    }

    private fun createNotificationChannel(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_ID, NotificationManager.IMPORTANCE_NONE
            )
            channel.lightColor = ContextCompat.getColor(this, R.color.greenPrimary)
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            notificationManager.createNotificationChannel(channel)
        }
        return CHANNEL_ID
    }

    // Audio mode listening area - Start
    private fun onAudioModeChange(newMode: Int) {
        when (newMode) {
            MODE_IN_CALL -> {
                Timber.d("AudioMode>>> In a call")
                startRecording(VOICE_CALL)
            }
            MODE_IN_COMMUNICATION -> {
                Timber.d("AudioMode>>> In a VOIP call")
                startRecording(VOICE_RECOGNITION)
            }
            MODE_RINGTONE -> {
                Timber.d("AudioMode>>> In a ringtone")
            }
            MODE_CALL_SCREENING -> {
                Timber.d("AudioMode>>> In a call screening")
            }
            MODE_INVALID -> {
                Timber.d("AudioMode>>> Invalid")
            }
            MODE_CURRENT -> {
                Timber.d("AudioMode>>> Current")
            }
            MODE_NORMAL -> {
                Timber.d("AudioMode>>> Normal")
                stopRecording()
            }
            MODE_CALL_REDIRECT -> {
                Timber.d("AudioMode>>> Call redirect")
            }
            MODE_COMMUNICATION_REDIRECT -> {
                Timber.d("AudioMode>>> VOIP call redirect")
            }
        }
        _audioMode.getAndSet(newMode)
    }
    // Audio mode listening area - End

    private fun startRecording(audioSource: Int) {
        val mode = audioMode
        if ((mode != MODE_IN_COMMUNICATION && mode != MODE_IN_CALL) && arePermissionsReady) {
            if (cancelRecordingTask != null) {
                cancelRecordingTask?.cancel()
                cancelRecordingTask = null
            } else {
                callRecorder.startCallRecording(this, audioSource)
                onStartRecording()
            }
        }
    }

    private fun stopRecording() {
        val mode = audioMode
        if ((mode == MODE_IN_COMMUNICATION || mode == MODE_IN_CALL) && cancelRecordingTask == null) {
            cancelRecordingTask = ioScope.launch {
                delay(ALLOWED_INTERRUPTING_TIME)
                callRecorder.stopCallRecording(this@CallRecordingService)
                onStopRecording()
                cancelRecordingTask = null
            }
        }
    }

    private fun onStartRecording() {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getText(R.string.recording_call))
            .setSmallIcon(R.drawable.ic_microphone_24dp)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.recording_call))
            .setOnlyAlertOnce(true)
            .apply {
                if (isBluetoothConnected) {
                    setStyle(
                        BigTextStyle().bigText(getString(R.string.bluetooth_on_notification))
                    )
                }
            }
            .build()
            .let {
                notificationManager.notify(RECORDING_NOTIFICATION_ID, it)
            }
    }

    private fun onStopRecording() {
        showStandByNotification()
    }

    private fun onPermissionReady() {
        showStandByNotification()
    }

    private fun onPermissionRemoval() {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getText(R.string.permissions_not_enough_title))
            .apply {
                setStyle(BigTextStyle().bigText(getText(R.string.permissions_not_enough_message)))
            }
            .setSmallIcon(R.drawable.ic_info_24dp)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.permissions_not_enough_title))
            .setOnlyAlertOnce(true)
            .build()
            .let {
                notificationManager.notify(RECORDING_NOTIFICATION_ID, it)
            }
    }

    private fun showStandByNotification() {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getText(R.string.waiting_for_call))
            .apply {
                if (BuildConfig.DEBUG) {
                    setStyle(
                        BigTextStyle().bigText(standbyNotificationContentText)
                    )
                }
            }
            .setSmallIcon(R.drawable.ic_standby_24dp)
            .setContentIntent(pendingIntent)
            .setTicker(getText(R.string.waiting_for_call))
            .setOnlyAlertOnce(true)
            .build()
            .let {
                notificationManager.notify(RECORDING_NOTIFICATION_ID, it)
            }
    }

    companion object {
        private const val CHANNEL_ID = "RecordingChannel"

        private const val RECORDING_NOTIFICATION_ID = 11

        private const val AUDIO_MODE_CHECK_INTERVAL = 1000L
        private const val PERMISSIONS_CHECK_INTERVAL = 5000L
        private const val ALLOWED_INTERRUPTING_TIME = 2000L
    }
}