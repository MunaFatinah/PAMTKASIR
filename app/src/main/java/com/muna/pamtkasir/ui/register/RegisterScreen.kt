package com.muna.pamtkasir.ui.register

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muna.pamtkasir.auth.AuthState
import com.muna.pamtkasir.auth.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onGoToLogin: () -> Unit
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

        Text(text = "Register")

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
                viewModel.register(email, password)
            }
        ) {
            Text("Register")
        }

        TextButton(onClick = onGoToLogin) {
            Text("Sudah punya akun? Login")
        }

        when (state) {

            is AuthState.RegisterSuccess -> {
                LaunchedEffect(Unit) {
                    onRegisterSuccess()
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