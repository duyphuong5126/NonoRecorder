package com.nonoka.nonorecorder.feature.tutorials

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.nonoka.nonorecorder.NonoTheme
import com.nonoka.nonorecorder.R
import com.nonoka.nonorecorder.constant.Dimens
import com.nonoka.nonorecorder.constant.IntentConstants.extraTutorialModeId
import com.nonoka.nonorecorder.constant.titleAppBar

class TutorialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tutorialMode = TutorialMode.fromModeId(intent.getIntExtra(extraTutorialModeId, -1))
        setContent {

            TutorialsPage(mode = tutorialMode)
        }
    }

    companion object {
        @JvmStatic
        fun start(context: Context, tutorialMode: TutorialMode) {
            context.startActivity(Intent(context, TutorialActivity::class.java).apply {
                putExtra(extraTutorialModeId, tutorialMode.modeId)
            })
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TutorialsPage(
        mode: TutorialMode
    ) {
        NonoTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Permission setup guide",
                                style = MaterialTheme.typography.titleAppBar,
                            )
                        },
                        colors = TopAppBarDefaults.smallTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                        ),
                        navigationIcon = {
                            IconButton(onClick = {
                                onBackPressedDispatcher.onBackPressed()
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_back_solid_24dp),
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                },
            ) {
                Column(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Top
                ) {
                    when (mode) {
                        TutorialMode.AppearsOnTop -> AppearsOnTopPermissionPage(
                            header = {
                                TutorialHeader(tutorialMode = mode)
                            },
                        )
                        TutorialMode.Recording -> RecordingPermissionPage(
                            header = {
                                TutorialHeader(tutorialMode = mode)
                            },
                        )
                        TutorialMode.Accessibility -> AccessibilityPermissionPage(
                            header = {
                                TutorialHeader(tutorialMode = mode)
                            },
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun TutorialHeader(tutorialMode: TutorialMode) {
        Column(
            modifier = Modifier
                .padding(horizontal = Dimens.mediumSpace)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_help_24dp),
                contentDescription = "Permissions information",
                colorFilter = ColorFilter.tint(if (!isSystemInDarkTheme()) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.size(Dimens.largeIconSize)
            )

            Box(modifier = Modifier.height(Dimens.mediumSpace))

            Text(
                text = "How to setup\n${tutorialName(tutorialMode)}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = Dimens.mediumSpace),
                textAlign = TextAlign.Center
            )
        }
    }

    private fun tutorialName(mode: TutorialMode): String {
        return when (mode) {
            TutorialMode.AppearsOnTop -> "Display over other apps\npermission"
            TutorialMode.Recording -> "Recording permission"
            TutorialMode.Accessibility -> "Accessibility permission"
        }
    }
}