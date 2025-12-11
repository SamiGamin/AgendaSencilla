package com.SamiDev.agendasencilla.ui.gestion

import android.app.Application
import android.provider.ContactsContract
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.SamiDev.agendasencilla.data.database.AppDatabase
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.data.repositorio.ContactoRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GestionContactoViewModel(
    private val application: Application,
    private val contactoRepositorio: ContactoRepositorio
) : ViewModel() {

    private val _estadoImportacion = MutableStateFlow<String?>(null)
    val estadoImportacion: StateFlow<String?> = _estadoImportacion.asStateFlow()

    private val _importacionEnCurso = MutableStateFlow(false)
    val importacionEnCurso: StateFlow<Boolean> = _importacionEnCurso.asStateFlow()

    private val _contactoCargado = MutableStateFlow<Contacto?>(null)
    val contactoCargado: StateFlow<Contacto?> = _contactoCargado.asStateFlow()

    fun cargarContacto(id: Int) {
        viewModelScope.launch {
            contactoRepositorio.obtenerContactoPorId(id).collect { contacto ->
                _contactoCargado.value = contacto
            }
        }
    }

    fun guardarContacto(contacto: Contacto) {
        viewModelScope.launch {
            if (contacto.id == 0) {
                contactoRepositorio.insertarContacto(contacto)
            } else {
                contactoRepositorio.actualizarContacto(contacto)
            }
        }
    }

    /**
     * Importa y actualiza contactos del dispositivo de forma inteligente, aplicando reglas de negocio
     * para agrupar por nombre y validar/normalizar cada número de teléfono.
     */
    fun importarContactosDelDispositivo() {
        viewModelScope.launch {
            _importacionEnCurso.value = true
            _estadoImportacion.value = "Analizando y agrupando contactos..."

            try {
                // 1. Obtiene los contactos actuales de la BD y los prepara para consulta rápida.
                val contactosActuales = contactoRepositorio.obtenerTodosLosContactos().first()
                val mapaContactosActualesPorNombre = contactosActuales.associateBy { it.nombreCompleto }.toMutableMap()

                // 2. Agrupa todos los números y fotos del dispositivo por nombre de contacto.
                val mapaContactosDispositivo = mutableMapOf<String, MutableSet<String>>()
                val mapaFotosDispositivo = mutableMapOf<String, String>()

                val contentResolver = application.contentResolver
                val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI
                )

                contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, null, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                )?.use { cursor ->
                    val nombreIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numeroIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val fotoIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)

                    while (cursor.moveToNext()) {
                        val nombre = cursor.getString(nombreIndex)
                        val numero = cursor.getString(numeroIndex)
                        val foto = if (fotoIndex != -1) cursor.getString(fotoIndex) else null

                        if (nombre != null && numero != null && numero.isNotBlank()) {
                            Log.d(TAG, "Dispositivo -> Leyendo: '$nombre', Número crudo: '$numero'")
                            mapaContactosDispositivo.getOrPut(nombre) { mutableSetOf() }.add(numero)
                            if (foto != null && !mapaFotosDispositivo.containsKey(nombre)) {
                                mapaFotosDispositivo[nombre] = foto
                            }
                        }
                    }
                }

                // 3. Procesa cada contacto agrupado, valida sus números y decide si insertar o actualizar.
                var contadorNuevos = 0
                var contadorActualizados = 0

                for ((nombre, numerosDispositivo) in mapaContactosDispositivo) {
                    Log.d(TAG, "Procesando: '$nombre', Números originales: $numerosDispositivo")
                    // Normaliza y valida todos los números para esta persona según las reglas de negocio.
                    val numerosValidosYUnicos = numerosDispositivo
                        .map(::normalizarNumeroTelefono)
                        .filter { it.length == 10 && it.startsWith("3") }
                        .toSet()

                    Log.d(TAG, " -> Números válidos y únicos para '$nombre': $numerosValidosYUnicos")

                    if (numerosValidosYUnicos.isEmpty()) {
                        Log.d(TAG, " -> Ignorando a '$nombre', no tiene números válidos.")
                        continue // Omite si no hay números válidos.
                    }

                    val contactoExistente = mapaContactosActualesPorNombre[nombre]

                    if (contactoExistente == null) {
                        // El contacto es nuevo. Se crea e inserta.
                        val numerosAGuardar = numerosValidosYUnicos.joinToString(",")
                        Log.d(TAG, "   -> ¡NUEVO! Guardando a '$nombre' con números: '$numerosAGuardar'")
                        val nuevoContacto = Contacto(
                            nombreCompleto = nombre,
                            numeroTelefono = numerosAGuardar,
                            fotoUri = mapaFotosDispositivo[nombre],
                            esFavorito = false,
                            notas = null
                        )
                        contactoRepositorio.insertarContacto(nuevoContacto)
                        contadorNuevos++
                    } else {
                        // El contacto ya existe. Comprueba si hay números nuevos para añadir.
                        val numerosActuales = contactoExistente.numeroTelefono?.split(",")?.toSet() ?: emptySet()
                        val numerosNuevosParaAnadir = numerosValidosYUnicos - numerosActuales

                        if (numerosNuevosParaAnadir.isNotEmpty()) {
                            val todosLosNumeros = (numerosActuales + numerosNuevosParaAnadir).distinct()
                            val numerosAGuardar = todosLosNumeros.joinToString(",")
                            Log.d(TAG, "   -> ¡ACTUALIZANDO! Añadiendo a '$nombre' los números: $numerosNuevosParaAnadir. Resultado final: '$numerosAGuardar'")
                            val contactoActualizado = contactoExistente.copy(
                                numeroTelefono = numerosAGuardar
                            )
                            contactoRepositorio.actualizarContacto(contactoActualizado)
                            contadorActualizados++
                        } else {
                            Log.d(TAG, "   -> Sin cambios para '$nombre', los números válidos ya existen.")
                        }
                    }
                }

                // 4. Informa el resultado de la operación.
                val mensajeFinal = when {
                    contadorNuevos > 0 && contadorActualizados > 0 -> "$contadorNuevos contactos importados y $contadorActualizados actualizados."
                    contadorNuevos > 0 -> "$contadorNuevos contactos nuevos importados."
                    contadorActualizados > 0 -> "$contadorActualizados contactos fueron actualizados con nuevos números."
                    else -> "No se encontraron nuevos contactos o números para importar."
                }
                _estadoImportacion.value = mensajeFinal

            } catch (e: Exception) {
                _estadoImportacion.value = "Error durante la importación: ${e.message}"
                Log.e(TAG, "Error fatal durante la importación", e)
            } finally {
                _importacionEnCurso.value = false
            }
        }
    }

    /**
     * Normaliza un número de teléfono para el formato estándar de 10 dígitos de Colombia.
     * Ej: "+57 (310) 123-4567" -> "3101234567"
     */
    private fun normalizarNumeroTelefono(numero: String): String {
        val digitos = numero.filter { it.isDigit() }
        return if (digitos.length == 12 && digitos.startsWith("57")) {
            digitos.substring(2)
        } else {
            digitos
        }
    }

    fun limpiarEstadoImportacion() {
        _estadoImportacion.value = null
    }

    companion object {
        private const val TAG = "GestionContactoViewModel"
    }
}

@Suppress("UNCHECKED_CAST")
class GestionContactoViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GestionContactoViewModel::class.java)) {
            val contactoDao = AppDatabase.obtenerInstancia(application).contactoDao()
            val repositorio = ContactoRepositorio(contactoDao)
            return GestionContactoViewModel(application, repositorio) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida: " + modelClass.name)
    }
}