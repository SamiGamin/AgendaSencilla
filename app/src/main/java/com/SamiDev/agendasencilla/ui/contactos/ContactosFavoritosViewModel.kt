package com.SamiDev.agendasencilla.ui.contactos

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.SamiDev.agendasencilla.data.database.AppDatabase
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.data.repositorio.ContactoRepositorio
import kotlinx.coroutines.flow.Flow

/**
 * ViewModel para el fragmento que muestra la lista de contactos favoritos.
 * Se encarga de obtener los datos del repositorio y exponerlos al fragmento.
 *
 * @property contactoRepositorio El repositorio para acceder a los datos de los contactos.
 */
class ContactosFavoritosViewModel(private val contactoRepositorio: ContactoRepositorio) : ViewModel() {

    /**
     * Un [Flow] que emite la lista de contactos favoritos desde la base de datos.
     * El fragmento observará este Flow para actualizar la UI.
     */
    val contactosFavoritos: Flow<List<Contacto>> = contactoRepositorio.obtenerContactosFavoritos()
}

/**
 * Factory para crear instancias de [ContactosFavoritosViewModel].
 * Es necesario para poder pasar la [Application] (y por ende el [ContactoRepositorio]) al ViewModel.
 *
 * @property application La instancia de la aplicación, necesaria para obtener la base de datos.
 */
class ContactosFavoritosViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactosFavoritosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val contactoDao = AppDatabase.obtenerInstancia(application).contactoDao()
            val repositorio = ContactoRepositorio(contactoDao)
            return ContactosFavoritosViewModel(repositorio) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: " + modelClass.name)
    }
}