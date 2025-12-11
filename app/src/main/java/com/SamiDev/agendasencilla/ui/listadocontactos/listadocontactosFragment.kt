package com.SamiDev.agendasencilla.ui.listadocontactos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.SamiDev.agendasencilla.R
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.databinding.FragmentListadocontactosBinding
import com.SamiDev.agendasencilla.util.LectorDeVoz
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class listadocontactosFragment : Fragment() {

    private var _binding: FragmentListadocontactosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ListadocontactosViewModel by viewModels {
        ListadocontactosViewModelFactory(requireActivity().application)
    }

    private lateinit var ListadocontactoAdapter: ContactoAdapter
    private lateinit var lectorDeVoz: LectorDeVoz

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListadocontactosBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true) // Indicar que este fragmento tiene su propio menú de opciones
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lectorDeVoz = LectorDeVoz.obtenerInstancia()
        lectorDeVoz.inicializar(requireContext())

        configurarRecyclerView()
        configurarObservadores()
        configurarListeners()
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
        ListadocontactoAdapter = ContactoAdapter(
            onItemClicked = {
                manejarClicEnContacto(it)
            },
            onFavoritoClicked = {
                viewModel.actualizarEstadoFavorito(it)
            }
        )

        binding.rvContactos.apply {
            adapter = ListadocontactoAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun configurarObservadores() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.todosLosContactos.collectLatest { listaDeContactos ->
                ListadocontactoAdapter.submitList(listaDeContactos)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lecturaActivada.collectLatest { activada ->
                    android.util.Log.d("ListadoContactosFrag", "Fragmento observó cambio en preferencia: $activada")
                    ListadocontactoAdapter.actualizarPreferenciaLectura(activada)
                }
            }
        }
    }

    private fun configurarListeners() {
        binding.fabAnadirContacto.setOnClickListener {
            findNavController().navigate(R.id.action_listadocontactosFragment_to_gestionContactoFragment)
        }
    }

    private fun manejarClicEnContacto(contacto: Contacto) {
//        Toast.makeText(requireContext(), "Contacto seleccionado: ${contacto.nombreCompleto}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}