package com.muna.pamtkasir.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary      = Color(0xFF1A6651),
    onPrimary    = Color.White,
    background   = Color(0xFF66B499),
    onBackground = Color.Black,
    surface      = Color(0xFFF6F6F6),
    onSurface    = Color.Black,
)

@Composable
fun PAMTKASIRTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = AppTypography,
        content     = content
    )
}