package com.SamiDev.agendasencilla.ui.listadocontactos

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.SamiDev.agendasencilla.R // Importar R para acceder a los drawables
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.databinding.ItemContactoListaBinding
import com.bumptech.glide.Glide // Importar Glide

/**
 * Adaptador para el RecyclerView que muestra la lista de contactos.
 * Utiliza ListAdapter con DiffUtil para un rendimiento eficiente al actualizar la lista.
 *
 * @param onItemClicked Lambda que se ejecuta al hacer clic en un ítem.
 * @param onFavoritoClicked Lambda que se ejecuta al hacer clic en el botón de favorito de un ítem.
 */
class ContactoAdapter(
    private val onItemClicked: (Contacto) -> Unit,
    private val onFavoritoClicked: (Contacto) -> Unit // Nueva lambda para el clic en favorito
) :
    ListAdapter<Contacto, ContactoAdapter.ContactoViewHolder>(DiffCallback) {

    /**
     * ViewHolder para cada ítem de contacto.
     * Contiene las referencias a las vistas definidas en item_contacto_lista.xml.
     */
    inner class ContactoViewHolder(private val binding: ItemContactoListaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Listener para el clic en el ítem completo
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClicked(getItem(position))
                }
            }
            binding.btnLlamar.setOnClickListener {
                Toast.makeText(itemView.context, "Llamando...", Toast.LENGTH_SHORT).show()
            }

            // Listener para el clic en el botón de favorito
            binding.btnfavorito.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onFavoritoClicked(getItem(position))
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
            binding.btnLlamar.setIconTintResource(R.color.md_theme_onPrimaryFixed)
            binding.btnLlamar.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.md_theme_primary)
            // Cargar imagen del contacto con Glide
            Glide.with(itemView.context)
                .load(contacto.fotoUri) // fotoUri es un String?, Glide lo maneja bien.
                .placeholder(R.drawable.ic_face) // Placeholder
                .error(R.drawable.ic_face)       // Imagen de error
                .circleCrop() // Para hacer la imagen circular
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

    companion object DiffCallback : DiffUtil.ItemCallback<Contacto>() {
        override fun areItemsTheSame(oldItem: Contacto, newItem: Contacto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contacto, newItem: Contacto): Boolean {
            return oldItem == newItem
        }
    }
}