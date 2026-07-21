package com.example.somos_sumapaz_agro.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.somos_sumapaz_agro.db.VisitasDbHelper
import com.example.somos_sumapaz_agro.model.VisitaPecuaria
import com.example.somos_sumapaz_agro.ui.components.SignaturePad
import com.example.somos_sumapaz_agro.util.LocationHelper
import com.example.somos_sumapaz_agro.util.PdfGenerator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PecuariaForm(
    dbHelper: VisitasDbHelper,
    onNavigateToHistorial: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Form States
    val calendar = Calendar.getInstance()
    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    val nowTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
    
    // 1. Información General
    var fecha by remember { mutableStateOf(todayStr) }
    var corregimiento by remember { mutableStateOf("Nazareth") }
    var vereda by remember { mutableStateOf("") }
    var finca by remember { mutableStateOf("") }
    var cuenca by remember { mutableStateOf("Río Sumapaz") }
    var horaInicio by remember { mutableStateOf(nowTimeStr) }
    var horaFin by remember { mutableStateOf("") }
    
    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }
    var usuario by remember { mutableStateOf("") }
    var documento by remember { mutableStateOf("") }

    // 2. Especies
    val especiesList = listOf(
        "Bovino", "Equino", "Caprino", "Ovino", "Porcícola", 
        "Cunícola", "Apícola", "Piscícola", "Avícola", "Ordenamiento de finca"
    )
    val selectedEspecies = remember { mutableStateListOf<String>() }

    // Motivo
    var esPrimeraVez by remember { mutableStateOf(true) }
    var fechaVisitaAnterior by remember { mutableStateOf("") }

    // 3. Diagnóstico
    var diagnostico by remember { mutableStateOf("") }

    // 4. Procedimiento
    var procedimiento by remember { mutableStateOf("") }

    // 5. Recomendaciones
    var recomendaciones by remember { mutableStateOf("") }

    // 6. Corresponsabilidad
    var aceptaCorresponsabilidad by remember { mutableStateOf(false) }

    // 7. Próxima Visita y Firmas
    var proximaVisita by remember { mutableStateOf("") }
    var profesional by remember { mutableStateOf("") }
    var tarjetaProfesional by remember { mutableStateOf("") }
    var cedulaOperario by remember { mutableStateOf("") }
    var cedulaUsuario by remember { mutableStateOf("") }

    // Signatures (Base64)
    var firmaProfesional by remember { mutableStateOf<String?>(null) }
    var firmaOperario by remember { mutableStateOf<String?>(null) }
    var firmaUsuario by remember { mutableStateOf<String?>(null) }

    // Sincronizar cédula operario y usuario por defecto
    LaunchedEffect(documento) {
        cedulaUsuario = documento
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Acompañamiento Área Pecuaria",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
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
                Text("1. Información General", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))

                // Fecha Picker
                Button(
                    onClick = {
                        showDatePickerDialog(context) { fecha = it }
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    Text("Fecha de Visita: $fecha")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Corregimiento
                Text("Corregimiento:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    listOf("Nazareth", "Betania", "San Juan").forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.selectable(
                                selected = (corregimiento == item),
                                onClick = { corregimiento = item }
                            ).padding(8.dp)
                        ) {
                            RadioButton(selected = (corregimiento == item), onClick = { corregimiento = item })
                            Text(text = item, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }

                OutlinedTextField(
                    value = vereda,
                    onValueChange = { vereda = it },
                    label = { Text("Vereda *") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = finca,
                    onValueChange = { finca = it },
                    label = { Text("Finca *") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Cuenca
                Text("Cuenca:", style = MaterialTheme.typography.bodyMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    listOf("Río Sumapaz", "Río Blanco").forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.selectable(
                                selected = (cuenca == item),
                                onClick = { cuenca = item }
                            ).padding(8.dp)
                        ) {
                            RadioButton(selected = (cuenca == item), onClick = { cuenca = item })
                            Text(text = item, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { showTimePickerDialog(context) { horaInicio = it } },
                        modifier = Modifier.weight(1f).padding(end = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) {
                        Text("Inicio: $horaInicio")
                    }
                    Button(
                        onClick = { showTimePickerDialog(context) { horaFin = it } },
                        modifier = Modifier.weight(1f).padding(start = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) {
                        Text(text = if (horaFin.isEmpty()) "Hora Fin" else "Fin: $horaFin")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // GPS Location
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Lat: ${latitud ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                        Text("Lon: ${longitud ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(
                        onClick = {
                            LocationHelper.getCurrentLocation(context,
                                onLocationFetched = { lat, lon, _ ->
                                    latitud = lat
                                    longitud = lon
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
                    value = usuario,
                    onValueChange = { usuario = it },
                    label = { Text("Nombre del Productor (Usuario) *") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                OutlinedTextField(
                    value = documento,
                    onValueChange = { documento = it },
                    label = { Text("Documento de Identidad *") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }
        }

        // ==========================================
        // SECCIÓN 2: TIPO DE ESPECIE
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("2. Tipo de Especie Atendida", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    maxItemsInEachRow = 2
                ) {
                    especiesList.forEach { especie ->
                        val isChecked = selectedEspecies.contains(especie)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .padding(vertical = 2.dp)
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) selectedEspecies.add(especie)
                                    else selectedEspecies.remove(especie)
                                }
                            )
                            Text(especie, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Motivo de la Visita", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.selectable(selected = esPrimeraVez, onClick = { esPrimeraVez = true })
                    ) {
                        RadioButton(selected = esPrimeraVez, onClick = { esPrimeraVez = true })
                        Text("Primera vez", modifier = Modifier.padding(start = 4.dp))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.selectable(selected = !esPrimeraVez, onClick = { esPrimeraVez = false })
                    ) {
                        RadioButton(selected = !esPrimeraVez, onClick = { esPrimeraVez = false })
                        Text("Seguimiento", modifier = Modifier.padding(start = 4.dp))
                    }
                }

                if (!esPrimeraVez) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showDatePickerDialog(context) { fechaVisitaAnterior = it } },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                    ) {
                        Text(text = if (fechaVisitaAnterior.isEmpty()) "Fecha de Visita Anterior *" else "Anterior: $fechaVisitaAnterior")
                    }
                }
            }
        }

        // ==========================================
        // SECCIÓN 3: DIAGNÓSTICO
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("3. Diagnóstico / Seguimiento a actividades *", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = diagnostico,
                    onValueChange = { diagnostico = it },
                    label = { Text("Registrar hallazgos, estado sanitario, alimentación, infraestructura, etc.") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 10
                )
            }
        }

        // ==========================================
        // SECCIÓN 4: PROCEDIMIENTO
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("4. Procedimiento / Recomendaciones *", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = procedimiento,
                    onValueChange = { procedimiento = it },
                    label = { Text("Registrar tratamientos, capacitación, manejos y procedimientos efectuados.") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 10
                )
            }
        }

        // ==========================================
        // SECCIÓN 5: RECOMENDACIONES DE LA VISITA
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("5. Tareas del Productor *", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = recomendaciones,
                    onValueChange = { recomendaciones = it },
                    label = { Text("Registrar tareas pendientes para el productor (ej. vacunar, cambiar alimento, etc.)") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 10
                )
            }
        }

        // ==========================================
        // SECCIÓN 6: CORRESPONSABILIDAD
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("6. Corresponsabilidad y Autorización", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "El productor declara que recibió la asistencia técnica, entendió el procedimiento, acepta las recomendaciones, conoce los posibles riesgos y exonera de responsabilidad a la Alcaldía Local de Sumapaz, la ULATA y al profesional por las actuaciones realizadas.",
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
        // SECCIÓN 7: FIRMAS Y PRÓXIMA VISITA
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("7. Recordatorio y Firmas", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(12.dp))

                // Recordatorio Próxima Visita
                Button(
                    onClick = { showDatePickerDialog(context) { proximaVisita = it } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
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

                // Datos Usuario (Nombre y Cédula pre-sincronizados pero editables aquí)
                Text("Usuario: $usuario ($cedulaUsuario)", style = MaterialTheme.typography.bodyMedium)

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
                if (vereda.isBlank() || finca.isBlank() || usuario.isBlank() || documento.isBlank() ||
                    diagnostico.isBlank() || procedimiento.isBlank() || recomendaciones.isBlank() ||
                    profesional.isBlank() || tarjetaProfesional.isBlank() || cedulaOperario.isBlank()
                ) {
                    Toast.makeText(context, "Por favor complete todos los campos marcados con (*)", Toast.LENGTH_LONG).show()
                    return@Button
                }

                if (!esPrimeraVez && fechaVisitaAnterior.isBlank()) {
                    Toast.makeText(context, "Debe ingresar la fecha de la visita anterior para el seguimiento", Toast.LENGTH_LONG).show()
                    return@Button
                }

                if (selectedEspecies.isEmpty()) {
                    Toast.makeText(context, "Debe seleccionar al menos una especie de la lista", Toast.LENGTH_LONG).show()
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

                // Guardar en la base de datos
                val visita = VisitaPecuaria(
                    fecha = fecha,
                    corregimiento = corregimiento,
                    vereda = vereda,
                    finca = finca,
                    cuenca = cuenca,
                    hora_inicio = horaInicio,
                    hora_fin = if (horaFin.isEmpty()) nowTimeStr else horaFin,
                    latitud = latitud,
                    longitud = longitud,
                    usuario = usuario,
                    primera_vez = esPrimeraVez,
                    seguimiento = !esPrimeraVez,
                    fecha_visita_anterior = if (esPrimeraVez) null else fechaVisitaAnterior,
                    diagnostico = diagnostico,
                    procedimiento = procedimiento,
                    recomendaciones = recomendaciones,
                    acepta_corresponsabilidad = aceptaCorresponsabilidad,
                    proxima_visita = if (proximaVisita.isEmpty()) null else proximaVisita,
                    profesional = profesional,
                    tarjeta_profesional = tarjetaProfesional,
                    cedula_operario = cedulaOperario,
                    cedula_usuario = cedulaUsuario,
                    firma_profesional = firmaProfesional,
                    firma_operario = firmaOperario,
                    firma_usuario = firmaUsuario,
                    especies = selectedEspecies.toList()
                )

                val id = dbHelper.insertVisitaPecuaria(visita)
                if (id != -1L) {
                    Toast.makeText(context, "Visita pecuaria guardada correctamente", Toast.LENGTH_SHORT).show()
                    // Generar PDF
                    try {
                        val pdfFile = PdfGenerator.generateVisitaPecuariaPdf(context, visita.copy(id = id.toInt()))
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
