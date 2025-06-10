package me.shwetagoyal.sliidesampleapp.presentation.userlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    private val _state = MutableStateFlow(UserState())
    val state: StateFlow<UserState> = _state.asStateFlow()

    private val _event = MutableSharedFlow<UserEvent>()
    val event = _event.asSharedFlow()

    private val maxRetries = 3

    private val retryableErrors = setOf(
        DataError.Network.NO_INTERNET,
        DataError.Network.REQUEST_TIMEOUT,
        DataError.Network.SERVER_ERROR,
        DataError.Network.TOO_MANY_REQUESTS
    )

    init {
        loadUsers()
    }

    fun onAction(action: UserAction) {
        when (action) {
            UserAction.OnCreateUserClick -> _state.update {
                it.copy(showCreateDialog = true)
            }

            is UserAction.OnConfirmCreateUser -> createUser(action.name, action.email)
            is UserAction.OnDeleteUserClick -> _state.update {
                it.copy(showDeleteDialog = true, userIdToDelete = action.userId)
            }

            UserAction.OnConfirmDeleteUser -> deleteUser()
            UserAction.OnDialogDismiss -> _state.update {
                it.copy(
                    showCreateDialog = false,
                    showDeleteDialog = false,
                    userIdToDelete = null
                )
            }

            is UserAction.OnRefreshAction -> loadUsers(isRefresh = action.isUserGesture)
        }
    }

    private fun loadUsers(isRefresh: Boolean = false) {
        if (!isRefresh && _state.value.hasLoadedUsers) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    isRefreshing = isRefresh
                )
            }

            var retryCount = 0

            while (true) {
                val result = withContext(ioDispatcher) {
                    userRepository.getUsersLastPage()
                }

                when (result) {
                    is Result.Success -> {
                        val users =
                            result.data.map { it.copy(createdAt = System.currentTimeMillis()) }
                        _state.update { current ->
                            current.copy(
                                users = users,
                                isLoading = false,
                                isRefreshing = false,
                                hasLoadedUsers = true
                            )
                        }
                        return@launch
                    }

                    is Result.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false
                            )
                        }
                        _event.emit(UserEvent.UserLoadFailed(result.error.asUiText()))

                        if (result.error in retryableErrors && retryCount < maxRetries) {
                            retryCount++
                            delay(3000)
                            continue
                        } else {
                            return@launch
                        }
                    }
                }
            }
        }
    }


    private fun createUser(name: String, email: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, showCreateDialog = false, hasLoadedUsers = false)
            }
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
                    _state.update {
                        it.copy(
                            isLoading = false,
                            hasLoadedUsers = true
                        )
                    }
                    _event.emit(UserEvent.Error(message))
                }
            }
        }
    }


    private fun deleteUser() {
        viewModelScope.launch {
            val id = state.value.userIdToDelete ?: return@launch
            _state.update {
                it.copy(
                    isLoading = true,
                    showDeleteDialog = false,
                    userIdToDelete = null,
                    hasLoadedUsers = false
                )
            }

            when (val result = userRepository.deleteUser(id)) {
                is Result.Success -> {
                    onAction(UserAction.OnRefreshAction(isUserGesture = false))
                    _event.emit(UserEvent.UserDeleted)
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            hasLoadedUsers = true,
                        )
                    }
                    _event.emit(UserEvent.Error(result.error.asUiText()))
                }
            }
        }
    }
}