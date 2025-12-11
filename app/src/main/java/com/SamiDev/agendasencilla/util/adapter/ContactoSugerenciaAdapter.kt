package com.SamiDev.agendasencilla.util.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.databinding.ItemNumeroMarcarBinding
import com.bumptech.glide.Glide

class ContactoSugerenciaAdapter(
    private val onLlamarClick: (String) -> Unit
) : ListAdapter<Contacto, ContactoSugerenciaAdapter.VH>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemNumeroMarcarBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VH(private val binding: ItemNumeroMarcarBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contacto: Contacto) {
            binding.tvNombre.text = contacto.nombreCompleto
            binding.tvNumeroTelefono.text = contacto.numeroTelefono

            Glide.with(binding.ivFotoContacto)
                .load(contacto.fotoUri)
                .placeholder(com.SamiDev.agendasencilla.R.drawable.ic_perm_identity)
                .circleCrop()
                .into(binding.ivFotoContacto)

            binding.btnLlamar.setOnClickListener {
                onLlamarClick(contacto.numeroTelefono)
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<Contacto>() {
        override fun areItemsTheSame(old: Contacto, new: Contacto) = old.id == new.id
        override fun areContentsTheSame(old: Contacto, new: Contacto) = old == new
    }
}