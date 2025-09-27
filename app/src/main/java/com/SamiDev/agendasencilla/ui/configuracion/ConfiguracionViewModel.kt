package com.SamiDev.agendasencilla.ui.configuracion

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.SamiDev.agendasencilla.data.preferencias.OpcionTamanoFuente // Importar el enum
import com.SamiDev.agendasencilla.data.preferencias.PreferenciasManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Enum para representar las opciones de tema disponibles para el usuario.
 */
enum class OpcionTema {
    CLARO,
    OSCURO,
    SISTEMA
}

/**
 * ViewModel para el fragmento de Configuración.
 * Gestiona la lógica relacionada con las preferencias del usuario, como el tema y tamaño de fuente.
 *
 * @property application La instancia de la aplicación, necesaria para acceder a recursos del sistema.
 * @property preferenciasManager El gestor para acceder y modificar las SharedPreferences.
 */
class ConfiguracionViewModel(
    private val application: Application,
    private val preferenciasManager: PreferenciasManager
) : ViewModel() {

    // --- Preferencias de Tema ---
    private val _opcionTemaSeleccionada: MutableStateFlow<OpcionTema>
    val opcionTemaSeleccionada: StateFlow<OpcionTema>

    // --- Preferencias de Tamaño de Fuente ---
    private val _opcionTamanoFuenteSeleccionada: MutableStateFlow<OpcionTamanoFuente>
    val opcionTamanoFuenteSeleccionada: StateFlow<OpcionTamanoFuente>

    // --- Evento para recrear la actividad ---
    private val _eventoRecrearActividad = MutableSharedFlow<Unit>()
    val eventoRecrearActividad: SharedFlow<Unit> = _eventoRecrearActividad.asSharedFlow()

    init {
        // Inicialización para Tema
        _opcionTemaSeleccionada = MutableStateFlow(obtenerOpcionTemaActual())
        opcionTemaSeleccionada = _opcionTemaSeleccionada.asStateFlow()

        // Inicialización para Tamaño de Fuente
        _opcionTamanoFuenteSeleccionada = MutableStateFlow(preferenciasManager.obtenerOpcionTamanoFuente())
        opcionTamanoFuenteSeleccionada = _opcionTamanoFuenteSeleccionada.asStateFlow()
    }

    private fun obtenerOpcionTemaActual(): OpcionTema {
        return if (preferenciasManager.tienePreferenciaTemaGuardada()) {
            if (preferenciasManager.obtenerTemaOscuro()) {
                OpcionTema.OSCURO
            } else {
                OpcionTema.CLARO
            }
        } else {
            OpcionTema.SISTEMA
        }
    }

    fun actualizarOpcionTema(opcion: OpcionTema) {
        _opcionTemaSeleccionada.value = opcion
        when (opcion) {
            OpcionTema.CLARO -> {
                preferenciasManager.guardarTemaOscuro(false)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            OpcionTema.OSCURO -> {
                preferenciasManager.guardarTemaOscuro(true)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            OpcionTema.SISTEMA -> {
                preferenciasManager.borrarPreferenciaTema()
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    /**
     * Actualiza la preferencia del tamaño de fuente de la aplicación.
     * Guarda la preferencia y emite un evento para que la actividad se recree.
     *
     * @param opcion La [OpcionTamanoFuente] seleccionada por el usuario.
     */
    fun actualizarOpcionTamanoFuente(opcion: OpcionTamanoFuente) {
        val opcionAnterior = _opcionTamanoFuenteSeleccionada.value
        if (opcionAnterior != opcion) {
            preferenciasManager.guardarOpcionTamanoFuente(opcion)
            _opcionTamanoFuenteSeleccionada.value = opcion
            viewModelScope.launch {
                _eventoRecrearActividad.emit(Unit)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class ConfiguracionViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConfiguracionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val preferenciasManager = PreferenciasManager(application.applicationContext)
            return ConfiguracionViewModel(application, preferenciasManager) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: " + modelClass.name)
    }
}