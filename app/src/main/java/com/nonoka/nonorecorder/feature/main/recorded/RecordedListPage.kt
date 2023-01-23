package com.nonoka.nonorecorder.feature.main.recorded

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.nonoka.nonorecorder.R
import com.nonoka.nonorecorder.constant.Dimens
import com.nonoka.nonorecorder.constant.titleAppBar
import com.nonoka.nonorecorder.feature.main.recorded.uimodel.RecordedItem.RecordedDate
import com.nonoka.nonorecorder.feature.main.recorded.uimodel.RecordedItem.RecordedFileUiModel
import com.nonoka.nonorecorder.feature.main.recorded.uimodel.RecordedItem.BrokenRecordedFileUiModel
import com.nonoka.nonorecorder.feature.main.recorded.uimodel.RecordedItem.FirstBannerAdUiModel
import com.nonoka.nonorecorder.isDarkTheme
import com.nonoka.nonorecorder.shared.GifImage
import com.nonoka.nonorecorder.shared.YesNoDialog
import com.nonoka.nonorecorder.shared.createSharedAudioFile
import com.nonoka.nonorecorder.shared.exportFolder
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("VisibleForTests")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordedListPage(
    recordedListViewModel: RecordedListViewModel,
    onStartPlaying: (RecordedFileUiModel) -> Unit,
    onRenameFile: (filePath: String, currentFileName: String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.recorded_list_page_title),
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
        if (recordedListViewModel.recordedList.isNotEmpty()) {
            RecordedList(
                recordedListViewModel = recordedListViewModel,
                paddingValues = it,
                onStartPlaying = onStartPlaying,
                onRenameFile = onRenameFile
            )
        } else {
            EmptyRecordedList(recordedListViewModel = recordedListViewModel, paddingValues = it)
        }
    }
}

