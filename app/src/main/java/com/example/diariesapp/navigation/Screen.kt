package com.example.diariesapp.navigation

import com.example.diariesapp.utils.Constants.EDIT_SCREEN_ARG_KEY

sealed class Screen(val route: String) {
    object Authentication : Screen(route = "anthentication_screen")
    object Home : Screen(route = "home_screen")
    object Edit : Screen(
        route = "edit_screen?$EDIT_SCREEN_ARG_KEY=" +
                "{$EDIT_SCREEN_ARG_KEY}"
    ) {
        fun passDiaryId(diaryId: String) =
            "edit_screen?$EDIT_SCREEN_ARG_KEY=$diaryId"
    }
}