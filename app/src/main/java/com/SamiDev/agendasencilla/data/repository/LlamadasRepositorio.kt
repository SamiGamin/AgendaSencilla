package com.SamiDev.agendasencilla.data.repository

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import com.SamiDev.agendasencilla.data.database.LlamadaLog

class LlamadasRepositorio(val context: Context) {fun obtenerHistorialLlamadas(filtroTipo: Int? = null): List<LlamadaLog> {
    val listaLlamadas = mutableListOf<LlamadaLog>()

    // 1. Definir qué columnas queremos leer
    val proyeccion = arrayOf(
        CallLog.Calls.NUMBER,
        CallLog.Calls.CACHED_NAME,
        CallLog.Calls.TYPE,
        CallLog.Calls.DATE,
        CallLog.Calls.DURATION
    )

    // 2. Configurar el filtro (WHERE)
    var seleccion: String? = null
    var argumentos: Array<String>? = null

    if (filtroTipo != null) {
        seleccion = "${CallLog.Calls.TYPE} = ?"
        argumentos = arrayOf(filtroTipo.toString())
    }

    // 3. Ordenar por fecha (más reciente primero)
    val orden = "${CallLog.Calls.DATE} DESC"

    try {
        val cursor: Cursor? = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            proyeccion,
            seleccion,
            argumentos,
            orden
        )

        cursor?.use {
            val idxNumero = it.getColumnIndex(CallLog.Calls.NUMBER)
            val idxNombre = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val idxTipo = it.getColumnIndex(CallLog.Calls.TYPE)
            val idxFecha = it.getColumnIndex(CallLog.Calls.DATE)
            val idxDuracion = it.getColumnIndex(CallLog.Calls.DURATION)

            while (it.moveToNext()) {
                val numero = it.getString(idxNumero)
                val nombre = it.getString(idxNombre) ?: "Desconocido"
                val tipo = it.getInt(idxTipo)
                val fechaLong = it.getLong(idxFecha)
                val duracionSeg = it.getString(idxDuracion)

                listaLlamadas.add(
                    LlamadaLog(numero, nombre, tipo, fechaLong, formatearDuracion(duracionSeg))
                )
            }
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
        // Aquí deberías manejar si no hay permisos
    }

    return listaLlamadas
}

    private fun formatearDuracion(segundos: String): String {
        val seg = segundos.toIntOrNull() ?: 0
        return if (seg < 60) "${seg}s" else "${seg / 60}m ${seg % 60}s"
    }
}