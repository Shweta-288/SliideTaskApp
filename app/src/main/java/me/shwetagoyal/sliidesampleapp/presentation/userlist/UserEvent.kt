package me.shwetagoyal.sliidesampleapp.presentation.userlist

import me.shwetagoyal.sliidesampleapp.presentation.util.UiText

sealed interface UserEvent {
    data object UserCreationSuccess : UserEvent
    data class Error(val message: UiText) : UserEvent
    data object UserDeleted : UserEvent
    data class UserLoadFailed(val message: UiText) : UserEvent
}
