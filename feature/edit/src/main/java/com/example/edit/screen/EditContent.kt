package com.example.edit.screen

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.GalleryImage
import com.example.ui.GalleryState
import com.example.ui.components.GalleryUploader
import com.example.util.model.Diary
import com.example.util.model.Mood
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import io.realm.kotlin.ext.toRealmList
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class, ExperimentalPagerApi::class
)
@Composable
fun EditContent(
    title: String,
    description: String,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    pagerState: PagerState,
    paddingValues: PaddingValues,
    uiState: UiState,
    galleryState: com.example.ui.GalleryState,
    onSaveClicked: (Diary) -> Unit,
    onImageSelect: (Uri) -> Unit,
    onImageClicked: (GalleryImage) -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    } // This is use to scroll automatically when we are writing a long text

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding()
            .padding(bottom = paddingValues.calculateBottomPadding())
            .padding(bottom = 24.dp)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(state = scrollState) // this verticalScroll is useful to handle de scroll when the keyboard appears
        ) {
            Spacer(
                modifier = Modifier
                    .height(30.dp)
            )
            HorizontalPager(
                state = pagerState,
                count = Mood.values().size
            ) { pageIndex ->
                AsyncImage(
                    modifier = Modifier
                        .size(120.dp),
                    model = ImageRequest
                        .Builder(LocalContext.current)
                        .data(Mood.values()[pageIndex].icon)
                        .crossfade(true)
                        .build(),
                    contentDescription = " image carousel"
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = title,
                onValueChange = onTitleChanged,
                placeholder = {
                    Text(text = "Title of your diary")
                },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Unspecified,
                    disabledIndicatorColor = Color.Unspecified,
                    unfocusedIndicatorColor = Color.Unspecified,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        scope.launch {
                            scrollState.scrollTo(Int.MAX_VALUE)
                        }
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                ),
                maxLines = 1,
                singleLine = true,
            )

            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = description,
                onValueChange = onDescriptionChanged,
                placeholder = {
                    Text(text = "Details ?")
                },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Unspecified,
                    disabledIndicatorColor = Color.Unspecified,
                    unfocusedIndicatorColor = Color.Unspecified,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        focusManager.clearFocus()
                    }
                )

            )

        }

        Column(verticalArrangement = Arrangement.Bottom) {
            Spacer(modifier = Modifier.height(12.dp))
            GalleryUploader(
                galleryState = galleryState,
                onAddClicked = { focusManager.clearFocus() },
                onImageSelect = onImageSelect,
                onImageClicked = onImageClicked
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                onClick = {
                    if (uiState.title.isNotEmpty() && uiState.description.isNotEmpty()) {
                        onSaveClicked(
                            Diary().apply {
                                this.title = uiState.title
                                this.description = uiState.description
                                this.images = galleryState.images.map {
                                    it.remoteImagePath
                                }.toRealmList()
                            }
                        )
                    } else {
                        Toast.makeText(
                            context,
                            "Fields must be filled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                shape = Shapes().small
            ) {
                Text(text = "Save")
            }
        }
    }
}