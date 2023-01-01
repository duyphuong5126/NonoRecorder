package com.nonoka.nonorecorder.feature.main

import android.Manifest.permission.RECORD_AUDIO
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.core.app.ActivityCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nonoka.nonorecorder.CallRecordingService
import com.nonoka.nonorecorder.R
import com.nonoka.nonorecorder.constant.IntentConstants.actionFinishedRecording
import com.nonoka.nonorecorder.constant.IntentConstants.extraDirectory
import com.nonoka.nonorecorder.constant.Colors
import com.nonoka.nonorecorder.feature.main.home.HomePage
import com.nonoka.nonorecorder.feature.main.home.HomeViewModel
import com.nonoka.nonorecorder.feature.main.recorded.RecordedListPage
import com.nonoka.nonorecorder.feature.main.recorded.RecordedListViewModel
import com.nonoka.nonorecorder.feature.player.AudioPlayerActivity
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : AppCompatActivity() {
    private val homeViewModel: HomeViewModel by viewModels()
    private val recordedListViewModel: RecordedListViewModel by viewModels()

    private val drawOverlayPermissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK || it.resultCode == RESULT_CANCELED) {
                homeViewModel.drawOverlayPermissionStateChange(Settings.canDrawOverlays(this))
                if (!Settings.canDrawOverlays(this)) {
                    MaterialAlertDialogBuilder(this).setTitle(R.string.permission_required_title)
                        .setMessage(R.string.accessibility_permission_required_message)
                        .setPositiveButton(R.string.action_ok, null).show()
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
            val service = "$packageName/$packageName.${CallRecordingService::class.java.simpleName}"
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

    private val recordingFinishedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == actionFinishedRecording) {
                intent.getStringExtra(extraDirectory)?.let(recordedListViewModel::refresh)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeViewModel.initPermission(
            canDrawOverlay = Settings.canDrawOverlays(this@MainActivity),
            canRecordAudio = isRecordingPermissionGranted,
            hasAccessibilityPermission = isAccessibilitySettingsOn
        )
        val defaultNavigationRoutes =
            arrayOf(
                MainNavigationRoute.HomeRouteMain(label = "Home"),
                MainNavigationRoute.RecordedListRouteMain(label = "Recorded List")
            )
        setContent {

            val navController = rememberNavController()
            MaterialTheme(
                colorScheme = Colors.getColorScheme(),
            ) {
                Scaffold(
                    bottomBar = {
                        BottomNavigation(
                            backgroundColor = MaterialTheme.colorScheme.surface,
                        ) {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination

                            defaultNavigationRoutes.forEach { route ->
                                val isSelected = currentDestination?.route == route.id
                                BottomNavigationItem(
                                    selected = isSelected,
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
                                            contentDescription = "Home",
                                        )
                                    },
                                    selectedContentColor = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    },
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = defaultNavigationRoutes[0].id,
                        modifier = Modifier.padding(it),
                    ) {
                        composable(homeRouteName) {
                            HomePage(
                                viewModel = homeViewModel,
                                handleDrawOverlayPermission = {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:$packageName")
                                    )
                                    drawOverlayPermissionRequestLauncher.launch(
                                        Intent.createChooser(
                                            intent, "Settings app"
                                        )
                                    )
                                },
                                handleAudioPermission = this@MainActivity::handleAudioPermission,
                                handleAccessibilityPermission = {
                                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                    startActivity(intent)
                                }
                            )
                        }

                        composable(recordedListRouteName) {
                            RecordedListPage(recordedListViewModel) { filePath ->
                                AudioPlayerActivity.start(this@MainActivity, filePath)
                            }
                        }
                    }
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.accessibilityPermissionPermissionStateChange(isAccessibilitySettingsOn)
        recordedListViewModel.initialize(filesDir.absolutePath)

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
                homeViewModel.recordAudioPermissionStateChange(grantResults[index] == PERMISSION_GRANTED)
                return
            }
        }
    }

    private fun handleAudioPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this, RECORD_AUDIO
            )
        ) {
            MaterialAlertDialogBuilder(this).setTitle(R.string.permission_required_title)
                .setMessage(R.string.record_audio_permission_required_message)
                .setPositiveButton(R.string.action_yes) { _, _ ->
                    handleAudioPermission()
                }.setNegativeButton(R.string.action_no, null)
                .show()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(RECORD_AUDIO), URGENT_AUDIO_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getIconRes(route: MainNavigationRoute): Int {
        return when (route) {
            is MainNavigationRoute.HomeRouteMain -> R.drawable.ic_home_solid_24dp
            is MainNavigationRoute.RecordedListRouteMain -> R.drawable.ic_list_solid_24dp
        }
    }

    companion object {
        private const val URGENT_AUDIO_PERMISSION_REQUEST_CODE = 3726
    }
}