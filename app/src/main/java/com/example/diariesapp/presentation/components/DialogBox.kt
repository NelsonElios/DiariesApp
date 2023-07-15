package com.example.diariesapp.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight


@Composable
fun DialogBox(
    title: String,
    message: String,
    dialogOpened: Boolean,
    onCloseDialog: () -> Unit,
    onYesClicked: () -> Unit
) {

    if (dialogOpened) {
        AlertDialog(
            // modifier = Modifier.padding(horizontal = 24.dp),
            title = {
                Text(
                    text = title,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = message,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    fontWeight = FontWeight.Normal
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onYesClicked()
                        onCloseDialog()
                    }
                ) {
                    Text(text = "Yes")

                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onCloseDialog
                ) {
                    Text(text = "No")
                }
            },
            onDismissRequest = onCloseDialog,
        )
    }

}