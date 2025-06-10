package me.shwetagoyal.sliidesampleapp.presentation.userlist

import android.widget.Toast
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import me.shwetagoyal.sliidesampleapp.R
import me.shwetagoyal.sliidesampleapp.data.UserResponse
import me.shwetagoyal.sliidesampleapp.presentation.util.toTimeOnly

@Composable
fun UserListScreenRoot(viewModel: UserListViewModel = hiltViewModel()) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onAction(UserAction.OnScreenLoad)
        viewModel.event.collectLatest { event ->
            when (event) {
                is UserEvent.UserCreationSuccess -> {
                    Toast.makeText(context, R.string.user_created_successfully, Toast.LENGTH_SHORT)
                        .show()
                }

                is UserEvent.UserDeleted -> {
                    Toast.makeText(context, R.string.user_deleted_successfully, Toast.LENGTH_SHORT)
                        .show()
                }

                is UserEvent.Error -> {
                    Toast.makeText(context, event.message.asString(context), Toast.LENGTH_LONG)
                        .show()
                }

                is UserEvent.UserLoadFailed -> {
                    Toast.makeText(context, event.message.asString(context), Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }
    UserList(
        state = viewModel.state,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserList(
    state: UserState,
    onAction: (UserAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.sliide_user_list)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAction(UserAction.OnCreateUserClick) }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(UserAction.OnRefreshAction(isUserGesture = true)) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.users) { user ->
                            UserItem(
                                user = user,
                                onLongClick = {
                                    onAction(UserAction.OnDeleteUserClick(user.id))
                                }
                            )
                        }
                    }
                }
            }

            if (state.showCreateDialog) {
                CreateUserDialog(
                    onConfirm = { name, email ->
                        onAction(UserAction.OnConfirmCreateUser(name, email))
                    },
                    onDismiss = { onAction(UserAction.OnDialogDismiss) }
                )
            }

            if (state.showDeleteDialog) {
                ConfirmDeleteDialog(
                    onConfirm = { onAction(UserAction.OnConfirmDeleteUser) },
                    onDismiss = { onAction(UserAction.OnDialogDismiss) }
                )
            }
        }
    }
}

@Composable
fun UserItem(user: UserResponse, onLongClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = user.createdAt.toTimeOnly(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun CreateUserDialog(onConfirm: (String, String) -> Unit, onDismiss: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.create_user_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, email) }) {
                Text(
                    text = stringResource(id = R.string.create_button_text)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(id = R.string.cancel_button_text)
                )
            }
        }
    )
}

@Composable
fun ConfirmDeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(id = R.string.confirm_delete_title))
        },
        text = {
            Text(text = stringResource(id = R.string.confirm_delete_message))
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(text = stringResource(id = R.string.delete_button_text)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(text = stringResource(id = R.string.cancel_button_text)) } }
    )
}