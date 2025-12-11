package com.SamiDev.agendasencilla.ui.gestion.directorio

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.SamiDev.agendasencilla.R
import com.SamiDev.agendasencilla.data.database.AppDatabase
import com.SamiDev.agendasencilla.data.repository.ContactoRepositorio
import com.SamiDev.agendasencilla.databinding.FragmentMarcarBinding
import com.SamiDev.agendasencilla.util.PhoneNumberFormatter
import com.SamiDev.agendasencilla.util.adapter.ContactoSugerenciaAdapter

class MarcarFragment : Fragment() {

    private var _binding: FragmentMarcarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MarcarViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val dao = AppDatabase.obtenerInstancia(requireContext()).contactoDao()
                val repo = ContactoRepositorio(dao)
                @Suppress("UNCHECKED_CAST")
                return MarcarViewModel(repo) as T
            }
        }
    }

    private lateinit var adapter: ContactoSugerenciaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarcarBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PhoneNumberFormatter.formatar(binding.etNumero)

        setupRecyclerView()
        observarViewModel()
        configurarTeclas()
        configurarBotonBorrar()
        configurarBotonLlamar()
    }

    private fun setupRecyclerView() {
        adapter = ContactoSugerenciaAdapter { numero -> llamar(numero) }
        binding.rvSugerencias.adapter = adapter
    }

    private fun observarViewModel() {
        viewModel.numeroActual.observe(viewLifecycleOwner) { numero ->
            binding.etNumero.setText(numero)
            binding.etNumero.setSelection(numero.length)
        }

        viewModel.sugerencias.observe(viewLifecycleOwner) { lista ->
            adapter.submitList(lista)
            binding.rvSugerencias.visibility = if (lista.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun configurarTeclas() {
        binding.gridTeclas.children.forEach { btn ->
            if (btn is com.google.android.material.button.MaterialButton) {
                btn.setOnClickListener {
                    viewModel.agregarDigito(btn.text.toString())
                }
            }
        }
    }

    private fun configurarBotonBorrar() {
        var ultimoClick = 0L
        binding.btnBorrar.setOnClickListener {
            val ahora = System.currentTimeMillis()
            if (ahora - ultimoClick < 400) {
                viewModel.borrarTodo()
            } else {
                viewModel.borrarUltimoDigito()
            }
            ultimoClick = ahora
        }
        binding.btnBorrar.setOnLongClickListener {
            viewModel.borrarTodo()
            true
        }
    }

    private fun configurarBotonLlamar() {
        binding.btnAgregar.setOnClickListener {
            val numero = viewModel.obtenerNumeroParaLlamar()
            if (numero.isNotEmpty()) llamar(numero)
        }
    }

    private fun llamar(numeroConFormato: String) {
        val numeroLimpio = numeroConFormato.replace(" ", "")
        if (numeroLimpio.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$numeroLimpio")
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}