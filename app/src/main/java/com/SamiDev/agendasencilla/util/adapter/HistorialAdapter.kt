package com.SamiDev.agendasencilla.util.adapter

import android.graphics.Color
import android.provider.CallLog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.SamiDev.agendasencilla.R
import com.SamiDev.agendasencilla.data.database.LlamadaLog
import com.SamiDev.agendasencilla.databinding.ItemHistorialBinding

class HistorialAdapter (private val onClick: (String) -> Unit
) : ListAdapter<LlamadaLog, HistorialAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemHistorialBinding.bind(view)

        fun bind(item: LlamadaLog) {
            // Mostrar nombre si existe, sino el número
            binding.txtNumeroONombre.text = if (!item.nombre.isNullOrEmpty()) item.nombre else item.numero

            // Formatear fecha (puedes usar SimpleDateFormat aquí o en el repo)
            binding.txtFecha.text = "${item.duracion}" // Aquí puedes concatenar la fecha

            // Lógica de iconos y colores
            val (icono, color) = when (item.tipo) {
                CallLog.Calls.INCOMING_TYPE -> Pair(R.drawable.call_received, Color.GREEN)
                CallLog.Calls.OUTGOING_TYPE -> Pair(R.drawable.call_made, Color.BLUE)
                CallLog.Calls.MISSED_TYPE -> Pair(R.drawable.call_missed, Color.RED)
                else -> Pair(R.drawable.ic_call, Color.GRAY)
            }
            binding.imgTipo.setImageResource(icono)
            binding.imgTipo.setColorFilter(color)

            // Click en todo el ítem o en el botón llamar
            binding.root.setOnClickListener { onClick(item.numero) }
            binding.btnLlamar.setOnClickListener { onClick(item.numero) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_historial, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<LlamadaLog>() {
        override fun areItemsTheSame(oldItem: LlamadaLog, newItem: LlamadaLog) = oldItem.fecha == newItem.fecha
        override fun areContentsTheSame(oldItem: LlamadaLog, newItem: LlamadaLog) = oldItem == newItem
    }
}