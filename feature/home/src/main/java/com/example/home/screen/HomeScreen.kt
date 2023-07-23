package com.example.home.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.mongo.repository.Diaries
import com.example.util.RequestState
import java.time.ZonedDateTime


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    diaries: Diaries,
    drawerState: DrawerState,
    onMenuClicked: () -> Unit,
    navigateToEdit: () -> Unit,
    dateIsSelected: Boolean,
    onDateSelected: (ZonedDateTime) -> Unit,
    onDateReset: () -> Unit,
    navigateToEditWithArgs: (String) -> Unit,
    onSignOutClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    var padding by remember {
        mutableStateOf(PaddingValues())
    }

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior() // animation to pass to our topBar
    NavigationDrawer(
        drawerState = drawerState,
        onSignOutClicked = onSignOutClicked,
        onDeleteClicked = onDeleteClicked
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(
                scrollBehavior.nestedScrollConnection
            ),// We connect the scrollbehavior to the scaffold so it can follow the scroll
            topBar = {
                HomeTopBar(
                    onMenuClicked = onMenuClicked,
                    scrollBehavior = scrollBehavior,
                    dateIsSelected = dateIsSelected,
                    onDateReset = onDateReset,
                    onDateSelected = onDateSelected

                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(end = padding.calculateEndPadding(LayoutDirection.Ltr)),
                    onClick = navigateToEdit
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "new Diary"
                    )
                }
            }
        ) {
            padding = it
            when (diaries) {
                is RequestState.Success -> {
                    HomeContent(
                        paddingValues = it,
                        diaryNotes = diaries.data,
                        onClick = navigateToEditWithArgs
                    )
                }

                is RequestState.Error -> {
                    EmptyContent(
                        title = "An error occured ! Please, Try again later !",
                        subtitle = "${diaries.error.message}"
                    )
                }

                is RequestState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                else -> {}
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    onSignOutClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        modifier = Modifier.size(250.dp),
                        painter = painterResource(id = com.example.ui.R.drawable.logo),
                        contentDescription = "Logo"
                    )

                }
                NavigationDrawerItem(
                    label = {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = com.example.ui.R.drawable.google_logo),
                                contentDescription = "Google Logo"
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "Sign out", color = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    selected = false,
                    onClick = onSignOutClicked
                )
                NavigationDrawerItem(
                    label = {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Google Logo"
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Delete all diaries",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    selected = false,
                    onClick = onDeleteClicked
                )
            }
        },
        content = content,
    )


}