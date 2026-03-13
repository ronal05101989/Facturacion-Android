package com.playground.facturacion.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = VerdeBosque,
    secondary = VerdeSuave,
    tertiary = Cobre,
    background = Marfil,
    surface = Marfil
)

private val DarkColors = darkColorScheme(
    primary = VerdeSuave,
    secondary = Arena,
    tertiary = Cobre
)

@Composable
fun FacturacionTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content
    )
}
