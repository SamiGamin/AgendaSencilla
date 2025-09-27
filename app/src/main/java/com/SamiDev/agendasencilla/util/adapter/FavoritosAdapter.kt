package com.SamiDev.agendasencilla.util.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.SamiDev.agendasencilla.R // Importar R para acceder a los drawables
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.databinding.ItemContactoBinding // Asumiendo este nombre para el binding de item_contacto.xml
import com.SamiDev.agendasencilla.util.ContactoDiffCallback
import com.bumptech.glide.Glide // Importar Glide

class FavoritosAdapter(private val onItemClicked: (Contacto) -> Unit) : ListAdapter<Contacto, FavoritosAdapter.FavoritoViewHolder>(
    ContactoDiffCallback()
) {

    inner class FavoritoViewHolder(private val binding: ItemContactoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contacto: Contacto) {
            binding.tvNombreContacto.text = contacto.nombreCompleto
            binding.btnLlamar.setIconTintResource(R.color.md_theme_onPrimaryFixed)
            binding.btnLlamar.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.md_theme_primary)

            Glide.with(itemView.context)
                .load(contacto.fotoUri) // fotoUri es un String?, Glide lo maneja bien.
                .placeholder(R.drawable.ic_face) // Placeholder (asegúrate que exista o cámbialo)
                .error(R.drawable.ic_face)       // Imagen de error (asegúrate que exista o cámbialo)
                .circleCrop() // Para hacer la imagen circular
                .into(binding.ivFotoContacto) // Asegúrate que el ID en ItemContactoBinding sea ivFotoContacto

            binding.btnLlamar.setOnClickListener {
                // Lógica para iniciar una llamada con contacto.numeroTelefono
                // Implementar según sea necesario
            }

            itemView.setOnClickListener {
                onItemClicked(contacto)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritoViewHolder {
        val binding = ItemContactoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoritoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoritoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}