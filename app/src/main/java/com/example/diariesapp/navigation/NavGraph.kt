package com.example.diariesapp.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.auth.navigation.authenticationRoute

import com.example.edit.navigation.editRoute
import com.example.home.navigation.homeRoute
import com.example.util.Screen




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
