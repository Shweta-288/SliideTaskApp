package me.shwetagoyal.sliidesampleapp.presentation.userlist

import me.shwetagoyal.sliidesampleapp.data.UserResponse
import me.shwetagoyal.sliidesampleapp.presentation.util.UiText

data class UserState(
    val users: List<UserResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val hasLoadedUsers: Boolean = false,
    val showCreateDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val userIdToDelete: Int? = null,
    val errorMessage: UiText? = null
)
