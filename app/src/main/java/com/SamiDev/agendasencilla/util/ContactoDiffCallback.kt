package com.SamiDev.agendasencilla.util

import androidx.recyclerview.widget.DiffUtil
import com.SamiDev.agendasencilla.data.database.Contacto

/**
 * Implementación de [androidx.recyclerview.widget.DiffUtil.ItemCallback] para la clase [com.SamiDev.agendasencilla.data.database.Contacto].
 * Ayuda al ListAdapter a determinar de manera eficiente las diferencias entre listas de contactos
 * para actualizar el RecyclerView solo cuando sea necesario y de forma optimizada.
 */
class ContactoDiffCallback : DiffUtil.ItemCallback<Contacto>() {
    /**
     * Comprueba si dos objetos representan el mismo ítem.
     * Por ejemplo, si los ítems tienen IDs únicos, este método debe verificar la igualdad de sus IDs.
     *
     * @param oldItem El ítem en la lista antigua.
     * @param newItem El ítem en la lista nueva.
     * @return `true` si los dos ítems representan el mismo objeto o `false` en caso contrario.
     */
    override fun areItemsTheSame(oldItem: Contacto, newItem: Contacto): Boolean {
        return oldItem.id == newItem.id // Compara por el ID único del contacto.
    }

    /**
     * Comprueba si dos ítems tienen los mismos datos.
     * Este método solo se llama si [areItemsTheSame] devuelve `true`.
     * Si tus ítems son inmutables, puedes retornar `oldItem == newItem`.
     *
     * @param oldItem El ítem en la lista antigua.
     * @param newItem El ítem en la lista nueva.
     * @return `true` si el contenido de los ítems es el mismo o `false` en caso contrario.
     */
    override fun areContentsTheSame(oldItem: Contacto, newItem: Contacto): Boolean {
        // Compara todos los campos relevantes del objeto Contacto.

        return oldItem == newItem
    }
}