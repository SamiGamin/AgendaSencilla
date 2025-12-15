package com.SamiDev.agendasencilla.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabla_favoritos")
data class FavoritoEntity(
    @PrimaryKey
    val idContacto: String // Guardamos el ID que nos da Android (ej: "1254")
)