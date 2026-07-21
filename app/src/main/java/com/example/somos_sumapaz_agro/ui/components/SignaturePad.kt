package com.example.somos_sumapaz_agro.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import java.io.ByteArrayOutputStream

data class DrawingStroke(val points: List<Offset>)

@Composable
fun SignaturePad(
    label: String,
    onSignatureSaved: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val strokes = remember { mutableStateListOf<DrawingStroke>() }
    var currentStrokePoints = remember { mutableStateListOf<Offset>() }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var hasSigned by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentStrokePoints.clear()
                                currentStrokePoints.add(offset)
                                strokes.add(DrawingStroke(currentStrokePoints.toList()))
                                hasSigned = true
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                currentStrokePoints.add(change.position)
                                if (strokes.isNotEmpty()) {
                                    strokes[strokes.size - 1] = DrawingStroke(currentStrokePoints.toList())
                                }
                            },
                            onDragEnd = {
                                currentStrokePoints = mutableStateListOf()
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    for (stroke in strokes) {
                        val path = androidx.compose.ui.graphics.Path()
                        if (stroke.points.isNotEmpty()) {
                            path.moveTo(stroke.points[0].x, stroke.points[0].y)
                            for (i in 1 until stroke.points.size) {
                                path.lineTo(stroke.points[i].x, stroke.points[i].y)
                            }
                            drawPath(
                                path = path,
                                color = Color.Black,
                                style = Stroke(
                                    width = 4f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }

                if (!hasSigned) {
                    Text(
                        text = "Dibuje su firma aquí con el dedo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        strokes.clear()
                        currentStrokePoints.clear()
                        hasSigned = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Limpiar")
                }

                Button(
                    onClick = {
                        if (hasSigned && canvasSize.width > 0 && canvasSize.height > 0) {
                            val base64 = exportToBitmap(strokes, canvasSize.width, canvasSize.height)
                            if (base64 != null) {
                                onSignatureSaved(base64)
                            }
                        }
                    },
                    enabled = hasSigned,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Confirmar Firma")
                }
            }
        }
    }
}

private fun exportToBitmap(strokes: List<DrawingStroke>, width: Int, height: Int): String? {
    if (width <= 0 || height <= 0) return null
    try {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 6f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }

        for (stroke in strokes) {
            val path = AndroidPath()
            if (stroke.points.isNotEmpty()) {
                path.moveTo(stroke.points[0].x, stroke.points[0].y)
                for (i in 1 until stroke.points.size) {
                    path.lineTo(stroke.points[i].x, stroke.points[i].y)
                }
                canvas.drawPath(path, paint)
            }
        }

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP) // Use NO_WRAP to keep it in a single line
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}
