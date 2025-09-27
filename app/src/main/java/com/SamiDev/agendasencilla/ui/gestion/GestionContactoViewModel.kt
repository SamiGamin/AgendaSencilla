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
            // Podrías emitir un estado aquí si es necesario, por ejemplo, para confirmar guardado.
        }
    }

    /**
     * Inicia el proceso de importación de contactos desde el dispositivo.
     * Lee los contactos del ContentProvider del dispositivo y los guarda en la base de datos Room
     * si no existen previamente (verificando por número de teléfono).
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
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )

                // Usar try-with-resources para asegurar que el cursor se cierre
                contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    null, // Sin selección, obtiene todos los contactos con número
                    null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC" // Ordenar por nombre
                )?.use { cursor -> // El bloque use cierra el cursor automáticamente
                    val indiceNombre = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val indiceNumero = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                    if (indiceNombre >= 0 && indiceNumero >= 0) {
                        while (cursor.moveToNext()) {
                            val nombre = cursor.getString(indiceNombre)
                            val numeroTelefono = cursor.getString(indiceNumero)

                            if (nombre != null && numeroTelefono != null && numeroTelefono.isNotBlank()) {
                                // Verificar si el contacto ya existe en Room por número de teléfono
                                val contactoExistente = contactoRepositorio.obtenerContactoPorNumero(numeroTelefono)
                                if (contactoExistente == null) {
                                    val nuevoContacto = Contacto(
                                        nombreCompleto = nombre,
                                        numeroTelefono = numeroTelefono,
                                        fotoUri = null, // Valor por defecto
                                        esFavorito = false, // Valor por defecto
                                        notas = null // Valor por defecto
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
                // Considera loguear el error completo: Log.e("GestionContactoViewModel", "Error importando", e)
            } finally {
                _importacionEnCurso.value = false
            }
        }
    }

    /**
     * Limpia el mensaje de estado de importación.
     * Útil para llamar después de que el usuario ha visto el mensaje.
     */
    fun limpiarEstadoImportacion() {
        _estadoImportacion.value = null
    }
}

/**
 * Factory para crear instancias de [GestionContactoViewModel].
 * Es necesario para poder pasar [Application] y [ContactoRepositorio] al ViewModel.
 *
 * @property application La instancia de la aplicación.
 */
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