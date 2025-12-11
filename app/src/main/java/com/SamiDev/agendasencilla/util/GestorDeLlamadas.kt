package com.SamiDev.agendasencilla.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat

object GestorDeLlamadas {

    /**
     * Realiza una llamada directa a un número específico.
     *
     * Utiliza la acción ACTION_CALL, que requiere el permiso CALL_PHONE.
     * Incluye una comprobación de seguridad para no intentar llamar si el permiso no está concedido.
     *
     * @param context El contexto para iniciar la actividad y comprobar permisos.
     * @param numero El número de teléfono al que se desea llamar.
     */
    fun llamar(context: Context, numero: String?) {
        if (numero.isNullOrBlank()) {
            Toast.makeText(context, "El contacto no tiene un número de teléfono.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$numero")
        }

        // Comprobar si el permiso CALL_PHONE ha sido concedido
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            // Verificar si hay una aplicación que pueda manejar el intent
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "No se encontró una aplicación para realizar llamadas.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Informar al usuario que el permiso es necesario
            Toast.makeText(context, "Permiso para realizar llamadas no concedido.", Toast.LENGTH_SHORT).show()
        }
    }
}