package com.SamiDev.agendasencilla.ui.listadocontactos

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.SamiDev.agendasencilla.data.ContactoTelefono
import com.SamiDev.agendasencilla.data.database.AppDatabase
import com.SamiDev.agendasencilla.data.preferencias.PreferenciasManager
import com.SamiDev.agendasencilla.data.repository.ContactoTelefonoRepositorio
import com.SamiDev.agendasencilla.util.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ListadocontactosViewModel(
    private val repositorio: ContactoTelefonoRepositorio,
    private val preferenciasManager: PreferenciasManager
) : ViewModel() {


    private val TAG = ListadocontactosViewModel::class.java.simpleName

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
                    // Inicialmente mostramos la lista completa
                    _estadoUi.value = Resultado.Exito(resultado.datos)
                }
                is Resultado.Error -> {
                    _estadoUi.value = resultado
                    Log.e(TAG, "Error cargando contactos: ${resultado.mensaje}")
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
                // Solo actualizamos si ya tenemos datos cargados exitosamente
                if (_estadoUi.value is Resultado.Exito) {
                    _estadoUi.value = Resultado.Exito(listaFiltrada)
                }
            }
        }
    }

    private fun observarPreferencias() {
        viewModelScope.launch {
            preferenciasManager.lecturaEnVozActivadaFlow.collectLatest { activada ->
                Log.d(TAG, "Preferencia lectura voz: $activada")
                _lecturaActivada.value = activada
            }
        }
    }

    fun actualizarTerminoBusqueda(query: String) {
        _terminoBusqueda.value = query
    }
    fun actualizarEstadoFavorito(contacto: ContactoTelefono, esFavorito: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (esFavorito) {
                repositorio.marcarComoFavorito(contacto.id)
                Log.d("ViewModel", "Guardando favorito: ${contacto.nombreCompleto}")
            } else {
                repositorio.desmarcarFavorito(contacto.id)
                Log.d("ViewModel", "Removiendo favorito: ${contacto.nombreCompleto}")
            }

            // Opcional: Actualizar la lista _listaOriginal en memoria para reflejar el cambio
            // sin tener que recargar todo del teléfono.
        }
    }
}


// Factory actualizada para inyectar el nuevo Repositorio
@Suppress("UNCHECKED_CAST")
class ListadocontactosViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListadocontactosViewModel::class.java)) {

            val database = AppDatabase.obtenerInstancia(application)
            // Creamos el repositorio pasando el contexto de la aplicación
            val repositorio = ContactoTelefonoRepositorio(application.applicationContext , database.favoritoDao())
            val preferenciasManager = PreferenciasManager.getInstance(application.applicationContext)

            return ListadocontactosViewModel(repositorio, preferenciasManager) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: " + modelClass.name)
    }
}