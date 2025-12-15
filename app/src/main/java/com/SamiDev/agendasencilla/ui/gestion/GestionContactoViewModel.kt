package com.SamiDev.agendasencilla.ui.gestion

import android.app.Application
import android.content.ContentProviderOperation
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.SamiDev.agendasencilla.data.ContactoTelefono
import com.SamiDev.agendasencilla.data.database.AppDatabase
import com.SamiDev.agendasencilla.data.repository.ContactoTelefonoRepositorio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
class GestionContactoViewModel(
    private val application: Application,
    private val repositorio: ContactoTelefonoRepositorio
) : ViewModel() {

    private val TAG = GestionContactoViewModel::class.java.simpleName

    private val _contactoCargado = MutableStateFlow<ContactoTelefono?>(null)
    val contactoCargado: StateFlow<ContactoTelefono?> = _contactoCargado.asStateFlow()

    private val _estadoGuardado = MutableStateFlow<Boolean?>(null)
    val estadoGuardado: StateFlow<Boolean?> = _estadoGuardado.asStateFlow()

    fun cargarContacto(id: String) {
        viewModelScope.launch {
            val contacto = repositorio.obtenerContactoPorId(id)
            _contactoCargado.value = contacto
        }
    }

    /**
     * Guarda cambios en el Sistema (Nombre/Teléfono) y en Local (Favorito)
     */
    fun guardarCambios(contacto: ContactoTelefono, nuevoNombre: String, nuevoTelefono: String, esFavorito: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Actualizar Favorito Localmente
                if (esFavorito) {
                    repositorio.marcarComoFavorito(contacto.id)
                } else {
                    repositorio.desmarcarFavorito(contacto.id)
                }

                // 2. Actualizar Nombre y Teléfono en el Sistema Android
                // Esto requiere permiso WRITE_CONTACTS
                val operations = ArrayList<ContentProviderOperation>()

                // Actualizar Nombre
                val whereNombre = "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?"
                val argsNombre = arrayOf(contacto.id, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)

                operations.add(
                    ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(whereNombre, argsNombre)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, nuevoNombre)
                        .build()
                )

                // Actualizar Teléfono
                val wherePhone = "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?"
                val argsPhone = arrayOf(contacto.id, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)

                operations.add(
                    ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(wherePhone, argsPhone)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, nuevoTelefono)
                        .build()
                )

                // Ejecutar lote
                application.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)

                _estadoGuardado.value = true

            } catch (e: Exception) {
                Log.e(TAG, "Error guardando contacto", e)
                _estadoGuardado.value = false
            }
        }
    }

    fun reiniciarEstadoGuardado() {
        _estadoGuardado.value = null
    }
}

@Suppress("UNCHECKED_CAST")
class GestionContactoViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GestionContactoViewModel::class.java)) {
            val database = AppDatabase.obtenerInstancia(application)
            val repositorio = ContactoTelefonoRepositorio(application.applicationContext, database.favoritoDao())
            return GestionContactoViewModel(application, repositorio) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}