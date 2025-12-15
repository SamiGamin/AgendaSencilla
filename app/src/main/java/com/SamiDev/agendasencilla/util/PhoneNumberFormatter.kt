package com.SamiDev.agendasencilla.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/**
 * Utilidad para formatear números de teléfono.
 * Aplica formato estilo Colombia (3xx xxx xxxx) y facilita la lectura.
 */
object PhoneNumberFormatter {

    /**
     * Aplica formato automático a un EditText mientras el usuario escribe.
     * Formato: 350 489 7017 (Solo números, max 10 dígitos, espacios cada 3).
     *
     * @param editText El EditText a formatear.
     */
    fun formatar(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var deleting = false
            private val space = ' '

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                deleting = count > after
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return

                isFormatting = true

                val cursorPosition = editText.selectionStart
                val soloNumeros = s.toString().replace(" ", "")
                val digitos = soloNumeros.take(10)

                val formateado = StringBuilder()
                digitos.forEachIndexed { index, char ->
                    if (index == 3 || index == 6) {
                        formateado.append(space)
                    }
                    formateado.append(char)
                }

                var nuevaPosicion = cursorPosition
                if (!deleting && cursorPosition > 0 && (cursorPosition == 4 || cursorPosition == 8)) {
                    nuevaPosicion++
                }

                editText.setText(formateado.toString())
                editText.setSelection(nuevaPosicion.coerceAtMost(formateado.length))

                isFormatting = false
            }
        })
    }

    /**
     * Formatea un número de teléfono en formato string para mostrarlo.
     * Recibe "3504897017" y retorna "350 489 7017".
     *
     * @param numero El número de teléfono sin formato.
     * @return El número formateado con espacios.
     */
    fun formatearParaLectura(numero: String): String {
        val soloNumeros = numero.filter { it.isDigit() }
        val sb = StringBuilder()
        soloNumeros.forEachIndexed { index, char ->
            if (index == 3 || index == 6) {
                sb.append(" ")
            }
            sb.append(char)
        }
        return sb.toString()
    }
}