package com.example.somos_sumapaz_agro.model

// Modelos para la visita pecuaria
data class VisitaPecuaria(
    val id: Int = 0,
    val fecha: String,
    val corregimiento: String, // Nazareth, Betania, San Juan
    val vereda: String,
    val finca: String,
    val cuenca: String, // Río Sumapaz, Río Blanco
    val hora_inicio: String,
    val hora_fin: String,
    val latitud: Double?,
    val longitud: Double?,
    val usuario: String, // Nombre del productor
    val primera_vez: Boolean,
    val seguimiento: Boolean,
    val fecha_visita_anterior: String?,
    val diagnostico: String,
    val procedimiento: String,
    val recomendaciones: String,
    val acepta_corresponsabilidad: Boolean,
    val proxima_visita: String?,
    val profesional: String,
    val tarjeta_profesional: String,
    val cedula_operario: String,
    val cedula_usuario: String,
    val firma_profesional: String?, // Codificado en Base64
    val firma_operario: String?,    // Codificado en Base64
    val firma_usuario: String?,     // Codificado en Base64
    val especies: List<String> = emptyList(), // Guardados en visita_pecuaria_especies
    val synced: Boolean = false
)

// Modelos para la visita agrícola
data class VisitaAgricola(
    val id: Int = 0,
    val fecha: String,
    val nombre: String, // Productor
    val finca: String,
    val vereda: String,
    val corregimiento: String,
    val cuenca: String,
    val telefono: String,
    val hora_inicio: String,
    val hora_fin: String,
    val numero_registro: String,
    val objetivo_visita: String,
    val recomendaciones: String,
    val muestra_suelo: Boolean,
    val numero_muestra: String?,
    val latitud: Double?,
    val longitud: Double?,
    val altitud: Double?,
    val observaciones_geo: String?,
    val area_intervenir: Double?,
    val acepta_corresponsabilidad: Boolean,
    val proxima_visita: String?,
    val profesional: String,
    val tarjeta_profesional: String,
    val cedula_operario: String,
    val cedula_usuario: String,
    val firma_profesional: String?, // Codificado en Base64
    val firma_operario: String?,    // Codificado en Base64
    val firma_usuario: String?,     // Codificado en Base64
    val motivos: List<String> = emptyList(),      // Tabla motivos_visita_agricola
    val tiposHuerta: List<String> = emptyList(),  // Tabla tipo_huerta
    val cultivos: List<CultivoVisita> = emptyList(), // Tabla cultivos_visita
    val materiales: List<MaterialEntregado> = emptyList(), // Tabla materiales_entregados
    val synced: Boolean = false
)

data class CultivoVisita(
    val id: Int = 0,
    val categoria: String, // Leguminosas, Hortalizas, Tubérculos, Aromáticas y medicinales, Frutales, Otras
    val tipo: String,
    val especie: String,
    val areaM2: Double,
    val produccionKg: Double,
    val observaciones: String
)

data class MaterialEntregado(
    val id: Int = 0,
    val material: String,
    val cantidad: Double,
    val unidad: String
)
