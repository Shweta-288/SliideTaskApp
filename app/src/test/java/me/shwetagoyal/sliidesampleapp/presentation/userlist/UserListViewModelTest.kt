package me.shwetagoyal.sliidesampleapp.presentation.userlist

import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import me.shwetagoyal.sliidesampleapp.data.UserResponse
import me.shwetagoyal.sliidesampleapp.domain.*
import me.shwetagoyal.sliidesampleapp.domain.util.DataError
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import me.shwetagoyal.sliidesampleapp.domain.util.Result
import org.junit.jupiter.api.Assertions.assertEquals

class UserListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val userRepository: UserRepository = mockk(relaxed = true)
    private lateinit var viewModel: UserListViewModel


    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = UserListViewModel(userRepository, testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadUsers should update state with user list on success`() = runTest {
        val users = listOf(UserResponse(1, "John", "john@example.com", "male", "active"))
        coEvery { userRepository.getUsersLastPage() } returns Result.Success(users)

        advanceUntilIdle()

        val uiState = viewModel.state.value
        assertTrue(uiState.users.isNotEmpty())
        assertFalse(uiState.isLoading)
        assertTrue(uiState.hasLoadedUsers)
        assertFalse(uiState.isRefreshing)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `loadUsers should emit UserLoadFailed event on error and eventually complete`() = runTest {
        val error = DataError.Network.NO_INTERNET
        var callCount = 0
        coEvery { userRepository.getUsersLastPage() } coAnswers {
            callCount++
            if (callCount <= 3) Result.Error(error) else Result.Success(emptyList())
        }
        val events = mutableListOf<UserEvent>()
        val job = launch {
            viewModel.event.collect { events.add(it) }
        }

        advanceTimeBy(3_000L * 3 + 100L) // Simulate 3 retries
        job.cancel()
        coVerify(exactly = 4) { userRepository.getUsersLastPage() }
        assertTrue(events.any { it is UserEvent.UserLoadFailed })
        assertFalse(viewModel.state.value.isLoading)
        assertFalse(viewModel.state.value.isRefreshing)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `createUser should emit UserCreationSuccess on success`() = runTest {
        val user = UserResponse(1, "John", "john@example.com", "male", "active")
        coEvery { userRepository.addUser(any(), any()) } returns Result.Success(user)
        coEvery { userRepository.getUsersLastPage() } returns Result.Success(listOf(user))

        val events = mutableListOf<UserEvent>()
        val job = launch { viewModel.event.collect { events.add(it) } }

        viewModel.onAction(UserAction.OnConfirmCreateUser("John", "john@example.com"))
        advanceUntilIdle()

        job.cancel()

        assertTrue(events.any() { it is UserEvent.UserCreationSuccess })
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `createUser should emit Error event with email exists message on conflict`() = runTest {
        coEvery { userRepository.getUsersLastPage() } returns Result.Success(emptyList())
        coEvery { userRepository.addUser(any(), any()) } returns Result.Error(DataError.Network.CONFLICT)

        val events = mutableListOf<UserEvent>()
        val job = launch { viewModel.event.collect { events.add(it) } }
        viewModel.onAction(UserAction.OnConfirmCreateUser("John", "john@example.com"))

        advanceUntilIdle()

        job.cancel()
        assertTrue(events.any() { it is UserEvent.Error })
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.hasLoadedUsers)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `deleteUser should emit UserDeleted on success`() = runTest {
        coEvery { userRepository.deleteUser(any()) } returns Result.Success(Unit)
        coEvery { userRepository.getUsersLastPage() } returns Result.Success(emptyList())
        val events = mutableListOf<UserEvent>()
        val job = launch { viewModel.event.collect { events.add(it) } }

        viewModel.onAction(UserAction.OnDeleteUserClick(123))
        viewModel.onAction(UserAction.OnConfirmDeleteUser)
        advanceUntilIdle()

        job.cancel()
        assertTrue(events.any() { it is UserEvent.UserDeleted })
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `deleteUser should emit Error event on generic error`() = runTest {
        coEvery { userRepository.getUsersLastPage() } returns Result.Success(emptyList())
        coEvery { userRepository.deleteUser(any()) } returns Result.Error(DataError.Network.SERVER_ERROR)


        val events = mutableListOf<UserEvent>()
        val job = launch { viewModel.event.collect { events.add(it) } }

        viewModel.onAction(UserAction.OnDeleteUserClick(123))
        viewModel.onAction(UserAction.OnConfirmDeleteUser)

        advanceUntilIdle()

        job.cancel()
        assertTrue(events.any() { it is UserEvent.Error })
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.hasLoadedUsers)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `OnRefresh toggles isRefreshing and repopulates`() = runTest {
        coEvery { userRepository.getUsersLastPage() } returns Result.Success(emptyList())
        advanceUntilIdle()
        clearMocks(userRepository, recordedCalls = true)

        val users = listOf(UserResponse(1, "Jane", "jane@example.com", "female", "active"))
        coEvery { userRepository.getUsersLastPage() } returns Result.Success(users)

        viewModel.onAction(UserAction.OnRefreshAction(isUserGesture = true))

        advanceUntilIdle()

        assertFalse(viewModel.state.value.isRefreshing)
        assertTrue(viewModel.state.value.hasLoadedUsers)
        assertEquals(users.size, viewModel.state.value.users.size)

        coVerify(exactly = 1) { userRepository.getUsersLastPage() }
    }
}