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
fun AppearsOnTopPermissionPage(
    header: @Composable () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = Dimens.normalSpace)
    ) {
        //========================================================
        item {
            header()
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
            Box(modifier = Modifier.height(Dimens.mediumSpace))
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.appear_on_top_1),
                    contentDescription = "Appears on top permission guild: Step 1",
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
                text = "Select \"Nono Recorder\" app on the \"Display over other apps\" list.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Box(modifier = Modifier.height(Dimens.smallSpace))
        }

        item {
            Text(
                text = "* Note: This section may be called \"Appear on top\" instead of \"Display over other apps\", depending on the phone manufacturer.",
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
                    painter = painterResource(id = R.drawable.appear_on_top_2),
                    contentDescription = "Appears on top permission guild: Step 2",
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
                text = "Turn on the \"Allow display over other apps\" switch.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            Box(modifier = Modifier.height(Dimens.smallSpace))
        }

        item {
            Text(
                text = "* Note: This switch may appear directly on the \"Display over other apps\" list (or \"Appear on top\") of Step 2, not on a separate page like this. " +
                        "It depends on the phone manufacturer.",
                style = MaterialTheme.typography.bodySmall,
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
                        painter = painterResource(id = R.drawable.appear_on_top_3),
                        contentDescription = "Appears on top permission guild: Step 3",
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