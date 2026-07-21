package com.example.somos_sumapaz_agro.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NaranjaArena,
    secondary = AmarilloCrema,
    tertiary = VerdeOlivaMedio,
    background = VerdePrincipal,
    surface = VerdeOlivaMedio,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = NaranjaArena,             // Botones principales en naranja arena crema
    secondary = AmarilloCrema,
    tertiary = VerdeOlivaMedio,         // Headers en verde olivo
    background = Color.White,           // Fondo blanco
    surface = Color.White,              // Superficies/Tarjetas blancas
    onPrimary = Color.Black,            // Letra negra en los botones
    onSecondary = Color.Black,
    onBackground = Color(0xFF1E211A),   // Letra de contenido negra
    onSurface = Color(0xFF1E211A)       // Letra de superficies negra
)

@Composable
fun Somos_sumapaz_agroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}