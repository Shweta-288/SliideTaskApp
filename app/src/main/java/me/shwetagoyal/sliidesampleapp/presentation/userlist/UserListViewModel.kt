package me.shwetagoyal.sliidesampleapp.presentation.userlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.shwetagoyal.sliidesampleapp.R
import me.shwetagoyal.sliidesampleapp.di.IoDispatcher
import me.shwetagoyal.sliidesampleapp.domain.UserRepository
import me.shwetagoyal.sliidesampleapp.domain.util.DataError
import me.shwetagoyal.sliidesampleapp.domain.util.Result
import me.shwetagoyal.sliidesampleapp.presentation.util.UiText
import me.shwetagoyal.sliidesampleapp.presentation.util.asUiText
import javax.inject.Inject

@HiltViewModel
class UserListViewModel @Inject constructor(
    private val userRepository: UserRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    var state by mutableStateOf(UserState())
        private set

    private val _event = MutableSharedFlow<UserEvent>()
    val event = _event.asSharedFlow()

    private val retryableErrors = setOf(
        DataError.Network.NO_INTERNET,
        DataError.Network.REQUEST_TIMEOUT,
        DataError.Network.SERVER_ERROR,
        DataError.Network.TOO_MANY_REQUESTS
    )


    fun onAction(action: UserAction) {
        when (action) {
            UserAction.OnScreenLoad -> loadUsers()
            UserAction.OnCreateUserClick -> state = state.copy(showCreateDialog = true)
            is UserAction.OnConfirmCreateUser -> createUser(action.name, action.email)
            is UserAction.OnDeleteUserClick -> state =
                state.copy(showDeleteDialog = true, userIdToDelete = action.userId)

            UserAction.OnConfirmDeleteUser -> deleteUser()
            UserAction.OnDialogDismiss -> state = state.copy(
                showCreateDialog = false,
                showDeleteDialog = false,
                userIdToDelete = null
            )

            is UserAction.OnRefreshAction -> loadUsers(
                forceReload = true,
                isUserGesture = action.isUserGesture
            )

        }
    }

    private fun loadUsers(forceReload: Boolean = false, isUserGesture: Boolean = false) {
        if (state.hasLoadedUsers && !forceReload) return

        viewModelScope.launch {
            state = state.copy(
                isLoading = !isUserGesture,
                isRefreshing = isUserGesture
            )

            while (true) {
                val result = withContext(ioDispatcher) {
                    userRepository.getUsersLastPage()
                }

                when (result) {
                    is Result.Success -> {
                        val now = System.currentTimeMillis()
                        val users = result.data.map { it.copy(createdAt = now) }

                        state = state.copy(
                            users = users,
                            isLoading = false,
                            isRefreshing = false,
                            hasLoadedUsers = true
                        )
                        return@launch
                    }

                    is Result.Error -> {
                        state = state.copy(
                            isLoading = false,
                            isRefreshing = false
                        )
                        _event.emit(UserEvent.UserLoadFailed(result.error.asUiText()))

                        if (result.error in retryableErrors) {
                            delay(3000)
                            continue
                        }else {
                            return@launch
                        }
                    }
                }
            }
        }
    }


    private fun createUser(name: String, email: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, showCreateDialog = false)
            val result = userRepository.addUser(
                name = name,
                email = email,
            )
            when (result) {
                is Result.Success -> {
                    onAction(UserAction.OnRefreshAction(isUserGesture = false))
                    _event.emit(UserEvent.UserCreationSuccess)
                }

                is Result.Error -> {
                    val message = if (result.error == DataError.Network.CONFLICT) {
                        UiText.StringResource(R.string.error_email_exists)
                    } else {
                        result.error.asUiText()
                    }
                    state = state.copy(isLoading = false)
                    _event.emit(UserEvent.Error(message))
                }
            }
        }
    }


    private fun deleteUser() {
        viewModelScope.launch {
            val id = state.userIdToDelete ?: return@launch
            state = state.copy(
                isLoading = true,
                showDeleteDialog = false,
                userIdToDelete = null)

            when (val result = userRepository.deleteUser(id)) {
                is Result.Success -> {
                    onAction(UserAction.OnRefreshAction(isUserGesture = false))
                    _event.emit(UserEvent.UserDeleted)
                }

                is Result.Error -> {
                    state = state.copy(isLoading = false)
                    _event.emit(UserEvent.Error(result.error.asUiText()))
                }
            }
        }
    }
}