package com.SamiDev.agendasencilla.data.repository

import android.content.Context
import android.database.Cursor
import android.provider.CallLog
import com.SamiDev.agendasencilla.data.database.LlamadaLog

/**
 * Repositorio para acceder y gestionar el historial de llamadas del dispositivo.
 *
 * @property context Contexto de la aplicación.
 */
class LlamadasRepositorio(val context: Context) {

    /**
     * Obtiene el historial de llamadas del dispositivo, con opción de filtrado por tipo.
     *
     * @param filtroTipo Tipo de llamada para filtrar (opcional). Use constantes de [CallLog.Calls] (e.g., INCOMING_TYPE).
     * @return Lista de objetos [LlamadaLog] con la información de las llamadas.
     */
    fun obtenerHistorialLlamadas(filtroTipo: Int? = null): List<LlamadaLog> {
        val listaLlamadas = mutableListOf<LlamadaLog>()

        val proyeccion = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )

        var seleccion: String? = null
        var argumentos: Array<String>? = null

        if (filtroTipo != null) {
            seleccion = "${CallLog.Calls.TYPE} = ?"
            argumentos = arrayOf(filtroTipo.toString())
        }

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
                    val numero = it.getString(idxNumero) ?: ""
                    val nombre = it.getString(idxNombre) ?: "Desconocido"
                    val tipo = it.getInt(idxTipo)
                    val fechaLong = it.getLong(idxFecha)
                    val duracionSeg = it.getString(idxDuracion) ?: "0"

                    listaLlamadas.add(
                        LlamadaLog(numero, nombre, tipo, fechaLong, formatearDuracion(duracionSeg))
                    )
                }
            }
        } catch (e: SecurityException) {
            // Manejo silencioso de error de permisos o log a sistema de crash reporting
        }

        return listaLlamadas
    }

    private fun formatearDuracion(segundos: String): String {
        val seg = segundos.toIntOrNull() ?: 0
        return if (seg < 60) "${seg}s" else "${seg / 60}m ${seg % 60}s"
    }
}