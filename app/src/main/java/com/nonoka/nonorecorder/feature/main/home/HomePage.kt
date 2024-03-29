package com.nonoka.nonorecorder.feature.main.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.nonoka.nonorecorder.R
import com.nonoka.nonorecorder.constant.Dimens
import com.nonoka.nonorecorder.constant.titleAppBar
import com.nonoka.nonorecorder.feature.tutorials.TutorialMode
import com.nonoka.nonorecorder.shared.YesNoDialog
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    viewModel: HomeViewModel,
    handleDrawOverlayPermission: () -> Unit,
    requestDrawOverlayPermission: () -> Unit,
    handleAudioPermission: () -> Unit,
    requestAudioPermission: () -> Unit,
    handleAccessibilityPermission: () -> Unit,
    requestPostNotificationPermission: () -> Unit,
    handleLearnMore: (TutorialMode) -> Unit
) {
    if (viewModel.showAudioPermissionRationale) {
        YesNoDialog(
            title = stringResource(id = R.string.permission_required_title),
            description = stringResource(id = R.string.record_audio_permission_required_message),
            onDismiss = {
                viewModel.showAudioPermissionRationale = false
            },
            onAnswerYes = requestAudioPermission,
        )
    }
    if (viewModel.showPostNotificationPermissionRationale) {
        YesNoDialog(
            title = stringResource(id = R.string.permission_required_title),
            description = stringResource(id = R.string.post_notification_permission_required_message),
            yesLabel = stringResource(id = R.string.action_ok),
            noLabel = stringResource(id = R.string.action_cancel),
            onDismiss = {
                viewModel.showPostNotificationPermissionRationale = false
            },
            onAnswerYes = requestPostNotificationPermission,
        )
    }
    if (viewModel.showDrawOverlayPermissionRationale) {
        YesNoDialog(
            title = stringResource(id = R.string.permission_required_title),
            description = stringResource(id = R.string.accessibility_permission_required_message),
            yesLabel = stringResource(id = R.string.action_ok),
            noLabel = stringResource(id = R.string.action_cancel),
            onDismiss = {
                viewModel.showDrawOverlayPermissionRationale = false
            },
            onAnswerYes = requestDrawOverlayPermission
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.home_page_title),
                        style = MaterialTheme.typography.titleAppBar,
                    )
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(modifier = Modifier.height(Dimens.mediumSpace))
            }

            if (viewModel.canDrawOverlay && viewModel.canRecordAudio && viewModel.hasAccessibilityPermission) {
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = Dimens.mediumSpace)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier.wrapContentSize()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_circle_solid_white_24dp),
                                contentDescription = "All set icon background",
                                modifier = Modifier.size(Dimens.largeIconSize)
                            )

                            Image(
                                painter = painterResource(id = R.drawable.ic_circle_check_solid_24dp),
                                contentDescription = "All set icon",
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.size(Dimens.largeIconSize)
                            )
                        }

                        Box(modifier = Modifier.height(Dimens.mediumSpace))

                        Text(
                            text = "You're all set!",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = Dimens.mediumSpace),
                        )

                        Box(modifier = Modifier.height(Dimens.smallSpace))

                        Text(
                            text = "All permissions for recording calls are granted.\nYour calls will be recorded automatically.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = Dimens.mediumSpace),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                item {
                    Box(modifier = Modifier.height(Dimens.normalSpace))
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.mediumSpace)
                            .clip(
                                shape = RoundedCornerShape(Dimens.normalCornersRadius)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = MaterialTheme.colorScheme.surface)
                                .padding(Dimens.normalSpace),
                        ) {
                            Text(
                                text = "✓ Display over other apps is enabled",
                                style = MaterialTheme.typography.bodyLarge,
                            )

                            Box(modifier = Modifier.height(Dimens.mediumSpace))

                            Text(
                                text = "✓ Recording permission is enabled",
                                style = MaterialTheme.typography.bodyLarge,
                            )

                            Box(modifier = Modifier.height(Dimens.mediumSpace))

                            Text(
                                text = "✓ Accessibility service is enabled",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.mediumSpace)
                            .clip(
                                shape = RoundedCornerShape(Dimens.normalCornersRadius)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = MaterialTheme.colorScheme.surface)
                                .padding(Dimens.normalSpace),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "[Important] Grant your call app necessary permissions",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Box(modifier = Modifier.height(Dimens.mediumSpace))

                            Text(
                                text = "No matter what app you use to call (VOIP or telephony), please grant it these permissions:\n- Phone\n- Microphone\nThese permissions is to make phone calls, and also makes the recording feature work properly.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth(),
                            )

                            Box(modifier = Modifier.height(Dimens.largeSpace))

                            Button(onClick = {
                                handleLearnMore(TutorialMode.CallAppPermissions)
                            }) {
                                Text(text = "Learn more")
                            }
                        }
                    }
                }
            } else {
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = Dimens.mediumSpace)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier.wrapContentSize()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_circle_solid_white_24dp),
                                contentDescription = "Permissions information background",
                                modifier = Modifier.size(Dimens.largeIconSize)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.ic_info_solid_24dp),
                                contentDescription = "Permissions information",
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                                modifier = Modifier.size(Dimens.largeIconSize)
                            )
                        }

                        Box(modifier = Modifier.height(Dimens.mediumSpace))

                        Text(
                            text = "Permissions required",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = Dimens.mediumSpace),
                        )

                        Box(modifier = Modifier.height(Dimens.smallSpace))

                        Text(
                            text = "App needs some permissions to record calls.\nPlease consider granting them.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = Dimens.mediumSpace),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                item {
                    Box(modifier = Modifier.height(Dimens.normalSpace))
                }

                // Appears on top permission
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.mediumSpace)
                            .clip(
                                shape = RoundedCornerShape(Dimens.normalCornersRadius)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = MaterialTheme.colorScheme.surface)
                                .padding(Dimens.normalSpace),
                        ) {
                            Text(
                                text = "Display over other apps permission${if (viewModel.canDrawOverlay) " is enabled" else ""}",
                                style = MaterialTheme.typography.bodyLarge,
                            )

                            Box(modifier = Modifier.height(Dimens.mediumSpace))

                            Text(
                                text = "This app needs to display over the call apps so that it can share to the audio input with them.",
                                style = MaterialTheme.typography.bodySmall,
                            )

                            Box(modifier = Modifier.height(Dimens.largeSpace))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (viewModel.canDrawOverlay) Arrangement.End else Arrangement.SpaceBetween,
                            ) {
                                if (!viewModel.canDrawOverlay) {
                                    TextButton(onClick = {
                                        handleLearnMore(TutorialMode.AppearsOnTop)
                                    }) {
                                        Text(
                                            text = "Learn more",
                                            style = MaterialTheme.typography.bodyLarge,
                                            textDecoration = TextDecoration.Underline,
                                        )
                                    }

                                    Button(onClick = handleDrawOverlayPermission) {
                                        Text(text = "Enable")
                                    }
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_square_check_regular_24dp),
                                        contentDescription = "Display over other apps permission enabled",
                                        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onSurface)
                                    )
                                }
                            }
                        }
                    }
                }

                // Recording permission
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimens.mediumSpace)
                            .clip(
                                shape = RoundedCornerShape(Dimens.normalCornersRadius)
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = MaterialTheme.colorScheme.surface)
                                .padding(Dimens.normalSpace),
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
                                        handleLearnMore(TutorialMode.Recording)
                                    }) {
                                        Text(
                                            text = "Learn more",
                                            style = MaterialTheme.typography.bodyLarge,
                                            textDecoration = TextDecoration.Underline,
                                        )
                                    }

                                    Button(onClick = handleAudioPermission) {
                                        Text(text = "Enable")
                                    }
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_square_check_regular_24dp),
                                        contentDescription = "Recording permission enabled",
                                        colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onSurface)
                                    )
                                }
                            }
                        }
                    }
                }

                // Accessibility permission
                if (viewModel.canDrawOverlay && viewModel.canRecordAudio) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.mediumSpace)
                                .clip(
                                    shape = RoundedCornerShape(Dimens.normalCornersRadius)
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = MaterialTheme.colorScheme.surface)
                                    .padding(Dimens.normalSpace),
                            ) {
                                Text(
                                    text = "Accessibility permission${if (viewModel.hasAccessibilityPermission) " is enabled" else ""}",
                                    style = MaterialTheme.typography.bodyLarge,
                                )

                                Box(modifier = Modifier.height(Dimens.mediumSpace))

                                Text(
                                    text = "This app needs accessibility permission so it can share the audio input with the call apps.",
                                    style = MaterialTheme.typography.bodySmall,
                                )

                                Box(modifier = Modifier.height(Dimens.largeSpace))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (viewModel.hasAccessibilityPermission) Arrangement.End else Arrangement.SpaceBetween,
                                ) {
                                    Timber.d("hasAccessibilityPermission=${viewModel.hasAccessibilityPermission}")
                                    if (!viewModel.hasAccessibilityPermission) {
                                        TextButton(onClick = {
                                            handleLearnMore(TutorialMode.Accessibility)
                                        }) {
                                            Text(
                                                text = "Learn more",
                                                style = MaterialTheme.typography.bodyLarge,
                                                textDecoration = TextDecoration.Underline,
                                            )
                                        }

                                        Button(onClick = handleAccessibilityPermission) {
                                            Text(text = "Enable")
                                        }
                                    } else {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_square_check_regular_24dp),
                                            contentDescription = "Accessibility permission enabled",
                                            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.onSurface)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.mediumSpace)
                        .clip(
                            shape = RoundedCornerShape(Dimens.normalCornersRadius)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.surface)
                            .padding(Dimens.normalSpace),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "[Important] This app does not support recording via wired or wireless headsets",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Box(modifier = Modifier.height(Dimens.mediumSpace))

                        Text(
                            text = "When the phone is connected to those headsets, this app can still record your call.\nHowever, the recording result may not contain the voice of you or other people in that call.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            item {
                Box(modifier = Modifier.height(Dimens.ultraLargeSpace))
            }
        }
    }
}