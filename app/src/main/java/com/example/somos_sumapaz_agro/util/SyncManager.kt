package com.example.somos_sumapaz_agro.util

import android.content.Context
import android.util.Log
import com.example.somos_sumapaz_agro.db.VisitasDbHelper
import com.example.somos_sumapaz_agro.model.VisitaAgricola
import com.example.somos_sumapaz_agro.model.VisitaPecuaria
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Collections

object SyncManager {
    private const val TAG = "SyncManager"
    const val BASE_URL = "https://productorescampesinos.com/api"

    private val syncMutex = Mutex()
    private val activePecuariaSyncs = Collections.synchronizedSet(HashSet<Int>())
    private val activeAgricolaSyncs = Collections.synchronizedSet(HashSet<Int>())

    suspend fun uploadPecuaria(visita: VisitaPecuaria): Boolean = withContext(Dispatchers.IO) {
        if (visita.id > 0 && !activePecuariaSyncs.add(visita.id)) {
            Log.d(TAG, "Visita pecuaria ID ${visita.id} ya se está subiendo.")
            return@withContext false
        }
        try {
            val json = JSONObject().apply {
                put("fecha", visita.fecha)
                put("corregimiento", visita.corregimiento)
                put("vereda", visita.vereda)
                put("finca", visita.finca)
                put("cuenca", visita.cuenca)
                put("hora_inicio", visita.hora_inicio)
                put("hora_fin", visita.hora_fin)
                put("latitud", visita.latitud)
                put("longitud", visita.longitud)
                put("usuario", visita.usuario)
                put("primera_vez", visita.primera_vez)
                put("seguimiento", visita.seguimiento)
                put("fecha_visita_anterior", visita.fecha_visita_anterior)
                put("diagnostico", visita.diagnostico)
                put("procedimiento", visita.procedimiento)
                put("recomendaciones", visita.recomendaciones)
                put("acepta_corresponsabilidad", visita.acepta_corresponsabilidad)
                put("proxima_visita", visita.proxima_visita)
                put("profesional", visita.profesional)
                put("tarjeta_profesional", visita.tarjeta_profesional)
                put("cedula_operario", visita.cedula_operario)
                put("cedula_usuario", visita.cedula_usuario)
                put("firma_profesional", visita.firma_profesional)
                put("firma_operario", visita.firma_operario)
                put("firma_usuario", visita.firma_usuario)
                put("especies", JSONArray(visita.especies))
            }

            val url = URL("$BASE_URL/submit_visita_pecuaria.php")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use { os ->
                os.write(json.toString())
            }

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Respuesta Pecuaria: $responseStr")
                val respJson = JSONObject(responseStr)
                return@withContext respJson.optBoolean("success", false)
            } else {
                Log.e(TAG, "Error HTTP Pecuaria: $responseCode")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al subir visita pecuaria", e)
            return@withContext false
        } finally {
            if (visita.id > 0) {
                activePecuariaSyncs.remove(visita.id)
            }
        }
    }

    suspend fun uploadAgricola(visita: VisitaAgricola): Boolean = withContext(Dispatchers.IO) {
        if (visita.id > 0 && !activeAgricolaSyncs.add(visita.id)) {
            Log.d(TAG, "Visita agrícola ID ${visita.id} ya se está subiendo.")
            return@withContext false
        }
        try {
            val json = JSONObject().apply {
                put("fecha", visita.fecha)
                put("nombre", visita.nombre)
                put("finca", visita.finca)
                put("vereda", visita.vereda)
                put("corregimiento", visita.corregimiento)
                put("cuenca", visita.cuenca)
                put("telefono", visita.telefono)
                put("hora_inicio", visita.hora_inicio)
                put("hora_fin", visita.hora_fin)
                put("numero_registro", visita.numero_registro)
                put("objetivo_visita", visita.objetivo_visita)
                put("recomendaciones", visita.recomendaciones)
                put("muestra_suelo", visita.muestra_suelo)
                put("numero_muestra", visita.numero_muestra)
                put("latitud", visita.latitud)
                put("longitud", visita.longitud)
                put("altitud", visita.altitud)
                put("observaciones_geo", visita.observaciones_geo)
                put("area_intervenir", visita.area_intervenir)
                put("acepta_corresponsabilidad", visita.acepta_corresponsabilidad)
                put("proxima_visita", visita.proxima_visita)
                put("profesional", visita.profesional)
                put("tarjeta_profesional", visita.tarjeta_profesional)
                put("cedula_operario", visita.cedula_operario)
                put("cedula_usuario", visita.cedula_usuario)
                put("firma_profesional", visita.firma_profesional)
                put("firma_operario", visita.firma_operario)
                put("firma_usuario", visita.firma_usuario)
                put("motivos", JSONArray(visita.motivos))
                put("tiposHuerta", JSONArray(visita.tiposHuerta))

                val cultivosArray = JSONArray()
                for (cul in visita.cultivos) {
                    val culObj = JSONObject().apply {
                        put("categoria", cul.categoria)
                        put("tipo", cul.tipo)
                        put("especie", cul.especie)
                        put("areaM2", cul.areaM2)
                        put("produccionKg", cul.produccionKg)
                        put("observaciones", cul.observaciones)
                    }
                    cultivosArray.put(culObj)
                }
                put("cultivos", cultivosArray)

                val matArray = JSONArray()
                for (mat in visita.materiales) {
                    val matObj = JSONObject().apply {
                        put("material", mat.material)
                        put("cantidad", mat.cantidad)
                        put("unidad", mat.unidad)
                    }
                    matArray.put(matObj)
                }
                put("materiales", matArray)
            }

            val url = URL("$BASE_URL/submit_visita_agricola.php")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                doOutput = true
                connectTimeout = 15000
                readTimeout = 15000
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use { os ->
                os.write(json.toString())
            }

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                val responseStr = conn.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "Respuesta Agrícola: $responseStr")
                val respJson = JSONObject(responseStr)
                return@withContext respJson.optBoolean("success", false)
            } else {
                Log.e(TAG, "Error HTTP Agrícola: $responseCode")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al subir visita agrícola", e)
            return@withContext false
        } finally {
            if (visita.id > 0) {
                activeAgricolaSyncs.remove(visita.id)
            }
        }
    }

    suspend fun syncAllPending(context: Context, dbHelper: VisitasDbHelper, onResult: (syncedCount: Int) -> Unit = {}) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            withContext(Dispatchers.Main) { onResult(0) }
            return
        }

        if (syncMutex.isLocked) {
            Log.d(TAG, "Sincronización en curso. Omitiendo invocación repetida.")
            withContext(Dispatchers.Main) { onResult(0) }
            return
        }

        syncMutex.withLock {
            var syncedCount = 0

            // Pecuarias
            val pendingPecuarias = dbHelper.getUnsyncedVisitasPecuarias()
            for (visita in pendingPecuarias) {
                if (visita.synced) continue
                val success = uploadPecuaria(visita)
                if (success) {
                    dbHelper.markVisitaPecuariaSynced(visita.id)
                    syncedCount++
                }
            }

            // Agrícolas
            val pendingAgricolas = dbHelper.getUnsyncedVisitasAgricolas()
            for (visita in pendingAgricolas) {
                if (visita.synced) continue
                val success = uploadAgricola(visita)
                if (success) {
                    dbHelper.markVisitaAgricolaSynced(visita.id)
                    syncedCount++
                }
            }

            withContext(Dispatchers.Main) {
                onResult(syncedCount)
            }
        }
    }
}
