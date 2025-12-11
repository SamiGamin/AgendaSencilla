package com.SamiDev.agendasencilla.util.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.SamiDev.agendasencilla.R
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.databinding.ItemContactoBinding
import com.SamiDev.agendasencilla.util.ContactoDiffCallback
import com.SamiDev.agendasencilla.util.GestorDeLlamadas
import com.SamiDev.agendasencilla.util.LectorDeVoz
import com.bumptech.glide.Glide

class FavoritosAdapter(private val onEditClicked: (Contacto) -> Unit) : ListAdapter<Contacto, FavoritosAdapter.FavoritoViewHolder>(
    ContactoDiffCallback()
) {
    private var lecturaContactoHabilitada: Boolean = false

    companion object {
        private const val TAG = "FavoritosAdapter"
    }

    inner class FavoritoViewHolder(private val binding: ItemContactoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contacto: Contacto) {
            binding.tvNombreContacto.text = contacto.nombreCompleto
            binding.tvNombreContacto.isSelected = true
            binding.btnLlamar.setIconTintResource(R.color.md_theme_onPrimaryFixed)
            binding.btnLlamar.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.md_theme_primary)

            Glide.with(itemView.context)
                .load(contacto.fotoUri)
                .placeholder(R.drawable.ic_face)
                .error(R.drawable.ic_face)
                .circleCrop()
                .into(binding.ivFotoContacto)

            // La lógica para llamar permanece igual.
            binding.btnLlamar.setOnClickListener {
                val textoParaLeer = "Llamando a ${contacto.nombreCompleto}"
                LectorDeVoz.obtenerInstancia().leerEnVozAlta(textoParaLeer, onDone = {
                    GestorDeLlamadas.llamar(itemView.context, contacto.numeroTelefono)
                })
            }

            // Se asigna la acción de edición SOLO al nuevo botón.
            binding.btnEditarContacto.setOnClickListener {
                onEditClicked(contacto)
            }

            // Configurar la acción de clic en toda la tarjeta
            itemView.setOnClickListener {
                if (lecturaContactoHabilitada) {
                    val textoParaLeer = contacto.nombreCompleto
                    Log.d(TAG, "Clic detectado, leyendo en voz alta: '$textoParaLeer'")
                    LectorDeVoz.obtenerInstancia().leerEnVozAlta(textoParaLeer)
                } else {
                    Log.d(TAG, "Clic detectado, pero la lectura en voz está desactivada.")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritoViewHolder {
        val binding = ItemContactoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoritoViewHolder(binding)
    }

    /**
     * Método público para actualizar el estado de la preferencia desde el Fragment.
     */
    fun actualizarPreferenciaLectura(estaActivada: Boolean) {
        if (lecturaContactoHabilitada != estaActivada) {
            Log.d(TAG, "Adapter recibió nueva preferencia de lectura: $estaActivada")
            lecturaContactoHabilitada = estaActivada
            // No es necesario notificar cambios en el dataset, ya que la lógica ahora es bajo demanda (al hacer clic).
        }
    }

    override fun onBindViewHolder(holder: FavoritoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}