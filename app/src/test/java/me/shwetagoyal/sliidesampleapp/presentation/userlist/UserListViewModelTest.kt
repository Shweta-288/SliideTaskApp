package me.shwetagoyal.sliidesampleapp.presentation.userlist

import io.mockk.*
import kotlinx.coroutines.test.*
import me.shwetagoyal.sliidesampleapp.domain.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals

class UserListViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private val userRepository: UserRepository = mockk(relaxed = true)
  private lateinit var viewModel: UserListViewModel

    @Test
    fun exampleTest() = runTest {
        val mock = mockk<MutableList<String>>()
        every { mock.add("hello") } returns true

        val result = mock.add("hello")

        assertEquals(true, result)
        verify { mock.add("hello") }
    }

//@OptIn(ExperimentalCoroutinesApi::class)
//@BeforeEach
// fun setUp() {
// Dispatchers.setMain(testDispatcher)
// viewModel = UserListViewModel(userRepository, testDispatcher)
// }
//
//@OptIn(ExperimentalCoroutinesApi::class)
//@AfterEach
// fun tearDown() {
// Dispatchers.resetMain()
// }

//  @Test
//  fun `loadUsers should update state with user list on success`(): Unit = runTest {
//   val users = listOf(UserResponse(1, "John", "john@example.com", "male", "active"))
//   coEvery { userRepository.getUsersLastPage() } returns Result.Success(users)
//
//   viewModel.onAction(UserAction.OnScreenLoad)
//   advanceUntilIdle()
//
//   assertTrue(viewModel.state.users.isNotEmpty())
//   assertFalse(viewModel.state.isLoading)
//  }
//
//  @Test
//  fun `createUser should emit UserCreationSuccess on success`(): Unit = runTest {
//   val user = UserResponse(1, "John", "john@example.com", "male", "active")
//   coEvery { userRepository.addUser(any(), any()) } returns Result.Success(user)
//   coEvery { userRepository.getUsersLastPage() } returns Result.Success(listOf(user))
//
//   viewModel.onAction(UserAction.OnConfirmCreateUser("John", "john@example.com"))
//   advanceUntilIdle()
//
//   val event = viewModel.event.replayCache.last()
//   assertTrue(event is UserEvent.UserCreationSuccess)
//  }
//
//  @Test
//  fun `deleteUser should emit UserDeleted on success`(): Unit = runTest {
//   coEvery { userRepository.deleteUser(any()) } returns Result.Success(Unit)
//   coEvery { userRepository.getUsersLastPage() } returns Result.Success(emptyList())
//
//   viewModel.onAction(UserAction.OnDeleteUserClick(123))
//   viewModel.onAction(UserAction.OnConfirmDeleteUser)
//   advanceUntilIdle()
//
//   val event = viewModel.event.replayCache.last()
//   assertTrue(event is UserEvent.UserDeleted)
//  }
}
