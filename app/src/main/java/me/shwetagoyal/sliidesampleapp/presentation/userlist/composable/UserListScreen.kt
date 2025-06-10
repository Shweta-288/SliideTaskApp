package me.shwetagoyal.sliidesampleapp.presentation.userlist.composable

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import me.shwetagoyal.sliidesampleapp.R
import me.shwetagoyal.sliidesampleapp.presentation.core.ObserveAsEvents
import me.shwetagoyal.sliidesampleapp.presentation.userlist.UserAction
import me.shwetagoyal.sliidesampleapp.presentation.userlist.UserEvent
import me.shwetagoyal.sliidesampleapp.presentation.userlist.UserListViewModel
import me.shwetagoyal.sliidesampleapp.presentation.userlist.UserState

@Composable
fun UserListScreenRoot(viewModel: UserListViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsState()

    ObserveAsEvents(viewModel.event) { event ->
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
    UserList(
        state = uiState,
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
                if (state.isLoading && !state.hasLoadedUsers) {
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