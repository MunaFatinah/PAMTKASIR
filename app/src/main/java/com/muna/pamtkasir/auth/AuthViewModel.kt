package com.muna.pamtkasir.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class LoginSuccess(val role: String) : AuthState()
    object RegisterSuccess : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _authState =
        MutableStateFlow<AuthState>(AuthState.Idle)

    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {

        viewModelScope.launch {

            _authState.value = AuthState.Loading

            try {

                val role = repository.login(email, password)

                _authState.value =
                    AuthState.LoginSuccess(role)

            } catch (e: Exception) {

                _authState.value =
                    AuthState.Error(
                        e.message ?: "Login gagal"
                    )
            }
        }
    }

    fun register(email: String, password: String) {

        viewModelScope.launch {

            _authState.value = AuthState.Loading

            try {

                repository.register(email, password)

                _authState.value =
                    AuthState.RegisterSuccess

            } catch (e: Exception) {

                _authState.value =
                    AuthState.Error(
                        e.message ?: "Register gagal"
                    )
            }
        }
    }
}