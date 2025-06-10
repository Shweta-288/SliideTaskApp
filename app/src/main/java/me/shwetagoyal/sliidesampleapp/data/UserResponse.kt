package me.shwetagoyal.sliidesampleapp.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val gender: String,
    val status: String,
    @Transient val createdAt: Long = 0L
)
