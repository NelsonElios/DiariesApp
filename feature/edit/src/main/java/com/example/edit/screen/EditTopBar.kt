package com.example.edit.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.components.DialogBox
import com.example.util.model.Diary
import com.example.util.toInstant
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockSelection
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTopBar(
    diary: Diary? = null,
    moodName: () -> String,
    onUpdateDateTime: (ZonedDateTime) -> Unit,
    onDeleteConfirmed: () -> Unit,
    onBackClicked: () -> Unit
) {

    val dateDialog = rememberSheetState()
    val timeDialog = rememberSheetState()

    var currentDate by remember {
        mutableStateOf(LocalDate.now())
    }

    var currentTime by remember {
        mutableStateOf(LocalTime.now())
    }

    val formattedDate = remember(key1 = currentDate) {
        DateTimeFormatter
            .ofPattern("dd MMM yyyy")
            .format(currentDate).uppercase()
    }

    val formattedTime = remember(key1 = currentTime) {
        DateTimeFormatter
            .ofPattern("hh:mm a")
            .format(currentTime).uppercase()
    }

    var dateTimeUpdated by remember {
        mutableStateOf(false)
    }

    // NAL_QUESTION : can we use derivedStateOf ?
    val selectedDiaryDate = remember(key1 = diary) {

        /*if (diary != null) {
            SimpleDateFormat("dd MMM yyyy, hh:mm;aa", Locale.getDefault())
                .format(Date.from(diary.date.toInstant())).uppercase()
        } else {
            "unknown"
        }*/
        diary?.let {
            SimpleDateFormat("dd MMM yyyy, hh:mm;aa", Locale.getDefault())
                .format(Date.from(it.date.toInstant())).uppercase()
        } ?: ""
    }

    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "edit back button"
                )
            }
        },
        title = {
            Column() {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = moodName(),
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = if (diary != null && dateTimeUpdated) "$formattedDate, $formattedTime"
                    else if (diary != null) selectedDiaryDate
                    else "$formattedDate, $formattedTime",
                    style = TextStyle(
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    ),
                    textAlign = TextAlign.Center
                )
            }
        },
        actions = {
            if (dateTimeUpdated) {
                IconButton(onClick = {
                    currentDate = LocalDate.now()
                    currentTime = LocalTime.now()
                    dateTimeUpdated = false
                    onUpdateDateTime(
                        ZonedDateTime.of(
                            currentDate,
                            currentTime,
                            ZoneId.systemDefault()
                        )
                    )
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Icon"
                    )
                }
            } else {

                IconButton(onClick = {
                    dateDialog.show()
                }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Date icon"
                    )
                }
            }
            diary?.let {
                DeleteDiaryAction(
                    selectedDiary = diary,
                    onDeleteConfirmed = onDeleteConfirmed
                )

            }
        }
    )

    CalendarDialog(
        state = dateDialog,
        selection = CalendarSelection.Date { localDate ->
            currentDate = localDate
            timeDialog.show()
        },
        config = CalendarConfig(monthSelection = true, yearSelection = true)
    )

    ClockDialog(state = timeDialog, selection = ClockSelection.HoursMinutes { hours, minutes ->
        currentTime = LocalTime.of(hours, minutes)
        dateTimeUpdated = true
        onUpdateDateTime(
            ZonedDateTime.of(
                currentDate,
                currentTime,
                ZoneId.systemDefault()
            )
        )

    }
    )
}

@Composable
fun DeleteDiaryAction(
    selectedDiary: Diary?,
    onDeleteConfirmed: () -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }
    var openDialog by remember {
        mutableStateOf(false)
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            expanded = false
        }
    ) {

        DropdownMenuItem(
            text = {
                Text(text = "Delete")
            },
            onClick = {
                openDialog = true
                expanded = false
            }
        )
    }

    DialogBox(
        title = "Delete",
        message = " Do you really want to delete this diary ?",
        dialogOpened = openDialog,
        onCloseDialog = {
            openDialog = false
        },
        onYesClicked = onDeleteConfirmed
    )

    IconButton(
        onClick = {
            expanded = !expanded
        }
    ) {

        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = " overflow menu icon",
            tint = MaterialTheme.colorScheme.onSurface
        )
    }

}