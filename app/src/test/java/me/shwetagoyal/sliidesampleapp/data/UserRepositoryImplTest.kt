package me.shwetagoyal.sliidesampleapp.data

import io.mockk.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import me.shwetagoyal.sliidesampleapp.domain.util.DataError
import me.shwetagoyal.sliidesampleapp.domain.util.Result
import me.shwetagoyal.sliidesampleapp.network.UserRemoteDataSource
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.IOException

class UserRepositoryImplTest {

    private val remoteDataSource: UserRemoteDataSource = mockk()
    private lateinit var repository: UserRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = UserRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getUsersLastPage returns users on success`() = runTest {
        val users = listOf(UserResponse(1, "John", "john@example.com", "male", "active"))
        coEvery { remoteDataSource.getUsersLastPage() } returns Result.Success(users)

        val result = repository.getUsersLastPage()
        assertTrue(result is Result.Success)
        assertEquals(users, (result as Result.Success).data)
    }

    @Test
    fun `getUsersLastPage returns error if getLastPageNumberFromHeader fails`() = runTest {
        val error = DataError.Network.NO_INTERNET
        coEvery { remoteDataSource.getUsersLastPage() } returns Result.Error(error)

        val result = repository.getUsersLastPage()

        assertTrue(result is Result.Error)
        assertEquals(error, (result as Result.Error).error)
    }

    @Test
    fun `getUsersLastPage returns empty list when no users`() = runTest {
        coEvery { remoteDataSource.getUsersLastPage() } returns Result.Success(emptyList())

        val result = repository.getUsersLastPage()

        assertTrue(result is Result.Success)
        assertEquals(emptyList<UserResponse>(), (result as Result.Success).data)
    }

    @Test
    fun `getUsersLastPage delegates to remoteDataSource once`() = runTest {
        coEvery { remoteDataSource.getUsersLastPage() } returns Result.Success(emptyList())

        repository.getUsersLastPage()

        coVerify(exactly = 1) { remoteDataSource.getUsersLastPage() }
    }

    @Test
    fun `getUsersLastPage propagates exception`() = runTest {
        coEvery { remoteDataSource.getUsersLastPage() } throws IOException("Network down")

        assertThrows(IOException::class.java) {
            runBlocking { repository.getUsersLastPage() }
        }
    }

    @Test
    fun `addUser returns user on success`() = runTest {
        val user = UserResponse(1, "John", "john@example.com", "male", "active")
        coEvery { remoteDataSource.addUser("John","john@example.com") } returns Result.Success(user)

        val result = repository.addUser("John", "john@example.com")

        assertTrue(result is Result.Success)
        assertEquals(user, (result as Result.Success).data)
    }

    @Test
    fun `addUser returns error on failure`() = runTest {
        val error = DataError.Network.SERVER_ERROR
        coEvery { remoteDataSource.addUser("John","john@example.com") } returns Result.Error(error)

        val result = repository.addUser("John", "john@example.com")

        assertTrue(result is Result.Error)
        assertEquals(error, (result as Result.Error).error)
    }

    @Test
    fun `addUser delegates to remoteDataSource with correct args`() = runTest {
        coEvery { remoteDataSource.addUser(any(), any()) } returns Result.Success(
            UserResponse(7, "Carol", "carol@example.com", "female", "active")
        )

        repository.addUser("Carol", "carol@example.com")

        coVerify(exactly = 1) { remoteDataSource.addUser("Carol", "carol@example.com") }
    }

    @Test
    fun `addUser propagates exception`() = runTest {
        coEvery { remoteDataSource.addUser(any(), any()) } throws IOException("API bug")

        assertThrows(IOException::class.java) {
            runBlocking { repository.addUser("Dave", "dave@example.com") }
        }
    }

    @Test
    fun `deleteUser returns success`() = runTest {
        coEvery { remoteDataSource.deleteUser(1) } returns Result.Success(Unit)

        val result = repository.deleteUser(1)

        assertTrue(result is Result.Success)
    }

    @Test
    fun `deleteUser returns error on failure`() = runTest {
        val error = DataError.Network.REQUEST_TIMEOUT
        coEvery { remoteDataSource.deleteUser(1) } returns Result.Error(error)

        val result = repository.deleteUser(1)

        assertTrue(result is Result.Error)
        assertEquals(error, (result as Result.Error).error)
    }

    @Test
    fun `deleteUser delegates to remoteDataSource with correct id`() = runTest {
        coEvery { remoteDataSource.deleteUser(5) } returns Result.Success(Unit)

        repository.deleteUser(5)

        coVerify(exactly = 1) { remoteDataSource.deleteUser(5) }
    }

    @Test
    fun `deleteUser propagates exception`() = runTest {
        coEvery { remoteDataSource.deleteUser(any()) } throws IOException("Timeout")

        assertThrows(IOException::class.java) {
            runBlocking { repository.deleteUser(123) }
        }
    }
}