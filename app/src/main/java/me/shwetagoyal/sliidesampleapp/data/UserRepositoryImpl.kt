package me.shwetagoyal.sliidesampleapp.data

import me.shwetagoyal.sliidesampleapp.domain.UserRepository
import me.shwetagoyal.sliidesampleapp.domain.util.DataError
import me.shwetagoyal.sliidesampleapp.domain.util.Result
import me.shwetagoyal.sliidesampleapp.network.UserRemoteDataSource
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val remoteDataSource: UserRemoteDataSource
): UserRepository {
    override suspend fun getUsersLastPage(): Result<List<UserResponse>, DataError.Network> =
        remoteDataSource.getUsersLastPage()

    override suspend fun addUser(name: String, email: String): Result<UserResponse, DataError.Network> =
        remoteDataSource.addUser(name, email)

    override suspend fun deleteUser(userId: Int): Result<Unit, DataError.Network> =
        remoteDataSource.deleteUser(userId)
}