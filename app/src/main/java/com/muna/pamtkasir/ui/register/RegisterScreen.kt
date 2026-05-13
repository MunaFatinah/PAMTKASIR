package com.muna.pamtkasir.ui.register

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muna.pamtkasir.R
import com.muna.pamtkasir.auth.AuthState
import com.muna.pamtkasir.auth.AuthViewModel

private val BgGreen     = Color(0xFF66B499)
private val HeaderGreen = Color(0xFF1A6651)
private val CardBg      = Color(0xFFF6F6F6)
private val FieldBorder = Color(0xFF19242F)

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onGoToLogin: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel()
    val state by viewModel.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var localError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGreen)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(180.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 38.dp)
                .shadow(
                    elevation = 30.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color(0x40000000)
                )
                .clip(RoundedCornerShape(20.dp))
                .fillMaxWidth()
                .background(CardBg)
                .padding(bottom = 34.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        HeaderGreen,
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .padding(vertical = 27.dp)
            ) {

                Image(
                    painter = painterResource(id = R.drawable.logo_kasirku),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .padding(start = 21.dp, end = 11.dp)
                        .size(50.dp)
                        .clip(RoundedCornerShape(10.dp))
                )

                Column {

                    Text(
                        text = "REGISTER",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    Text(
                        text = "Buat akun kasir baru",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(29.dp))

            Column(
                modifier = Modifier
                    .padding(bottom = 13.dp, start = 21.dp, end = 21.dp)
                    .fillMaxWidth()
            ) {

                Text(
                    text = "Email",
                    color = Color.Black,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 7.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = {
                        Text(
                            text = "nama@toko.com",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 11.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = CardBg,
                        focusedContainerColor = CardBg,
                        unfocusedBorderColor = FieldBorder,
                        focusedBorderColor = HeaderGreen,
                        unfocusedTextColor = Color.Black,
                        focusedTextColor = Color.Black,
                        cursorColor = HeaderGreen
                    )
                )
            }

            Column(
                modifier = Modifier
                    .padding(bottom = 13.dp, start = 21.dp, end = 21.dp)
                    .fillMaxWidth()
            ) {

                Text(
                    text = "Password",
                    color = Color.Black,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 7.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = {
                        Text(
                            text = "••••••••",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    visualTransformation =
                        if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),

                    trailingIcon = {
                        IconButton(
                            onClick = {
                                passwordVisible = !passwordVisible
                            }
                        ) {
                            Icon(
                                imageVector =
                                    if (passwordVisible)
                                        Icons.Outlined.VisibilityOff
                                    else
                                        Icons.Outlined.Visibility,

                                contentDescription = null,
                                tint = FieldBorder
                            )
                        }
                    },

                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 11.sp),

                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = CardBg,
                        focusedContainerColor = CardBg,
                        unfocusedBorderColor = FieldBorder,
                        focusedBorderColor = HeaderGreen,
                        unfocusedTextColor = Color.Black,
                        focusedTextColor = Color.Black,
                        cursorColor = HeaderGreen
                    )
                )
            }

            Column(
                modifier = Modifier
                    .padding(bottom = 26.dp, start = 21.dp, end = 21.dp)
                    .fillMaxWidth()
            ) {

                Text(
                    text = "Konfirmasi Password",
                    color = Color.Black,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 7.dp)
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = {
                        Text(
                            text = "••••••••",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    },

                    singleLine = true,

                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),

                    visualTransformation =
                        if (confirmPasswordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),

                    trailingIcon = {
                        IconButton(
                            onClick = {
                                confirmPasswordVisible =
                                    !confirmPasswordVisible
                            }
                        ) {

                            Icon(
                                imageVector =
                                    if (confirmPasswordVisible)
                                        Icons.Outlined.VisibilityOff
                                    else
                                        Icons.Outlined.Visibility,

                                contentDescription = null,
                                tint = FieldBorder
                            )
                        }
                    },

                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 11.sp),

                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = CardBg,
                        focusedContainerColor = CardBg,
                        unfocusedBorderColor = FieldBorder,
                        focusedBorderColor = HeaderGreen,
                        unfocusedTextColor = Color.Black,
                        focusedTextColor = Color.Black,
                        cursorColor = HeaderGreen
                    )
                )
            }

            val errorMessage = when {
                localError.isNotEmpty() -> localError
                state is AuthState.Error ->
                    (state as AuthState.Error).message
                else -> ""
            }

            AnimatedVisibility(
                visible = errorMessage.isNotEmpty(),
                modifier = Modifier.padding(horizontal = 21.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x1AE24B4A))
                        .padding(10.dp)
                ) {

                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = Color(0xFFE24B4A),
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = errorMessage,
                        color = Color(0xFFE24B4A),
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(bottom = 26.dp, start = 21.dp, end = 21.dp)
                    .shadow(4.dp, RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .fillMaxWidth()
                    .background(HeaderGreen)
                    .clickable {

                        localError = ""

                        when {

                            email.isEmpty() ||
                                    password.isEmpty() ||
                                    confirmPassword.isEmpty() -> {

                                localError = "Semua field wajib diisi"
                            }

                            password != confirmPassword -> {

                                localError = "Password tidak cocok"
                            }

                            password.length < 6 -> {

                                localError =
                                    "Password minimal 6 karakter"
                            }

                            else -> {

                                viewModel.register(email, password)
                            }
                        }
                    }
                    .padding(vertical = 15.dp)
            ) {

                Text(
                    text = "Daftar",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            Text(
                text = "Sudah punya akun? Masuk sekarang",
                color = Color.Black,
                fontSize = 10.sp,
                modifier = Modifier.clickable {
                    onGoToLogin()
                }
            )
        }

        Text(
            text = "Kelompok 5",
            color = Color.Black,
            fontSize = 10.sp,
            modifier = Modifier.padding(
                top = 31.dp,
                bottom = 31.dp
            )
        )

        val currentState = state

        if (currentState is AuthState.RegisterSuccess) {

            LaunchedEffect(currentState) {
                onRegisterSuccess()
            }
        }
    }
}