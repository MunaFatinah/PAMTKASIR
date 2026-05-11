package com.muna.pamtkasir.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muna.pamtkasir.auth.AuthState
import com.muna.pamtkasir.auth.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onGoToRegister: () -> Unit
) {

    val viewModel: AuthViewModel = viewModel()
    val state by viewModel.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(text = "Login")

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.login(email, password)
            }
        ) {
            Text("Login")
        }

        TextButton(onClick = onGoToRegister) {
            Text("Belum punya akun? Register")
        }

        when (state) {

            is AuthState.LoginSuccess -> {
                val role = (state as AuthState.LoginSuccess).role
                LaunchedEffect(Unit) {
                    onLoginSuccess(role)
                }
            }

            is AuthState.Error -> {
                Text(
                    text = (state as AuthState.Error).message
                )
            }

            else -> {}
        }
    }
}