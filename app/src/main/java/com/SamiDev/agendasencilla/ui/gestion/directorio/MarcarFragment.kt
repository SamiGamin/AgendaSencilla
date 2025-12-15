package com.SamiDev.agendasencilla.ui.gestion.directorio

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.view.menu.MenuBuilder
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.SamiDev.agendasencilla.R
import com.SamiDev.agendasencilla.data.database.AppDatabase
import com.SamiDev.agendasencilla.data.repository.ContactoTelefonoRepositorio
import com.SamiDev.agendasencilla.data.repository.LlamadasRepositorio
import com.SamiDev.agendasencilla.databinding.FragmentMarcarBinding
import com.SamiDev.agendasencilla.util.PhoneNumberFormatter
import com.SamiDev.agendasencilla.util.adapter.ContactoSugerenciaAdapter
import com.SamiDev.agendasencilla.util.adapter.HistorialAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

/**
 * Fragmento que implementa el marcador telefónico (Dialer).
 * Permite marcar números, ver historial de llamadas, buscar sugerencias de contactos y realizar llamadas.
 */
@Suppress("DEPRECATION")
class MarcarFragment : Fragment() {

    private var _binding: FragmentMarcarBinding? = null
    private val binding get() = _binding!!

    private val REQUEST_CODE_LOG = 100

    private val viewModel: MarcarViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val context = requireContext().applicationContext
                val database = AppDatabase.obtenerInstancia(context)
                val repoContactos = ContactoTelefonoRepositorio(context, database.favoritoDao())
                val repoLlamadas = LlamadasRepositorio(context)

                @Suppress("UNCHECKED_CAST")
                return MarcarViewModel(repoContactos, repoLlamadas) as T
            }
        }
    }

    private lateinit var adapterSugerencias: ContactoSugerenciaAdapter
    private lateinit var adapterHistorial: HistorialAdapter

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

        setupMenu()
        setupRecyclerView()
        observarViewModel()
        configurarTeclas()
        configurarBotonBorrar()
        setupBtnAgregar()
        setupManejadorAtras()
        setupFab()
    }

    private fun setupFab() {
        binding.fabAbrirTeclado.setOnClickListener {
            viewModel.activarModoMarcador()
        }
    }

    /**
     * Configura el menú de opciones para filtrar historial de llamadas.
     */
    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.menu_historial_llamadas, menu)
            }

            @SuppressLint("RestrictedApi")
            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                if (menu is MenuBuilder) {
                    menu.setOptionalIconsVisible(true)
                }
                for (i in 0 until menu.size()) {
                    val item = menu.getItem(i)
                    val icon = item.icon

                    if (icon != null) {
                        val wrappedDrawable = DrawableCompat.wrap(icon).mutate()
                        val color = when (item.itemId) {
                            R.id.llamada_perdida, R.id.eliminar_registro -> ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                            R.id.llamada_entrante -> ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
                            R.id.llamada_saliente -> ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)
                            else -> ContextCompat.getColor(requireContext(), R.color.md_theme_scrim)
                        }
                        DrawableCompat.setTint(wrappedDrawable, color)
                        item.icon = wrappedDrawable
                    }
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CALL_LOG)
                    != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(android.Manifest.permission.READ_CALL_LOG), REQUEST_CODE_LOG)
                    return false
                }
                return when (menuItem.itemId) {
                    R.id.llamada_saliente -> {
                        viewModel.cargarHistorial(CallLog.Calls.OUTGOING_TYPE)
                        viewModel.activarModoHistorial()
                        true
                    }
                    R.id.llamada_entrante -> {
                        viewModel.cargarHistorial(CallLog.Calls.INCOMING_TYPE)
                        viewModel.activarModoHistorial()
                        true
                    }
                    R.id.llamada_perdida -> {
                        viewModel.cargarHistorial(CallLog.Calls.MISSED_TYPE)
                        viewModel.activarModoHistorial()
                        true
                    }
                    R.id.eliminar_registro -> {
                        true
                    }
                    else ->{
                        viewModel.cargarHistorial(null)
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupRecyclerView() {
        adapterSugerencias = ContactoSugerenciaAdapter { numero -> llamar(numero) }
        adapterHistorial = HistorialAdapter { numero -> llamar(numero) }
        binding.rvSugerencias.apply {
            layoutManager = LinearLayoutManager(context)
            binding.rvSugerencias.adapter = adapterHistorial
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 10) {
                        if (viewModel.modoTeclado.value == true) {
                            viewModel.activarModoHistorial()
                        }
                    }
                }
            })
        }
    }

    private fun observarViewModel() {
        viewModel.modoTeclado.observe(viewLifecycleOwner) { mostrarTeclado ->
            binding.gridTeclas.visibility = if (mostrarTeclado) View.VISIBLE else View.GONE
            binding.linearLayout.visibility = if (mostrarTeclado) View.VISIBLE else View.GONE
            if (mostrarTeclado) binding.fabAbrirTeclado.hide() else binding.fabAbrirTeclado.show()
            val params = binding.rvSugerencias.layoutParams
            if (params is ConstraintLayout.LayoutParams) {
                if (mostrarTeclado) {
                    params.height = 0
                    params.bottomToTop = binding.linearLayout.id
                    params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
                    params.verticalWeight = 0f
                }
                else{
                    params.height = 0
                    params.bottomToTop = ConstraintLayout.LayoutParams.UNSET
                    params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    params.verticalWeight = 0f
                }
                binding.rvSugerencias.layoutParams = params
                binding.rvSugerencias.requestLayout()

            }
        }

        viewModel.numeroActual.observe(viewLifecycleOwner) { numero ->
            binding.etNumero.setText(numero)
            binding.etNumero.setSelection(numero.length)

            val longitudReal = numero.replace(" ", "").length

            if (longitudReal > 3) {
                binding.btnAgregar.visibility = View.VISIBLE
            } else {
                binding.btnAgregar.visibility = View.GONE
            }
            if (numero.isEmpty()) {
                binding.rvSugerencias.adapter = adapterHistorial
                viewModel.cargarHistorial()
            } else {
                binding.rvSugerencias.adapter = adapterSugerencias
            }
        }

        viewModel.historial.observe(viewLifecycleOwner) { listaLlamadas ->
            if (binding.rvSugerencias.adapter == adapterHistorial) {
                adapterHistorial.submitList(listaLlamadas)
            }
        }

        viewModel.sugerencias.observe(viewLifecycleOwner) { lista ->
            adapterSugerencias.submitList(lista)
            binding.rvSugerencias.visibility = View.VISIBLE
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.eventoLlamar.collect { numero ->
                llamar(numero)
            }
        }
    }

    private fun setupManejadorAtras() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewModel.modoTeclado.value == false) {
                    viewModel.activarModoMarcador()
                    binding.rvSugerencias.adapter = adapterHistorial
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
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

    private fun setupBtnAgregar() {
        binding.btnAgregar.setOnClickListener {
            val numeroAguardar = viewModel.numeroActual.value.orEmpty()
            if (numeroAguardar.isNotEmpty()) {
                try {
                    val intent = Intent(Intent.ACTION_INSERT).apply {
                        type = ContactsContract.Contacts.CONTENT_TYPE
                        putExtra(ContactsContract.Intents.Insert.PHONE, numeroAguardar)
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error al abrir contactos", Toast.LENGTH_SHORT).show()
                }
            } else {
             Toast.makeText(requireContext(), "Escribe un número primero", Toast.LENGTH_SHORT).show()
            }
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