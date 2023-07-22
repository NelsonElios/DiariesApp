package com.example.auth.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.util.Constants.GOOGLE_CLIENT_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.stevdzasan.messagebar.ContentWithMessageBar
import com.stevdzasan.messagebar.MessageBarState
import com.stevdzasan.onetap.OneTapSignInState
import com.stevdzasan.onetap.OneTapSignInWithGoogle


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationScreen(
    authenticated: Boolean,
    loadingState: Boolean,
    oneTapState: OneTapSignInState, // from Custom lib : stevdza-san:OneTapCompose
    messageBarState: MessageBarState, // from Custom lib : stevdza-san:MessageBarCompose:1.0.5
    onButtonClicked: () -> Unit,
    onSuccessfulFirebaseSignIn: (String) -> Unit,
    onFailedFirebaseSignIn:(Exception) -> Unit,
    onDialogDismissed: (String) -> Unit,
    navigateToHome: () -> Unit,
) {

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {

        // AuthenticationContent(loadingState = loadingState, onButtonClicked = onButtonClicked)
        ContentWithMessageBar(messageBarState = messageBarState) {
            AuthenticationContent(loadingState = loadingState, onButtonClicked = onButtonClicked)
        }
    }

    OneTapSignInWithGoogle(
        state = oneTapState,
        clientId = GOOGLE_CLIENT_ID,
        onTokenIdReceived = { tokenId -> // this is the token provided by google when user is authenticated with google.
            // messageBarState.addSuccess("Successfully authenticated")
            val credentials = GoogleAuthProvider.getCredential(tokenId, null)
            FirebaseAuth.getInstance().signInWithCredential(credentials)
                .addOnCompleteListener{
                    if(it.isSuccessful) {
                        onSuccessfulFirebaseSignIn(tokenId)
                    } else {
                        it.exception?.let { it1 -> onFailedFirebaseSignIn(it1) }
                    }
                }
            onSuccessfulFirebaseSignIn(tokenId)
        },
        onDialogDismissed = { message ->
            onDialogDismissed(message)
            //  messageBarState.addError(Exception(message))

        }
    )

    LaunchedEffect(key1 = authenticated) {
        if (authenticated) {
            navigateToHome()
        }
    }


}