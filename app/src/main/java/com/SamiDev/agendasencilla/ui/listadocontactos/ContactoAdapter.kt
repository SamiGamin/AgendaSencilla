package com.SamiDev.agendasencilla.ui.listadocontactos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.SamiDev.agendasencilla.R
import com.SamiDev.agendasencilla.data.ContactoTelefono
import com.SamiDev.agendasencilla.databinding.ItemContactoListaBinding
import com.SamiDev.agendasencilla.util.GestorDeLlamadas
import com.SamiDev.agendasencilla.util.LectorDeVoz
import com.SamiDev.agendasencilla.util.PhoneNumberFormatter
import com.bumptech.glide.Glide

/**
 * Adaptador para mostrar la lista de contactos.
 * Maneja la visualización de cada contacto, incluyendo foto, nombre, teléfono y estado de favorito.
 *
 * @property alHacerClicEnItem Callback al hacer clic en un contacto.
 * @property alHacerClicEnFavorito Callback al hacer clic en el botón de favorito.
 */
class ContactoAdapter(
    private val alHacerClicEnItem: (ContactoTelefono) -> Unit,
    private val alHacerClicEnFavorito: (ContactoTelefono, Boolean) -> Unit
) : ListAdapter<ContactoTelefono, ContactoAdapter.ContactoViewHolder>(DiffCallback) {

    private var lecturaVozActivada: Boolean = false

    /**
     * Actualiza la preferencia de lectura en voz.
     */
    fun actualizarPreferenciaLectura(activada: Boolean) {
        lecturaVozActivada = activada
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactoViewHolder {
        val binding = ItemContactoListaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactoViewHolder, position: Int) {
        val contacto = getItem(position)
        holder.bind(contacto)
    }

    inner class ContactoViewHolder(private val binding: ItemContactoListaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contacto: ContactoTelefono) {
            binding.tvNombre.text = contacto.nombreCompleto
            binding.tvTelefono.text = PhoneNumberFormatter.formatearParaLectura(contacto.numeroTelefono)

            binding.btnLlamar.setIconTintResource(R.color.md_theme_onPrimaryFixed)
            binding.btnLlamar.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.md_theme_primary)

            if (contacto.fotoUri != null) {
                Glide.with(binding.root.context)
                    .load(contacto.fotoUri)
                    .circleCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(binding.ivFotoPerfil)
            } else {
                binding.ivFotoPerfil.setImageResource(R.drawable.ic_launcher_foreground)
            }
            val iconoFavorito = R.drawable.ic_favorite
            val iconoNoFavorito = R.drawable.ic_favorito_borde

            if (contacto.esFavorito) {
                binding.btnFavorito.setIconResource(iconoFavorito)
            } else {
                binding.btnFavorito.setIconResource(iconoNoFavorito)
            }

            binding.btnLlamar.setOnClickListener {
                val textoParaLeer = "Llamando a ${contacto.nombreCompleto}"
                LectorDeVoz.obtenerInstancia().leerEnVozAlta(textoParaLeer, onDone = {
                    GestorDeLlamadas.llamar(itemView.context, contacto.numeroTelefono)
                })
            }
            binding.btnFavorito.setOnClickListener {
                val nuevoEstado = !contacto.esFavorito
                contacto.esFavorito = nuevoEstado

                if (nuevoEstado) {
                    binding.btnFavorito.setIconResource(iconoFavorito)
                    binding.btnFavorito.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.md_theme_onBackground_mediumContrast)
                } else {
                    binding.btnFavorito.setIconResource(iconoNoFavorito)
                    binding.btnFavorito.backgroundTintList = ContextCompat.getColorStateList(itemView.context, android.R.color.transparent)
                }

                alHacerClicEnFavorito(contacto, nuevoEstado)
            }

            binding.root.setOnClickListener {
                alHacerClicEnItem(contacto)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ContactoTelefono>() {
        override fun areItemsTheSame(oldItem: ContactoTelefono, newItem: ContactoTelefono): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ContactoTelefono, newItem: ContactoTelefono): Boolean {
            return oldItem == newItem
        }
    }
}