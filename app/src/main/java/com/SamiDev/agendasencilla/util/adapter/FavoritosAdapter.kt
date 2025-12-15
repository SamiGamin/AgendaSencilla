package com.SamiDev.agendasencilla.util.adapter

import android.content.Intent
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.SamiDev.agendasencilla.R
import com.SamiDev.agendasencilla.data.ContactoTelefono
import com.SamiDev.agendasencilla.databinding.ItemContactoBinding
import com.SamiDev.agendasencilla.ui.listadocontactos.ContactoAdapter
import com.SamiDev.agendasencilla.util.GestorDeLlamadas
import com.SamiDev.agendasencilla.util.LectorDeVoz
import com.SamiDev.agendasencilla.util.PhoneNumberFormatter
import com.bumptech.glide.Glide

/**
 * Adaptador para mostrar la lista de contactos favoritos en un RecyclerView.
 * Gestiona la visualización de datos, carga de imágenes e interacciones como llamadas y edición.
 */
class FavoritosAdapter : ListAdapter<ContactoTelefono, FavoritosAdapter.FavoritoViewHolder>(
    ContactoAdapter.DiffCallback
) {
    private var lecturaContactoHabilitada: Boolean = false

    companion object {
        private val TAG = FavoritosAdapter::class.java.simpleName
    }

    /**
     * ViewHolder para los elementos de la lista de favoritos.
     */
    inner class FavoritoViewHolder(private val binding: ItemContactoBinding) : RecyclerView.ViewHolder(binding.root) {
        /**
         * Vincula los datos del contacto con las vistas.
         *
         * @param contacto El objeto [ContactoTelefono] a mostrar.
         */
        fun bind(contacto: ContactoTelefono) {
            binding.tvNombreContacto.text = contacto.nombreCompleto
            binding.tvNombreContacto.isSelected = true

            binding.tvTelefonoContacto.text = PhoneNumberFormatter.formatearParaLectura(contacto.numeroTelefono)

            binding.btnLlamar.setIconTintResource(R.color.md_theme_onPrimaryFixed)
            binding.btnLlamar.backgroundTintList = ContextCompat.getColorStateList(itemView.context, R.color.md_theme_primary)

            if (contacto.fotoUri != null) {
                Glide.with(itemView.context)
                    .load(contacto.fotoUri)
                    .placeholder(R.drawable.ic_face)
                    .error(R.drawable.ic_face)
                    .circleCrop()
                    .into(binding.ivFotoContacto)
            } else {
                binding.ivFotoContacto.setImageResource(R.drawable.ic_face)
            }

            binding.btnLlamar.setOnClickListener {
                val textoParaLeer = "Llamando a ${contacto.nombreCompleto}"
                LectorDeVoz.obtenerInstancia().leerEnVozAlta(textoParaLeer, onDone = {
                    GestorDeLlamadas.llamar(itemView.context, contacto.numeroTelefono)
                })
            }

            binding.btnEditarContacto.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    val uri = android.net.Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contacto.id)
                    intent.data = uri
                    itemView.context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Error abriendo contacto nativo", e)
                }
            }

            itemView.setOnClickListener {
                if (lecturaContactoHabilitada) {
                    val textoParaLeer = contacto.nombreCompleto
                    Log.d(TAG, "Leyendo: $textoParaLeer")
                    LectorDeVoz.obtenerInstancia().leerEnVozAlta(textoParaLeer)
                }
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

    /**
     * Actualiza la preferencia de lectura en voz alta al tocar un contacto.
     *
     * @param estaActivada `true` si la lectura está habilitada, `false` en caso contrario.
     */
    fun actualizarPreferenciaLectura(estaActivada: Boolean) {
        lecturaContactoHabilitada = estaActivada
    }
}