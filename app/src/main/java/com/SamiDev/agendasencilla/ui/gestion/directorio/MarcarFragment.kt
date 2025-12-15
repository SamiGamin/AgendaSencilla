package com.SamiDev.agendasencilla.ui.gestion.directorio

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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

class MarcarFragment : Fragment() {

    private var _binding: FragmentMarcarBinding? = null
    private val binding get() = _binding!!

    val REQUEST_CODE_LOG = 100

    private val viewModel: MarcarViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val context = requireContext().applicationContext
                val database = AppDatabase.obtenerInstancia(context)

                // Repositorio 1: Contactos (con soporte Favoritos)
                val repoContactos = ContactoTelefonoRepositorio(context, database.favoritoDao())

                // Repositorio 2: Llamadas
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
        configurarBotonLlamar()
        setupManejadorAtras()
        setupFab()
    }

    private fun setupFab() {
        binding.fabAbrirTeclado.setOnClickListener {
            viewModel.activarModoMarcador()
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Esto BORRA los iconos que vengan del MainActivity (lupa, configuración, etc.)
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
                        // Es importante usar mutate() para no afectar el icono en otras partes de la app
                        val wrappedDrawable = DrawableCompat.wrap(icon).mutate()

                        // Lógica de colores según el ID del ítem
                        val color = when (item.itemId) {
                            R.id.llamada_perdida, R.id.eliminar_registro -> ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
                            R.id.llamada_entrante -> ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark)
                            R.id.llamada_saliente -> ContextCompat.getColor(requireContext(), android.R.color.holo_blue_dark)
                            else -> ContextCompat.getColor(requireContext(), R.color.md_theme_scrim) // Color por defecto
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
            // ¡ESTA LÍNEA ES OBLIGATORIA! Sin ella, la lista es invisible
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            binding.rvSugerencias.adapter = adapterHistorial
        }
        Log.d("MARCADOR_DEBUG", "RecyclerView configurado correctamente")
    }

    private fun observarViewModel() {
        // NUEVO: Observar si mostramos teclado o historial
        viewModel.modoTeclado.observe(viewLifecycleOwner) { mostrarTeclado ->
            val layoutParams = binding.rvSugerencias.layoutParams
            if (mostrarTeclado) {
                // MODO MARCADOR
                binding.gridTeclas.visibility = View.VISIBLE
                binding.linearLayout.visibility = View.VISIBLE // El campo donde sale el número
                binding.fabAbrirTeclado.hide()
                // Ajustar altura del Recycler para compartir pantalla (usando pesos)
                if (layoutParams is android.widget.LinearLayout.LayoutParams) {
                    layoutParams.height = 0
                    layoutParams.weight = 0.8f // Usamos 'weight' normal, no 'verticalWeight'
                    binding.rvSugerencias.layoutParams = layoutParams
                }
                // CÓDIGO PARA CONSTRAINT LAYOUT (Por si acaso cambias el XML luego)
                else if (layoutParams is androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
                    layoutParams.height = 0
                    layoutParams.verticalWeight = 0.8f
                    binding.rvSugerencias.layoutParams = layoutParams
                }

            } else {
                // MODO HISTORIAL (Pantalla Completa)
                binding.gridTeclas.visibility = View.GONE
                binding.linearLayout.visibility = View.GONE
                binding.fabAbrirTeclado.show()

                if (layoutParams is android.widget.LinearLayout.LayoutParams) {
                    layoutParams.height = 0
                    layoutParams.weight = 1f // Peso 1 para llenar todo
                    binding.rvSugerencias.layoutParams = layoutParams
                }
                else if (layoutParams is androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
                    layoutParams.height = 0
                    layoutParams.verticalWeight = 0f
                    layoutParams.bottomToBottom = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
                    binding.rvSugerencias.layoutParams = layoutParams
                }
            }
        }

        // Esto actualiza el EditText
        viewModel.numeroActual.observe(viewLifecycleOwner) { numero ->
            binding.etNumero.setText(numero)
            binding.etNumero.setSelection(numero.length)

            if (numero.isEmpty()) {
                // Si borra el número -> Mostrar Historial
                binding.rvSugerencias.adapter = adapterHistorial
                viewModel.cargarHistorial() // Recargar historial fresco
            } else {
                // Si hay números -> Mostrar Sugerencias
                binding.rvSugerencias.adapter = adapterSugerencias
            }
        }
// B. Observar datos del HISTORIAL
        viewModel.historial.observe(viewLifecycleOwner) { listaLlamadas ->
            // Solo actualizamos si el adaptador actual es el de historial
            if (binding.rvSugerencias.adapter == adapterHistorial) {
                adapterHistorial.submitList(listaLlamadas)
            }
        }

        viewModel.sugerencias.observe(viewLifecycleOwner) { lista ->
            Log.d("MARCADOR_DEBUG", "Sugerencias recibidas: ${lista.size} contactos")
            lista.forEach { contacto ->
                Log.d("MARCADOR_DEBUG", "→ ${contacto.nombreCompleto} | ${contacto.numeroTelefono}")
            }
            adapterSugerencias.submitList(lista)
//            binding.rvSugerencias.visibility = if (lista.isEmpty()) View.GONE else View.VISIBLE
            binding.rvSugerencias.visibility = View.VISIBLE
        }

        // Llamar desde sugerencia o FAB
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
                    // Si estamos viendo historial, volvemos al teclado
                    viewModel.activarModoMarcador()
                    // Opcional: Restaurar la lista por defecto
                    binding.rvSugerencias.adapter = adapterHistorial // o adapterSugerencias
                } else {
                    // Si ya estamos en el teclado, el comportamiento normal (salir o ir atrás)
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
                    val digito = btn.text.toString()
                    viewModel.agregarDigito(btn.text.toString())
                    Log.d("MARCADOR_DEBUG", "Tecla pulsada: $digito → número actual: ${viewModel.numeroActual.value}")
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
            viewModel.solicitarLlamada()
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