package com.muna.pamtkasir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.muna.pamtkasir.ui.login.LoginScreen
import com.muna.pamtkasir.ui.register.RegisterScreen
import com.muna.pamtkasir.ui.theme.PAMTKASIRTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContent {

            PAMTKASIRTheme {

                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {

                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = { role ->
                                when (role) {
                                    "admin" -> navController.navigate("admin_dashboard")
                                    "cashier" -> navController.navigate("cashier_dashboard")
                                }
                            },
                            onGoToRegister = {
                                navController.navigate("register")
                            }
                        )
                    }

                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = {
                                navController.navigate("login")
                            },
                            onGoToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // Placeholder dashboard kasir
                    composable("cashier_dashboard") {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Selamat datang, Kasir! 🎉", fontSize = 20.sp)
                        }
                    }

                    // Placeholder dashboard admin
                    composable("admin_dashboard") {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Selamat datang, Admin! 🎉", fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}