package com.SamiDev.agendasencilla.ui.contactos

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.SamiDev.agendasencilla.data.ContactoTelefono
import com.SamiDev.agendasencilla.data.database.AppDatabase
import com.SamiDev.agendasencilla.data.preferencias.PreferenciasManager
import com.SamiDev.agendasencilla.data.repository.ContactoTelefonoRepositorio
import com.SamiDev.agendasencilla.util.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel para el fragmento de Contactos Favoritos.
 * Gestiona la carga de favoritos, el filtrado por búsqueda y las preferencias de usuario.
 *
 * @property repositorio Repositorio para acceder a los datos de contactos.
 * @property preferenciasManager Gestor de preferencias de la aplicación.
 */
class ContactosFavoritosViewModel(
    private val repositorio: ContactoTelefonoRepositorio,
    private val preferenciasManager: PreferenciasManager
) : ViewModel() {

    // Estado interno: Lista completa de favoritos cargada
    private val _listaFavoritosOriginal = MutableStateFlow<List<ContactoTelefono>>(emptyList())

    // Estado expuesto a la UI (con carga y error)
    private val _estadoUi = MutableStateFlow<Resultado<List<ContactoTelefono>>>(Resultado.Cargando)
    val estadoUi: StateFlow<Resultado<List<ContactoTelefono>>> = _estadoUi.asStateFlow()

    // Búsqueda
    private val _terminoBusqueda = MutableStateFlow("")
    val terminoBusqueda: StateFlow<String> = _terminoBusqueda.asStateFlow()

    // Preferencias
    private val _lecturaActivada = MutableStateFlow(false)
    val lecturaActivada: StateFlow<Boolean> = _lecturaActivada.asStateFlow()

    init {
        observarPreferencias()
        configurarFiltrado()
    }

    /**
     * Carga la lista de favoritos desde el repositorio.
     * Filtra los contactos obtenidos para mantener solo aquellos marcados como favoritos.
     * Se debe llamar al iniciar o retomar la vista para asegurar datos frescos.
     */
    fun cargarFavoritos() {
        viewModelScope.launch {
            _estadoUi.value = Resultado.Cargando

            when (val resultado = repositorio.obtenerContactosDelTelefono()) {
                is Resultado.Exito -> {
                    val soloFavoritos = resultado.datos.filter { it.esFavorito }
                    _listaFavoritosOriginal.value = soloFavoritos
                    _estadoUi.value = Resultado.Exito(soloFavoritos)
                }
                is Resultado.Error -> {
                    _estadoUi.value = resultado
                }
                else -> {}
            }
        }
    }

    /**
     * Configura la lógica de filtrado reactivo.
     * Combina el término de búsqueda con la lista original de favoritos para emitir la lista filtrada.
     */
    private fun configurarFiltrado() {
        viewModelScope.launch {
            _terminoBusqueda.combine(_listaFavoritosOriginal) { query, favoritos ->
                if (query.isBlank()) {
                    favoritos
                } else {
                    favoritos.filter {
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
}

/**
 * Factory para instanciar [ContactosFavoritosViewModel] con sus dependencias.
 */
@Suppress("UNCHECKED_CAST")
class ContactosFavoritosViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactosFavoritosViewModel::class.java)) {

            val database = AppDatabase.obtenerInstancia(application)
            val repositorio = ContactoTelefonoRepositorio(application.applicationContext, database.favoritoDao())
            val preferenciasManager = PreferenciasManager.getInstance(application.applicationContext)

            return ContactosFavoritosViewModel(repositorio, preferenciasManager) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}