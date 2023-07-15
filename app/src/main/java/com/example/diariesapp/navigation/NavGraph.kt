package com.example.diariesapp.navigation

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.diariesapp.data.repository.MongoDb
import com.example.diariesapp.model.GalleryImage
import com.example.diariesapp.model.Mood
import com.example.diariesapp.model.rememberGalleryState
import com.example.diariesapp.presentation.components.DialogBox
import com.example.diariesapp.presentation.screens.auth.AuthenticationScreen
import com.example.diariesapp.presentation.screens.auth.AuthenticationViewModel
import com.example.diariesapp.presentation.screens.edit.EditScreen
import com.example.diariesapp.presentation.screens.edit.EditViewModel
import com.example.diariesapp.presentation.screens.home.HomeScreen
import com.example.diariesapp.presentation.screens.home.HomeViewModel
import com.example.diariesapp.utils.Constants.APP_ID
import com.example.diariesapp.utils.Constants.EDIT_SCREEN_ARG_KEY
import com.example.diariesapp.utils.RequestState
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception


@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String,
    onDataLoaded: () -> Unit
) {
    NavHost(navController = navController, startDestination = startDestination) {
        authenticationRoute(
            navigateToHome = {
                navController.popBackStack()
                navController.navigate(Screen.Home.route)
            },
            onDataLoaded = onDataLoaded
        )
        homeRoute(
            navigateToEdit = {
                navController.navigate(Screen.Edit.route)
            },
            navigateToEditWithArgs = {
                navController.navigate(Screen.Edit.passDiaryId(diaryId = it))
            },
            navigateToAuth = {
                navController.popBackStack()
                navController.navigate(Screen.Authentication.route)
            },
            onDataLoaded = onDataLoaded
        )
        editRoute(onBackClicked = {
            navController.popBackStack()
        }
        )
    }
}


fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit,
    onDataLoaded: () -> Unit
) {
    composable(route = Screen.Authentication.route) {
        val oneTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()
        val viewModel: AuthenticationViewModel = viewModel()
        val authenticated by viewModel.authenticated
        val loadingState by viewModel.loadingState

        LaunchedEffect(key1 = Unit) {
            onDataLoaded()
        }
        AuthenticationScreen(
            authenticated = authenticated,
            loadingState = oneTapState.opened,
            oneTapState = oneTapState,
            messageBarState = messageBarState,
            onButtonClicked = {
                oneTapState.open()
                viewModel.setLoading(true)
            },
            onSuccessfulFirebaseSignIn = { tokenId ->
                //messageBarState.addSuccess("Successfully Authenticated")
                viewModel.signInWithMongoDbAtlas(
                    tokenId,
                    onSuccess = {
                        messageBarState.addSuccess("Successfully Authenticated")
                        viewModel.setLoading(false)
                    },
                    onError = { exception ->
                        messageBarState.addError(exception)
                        viewModel.setLoading(false)
                    }
                )
            },
            onFailedFirebaseSignIn = {
                messageBarState.addError(it)
                viewModel.setLoading(false)
            },
            onDialogDismissed = { message ->
                messageBarState.addError(Exception(message))
                viewModel.setLoading(false)
            },
            navigateToHome = navigateToHome

        )

    }
}

@RequiresApi(Build.VERSION_CODES.N)
@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.homeRoute(
    navigateToEdit: () -> Unit,
    navigateToEditWithArgs: (String) -> Unit,
    navigateToAuth: () -> Unit,
    onDataLoaded: () -> Unit,
) {
    composable(route = Screen.Home.route) {
        val vm: HomeViewModel = hiltViewModel()
        val context = LocalContext.current
        val diaries by vm.diaries
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        var signOutDialogOpened by remember {
            mutableStateOf(false)
        }
        var deleteDialogOpened by remember {
            mutableStateOf(false)
        }
        val scope = rememberCoroutineScope()

        LaunchedEffect(key1 = diaries) {
            if (diaries !is RequestState.Loading) {
                onDataLoaded()
            }
        }

        HomeScreen(
            diaries = diaries,
            drawerState = drawerState,
            onMenuClicked = {
                scope.launch {
                    drawerState.open()
                }
            },
            navigateToEdit = navigateToEdit,
            navigateToEditWithArgs = navigateToEditWithArgs,
            onSignOutClicked = {
                signOutDialogOpened = true
            },
            onDeleteClicked = {
                deleteDialogOpened = true
            },
            onDateSelected = {
                vm.getDiaries(it)
            },
            onDateReset = {
                vm.getDiaries()
            },
            dateIsSelected = vm.dateIsSelected
        )

        LaunchedEffect(key1 = Unit) {
            MongoDb.configureRealm()
        }


        DialogBox(
            title = "Sign out",
            message = " Are you sure ?",
            dialogOpened = signOutDialogOpened,
            onCloseDialog = { signOutDialogOpened = false },
            onYesClicked = {
                scope.launch(Dispatchers.IO) {
                    val user = App.create(APP_ID).currentUser
                    if (user != null) {
                        user.logOut()
                        //navigation must be call in the main thread.
                        withContext(Dispatchers.Main) {
                            navigateToAuth()

                        }

                    }
                }
            }
        )

        DialogBox(
            title = "Delete all diaries",
            message = " Are you sure ?",
            dialogOpened = deleteDialogOpened,
            onCloseDialog = { deleteDialogOpened = false },
            onYesClicked = {
                vm.deleteAllDiaries(
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "All gone !",
                            Toast.LENGTH_SHORT
                        ).show()
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    onError = {
                        Toast.makeText(
                            context,
                            if (it.message === "") "No internet !" else "Something went wrong",
                            Toast.LENGTH_SHORT
                        ).show()
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        )

    }
}

@OptIn(ExperimentalPagerApi::class)
fun NavGraphBuilder.editRoute(
    onBackClicked: () -> Unit
) {
    composable(
        route = Screen.Edit.route,
        arguments = listOf(navArgument(name = EDIT_SCREEN_ARG_KEY) {
            type = NavType.StringType
            nullable = true
            defaultValue = ""
        })
    ) {
        val viewModel: EditViewModel = hiltViewModel()
        val uiState = viewModel.uiState
        val pagerState = rememberPagerState()
        val pageNumber by remember {
            derivedStateOf { pagerState.currentPage }
        }
        val galleryState = viewModel.galleryState

        val context = LocalContext.current



        EditScreen(
            uiState = uiState,
            pagerState = pagerState,
            galleryState = galleryState,
            onBackClicked = onBackClicked,
            onDeleteConfirmed = {
                viewModel.deleteDiary(
                    onSuccess = {
                        Toast.makeText(
                            context, "Deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                        onBackClicked()
                    },
                    onError = {
                        Toast.makeText(
                            context, it,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            onTitleChanged = {
                viewModel.setTitle(title = it)
            },
            onUpdateDateTime = {
                viewModel.updateDateTime(zonedDateTime = it)
            },
            onDescriptionChanged = viewModel::setDescription,
            moodName = { Mood.values()[pageNumber].name },
            onSaveClicked = { diary ->
                viewModel.upsertDiary(diary = diary.apply {
                    mood = Mood.values()[pageNumber].name
                },
                    onSuccess = onBackClicked,
                    onError = {
                        Toast.makeText(
                            context, it,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            onImageSelect = {
                val type = context.contentResolver.getType(it)?.split("/")?.last()
                    ?: "jpg" // we get the time of the image
                viewModel.addImage(image = it, imageType = type)

            },
            onDeleteImageClicked = {
                galleryState.removeImage(it)
            }
        )

    }
}