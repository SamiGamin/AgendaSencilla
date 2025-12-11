package com.SamiDev.agendasencilla.ui.contactos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.SamiDev.agendasencilla.R
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.databinding.FragmentContactosFavoritosBinding
import com.SamiDev.agendasencilla.util.LectorDeVoz
import com.SamiDev.agendasencilla.util.adapter.FavoritosAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ContactosFavoritosFragment : Fragment() {

    private var _binding: FragmentContactosFavoritosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactosFavoritosViewModel by viewModels {
        ContactosFavoritosViewModelFactory(requireActivity().application)
    }

    private lateinit var favoritosAdapter: FavoritosAdapter
    private lateinit var lectorDeVoz: LectorDeVoz

    companion object {
        private const val TAG = "ContactosFavoritosFrag"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactosFavoritosBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true) // Indicar que este fragmento tiene su propio menú de opciones
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar el LectorDeVoz
        lectorDeVoz = LectorDeVoz.obtenerInstancia()
        lectorDeVoz.inicializar(requireContext())

        configurarRecyclerView()
        configurarObservadores()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? SearchView

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.actualizarTerminoBusqueda(newText.orEmpty())
                return true
            }
        })
    }

    private fun configurarRecyclerView() {
        favoritosAdapter = FavoritosAdapter { contacto ->
            manejarClicEnContacto(contacto)
        }

        binding.rvContactos.apply {
            adapter = favoritosAdapter
        }
    }

    private fun configurarObservadores() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.contactosFavoritos.collectLatest { listaDeContactosFavoritos ->
                favoritosAdapter.submitList(listaDeContactosFavoritos)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lecturaActivada.collectLatest { activada ->
                    Log.d(TAG, "Fragmento observó cambio en preferencia de lectura: $activada. Actualizando adapter.")
                    // Pasamos el estado de la preferencia al adapter
                    favoritosAdapter.actualizarPreferenciaLectura(activada)
                }
            }
        }
    }

    private fun manejarClicEnContacto(contacto: Contacto) {
        val bundle = bundleOf("contactId" to contacto.id)
        findNavController().navigate(R.id.action_contactosFavoritosFragment_to_gestionContactoFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
