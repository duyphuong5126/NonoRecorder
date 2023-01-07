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
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nonoka.nonorecorder.CallRecordingService
import com.nonoka.nonorecorder.R
import com.nonoka.nonorecorder.constant.Colors
import com.nonoka.nonorecorder.constant.Dimens
import com.nonoka.nonorecorder.constant.IntentConstants.actionFinishedRecording
import com.nonoka.nonorecorder.constant.IntentConstants.extraDirectory
import com.nonoka.nonorecorder.constant.brandTypography
import com.nonoka.nonorecorder.databinding.LayoutDialogInputBinding
import com.nonoka.nonorecorder.feature.main.home.HomePage
import com.nonoka.nonorecorder.feature.main.home.HomeViewModel
import com.nonoka.nonorecorder.feature.main.recorded.RecordedListPage
import com.nonoka.nonorecorder.feature.main.recorded.RecordedListViewModel
import com.nonoka.nonorecorder.feature.player.AudioPlayerActivity
import com.nonoka.nonorecorder.feature.tutorials.TutorialActivity
import kotlinx.coroutines.launch
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

    private val canDrawOverlays: Boolean get() = Settings.canDrawOverlays(this@MainActivity)

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
            canDrawOverlay = canDrawOverlays,
            canRecordAudio = isRecordingPermissionGranted,
            hasAccessibilityPermission = isAccessibilitySettingsOn
        )
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

        val defaultNavigationRoutes =
            arrayOf(
                MainNavigationRoute.HomeRouteMain(label = "Home"),
                MainNavigationRoute.RecordedListRouteMain(label = "Recorded")
            )
        setContent {

            val navController = rememberNavController()
            MaterialTheme(
                colorScheme = Colors.getColorScheme(),
                typography = MaterialTheme.brandTypography()
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
                                },
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
                                onDeleteFile = { filePath ->
                                    MaterialAlertDialogBuilder(this@MainActivity).setTitle(R.string.delete_file_title)
                                        .setMessage(R.string.delete_file_message)
                                        .setPositiveButton(
                                            R.string.action_yes
                                        ) { _, _ ->
                                            recordedListViewModel.deleteFile(filePath)
                                        }.setNegativeButton(R.string.action_no, null).show()
                                },
                                onRenameFile = this@MainActivity::onRenameFile,
                            )
                        }
                    }
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.initPermission(
            canDrawOverlay = canDrawOverlays,
            canRecordAudio = isRecordingPermissionGranted,
            hasAccessibilityPermission = isAccessibilitySettingsOn
        )
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
                    ActivityCompat.requestPermissions(
                        this, arrayOf(RECORD_AUDIO), URGENT_AUDIO_PERMISSION_REQUEST_CODE
                    )
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

    private fun onRenameFile(filePath: String, currentFileName: String) {
        val viewBinding = LayoutDialogInputBinding.inflate(layoutInflater)
        viewBinding.inputText.setText(currentFileName)
        val dialog = MaterialAlertDialogBuilder(this@MainActivity)
            .setTitle(R.string.rename_file_title)
            .setView(viewBinding.root)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_rename, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val newName = viewBinding.inputText.text.toString()
                if (newName.isBlank()) {
                    Toast.makeText(this@MainActivity, "Name cannot be blank", Toast.LENGTH_SHORT)
                        .show()
                    viewBinding.inputLayout.error = "File name cannot be blank"
                } else {
                    recordedListViewModel.renameFile(filePath, newName)
                    dialog.dismiss()
                }
            }
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    companion object {
        private const val URGENT_AUDIO_PERMISSION_REQUEST_CODE = 3726

        @JvmStatic
        fun start(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java))
        }
    }
}