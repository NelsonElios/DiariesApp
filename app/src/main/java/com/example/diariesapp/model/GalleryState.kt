package com.example.diariesapp.model

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember


@Composable
fun rememberGalleryState(): GalleryState {
    return remember {
        GalleryState()
    }
}

class GalleryState {
    val images = mutableStateListOf<GalleryImage>()
    val imageToBeDeleted = mutableStateListOf<GalleryImage>()

    fun addImage(galleryImage: GalleryImage) {
        images.add(galleryImage)
    }

    fun removeImage(galleryImage: GalleryImage) {
        images.remove(galleryImage)
        imageToBeDeleted.add(galleryImage)
    }

    /*fun clearImagesToBeDeleted() {
        imageToBeDeleted.clear()
    }*/
}

data class GalleryImage(
    val image: Uri,
    val remoteImagePath: String = "",
)