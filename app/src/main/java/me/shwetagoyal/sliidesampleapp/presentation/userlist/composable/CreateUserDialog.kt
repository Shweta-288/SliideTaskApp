package me.shwetagoyal.sliidesampleapp.presentation.userlist.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.shwetagoyal.sliidesampleapp.R
import me.shwetagoyal.sliidesampleapp.presentation.core.CustomAlertDialog

@Composable
fun CreateUserDialog(
    onConfirm: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    CustomAlertDialog(
        title = stringResource(R.string.create_user_title),
        confirmText = stringResource(R.string.create_button_text),
        dismissText = stringResource(R.string.cancel_button_text),
        onConfirm = { onConfirm(name, email) },
        onDismiss = onDismiss
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            label = { Text(stringResource(R.string.name_label)) }
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            label = { Text(stringResource(R.string.email_label)) }
        )
    }
}