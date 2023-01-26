package com.nonoka.nonorecorder.feature.main

import android.Manifest.permission.POST_NOTIFICATIONS
import android.Manifest.permission.RECORD_AUDIO
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.nonoka.nonorecorder.App
import com.nonoka.nonorecorder.CallRecordingService
import com.nonoka.nonorecorder.NonoTheme
import com.nonoka.nonorecorder.R
import com.nonoka.nonorecorder.constant.Dimens
import com.nonoka.nonorecorder.constant.IntentConstants.actionFinishedRecording
import com.nonoka.nonorecorder.constant.IntentConstants.extraDirectory
import com.nonoka.nonorecorder.feature.main.home.HomePage
import com.nonoka.nonorecorder.feature.main.home.HomeViewModel
import com.nonoka.nonorecorder.feature.main.recorded.RecordedListPage
import com.nonoka.nonorecorder.feature.main.recorded.RecordedListViewModel
import com.nonoka.nonorecorder.feature.main.settings.SettingsPage
import com.nonoka.nonorecorder.feature.main.settings.SettingsViewModel
import com.nonoka.nonorecorder.feature.player.AudioPlayerActivity
import com.nonoka.nonorecorder.feature.tutorials.TutorialActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber


@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val homeViewModel: HomeViewModel by viewModels()
    private val recordedListViewModel: RecordedListViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private var rewardedAd: RewardedAd? = null

    private val drawOverlayPermissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK || it.resultCode == RESULT_CANCELED) {
                homeViewModel.canDrawOverlay = Settings.canDrawOverlays(this)
                if (!Settings.canDrawOverlays(this)) {
                    homeViewModel.showDrawOverlayPermissionRationale = true
                }
            }
        }

    private val isRecordingPermissionGranted: Boolean
        get() = ActivityCompat.checkSelfPermission(
            this, RECORD_AUDIO
        ) == PERMISSION_GRANTED

    private val isAccessibilitySettingsOn: Boolean
        get() {
            var accessibilityEnabled = 0
            val packageName = packageName
            val codePackageName = packageName.replace(".debug", "")
            val service =
                "$packageName/$codePackageName.${CallRecordingService::class.java.simpleName}"
            Timber.d("service = $service")
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

    private val canDrawOverlay: Boolean get() = Settings.canDrawOverlays(this@MainActivity)

    private val recordingFinishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == actionFinishedRecording) {
                intent.getStringExtra(extraDirectory)?.let(recordedListViewModel::refresh)
            }
        }
    }

    @SuppressLint("VisibleForTests")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(
            this
        ) {
            Timber.d("Initialized Admob, status=$it")
        }
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            this,
            getString(R.string.primary_rewarded_ad),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    Timber.d("Ad>>> RewardedAd onAdFailedToLoad, error: $error")
                    rewardedAd = null
                    recordedListViewModel.setChallengeAvailability(false)
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    super.onAdLoaded(ad)
                    Timber.d("Ad>>> RewardedAd onAdLoaded, ad: ${ad.adUnitId}")
                    rewardedAd = ad
                    recordedListViewModel.setChallengeAvailability(true)
                    rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdClicked() {
                            // Called when a click is recorded for an ad.
                            Timber.d("Ad>>> Ad was clicked.")
                        }

                        override fun onAdDismissedFullScreenContent() {
                            // Called when ad is dismissed.
                            // Set the ad reference to null so you don't show the ad a second time.
                            Timber.d("Ad>>> Ad dismissed fullscreen content.")
                            rewardedAd = null
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            // Called when ad fails to show.
                            Timber.d("Ad>>> Ad failed to show fullscreen content.")
                            rewardedAd = null
                        }

                        override fun onAdImpression() {
                            // Called when an impression is recorded for an ad.
                            Timber.d("Ad>>> Ad recorded an impression.")
                        }

                        override fun onAdShowedFullScreenContent() {
                            // Called when ad is shown.
                            Timber.d("Ad>>> Ad showed fullscreen content.")
                        }
                    }
                }
            },
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (shouldShowRequestPermissionRationale(POST_NOTIFICATIONS)) {
                homeViewModel.showPostNotificationPermissionRationale = true
            } else {
                handleNotificationPermission()
            }
        }
        homeViewModel.canDrawOverlay = canDrawOverlay
        homeViewModel.canRecordAudio = isRecordingPermissionGranted
        homeViewModel.hasAccessibilityPermission = isAccessibilitySettingsOn

        recordedListViewModel.initialize(filesDir.absolutePath)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                recordedListViewModel.startPlayingList.collect {
                    AudioPlayerActivity.start(this@MainActivity, it.filePathList, it.startPosition)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                recordedListViewModel.toastMessage.collect {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                recordedListViewModel.exportingFileChallenge.collect { exportFile ->
                    if (exportFile != null) {
                        rewardedAd?.show(
                            this@MainActivity
                        ) {
                            Timber.d("Ad>>> Reward earned, type=${it.type}, amount=${it.amount}")
                            recordedListViewModel.exportingFile = exportFile
                            recordedListViewModel.onFinishExportingFileChallenge()
                        }
                    }
                }
            }
        }

        settingsViewModel.init()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                settingsViewModel.nightMode.collect {
                    (applicationContext as App).nightModeSetting = it
                }
            }
        }

        val defaultNavigationRoutes =
            arrayOf(
                MainNavigationRoute.HomeRouteMain(label = getString(R.string.home_tab_label)),
                MainNavigationRoute.RecordedListRouteMain(label = getString(R.string.recorded_list_tab_label)),
                MainNavigationRoute.SettingsRouteMain(label = getString(R.string.settings_tab_label))
            )
        setContent {
            val navController = rememberNavController()
            NonoTheme(
                onThemeRendering = { isInDarkTheme ->
                    val window = window
                    window.addFlags(FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = if (isInDarkTheme) {
                        getColor(R.color.black)
                    } else {
                        getColor(R.color.gray_200)
                    }
                    WindowCompat.getInsetsController(
                        window, window.decorView
                    ).isAppearanceLightStatusBars = !isInDarkTheme
                },
            ) {
                Scaffold(
                    bottomBar = {
                        BottomNavigation(
                            backgroundColor = MaterialTheme.colorScheme.background,
                            elevation = 0.dp
                        ) {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination

                            defaultNavigationRoutes.forEach { route ->
                                val isSelected = currentDestination?.route == route.id
                                BottomNavigationItem(
                                    selected = isSelected,
                                    label = {
                                        Text(
                                            text = route.label,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isSelected) LocalContentColor.current else LocalContentColor.current.copy(
                                                alpha = ContentAlpha.disabled
                                            )
                                        )
                                    },
                                    onClick = {
                                        navController.navigate(route.id) {
                                            // Pop up to the start destination of the graph to
                                            // avoid building up a large stack of destinations
                                            // on the back stack as users select items
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            // Avoid multiple copies of the same destination when
                                            // re-selecting the same item
                                            launchSingleTop = true
                                            // Restore state when re-selecting a previously selected item
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            painter = painterResource(id = getIconRes(route)),
                                            contentDescription = route.label,
                                            modifier = Modifier
                                                .size(Dimens.normalIconSize)
                                                .padding(bottom = Dimens.smallSpace),
                                        )
                                    },
                                    selectedContentColor = MaterialTheme.colorScheme.onSurface,
                                    unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = ContentAlpha.disabled
                                    )
                                )
                            }
                        }
                    },
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = defaultNavigationRoutes[0].id,
                            modifier = Modifier.padding(it),
                        ) {
                            composable(homeRouteName) {
                                HomePage(
                                    viewModel = homeViewModel,
                                    handleDrawOverlayPermission = this@MainActivity::requestDrawOverlayPermission,
                                    requestDrawOverlayPermission = this@MainActivity::requestDrawOverlayPermission,
                                    handleAudioPermission = this@MainActivity::handleAudioPermission,
                                    requestAudioPermission = this@MainActivity::requestAudioPermission,
                                    handleAccessibilityPermission = {
                                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                        startActivity(intent)
                                    },
                                    requestPostNotificationPermission = this@MainActivity::handleNotificationPermission,
                                    handleLearnMore = { tutorialMode ->
                                        TutorialActivity.start(this@MainActivity, tutorialMode)
                                    }
                                )
                            }

                            composable(recordedListRouteName) {
                                RecordedListPage(
                                    recordedListViewModel,
                                    onStartPlaying = { file ->
                                        recordedListViewModel.generatePlayingList(file)
                                    },
                                )
                            }

                            composable(settingsRouteName) {
                                SettingsPage(settingsViewModel)
                            }
                        }

                        AndroidView(
                            modifier = Modifier
                                .padding(it)
                                .fillMaxWidth(),
                            factory = { context ->
                                AdView(context).apply {
                                    setAdSize(AdSize.FULL_BANNER)
                                    adUnitId = context.getString(R.string.sticky_banner_ad_id)
                                    loadAd(AdRequest.Builder().build())
                                }
                            }
                        )
                    }
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.canDrawOverlay = canDrawOverlay
        homeViewModel.canRecordAudio = isRecordingPermissionGranted
        homeViewModel.hasAccessibilityPermission = isAccessibilitySettingsOn

        registerReceiver(recordingFinishedReceiver, IntentFilter(actionFinishedRecording))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(recordingFinishedReceiver)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissions.forEachIndexed { index, permission ->
            if (permission == RECORD_AUDIO && requestCode == URGENT_AUDIO_PERMISSION_REQUEST_CODE) {
                homeViewModel.canRecordAudio = grantResults[index] == PERMISSION_GRANTED
                return
            }
        }
    }

    private fun handleAudioPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, RECORD_AUDIO
            )
        ) {
            homeViewModel.showAudioPermissionRationale = true
        } else {
            requestAudioPermission()
        }
    }

    private fun requestAudioPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(RECORD_AUDIO), URGENT_AUDIO_PERMISSION_REQUEST_CODE
        )
    }

    private fun handleNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this, arrayOf(POST_NOTIFICATIONS), POST_NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getIconRes(route: MainNavigationRoute): Int {
        return when (route) {
            is MainNavigationRoute.HomeRouteMain -> R.drawable.ic_home_solid_24dp
            is MainNavigationRoute.RecordedListRouteMain -> R.drawable.ic_list_solid_24dp
            is MainNavigationRoute.SettingsRouteMain -> R.drawable.ic_settings_24dp
        }
    }

    private fun requestDrawOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        drawOverlayPermissionRequestLauncher.launch(
            Intent.createChooser(
                intent, "Settings app"
            )
        )
    }

    companion object {
        private const val URGENT_AUDIO_PERMISSION_REQUEST_CODE = 3726
        private const val POST_NOTIFICATION_PERMISSION_REQUEST_CODE = 3734

        @JvmStatic
        fun start(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}