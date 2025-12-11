package com.SamiDev.agendasencilla.ui.listadocontactos

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.SamiDev.agendasencilla.data.database.AppDatabase
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.data.preferencias.PreferenciasManager
import com.SamiDev.agendasencilla.data.repositorio.ContactoRepositorio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ListadocontactosViewModel(
    private val contactoRepositorio: ContactoRepositorio,
    private val preferenciasManager: PreferenciasManager
) : ViewModel() {

    private val _terminoBusqueda = MutableStateFlow("")
    val terminoBusqueda: StateFlow<String> = _terminoBusqueda.asStateFlow()

    val todosLosContactos: Flow<List<Contacto>> = terminoBusqueda.combine(contactoRepositorio.obtenerTodosLosContactos()) { query, contactos ->
        if (query.isBlank()) {
            contactos
        } else {
            contactos.filter {
                it.nombreCompleto.contains(query, ignoreCase = true) || it.numeroTelefono.contains(query)
            }
        }
    }

    private val _lecturaActivada = MutableStateFlow(false)
    val lecturaActivada: StateFlow<Boolean> = _lecturaActivada.asStateFlow()

    init {
        viewModelScope.launch {
            preferenciasManager.lecturaEnVozActivadaFlow.collectLatest { activada ->
                Log.d("ListadoContactosVM", "Preferencia de lectura en voz actualizada a: $activada")
                _lecturaActivada.value = activada
            }
        }
    }

    fun actualizarTerminoBusqueda(query: String) {
        _terminoBusqueda.value = query
    }

    fun actualizarEstadoFavorito(contacto: Contacto) {
        viewModelScope.launch {
            val contactoActualizado = contacto.copy(esFavorito = !contacto.esFavorito)
            contactoRepositorio.actualizarContacto(contactoActualizado)
        }
    }
}

class ListadocontactosViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListadocontactosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val contactoDao = AppDatabase.obtenerInstancia(application).contactoDao()
            val repositorio = ContactoRepositorio(contactoDao)
            val preferenciasManager = PreferenciasManager.getInstance(application.applicationContext)
            return ListadocontactosViewModel(repositorio, preferenciasManager) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: " + modelClass.name)
    }
}