package com.SamiDev.agendasencilla.ui.gestion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.databinding.FragmentGestionContactoBinding // Corregido el tipo de Binding

class GestionContactoFragment : Fragment() {

    // Binding para acceder a las vistas del layout de forma segura y eficiente.
    private var _binding: FragmentGestionContactoBinding? = null
    private val binding get() = _binding!!

    // ViewModel instanciado usando la Factory personalizada para pasar dependencias (Application context).
    private val viewModel: GestionContactoViewModel by viewModels {
        GestionContactoViewModelFactory(requireActivity().application)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGestionContactoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarListeners()
    }

    /**
     * Configura los listeners para los elementos interactivos de la UI, como el botón de guardar.
     */
    private fun configurarListeners() {
        binding.btnGuardarContacto.setOnClickListener {
            guardarNuevoContacto()
        }

        // TODO: Añadir listener para binding.btnSeleccionarFoto si se implementa la selección de imagen
    }

    /**
     * Recoge los datos de los campos de entrada, los valida y, si son correctos,
     * crea un nuevo objeto [Contacto] y lo guarda a través del ViewModel.
     */
    private fun guardarNuevoContacto() {
        val nombre = binding.etNombreCompleto.text.toString().trim()
        val telefono = binding.etNumeroTelefono.text.toString().trim()
        val notas = binding.etNotasContacto.text.toString().trim()
        val esFavorito = binding.switchFavorito.isChecked
        // Por ahora, la fotoUri será null. Se implementará la selección de imagen más adelante.
        val fotoUri: String? = null

        if (validarEntradas(nombre, telefono)) {
            val nuevoContacto = Contacto(
                nombreCompleto = nombre,
                numeroTelefono = telefono,
                fotoUri = fotoUri, // Se asignará cuando se implemente la selección de foto
                esFavorito = esFavorito,
                notas = notas
            )
            viewModel.guardarContacto(nuevoContacto)
            Toast.makeText(requireContext(), "Contacto guardado exitosamente", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack() // Volver a la pantalla anterior
        } else {
            Toast.makeText(requireContext(), "Por favor, complete el nombre y el teléfono", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Valida que los campos obligatorios (nombre y teléfono) no estén vacíos.
     * @param nombre El nombre del contacto.
     * @param telefono El número de teléfono del contacto.
     * @return `true` si las entradas son válidas, `false` en caso contrario.
     */
    private fun validarEntradas(nombre: String, telefono: String): Boolean {
        return nombre.isNotEmpty() && telefono.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpiar la referencia al binding para evitar memory leaks
    }
}
