package com.muna.pamtkasir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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

                                    "admin" -> {
                                        navController.navigate(
                                            "admin_dashboard"
                                        )
                                    }

                                    "cashier" -> {
                                        navController.navigate(
                                            "cashier_dashboard"
                                        )
                                    }
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
                }
            }
        }
    }
}