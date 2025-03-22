package com.example.myapplication.feature.login

import com.example.myapplication.repository.LoginRepository
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var viewModel: LoginViewModel
    private lateinit var repository: LoginRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk()
        viewModel = LoginViewModel(repository, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // -- State transform tests --

    @Test
    fun `updateUsername should update username in state and clear error`() =
        runTest(testDispatcher) {
        // Given
        val username = "testUser"

        // When
        viewModel.updateUsername(username)

        // Then
        val state = viewModel.loginState.first()
        assertEquals(username, state.username)
        assertNull(state.errorMessage)
    }

    @Test
    fun `updatePassword should update password in state and clear error`() =
        runTest(testDispatcher) {
        // Given
        val password = "testPassword"

        // When
        viewModel.updatePassword(password)

        // Then
        val state = viewModel.loginState.first()
        assertEquals(password, state.password)
        assertNull(state.errorMessage)
    }

    @Test
    fun `clearError should reset error message and dialog flag`() = runTest(testDispatcher) {
        // Given
        viewModel.updateState {
            copy(errorMessage = "Some error", showErrorDialog = true)
        }

        // When
        viewModel.clearError()

        // Then
        val state = viewModel.loginState.first()
        assertNull(state.errorMessage)
        assertFalse(state.showErrorDialog)
    }

    @Test
    fun `updateLoadingState should update loading state`() = runTest(testDispatcher) {
        // Given
        val loadingState = true

        // When
        viewModel.updateLoadingState(loadingState)

        // Then
        val state = viewModel.loginState.first()
        assertTrue(state.isLoading)
    }

    @Test
    fun `updateIsValidated with true should update isValidated flag`() = runTest(testDispatcher) {
        // Given
        val validated = true

        // When
        viewModel.updateIsValidated(validated)

        // Then
        val state = viewModel.loginState.first()
        assertTrue(state.isValidated)
        assertNull(state.errorMessage)
        assertFalse(state.showErrorDialog)
    }

    @Test
    fun `updateIsValidated with false should update isValidated flag and show error`() =
        runTest(testDispatcher) {
            // Given
            val validated = false

            // When
            viewModel.updateIsValidated(validated)

            // Then
            val state = viewModel.loginState.first()
            assertFalse(state.isValidated)
            assertEquals("Invalid username or password", state.errorMessage)
            assertTrue(state.showErrorDialog)
        }

    @Test
    fun `login should update state based on repository response`() = runTest(testDispatcher) {
        // Given
        val username = "user"
        val password = "password"
        viewModel.updateUsername(username)
        viewModel.updatePassword(password)
        coEvery { repository.loginWithCredentials(username, password) } returns true

        // When
        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.loginState.first()
        assertFalse(state.isLoading)
        assertTrue(state.isValidated)
        assertNull(state.errorMessage)
        assertFalse(state.showErrorDialog)
    }

    @Test
    fun `login should show error when credentials are invalid`() = runTest(testDispatcher) {
        // Given
        val username = "wrongUser"
        val password = "wrongPassword"
        viewModel.updateUsername(username)
        viewModel.updatePassword(password)
        coEvery { repository.loginWithCredentials(username, password) } returns false

        // When
        viewModel.login()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.loginState.first()
        assertFalse(state.isLoading)
        assertFalse(state.isValidated)
        assertEquals("Invalid username or password", state.errorMessage)
        assertTrue(state.showErrorDialog)
    }


    // -- Intent tests --

    @Test
    fun `processIntent Login should call login`() = runTest(testDispatcher) {
        // Given
        val username = "user"
        val password = "password"
        viewModel.updateUsername(username)
        viewModel.updatePassword(password)
        coEvery { repository.loginWithCredentials(username, password) } returns true

        // When
        viewModel.processIntent(LoginIntent.Login)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.loginState.first()
        assertFalse(state.isLoading)
        assertTrue(state.isValidated)
    }

    @Test
    fun `processIntent UpdateUsername should update username`() = runTest(testDispatcher) {
        // Given
        val username = "newUsername"

        // When
        viewModel.processIntent(LoginIntent.UpdateUsername(username))

        // Then
        val state = viewModel.loginState.first()
        assertEquals(username, state.username)
    }

    @Test
    fun `processIntent UpdatePassword should update password`() = runTest(testDispatcher) {
        // Given
        val password = "newPassword"

        // When
        viewModel.processIntent(LoginIntent.UpdatePassword(password))

        // Then
        val state = viewModel.loginState.first()
        assertEquals(password, state.password)
    }

    @Test
    fun `processIntent DismissError should clear error`() = runTest(testDispatcher) {
        // Given
        viewModel.updateState {
            copy(errorMessage = "Some error", showErrorDialog = true)
        }

        // When
        viewModel.processIntent(LoginIntent.DismissError)

        // Then
        val state = viewModel.loginState.first()
        assertNull(state.errorMessage)
        assertFalse(state.showErrorDialog)
    }


    // -- State class derived methods tests --

    @Test
    fun `isContinueButtonEnabled returns true when username and password are not empty`() =
        runTest(testDispatcher) {
            // Given
            viewModel.updateUsername("user")
            viewModel.updatePassword("pass")

            // When
            val state = viewModel.loginState.first()

            // Then
            assertTrue(state.isContinueButtonEnabled())
        }

    @Test
    fun `isContinueButtonEnabled returns false when password is empty`() = runTest(testDispatcher) {
        // Given
        viewModel.updateUsername("user")
        viewModel.updatePassword("")

        // When
        val state = viewModel.loginState.first()

        // Then
        assertFalse(state.isContinueButtonEnabled())
    }
}