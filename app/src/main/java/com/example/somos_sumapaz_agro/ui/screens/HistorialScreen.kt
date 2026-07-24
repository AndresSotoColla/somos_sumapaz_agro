package com.example.somos_sumapaz_agro.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.somos_sumapaz_agro.db.VisitasDbHelper
import com.example.somos_sumapaz_agro.model.VisitaAgricola
import com.example.somos_sumapaz_agro.model.VisitaPecuaria
import com.example.somos_sumapaz_agro.util.CsvExporter
import com.example.somos_sumapaz_agro.util.NetworkUtils
import com.example.somos_sumapaz_agro.util.PdfGenerator
import com.example.somos_sumapaz_agro.util.SyncManager
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun HistorialScreen(dbHelper: VisitasDbHelper) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isSyncing by remember { mutableStateOf(false) }

    // Cargar listas de visitas
    var visitasPecuarias by remember { mutableStateOf(emptyList<VisitaPecuaria>()) }
    var visitasAgricolas by remember { mutableStateOf(emptyList<VisitaAgricola>()) }
    
    // Función para refrescar datos
    val refreshData = {
        visitasPecuarias = dbHelper.getAllVisitasPecuarias()
        visitasAgricolas = dbHelper.getAllVisitasAgricolas()
    }

    val triggerSync = {
        if (!isSyncing) {
            isSyncing = true
            coroutineScope.launch {
                SyncManager.syncAllPending(context, dbHelper) { count ->
                    isSyncing = false
                    refreshData()
                    if (count > 0) {
                        Toast.makeText(context, "$count visita(s) sincronizada(s) exitosamente", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
        triggerSync()
    }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Pecuaria, 1 = Agrícola

    // Launchers de guardado de CSV
    val pecuariaCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                val csvContent = CsvExporter.generatePecuariaCsv(visitasPecuarias)
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(csvContent.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Excel Pecuario guardado con éxito", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    val agricolaCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                val csvContent = CsvExporter.generateAgricolaCsv(visitasAgricolas)
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(csvContent.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Excel Agrícola guardado con éxito", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Historial de Visitas",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
            Button(
                onClick = { triggerSync() },
                enabled = !isSyncing,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(if (isSyncing) "Sincronizando..." else "Sincronizar", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Botones de Exportar a Excel (CSV)
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    if (visitasPecuarias.isEmpty()) {
                        Toast.makeText(context, "No hay visitas pecuarias para exportar", Toast.LENGTH_SHORT).show()
                    } else {
                        pecuariaCsvLauncher.launch("visitas_pecuarias_${System.currentTimeMillis() / 1000}.csv")
                    }
                },
                modifier = Modifier.weight(1f).padding(end = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Excel Pecuaria", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    if (visitasAgricolas.isEmpty()) {
                        Toast.makeText(context, "No hay visitas agrícolas para exportar", Toast.LENGTH_SHORT).show()
                    } else {
                        agricolaCsvLauncher.launch("visitas_agricolas_${System.currentTimeMillis() / 1000}.csv")
                    }
                },
                modifier = Modifier.weight(1f).padding(start = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Excel Agrícola", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }

        // Tabs de visualización
        TabRow(selectedTabIndex = selectedTab, modifier = Modifier.fillMaxWidth()) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Pecuaria (${visitasPecuarias.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Agrícola (${visitasAgricolas.size})") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            // Lista Pecuaria
            if (visitasPecuarias.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se han registrado visitas pecuarias.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(visitasPecuarias) { visita ->
                        VisitaPecuariaCard(
                            visita = visita,
                            onOpenPdf = { openPecuariaPdf(context, visita) },
                            onDelete = {
                                dbHelper.deleteVisitaPecuaria(visita.id)
                                Toast.makeText(context, "Registro eliminado", Toast.LENGTH_SHORT).show()
                                refreshData()
                            }
                        )
                    }
                }
            }
        } else {
            // Lista Agrícola
            if (visitasAgricolas.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se han registrado visitas agrícolas.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(visitasAgricolas) { visita ->
                        VisitaAgricolaCard(
                            visita = visita,
                            onOpenPdf = { openAgricolaPdf(context, visita) },
                            onDelete = {
                                dbHelper.deleteVisitaAgricola(visita.id)
                                Toast.makeText(context, "Registro eliminado", Toast.LENGTH_SHORT).show()
                                refreshData()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VisitaPecuariaCard(
    visita: VisitaPecuaria,
    onOpenPdf: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(visita.usuario, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(visita.fecha, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (visita.synced) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (visita.synced) "Sincronizado ✓" else "Pendiente ☁",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Finca: ${visita.finca} | Vereda: ${visita.vereda}", style = MaterialTheme.typography.bodyMedium)
            Text("Corregimiento: ${visita.corregimiento} | Cuenca: ${visita.cuenca}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(6.dp))
            Text("Especies: ${visita.especies.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onOpenPdf,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Ver Acta", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun VisitaAgricolaCard(
    visita: VisitaAgricola,
    onOpenPdf: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(visita.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(visita.fecha, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (visita.synced) Color(0xFF4CAF50) else Color(0xFFFF9800),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (visita.synced) "Sincronizado ✓" else "Pendiente ☁",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Finca: ${visita.finca} | Vereda: ${visita.vereda}", style = MaterialTheme.typography.bodyMedium)
            Text("Reg: ${visita.numero_registro} | Tel: ${visita.telefono}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(6.dp))
            Text("Cultivos: ${visita.cultivos.size} especies | Insumos: ${visita.materiales.size} tipos", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onOpenPdf,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Ver Acta", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

private fun openPecuariaPdf(context: Context, visita: VisitaPecuaria) {
    try {
        val pdfFile = PdfGenerator.generateVisitaPecuariaPdf(context, visita)
        sharePdfFile(context, pdfFile)
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

private fun openAgricolaPdf(context: Context, visita: VisitaAgricola) {
    try {
        val pdfFile = PdfGenerator.generateVisitaAgricolaPdf(context, visita)
        sharePdfFile(context, pdfFile)
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

private fun sharePdfFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "com.example.somos_sumapaz_agro.fileprovider",
        file
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Abrir Acta de Visita"))
    } catch (e: Exception) {
        Toast.makeText(context, "No hay una aplicación para abrir PDF instalada.", Toast.LENGTH_LONG).show()
    }
}
