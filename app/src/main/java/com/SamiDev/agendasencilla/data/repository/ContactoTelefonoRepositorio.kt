package com.SamiDev.agendasencilla.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.SamiDev.agendasencilla.data.ContactoTelefono
import com.SamiDev.agendasencilla.data.database.FavoritoDao
import com.SamiDev.agendasencilla.data.database.FavoritoEntity
import com.SamiDev.agendasencilla.util.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repositorio encargado de gestionar los contactos.
 * Combina datos de los contactos nativos del dispositivo y la base de datos local de favoritos.
 *
 * @property context Contexto de la aplicación.
 * @property favoritoDao DAO para acceder a los favoritos locales.
 */
class ContactoTelefonoRepositorio(
    private val context: Context,
    private val favoritoDao: FavoritoDao
) {

    /**
     * Obtiene la lista completa de contactos del dispositivo, filtrando duplicados y números inválidos.
     * También verifica si cada contacto es "favorito" consultando la base de datos local.
     *
     * @return [Resultado] con la lista de [ContactoTelefono] o un error.
     */
    suspend fun obtenerContactosDelTelefono(): Resultado<List<ContactoTelefono>> {
        return withContext(Dispatchers.IO) {
            val listaFinal = mutableListOf<ContactoTelefono>()
            val controlDuplicados = mutableSetOf<String>()

            try {
                val idsFavoritos = favoritoDao.obtenerIdsFavoritos().toSet()
                val contentResolver = context.contentResolver
                val proyeccion = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI
                )

                val cursor = contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    proyeccion,
                    null,
                    null,
                    "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
                )

                cursor?.use { c ->
                    val indexId = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                    val indexNombre = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val indexNumero = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val indexFoto = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                    while (c.moveToNext()) {
                        val id = c.getString(indexId) ?: ""
                        val nombre = c.getString(indexNombre) ?: "Sin Nombre"
                        val numeroCrudo = c.getString(indexNumero) ?: ""
                        val foto = c.getString(indexFoto)

                        var numeroNormalizado = numeroCrudo.replace(Regex("[^0-9+]"), "")

                        if (numeroNormalizado.startsWith("+57")) {
                            numeroNormalizado = numeroNormalizado.substring(3)
                        } else if (numeroNormalizado.startsWith("57") && numeroNormalizado.length > 10) {
                            numeroNormalizado = numeroNormalizado.substring(2)
                        }
                        if (numeroNormalizado.length <= 6) {
                            continue
                        }

                        val claveUnica = "${id}_$numeroNormalizado"
                        if (controlDuplicados.contains(claveUnica)) {
                            continue
                        }
                        controlDuplicados.add(claveUnica)

                        val esFavoritoLocal = idsFavoritos.contains(id)

                        listaFinal.add(
                            ContactoTelefono(
                                id = id,
                                nombreCompleto = nombre,
                                numeroTelefono = numeroNormalizado,
                                fotoUri = foto,
                                esFavorito = esFavoritoLocal
                            )
                        )
                    }
                }

                if (listaFinal.isEmpty()) {
                    // Considerar registrar un evento o alerta silenciosa si es necesario
                }

                Resultado.Exito(listaFinal)

            } catch (e: Exception) {
                Resultado.Error("No se pudieron cargar los contactos.", e)
            }
        }
    }

    /**
     * Marca un contacto como favorito guardándolo en la base de datos local.
     */
    suspend fun marcarComoFavorito(idContacto: String) {
        withContext(Dispatchers.IO) {
            favoritoDao.agregarFavorito(FavoritoEntity(idContacto))
        }
    }

    /**
     * Desmarca un contacto como favorito eliminándolo de la base de datos local.
     */
    suspend fun desmarcarFavorito(idContacto: String) {
        withContext(Dispatchers.IO) {
            favoritoDao.eliminarFavorito(idContacto)
        }
    }

    /**
     * Busca un contacto específico por su ID en los contactos del dispositivo.
     * Recupera nombre, número y foto, y verifica si es favorito.
     *
     * @param idContacto ID del contacto a buscar.
     * @return [ContactoTelefono] si se encuentra, `null` en caso contrario.
     */
    suspend fun obtenerContactoPorId(idContacto: String): ContactoTelefono? {
        return withContext(Dispatchers.IO) {
            var contactoEncontrado: ContactoTelefono? = null

            val esFavorito = favoritoDao.obtenerIdsFavoritos().contains(idContacto)

            val contentResolver = context.contentResolver
            val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"
            val selectionArgs = arrayOf(idContacto)

            val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)

            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val indexNombre = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val indexNumero = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val indexFoto = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                    val nombre = c.getString(indexNombre) ?: "Sin Nombre"
                    val numero = c.getString(indexNumero) ?: ""
                    val foto = c.getString(indexFoto)

                    val numeroLimpio = numero.replace("+57", "").trim()

                    contactoEncontrado = ContactoTelefono(
                        id = idContacto,
                        nombreCompleto = nombre,
                        numeroTelefono = numeroLimpio,
                        fotoUri = foto,
                        esFavorito = esFavorito
                    )
                }
            }
            contactoEncontrado
        }
    }
}