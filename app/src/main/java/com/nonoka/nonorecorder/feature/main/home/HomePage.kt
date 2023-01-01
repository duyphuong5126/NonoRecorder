package com.nonoka.nonorecorder.feature.main.home

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
import com.nonoka.nonorecorder.R
import com.nonoka.nonorecorder.constant.Colors
import com.nonoka.nonorecorder.constant.Dimens
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    viewModel: HomeViewModel,
    handleDrawOverlayPermission: () -> Unit,
    handleAudioPermission: () -> Unit,
    handleAccessibilityPermission: () -> Unit
) {
    MaterialTheme(colorScheme = Colors.getColorScheme()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Recording permissions")
                    },
                )
            },
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
                            painter = painterResource(id = R.drawable.ic_circle_check_solid_24dp),
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
                        text = "All permissions for recording VOIP calls are granted.\nA call detector is running and ready to record your calls.",
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
                            painter = painterResource(id = R.drawable.ic_info_solid_24dp),
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
                        text = "Some permissions are required so that the app can record VOIP calls properly. Please consider granting them.",
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

                                Button(onClick = handleDrawOverlayPermission) {
                                    Text(text = "Enable")
                                }
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_square_check_regular_24dp),
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

                                Button(onClick = handleAudioPermission) {
                                    Text(text = "Enable")
                                }
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_square_check_regular_24dp),
                                    contentDescription = "Recording permission enabled",
                                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimaryContainer)
                                )
                            }
                        }
                    }
                }

                // Accessibility permission
                if (viewModel.canDrawOverlay && viewModel.canRecordAudio) {
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
                                Timber.d("Test>>> hasAccessibilityPermission=${viewModel.hasAccessibilityPermission}")
                                if (!viewModel.hasAccessibilityPermission) {
                                    TextButton(onClick = {

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
}