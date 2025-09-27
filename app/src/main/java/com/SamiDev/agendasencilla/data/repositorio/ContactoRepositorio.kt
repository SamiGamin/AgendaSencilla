package com.SamiDev.agendasencilla.data.repositorio

import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.data.database.ContactoDao
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para gestionar las operaciones de datos de los Contactos.
 * Sirve como intermediario entre los ViewModels y la fuente de datos (ContactoDao).
 * Proporciona una API limpia para acceder y modificar los datos de los contactos.
 *
 * @property contactoDao El Data Access Object (DAO) para interactuar con la base de datos de contactos.
 */
class ContactoRepositorio(private val contactoDao: ContactoDao) {

    /**
     * Obtiene todos los contactos de la base de datos como un [Flow].
     * Los contactos se emiten ordenados alfabéticamente por nombre completo.
     *
     * @return Un [Flow] que emite una lista de [Contacto].
     */
    fun obtenerTodosLosContactos(): Flow<List<Contacto>> = contactoDao.obtenerTodosLosContactos()

    /**
     * Obtiene todos los contactos marcados como favoritos como un [Flow].
     * Los contactos favoritos se emiten ordenados alfabéticamente por nombre completo.
     *
     * @return Un [Flow] que emite una lista de [Contacto] favoritos.
     */
    fun obtenerContactosFavoritos(): Flow<List<Contacto>> = contactoDao.obtenerContactosFavoritos()

    /**
     * Obtiene un contacto específico por su ID como un [Flow].
     *
     * @param id El ID del contacto a buscar.
     * @return Un [Flow] que emite el [Contacto] correspondiente o null si no se encuentra.
     */
    fun obtenerContactoPorId(id: Int): Flow<Contacto?> = contactoDao.obtenerContactoPorId(id)

    /**
     * Obtiene un contacto específico por su número de teléfono.
     * Esta es una operación suspendida y debe llamarse desde una corrutina.
     * Se utiliza para verificar si un contacto con un número de teléfono dado ya existe en la base de datos.
     *
     * @param numeroTelefono El número de teléfono del contacto a buscar.
     * @return El [Contacto] si se encuentra, o null si no existe ninguno con ese número.
     */
    suspend fun obtenerContactoPorNumero(numeroTelefono: String): Contacto? {
        return contactoDao.obtenerContactoPorNumero(numeroTelefono)
    }

    /**
     * Busca contactos cuyo nombre completo contenga el término de búsqueda proporcionado.
     * La búsqueda es insensible a mayúsculas y minúsculas y los resultados se ordenan alfabéticamente.
     *
     * @param terminoBusqueda El texto a buscar en los nombres de los contactos.
     * @return Un [Flow] que emite una lista de [Contacto] que coinciden con la búsqueda.
     */
    fun buscarContactosPorNombre(terminoBusqueda: String): Flow<List<Contacto>> {
        return contactoDao.buscarContactosPorNombre(terminoBusqueda)
    }

    /**
     * Inserta un nuevo contacto en la base de datos.
     * Esta es una operación suspendida y debe llamarse desde una corrutina.
     *
     * @param contacto El [Contacto] a insertar.
     */
    suspend fun insertarContacto(contacto: Contacto) {
        contactoDao.insertarContacto(contacto)
    }

    /**
     * Actualiza un contacto existente en la base de datos.
     * Esta es una operación suspendida y debe llamarse desde una corrutina.
     *
     * @param contacto El [Contacto] a actualizar.
     */
    suspend fun actualizarContacto(contacto: Contacto) {
        contactoDao.actualizarContacto(contacto)
    }

    /**
     * Elimina un contacto de la base de datos.
     * Esta es una operación suspendida y debe llamarse desde una corrutina.
     *
     * @param contacto El [Contacto] a eliminar.
     */
    suspend fun eliminarContacto(contacto: Contacto) {
        contactoDao.eliminarContacto(contacto)
    }
}