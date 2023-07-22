package com.example.auth.screen

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.Constants.APP_ID
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import io.realm.kotlin.mongodb.GoogleAuthType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

class AuthenticationViewModel : ViewModel() {

    var loadingState = mutableStateOf(false)
        private set

    var authenticated = mutableStateOf(false)
        private set


    fun setLoading(loading: Boolean) {
        loadingState.value = loading
    }

    var loginJob: Job? = null

    fun signInWithMongoDbAtlas(
        tokenId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        loginJob?.cancel()

        loginJob = viewModelScope.launch {
            try {
                // we use withContext
                val result = withContext(Dispatchers.IO) {
                    // We use the user logged with google we try to create an account for him on mongoDb
                    App.create(APP_ID).login(
                        Credentials.google(tokenId, GoogleAuthType.ID_TOKEN)
                      //  Credentials.jwt(tokenId)  We are using this type of logging because with the google type we can get fields from the
                        // the user like name , email. all this config is done on mongoDbAtlas ( section AppServices -> Authentication
                        /*  Credentials.google(tokenId, GoogleAuthType.ID_TOKEN) // we are using credentials.google because in our
                          // mongoDb atlas we enabled the authentication via google */
                    ).loggedIn
                }

                withContext(Dispatchers.Main) {
                    if (result) {
                        onSuccess()
                        delay(600.milliseconds)
                        authenticated.value = true
                    } else {
                        onError(java.lang.Exception("user not logged"))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }

    }

}