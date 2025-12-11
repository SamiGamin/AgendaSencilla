package com.SamiDev.agendasencilla.ui.gestion.directorio

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.data.database.LlamadaLog
import com.SamiDev.agendasencilla.data.repository.ContactoRepositorio
import com.SamiDev.agendasencilla.data.repository.LlamadasRepositorio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class MarcarViewModel (
    private val repositorio: ContactoRepositorio,
    private val repositorioLog: LlamadasRepositorio
): ViewModel() {
    private val _numeroActual = MutableLiveData("")
    val numeroActual: LiveData<String> = _numeroActual

    private val _sugerencias = MutableLiveData<List<Contacto>>(emptyList())

    private val _historial = MutableLiveData<List<LlamadaLog>>()
    val historial: LiveData<List<LlamadaLog>> get() = _historial
    val sugerencias: LiveData<List<Contacto>> = _sugerencias

    // Evento para llamar
    private val _eventoLlamar = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val eventoLlamar: SharedFlow<String> = _eventoLlamar.asSharedFlow()

    private val _modoTeclado = MutableLiveData<Boolean>(true)
    val modoTeclado: LiveData<Boolean> get() = _modoTeclado

    init {
        // Esta es la clave: cada vez que cambia el número → buscamos
        _numeroActual.observeForever { query ->
            buscarSugerencias(query)
        }
    }
    fun activarModoHistorial() {
        _modoTeclado.value = false
    }
    fun activarModoMarcador() {
        _modoTeclado.value = true
        cargarHistorial(null)
    }
    private fun buscarSugerencias(query: String) {
        Log.d("MARCADOR_DEBUG", "Buscando sugerencias para: '$query'")
        viewModelScope.launch {
            if (query.isEmpty()) {
                _sugerencias.postValue(emptyList())
                Log.d("MARCADOR_DEBUG", "Total de contactos en BD: ${_sugerencias.value}")
                return@launch
            }

            // Busca por nombre O por número
            val todos = repositorio.obtenerTodosLosContactos().first()

            val filtrados = todos.filter { contacto ->
                contacto.nombreCompleto.contains(query, ignoreCase = true) ||
                        contacto.numeroTelefono.replace(" ", "").contains(query.replace(" ", ""))
            }

            _sugerencias.postValue(filtrados.take(6)) // máximo 6 sugerencias
        }
        Log.d("MARCADOR_DEBUG", "Total de contactos en BD: ${_sugerencias.value}")
    }

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

    fun solicitarLlamada(numero: String) {
        _eventoLlamar.tryEmit(numero.replace(" ", ""))
    }

    fun obtenerNumeroParaLlamar() = _numeroActual.value.orEmpty().replace(" ", "")
    fun cargarHistorial(filtro: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            // Asumiendo que inyectaste el repo o tienes acceso al context
            val logs = repositorioLog.obtenerHistorialLlamadas(filtro)
            _historial.postValue(logs)
        }
    }
}