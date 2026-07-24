package com.example.somos_sumapaz_agro.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.somos_sumapaz_agro.model.*

class VisitasDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "SumapazAgro.db"
        const val DATABASE_VERSION = 2

        // Tablas Pecuarias
        const val TABLE_PEC_VISITA = "visitas_pecuarias"
        const val TABLE_PEC_ESPECIE = "visita_pecuaria_especies"

        // Tablas Agrícolas
        const val TABLE_AGR_VISITA = "visitas_agricolas"
        const val TABLE_AGR_MOTIVO = "motivos_visita_agricola"
        const val TABLE_AGR_HUERTA = "tipo_huerta"
        const val TABLE_AGR_CULTIVO = "cultivos_visita"
        const val TABLE_AGR_MATERIAL = "materiales_entregados"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Habilitar Llaves Foráneas
        db.execSQL("PRAGMA foreign_keys = ON;")

        // 1. Visitas Pecuarias
        db.execSQL("""
            CREATE TABLE $TABLE_PEC_VISITA (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fecha TEXT,
                corregimiento TEXT,
                vereda TEXT,
                finca TEXT,
                cuenca TEXT,
                hora_inicio TEXT,
                hora_fin TEXT,
                latitud REAL,
                longitud REAL,
                usuario TEXT,
                primera_vez INTEGER,
                seguimiento INTEGER,
                fecha_visita_anterior TEXT,
                diagnostico TEXT,
                procedimiento TEXT,
                recomendaciones TEXT,
                acepta_corresponsabilidad INTEGER,
                proxima_visita TEXT,
                profesional TEXT,
                tarjeta_profesional TEXT,
                cedula_operario TEXT,
                cedula_usuario TEXT,
                firma_profesional TEXT,
                firma_operario TEXT,
                firma_usuario TEXT,
                synced INTEGER DEFAULT 0
            );
        """.trimIndent())

        // 2. Visita Pecuaria Especies
        db.execSQL("""
            CREATE TABLE $TABLE_PEC_ESPECIE (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                visita_id INTEGER,
                especie TEXT,
                FOREIGN KEY(visita_id) REFERENCES $TABLE_PEC_VISITA(id) ON DELETE CASCADE
            );
        """.trimIndent())

        // 3. Visitas Agrícolas
        db.execSQL("""
            CREATE TABLE $TABLE_AGR_VISITA (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                fecha TEXT,
                nombre TEXT,
                finca TEXT,
                vereda TEXT,
                corregimiento TEXT,
                cuenca TEXT,
                telefono TEXT,
                hora_inicio TEXT,
                hora_fin TEXT,
                numero_registro TEXT,
                objetivo_visita TEXT,
                recomendaciones TEXT,
                muestra_suelo INTEGER,
                numero_muestra TEXT,
                latitud REAL,
                longitud REAL,
                altitud REAL,
                observaciones_geo TEXT,
                area_intervenir REAL,
                acepta_corresponsabilidad INTEGER,
                proxima_visita TEXT,
                profesional TEXT,
                tarjeta_profesional TEXT,
                cedula_operario TEXT,
                cedula_usuario TEXT,
                firma_profesional TEXT,
                firma_operario TEXT,
                firma_usuario TEXT,
                synced INTEGER DEFAULT 0
            );
        """.trimIndent())

        // 4. Motivos Visita Agrícola
        db.execSQL("""
            CREATE TABLE $TABLE_AGR_MOTIVO (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                visita_id INTEGER,
                motivo TEXT,
                FOREIGN KEY(visita_id) REFERENCES $TABLE_AGR_VISITA(id) ON DELETE CASCADE
            );
        """.trimIndent())

        // 5. Tipo Huerta Agrícola
        db.execSQL("""
            CREATE TABLE $TABLE_AGR_HUERTA (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                visita_id INTEGER,
                tipo_huerta TEXT,
                FOREIGN KEY(visita_id) REFERENCES $TABLE_AGR_VISITA(id) ON DELETE CASCADE
            );
        """.trimIndent())

        // 6. Cultivos Visita
        db.execSQL("""
            CREATE TABLE $TABLE_AGR_CULTIVO (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                visita_id INTEGER,
                categoria TEXT,
                tipo TEXT,
                especie TEXT,
                area_m2 REAL,
                produccion_kg REAL,
                observaciones TEXT,
                FOREIGN KEY(visita_id) REFERENCES $TABLE_AGR_VISITA(id) ON DELETE CASCADE
            );
        """.trimIndent())

        // 7. Materiales Entregados
        db.execSQL("""
            CREATE TABLE $TABLE_AGR_MATERIAL (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                visita_id INTEGER,
                material TEXT,
                cantidad REAL,
                unidad TEXT,
                FOREIGN KEY(visita_id) REFERENCES $TABLE_AGR_VISITA(id) ON DELETE CASCADE
            );
        """.trimIndent())
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE $TABLE_PEC_VISITA ADD COLUMN synced INTEGER DEFAULT 0;")
            } catch (e: Exception) {}
            try {
                db.execSQL("ALTER TABLE $TABLE_AGR_VISITA ADD COLUMN synced INTEGER DEFAULT 0;")
            } catch (e: Exception) {}
        } else {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_PEC_ESPECIE")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_PEC_VISITA")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_AGR_MATERIAL")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_AGR_CULTIVO")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_AGR_HUERTA")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_AGR_MOTIVO")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_AGR_VISITA")
            onCreate(db)
        }
    }

    // ==========================================
    // OPERACIONES PECUARIAS
    // ==========================================

    fun insertVisitaPecuaria(visita: VisitaPecuaria): Long {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            val values = ContentValues().apply {
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
                put("primera_vez", if (visita.primera_vez) 1 else 0)
                put("seguimiento", if (visita.seguimiento) 1 else 0)
                put("fecha_visita_anterior", visita.fecha_visita_anterior)
                put("diagnostico", visita.diagnostico)
                put("procedimiento", visita.procedimiento)
                put("recomendaciones", visita.recomendaciones)
                put("acepta_corresponsabilidad", if (visita.acepta_corresponsabilidad) 1 else 0)
                put("proxima_visita", visita.proxima_visita)
                put("profesional", visita.profesional)
                put("tarjeta_profesional", visita.tarjeta_profesional)
                put("cedula_operario", visita.cedula_operario)
                put("cedula_usuario", visita.cedula_usuario)
                put("firma_profesional", visita.firma_profesional)
                put("firma_operario", visita.firma_operario)
                put("firma_usuario", visita.firma_usuario)
                put("synced", if (visita.synced) 1 else 0)
            }
            val id = db.insert(TABLE_PEC_VISITA, null, values)
            if (id != -1L) {
                // Insertar Especies
                for (especie in visita.especies) {
                    val espValues = ContentValues().apply {
                        put("visita_id", id)
                        put("especie", especie)
                    }
                    db.insert(TABLE_PEC_ESPECIE, null, espValues)
                }
            }
            db.setTransactionSuccessful()
            return id
        } finally {
            db.endTransaction()
        }
    }

    fun markVisitaPecuariaSynced(id: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply { put("synced", 1) }
        db.update(TABLE_PEC_VISITA, values, "id = ?", arrayOf(id.toString()))
    }

    fun getAllVisitasPecuarias(): List<VisitaPecuaria> {
        val list = mutableListOf<VisitaPecuaria>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_PEC_VISITA ORDER BY id DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                
                // Obtener especies
                val especies = mutableListOf<String>()
                val espCursor = db.rawQuery("SELECT especie FROM $TABLE_PEC_ESPECIE WHERE visita_id = ?", arrayOf(id.toString()))
                if (espCursor.moveToFirst()) {
                    do {
                        especies.add(espCursor.getString(espCursor.getColumnIndexOrThrow("especie")))
                    } while (espCursor.moveToNext())
                }
                espCursor.close()

                val syncedColIdx = cursor.getColumnIndex("synced")
                val isSynced = if (syncedColIdx != -1) cursor.getInt(syncedColIdx) == 1 else false

                val visita = VisitaPecuaria(
                    id = id,
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha")),
                    corregimiento = cursor.getString(cursor.getColumnIndexOrThrow("corregimiento")),
                    vereda = cursor.getString(cursor.getColumnIndexOrThrow("vereda")),
                    finca = cursor.getString(cursor.getColumnIndexOrThrow("finca")),
                    cuenca = cursor.getString(cursor.getColumnIndexOrThrow("cuenca")),
                    hora_inicio = cursor.getString(cursor.getColumnIndexOrThrow("hora_inicio")),
                    hora_fin = cursor.getString(cursor.getColumnIndexOrThrow("hora_fin")),
                    latitud = if (cursor.isNull(cursor.getColumnIndexOrThrow("latitud"))) null else cursor.getDouble(cursor.getColumnIndexOrThrow("latitud")),
                    longitud = if (cursor.isNull(cursor.getColumnIndexOrThrow("longitud"))) null else cursor.getDouble(cursor.getColumnIndexOrThrow("longitud")),
                    usuario = cursor.getString(cursor.getColumnIndexOrThrow("usuario")),
                    primera_vez = cursor.getInt(cursor.getColumnIndexOrThrow("primera_vez")) == 1,
                    seguimiento = cursor.getInt(cursor.getColumnIndexOrThrow("seguimiento")) == 1,
                    fecha_visita_anterior = cursor.getString(cursor.getColumnIndexOrThrow("fecha_visita_anterior")),
                    diagnostico = cursor.getString(cursor.getColumnIndexOrThrow("diagnostico")),
                    procedimiento = cursor.getString(cursor.getColumnIndexOrThrow("procedimiento")),
                    recomendaciones = cursor.getString(cursor.getColumnIndexOrThrow("recomendaciones")),
                    acepta_corresponsabilidad = cursor.getInt(cursor.getColumnIndexOrThrow("acepta_corresponsabilidad")) == 1,
                    proxima_visita = cursor.getString(cursor.getColumnIndexOrThrow("proxima_visita")),
                    profesional = cursor.getString(cursor.getColumnIndexOrThrow("profesional")),
                    tarjeta_profesional = cursor.getString(cursor.getColumnIndexOrThrow("tarjeta_profesional")),
                    cedula_operario = cursor.getString(cursor.getColumnIndexOrThrow("cedula_operario")),
                    cedula_usuario = cursor.getString(cursor.getColumnIndexOrThrow("cedula_usuario")),
                    firma_profesional = cursor.getString(cursor.getColumnIndexOrThrow("firma_profesional")),
                    firma_operario = cursor.getString(cursor.getColumnIndexOrThrow("firma_operario")),
                    firma_usuario = cursor.getString(cursor.getColumnIndexOrThrow("firma_usuario")),
                    especies = especies,
                    synced = isSynced
                )
                list.add(visita)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getUnsyncedVisitasPecuarias(): List<VisitaPecuaria> {
        return getAllVisitasPecuarias().filter { !it.synced }
    }

    fun deleteVisitaPecuaria(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_PEC_VISITA, "id = ?", arrayOf(id.toString()))
    }

    // ==========================================
    // OPERACIONES AGRÍCOLAS
    // ==========================================

    fun insertVisitaAgricola(visita: VisitaAgricola): Long {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            val values = ContentValues().apply {
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
                put("muestra_suelo", if (visita.muestra_suelo) 1 else 0)
                put("numero_muestra", visita.numero_muestra)
                put("latitud", visita.latitud)
                put("longitud", visita.longitud)
                put("altitud", visita.altitud)
                put("observaciones_geo", visita.observaciones_geo)
                put("area_intervenir", visita.area_intervenir)
                put("acepta_corresponsabilidad", if (visita.acepta_corresponsabilidad) 1 else 0)
                put("proxima_visita", visita.proxima_visita)
                put("profesional", visita.profesional)
                put("tarjeta_profesional", visita.tarjeta_profesional)
                put("cedula_operario", visita.cedula_operario)
                put("cedula_usuario", visita.cedula_usuario)
                put("firma_profesional", visita.firma_profesional)
                put("firma_operario", visita.firma_operario)
                put("firma_usuario", visita.firma_usuario)
                put("synced", if (visita.synced) 1 else 0)
            }
            val id = db.insert(TABLE_AGR_VISITA, null, values)
            if (id != -1L) {
                // Insertar Motivos
                for (motivo in visita.motivos) {
                    val motValues = ContentValues().apply {
                        put("visita_id", id)
                        put("motivo", motivo)
                    }
                    db.insert(TABLE_AGR_MOTIVO, null, motValues)
                }

                // Insertar Tipos de Huerta
                for (huerta in visita.tiposHuerta) {
                    val hValues = ContentValues().apply {
                        put("visita_id", id)
                        put("tipo_huerta", huerta)
                    }
                    db.insert(TABLE_AGR_HUERTA, null, hValues)
                }

                // Insertar Cultivos
                for (cultivo in visita.cultivos) {
                    val culValues = ContentValues().apply {
                        put("visita_id", id)
                        put("categoria", cultivo.categoria)
                        put("tipo", cultivo.tipo)
                        put("especie", cultivo.especie)
                        put("area_m2", cultivo.areaM2)
                        put("produccion_kg", cultivo.produccionKg)
                        put("observaciones", cultivo.observaciones)
                    }
                    db.insert(TABLE_AGR_CULTIVO, null, culValues)
                }

                // Insertar Materiales
                for (material in visita.materiales) {
                    val matValues = ContentValues().apply {
                        put("visita_id", id)
                        put("material", material.material)
                        put("cantidad", material.cantidad)
                        put("unidad", material.unidad)
                    }
                    db.insert(TABLE_AGR_MATERIAL, null, matValues)
                }
            }
            db.setTransactionSuccessful()
            return id
        } finally {
            db.endTransaction()
        }
    }

    fun markVisitaAgricolaSynced(id: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply { put("synced", 1) }
        db.update(TABLE_AGR_VISITA, values, "id = ?", arrayOf(id.toString()))
    }

    fun getAllVisitasAgricolas(): List<VisitaAgricola> {
        val list = mutableListOf<VisitaAgricola>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_AGR_VISITA ORDER BY id DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))

                // Obtener Motivos
                val motivos = mutableListOf<String>()
                val motCursor = db.rawQuery("SELECT motivo FROM $TABLE_AGR_MOTIVO WHERE visita_id = ?", arrayOf(id.toString()))
                if (motCursor.moveToFirst()) {
                    do {
                        motivos.add(motCursor.getString(motCursor.getColumnIndexOrThrow("motivo")))
                    } while (motCursor.moveToNext())
                }
                motCursor.close()

                // Obtener Huerta
                val tiposHuerta = mutableListOf<String>()
                val hCursor = db.rawQuery("SELECT tipo_huerta FROM $TABLE_AGR_HUERTA WHERE visita_id = ?", arrayOf(id.toString()))
                if (hCursor.moveToFirst()) {
                    do {
                        tiposHuerta.add(hCursor.getString(hCursor.getColumnIndexOrThrow("tipo_huerta")))
                    } while (hCursor.moveToNext())
                }
                hCursor.close()

                // Obtener Cultivos
                val cultivos = mutableListOf<CultivoVisita>()
                val culCursor = db.rawQuery("SELECT * FROM $TABLE_AGR_CULTIVO WHERE visita_id = ?", arrayOf(id.toString()))
                if (culCursor.moveToFirst()) {
                    do {
                        val cul = CultivoVisita(
                            id = culCursor.getInt(culCursor.getColumnIndexOrThrow("id")),
                            categoria = culCursor.getString(culCursor.getColumnIndexOrThrow("categoria")),
                            tipo = culCursor.getString(culCursor.getColumnIndexOrThrow("tipo")),
                            especie = culCursor.getString(culCursor.getColumnIndexOrThrow("especie")),
                            areaM2 = culCursor.getDouble(culCursor.getColumnIndexOrThrow("area_m2")),
                            produccionKg = culCursor.getDouble(culCursor.getColumnIndexOrThrow("produccion_kg")),
                            observaciones = culCursor.getString(culCursor.getColumnIndexOrThrow("observaciones"))
                        )
                        cultivos.add(cul)
                    } while (culCursor.moveToNext())
                }
                culCursor.close()

                // Obtener Materiales
                val materiales = mutableListOf<MaterialEntregado>()
                val matCursor = db.rawQuery("SELECT * FROM $TABLE_AGR_MATERIAL WHERE visita_id = ?", arrayOf(id.toString()))
                if (matCursor.moveToFirst()) {
                    do {
                        val mat = MaterialEntregado(
                            id = matCursor.getInt(matCursor.getColumnIndexOrThrow("id")),
                            material = matCursor.getString(matCursor.getColumnIndexOrThrow("material")),
                            cantidad = matCursor.getDouble(matCursor.getColumnIndexOrThrow("cantidad")),
                            unidad = matCursor.getString(matCursor.getColumnIndexOrThrow("unidad"))
                        )
                        materiales.add(mat)
                    } while (matCursor.moveToNext())
                }
                matCursor.close()

                val syncedColIdx = cursor.getColumnIndex("synced")
                val isSynced = if (syncedColIdx != -1) cursor.getInt(syncedColIdx) == 1 else false

                val visita = VisitaAgricola(
                    id = id,
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha")),
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                    finca = cursor.getString(cursor.getColumnIndexOrThrow("finca")),
                    vereda = cursor.getString(cursor.getColumnIndexOrThrow("vereda")),
                    corregimiento = cursor.getString(cursor.getColumnIndexOrThrow("corregimiento")),
                    cuenca = cursor.getString(cursor.getColumnIndexOrThrow("cuenca")),
                    telefono = cursor.getString(cursor.getColumnIndexOrThrow("telefono")),
                    hora_inicio = cursor.getString(cursor.getColumnIndexOrThrow("hora_inicio")),
                    hora_fin = cursor.getString(cursor.getColumnIndexOrThrow("hora_fin")),
                    numero_registro = cursor.getString(cursor.getColumnIndexOrThrow("numero_registro")),
                    objetivo_visita = cursor.getString(cursor.getColumnIndexOrThrow("objetivo_visita")),
                    recomendaciones = cursor.getString(cursor.getColumnIndexOrThrow("recomendaciones")),
                    muestra_suelo = cursor.getInt(cursor.getColumnIndexOrThrow("muestra_suelo")) == 1,
                    numero_muestra = cursor.getString(cursor.getColumnIndexOrThrow("numero_muestra")),
                    latitud = if (cursor.isNull(cursor.getColumnIndexOrThrow("latitud"))) null else cursor.getDouble(cursor.getColumnIndexOrThrow("latitud")),
                    longitud = if (cursor.isNull(cursor.getColumnIndexOrThrow("longitud"))) null else cursor.getDouble(cursor.getColumnIndexOrThrow("longitud")),
                    altitud = if (cursor.isNull(cursor.getColumnIndexOrThrow("altitud"))) null else cursor.getDouble(cursor.getColumnIndexOrThrow("altitud")),
                    observaciones_geo = cursor.getString(cursor.getColumnIndexOrThrow("observaciones_geo")),
                    area_intervenir = if (cursor.isNull(cursor.getColumnIndexOrThrow("area_intervenir"))) null else cursor.getDouble(cursor.getColumnIndexOrThrow("area_intervenir")),
                    acepta_corresponsabilidad = cursor.getInt(cursor.getColumnIndexOrThrow("acepta_corresponsabilidad")) == 1,
                    proxima_visita = cursor.getString(cursor.getColumnIndexOrThrow("proxima_visita")),
                    profesional = cursor.getString(cursor.getColumnIndexOrThrow("profesional")),
                    tarjeta_profesional = cursor.getString(cursor.getColumnIndexOrThrow("tarjeta_profesional")),
                    cedula_operario = cursor.getString(cursor.getColumnIndexOrThrow("cedula_operario")),
                    cedula_usuario = cursor.getString(cursor.getColumnIndexOrThrow("cedula_usuario")),
                    firma_profesional = cursor.getString(cursor.getColumnIndexOrThrow("firma_profesional")),
                    firma_operario = cursor.getString(cursor.getColumnIndexOrThrow("firma_operario")),
                    firma_usuario = cursor.getString(cursor.getColumnIndexOrThrow("firma_usuario")),
                    motivos = motivos,
                    tiposHuerta = tiposHuerta,
                    cultivos = cultivos,
                    materiales = materiales,
                    synced = isSynced
                )
                list.add(visita)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getUnsyncedVisitasAgricolas(): List<VisitaAgricola> {
        return getAllVisitasAgricolas().filter { !it.synced }
    }

    fun deleteVisitaAgricola(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_AGR_VISITA, "id = ?", arrayOf(id.toString()))
    }
}
