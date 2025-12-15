package com.SamiDev.agendasencilla.data.preferencias

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Enum que define las opciones de tamaño de fuente disponibles.
 *
 * @property escala Factor de escala para el tamaño de la fuente.
 */
enum class OpcionTamanoFuente(val escala: Float) {
    NORMAL(0.1f),
    GRANDE(0.5f),
    MAS_GRANDE(1.0f)
}

/**
 * Gestor de preferencias de la aplicación.
 * Centraliza el acceso y modificación de las SharedPreferences.
 * Implementa el patrón Singleton para un acceso unificado.
 */
class PreferenciasManager private constructor(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFERENCIAS_NOMBRE, Context.MODE_PRIVATE)

    companion object {
        private const val PREFERENCIAS_NOMBRE = "preferencias_agenda"
        private const val PREF_TEMA_OSCURO = "pref_tema_oscuro"
        private const val PREF_TIENE_TEMA_GUARDADO = "pref_tiene_tema_guardado"
        private const val PREF_TAMANO_FUENTE = "pref_tamano_fuente"
        private const val PREF_LECTURA_EN_VOZ = "pref_lectura_en_voz_activada"

        @Volatile
        private var INSTANCE: PreferenciasManager? = null

        /**
         * Obtiene la instancia única del gestor de preferencias.
         *
         * @param context Contexto de la aplicación.
         * @return La instancia singleton de [PreferenciasManager].
         */
        fun getInstance(context: Context): PreferenciasManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferenciasManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val _lecturaEnVozActivadaFlow = MutableStateFlow(obtenerPreferenciaLecturaEnVoz())
    val lecturaEnVozActivadaFlow: StateFlow<Boolean> = _lecturaEnVozActivadaFlow.asStateFlow()

    /**
     * Guarda la preferencia del tema oscuro.
     *
     * @param esOscuro `true` si se activa el tema oscuro, `false` para claro.
     */
    fun guardarTemaOscuro(esOscuro: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_TEMA_OSCURO, esOscuro).apply()
        sharedPreferences.edit().putBoolean(PREF_TIENE_TEMA_GUARDADO, true).apply()
    }

    /**
     * Obtiene si el tema oscuro está activado.
     */
    fun obtenerTemaOscuro(): Boolean {
        return sharedPreferences.getBoolean(PREF_TEMA_OSCURO, false)
    }

    /**
     * Verifica si el usuario ya ha configurado una preferencia de tema previamente.
     */
    fun tienePreferenciaTemaGuardada(): Boolean {
        return sharedPreferences.getBoolean(PREF_TIENE_TEMA_GUARDADO, false)
    }

    /**
     * Elimina las preferencias relacionadas con el tema.
     */
    fun borrarPreferenciaTema() {
        sharedPreferences.edit().remove(PREF_TEMA_OSCURO).remove(PREF_TIENE_TEMA_GUARDADO).apply()
    }

    /**
     * Guarda la opción de tamaño de fuente seleccionada.
     *
     * @param opcion La opción [OpcionTamanoFuente] a guardar.
     */
    fun guardarOpcionTamanoFuente(opcion: OpcionTamanoFuente) {
        sharedPreferences.edit().putString(PREF_TAMANO_FUENTE, opcion.name).apply()
    }

    /**
     * Obtiene la opción de tamaño de fuente guardada.
     * Si no existe o es inválida, retorna [OpcionTamanoFuente.NORMAL].
     */
    fun obtenerOpcionTamanoFuente(): OpcionTamanoFuente {
        val nombreOpcion = sharedPreferences.getString(PREF_TAMANO_FUENTE, OpcionTamanoFuente.NORMAL.name)
        return try {
            OpcionTamanoFuente.valueOf(nombreOpcion ?: OpcionTamanoFuente.NORMAL.name)
        } catch (e: IllegalArgumentException) {
            OpcionTamanoFuente.NORMAL
        }
    }

    /**
     * Guarda la preferencia de lectura en voz alta.
     * Actualiza el StateFlow para notificar a los observadores.
     *
     * @param activada `true` para activar, `false` para desactivar.
     */
    fun guardarPreferenciaLecturaEnVoz(activada: Boolean) {
        sharedPreferences.edit().putBoolean(PREF_LECTURA_EN_VOZ, activada).apply()
        _lecturaEnVozActivadaFlow.value = activada
    }

    /**
     * Obtiene la preferencia de lectura en voz alta.
     *
     * @return `true` si está activada, `false` por defecto.
     */
    fun obtenerPreferenciaLecturaEnVoz(): Boolean {
        return sharedPreferences.getBoolean(PREF_LECTURA_EN_VOZ, false)
    }
}