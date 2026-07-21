package com.example.somos_sumapaz_agro.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.util.Base64
import com.example.somos_sumapaz_agro.model.VisitaAgricola
import com.example.somos_sumapaz_agro.model.VisitaPecuaria
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    private class PdfPageHelper(val context: Context, val document: PdfDocument, val title: String) {
        var pageNum = 1
        var pageInfo = PdfDocument.PageInfo.Builder(612, 792, pageNum).create()
        var currentPage = document.startPage(pageInfo)
        var canvas = currentPage.canvas
        var y = 50f
        val marginStart = 45f
        val marginEnd = 567f // 612 - 45
        val contentWidth = marginEnd - marginStart

        // Pinceles predefinidos
        val paintTitle = Paint().apply {
            color = Color.rgb(46, 125, 50) // Verde Bosque
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintSubTitle = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintLabel = Paint().apply {
            color = Color.DKGRAY
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintValue = Paint().apply {
            color = Color.BLACK
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintText = Paint().apply {
            color = Color.BLACK
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintBorder = Paint().apply {
            color = Color.rgb(200, 200, 200)
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }

        val paintHeaderBg = Paint().apply {
            color = Color.rgb(240, 240, 240)
            style = Paint.Style.FILL
        }

        init {
            drawHeader()
        }

        fun checkPageSpace(requiredSpace: Float) {
            if (y + requiredSpace > 730f) {
                // Cerrar página actual
                document.finishPage(currentPage)
                pageNum++
                // Crear nueva página
                pageInfo = PdfDocument.PageInfo.Builder(612, 792, pageNum).create()
                currentPage = document.startPage(pageInfo)
                canvas = currentPage.canvas
                y = 40f
                
                // Dibujar cabecera secundaria
                val headerPaint = Paint().apply {
                    color = Color.GRAY
                    textSize = 8f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    isAntiAlias = true
                }
                canvas.drawText("ULATA Sumapaz - $title (Pág. $pageNum)", marginStart, y, headerPaint)
                canvas.drawLine(marginStart, y + 4, marginEnd, y + 4, paintBorder)
                y += 25f
            }
        }

        fun drawHeader() {
            y = 45f
            val headerTitle = "ALCALDÍA LOCAL DE SUMAPAZ"
            val subHeaderTitle = "UNIDAD LOCAL DE ASISTENCIA TÉCNICA AGROPECUARIA - ULATA"
            
            val pHeader = Paint().apply {
                color = Color.rgb(46, 125, 50)
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(headerTitle, 306f, y, pHeader)
            y += 15f
            pHeader.textSize = 8.5f
            pHeader.color = Color.DKGRAY
            canvas.drawText(subHeaderTitle, 306f, y, pHeader)
            y += 15f
            
            // Título de la sección
            val pSectionTitle = Paint().apply {
                color = Color.WHITE
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val bgRect = RectF(marginStart, y, marginEnd, y + 18f)
            val pBg = Paint().apply {
                color = Color.rgb(46, 125, 50)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(bgRect, 4f, 4f, pBg)
            canvas.drawText(title.uppercase(), 306f, y + 12f, pSectionTitle)
            
            y += 32f
        }

        fun drawSectionHeader(name: String) {
            checkPageSpace(30f)
            val p = Paint(paintSubTitle).apply {
                color = Color.rgb(141, 110, 99) // Color Tierra / Ocre
            }
            canvas.drawText(name, marginStart, y, p)
            canvas.drawLine(marginStart, y + 4f, marginEnd, y + 4f, paintBorder)
            y += 18f
        }

        fun drawField(label: String, value: String, xOffset: Float = 0f, widthShare: Float = 1f) {
            val space = 13f
            checkPageSpace(space)
            val currentX = marginStart + xOffset * contentWidth
            val maxTextWidth = contentWidth * widthShare - 10f
            
            val labelText = "$label: "
            val labelWidth = paintLabel.measureText(labelText)
            
            canvas.drawText(labelText, currentX, y, paintLabel)
            
            // Truncar si el valor es demasiado largo o envolver
            val valueText = if (value.isEmpty()) "N/A" else value
            val wrapped = wrapText(valueText, paintValue, maxTextWidth - labelWidth)
            if (wrapped.isNotEmpty()) {
                canvas.drawText(wrapped[0], currentX + labelWidth, y, paintValue)
                if (wrapped.size > 1) {
                    for (i in 1 until wrapped.size) {
                        y += space
                        checkPageSpace(space)
                        canvas.drawText(wrapped[i], currentX + 15f, y, paintValue)
                    }
                }
            }
        }

        fun drawParagraph(label: String?, text: String) {
            if (label != null) {
                checkPageSpace(15f)
                canvas.drawText(label, marginStart, y, paintLabel)
                y += 12f
            }
            val words = text.split(" ", "\n")
            var line = ""
            val paintText = paintText
            val space = 11f
            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                val testWidth = paintText.measureText(testLine)
                if (testWidth > contentWidth) {
                    checkPageSpace(space)
                    canvas.drawText(line, marginStart, y, paintText)
                    y += space
                    line = word
                } else {
                    line = testLine
                }
            }
            if (line.isNotEmpty()) {
                checkPageSpace(space)
                canvas.drawText(line, marginStart, y, paintText)
                y += space + 4f
            }
        }

        fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
            val result = mutableListOf<String>()
            val words = text.split(" ")
            var line = ""
            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                if (paint.measureText(testLine) > maxWidth) {
                    if (line.isNotEmpty()) {
                        result.add(line)
                    }
                    line = word
                } else {
                    line = testLine
                }
            }
            if (line.isNotEmpty()) {
                result.add(line)
            }
            return result
        }

        fun drawTableHeaders(headers: List<String>, columnWeights: List<Float>) {
            val h = 16f
            checkPageSpace(h)
            
            // Dibujar fondo
            val rect = RectF(marginStart, y - 11f, marginEnd, y + 5f)
            canvas.drawRect(rect, paintHeaderBg)
            canvas.drawRect(rect, paintBorder)

            var currentX = marginStart
            for (i in headers.indices) {
                val colWidth = columnWeights[i] * contentWidth
                canvas.drawText(headers[i], currentX + 4f, y, paintLabel)
                
                if (i > 0) {
                    canvas.drawLine(currentX, y - 11f, currentX, y + 5f, paintBorder)
                }
                currentX += colWidth
            }
            y += h
        }

        fun drawTableRow(cells: List<String>, columnWeights: List<Float>) {
            val cellHeight = 14f
            checkPageSpace(cellHeight)

            val startY = y - 10f
            var currentX = marginStart
            var maxHeight = cellHeight

            val cellLines = cells.mapIndexed { idx, text ->
                val colWidth = columnWeights[idx] * contentWidth - 8f
                wrapText(text, paintValue, colWidth)
            }

            val maxL = cellLines.maxOfOrNull { it.size } ?: 1
            maxHeight = maxL * 11f + 4f
            checkPageSpace(maxHeight)

            val endY = startY + maxHeight
            // Rectángulo de fila
            val rowRect = RectF(marginStart, startY, marginEnd, endY)
            canvas.drawRect(rowRect, paintBorder)

            for (i in cellLines.indices) {
                val colWidth = columnWeights[i] * contentWidth
                val lines = cellLines[i]
                var cellY = startY + 10f
                for (line in lines) {
                    canvas.drawText(line, currentX + 4f, cellY, paintValue)
                    cellY += 11f
                }
                if (i > 0) {
                    canvas.drawLine(currentX, startY, currentX, endY, paintBorder)
                }
                currentX += colWidth
            }
            y = endY + 10f
        }

        fun drawSignature(label: String, name: String, idCard: String, base64Sig: String?, xPercent: Float) {
            val currentX = marginStart + xPercent * contentWidth
            val sigWidth = 140f
            val sigHeight = 60f

            checkPageSpace(sigHeight + 45f)

            // Dibujar recuadro de firma
            val rect = RectF(currentX, y, currentX + sigWidth, y + sigHeight)
            canvas.drawRect(rect, paintBorder)

            val signatureBitmap = decodeBase64ToBitmap(base64Sig)
            if (signatureBitmap != null) {
                val srcRect = Rect(0, 0, signatureBitmap.width, signatureBitmap.height)
                canvas.drawBitmap(signatureBitmap, srcRect, rect, null)
            } else {
                val textPaint = Paint(paintText).apply {
                    color = Color.LTGRAY
                    textSize = 8f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("Sin firma", currentX + sigWidth / 2f, y + sigHeight / 2f + 3f, textPaint)
            }

            // Línea sobre firmas
            canvas.drawLine(currentX, y + sigHeight + 4f, currentX + sigWidth, y + sigHeight + 4f, paintText)
            
            // Textos descriptivos
            val pDesc = Paint(paintLabel).apply { textSize = 7.5f }
            val pVal = Paint(paintValue).apply { textSize = 7.5f }

            canvas.drawText(label, currentX, y + sigHeight + 13f, pDesc)
            
            if (name.isNotEmpty()) {
                canvas.drawText("Nombre: $name", currentX, y + sigHeight + 23f, pVal)
            }
            if (idCard.isNotEmpty()) {
                canvas.drawText("Cédula/TP: $idCard", currentX, y + sigHeight + 33f, pVal)
            }
        }

        fun finish() {
            document.finishPage(currentPage)
        }
    }

    fun generateVisitaPecuariaPdf(context: Context, v: VisitaPecuaria): File {
        val file = File(context.cacheDir, "acta_visita_pecuaria_${v.id}.pdf")
        val document = PdfDocument()
        val helper = PdfPageHelper(context, document, "Formato de Visita de Acompañamiento Área Pecuaria")

        // 1. Información General de la Visita
        helper.drawSectionHeader("1. Información General de la Visita")
        helper.drawField("Fecha", v.fecha, 0f, 0.5f)
        helper.drawField("Corregimiento", v.corregimiento, 0.5f, 0.5f)
        
        helper.y += 12f
        helper.drawField("Vereda", v.vereda, 0f, 0.5f)
        helper.drawField("Finca", v.finca, 0.5f, 0.5f)

        helper.y += 12f
        helper.drawField("Cuenca", v.cuenca, 0f, 0.5f)
        helper.drawField("Horas de visita", "${v.hora_inicio} a ${v.hora_fin}", 0.5f, 0.5f)

        helper.y += 12f
        val geoStr = if (v.latitud != null && v.longitud != null) "${v.latitud}, ${v.longitud}" else "No registrada"
        helper.drawField("Georreferenciación", geoStr, 0f, 0.5f)
        helper.drawField("Productor (Usuario)", v.usuario, 0.5f, 0.5f)

        helper.y += 18f

        // 2. Tipo de Especie y Motivo
        helper.drawSectionHeader("2. Tipo de Especie y Motivo de Acompañamiento")
        helper.drawField("Especies Atendidas", v.especies.joinToString(", ").ifEmpty { "Ninguna seleccionada" })
        helper.y += 12f
        val motivoVisita = when {
            v.primera_vez -> "Primera Vez"
            v.seguimiento -> "Seguimiento"
            else -> "No especificado"
        }
        helper.drawField("Motivo de Visita", motivoVisita, 0f, 0.5f)
        if (v.seguimiento && !v.fecha_visita_anterior.isNullOrEmpty()) {
            helper.drawField("Fecha Visita Anterior", v.fecha_visita_anterior, 0.5f, 0.5f)
        }
        
        helper.y += 18f

        // 3. Diagnóstico / Seguimiento
        helper.drawSectionHeader("3. Diagnóstico / Seguimiento a Actividades")
        helper.drawParagraph(null, v.diagnostico.ifEmpty { "Sin diagnóstico registrado." })

        // 4. Procedimiento / Recomendaciones
        helper.drawSectionHeader("4. Procedimiento Realizado / Capacitación")
        helper.drawParagraph(null, v.procedimiento.ifEmpty { "Sin procedimientos registrados." })

        // 5. Recomendaciones de la Visita
        helper.drawSectionHeader("5. Compromisos / Recomendaciones al Productor")
        helper.drawParagraph(null, v.recomendaciones.ifEmpty { "Sin compromisos registrados." })

        // 6. Corresponsabilidad y Firma próxima visita
        helper.drawSectionHeader("6. Corresponsabilidad y Autorización")
        val legalText = "El productor declara que recibió la asistencia técnica, entendió el procedimiento, acepta las recomendaciones, conoce los posibles riesgos y exonera de responsabilidad a la Alcaldía Local de Sumapaz, la ULATA y al profesional por las actuaciones realizadas."
        helper.drawParagraph(null, legalText)
        
        val corresponsabilidadAceptada = if (v.acepta_corresponsabilidad) "SÍ ACEPTA" else "NO ACEPTA"
        helper.drawField("Aceptación de corresponsabilidad", corresponsabilidadAceptada)
        helper.y += 12f
        helper.drawField("Recordatorio Próxima Visita", v.proxima_visita ?: "No programada")

        helper.y += 24f

        // 7. Firmas
        helper.drawSectionHeader("7. Firmas de Soporte")
        
        val startY = helper.y
        // Firma Profesional
        helper.drawSignature("PROFESIONAL ULATA", v.profesional, v.tarjeta_profesional, v.firma_profesional, 0f)
        
        // Firma Operario
        helper.y = startY
        helper.drawSignature("OPERARIO DE CAMPO", "Operario ULATA", v.cedula_operario, v.firma_operario, 0.35f)
        
        // Firma Usuario
        helper.y = startY
        helper.drawSignature("PRODUCTOR (USUARIO)", v.usuario, v.cedula_usuario, v.firma_usuario, 0.70f)

        // Completar PDF
        helper.finish()
        
        val fileOutputStream = FileOutputStream(file)
        document.writeTo(fileOutputStream)
        document.close()
        fileOutputStream.close()

        return file
    }

    fun generateVisitaAgricolaPdf(context: Context, v: VisitaAgricola): File {
        val file = File(context.cacheDir, "acta_visita_agricola_${v.id}.pdf")
        val document = PdfDocument()
        val helper = PdfPageHelper(context, document, "Formato de Asistencia Técnica Agrícola")

        // 1. Información General
        helper.drawSectionHeader("1. Información General")
        helper.drawField("Número Registro", v.numero_registro, 0f, 0.5f)
        helper.drawField("Fecha", v.fecha, 0.5f, 0.5f)
        
        helper.y += 12f
        helper.drawField("Productor", v.nombre, 0f, 0.5f)
        helper.drawField("Teléfono", v.telefono, 0.5f, 0.5f)

        helper.y += 12f
        helper.drawField("Finca", v.finca, 0f, 0.5f)
        helper.drawField("Vereda", v.vereda, 0.5f, 0.5f)

        helper.y += 12f
        helper.drawField("Corregimiento", v.corregimiento, 0f, 0.5f)
        helper.drawField("Cuenca", v.cuenca, 0.5f, 0.5f)

        helper.y += 12f
        helper.drawField("Hora Inicio", v.hora_inicio, 0f, 0.5f)
        helper.drawField("Hora Fin", v.hora_fin, 0.5f, 0.5f)

        helper.y += 18f

        // 2. Motivo y Huerta
        helper.drawSectionHeader("2. Motivos de Acompañamiento y Tipo de Huerta")
        helper.drawField("Objetivos", v.motivos.joinToString(", ").ifEmpty { "Ninguno" })
        helper.y += 12f
        helper.drawField("Tipos Huerta", v.tiposHuerta.joinToString(", ").ifEmpty { "Ninguno" })
        helper.y += 12f
        helper.drawParagraph("Objetivo Específico de la Visita:", v.objetivo_visita.ifEmpty { "Sin registrar." })

        // 3. Actividades realizadas (Cultivos)
        helper.drawSectionHeader("3. Registro Técnico de Cultivos")
        if (v.cultivos.isEmpty()) {
            helper.drawParagraph(null, "No se registraron cultivos en esta visita.")
        } else {
            val headers = listOf("Categoría", "Tipo / Especie", "Área (m²)", "Prod (Kg)", "Observaciones")
            val weights = listOf(0.18f, 0.22f, 0.12f, 0.12f, 0.36f)
            
            helper.drawTableHeaders(headers, weights)
            for (c in v.cultivos) {
                val cells = listOf(
                    c.categoria,
                    "${c.tipo} / ${c.especie}",
                    c.areaM2.toString(),
                    c.produccionKg.toString(),
                    c.observaciones
                )
                helper.drawTableRow(cells, weights)
            }
        }

        helper.y += 12f
        val muestraSueloStr = if (v.muestra_suelo) "SÍ (Muestra #${v.numero_muestra})" else "NO"
        helper.drawField("¿Se tomó muestra de suelo?", muestraSueloStr)

        helper.y += 18f

        // 4. Materiales entregados
        helper.drawSectionHeader("4. Materiales Insumos Entregados")
        if (v.materiales.isEmpty()) {
            helper.drawParagraph(null, "No se entregaron materiales en esta visita.")
        } else {
            val headers = listOf("Insumo / Material", "Cantidad Entregada", "Unidad de Medida")
            val weights = listOf(0.5f, 0.25f, 0.25f)
            
            helper.drawTableHeaders(headers, weights)
            for (m in v.materiales) {
                val cells = listOf(
                    m.material,
                    m.cantidad.toString(),
                    m.unidad
                )
                helper.drawTableRow(cells, weights)
            }
        }

        helper.y += 12f

        // 5. Georreferenciación
        helper.drawSectionHeader("5. Georreferenciación y Área")
        helper.drawField("Latitud", v.latitud?.toString() ?: "N/A", 0f, 0.33f)
        helper.drawField("Longitud", v.longitud?.toString() ?: "N/A", 0.33f, 0.33f)
        helper.drawField("Altitud", v.altitud?.let { "${it} msnm" } ?: "N/A", 0.66f, 0.34f)
        
        helper.y += 12f
        helper.drawField("Área Total a Intervenir", v.area_intervenir?.let { "$it m²" } ?: "N/A")
        helper.y += 12f
        helper.drawParagraph("Observaciones de la ubicación:", v.observaciones_geo.orEmpty().ifEmpty { "Sin observaciones." })

        // 6. Recomendaciones generales
        helper.drawSectionHeader("6. Recomendaciones Técnicas Finales")
        helper.drawParagraph(null, v.recomendaciones.ifEmpty { "Sin recomendaciones técnicas registradas." })

        // 7. Corresponsabilidad
        helper.drawSectionHeader("7. Corresponsabilidad y Autorización")
        val legalText = "El productor declara que recibió la asistencia técnica, comprendió el procedimiento, acepta las recomendaciones, conoce los posibles riesgos y exonera de responsabilidad a la Alcaldía Local de Sumapaz, la ULATA y al profesional por las actuaciones realizadas."
        helper.drawParagraph(null, legalText)
        
        val corresponsabilidadAceptada = if (v.acepta_corresponsabilidad) "SÍ ACEPTA" else "NO ACEPTA"
        helper.drawField("Aceptación de corresponsabilidad", corresponsabilidadAceptada)
        helper.y += 12f
        helper.drawField("Recordatorio Próxima Visita", v.proxima_visita ?: "No programada")

        helper.y += 24f

        // 8. Firmas
        helper.drawSectionHeader("8. Firmas de Soporte")
        
        val startY = helper.y
        // Firma Profesional
        helper.drawSignature("PROFESIONAL ULATA", v.profesional, v.tarjeta_profesional, v.firma_profesional, 0f)
        
        // Firma Operario
        helper.y = startY
        helper.drawSignature("OPERARIO DE CAMPO", "Operario ULATA", v.cedula_operario, v.firma_operario, 0.35f)
        
        // Firma Usuario
        helper.y = startY
        helper.drawSignature("PRODUCTOR (USUARIO)", v.nombre, v.cedula_usuario, v.firma_usuario, 0.70f)

        // Completar PDF
        helper.finish()
        
        val fileOutputStream = FileOutputStream(file)
        document.writeTo(fileOutputStream)
        document.close()
        fileOutputStream.close()

        return file
    }

    private fun decodeBase64ToBitmap(base64Str: String?): Bitmap? {
        if (base64Str.isNullOrEmpty()) return null
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.NO_WRAP)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
