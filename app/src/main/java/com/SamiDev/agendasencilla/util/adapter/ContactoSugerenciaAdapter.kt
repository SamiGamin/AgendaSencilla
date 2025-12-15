package com.SamiDev.agendasencilla.util.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.SamiDev.agendasencilla.R
import com.SamiDev.agendasencilla.data.ContactoTelefono
import com.SamiDev.agendasencilla.databinding.ItemNumeroMarcarBinding
import com.SamiDev.agendasencilla.util.PhoneNumberFormatter
import com.bumptech.glide.Glide

/**
 * Adaptador para mostrar sugerencias de contactos en el marcador (Dialer).
 *
 * @property onLlamarClick Callback que se invoca al intentar llamar a un contacto.
 */
class ContactoSugerenciaAdapter(
    private val onLlamarClick: (String) -> Unit
) : ListAdapter<ContactoTelefono, ContactoSugerenciaAdapter.VH>(DiffCallback) {

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

        fun bind(contacto: ContactoTelefono) {
            binding.tvNombre.text = contacto.nombreCompleto
            binding.tvTelefono.text = PhoneNumberFormatter.formatearParaLectura(contacto.numeroTelefono)

            if (contacto.fotoUri != null) {
                Glide.with(binding.ivFotoContacto)
                    .load(contacto.fotoUri)
                    .placeholder(R.drawable.ic_perm_identity)
                    .error(R.drawable.ic_perm_identity)
                    .circleCrop()
                    .into(binding.ivFotoContacto)
            } else {
                binding.ivFotoContacto.setImageResource(R.drawable.ic_perm_identity)
            }

            binding.btnLlamar.setOnClickListener {
                onLlamarClick(contacto.numeroTelefono)
            }

            itemView.setOnClickListener {
                onLlamarClick(contacto.numeroTelefono)
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<ContactoTelefono>() {
        override fun areItemsTheSame(old: ContactoTelefono, new: ContactoTelefono) = old.id == new.id
        override fun areContentsTheSame(old: ContactoTelefono, new: ContactoTelefono) = old == new
    }
}