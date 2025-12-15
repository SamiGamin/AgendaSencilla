package com.SamiDev.agendasencilla.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad que representa un favorito en la base de datos de Room.
 * Almacena el ID del contacto marcado como favorito.
 *
 * @property idContacto ID único del contacto asociado en la agenda del dispositivo.
 */
@Entity(tableName = "tabla_favoritos")
data class FavoritoEntity(
    @PrimaryKey
    val idContacto: String
)