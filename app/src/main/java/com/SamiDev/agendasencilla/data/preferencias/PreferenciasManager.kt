package com.SamiDev.agendasencilla.data.preferencias

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Definir el enum para las opciones de tamaño de fuente
enum class OpcionTamanoFuente(val escala: Float) {
    NORMAL(0.1f),
    GRANDE(0.5f),
    MAS_GRANDE(1.0f)
}

/**
 * Gestor de preferencias de la aplicación.
 * Centraliza el acceso y modificación de las SharedPreferences.
 */
class PreferenciasManager private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFERENCIAS_NOMBRE, Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "PreferenciasManager"
        private const val PREFERENCIAS_NOMBRE = "preferencias_agenda"
        private const val PREF_TEMA_OSCURO = "pref_tema_oscuro"
        private const val PREF_TIENE_TEMA_GUARDADO = "pref_tiene_tema_guardado"
        private const val PREF_TAMANO_FUENTE = "pref_tamano_fuente"
        private const val PREF_LECTURA_EN_VOZ = "pref_lectura_en_voz_activada"

        @Volatile
        private var INSTANCE: PreferenciasManager? = null

        fun getInstance(context: Context): PreferenciasManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferenciasManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    // Flow para la preferencia de lectura en voz
    private val _lecturaEnVozActivadaFlow = MutableStateFlow(obtenerPreferenciaLecturaEnVoz())
    val lecturaEnVozActivadaFlow: StateFlow<Boolean> = _lecturaEnVozActivadaFlow.asStateFlow()

    // --- Preferencias de Tema ---

    fun guardarTemaOscuro(esOscuro: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_TEMA_OSCURO, esOscuro).apply()
        sharedPreferences.edit().putBoolean(PREF_TIENE_TEMA_GUARDADO, true).apply()
    }

    fun obtenerTemaOscuro(): Boolean {
        return sharedPreferences.getBoolean(PREF_TEMA_OSCURO, false)
    }

    fun tienePreferenciaTemaGuardada(): Boolean {
        return sharedPreferences.getBoolean(PREF_TIENE_TEMA_GUARDADO, false)
    }

    fun borrarPreferenciaTema() {
        sharedPreferences.edit().remove(PREF_TEMA_OSCURO).remove(PREF_TIENE_TEMA_GUARDADO).apply()
    }

    // --- Preferencias de Tamaño de Fuente ---

    fun guardarOpcionTamanoFuente(opcion: OpcionTamanoFuente) {
        sharedPreferences.edit().putString(PREF_TAMANO_FUENTE, opcion.name).apply()
    }

    fun obtenerOpcionTamanoFuente(): OpcionTamanoFuente {
        val nombreOpcion = sharedPreferences.getString(PREF_TAMANO_FUENTE, OpcionTamanoFuente.NORMAL.name)
        return try {
            OpcionTamanoFuente.valueOf(nombreOpcion ?: OpcionTamanoFuente.NORMAL.name)
        } catch (e: IllegalArgumentException) {
            OpcionTamanoFuente.NORMAL
        }
    }

    // --- Preferencias de Lectura en Voz ---

    /**
     * Guarda la preferencia del usuario para activar o desactivar la lectura en voz alta.
     */
    fun guardarPreferenciaLecturaEnVoz(activada: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_LECTURA_EN_VOZ, activada).apply()
        _lecturaEnVozActivadaFlow.value = activada // Actualizar el StateFlow
        Log.d(TAG, "Preferencia de lectura en voz guardada: $activada")
    }

    /**
     * Obtiene el estado actual de la preferencia de lectura en voz alta.
     * Por defecto, estará desactivada.
     */
    fun obtenerPreferenciaLecturaEnVoz(): Boolean {
        val activada = sharedPreferences.getBoolean(PREF_LECTURA_EN_VOZ, false)
        Log.d(TAG, "Preferencia de lectura en voz obtenida: $activada")
        return activada
    }
}