package com.example.diariesapp.presentation.screens.edit

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mongo.database.ImagesDatabase
import com.example.mongo.database.entity.ImageToDelete
import com.example.mongo.database.entity.ImageToUpload
import com.example.mongo.repository.MongoDb
import com.example.ui.GalleryImage
import com.example.ui.GalleryState
import com.example.util.Constants.EDIT_SCREEN_ARG_KEY
import com.example.util.RequestState
import com.example.util.fetchImagesFromFirebase
import com.example.util.model.Diary
import com.example.util.model.Mood
import com.example.util.toRealmInstant


import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmInstant
//import io.realm.kotlin.types.ObjectId

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.lang.Exception
import java.time.ZonedDateTime
import javax.inject.Inject


/*@HiltViewModel
class EditViewModel(
    private val savedStateHandle: SavedStateHandle, // We allow us to get the data passed thru navigation
    private val imageToUpload: ImagesDao
) : ViewModel() {*/
@HiltViewModel
class EditViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle, // We allow us to get the data passed thru navigation
    private val imageDb: ImagesDatabase
) : ViewModel() {

    val galleryState = GalleryState()
    var uiState by mutableStateOf(UiState())
        private set
    // OR : var _uiState = mutableStateOf(UiState())
    // var uiState = _uiState

    init {
        getDiaryArgument()
        fetchSelectedDiaryId()
    }

    private fun getDiaryArgument() {
        uiState = uiState.copy(
            selectedDiaryId = savedStateHandle.get<String>(
                key = EDIT_SCREEN_ARG_KEY
            ) ?: ""
        )

        /*  savedStateHandle.get<String>(key = EDIT_SCREEN_ARG_KEY)?.let {
              uiState = uiState.copy(selectedDiaryId = it)
          }*/
    }

    private fun fetchSelectedDiaryId() {
        if (uiState.selectedDiaryId != "") {
            viewModelScope.launch(Dispatchers.Main) {
                MongoDb.getSelectedDiary(
                    // diaryId = ObjectId.Companion.from(uiState.selectedDiaryId!!),
                    diaryId = ObjectId(uiState.selectedDiaryId!!)
                )
                    .catch {
                        emit(RequestState.Error(Exception("diary is already deleted")))
                    }
                    .collect { diary ->

                        if (diary is RequestState.Success) {
                            setSelectedDiary(diary = diary.data)
                            setTitle(diary.data.title)
                            setDescription(diary.data.description)
                            setMood(Mood.valueOf(diary.data.mood))

                            fetchImagesFromFirebase(
                                remoteImagesPath = diary.data.images,
                                onImageDownload = {
                                    galleryState.addImage(
                                        GalleryImage(
                                            image = it,
                                            remoteImagePath = extractImagePath(
                                                fullImageUrl = it.toString()
                                            )
                                        )
                                    )
                                },
                                onImageDownloadFailed = {

                                },

                                )
                        }
                    }

            }
        }

    }

    fun setSelectedDiary(diary: Diary) {
        uiState = uiState.copy(selectedDiary = diary)
    }

    fun setTitle(title: String) {
        uiState = uiState.copy(title = title)
    }

    fun setDescription(description: String) {
        uiState = uiState.copy(description = description)
    }

    fun setMood(mood: Mood) {
        uiState = uiState.copy(mood = mood)
    }

    fun updateDateTime(zonedDateTime: ZonedDateTime) {
        // zonedDateTime?.let { uiState.copy(updatedDateTime = it.toInstant().toRealmInstant()) }
        uiState = uiState.copy(updatedDateTime = zonedDateTime.toInstant().toRealmInstant())
    }

    fun upsertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (uiState.selectedDiaryId != "") {
                updateDiary(diary = diary, onSuccess = onSuccess, onError = onError)
                // insertDiary(diary = diary, onSuccess, onError)
            } else {
                insertDiary(diary = diary, onSuccess, onError)
            }
            deleteImageFromFirebase(images = galleryState.imageToBeDeleted.map {
                it.remoteImagePath
            })
            // galleryState.clearImagesToBeDeleted()
        }

    }

    private suspend fun insertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        val diaryToBeInserted = if (uiState.updatedDateTime != null) diary.apply {
            date = uiState.updatedDateTime!!
        } else {
            diary
        }

        val result = MongoDb.insertNewDiary(diaryToBeInserted)
        if (result is RequestState.Success) {
            uploadImagesToFireBase()
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } else if (result is RequestState.Error) {
            withContext(Dispatchers.Main) {
                onError(result.error.message.toString())
            }
        }

    }

    private suspend fun updateDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val result = MongoDb.updateDiary(diary = diary.apply {
            _id = ObjectId(uiState.selectedDiaryId!!)
            //  uiState.selectedDiaryId?.let { ObjectId(it) }!!
            date = if (uiState.updatedDateTime != null) {
                uiState.updatedDateTime!!
            } else {
                uiState.selectedDiary!!.date
            }
            // USELESS ! date = uiState.selectedDiary!!.date // This line if we want to update a diary without updating his date
        })
        if (result is RequestState.Success) {
            uploadImagesToFireBase()
            deleteImageFromFirebase()
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        } else if (result is RequestState.Error) {
            withContext(Dispatchers.Main) {
                onError(result.error.message.toString())
            }
        }
    }

    fun deleteDiary(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (uiState.selectedDiaryId != null) {
                when (val result = MongoDb.deleteDiary(ObjectId(uiState.selectedDiaryId!!))) {
                    is RequestState.Success -> {
                        withContext(Dispatchers.Main) {
                            uiState.selectedDiary?.let { deleteImageFromFirebase(images = it.images) }
                            onSuccess()
                        }
                    }

                    is RequestState.Error -> {
                        withContext(Dispatchers.Main) {
                            onError(result.error.message.toString())
                        }
                    }

                    else -> {}
                }
            }

        }
    }

    fun addImage(image: Uri, imageType: String) {
        //val imageType = context.contentResolver.getType(image)?.split("/")?.last() ?: "jpg" // we get the time of the image
        val remoteImagePath =
            "images/${FirebaseAuth.getInstance().currentUser?.uid}/" +
                    "${image.lastPathSegment}-${System.currentTimeMillis()}.$imageType"
        galleryState.addImage(
            GalleryImage(
                image = image,
                remoteImagePath = remoteImagePath
            )
        )

    }


    private fun uploadImagesToFireBase() {
        val storage = FirebaseStorage.getInstance().reference
        galleryState.images.forEach { galleryImage ->
            val imagePath = storage.child(galleryImage.remoteImagePath)
            imagePath.putFile(galleryImage.image)
                .addOnProgressListener {
                    val sessionUri = it.uploadSessionUri

                    sessionUri?.let {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageDb.imageToUploadDao.addImageToUpload(
                                imageToUpload = ImageToUpload(
                                    remoteImagePath = galleryImage.remoteImagePath,
                                    imageUri = galleryImage.image.toString(),
                                    sessionUri = it.toString()
                                )
                            )
                        }
                    }
                }

        }
    }

    private fun deleteImageFromFirebase(images: List<String>? = null) {
        val storage = FirebaseStorage.getInstance().reference
        if (images == null) {
            galleryState.imageToBeDeleted.map { it.remoteImagePath }.forEach { remotePath ->
                storage.child(remotePath).delete()
                    .addOnFailureListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageDb.imageToDeleteDao.addImageToDelete(
                                ImageToDelete(
                                    remoteImagePath = remotePath
                                )
                            )
                        }
                    }
            }
        } else {
            images.forEach { remotePath ->
                storage.child(remotePath).delete()
                    .addOnFailureListener {
                        viewModelScope.launch(Dispatchers.IO) {
                            imageDb.imageToDeleteDao.addImageToDelete(
                                ImageToDelete(
                                    remoteImagePath = remotePath
                                )
                            )
                        }
                    }
            }

        }
    }

    private fun extractImagePath(fullImageUrl: String): String {
        val chunks = fullImageUrl.split("%2F")
        val imageName = chunks[2].split("?").first()
        return "images/${Firebase.auth.currentUser?.uid}/$imageName"
    }

}

data class UiState(
    val selectedDiaryId: String? = "",
    val selectedDiary: Diary? = null,
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Calm,
    val updatedDateTime: RealmInstant? = null
)
