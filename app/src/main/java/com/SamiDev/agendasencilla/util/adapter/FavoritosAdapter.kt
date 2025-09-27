package com.SamiDev.agendasencilla.util.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.databinding.ItemContactoBinding // Asumiendo este nombre para el binding de item_contacto.xml
import com.SamiDev.agendasencilla.util.ContactoDiffCallback

// La clase ContactoDiffCallback se tomará de ContactosAdapter.kt ya que están en el mismo paquete.
class FavoritosAdapter(private val onItemClicked: (Contacto) -> Unit) : ListAdapter<Contacto, FavoritosAdapter.FavoritoViewHolder>(
    ContactoDiffCallback()
) {

    // El ViewHolder contiene la lógica para vincular los datos a las vistas de un item de favorito.
    inner class FavoritoViewHolder(private val binding: ItemContactoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contacto: Contacto) {
            binding.tvNombreContacto.text = contacto.nombreCompleto
            // Aquí iría la lógica para cargar la imagen desde la ruta 'contacto.fotoUri'
            // Por ejemplo, usando Glide o Coil. Por ahora, se usará el placeholder si está definido en el XML.
            // binding.ivFotoContacto.setImageURI(Uri.parse(contacto.fotoUri))

            binding.btnLlamar.setOnClickListener {
                // Lógica para iniciar una llamada con contacto.numeroTelefono
                // Por ejemplo, podrías pasar esta acción a través de otra lambda en el constructor del adapter,
                // o manejarlo directamente si el contexto es fácilmente accesible y la acción es simple.
            }

            // Manejar clic en todo el ítem
            itemView.setOnClickListener {
                onItemClicked(contacto)
            }
        }
    }

    // Crea un nuevo ViewHolder cuando el RecyclerView lo necesita.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritoViewHolder {
        val binding = ItemContactoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoritoViewHolder(binding)
    }

    // Vincula los datos de un contacto específico a un ViewHolder.
    override fun onBindViewHolder(holder: FavoritoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