@Composable
private fun EmptyRecordedList(
    recordedListViewModel: RecordedListViewModel,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (recordedListViewModel.isProcessingAudio) {
            GifImage(
                gifResId = if (isDarkTheme()) R.drawable.ic_loading_dark_24dp else R.drawable.ic_loading_light_24dp,
                modifier = Modifier.fillMaxWidth()
            )

            Box(modifier = Modifier.height(Dimens.normalSpace))

            Text(
                text = "Processing new file",
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            Text(text = "No recorded call", style = MaterialTheme.typography.headlineMedium)

            Box(modifier = Modifier.height(Dimens.normalSpace))

            Text(
                text = "Your calls will be auto-recorded.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun RecordedList(
    recordedListViewModel: RecordedListViewModel,
    onStartPlaying: (RecordedFileUiModel) -> Unit,
    onRenameFile: (filePath: String, currentName: String) -> Unit,
    paddingValues: PaddingValues
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val exportFilePathHolder = remember { mutableStateOf<String?>(null) }
    val exportFile = exportFilePathHolder.value?.let(::File)
    val deleteFilePathHolder = remember { mutableStateOf<String?>(null) }
    val deleteFilePath = deleteFilePathHolder.value

    val context = LocalContext.current
    if (exportFile != null) {
        YesNoDialog(
            title = stringResource(id = R.string.export_file_title),
            description = stringResource(id = R.string.export_file_message, exportFolder),
            onDismiss = {
                exportFilePathHolder.value = null
            },
            onAnswerYes = {
                coroutineScope.launch(Dispatchers.IO) {
                    context.createSharedAudioFile(exportFile)
                    coroutineScope.launch(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Exported file ${exportFile.name} to $exportFolder folder",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
        )
    }

    if (deleteFilePath != null) {
        YesNoDialog(
            title = stringResource(id = R.string.delete_file_title),
            description = stringResource(id = R.string.delete_file_message),
            onDismiss = {
                deleteFilePathHolder.value = null
            },
            onAnswerYes = {
                recordedListViewModel.deleteFile(deleteFilePath)
            },
        )
    }
    LazyColumn(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Top,
        state = listState
    ) {
        if (recordedListViewModel.isProcessingAudio) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GifImage(
                        gifResId = if (isDarkTheme()) R.drawable.ic_loading_dark_24dp else R.drawable.ic_loading_light_24dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(modifier = Modifier.height(Dimens.normalSpace))

                    Text(
                        text = "Processing new file",
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Box(modifier = Modifier.height(Dimens.normalSpace))
                }
            }
            coroutineScope.launch {
                delay(500)
                listState.animateScrollToItem(0)
            }
        }

        items(items = recordedListViewModel.recordedList, key = { item ->
            item.hashCode()
        }) { recordedItem ->
            when (recordedItem) {
                is RecordedFileUiModel -> RecordedFile(
                    recordedFile = recordedItem,
                    onStartPlaying = onStartPlaying,
                    onDeleteFile = {
                        deleteFilePathHolder.value = it
                    },
                    onExportFile = {
                        exportFilePathHolder.value = it
                    },
                    onRenameFile = onRenameFile
                )
                is RecordedDate -> RecordedDateItem(recordedDate = recordedItem)
                is BrokenRecordedFileUiModel -> BrokenRecordedFile(
                    recordedFile = recordedItem,
                    onDeleteFile = {
                        deleteFilePathHolder.value = it
                    }
                )
                is FirstBannerAdUiModel -> FirstBannerAd()
            }
        }

        item {
            Box(modifier = Modifier.height(Dimens.ultraLargeSpace))
        }
    }
}

@Composable
private fun RecordedFile(
    onStartPlaying: (RecordedFileUiModel) -> Unit,
    recordedFile: RecordedFileUiModel,
    onDeleteFile: (String) -> Unit,
    onExportFile: (String) -> Unit,
    onRenameFile: (filePath: String, currentName: String) -> Unit,
) {
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
                .padding(Dimens.mediumSpace),
        ) {
            Text(
                text = recordedFile.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
            )

            Box(modifier = Modifier.height(Dimens.normalSpace))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            ) {
                Text(
                    text = recordedFile.lastModified,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Text(
                    text = recordedFile.duration,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Box(modifier = Modifier.height(Dimens.normalSpace))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Row {
                    IconButton(onClick = {
                        onRenameFile(recordedFile.filePath, recordedFile.nameWithoutExtension)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit_solid_24dp),
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimens.extraMediumIconSize)
                        )
                    }

                    Box(modifier = Modifier.height(Dimens.mediumSpace))

                    IconButton(onClick = {
                        onDeleteFile(recordedFile.filePath)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_trash_solid_24dp),
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(Dimens.extraMediumIconSize)
                        )
                    }

                    Box(modifier = Modifier.height(Dimens.mediumSpace))

                    IconButton(onClick = {
                        onExportFile(recordedFile.filePath)
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_export_solid_24dp),
                            contentDescription = "Export",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Button(onClick = {
                    onStartPlaying(recordedFile)
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_play_solid_24dp),
                        contentDescription = "Play",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                        modifier = Modifier.height(Dimens.extraMediumIconSize)
                    )

                    Box(modifier = Modifier.width(Dimens.mediumSpace))

                    Text(text = "Play")
                }
            }
        }
    }
}

@Composable
private fun BrokenRecordedFile(
    recordedFile: BrokenRecordedFileUiModel,
    onDeleteFile: (String) -> Unit,
) {
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
                .padding(Dimens.mediumSpace),
        ) {
            Text(
                text = recordedFile.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
            )

            Box(modifier = Modifier.height(Dimens.normalSpace))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
            ) {
                Text(
                    text = recordedFile.lastModified,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Text(
                    text = "N/A",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Box(modifier = Modifier.height(Dimens.mediumSpace))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_error_solid_24dp),
                        contentDescription = "Broken",
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .width(Dimens.extraMediumIconSize)
                            .height(Dimens.extraMediumIconSize)
                    )

                    Box(modifier = Modifier.width(Dimens.smallSpace))

                    Text(
                        text = "Broken file",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = {
                        onDeleteFile(recordedFile.filePath)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(text = "Delete")
                }
            }
        }
    }
}

@Composable
private fun RecordedDateItem(recordedDate: RecordedDate) {
    Box(Modifier.height(Dimens.normalSpace))

    Text(
        text = recordedDate.date,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(horizontal = Dimens.mediumSpace)
    )
}

@SuppressLint("VisibleForTests")
@Composable
fun FirstBannerAd() {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.LARGE_BANNER)
                adUnitId = context.getString(R.string.first_banner_ad_id)
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}