package com.SamiDev.agendasencilla.ui.listadocontactos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.SamiDev.agendasencilla.R
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.databinding.FragmentListadocontactosBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class listadocontactosFragment : Fragment() {

    private var _binding: FragmentListadocontactosBinding? = null
    private val binding get() = _binding!!

    // ViewModel instanciado usando la Factory personalizada
    private val viewModel: ListadocontactosViewModel by viewModels {
        ListadocontactosViewModelFactory(requireActivity().application)
    }

    private lateinit var ListadocontactoAdapter: ContactoAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListadocontactosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarRecyclerView()
        configurarObservadores()
        configurarListeners()
    }

    private fun configurarRecyclerView() {
        // Inicializar el adapter, pasando las lambdas para el clic en un ítem y en el botón de favorito
        ListadocontactoAdapter = ContactoAdapter(
            onItemClicked = { contacto ->
                manejarClicEnContacto(contacto)
            },
            onFavoritoClicked = { contacto ->
                viewModel.actualizarEstadoFavorito(contacto)
            }
        )

        binding.rvContactos.apply {
            adapter = ListadocontactoAdapter
            layoutManager = LinearLayoutManager(requireContext())
            // Considera añadir ItemDecoration para espaciado si es necesario
        }
    }

    private fun configurarObservadores() {
        // Observar la lista de contactos del ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.todosLosContactos.collectLatest { listaDeContactos ->
                ListadocontactoAdapter.submitList(listaDeContactos)
            }
        }
    }

    private fun configurarListeners() {
        // Listener para el FloatingActionButton para añadir un nuevo contacto
        binding.fabAnadirContacto.setOnClickListener {
            findNavController().navigate(R.id.action_listadocontactosFragment_to_gestionContactoFragment2)
        }

    }

    /**
     * Maneja la acción a realizar cuando se hace clic en un ítem de la lista de contactos.
     * @param contacto El [Contacto] sobre el que se hizo clic.
     */
    private fun manejarClicEnContacto(contacto: Contacto) {
        // Por ahora, muestra un Toast. Más adelante, podría navegar a una pantalla de detalle/edición.
        Toast.makeText(requireContext(), "Contacto seleccionado: ${contacto.nombreCompleto}", Toast.LENGTH_SHORT).show()
        // Ejemplo de navegación a edición (requiere que GestionContactoFragment maneje un argumento de ID):
        // val action = listadocontactosFragmentDirections.actionListadocontactosFragmentToGestionContactoFragment2(contacto.id)
        // findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpiar la referencia al binding para evitar memory leaks
    }
}