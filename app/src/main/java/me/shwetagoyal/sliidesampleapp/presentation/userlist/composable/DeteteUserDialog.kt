package me.shwetagoyal.sliidesampleapp.presentation.userlist.composable

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import me.shwetagoyal.sliidesampleapp.R
import me.shwetagoyal.sliidesampleapp.presentation.core.CustomAlertDialog

@Composable
fun ConfirmDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    CustomAlertDialog(
        title = stringResource(R.string.confirm_delete_title),
        confirmText = stringResource(R.string.delete_button_text),
        dismissText = stringResource(R.string.cancel_button_text),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    ) {
        Text(stringResource(R.string.confirm_delete_message))
    }
}