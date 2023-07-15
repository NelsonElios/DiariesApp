package com.example.diariesapp.presentation.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diariesapp.connectivity.ConnectivityObserver
import com.example.diariesapp.connectivity.NetworkConnectivityObserver
import com.example.diariesapp.data.database.ImagesDatabase
import com.example.diariesapp.data.database.entity.ImageToDelete
import com.example.diariesapp.data.repository.Diaries
import com.example.diariesapp.data.repository.MongoDb
import com.example.diariesapp.utils.RequestState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZonedDateTime
import javax.inject.Inject


@RequiresApi(Build.VERSION_CODES.N)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val connectivity: NetworkConnectivityObserver,
    private val imagesDatabase: ImagesDatabase
) : ViewModel() {
    private var network by mutableStateOf(ConnectivityObserver.Status.UNAVAILABLE)

    var diaries: MutableState<Diaries> = mutableStateOf(RequestState.Idle)
    var dateIsSelected by mutableStateOf(false)
        private set
    var diariesJob: Job? = null

    private lateinit var allDiariesJob: Job
    private lateinit var filteredDiariesJob: Job

    init {
        getDiaries()
        observeNetworkConnectivity()

    }

    fun getDiaries(zonedDateTime: ZonedDateTime? = null) {
        dateIsSelected = zonedDateTime != null
        diaries.value = RequestState.Loading
        if (dateIsSelected && zonedDateTime != null) {
            getFilteredDiaries(zonedDateTime)
        } else {
            getAllDiaries()
        }
    }

    private fun observeNetworkConnectivity() {
        viewModelScope.launch {
            connectivity.observe().collect {
                network = it
            }
        }
    }

    private fun getAllDiaries() {
        //  diariesJob?.cancel()
        allDiariesJob = viewModelScope.launch {
            if(::filteredDiariesJob.isInitialized) {
                filteredDiariesJob.cancelAndJoin()
            }
            MongoDb.getAllDiaries().collect { result ->
                diaries.value = result
            }
        }
    }

    private fun getFilteredDiaries(zonedDateTime: ZonedDateTime) {
        filteredDiariesJob = viewModelScope.launch {
            if(::allDiariesJob.isInitialized) {
                allDiariesJob.cancelAndJoin()
            }
            MongoDb.getFilteredDiaries(zonedDateTime).collect {
                diaries.value = it
            }
        }
    }

    fun deleteAllDiaries(
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (network == ConnectivityObserver.Status.AVAILABLE) {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val imagesDir = "images/${userId}"
            val storage = FirebaseStorage.getInstance().reference
            storage.child(imagesDir)
                .listAll()
                .addOnSuccessListener {
                    it.items.forEach { storageRef ->
                        val imagePath = "images/${userId}/${storageRef.name}"
                        storage.child(imagePath).delete()
                            .addOnFailureListener {
                                viewModelScope.launch(Dispatchers.IO) {
                                    imagesDatabase.imageToDeleteDao.addImageToDelete(
                                        ImageToDelete(
                                            remoteImagePath = imagePath
                                        )
                                    )
                                }
                            }
                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        val result = MongoDb.deleteAllDiaries()
                        if (result is RequestState.Success) {
                            withContext(Dispatchers.Main) {
                                onSuccess()
                            }
                        } else if (result is RequestState.Error) {
                            withContext(Dispatchers.Main) {
                                onError(result.error)
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    onError(it)
                }
        } else {
            onError(Exception("No network Connectivity"))
        }
    }
}