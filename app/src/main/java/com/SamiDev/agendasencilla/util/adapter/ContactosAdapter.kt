package com.SamiDev.agendasencilla.util.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.databinding.ItemContactoListaBinding
import com.SamiDev.agendasencilla.util.ContactoDiffCallback

class ContactosAdapter : ListAdapter<Contacto, ContactosAdapter.ContactoViewHolder>(
    ContactoDiffCallback()
) {

    // El ViewHolder contiene la lógica para vincular los datos a las vistas de un item.
    inner class ContactoViewHolder(private val binding: ItemContactoListaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contacto: Contacto) {
            binding.tvNombreContacto.text = contacto.nombreCompleto
            binding.tvNumeroTelefono.text = contacto.numeroTelefono // Asegúrate de que este ID exista en ItemContactoListaBinding
            // Aquí iría la lógica para cargar la imagen desde la ruta 'contacto.fotoUri'
            // Por ejemplo, usando Glide o Coil. Por ahora, se usará el placeholder si está definido en el XML.
            // binding.ivFotoContacto.setImageURI(Uri.parse(contacto.fotoUri))

            binding.btnLlamar.setOnClickListener {
                // Lógica para iniciar una llamada con contacto.numeroTelefono
                // Esta es una acción de ejemplo, podrías querer pasar esto a través de una lambda.
                Toast.makeText(binding.root.context, "Llamando a ${contacto.nombreCompleto}", Toast.LENGTH_SHORT).show()
            }

            // Aquí podrías añadir un listener para el clic en todo el ítem si es necesario, 
            // similar a como lo tienes en FavoritosAdapter, o a través de una lambda en el constructor.
            // itemView.setOnClickListener { onItemClicked(contacto) }
        }
    }

    // Crea un nuevo ViewHolder cuando el RecyclerView lo necesita.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactoViewHolder {
        val binding = ItemContactoListaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactoViewHolder(binding)
    }

    // Vincula los datos de un contacto específico a un ViewHolder.
    override fun onBindViewHolder(holder: ContactoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

