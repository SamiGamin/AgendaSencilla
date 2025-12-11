package com.SamiDev.agendasencilla.ui.configuracion

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.SamiDev.agendasencilla.R // Necesario para los IDs de los botones
import com.SamiDev.agendasencilla.data.preferencias.OpcionTamanoFuente // Importar enum
import com.SamiDev.agendasencilla.databinding.FragmentConfiguracionBinding
import kotlinx.coroutines.launch

class ConfiguracionFragment : Fragment() {

    companion object {
        private const val TAG = "ConfiguracionFragment"
    }

    private var _binding: FragmentConfiguracionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ConfiguracionViewModel by viewModels {
        ConfiguracionViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfiguracionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observarOpcionTemaSeleccionada()
        observarOpcionTamanoFuenteSeleccionada()
        observarPreferenciaLecturaVoz() // Observar el estado de la preferencia de lectura
        observarEventoRecrearActividad()
        configurarListeners()
    }

    /**
     * Observa la opción de tema seleccionada desde el ViewModel
     * y actualiza la UI (el MaterialButtonToggleGroup) correspondientemente.
     */
    private fun observarOpcionTemaSeleccionada() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.opcionTemaSeleccionada.collect { opcion ->
                    when (opcion) {
                        OpcionTema.CLARO -> binding.toggleGroupTema.check(R.id.btn_tema_claro)
                        OpcionTema.OSCURO -> binding.toggleGroupTema.check(R.id.btn_tema_oscuro)
                        OpcionTema.SISTEMA -> binding.toggleGroupTema.check(R.id.btn_tema_sistema)
                    }
                }
            }
        }
    }

    /**
     * Observa la opción de tamaño de fuente seleccionada desde el ViewModel
     * y actualiza la UI (el MaterialButtonToggleGroup) correspondientemente.
     */
    private fun observarOpcionTamanoFuenteSeleccionada() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.opcionTamanoFuenteSeleccionada.collect { opcion ->
                    when (opcion) {
                        OpcionTamanoFuente.NORMAL -> binding.toggleGroupTamanoFuente.check(R.id.btn_tamano_normal)
                        OpcionTamanoFuente.GRANDE -> binding.toggleGroupTamanoFuente.check(R.id.btn_tamano_grande)
                        OpcionTamanoFuente.MAS_GRANDE -> binding.toggleGroupTamanoFuente.check(R.id.btn_tamano_mas_grande)
                    }
                }
            }
        }
    }

    /**
     * Observa el estado de la preferencia de lectura en voz desde el ViewModel
     * y actualiza la UI (el SwitchMaterial) para que refleje el valor actual.
     */
    private fun observarPreferenciaLecturaVoz() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.preferenciaLecturaVozActiva.collect { activada ->
                    Log.d(TAG, "UI observing new voice preference state: $activada")
                    // Asegurarse de no entrar en un bucle si el listener también actualiza el ViewModel
                    if (binding.switchLecturaEnVoz.isChecked != activada) {
                        binding.switchLecturaEnVoz.isChecked = activada
                    }
                }
            }
        }
    }

    /**
     * Observa el evento para recrear la actividad desde el ViewModel.
     * Cuando se emite un evento, se llama a activity?.recreate().
     */
    private fun observarEventoRecrearActividad() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventoRecrearActividad.collect {
                    activity?.recreate()
                }
            }
        }
    }

    /**
     * Configura los listeners para los elementos interactivos de la UI.
     */
    private fun configurarListeners() {
        // Listener para el grupo de selección de tema
        binding.toggleGroupTema.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val nuevaOpcion = when (checkedId) {
                    R.id.btn_tema_claro -> OpcionTema.CLARO
                    R.id.btn_tema_oscuro -> OpcionTema.OSCURO
                    R.id.btn_tema_sistema -> OpcionTema.SISTEMA
                    else -> null
                }
                nuevaOpcion?.let {
                    if (viewModel.opcionTemaSeleccionada.value != it) {
                        viewModel.actualizarOpcionTema(it)
                    }
                }
            }
        }

        // Listener para el grupo de selección de tamaño de fuente
        binding.toggleGroupTamanoFuente.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val nuevaOpcion = when (checkedId) {
                    R.id.btn_tamano_normal -> OpcionTamanoFuente.NORMAL
                    R.id.btn_tamano_grande -> OpcionTamanoFuente.GRANDE
                    R.id.btn_tamano_mas_grande -> OpcionTamanoFuente.MAS_GRANDE
                    else -> null
                }
                nuevaOpcion?.let {
                    if (viewModel.opcionTamanoFuenteSeleccionada.value != it) {
                        viewModel.actualizarOpcionTamanoFuente(it)
                    }
                }
            }
        }

        // Listener para el Switch de lectura en voz
        binding.switchLecturaEnVoz.setOnCheckedChangeListener { _, isChecked ->
            Log.d(TAG, "Switch onCheckedChanged listener fired. New state: $isChecked")
            viewModel.actualizarPreferenciaLecturaEnVoz(isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpiar la referencia al binding para evitar memory leaks.
    }
}