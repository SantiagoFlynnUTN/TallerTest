package com.example.myapplication.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.repository.LoginRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val repository: LoginRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState

    fun processIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.UpdateUsername -> updateUsername(intent.username)
            is LoginIntent.UpdatePassword -> updatePassword(intent.password)
            is LoginIntent.Login -> login()
            is LoginIntent.DismissError -> clearError()
        }
    }

    internal fun login() {
        viewModelScope.launch(ioDispatcher) {
            updateLoadingState(true)
            val response =
                repository.loginWithCredentials(_loginState.value.username, _loginState.value.password)
            updateLoadingState(false)
            updateIsValidated(response)
        }
    }

    internal fun updateUsername(username: String) {
        updateState { copy(username = username, errorMessage = null) }
    }

    internal fun updatePassword(password: String) {
        updateState { copy(password = password, errorMessage = null) }
    }

    internal fun clearError() {
        updateState { copy(errorMessage = null, showErrorDialog = false) }
    }

    internal fun updateLoadingState(loadingState: Boolean) {
        updateState { copy(isLoading = loadingState) }
    }

    internal fun updateIsValidated(validated: Boolean) {
        updateState { copy(isValidated = validated) }
        if (!validated) {
            updateState {
                copy(
                    errorMessage = "Invalid username or password",
                    showErrorDialog = true
                )
            }
        }
    }

    fun updateState(transform: LoginState.() -> LoginState) {
        _loginState.update { currentState ->
            transform(currentState)
        }
    }
}

data class LoginState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isValidated: Boolean = false,
    val errorMessage: String? = null,
    val showErrorDialog: Boolean = false
) {
    fun isContinueButtonEnabled(): Boolean {
        return username.isNotEmpty() && password.isNotEmpty()
    }
}

sealed class LoginIntent {
    data class UpdateUsername(val username: String) : LoginIntent()
    data class UpdatePassword(val password: String) : LoginIntent()
    object Login : LoginIntent()
    object DismissError : LoginIntent()
}