package me.shwetagoyal.sliidesampleapp.network

import io.ktor.client.HttpClient
import me.shwetagoyal.sliidesampleapp.data.CreateUser
import me.shwetagoyal.sliidesampleapp.data.UserResponse
import me.shwetagoyal.sliidesampleapp.domain.util.DataError
import me.shwetagoyal.sliidesampleapp.domain.util.Result

class UserRemoteDataSourceImpl(
    private val client: HttpClient
): UserRemoteDataSource {
    override suspend fun getUsersLastPage(): Result<List<UserResponse>, DataError.Network> {
        return when (val pageResult = client.getLastPageNumberFromHeader("users")) {
            is Result.Success -> client.get("users", mapOf("page" to pageResult.data))
            is Result.Error -> pageResult
        }
    }

    override suspend fun addUser(name: String, email: String): Result<UserResponse, DataError.Network> {
        return client.post(
            route = "users",
            body = CreateUser(
                name = name,
                email = email,
                gender = "male",
                status = "active"
            )
        )
    }

    override suspend fun deleteUser(userId: Int): Result<Unit, DataError.Network> {
        return client.delete("users/$userId")
    }

}