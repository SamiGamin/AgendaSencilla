package com.SamiDev.agendasencilla.data.database

/**
 * Modelo de datos que representa una entrada en el historial de llamadas.
 *
 * @property numero Número de teléfono de la llamada.
 * @property nombre Nombre del contacto asociado (puede ser null si no está en la agenda).
 * @property tipo Tipo de llamada: 1 = Entrante, 2 = Saliente, 3 = Perdida.
 * @property fecha Fecha y hora de la llamada en milisegundos.
 * @property duracion Duración de la llamada formateada como String (ej: "02:15").
 */
data class LlamadaLog(
    val numero: String,
    val nombre: String?,
    val tipo: Int,
    val fecha: Long,
    val duracion: String
)