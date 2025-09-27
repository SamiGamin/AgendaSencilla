package com.SamiDev.agendasencilla.ui.listadocontactos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.databinding.ItemContactoListaBinding // Asegúrate que el nombre del binding coincida

/**
 * Adaptador para el RecyclerView que muestra la lista de contactos.
 * Utiliza ListAdapter con DiffUtil para un rendimiento eficiente al actualizar la lista.
 */
class ContactoAdapter(private val onItemClicked: (Contacto) -> Unit) :
    ListAdapter<Contacto, ContactoAdapter.ContactoViewHolder>(DiffCallback) {

    /**
     * ViewHolder para cada ítem de contacto.
     * Contiene las referencias a las vistas definidas en item_contacto_lista.xml.
     */
    inner class ContactoViewHolder(private val binding: ItemContactoListaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition // Usar bindingAdapterPosition es más seguro
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(getItem(position))
                }
            }
        }

        /**
         * Vincula los datos de un objeto [Contacto] a las vistas del layout del ítem.
         * @param contacto El contacto a mostrar.
         */
        fun bind(contacto: Contacto) {
            binding.tvNombreContacto.text = contacto.nombreCompleto
            binding.tvNumeroTelefono.text = contacto.numeroTelefono
            // Aquí puedes cargar la imagen usando contacto.fotoUri con una librería como Glide o Coil
            // Ejemplo con Glide:
            // com.bumptech.glide.Glide.with(itemView.context)
            //    .load(contacto.fotoUri)
            //    .placeholder(R.drawable.ic_avatar_placeholder) // Asegúrate de tener un placeholder
            //    .error(R.drawable.ic_avatar_placeholder) // Opcional: imagen para errores
            //    .circleCrop() // Opcional: para fotos redondas
            //    .into(binding.ivFotoContacto)
            // Por ahora, se usará el placeholder de tools:srcCompat si está definido en el XML.
        }
    }

    /**
     * Crea un nuevo ViewHolder cuando el RecyclerView lo necesita.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactoViewHolder {
        val binding = ItemContactoListaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContactoViewHolder(binding)
    }

    /**
     * Vincula los datos del contacto en la posición dada al ViewHolder.
     */
    override fun onBindViewHolder(holder: ContactoViewHolder, position: Int) {
        val contactoActual = getItem(position)
        holder.bind(contactoActual)
    }

    /**
     * Objeto companion para DiffUtil.Callback.
     * Ayuda al ListAdapter a determinar cómo actualizar la lista de manera eficiente.
     */
    companion object DiffCallback : DiffUtil.ItemCallback<Contacto>() {
        override fun areItemsTheSame(oldItem: Contacto, newItem: Contacto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contacto, newItem: Contacto): Boolean {
            return oldItem == newItem
        }
    }
}
