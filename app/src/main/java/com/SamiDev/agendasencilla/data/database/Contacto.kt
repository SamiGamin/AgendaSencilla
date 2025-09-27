package com.SamiDev.agendasencilla.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un contacto en la agenda.
 *
 * @property id Identificador único del contacto, generado automáticamente.
 * @property nombreCompleto Nombre completo del contacto.
 * @property numeroTelefono Número de teléfono del contacto.
 * @property fotoUri URI de la foto del contacto (puede ser una cadena vacía si no hay foto).
 * @property esFavorito Indica si el contacto está marcado como favorito.
 * @property notas Adicionales notas o recordatorios sobre el contacto.
 */
@Entity(tableName = "tabla_contactos")
data class Contacto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // El ID se genera automáticamente, por eso el valor por defecto es 0
    val nombreCompleto: String,
    val numeroTelefono: String,
    val fotoUri: String? = null, // Puede ser nulo si no hay foto
    val esFavorito: Boolean = false,
    val notas: String? = null // Notas adicionales, puede ser nulo
)
