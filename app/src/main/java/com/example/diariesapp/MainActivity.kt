package com.example.diariesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.diariesapp.navigation.NavGraph
import com.example.mongo.database.ImagesDatabase
import com.example.mongo.database.dao.ImageDao
import com.example.mongo.database.dao.ImageToDeleteDao
import com.example.mongo.database.entity.ImageToDelete
import com.example.mongo.database.entity.ImageToUpload
import com.example.ui.theme.DiariesAppTheme
import com.example.util.Constants.APP_ID
import com.example.util.Screen
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import dagger.hilt.android.AndroidEntryPoint
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var imagesDatabase: ImagesDatabase
    var keepSplashOpened = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition {
            keepSplashOpened
        } // setKeepOnScreen is used to avoid the blank screen after the splash
        // Line to make status bar transparent. the remaining config is done is theme.kt
        WindowCompat.setDecorFitsSystemWindows(window, false)
        FirebaseApp.initializeApp(this) // Firebase init
        setContent {
            DiariesAppTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    startDestination = getDestinationRoute(),
                    onDataLoaded = {
                        keepSplashOpened = false
                    }
                )
            }
        }
        cleanupCheck(
            scope = lifecycleScope,
            imageToUploadDao = imagesDatabase.imageToUploadDao,
            imageToDeleteDao = imagesDatabase.imageToDeleteDao
        )
    }
}

private fun cleanupCheck(
    scope: CoroutineScope,
    imageToUploadDao: ImageDao,
    imageToDeleteDao: ImageToDeleteDao
) {
    scope.launch {
        val result = imageToUploadDao.getAllImages()
        result.forEach {
            retryUploadImageToFirebase(
                imageToUpload = it, onSuccess = {
                    scope.launch(Dispatchers.IO) {
                        imageToUploadDao.deleteImage(imageId = it.id)
                    }
                }
            )
        }
        val result2 = imageToDeleteDao.getAllImages()
        result2.forEach {
            retryDeleteImageFromFirebase(
                imageToDelete = it, onSuccess = {
                    scope.launch(Dispatchers.IO) {
                        imageToDeleteDao.deleteImage(imageId = it.id)
                    }
                }
            )
        }
    }
}

private fun getDestinationRoute(): String {
    val user = App.create(APP_ID).currentUser
    return if (user != null && user.loggedIn) Screen.Home.route else Screen.Authentication.route
}

fun retryUploadImageToFirebase(imageToUpload: ImageToUpload, onSuccess: () -> Unit) {
    val storage = FirebaseStorage.getInstance().reference
    storage.child(imageToUpload.remoteImagePath).putFile(
        imageToUpload.imageUri.toUri(),
        storageMetadata { },
        imageToUpload.sessionUri.toUri()
    ).addOnSuccessListener {
        onSuccess()
    }
}

fun retryDeleteImageFromFirebase(imageToDelete: ImageToDelete, onSuccess: () -> Unit) {
    val storage = FirebaseStorage.getInstance().reference
    storage.child(imageToDelete.remoteImagePath).delete()
        .addOnSuccessListener { onSuccess() }
}

