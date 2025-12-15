package com.SamiDev.agendasencilla.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoritoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun agregarFavorito(favorito: FavoritoEntity)

    @Query("DELETE FROM tabla_favoritos WHERE idContacto = :id")
    suspend fun eliminarFavorito(id: String)

    // Obtenemos solo la lista de IDs para compararla rápidamente con la lista del teléfono
    @Query("SELECT idContacto FROM tabla_favoritos")
    suspend fun obtenerIdsFavoritos(): List<String>
}