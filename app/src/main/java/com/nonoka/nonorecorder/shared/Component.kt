package com.nonoka.nonorecorder.shared

import android.os.Build.VERSION.SDK_INT
import android.view.Gravity
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.nonoka.nonorecorder.R
import com.nonoka.nonorecorder.constant.Dimens

@Composable
fun GifImage(
    @DrawableRes gifResId: Int,
    modifier: Modifier? = Modifier,
    size: Size = Size.ORIGINAL,
    contentDescription: String? = null,
) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(LocalContext.current)
            .data(data = gifResId).apply(
                block = {
                    size(size)
                },
            ).build(),
        imageLoader = imageLoader
    )
    if (modifier != null) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = modifier,
        )
    } else {
        Image(
            painter = painter,
            contentDescription = contentDescription,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun YesNoDialog(
    title: String,
    description: String,
    yesLabel: String = stringResource(id = R.string.action_yes),
    noLabel: String = stringResource(id = R.string.action_no),
    onDismiss: () -> Unit,
    onAnswerYes: () -> Unit = {},
    onAnswerNo: () -> Unit = {},
    properties: DialogProperties = DialogProperties(),
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = properties.let {
            DialogProperties(
                dismissOnBackPress = it.dismissOnBackPress,
                dismissOnClickOutside = it.dismissOnClickOutside,
                securePolicy = it.securePolicy,
                usePlatformDefaultWidth = false
            )
        },
    ) {
        val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
        dialogWindowProvider.window.setGravity(Gravity.BOTTOM)

        Surface(
            modifier = Modifier
                .padding(Dimens.normalSpace)
                .fillMaxWidth(),
            shape = RoundedCornerShape(Dimens.largeSpace),
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(
                            vertical = Dimens.normalSpace,
                            horizontal = Dimens.largeSpace,
                        )
                        .fillMaxWidth()
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .padding(
                            vertical = Dimens.normalSpace,
                            horizontal = Dimens.largeSpace,
                        )
                        .fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        modifier = Modifier
                            .padding(
                                horizontal = Dimens.normalSpace,
                                vertical = Dimens.mediumSpace,
                            )
                            .weight(1f),
                        onClick = {
                            onDismiss()
                            onAnswerNo()
                        },
                    ) {
                        Text(
                            text = noLabel,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(vertical = Dimens.smallSpace)
                            .width(Dimens.tinySpace)
                            .height(Dimens.normalSpace)
                            .background(color = MaterialTheme.colorScheme.outline)
                    )

                    TextButton(
                        modifier = Modifier
                            .padding(
                                horizontal = Dimens.normalSpace,
                                vertical = Dimens.mediumSpace,
                            )
                            .weight(1f),
                        onClick = {
                            onDismiss()
                            onAnswerYes()
                        },
                    ) {
                        Text(
                            text = yesLabel,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun InputDialog(
    title: String,
    initValue: String,
    hint: String = "",
    keyboardType: KeyboardType,
    imeAction: ImeAction = ImeAction.Done,
    validator: ((String) -> String?)? = null,
    submitLabel: String = stringResource(id = R.string.action_yes),
    cancelLabel: String = stringResource(id = R.string.action_no),
    onDismiss: () -> Unit,
    onSubmit: (String) -> Unit = {},
    onCancel: () -> Unit = {},
    properties: DialogProperties = DialogProperties(),
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = properties.let {
            DialogProperties(
                dismissOnBackPress = it.dismissOnBackPress,
                dismissOnClickOutside = it.dismissOnClickOutside,
                securePolicy = it.securePolicy,
                usePlatformDefaultWidth = false
            )
        },
    ) {
        val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
        dialogWindowProvider.window.setGravity(Gravity.BOTTOM)

        Surface(
            modifier = Modifier
                .padding(Dimens.normalSpace)
                .fillMaxWidth(),
            shape = RoundedCornerShape(Dimens.largeSpace),
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(
                            vertical = Dimens.normalSpace,
                            horizontal = Dimens.largeSpace,
                        )
                        .fillMaxWidth()
                )

                val editText = remember { mutableStateOf(initValue) }
                val inputError = remember { mutableStateOf<String?>(null) }

                OutlinedTextField(
                    value = editText.value,
                    onValueChange = {
                        editText.value = it
                        inputError.value = validator?.invoke(it)
                    },
                    supportingText = {
                        val error = inputError.value
                        if (error != null) {
                            Text(text = error)
                        }
                    },
                    placeholder = {
                        Text(text = hint)
                    },
                    isError = inputError.value != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = imeAction,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onDismiss()
                            onSubmit(editText.value)
                        },
                    ),
                    maxLines = 1,
                    modifier = Modifier
                        .padding(horizontal = Dimens.normalSpace)
                        .fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        modifier = Modifier
                            .padding(
                                horizontal = Dimens.normalSpace,
                                vertical = Dimens.mediumSpace,
                            )
                            .weight(1f),
                        onClick = {
                            onDismiss()
                            onCancel()
                        },
                    ) {
                        Text(
                            text = cancelLabel,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(vertical = Dimens.smallSpace)
                            .width(Dimens.tinySpace)
                            .height(Dimens.normalSpace)
                            .background(color = MaterialTheme.colorScheme.outline)
                    )

                    TextButton(
                        modifier = Modifier
                            .padding(
                                horizontal = Dimens.normalSpace,
                                vertical = Dimens.mediumSpace,
                            )
                            .weight(1f),
                        onClick = {
                            onDismiss()
                            onSubmit(editText.value)
                        },
                        enabled = editText.value.isNotBlank() && inputError.value == null
                    ) {
                        Text(
                            text = submitLabel,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}