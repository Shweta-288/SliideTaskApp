package me.shwetagoyal.sliidesampleapp.data

import io.ktor.client.HttpClient
import me.shwetagoyal.sliidesampleapp.domain.UserRepository
import me.shwetagoyal.sliidesampleapp.domain.util.DataError
import me.shwetagoyal.sliidesampleapp.domain.util.Result
import me.shwetagoyal.sliidesampleapp.network.get
import me.shwetagoyal.sliidesampleapp.network.post
import me.shwetagoyal.sliidesampleapp.network.delete
import me.shwetagoyal.sliidesampleapp.network.getLastPageNumberFromHeader
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val client: HttpClient
): UserRepository {
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
        return client.delete<Unit>("users/$userId")
    }
}