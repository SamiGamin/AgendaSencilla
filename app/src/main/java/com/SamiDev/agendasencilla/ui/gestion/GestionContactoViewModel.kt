package com.SamiDev.agendasencilla.ui.gestion

import android.app.Application
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.SamiDev.agendasencilla.data.database.AppDatabase
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.data.repositorio.ContactoRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para el fragmento de gestión de contactos (crear/editar/importar).
 * Se encarga de la lógica de guardar, y ahora también de importar contactos.
 *
 * @property application La instancia de la aplicación, necesaria para acceder al ContentResolver.
 * @property contactoRepositorio El repositorio para acceder a los datos de los contactos.
 */
class GestionContactoViewModel(
    private val application: Application,
    private val contactoRepositorio: ContactoRepositorio
) : ViewModel() {

    private val _estadoImportacion = MutableStateFlow<String?>(null)
    val estadoImportacion: StateFlow<String?> = _estadoImportacion.asStateFlow()

    private val _importacionEnCurso = MutableStateFlow(false)
    val importacionEnCurso: StateFlow<Boolean> = _importacionEnCurso.asStateFlow()

    /**
     * Guarda un contacto (inserta uno nuevo o actualiza uno existente) en la base de datos.
     * Esta operación se ejecuta en el [viewModelScope] para asegurar que se realice en segundo plano.
     *
     * @param contacto El [Contacto] a guardar.
     */
    fun guardarContacto(contacto: Contacto) {
        viewModelScope.launch {
            if (contacto.id == 0) { // Si el ID es 0, es un nuevo contacto
                contactoRepositorio.insertarContacto(contacto)
            } else { // Si el ID no es 0, es un contacto existente que se actualiza
                contactoRepositorio.actualizarContacto(contacto)
            }
        }
    }

    /**
     * Inicia el proceso de importación de contactos desde el dispositivo.
     * Lee los contactos del ContentProvider del dispositivo, incluyendo su foto URI,
     * y los guarda en la base de datos Room si no existen previamente (verificando por número de teléfono).
     */
    fun importarContactosDelDispositivo() {
        viewModelScope.launch {
            _importacionEnCurso.value = true
            _estadoImportacion.value = "Importación iniciada..."
            var contadorContactosImportados = 0

            try {
                val contentResolver = application.contentResolver
                val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI // Añadido para obtener la URI de la foto
                )

                contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    null, 
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )?.use { cursor ->
                    val indiceNombre = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val indiceNumero = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val indiceFotoUri = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                    if (indiceNombre >= 0 && indiceNumero >= 0) { // No es estrictamente necesario verificar indiceFotoUri aquí, ya que puede ser null
                        while (cursor.moveToNext()) {
                            val nombre = cursor.getString(indiceNombre)
                            val numeroTelefono = cursor.getString(indiceNumero)
                            val fotoUriDesdeDispositivo = if (indiceFotoUri != -1) cursor.getString(indiceFotoUri) else null

                            if (nombre != null && numeroTelefono != null && numeroTelefono.isNotBlank()) {
                                val contactoExistente = contactoRepositorio.obtenerContactoPorNumero(numeroTelefono)
                                if (contactoExistente == null) {
                                    val nuevoContacto = Contacto(
                                        nombreCompleto = nombre,
                                        numeroTelefono = numeroTelefono,
                                        fotoUri = fotoUriDesdeDispositivo, // Asignar la foto URI obtenida
                                        esFavorito = false,
                                        notas = null
                                    )
                                    contactoRepositorio.insertarContacto(nuevoContacto)
                                    contadorContactosImportados++
                                }
                            }
                        }
                    }
                }

                if (contadorContactosImportados > 0) {
                    _estadoImportacion.value = "$contadorContactosImportados contactos importados exitosamente."
                } else {
                    _estadoImportacion.value = "No se encontraron nuevos contactos para importar o ya existían."
                }

            } catch (e: Exception) {
                _estadoImportacion.value = "Error durante la importación: ${e.message}"
                // Log.e("GestionContactoViewModel", "Error importando contactos", e) // Considera añadir logging real
            } finally {
                _importacionEnCurso.value = false
            }
        }
    }

    fun limpiarEstadoImportacion() {
        _estadoImportacion.value = null
    }
}

@Suppress("UNCHECKED_CAST")
class GestionContactoViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GestionContactoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val contactoDao = AppDatabase.obtenerInstancia(application).contactoDao()
            val repositorio = ContactoRepositorio(contactoDao)
            return GestionContactoViewModel(application, repositorio) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: " + modelClass.name)
    }
}