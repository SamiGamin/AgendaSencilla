package com.SamiDev.agendasencilla.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale
import java.util.UUID

/**
 * Clase Singleton para gestionar la funcionalidad de Text-To-Speech (TTS).
 * Centraliza la inicialización, el uso y la liberación del motor de TTS.
 */
@Suppress("DEPRECATION")
class LectorDeVoz private constructor() : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var inicializadoCorrectamente = false
    private val utteranceCallbacks = mutableMapOf<String, () -> Unit>()

    companion object {
        private const val TAG = "LectorDeVoz"

        // Instancia única del Singleton
        @Volatile
        private var INSTANCIA: LectorDeVoz? = null

        fun obtenerInstancia(): LectorDeVoz {
            return INSTANCIA ?: synchronized(this) {
                val instancia = LectorDeVoz()
                INSTANCIA = instancia
                instancia
            }
        }
    }

    /**
     * Inicializa el motor de TextToSpeech.
     * Debe llamarse desde un componente con contexto (como un Fragment o Activity)
     * antes de intentar leer cualquier texto.
     */
    fun inicializar(context: Context) {
        if (textToSpeech == null) {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Configurar el idioma a español
            val resultado = textToSpeech?.setLanguage(Locale("es", "ES"))
            if (resultado == TextToSpeech.LANG_MISSING_DATA || resultado == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "El idioma español no está soportado en este dispositivo.")
                inicializadoCorrectamente = false
            } else {
                Log.d(TAG, "Motor TextToSpeech inicializado correctamente en español.")
                inicializadoCorrectamente = true
            }
            // Configurar el listener para saber cuándo termina de hablar
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    val callback = utteranceCallbacks.remove(utteranceId)
                    callback?.let {
                        Handler(Looper.getMainLooper()).post(it)
                    }
                }




                override fun onError(utteranceId: String?) {
                    utteranceCallbacks.remove(utteranceId)
                }
            })
        } else {
            Log.e(TAG, "Error al inicializar el motor de TextToSpeech. Código de estado: $status")
            inicializadoCorrectamente = false
        }
    }

    /**
     * Lee en voz alta el texto proporcionado.
     * Si el motor no está inicializado, la operación se ignora.
     * @param texto El texto a leer.
     * @param onDone Un callback opcional que se ejecuta en el hilo principal cuando la lectura ha terminado.
     */
    fun leerEnVozAlta(texto: String, onDone: (() -> Unit)? = null) {
        if (!inicializadoCorrectamente || textToSpeech == null) {
            Log.w(TAG, "El motor TTS no está listo. No se puede leer el texto.")
            onDone?.invoke()
            return
        }

        if (onDone != null) {
            val utteranceId = UUID.randomUUID().toString()
            utteranceCallbacks[utteranceId] = onDone
            textToSpeech?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } else {
            textToSpeech?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    /**
     * Libera los recursos del motor TextToSpeech.
     * Es crucial llamar a este método en el onDestroy o onDestroyView del
     * componente que lo inicializó para evitar fugas de memoria.
     */
    fun liberarRecursos() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        inicializadoCorrectamente = false
        Log.d(TAG, "Recursos de TextToSpeech liberados.")
    }
}