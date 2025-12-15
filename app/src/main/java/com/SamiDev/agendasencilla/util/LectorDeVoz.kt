package com.SamiDev.agendasencilla.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
     */
    fun inicializar(context: Context) {
        if (textToSpeech == null) {
            textToSpeech = TextToSpeech(context.applicationContext, this)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val resultado = textToSpeech?.setLanguage(Locale("es", "ES"))
            if (resultado == TextToSpeech.LANG_MISSING_DATA || resultado == TextToSpeech.LANG_NOT_SUPPORTED) {
                inicializadoCorrectamente = false
            } else {
                inicializadoCorrectamente = true
            }
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
            inicializadoCorrectamente = false
        }
    }

    /**
     * Lee en voz alta el texto proporcionado.
     *
     * @param texto El texto a leer.
     * @param onDone Un callback opcional que se ejecuta en el hilo principal cuando la lectura ha terminado.
     */
    fun leerEnVozAlta(texto: String, onDone: (() -> Unit)? = null) {
        if (!inicializadoCorrectamente || textToSpeech == null) {
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
     */
    fun liberarRecursos() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        inicializadoCorrectamente = false
    }
}