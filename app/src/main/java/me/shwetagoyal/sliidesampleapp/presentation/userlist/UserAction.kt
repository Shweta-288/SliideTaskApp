package me.shwetagoyal.sliidesampleapp.presentation.userlist


sealed interface UserAction{
    data object OnCreateUserClick : UserAction
    data class OnConfirmCreateUser(val name: String, val email: String) : UserAction
    data class OnDeleteUserClick(val userId: Int) : UserAction
    data object OnConfirmDeleteUser : UserAction
    data object OnDialogDismiss : UserAction
    data class OnRefreshAction(val isUserGesture: Boolean) : UserAction
}