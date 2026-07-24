package com.example.somos_sumapaz_agro.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.somos_sumapaz_agro.Screen

@Composable
fun IndexScreen(onNavigate: (Screen) -> Unit) {
    var showSurveyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Logo / Identidad
        Text(
            text = "Somos Sumapaz",
            style = MaterialTheme.typography.headlineLarge,
            color = com.example.somos_sumapaz_agro.ui.theme.VerdePrincipal,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Agro Encuestas",
            style = MaterialTheme.typography.titleLarge,
            color = com.example.somos_sumapaz_agro.ui.theme.VerdeOlivaMedio,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
        )

        // Botón/Card 1: Encuestas
        IndexCard(
            title = "Encuestas",
            subtitle = "Registrar nueva visita técnica pecuaria o agrícola en campo.",
            icon = Icons.Default.Star,
            color = com.example.somos_sumapaz_agro.ui.theme.AmarilloCremaOscuro,
            textColor = Color.Black,
            onClick = { showSurveyDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón/Card 2: Historial y Reportes
        IndexCard(
            title = "Historial y Reportes",
            subtitle = "Ver visitas cargadas, descargar actas en PDF y exportar consolidados a Excel.",
            icon = Icons.Default.List,
            color = com.example.somos_sumapaz_agro.ui.theme.AmarilloCremaOscuro,
            textColor = Color.Black,
            onClick = { onNavigate(Screen.Historial) }
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "versión 1.0",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }

    // Diálogo de Selección de Encuesta
    if (showSurveyDialog) {
        AlertDialog(
            onDismissRequest = { showSurveyDialog = false },
            title = {
                Text(
                    text = "¿Qué encuesta desea realizar?",
                    style = MaterialTheme.typography.titleLarge,
                    color = com.example.somos_sumapaz_agro.ui.theme.VerdeOlivaMedio,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            showSurveyDialog = false
                            onNavigate(Screen.Pecuaria)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.example.somos_sumapaz_agro.ui.theme.AmarilloCremaOscuro,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Visita Pecuaria", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showSurveyDialog = false
                            onNavigate(Screen.Agricola)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = com.example.somos_sumapaz_agro.ui.theme.AmarilloCremaOscuro,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Visita Agrícola", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showSurveyDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun IndexCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    textColor: androidx.compose.ui.graphics.Color = contentColorFor(color)
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    tint = textColor
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.8f),
                    maxLines = 2
                )
            }
        }
    }
}
