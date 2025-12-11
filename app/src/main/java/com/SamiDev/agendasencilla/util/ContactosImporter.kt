package com.SamiDev.agendasencilla.util

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.data.repository.ContactoRepositorio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactosImporter(
    private val context: Context,
    private val repository: ContactoRepositorio,
    private val onEstadoChanged: (String) -> Unit,  // Para mostrar mensajes (Snackbar/Toast)
    private val onImportacionEnCurso: (Boolean) -> Unit // Para habilitar/deshabilitar botón
) {
    /**
     * Intenta iniciar la importación.
     * Devuelve true si ya tiene permiso (para que el llamador lance la corrutina).
     * Si no tiene permiso, lanza el request o muestra mensaje.
     */
    fun intentarImportar(permisoLauncher: ActivityResultLauncher<String>? = null): Boolean {
        return when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED -> {
                true  // Tiene permiso → el llamador debe lanzar importarContactosDelDispositivo()
            }
            permisoLauncher != null -> {
                permisoLauncher.launch(Manifest.permission.READ_CONTACTS)
                false
            }
            else -> {
                onEstadoChanged("Permiso denegado: no se pueden importar contactos.")
                false
            }
        }
    }

    // Esta función SÍ puede seguir siendo suspend
    suspend fun importarContactosDelDispositivo() {
        withContext(Dispatchers.IO) {
            onImportacionEnCurso(true)
            try {
                val contactosImportados = leerContactosDelDispositivo()
                var agregados = 0

                contactosImportados.forEach { contacto ->
                    if (!repository.existeContactoPorTelefono(contacto.numeroTelefono)) {
                        repository.insertarContacto(contacto)
                        agregados++
                    }
                }

                withContext(Dispatchers.Main) {
                    onEstadoChanged("Importación completada: $agregados contactos nuevos agregados.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onEstadoChanged("Error al importar: ${e.localizedMessage}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    onImportacionEnCurso(false)
                }
            }
        }
    }
    private fun leerContactosDelDispositivo(): List<Contacto> {
        val contactos = mutableListOf<Contacto>()
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val nombre = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)) ?: "Sin nombre"
                var telefono = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)) ?: ""
                telefono = telefono.replace(Regex("[^\\d]"), "") // limpia todo lo que no sea dígito

                if (telefono.isNotBlank()) {
                    contactos.add(
                        Contacto(
                            nombreCompleto = nombre,
                            numeroTelefono = telefono,
                            fotoUri = null,
                            esFavorito = false,
                            notas = "Importado del dispositivo"
                        )
                    )
                }
            }
        }
        return contactos.distinctBy { it.numeroTelefono }
    }
}