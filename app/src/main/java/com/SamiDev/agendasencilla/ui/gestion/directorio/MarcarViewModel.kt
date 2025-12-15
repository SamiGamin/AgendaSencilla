package com.SamiDev.agendasencilla.ui.gestion.directorio

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.SamiDev.agendasencilla.data.ContactoTelefono
import com.SamiDev.agendasencilla.data.database.LlamadaLog
import com.SamiDev.agendasencilla.data.repository.ContactoTelefonoRepositorio
import com.SamiDev.agendasencilla.data.repository.LlamadasRepositorio
import com.SamiDev.agendasencilla.util.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
class MarcarViewModel(
    private val repoContactos: ContactoTelefonoRepositorio,
    private val repoLlamadas: LlamadasRepositorio
) : ViewModel() {

    // Entrada del usuario (teclado numérico)
    private val _numeroActual = MutableLiveData("")
    val numeroActual: LiveData<String> = _numeroActual

    // Sugerencias de contactos (mientras escribe)
    private val _sugerencias = MutableLiveData<List<ContactoTelefono>>(emptyList())
    val sugerencias: LiveData<List<ContactoTelefono>> = _sugerencias

    // Historial de llamadas (cuando no escribe)
    private val _historial = MutableLiveData<List<LlamadaLog>>(emptyList())
    val historial: LiveData<List<LlamadaLog>> = _historial

    // Control de UI
    private val _modoTeclado = MutableLiveData(true)
    val modoTeclado: LiveData<Boolean> = _modoTeclado

    // Eventos (Llamar)
    private val _eventoLlamar = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val eventoLlamar: SharedFlow<String> = _eventoLlamar.asSharedFlow()

    // Caché local de contactos para búsqueda rápida sin consultar CP cada vez
    private var listaCompletaContactos: List<ContactoTelefono> = emptyList()

    init {
        // Cargar contactos en memoria al iniciar para búsqueda rápida
        cargarContactosEnMemoria()

        // Cargar historial inicial
        cargarHistorial()

        // Observar cambios en el número para filtrar
        _numeroActual.observeForever { query ->
            filtrarSugerencias(query)
        }
    }

    private fun cargarContactosEnMemoria() {
        viewModelScope.launch {
            val resultado = repoContactos.obtenerContactosDelTelefono()
            if (resultado is Resultado.Exito) {
                listaCompletaContactos = resultado.datos
            }
        }
    }

    fun cargarHistorial(filtroTipo: Int? = null) {
        viewModelScope.launch {
            val logs = repoLlamadas.obtenerHistorialLlamadas(filtroTipo)
            _historial.postValue(logs)
        }
    }

    private fun filtrarSugerencias(query: String) {
        if (query.isEmpty()) {
            _sugerencias.value = emptyList()
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            val queryLimpia = query.replace(" ", "")

            // Filtramos la lista en memoria
            val filtrados = listaCompletaContactos.filter { contacto ->
                // Coincidencia por nombre O por número
                contacto.nombreCompleto.contains(query, ignoreCase = true) ||
                        contacto.numeroTelefono.replace(" ", "").contains(queryLimpia)
            }

            _sugerencias.postValue(filtrados.take(5)) // Top 5 resultados
        }
    }

    // --- LÓGICA DEL TECLADO ---

    fun agregarDigito(digito: String) {
        val actual = _numeroActual.value.orEmpty()
        if (actual.replace(" ", "").length < 15) {
            _numeroActual.value = actual + digito
        }
    }

    fun borrarUltimoDigito() {
        val actual = _numeroActual.value.orEmpty()
        if (actual.isNotEmpty()) {
            _numeroActual.value = actual.dropLast(1)
        }
    }

    fun borrarTodo() {
        _numeroActual.value = ""
    }

    // --- MODOS DE VISTA ---

    fun activarModoHistorial() {
        _modoTeclado.value = false
    }

    fun activarModoMarcador() {
        _modoTeclado.value = true
        cargarHistorial(null) // Restaurar historial completo
    }

    // --- ACCIONES ---

    fun solicitarLlamada(numeroOpcional: String? = null) {
        val numeroALlamar = numeroOpcional ?: _numeroActual.value.orEmpty()
        if (numeroALlamar.isNotBlank()) {
            _eventoLlamar.tryEmit(numeroALlamar.replace(" ", ""))
        }
    }
}