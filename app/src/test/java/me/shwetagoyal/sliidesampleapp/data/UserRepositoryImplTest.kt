package me.shwetagoyal.sliidesampleapp.data

import io.ktor.client.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import me.shwetagoyal.sliidesampleapp.domain.*
import me.shwetagoyal.sliidesampleapp.domain.util.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test

//class UserRepositoryImplTest {
//  private val httpClient: HttpClient = mockk()
//  private lateinit var repository: UserRepository
//
//@BeforeEach
// fun setUp() {}
//
//
//  @Test
//  fun `addUser should return success when client returns success`(): Unit = runTest {
//   val expected = UserResponse(1, "John", "john@example.com", "male", "active")
//   coEvery { httpClient.post<UserResponse>(any(), any()) } returns Result.Success(expected)
//
//   val result = repository.addUser("John", "john@example.com")
//
//   assertTrue(result is Result.Success)
//   assertEquals(expected, (result as Result.Success).data)
//  }
//}