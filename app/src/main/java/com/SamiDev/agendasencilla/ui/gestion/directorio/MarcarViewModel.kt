package com.SamiDev.agendasencilla.ui.gestion.directorio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.data.repository.ContactoRepositorio
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class MarcarViewModel (
    private val repositorio: ContactoRepositorio
): ViewModel() {
    private val _numeroActual = MutableLiveData("")
    val numeroActual: LiveData<String> = _numeroActual

    private val _sugerencias = MutableLiveData<List<Contacto>>(emptyList())
    val sugerencias: LiveData<List<Contacto>> = _sugerencias

    init {
        // Escuchar cambios en el número y buscar sugerencias
        _numeroActual.observeForever { query ->
            if (query.isNullOrEmpty()) {
                _sugerencias.value = emptyList()
            } else {
                viewModelScope.launch {
                    val resultados = repositorio.buscarContactosPorNombre("%$query%").first()
                    val filtrados = resultados.filter { it.numeroTelefono.contains(query) }
                    _sugerencias.postValue(filtrados)
                }
            }
        }
    }

    fun agregarDigito(digito: String) {
        val actual = _numeroActual.value.orEmpty()
        if (actual.length < 20) {
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

    fun obtenerNumeroParaLlamar() = numeroActual.value.orEmpty().trim()
}