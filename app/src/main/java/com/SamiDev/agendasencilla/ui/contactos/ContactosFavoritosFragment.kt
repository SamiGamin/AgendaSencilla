package com.SamiDev.agendasencilla.ui.contactos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
// Ya no se necesita GridLayoutManager aquí si está en el XML, pero no hace daño mantenerlo por si se cambia
// import androidx.recyclerview.widget.GridLayoutManager
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.databinding.FragmentContactosFavoritosBinding
import com.SamiDev.agendasencilla.util.adapter.FavoritosAdapter // Cambiado de ContactoAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ContactosFavoritosFragment : Fragment() {

    private var _binding: FragmentContactosFavoritosBinding? = null
    private val binding get() = _binding!!

    // ViewModel instanciado usando la Factory personalizada
    private val viewModel: ContactosFavoritosViewModel by viewModels {
        ContactosFavoritosViewModelFactory(requireActivity().application)
    }

    private lateinit var favoritosAdapter: FavoritosAdapter // Cambiado de ContactoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactosFavoritosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarRecyclerView()
        configurarObservadores()
    }

    private fun configurarRecyclerView() {
        // Inicializar el nuevo FavoritosAdapter, pasando la lambda para el clic en un ítem
        favoritosAdapter = FavoritosAdapter { contacto -> // Cambiado de ContactoAdapter
            manejarClicEnContacto(contacto)
        }

        binding.rvContactos.apply {
            adapter = favoritosAdapter // Usar el nuevo adapter
            // GridLayoutManager ya está definido en el XML (app:layoutManager y app:spanCount)
            // Si se necesitara configurar programáticamente:
            // layoutManager = GridLayoutManager(requireContext(), 2) // El spanCount es 2 según el XML
        }
    }

    private fun configurarObservadores() {
        // Observar la lista de contactos favoritos del ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.contactosFavoritos.collectLatest { listaDeContactosFavoritos ->
                favoritosAdapter.submitList(listaDeContactosFavoritos) // Usar el nuevo adapter
            }
        }
    }

    /**
     * Maneja la acción a realizar cuando se hace clic en un ítem de la lista de contactos favoritos.
     * @param contacto El [Contacto] sobre el que se hizo clic.
     */
    private fun manejarClicEnContacto(contacto: Contacto) {
        Toast.makeText(requireContext(), "Favorito seleccionado: ${contacto.nombreCompleto}", Toast.LENGTH_SHORT).show()
        // Aquí podrías añadir navegación a una pantalla de detalle o edición si es necesario.
        // Ejemplo:
        // val action = ContactosFavoritosFragmentDirections.actionContactosFavoritosFragmentToGestionContactoFragment(contacto.id)
        // findNavController().navigate(action) // Necesitarías definir esta acción en nav_graph.xml y pasar el ID
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpiar la referencia al binding para evitar memory leaks
    }
}