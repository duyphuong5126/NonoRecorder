package com.nonoka.nonorecorder.feature.main.recorded

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nonoka.nonorecorder.constant.Colors
import com.nonoka.nonorecorder.constant.Dimens
import com.nonoka.nonorecorder.feature.main.recorded.uimodel.RecordedItem
import com.nonoka.nonorecorder.feature.main.recorded.uimodel.RecordedItem.RecordedDate
import com.nonoka.nonorecorder.feature.main.recorded.uimodel.RecordedItem.RecordedFileUiModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordedListPage(recordedListViewModel: RecordedListViewModel) {
    MaterialTheme(colorScheme = Colors.getColorScheme()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Recorded files")
                    },
                )
            },
        ) {
            if (recordedListViewModel.recordedList.isNotEmpty()) {
                RecordedList(list = recordedListViewModel.recordedList, paddingValues = it)
            } else {
                EmptyRecordedList(paddingValues = it)
            }
        }
    }
}

@Composable
private fun EmptyRecordedList(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "No recorded call", style = MaterialTheme.typography.headlineMedium)

        Box(modifier = Modifier.height(Dimens.normalSpace))

        Text(
            text = "You have no recorded VOIP call.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RecordedList(list: List<RecordedItem>, paddingValues: PaddingValues) {
    LazyColumn(
        modifier = Modifier.padding(paddingValues), verticalArrangement = Arrangement.Top,
    ) {
        items(items = list, key = { item ->
            item.hashCode()
        }) { recordedItem ->
            when (recordedItem) {
                is RecordedFileUiModel -> RecordedFile(recordedFile = recordedItem)
                is RecordedDate -> RecordedDateItem(recordedDate = recordedItem)
            }
        }
    }
}

@Composable
private fun RecordedFile(recordedFile: RecordedFileUiModel) {
    Card(
        modifier = Modifier
            .padding(Dimens.mediumSpace)
            .height(80.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.mediumSpace),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(Dimens.mediumSpace)
                .wrapContentHeight()
                .fillMaxWidth(),
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
                    style = MaterialTheme.typography.bodySmall,
                )
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