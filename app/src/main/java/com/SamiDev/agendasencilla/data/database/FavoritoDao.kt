package com.SamiDev.agendasencilla.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object (DAO) para la tabla de favoritos.
 * Contiene los métodos para interactuar con la base de datos.
 */
@Dao
interface FavoritoDao {

    /**
     * Inserta un nuevo favorito en la base de datos.
     * Si ya existe, se ignora la operación.
     *
     * @param favorito La entidad [FavoritoEntity] a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun agregarFavorito(favorito: FavoritoEntity)

    /**
     * Elimina un favorito de la base de datos por su ID de contacto.
     *
     * @param id El ID del contacto a eliminar.
     */
    @Query("DELETE FROM tabla_favoritos WHERE idContacto = :id")
    suspend fun eliminarFavorito(id: String)

    /**
     * Obtiene la lista de IDs de todos los contactos favoritos.
     * Útil para comparaciones rápidas con la lista de contactos del dispositivo.
     *
     * @return Una lista de Strings con los IDs de los favoritos.
     */
    @Query("SELECT idContacto FROM tabla_favoritos")
    suspend fun obtenerIdsFavoritos(): List<String>
}