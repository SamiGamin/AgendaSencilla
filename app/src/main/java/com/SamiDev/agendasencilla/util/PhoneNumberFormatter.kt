package com.SamiDev.agendasencilla.util


import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

object PhoneNumberFormatter {

    /**
     * Aplica formato automático: 350 489 7017
     * Solo números, máximo 10 dígitos (Colombia), agrega espacio cada 3
     */
    fun formatar(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var deleting = false
            private val space = ' '

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Detectar si se está borrando
                deleting = count > after
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting || s == null) return

                isFormatting = true

                // Guardar posición del cursor
                val cursorPosition = editText.selectionStart

                // Quitar todos los espacios
                val soloNumeros = s.toString().replace(" ", "")

                // Si hay más de 10 dígitos, recortar
                val digitos = soloNumeros.take(10)

                // Aplicar formato: 350 489 7017
                val formateado = StringBuilder()
                digitos.forEachIndexed { index, char ->
                    if (index == 3 || index == 6) {
                        formateado.append(space)
                    }
                    formateado.append(char)
                }

                // Calcular nueva posición del cursor
                var nuevaPosicion = cursorPosition
                if (!deleting && cursorPosition > 0 && (cursorPosition == 4 || cursorPosition == 8)) {
                    nuevaPosicion++ // saltar el espacio que se acaba de añadir
                }

                // Actualizar texto
                editText.setText(formateado.toString())
                // Mover cursor a la posición correcta
                editText.setSelection(nuevaPosicion.coerceAtMost(formateado.length))

                isFormatting = false
            }
        })
    }
    /**

     * Recibe "3504897017" y retorna "350 489 7017"
     */
    fun formatearParaLectura(numero: String): String {
        // 1. Nos aseguramos que solo haya números (por seguridad)
        val soloNumeros = numero.filter { it.isDigit() }

        // 2. Construimos el String con espacios
        val sb = StringBuilder()
        soloNumeros.forEachIndexed { index, char ->
            // Agregamos espacio en la posición 3 y 6 (después del 3er y 6to número)
            if (index == 3 || index == 6) {
                sb.append(" ")
            }
            sb.append(char)
        }
        return sb.toString()
    }
}