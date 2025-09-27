package com.SamiDev.agendasencilla.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de Acceso a Datos (DAO) para la entidad Contacto.
 * Define los métodos para interactuar con la tabla_contactos en la base de datos Room.
 */
@Dao
interface ContactoDao {

    /**
     * Inserta un nuevo contacto en la base de datos.
     * Si el contacto ya existe (conflicto de clave primaria), se reemplaza.
     * Esta operación se ejecuta en una corrutina.
     *
     * @param contacto El objeto [Contacto] a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarContacto(contacto: Contacto)

    /**
     * Actualiza un contacto existente en la base de datos.
     * Esta operación se ejecuta en una corrutina.
     *
     * @param contacto El objeto [Contacto] a actualizar.
     */
    @Update
    suspend fun actualizarContacto(contacto: Contacto)

    /**
     * Elimina un contacto de la base de datos.
     * Esta operación se ejecuta en una corrutina.
     *
     * @param contacto El objeto [Contacto] a eliminar.
     */
    @Delete
    suspend fun eliminarContacto(contacto: Contacto)

    /**
     * Obtiene un contacto específico por su ID.
     * Devuelve un [Flow] que emite el [Contacto] cuando cambia, o null si no se encuentra.
     *
     * @param id El ID del contacto a buscar.
     * @return Un Flow que emite el [Contacto] o null.
     */
    @Query("SELECT * FROM tabla_contactos WHERE id = :id")
    fun obtenerContactoPorId(id: Int): Flow<Contacto?>

    /**
     * Obtiene todos los contactos de la base de datos, ordenados alfabéticamente por nombreCompleto.
     * Devuelve un [Flow] que emite la lista de [Contacto] cada vez que los datos cambian.
     *
     * @return Un Flow que emite la lista de todos los contactos.
     */
    @Query("SELECT * FROM tabla_contactos ORDER BY nombreCompleto ASC")
    fun obtenerTodosLosContactos(): Flow<List<Contacto>>

    /**
     * Obtiene todos los contactos marcados como favoritos, ordenados alfabéticamente por nombreCompleto.
     * Devuelve un [Flow] que emite la lista de [Contacto] favoritos cada vez que los datos cambian.
     *
     * @return Un Flow que emite la lista de contactos favoritos.
     */
    @Query("SELECT * FROM tabla_contactos WHERE esFavorito = 1 ORDER BY nombreCompleto ASC")
    fun obtenerContactosFavoritos(): Flow<List<Contacto>>

    /**
     * Busca contactos cuyo nombreCompleto contenga el término de búsqueda proporcionado.
     * La búsqueda no distingue entre mayúsculas y minúsculas (LIKE con caracteres comodín %).
     * Devuelve un [Flow] que emite la lista de [Contacto] coincidentes, ordenados alfabéticamente.
     *
     * @param terminoBusqueda El texto a buscar en el nombre de los contactos.
     * @return Un Flow que emite la lista de contactos que coinciden con la búsqueda.
     */
    @Query("SELECT * FROM tabla_contactos WHERE nombreCompleto LIKE '%' || :terminoBusqueda || '%' ORDER BY nombreCompleto ASC")
    fun buscarContactosPorNombre(terminoBusqueda: String): Flow<List<Contacto>>

    /**
     * Obtiene un contacto específico por su número de teléfono.
     * Esta es una operación suspendida y debe llamarse desde una corrutina.
     * Se utiliza para verificar si un contacto con un número de teléfono dado ya existe en la base de datos.
     *
     * @param numeroTelefono El número de teléfono del contacto a buscar.
     * @return El [Contacto] si se encuentra, o null si no existe ninguno con ese número.
     */
    @Query("SELECT * FROM tabla_contactos WHERE numeroTelefono = :numeroTelefono LIMIT 1")
    suspend fun obtenerContactoPorNumero(numeroTelefono: String): Contacto?
}
