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

private val LightColorScheme = lightColorScheme(
    primary = NaranjaArena,             // Botones principales en naranja arena crema
    secondary = AmarilloCrema,          // Botones auxiliares en amarillo crema
    tertiary = VerdeOlivaMedio,         // Headers en verde olivo
    background = Color.White,           // Fondo blanco
    surface = Color.White,              // Superficies/Tarjetas blancas
    surfaceVariant = Color.White,       // Forzar blanco en variantes de tarjetas/campos
    onPrimary = Color.Black,            // Letra negra en los botones principales
    onSecondary = Color.Black,          // Letra negra en los botones auxiliares
    onBackground = Color(0xFF1E211A),   // Letra de contenido negra
    onSurface = Color(0xFF1E211A)       // Letra de superficies negra
)

@Composable
fun Somos_sumapaz_agroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Forzamos a que la aplicación siempre use la paleta clara (fondo blanco)
    // para evitar que el Modo Oscuro del celular altere los colores institucionales.
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}