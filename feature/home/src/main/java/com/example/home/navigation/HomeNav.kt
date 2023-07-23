package com.example.home.navigation

import android.widget.Toast
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.home.screen.HomeScreen
import com.example.home.screen.HomeViewModel
import com.example.mongo.repository.MongoDb
import com.example.ui.components.DialogBox
import com.example.util.Constants
import com.example.util.RequestState
import com.example.util.Screen
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                    val user = App.create(Constants.APP_ID).currentUser
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