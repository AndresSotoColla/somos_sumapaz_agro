package com.example.somos_sumapaz_agro.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

        // Botón/Card 1: Visita Pecuaria
        IndexCard(
            title = "Visita Pecuaria",
            subtitle = "Registrar visitas de acompañamiento para ganado, porcinos, aves, etc.",
            icon = Icons.Default.Home,
            color = com.example.somos_sumapaz_agro.ui.theme.VerdeOlivaMedio,
            textColor = Color.White,
            onClick = { onNavigate(Screen.Pecuaria) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón/Card 2: Visita Agrícola
        IndexCard(
            title = "Visita Agrícola",
            subtitle = "Registrar visitas para cultivos de hortalizas, tubérculos, frutales e insumos.",
            icon = Icons.Default.Star,
            color = com.example.somos_sumapaz_agro.ui.theme.NaranjaArena,
            textColor = Color.Black,
            onClick = { onNavigate(Screen.Agricola) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Botón/Card 3: Historial y Reportes
        IndexCard(
            title = "Historial y Reportes",
            subtitle = "Ver visitas cargadas, descargar actas en PDF y exportar consolidados a Excel.",
            icon = Icons.Default.List,
            color = com.example.somos_sumapaz_agro.ui.theme.AmarilloCremaOscuro,
            textColor = Color.Black,
            onClick = { onNavigate(Screen.Historial) }
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
