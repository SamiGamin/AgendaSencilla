package com.SamiDev.agendasencilla.ui.listadocontactos

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.SamiDev.agendasencilla.R
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.databinding.ItemContactoListaBinding
import com.SamiDev.agendasencilla.util.GestorDeLlamadas
import com.SamiDev.agendasencilla.util.LectorDeVoz
import com.bumptech.glide.Glide

class ContactoAdapter(
    private val onItemClicked: (Contacto) -> Unit,
    private val onFavoritoClicked: (Contacto) -> Unit
) :
    ListAdapter<Contacto, ContactoAdapter.ContactoViewHolder>(DiffCallback) {

    private var lecturaContactoHabilitada: Boolean = false

    inner class ContactoViewHolder(private val binding: ItemContactoListaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Listener para el clic en el ítem completo
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val contacto = getItem(position)
                    Log.d("ContactoAdapter", "Clic en item. Lectura habilitada: $lecturaContactoHabilitada")
                    // Leer el nombre si la preferencia está activada
                    if (lecturaContactoHabilitada) {
                        LectorDeVoz.obtenerInstancia().leerEnVozAlta(contacto.nombreCompleto)
                    }
                    // Ejecutar la acción de edición/navegación
                    onItemClicked(contacto)
                }
            }

            // Listener para llamar al hacer clic en la foto
            binding.ivFotoContacto.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val contacto = getItem(position)
                    val textoParaLeer = "Llamando a ${contacto.nombreCompleto}"
                    // Siempre leer en voz alta y luego llamar
                    LectorDeVoz.obtenerInstancia().leerEnVozAlta(textoParaLeer, onDone = {
                        GestorDeLlamadas.llamar(itemView.context, contacto.numeroTelefono)
                    })
                }
            }

            // Listener para el clic en el botón de favorito
            binding.btnfavorito.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onFavoritoClicked(getItem(position))
                }
            }
        }

        fun bind(contacto: Contacto) {
            binding.tvNombreContacto.text = contacto.nombreCompleto
            binding.tvNumeroTelefono.text = contacto.numeroTelefono
            binding.ivFotoContacto.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.md_theme_primary)
            // Cargar imagen del contacto con Glide
            Glide.with(itemView.context)
                .load(contacto.fotoUri)
                .placeholder(R.drawable.ic_face)
                .error(R.drawable.ic_face)
                .circleCrop()
                .into(binding.ivFotoContacto)

            // Actualizar el ícono y el fondo del botón de favorito según el estado del contacto
            if (contacto.esFavorito) {
                binding.btnfavorito.setIconResource(R.drawable.ic_favorite)
                binding.btnfavorito.iconTint =
                    ContextCompat.getColorStateList(itemView.context, R.color.md_theme_onPrimaryFixed)
                binding.btnfavorito.backgroundTintList =
                    ContextCompat.getColorStateList(itemView.context, R.color.md_theme_primary)
            } else {
                binding.btnfavorito.setIconResource(R.drawable.ic_favorito_borde) // Corregido para usar el ícono de borde
                binding.btnfavorito.iconTint =
                    ContextCompat.getColorStateList(itemView.context, R.color.md_theme_primary)
                // Restaura el estilo outlined por defecto (sin tinte de fondo explícito)
                binding.btnfavorito.backgroundTintList = null
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactoViewHolder {
        val binding = ItemContactoListaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContactoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactoViewHolder, position: Int) {
        val contactoActual = getItem(position)
        holder.bind(contactoActual)
    }

    /**
     * Método público para actualizar el estado de la preferencia desde el Fragment.
     */
    fun actualizarPreferenciaLectura(estaActivada: Boolean) {
        if (lecturaContactoHabilitada != estaActivada) {
            Log.d("ContactoAdapter", "Adapter recibió nueva preferencia de lectura: $estaActivada")
            lecturaContactoHabilitada = estaActivada
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Contacto>() {
        override fun areItemsTheSame(oldItem: Contacto, newItem: Contacto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contacto, newItem: Contacto): Boolean {
            return oldItem == newItem
        }
    }
}
