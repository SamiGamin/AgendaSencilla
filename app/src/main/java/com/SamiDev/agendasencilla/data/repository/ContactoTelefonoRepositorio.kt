package com.SamiDev.agendasencilla.data.repository

import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.SamiDev.agendasencilla.data.ContactoTelefono
import com.SamiDev.agendasencilla.data.database.FavoritoDao
import com.SamiDev.agendasencilla.data.database.FavoritoEntity
import com.SamiDev.agendasencilla.util.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactoTelefonoRepositorio(
    private val context: Context,
    private val favoritoDao: FavoritoDao
)  {
    // Función suspendida para no bloquear el hilo principal
    private val TAG = ContactoTelefonoRepositorio::class.java.simpleName

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
                                // Guardamos el número limpio o el original según prefieras.
                                // Aquí guardo el normalizado para que se vea limpio en la UI.
                                numeroTelefono = numeroNormalizado,
                                fotoUri = foto,
                                esFavorito = esFavoritoLocal
                            )
                        )
                    }
                }

                if (listaFinal.isEmpty()) {
                    Log.w(TAG, "La consulta no devolvió contactos o todos fueron filtrados.")
                }

                Resultado.Exito(listaFinal)

            } catch (e: Exception) {
                Log.e(TAG, "Error consultando contactos", e)
                Resultado.Error("No se pudieron cargar los contactos.", e)
            }
        }
    }
    suspend fun marcarComoFavorito(idContacto: String) {
        withContext(Dispatchers.IO) {
            favoritoDao.agregarFavorito(FavoritoEntity(idContacto))
        }
    }

    suspend fun desmarcarFavorito(idContacto: String) {
        withContext(Dispatchers.IO) {
            favoritoDao.eliminarFavorito(idContacto)
        }
    }
    suspend fun obtenerContactoPorId(idContacto: String): ContactoTelefono? {
        return withContext(Dispatchers.IO) {
            var contactoEncontrado: ContactoTelefono? = null

            // 1. Verificar si es favorito localmente
//            val esFavorito = favoritoDao.esFavorito(idContacto) // Necesitarás agregar esta fun al DAO o usar obtenerIdsFavoritos().contains(id)
            // Forma rápida si no quieres modificar el DAO ahora:
            val esFavorito = favoritoDao.obtenerIdsFavoritos().contains(idContacto)

            // 2. Consultar al sistema
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

                    // Limpieza básica del número para mostrar
                    val numeroLimpio = numero.replace("+57", "").trim()

                    contactoEncontrado = ContactoTelefono(
                        id = idContacto,
                        nombreCompleto = nombre,
                        numeroTelefono = numeroLimpio,
                        fotoUri = foto,
                        esFavorito = esFavorito // Usamos el valor booleano real
                    )
                }
            }
            contactoEncontrado
        }
    }
}