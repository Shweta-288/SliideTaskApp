package me.shwetagoyal.sliidesampleapp.network

import me.shwetagoyal.sliidesampleapp.data.UserResponse
import me.shwetagoyal.sliidesampleapp.domain.util.DataError
import me.shwetagoyal.sliidesampleapp.domain.util.Result

interface UserRemoteDataSource {
    suspend fun getUsersLastPage(): Result<List<UserResponse>, DataError.Network>
    suspend fun addUser(name: String, email: String): Result<UserResponse, DataError.Network>
    suspend fun deleteUser(userId: Int): Result<Unit, DataError.Network>
}