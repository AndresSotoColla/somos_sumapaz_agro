package com.example.somos_sumapaz_agro.util

import com.example.somos_sumapaz_agro.model.VisitaAgricola
import com.example.somos_sumapaz_agro.model.VisitaPecuaria

object CsvExporter {

    // Carácter UTF-8 BOM para que Excel detecte la codificación automáticamente
    private const val BOM = "\uFEFF"

    fun generatePecuariaCsv(visitas: List<VisitaPecuaria>): String {
        val sb = StringBuilder()
        sb.append(BOM)
        
        // Cabeceras
        val headers = listOf(
            "ID", "Fecha", "Corregimiento", "Vereda", "Finca", "Cuenca", 
            "Hora Inicio", "Hora Fin", "Latitud", "Longitud", "Productor (Usuario)", 
            "Primera Vez", "Seguimiento", "Fecha Visita Anterior", "Diagnóstico", 
            "Procedimiento", "Recomendaciones de Visita", "Acepta Corresponsabilidad", 
            "Próxima Visita", "Profesional", "Tarjeta Profesional", 
            "Cédula Operario", "Cédula Usuario", "Especies Atendidas"
        )
        sb.append(headers.joinToString(";")).append("\n")

        for (v in visitas) {
            val especiesStr = v.especies.joinToString(", ")
            val row = listOf(
                v.id.toString(),
                v.fecha,
                v.corregimiento,
                v.vereda,
                v.finca,
                v.cuenca,
                v.hora_inicio,
                v.hora_fin,
                v.latitud?.toString() ?: "",
                v.longitud?.toString() ?: "",
                v.usuario,
                if (v.primera_vez) "SÍ" else "NO",
                if (v.seguimiento) "SÍ" else "NO",
                v.fecha_visita_anterior ?: "",
                v.diagnostico,
                v.procedimiento,
                v.recomendaciones,
                if (v.acepta_corresponsabilidad) "SÍ" else "NO",
                v.proxima_visita ?: "",
                v.profesional,
                v.tarjeta_profesional,
                v.cedula_operario,
                v.cedula_usuario,
                especiesStr
            ).map { escapeCsvField(it) }
            
            sb.append(row.joinToString(";")).append("\n")
        }

        return sb.toString()
    }

    fun generateAgricolaCsv(visitas: List<VisitaAgricola>): String {
        val sb = StringBuilder()
        sb.append(BOM)

        // Cabeceras
        val headers = listOf(
            "ID", "Fecha", "Productor (Nombre)", "Finca", "Vereda", "Corregimiento", 
            "Cuenca", "Teléfono", "Hora Inicio", "Hora Fin", "Número Registro", 
            "Objetivo de Visita", "Recomendaciones Generales", "Muestra Suelo", 
            "Número Muestra", "Latitud", "Longitud", "Altitud", "Observaciones Geo", 
            "Área Intervenir", "Acepta Corresponsabilidad", "Próxima Visita", 
            "Profesional", "Tarjeta Profesional", "Cédula Operario", "Cédula Usuario",
            "Motivos Acompañamiento", "Tipos Huerta", "Cultivos Observados", "Materiales Entregados"
        )
        sb.append(headers.joinToString(";")).append("\n")

        for (v in visitas) {
            val motivosStr = v.motivos.joinToString(", ")
            val huertasStr = v.tiposHuerta.joinToString(", ")
            
            val cultivosStr = v.cultivos.joinToString(" | ") { c ->
                "${c.categoria}: ${c.especie} (${c.tipo}) - ${c.areaM2}m² - ${c.produccionKg}kg"
            }
            
            val materialesStr = v.materiales.joinToString(" | ") { m ->
                "${m.material} (${m.cantidad} ${m.unidad})"
            }

            val row = listOf(
                v.id.toString(),
                v.fecha,
                v.nombre,
                v.finca,
                v.vereda,
                v.corregimiento,
                v.cuenca,
                v.telefono,
                v.hora_inicio,
                v.hora_fin,
                v.numero_registro,
                v.objetivo_visita,
                v.recomendaciones,
                if (v.muestra_suelo) "SÍ" else "NO",
                v.numero_muestra ?: "",
                v.latitud?.toString() ?: "",
                v.longitud?.toString() ?: "",
                v.altitud?.toString() ?: "",
                v.observaciones_geo ?: "",
                v.area_intervenir?.toString() ?: "",
                if (v.acepta_corresponsabilidad) "SÍ" else "NO",
                v.proxima_visita ?: "",
                v.profesional,
                v.tarjeta_profesional,
                v.cedula_operario,
                v.cedula_usuario,
                motivosStr,
                huertasStr,
                cultivosStr,
                materialesStr
            ).map { escapeCsvField(it) }

            sb.append(row.joinToString(";")).append("\n")
        }

        return sb.toString()
    }

    private fun escapeCsvField(field: String): String {
        // Si contiene punto y coma, salto de línea o comillas, debemos escapar y envolver en comillas
        var escaped = field
        if (escaped.contains(";") || escaped.contains("\n") || escaped.contains("\r") || escaped.contains("\"")) {
            escaped = escaped.replace("\"", "\"\"")
            return "\"$escaped\""
        }
        return escaped
    }
}
