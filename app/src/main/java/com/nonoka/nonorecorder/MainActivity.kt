package com.nonoka.nonorecorder

import android.Manifest.permission.RECORD_AUDIO
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.app.ActivityCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nonoka.nonorecorder.constant.Colors
import com.nonoka.nonorecorder.constant.Dimens
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    private val drawOverlayPermissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK || it.resultCode == RESULT_CANCELED) {
                mainViewModel.drawOverlayPermissionStateChange(Settings.canDrawOverlays(this))
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel.initPermission(
            canDrawOverlay = Settings.canDrawOverlays(this@MainActivity),
            canRecordAudio = isRecordingPermissionGranted,
            hasAccessibilityPermission = isAccessibilitySettingsOn
        )
        setContent {
            HomePage(viewModel = mainViewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.accessibilityPermissionPermissionStateChange(isAccessibilitySettingsOn)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissions.forEachIndexed { index, permission ->
            if (permission == RECORD_AUDIO && requestCode == URGENT_AUDIO_PERMISSION_REQUEST_CODE) {
                mainViewModel.recordAudioPermissionStateChange(grantResults[index] == PERMISSION_GRANTED)
                return
            }
        }
    }

    @Composable
    fun HomePage(viewModel: MainViewModel) {
        MaterialTheme(colorScheme = Colors.getColorScheme()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = "VOIP Permissions")
                        },
                    )
                },
                floatingActionButton = {
                    if (viewModel.canDrawOverlay && viewModel.canRecordAudio && viewModel.hasAccessibilityPermission) {
                        ExtendedFloatingActionButton(
                            onClick = {
                                CallRecordingService.start(this@MainActivity)
                            },
                        ) {
                            Text(text = getString(R.string.start_call_detecting_service).uppercase())
                        }
                    }
                },
                floatingActionButtonPosition = FabPosition.Center,
            ) {
                Column(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.height(Dimens.mediumSpace))

                    if (viewModel.canDrawOverlay && viewModel.canRecordAudio && viewModel.hasAccessibilityPermission) {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = Dimens.mediumSpace)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_check_circle_24dp),
                                contentDescription = "All set icon",
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                                modifier = Modifier
                                    .width(Dimens.largeIconSize)
                                    .height(Dimens.largeIconSize)
                            )

                            Text(
                                text = "You're all set!",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(horizontal = Dimens.mediumSpace),
                            )
                        }

                        Text(
                            text = "All permissions for recording VOIP are granted.\nPlease press the button \"Start Detecting Call\" so the app can detect and record them.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = Dimens.mediumSpace),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = Dimens.mediumSpace)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_info_24dp),
                                contentDescription = "Permissions information",
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground),
                                modifier = Modifier
                                    .width(Dimens.headlineIconSize)
                                    .height(Dimens.headlineIconSize)
                            )

                            Box(modifier = Modifier.height(Dimens.mediumSpace))

                            Text(
                                text = "Permissions required",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(horizontal = Dimens.mediumSpace),
                            )
                        }

                        Text(
                            text = "Some permissions are required so that this app can record VOIP calls properly. Please consider granting them.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = Dimens.mediumSpace),
                        )
                    }

                    Box(modifier = Modifier.height(Dimens.normalSpace))

                    // Appears on top permission
                    Card(
                        modifier = Modifier.padding(Dimens.mediumSpace),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.mediumSpace),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.mediumSpace),
                        ) {
                            Text(
                                text = "Appear on top permission${if (viewModel.canDrawOverlay) " is enabled" else ""}",
                                style = MaterialTheme.typography.bodyLarge,
                            )

                            Box(modifier = Modifier.height(Dimens.mediumSpace))

                            Text(
                                text = "This app needs to appear on top of the VOIP apps so that it can share to the audio input with them.",
                                style = MaterialTheme.typography.bodySmall,
                            )

                            Box(modifier = Modifier.height(Dimens.largeSpace))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (viewModel.canDrawOverlay) Arrangement.End else Arrangement.SpaceBetween,
                            ) {
                                if (!viewModel.canDrawOverlay) {
                                    TextButton(onClick = {

                                    }) {
                                        Text(
                                            text = "Learn more",
                                            style = MaterialTheme.typography.bodyLarge,
                                            textDecoration = TextDecoration.Underline,
                                        )
                                    }

                                    Button(onClick = {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:$packageName")
                                        )
                                        drawOverlayPermissionRequestLauncher.launch(
                                            Intent.createChooser(
                                                intent, "Settings app"
                                            )
                                        )
                                    }) {
                                        Text(text = "Enable")
                                    }
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_check_24dp),
                                        contentDescription = "Appear on top permission enabled",
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                                    )
                                }
                            }
                        }
                    }

                    // Recording permission
                    Card(
                        modifier = Modifier.padding(Dimens.mediumSpace),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.mediumSpace),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.mediumSpace),
                        ) {
                            Text(
                                text = "Recording permission${if (viewModel.canRecordAudio) " is enabled" else ""}",
                                style = MaterialTheme.typography.bodyLarge,
                            )

                            Box(modifier = Modifier.height(Dimens.mediumSpace))

                            Text(
                                text = "This app needs this permission to perform audio recording.",
                                style = MaterialTheme.typography.bodySmall,
                            )

                            Box(modifier = Modifier.height(Dimens.largeSpace))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (viewModel.canRecordAudio) Arrangement.End else Arrangement.SpaceBetween,
                            ) {
                                if (!viewModel.canRecordAudio) {
                                    TextButton(onClick = {

                                    }) {
                                        Text(
                                            text = "Learn more",
                                            style = MaterialTheme.typography.bodyLarge,
                                            textDecoration = TextDecoration.Underline,
                                        )
                                    }

                                    Button(onClick = {
                                        handleAudioPermission()
                                    }) {
                                        Text(text = "Enable")
                                    }
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_check_24dp),
                                        contentDescription = "Recording permission enabled",
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                                    )
                                }
                            }
                        }
                    }

                    // Accessibility permission
                    Card(
                        modifier = Modifier.padding(Dimens.mediumSpace),
                        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.mediumSpace),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.mediumSpace),
                        ) {
                            Text(
                                text = "Accessibility permission${if (viewModel.hasAccessibilityPermission) " is enabled" else ""}",
                                style = MaterialTheme.typography.bodyLarge,
                            )

                            Box(modifier = Modifier.height(Dimens.mediumSpace))

                            Text(
                                text = "This app needs accessibility permission so it can share the audio input with the VOIP apps.",
                                style = MaterialTheme.typography.bodySmall,
                            )

                            Box(modifier = Modifier.height(Dimens.largeSpace))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (viewModel.hasAccessibilityPermission) Arrangement.End else Arrangement.SpaceBetween,
                            ) {
                                if (!viewModel.hasAccessibilityPermission) {
                                    TextButton(onClick = {

                                    }) {
                                        Text(
                                            text = "Learn more",
                                            style = MaterialTheme.typography.bodyLarge,
                                            textDecoration = TextDecoration.Underline,
                                        )
                                    }

                                    Button(onClick = {
                                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                                        startActivity(intent)
                                    }) {
                                        Text(text = "Enable")
                                    }
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_check_24dp),
                                        contentDescription = "Accessibility permission enabled",
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                                    )
                                }
                            }
                        }
                    }
                }
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

    companion object {
        private const val URGENT_AUDIO_PERMISSION_REQUEST_CODE = 3726
    }
}