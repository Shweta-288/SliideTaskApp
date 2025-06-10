package me.shwetagoyal.sliidesampleapp.data

import kotlinx.serialization.Serializable

@Serializable
data class CreateUser(
    val name: String,
    val email: String,
    val gender: String,
    val status: String
)
