package com.example.edit.navigation

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import com.google.accompanist.pager.rememberPagerState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.edit.screen.EditScreen
import com.example.edit.screen.EditViewModel
import com.example.util.Constants.EDIT_SCREEN_ARG_KEY
import com.example.util.Screen
import com.example.util.model.Mood
import com.google.accompanist.pager.ExperimentalPagerApi

@OptIn(ExperimentalFoundationApi::class, ExperimentalPagerApi::class)
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