package com.example.somos_sumapaz_agro.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.somos_sumapaz_agro.db.VisitasDbHelper
import com.example.somos_sumapaz_agro.model.*
import com.example.somos_sumapaz_agro.ui.components.DropdownSelector
import com.example.somos_sumapaz_agro.ui.components.MultiSelectDropdownSelector
import com.example.somos_sumapaz_agro.ui.components.SignaturePad
import com.example.somos_sumapaz_agro.util.LocationHelper
import com.example.somos_sumapaz_agro.util.PdfGenerator
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AgricolaForm(
    dbHelper: VisitasDbHelper,
    onNavigateToHistorial: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Form States
    val calendar = Calendar.getInstance()
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    val nowTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
    val autoRegNum = "REG-${System.currentTimeMillis() / 1000}"

    // 1. Información General
    var fecha by remember { mutableStateOf(todayStr) }
    var nombre by remember { mutableStateOf("") }
    var finca by remember { mutableStateOf("") }
    var vereda by remember { mutableStateOf("") }
    var corregimiento by remember { mutableStateOf("Nazareth") }
    var cuenca by remember { mutableStateOf("Río Sumapaz") }
    var telefono by remember { mutableStateOf("") }
    var horaInicio by remember { mutableStateOf(nowTimeStr) }
    var horaFin by remember { mutableStateOf("") }
    var numeroRegistro by remember { mutableStateOf(autoRegNum) }

    // 2. Motivo de Acompañamiento
    val motivosList = listOf(
        "Diagnóstico del sistema productivo",
        "Prestación de asistencia técnica",
        "Análisis de suelos",
        "Entrega de materiales",
        "Planta de compostaje",
        "Elaboración de biopreparados"
    )
    val selectedMotivos = remember { mutableStateListOf<String>() }

    val huertasList = listOf(
        "Huerta tradicional",
        "Huerta casera",
        "Huerta nueva",
        "Huerta fortalecida"
    )
    val selectedHuertas = remember { mutableStateListOf<String>() }
    var objetivoVisita by remember { mutableStateOf("") }

    // 3. Cultivos (Dynamic Rows)
    val cultivos = remember { mutableStateListOf<CultivoVisita>() }
    // Crop inputs
    val categoriasCultivo = listOf("Leguminosas", "Hortalizas", "Tubérculos", "Aromáticas y medicinales", "Frutales", "Otras")
    var tempCategoria by remember { mutableStateOf("Hortalizas") }
    var tempTipo by remember { mutableStateOf("") }
    var tempEspecie by remember { mutableStateOf("") }
    var tempArea by remember { mutableStateOf("") }
    var tempProduccion by remember { mutableStateOf("") }
    var tempObsCultivo by remember { mutableStateOf("") }

    // Suelos
    var muestraSuelo by remember { mutableStateOf(false) }
    var numeroMuestra by remember { mutableStateOf("") }

    // 4. Materiales Entregados (Dynamic Rows)
    val materiales = remember { mutableStateListOf<MaterialEntregado>() }
    // Material inputs
    var tempMaterial by remember { mutableStateOf("") }
    var tempCantidad by remember { mutableStateOf("") }
    var tempUnidad by remember { mutableStateOf("") }

    // 5. Georreferenciación
    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }
    var altitud by remember { mutableStateOf<Double?>(null) }
    var observacionesGeo by remember { mutableStateOf("") }
    var areaIntervenir by remember { mutableStateOf("") }

    // 6. Recomendaciones
    var recomendaciones by remember { mutableStateOf("") }

    // 7. Corresponsabilidad
    var aceptaCorresponsabilidad by remember { mutableStateOf(false) }

    // 8. Firmas
    var proximaVisita by remember { mutableStateOf("") }
    var profesional by remember { mutableStateOf("") }
    var tarjetaProfesional by remember { mutableStateOf("") }
    var cedulaOperario by remember { mutableStateOf("") }
    var cedulaUsuario by remember { mutableStateOf("") }

    // Signatures (Base64)
    var firmaProfesional by remember { mutableStateOf<String?>(null) }
    var firmaOperario by remember { mutableStateOf<String?>(null) }
    var firmaUsuario by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Asistencia Técnica Agrícola",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // ==========================================
        // SECCIÓN 1: INFORMACIÓN GENERAL
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("1. Información General", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = numeroRegistro,
                    onValueChange = { numeroRegistro = it },
                    label = { Text("Número de Registro *") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                Button(
                    onClick = { showDatePickerDialog(context) { fecha = it } },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)
                ) {
                    Text("Fecha de Visita: $fecha")
                }

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del Productor *") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono *") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = finca,
                    onValueChange = { finca = it },
                    label = { Text("Finca *") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = vereda,
                    onValueChange = { vereda = it },
                    label = { Text("Vereda *") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                // Lista desplegable para Corregimiento
                DropdownSelector(
                    label = "Corregimiento *",
                    options = listOf("Nazareth", "Betania", "San Juan"),
                    selectedOption = corregimiento,
                    onOptionSelected = { corregimiento = it },
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Lista desplegable para Cuenca
                DropdownSelector(
                    label = "Cuenca *",
                    options = listOf("Río Sumapaz", "Río Blanco"),
                    selectedOption = cuenca,
                    onOptionSelected = { cuenca = it },
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { showTimePickerDialog(context) { horaInicio = it } },
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)
                    ) {
                        Text("Inicio: $horaInicio")
                    }
                    Button(
                        onClick = { showTimePickerDialog(context) { horaFin = it } },
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)
                    ) {
                        Text(text = if (horaFin.isEmpty()) "Hora Fin" else "Fin: $horaFin")
                    }
                }
            }
        }

        // ==========================================
        // SECCIÓN 2: MOTIVOS Y HUERTA
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("2. Motivos de Acompañamiento", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.height(12.dp))

                // Lista desplegable de Selección Múltiple para Motivos
                MultiSelectDropdownSelector(
                    label = "Objetivo de Acompañamiento *",
                    options = motivosList,
                    selectedOptions = selectedMotivos,
                    onOptionToggled = { motivo ->
                        if (selectedMotivos.contains(motivo)) selectedMotivos.remove(motivo)
                        else selectedMotivos.add(motivo)
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Lista desplegable de Selección Múltiple para Huerta
                MultiSelectDropdownSelector(
                    label = "Estado de la Huerta *",
                    options = huertasList,
                    selectedOptions = selectedHuertas,
                    onOptionToggled = { huerta ->
                        if (selectedHuertas.contains(huerta)) selectedHuertas.remove(huerta)
                        else selectedHuertas.add(huerta)
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = objetivoVisita,
                    onValueChange = { objetivoVisita = it },
                    label = { Text("Objetivo Específico de la Visita *") },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    maxLines = 5
                )
            }
        }

        // ==========================================
        // SECCIÓN 3: ACTIVIDADES REALIZADAS (CULTIVOS)
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("3. Actividades Realizadas (Cultivos)", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.height(8.dp))

                // Listar Cultivos Agregados
                if (cultivos.isEmpty()) {
                    Text("No hay cultivos agregados. Agregue uno abajo.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                } else {
                    cultivos.forEachIndexed { index, cultivo ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${cultivo.categoria}: ${cultivo.especie} (${cultivo.tipo})", style = MaterialTheme.typography.titleSmall)
                                Text("Área: ${cultivo.areaM2} m² | Prod: ${cultivo.produccionKg} kg", style = MaterialTheme.typography.bodySmall)
                                if (cultivo.observaciones.isNotEmpty()) {
                                    Text("Obs: ${cultivo.observaciones}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            IconButton(onClick = { cultivos.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Agregar Cultivo:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.height(8.dp))

                // Lista desplegable para Categoría de Cultivo
                DropdownSelector(
                    label = "Categoría de Cultivo *",
                    options = categoriasCultivo,
                    selectedOption = tempCategoria,
                    onOptionSelected = { tempCategoria = it },
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = tempTipo,
                    onValueChange = { tempTipo = it },
                    label = { Text("Variedad / Clasificación / Tipo") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )

                OutlinedTextField(
                    value = tempEspecie,
                    onValueChange = { tempEspecie = it },
                    label = { Text("Especie cultivada (Nombre) *") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = tempArea,
                        onValueChange = { tempArea = it },
                        label = { Text("Área (m²) *") },
                        modifier = Modifier.weight(1f).padding(end = 2.dp)
                    )
                    OutlinedTextField(
                        value = tempProduccion,
                        onValueChange = { tempProduccion = it },
                        label = { Text("Producción (kg) *") },
                        modifier = Modifier.weight(1f).padding(start = 2.dp)
                    )
                }

                OutlinedTextField(
                    value = tempObsCultivo,
                    onValueChange = { tempObsCultivo = it },
                    label = { Text("Observaciones del Cultivo") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )

                Button(
                    onClick = {
                        val area = tempArea.toDoubleOrNull()
                        val prod = tempProduccion.toDoubleOrNull()
                        if (tempEspecie.isBlank() || area == null || prod == null) {
                            Toast.makeText(context, "Ingrese especie, área y producción válidos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        cultivos.add(
                            CultivoVisita(
                                categoria = tempCategoria,
                                tipo = tempTipo,
                                especie = tempEspecie,
                                areaM2 = area,
                                produccionKg = prod,
                                observaciones = tempObsCultivo
                            )
                        )
                        // Reset
                        tempTipo = ""
                        tempEspecie = ""
                        tempArea = ""
                        tempProduccion = ""
                        tempObsCultivo = ""
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text("Agregar Cultivo a la Lista")
                }

                Spacer(modifier = Modifier.height(16.dp))
                // Muestra de Suelo
                Text("Análisis de Suelos:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = muestraSuelo,
                        onCheckedChange = { muestraSuelo = it }
                    )
                    Text("¿Se tomó muestra de suelo?", style = MaterialTheme.typography.bodyMedium)
                }

                if (muestraSuelo) {
                    OutlinedTextField(
                        value = numeroMuestra,
                        onValueChange = { numeroMuestra = it },
                        label = { Text("Número de Muestra *") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            }
        }

        // ==========================================
        // SECCIÓN 4: MATERIALES ENTREGADOS
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("4. Materiales entregados", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.height(8.dp))

                // Listar Materiales Agregados
                if (materiales.isEmpty()) {
                    Text("No se han agregado materiales.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                } else {
                    materiales.forEachIndexed { index, mat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mat.material, style = MaterialTheme.typography.titleSmall)
                                Text("Cantidad: ${mat.cantidad} ${mat.unidad}", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { materiales.removeAt(index) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Agregar Material Entregado:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.tertiary)

                OutlinedTextField(
                    value = tempMaterial,
                    onValueChange = { tempMaterial = it },
                    label = { Text("Material / Insumo") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = tempCantidad,
                        onValueChange = { tempCantidad = it },
                        label = { Text("Cantidad") },
                        modifier = Modifier.weight(1f).padding(end = 2.dp)
                    )
                    OutlinedTextField(
                        value = tempUnidad,
                        onValueChange = { tempUnidad = it },
                        label = { Text("Unidad (ej: kg, bulto, plántula)") },
                        modifier = Modifier.weight(1f).padding(start = 2.dp)
                    )
                }

                Button(
                    onClick = {
                        val cant = tempCantidad.toDoubleOrNull()
                        if (tempMaterial.isBlank() || cant == null || tempUnidad.isBlank()) {
                            Toast.makeText(context, "Ingrese material, cantidad y unidad válidos", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        materiales.add(
                            MaterialEntregado(
                                material = tempMaterial,
                                cantidad = cant,
                                unidad = tempUnidad
                            )
                        )
                        // Reset
                        tempMaterial = ""
                        tempCantidad = ""
                        tempUnidad = ""
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text("Agregar Material")
                }
            }
        }

        // ==========================================
        // SECCIÓN 5: GEORREFERENCIACIÓN
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("5. Georreferenciación y Área", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Lat: ${latitud ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                        Text("Lon: ${longitud ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                        Text("Alt: ${altitud?.let { "$it msnm" } ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(
                        onClick = {
                            LocationHelper.getCurrentLocation(context,
                                onLocationFetched = { lat, lon, alt ->
                                    latitud = lat
                                    longitud = lon
                                    altitud = alt
                                    Toast.makeText(context, "Ubicación obtenida con éxito", Toast.LENGTH_SHORT).show()
                                },
                                onError = {
                                    Toast.makeText(context, "Error GPS: $it", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    ) {
                        Text("GPS")
                    }
                }

                OutlinedTextField(
                    value = areaIntervenir,
                    onValueChange = { areaIntervenir = it },
                    label = { Text("Área total a intervenir (m² o Hectáreas) *") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = observacionesGeo,
                    onValueChange = { observacionesGeo = it },
                    label = { Text("Observaciones de la ubicación") },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    maxLines = 4
                )
            }
        }

        // ==========================================
        // SECCIÓN 6: RECOMENDACIONES
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("6. Recomendaciones Generales *", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = recomendaciones,
                    onValueChange = { recomendaciones = it },
                    label = { Text("Fertilización, manejo fitosanitario, riego, podas, control de plagas, etc.") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 10
                )
            }
        }

        // ==========================================
        // SECCIÓN 7: CORRESPONSABILIDAD
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("7. Corresponsabilidad y Autorización", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "El productor declara que recibió la asistencia técnica, comprendió el procedimiento, acepta las recomendaciones, conoce los posibles riesgos y exonera de responsabilidad a la Alcaldía Local de Sumapaz, la ULATA y al profesional.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = aceptaCorresponsabilidad,
                        onCheckedChange = { aceptaCorresponsabilidad = it }
                    )
                    Text("He leído y acepto el texto de corresponsabilidad. *", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // ==========================================
        // SECCIÓN 8: FIRMAS Y PRÓXIMA VISITA
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("8. Recordatorio y Firmas", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.height(12.dp))

                // Recordatorio Próxima Visita
                Button(
                    onClick = { showDatePickerDialog(context) { proximaVisita = it } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)
                ) {
                    Text(text = if (proximaVisita.isEmpty()) "Recordatorio Próxima Visita" else "Próxima Visita: $proximaVisita")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Datos Profesional
                OutlinedTextField(
                    value = profesional,
                    onValueChange = { profesional = it },
                    label = { Text("Nombre del Profesional *") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = tarjetaProfesional,
                    onValueChange = { tarjetaProfesional = it },
                    label = { Text("Tarjeta Profesional *") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                // Firma Profesional
                SignaturePad(
                    label = "Firma del Profesional *",
                    onSignatureSaved = {
                        firmaProfesional = it
                        Toast.makeText(context, "Firma del profesional guardada", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Datos Operario
                OutlinedTextField(
                    value = cedulaOperario,
                    onValueChange = { cedulaOperario = it },
                    label = { Text("Cédula del Operario *") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                // Firma Operario
                SignaturePad(
                    label = "Firma del Operario de Campo *",
                    onSignatureSaved = {
                        firmaOperario = it
                        Toast.makeText(context, "Firma del operario guardada", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Datos Usuario
                OutlinedTextField(
                    value = cedulaUsuario,
                    onValueChange = { cedulaUsuario = it },
                    label = { Text("Cédula del Productor (Usuario) *") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                // Firma Usuario
                SignaturePad(
                    label = "Firma del Usuario (Productor) *",
                    onSignatureSaved = {
                        firmaUsuario = it
                        Toast.makeText(context, "Firma del usuario guardada", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón de Guardado
        Button(
            onClick = {
                // VALIDACIONES
                if (nombre.isBlank() || finca.isBlank() || vereda.isBlank() || telefono.isBlank() ||
                    objetivoVisita.isBlank() || recomendaciones.isBlank() || profesional.isBlank() ||
                    tarjetaProfesional.isBlank() || cedulaOperario.isBlank() || cedulaUsuario.isBlank() ||
                    areaIntervenir.isBlank()
                ) {
                    Toast.makeText(context, "Por favor complete todos los campos marcados con (*)", Toast.LENGTH_LONG).show()
                    return@Button
                }

                if (muestraSuelo && numeroMuestra.isBlank()) {
                    Toast.makeText(context, "Ingrese el número de la muestra de suelo", Toast.LENGTH_LONG).show()
                    return@Button
                }

                if (selectedMotivos.isEmpty()) {
                    Toast.makeText(context, "Debe seleccionar al menos un motivo de acompañamiento", Toast.LENGTH_LONG).show()
                    return@Button
                }

                if (!aceptaCorresponsabilidad) {
                    Toast.makeText(context, "Debe aceptar el texto de corresponsabilidad legal", Toast.LENGTH_LONG).show()
                    return@Button
                }

                if (firmaProfesional == null) {
                    Toast.makeText(context, "Falta la firma digital del Profesional", Toast.LENGTH_LONG).show()
                    return@Button
                }
                if (firmaOperario == null) {
                    Toast.makeText(context, "Falta la firma digital del Operario de Campo", Toast.LENGTH_LONG).show()
                    return@Button
                }
                if (firmaUsuario == null) {
                    Toast.makeText(context, "Falta la firma digital del Productor (Usuario)", Toast.LENGTH_LONG).show()
                    return@Button
                }

                val areaVal = areaIntervenir.toDoubleOrNull()
                if (areaVal == null) {
                    Toast.makeText(context, "Ingrese un valor numérico válido para el área a intervenir", Toast.LENGTH_LONG).show()
                    return@Button
                }

                // Guardar en la base de datos
                val visita = VisitaAgricola(
                    fecha = fecha,
                    nombre = nombre,
                    finca = finca,
                    vereda = vereda,
                    corregimiento = corregimiento,
                    cuenca = cuenca,
                    telefono = telefono,
                    hora_inicio = horaInicio,
                    hora_fin = if (horaFin.isEmpty()) nowTimeStr else horaFin,
                    numero_registro = numeroRegistro,
                    objetivo_visita = objetivoVisita,
                    recomendaciones = recomendaciones,
                    muestra_suelo = muestraSuelo,
                    numero_muestra = if (muestraSuelo) numeroMuestra else null,
                    latitud = latitud,
                    longitud = longitud,
                    altitud = altitud,
                    observaciones_geo = observacionesGeo,
                    area_intervenir = areaVal,
                    acepta_corresponsabilidad = aceptaCorresponsabilidad,
                    proxima_visita = if (proximaVisita.isEmpty()) null else proximaVisita,
                    profesional = profesional,
                    tarjeta_profesional = tarjetaProfesional,
                    cedula_operario = cedulaOperario,
                    cedula_usuario = cedulaUsuario,
                    firma_profesional = firmaProfesional,
                    firma_operario = firmaOperario,
                    firma_usuario = firmaUsuario,
                    motivos = selectedMotivos.toList(),
                    tiposHuerta = selectedHuertas.toList(),
                    cultivos = cultivos.toList(),
                    materiales = materiales.toList()
                )

                val id = dbHelper.insertVisitaAgricola(visita)
                if (id != -1L) {
                    Toast.makeText(context, "Visita agrícola guardada correctamente", Toast.LENGTH_SHORT).show()
                    // Generar PDF
                    try {
                        val pdfFile = PdfGenerator.generateVisitaAgricolaPdf(context, visita.copy(id = id.toInt()))
                        Toast.makeText(context, "PDF generado: ${pdfFile.name}", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al generar PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                    onNavigateToHistorial()
                } else {
                    Toast.makeText(context, "Error al guardar en la base de datos", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("GUARDAR Y GENERAR ACTA", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

private fun showDatePickerDialog(context: Context, onDateSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateSelected(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun showTimePickerDialog(context: Context, onTimeSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val formattedTime = String.format("%02d:%02d", hourOfDay, minute)
            onTimeSelected(formattedTime)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    ).show()
}
