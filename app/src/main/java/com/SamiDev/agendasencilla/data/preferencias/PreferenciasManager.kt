package com.SamiDev.agendasencilla.data.preferencias

import android.content.Context
import android.content.SharedPreferences

/**
 * Enum para representar las opciones de tamaño de fuente disponibles.
 */
enum class OpcionTamanoFuente {
    NORMAL,
    GRANDE,
    MAS_GRANDE
}

/**
 * Gestiona el acceso a las SharedPreferences de la aplicación.
 * Permite guardar y recuperar configuraciones persistentes del usuario.
 *
 * @property contexto El contexto de la aplicación, necesario para acceder a SharedPreferences.
 */
class PreferenciasManager(private val contexto: Context) {

    private val preferencias: SharedPreferences
        get() = contexto.getSharedPreferences(NOMBRE_ARCHIVO_PREFERENCIAS, Context.MODE_PRIVATE)

    // --- Preferencias de Tema ---
    /**
     * Guarda el estado de la preferencia del tema oscuro.
     * Al guardar explícitamente, se establece una preferencia del usuario.
     *
     * @param habilitado True si el tema oscuro debe estar habilitado, false en caso contrario.
     */
    fun guardarTemaOscuro(habilitado: Boolean) {
        preferencias.edit().putBoolean(CLAVE_TEMA_OSCURO, habilitado).apply()
    }

    /**
     * Obtiene el estado actual de la preferencia del tema oscuro guardada por el usuario.
     *
     * @return True si el tema oscuro está habilitado según la preferencia explícita del usuario, 
     *         false por defecto si no se ha guardado una preferencia explícita.
     */
    fun obtenerTemaOscuro(): Boolean {
        return preferencias.getBoolean(CLAVE_TEMA_OSCURO, false) 
    }

    /**
     * Verifica si el usuario ha guardado explícitamente una preferencia para el tema.
     *
     * @return True si la preferencia existe, false en caso contrario.
     */
    fun tienePreferenciaTemaGuardada(): Boolean {
        return preferencias.contains(CLAVE_TEMA_OSCURO)
    }

    /**
     * Elimina la preferencia explícita del tema guardada por el usuario.
     * Esto permite que la aplicación vuelva a seguir la configuración del sistema.
     */
    fun borrarPreferenciaTema() {
        preferencias.edit().remove(CLAVE_TEMA_OSCURO).apply()
    }

    // --- Preferencias de Tamaño de Fuente ---
    /**
     * Guarda la opción de tamaño de fuente seleccionada por el usuario.
     *
     * @param opcion La [OpcionTamanoFuente] seleccionada.
     */
    fun guardarOpcionTamanoFuente(opcion: OpcionTamanoFuente) {
        preferencias.edit().putString(CLAVE_TAMANO_FUENTE, opcion.name).apply()
    }

    /**
     * Obtiene la opción de tamaño de fuente guardada por el usuario.
     *
     * @return La [OpcionTamanoFuente] guardada, o [OpcionTamanoFuente.NORMAL] si no hay ninguna guardada.
     */
    fun obtenerOpcionTamanoFuente(): OpcionTamanoFuente {
        val nombreOpcion = preferencias.getString(CLAVE_TAMANO_FUENTE, OpcionTamanoFuente.NORMAL.name)
        return try {
            OpcionTamanoFuente.valueOf(nombreOpcion ?: OpcionTamanoFuente.NORMAL.name)
        } catch (e: IllegalArgumentException) {
            OpcionTamanoFuente.NORMAL // En caso de un valor inválido guardado, volver al normal
        }
    }

    companion object {
        private const val NOMBRE_ARCHIVO_PREFERENCIAS = "preferencias_agenda_sencilla"

        // Claves para las preferencias de TEMA
        const val CLAVE_TEMA_OSCURO = "clave_tema_explicito_usuario"

        // Claves para las preferencias de TAMAÑO DE FUENTE
        const val CLAVE_TAMANO_FUENTE = "clave_tamano_fuente"
    }
}
