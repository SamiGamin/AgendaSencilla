package com.SamiDev.agendasencilla.ui.listadocontactos

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.SamiDev.agendasencilla.data.ContactoTelefono
import com.SamiDev.agendasencilla.data.database.AppDatabase
import com.SamiDev.agendasencilla.data.preferencias.PreferenciasManager
import com.SamiDev.agendasencilla.data.repository.ContactoTelefonoRepositorio
import com.SamiDev.agendasencilla.util.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel para el fragmento de listado de contactos.
 * Gestiona la carga, filtrado y actualización de favoritos de los contactos.
 *
 * @property repositorio Repositorio de contactos.
 * @property preferenciasManager Gestor de preferencias.
 */
class ListadocontactosViewModel(
    private val repositorio: ContactoTelefonoRepositorio,
    private val preferenciasManager: PreferenciasManager
) : ViewModel() {

    // Estado interno de la lista completa cargada del teléfono
    private val _listaOriginal = MutableStateFlow<List<ContactoTelefono>>(emptyList())

    // Estado expuesto a la UI (Filtrado y Estado de Carga)
    private val _estadoUi = MutableStateFlow<Resultado<List<ContactoTelefono>>>(Resultado.Cargando)
    val estadoUi: StateFlow<Resultado<List<ContactoTelefono>>> = _estadoUi.asStateFlow()

    // Búsqueda
    private val _terminoBusqueda = MutableStateFlow("")
    val terminoBusqueda: StateFlow<String> = _terminoBusqueda.asStateFlow()

    // Preferencias
    private val _lecturaActivada = MutableStateFlow(false)
    val lecturaActivada: StateFlow<Boolean> = _lecturaActivada.asStateFlow()

    init {
        cargarContactosIniciales()
        observarPreferencias()
        configurarFiltrado()
    }

    /**
     * Carga los contactos del teléfono una sola vez al iniciar.
     */
    fun cargarContactosIniciales() {
        viewModelScope.launch {
            _estadoUi.value = Resultado.Cargando
            when (val resultado = repositorio.obtenerContactosDelTelefono()) {
                is Resultado.Exito -> {
                    _listaOriginal.value = resultado.datos
                    _estadoUi.value = Resultado.Exito(resultado.datos)
                }
                is Resultado.Error -> {
                    _estadoUi.value = resultado
                }
                else -> {}
            }
        }
    }

    /**
     * Combina el término de búsqueda con la lista original cargada en memoria.
     */
    private fun configurarFiltrado() {
        viewModelScope.launch {
            _terminoBusqueda.combine(_listaOriginal) { query, contactos ->
                if (query.isBlank()) {
                    contactos
                } else {
                    contactos.filter {
                        it.nombreCompleto.contains(query, ignoreCase = true) ||
                                it.numeroTelefono.contains(query)
                    }
                }
            }.collectLatest { listaFiltrada ->
                if (_estadoUi.value is Resultado.Exito) {
                    _estadoUi.value = Resultado.Exito(listaFiltrada)
                }
            }
        }
    }

    private fun observarPreferencias() {
        viewModelScope.launch {
            preferenciasManager.lecturaEnVozActivadaFlow.collectLatest { activada ->
                _lecturaActivada.value = activada
            }
        }
    }

    fun actualizarTerminoBusqueda(query: String) {
        _terminoBusqueda.value = query
    }

    /**
     * Actualiza el estado de favorito de un contacto.
     *
     * @param contacto El contacto a actualizar.
     * @param esFavorito El nuevo estado de favorito.
     */
    fun actualizarEstadoFavorito(contacto: ContactoTelefono, esFavorito: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (esFavorito) {
                repositorio.marcarComoFavorito(contacto.id)
            } else {
                repositorio.desmarcarFavorito(contacto.id)
            }
        }
    }
}

/**
 * Factory para instanciar [ListadocontactosViewModel].
 */
@Suppress("UNCHECKED_CAST")
class ListadocontactosViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListadocontactosViewModel::class.java)) {
            val database = AppDatabase.obtenerInstancia(application)
            val repositorio = ContactoTelefonoRepositorio(application.applicationContext , database.favoritoDao())
            val preferenciasManager = PreferenciasManager.getInstance(application.applicationContext)
            return ListadocontactosViewModel(repositorio, preferenciasManager) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: " + modelClass.name)
    }
}