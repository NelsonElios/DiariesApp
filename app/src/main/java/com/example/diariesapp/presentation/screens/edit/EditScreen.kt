package com.example.diariesapp.presentation.screens.edit

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.diariesapp.model.Diary
import com.example.diariesapp.model.GalleryImage
import com.example.diariesapp.model.GalleryState
import com.example.diariesapp.model.Mood
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import java.time.ZonedDateTime

@OptIn(ExperimentalPagerApi::class)
@Composable
fun EditScreen(
    uiState: UiState,
    pagerState: PagerState,
    galleryState: GalleryState,
    onDeleteConfirmed: () -> Unit,
    onBackClicked: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    moodName: () -> String,
    onSaveClicked: (Diary) -> Unit,
    onUpdateDateTime: (ZonedDateTime) -> Unit,
    onImageSelect: (Uri) -> Unit,
    onDeleteImageClicked: (GalleryImage) -> Unit
) {

    var selectedGalleryImage by remember {
        mutableStateOf<GalleryImage?>(null)
    }

    LaunchedEffect(key1 = uiState.mood) {
        pagerState.scrollToPage(Mood.valueOf(uiState.mood.name).ordinal)
    }

    Scaffold(
        topBar = {
            EditTopBar(
                diary = uiState.selectedDiary,
                onDeleteConfirmed = onDeleteConfirmed,
                onBackClicked = onBackClicked,
                moodName = moodName,
                onUpdateDateTime = onUpdateDateTime
            )
        }
    ) { paddingValues ->

        EditContent(
            title = uiState.title,
            description = uiState.description,
            onTitleChanged = onTitleChanged,
            onDescriptionChanged = onDescriptionChanged,
            pagerState = pagerState,
            paddingValues = paddingValues,
            uiState = uiState,
            galleryState = galleryState,
            onSaveClicked = onSaveClicked,
            onImageSelect = onImageSelect,
            onImageClicked = {
                selectedGalleryImage = it
            }

        )
        AnimatedVisibility(visible = selectedGalleryImage != null) {
            Dialog(onDismissRequest = { selectedGalleryImage = null }) {
                ZoomableImage(
                    selectedGalleryImage = selectedGalleryImage!!,
                    onCloseClicked = { selectedGalleryImage = null },
                    onDeleteClicked = {
                        if(selectedGalleryImage != null){
                            onDeleteImageClicked(selectedGalleryImage!!)
                            selectedGalleryImage = null
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ZoomableImage(
    selectedGalleryImage: GalleryImage,
    onCloseClicked: () -> Unit,
    onDeleteClicked: () -> Unit,

    ) {

    var offsetX by remember {
        mutableStateOf(0f)
    }
    var offsetY by remember {
        mutableStateOf(0f)
    }
    var scale by remember {
        mutableStateOf(1f)
    }
    Box(modifier = Modifier
        .pointerInput(Unit) {
            detectTransformGestures { _, pan, zoom, _ ->
                scale = maxOf(1f, minOf(scale * zoom, 5f))
                val maxX = (size.width * (scale - 1)) / 2
                val minX = -maxX
                offsetX = maxOf(minX, minOf(maxX, offsetX + pan.x))
                val maxY = (size.height * (scale - 1)) / 2
                val minY = -maxY
                offsetY = maxOf(minY, minOf(maxY, offsetY + pan.y))
            }
        }
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = maxOf(.5f, minOf(3f, scale)),
                    scaleY = maxOf(.5f, minOf(3f, scale)),
                    translationX = offsetX,
                    translationY = offsetY
                ),
            model = ImageRequest.Builder(LocalContext.current)
                .data(selectedGalleryImage.image.toString())
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Fit,
            contentDescription = "Gallery Image"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onCloseClicked) {
                Icon(imageVector = Icons.Default.Close, contentDescription = " Close Icon")
                Text(text = "Close")
            }

            Button(onClick = onDeleteClicked) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = " Delete Icon")
                Text(text = "Delete")
            }
        }
    }


}