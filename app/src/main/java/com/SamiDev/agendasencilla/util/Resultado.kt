package com.SamiDev.agendasencilla.util

/**
 * Clase sellada que representa el resultado de una operación.
 * Puede ser Exito, Error o Cargando.
 */
sealed class Resultado<out T> {
    data class Exito<out T>(val datos: T) : Resultado<T>()
    data class Error(val mensaje: String, val excepcion: Exception? = null) : Resultado<Nothing>()
    object Cargando : Resultado<Nothing>()
}