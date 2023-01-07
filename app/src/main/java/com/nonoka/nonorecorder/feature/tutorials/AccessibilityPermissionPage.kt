package com.nonoka.nonorecorder.feature.tutorials

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import com.nonoka.nonorecorder.R
import com.nonoka.nonorecorder.constant.Colors
import com.nonoka.nonorecorder.constant.Dimens

@Composable
fun AccessibilityPermissionPage(
    header: @Composable () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = Dimens.normalSpace)
    ) {
        //========================================================
        item {
            header()
        }

        item {
            Box(modifier = Modifier.height(Dimens.normalSpace))
        }

        //========================================================
        item {
            Text(
                text = "* Note: This guideline bases on stock Android\'s UI. Other Android distributions or ROMs may have different naming and UIs.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        //========================================================

        item {
            Box(modifier = Modifier.height(Dimens.normalSpace))
        }

        //========================================================
        item {
            Text(
                text = "* Prerequisite: \"Recording permission\" and \"Display over other apps\" permission are granted.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        //========================================================

        item {
            Box(modifier = Modifier.height(Dimens.normalSpace))
        }

        //========================================================
        item {
            Text(
                text = "Step 1:",
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = TextDecoration.Underline,
            )
        }

        item {
            Box(modifier = Modifier.height(Dimens.mediumSpace))
        }

        item {
            Text(
                text = "Press the Enable button on the Permission page.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Box(modifier = Modifier.height(Dimens.smallSpace))
        }

        item {
            Text(
                text = "* Note: this section only appears after you granted the other two permissions.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        item {
            Box(modifier = Modifier.height(Dimens.mediumSpace))
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.accessibility_1),
                    contentDescription = "Accessibility permission guild: Step 1",
                    modifier = Modifier.border(
                        width = Dimens.tinySpace,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                )
            }
        }
        //========================================================

        item {
            Box(modifier = Modifier.height(Dimens.normalSpace))
        }

        //========================================================
        item {
            Text(
                text = "Step 2:",
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = TextDecoration.Underline,
            )
        }

        item {
            Box(modifier = Modifier.height(Dimens.mediumSpace))
        }

        item {
            Text(
                text = "Select the \"Nono Recorder\" app in the \"Downloaded apps\" section of Accessibility page.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Box(modifier = Modifier.height(Dimens.smallSpace))
        }

        item {
            Text(
                text = "* Note: this section may be called \"Installed apps\" instead of \"Downloaded apps\", depending on the phone manufacturer.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        item {
            Box(modifier = Modifier.height(Dimens.mediumSpace))
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.accessibility_2),
                    contentDescription = "Accessibility permission guild: Step 2",
                    modifier = Modifier.border(
                        width = Dimens.tinySpace,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                )
            }
        }
        //========================================================

        item {
            Box(modifier = Modifier.height(Dimens.normalSpace))
        }

        //========================================================
        item {
            Text(
                text = "Step 3:",
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = TextDecoration.Underline,
            )
        }

        item {
            Box(modifier = Modifier.height(Dimens.mediumSpace))
        }

        item {
            Text(
                text = "Turn on the \"Use Nono Recorder\" switch in the app's Accessibility page.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Box(modifier = Modifier.height(Dimens.smallSpace))
        }

        item {
            Text(
                text = "* Note 1: this switch may have the label \"On/Off\" instead of \"Use Nono Recorder\", depending on the phone manufacturer.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        item {
            Box(modifier = Modifier.height(Dimens.smallSpace))
        }

        item {
            Text(
                text = "* Note 2: this app does not support shortcuts, please don\'t turn that option on.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        item {
            Box(modifier = Modifier.height(Dimens.mediumSpace))
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.accessibility_3),
                    contentDescription = "Accessibility permission guild: Step 3",
                    modifier = Modifier.border(
                        width = Dimens.tinySpace,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                )
            }
        }
        //========================================================

        item {
            Box(modifier = Modifier.height(Dimens.normalSpace))
        }

        //========================================================
        item {
            Text(
                text = "Step 4:",
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = TextDecoration.Underline,
            )
        }

        item {
            Box(modifier = Modifier.height(Dimens.mediumSpace))
        }

        item {
            Text(
                text = "Select the \"Allow\" option on the accessibility popup.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Box(modifier = Modifier.height(Dimens.smallSpace))
        }

        item {
            Text(
                text = "* Note 1: this app does not view or control your screen.\nThis feature is available for accessibility apps, but we don\'t use it.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        item {
            Box(modifier = Modifier.height(Dimens.smallSpace))
        }

        item {
            Text(
                text = "* Note 2: this app does not view or perform actions, nor interact with apps on your behalf.\nThis feature is available for accessibility apps, but we don\'t use it.",
                style = MaterialTheme.typography.bodySmall
            )
        }

        item {
            Box(modifier = Modifier.height(Dimens.mediumSpace))
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.accessibility_4),
                    contentDescription = "Accessibility permission guild: Step 4",
                    modifier = Modifier.border(
                        width = Dimens.tinySpace,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                )
            }
        }

        item {
            Box(modifier = Modifier.height(Dimens.mediumSpace))
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.accessibility_5),
                        contentDescription = "Accessibility permission guild: Step 4",
                        modifier = Modifier.border(
                            width = Dimens.tinySpace,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    )

                    Column(
                        modifier = Modifier
                            .align(alignment = Alignment.BottomCenter),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_thumbs_up_solid_24dp),
                            contentDescription = "Well done",
                            colorFilter = ColorFilter.tint(Colors.successColor),
                            modifier = Modifier
                                .size(Dimens.tutorialThumbsUpIconSize)
                        )
                        Box(modifier = Modifier.height(Dimens.ultraLargeSpace))
                    }
                }
            }
        }

        item {
            Box(modifier = Modifier.height(Dimens.largeSpace))
        }
        //========================================================
    }
}