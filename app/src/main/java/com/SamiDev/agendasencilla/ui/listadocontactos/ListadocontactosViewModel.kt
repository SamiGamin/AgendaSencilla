@file:Suppress("UNCHECKED_CAST")

package com.SamiDev.agendasencilla.ui.listadocontactos

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.SamiDev.agendasencilla.data.database.AppDatabase
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.data.repositorio.ContactoRepositorio
import kotlinx.coroutines.flow.Flow

/**
 * ViewModel para el fragmento que muestra la lista de todos los contactos.
 * Se encarga de obtener los datos del repositorio y exponerlos al fragmento.
 *
 * @property contactoRepositorio El repositorio para acceder a los datos de los contactos.
 */
class ListadocontactosViewModel(private val contactoRepositorio: ContactoRepositorio) : ViewModel() {

    /**
     * Un [Flow] que emite la lista completa de contactos desde la base de datos.
     * El fragmento observará este Flow para actualizar la UI.
     */
    val todosLosContactos: Flow<List<Contacto>> = contactoRepositorio.obtenerTodosLosContactos()

    // Aquí se podrían añadir funciones para otras operaciones, como eliminar un contacto,
    // aunque la creación/edición se manejará en GestionContactoViewModel.
}

/**
 * Factory para crear instancias de [ListadocontactosViewModel].
 * Es necesario para poder pasar el [Application] (y por ende el [ContactoRepositorio]) al ViewModel.
 *
 * @property application La instancia de la aplicación, necesaria para obtener la base de datos.
 */
class ListadocontactosViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListadocontactosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val contactoDao = AppDatabase.obtenerInstancia(application).contactoDao()
            val repositorio = ContactoRepositorio(contactoDao)
            return ListadocontactosViewModel(repositorio) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: " + modelClass.name)
    }
}