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
fun CallAppPermissions(
    header: @Composable () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = Dimens.normalSpace)
    ) {
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

        // =====================================================================
        item {
            Box(modifier = Modifier.height(Dimens.normalSpace))
        }

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
                text = "Press and hold the launcher icon of your call app, then click the info icon.\nIn the tutorial, it's the default telephone app.",
                style = MaterialTheme.typography.bodyMedium
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
                    painter = painterResource(id = R.drawable.call_app_permissions_1),
                    contentDescription = "Phone and recording permissions guild: Step 1",
                    modifier = Modifier.border(
                        width = Dimens.tinySpace,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                )
            }
        }

        // =====================================================================
        item {
            Box(modifier = Modifier.height(Dimens.normalSpace))
        }

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
                text = "Select \"Permissions\" section on the app's Settings page",
                style = MaterialTheme.typography.bodyMedium
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
                    painter = painterResource(id = R.drawable.call_app_permissions_2),
                    contentDescription = "Phone and recording permissions guild: Step 2",
                    modifier = Modifier.border(
                        width = Dimens.tinySpace,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                )
            }
        }

        item {
            Box(modifier = Modifier.height(Dimens.normalSpace))
        }

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
                text = "Make sure to grant the following permissions: Recording and Phone",
                style = MaterialTheme.typography.bodyMedium
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
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.call_app_permissions_3),
                        contentDescription = "Phone and recording permissions guild: Step 3",
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
    }
}