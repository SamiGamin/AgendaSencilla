package com.SamiDev.agendasencilla.ui.contactos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.SamiDev.agendasencilla.R
import com.SamiDev.agendasencilla.data.ContactoTelefono
import com.SamiDev.agendasencilla.databinding.FragmentContactosFavoritosBinding
import com.SamiDev.agendasencilla.util.LectorDeVoz
import com.SamiDev.agendasencilla.util.Resultado
import com.SamiDev.agendasencilla.util.adapter.FavoritosAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Fragmento que muestra la lista de contactos favoritos.
 * Permite buscar entre los favoritos y acceder a su detalle.
 */
class ContactosFavoritosFragment : Fragment() {

    private var _binding: FragmentContactosFavoritosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactosFavoritosViewModel by viewModels {
        ContactosFavoritosViewModelFactory(requireActivity().application)
    }

    private lateinit var favoritosAdapter: FavoritosAdapter
    private lateinit var lectorDeVoz: LectorDeVoz

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactosFavoritosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lectorDeVoz = LectorDeVoz.obtenerInstancia()
        lectorDeVoz.inicializar(requireContext())

        configurarMenu()
        configurarRecyclerView()
        configurarObservadores()
    }

    override fun onResume() {
        super.onResume()
        // Recargamos los favoritos al volver a la pantalla para asegurar datos actualizados
        viewModel.cargarFavoritos()
    }

    /**
     * Configura el RecyclerView y su adaptador.
     */
    private fun configurarRecyclerView() {
        favoritosAdapter = FavoritosAdapter()

        binding.rvContactos.apply {
            adapter = favoritosAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    /**
     * Configura los observadores del ViewModel.
     */
    private fun configurarObservadores() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observar lista y estado
                launch {
                    viewModel.estadoUi.collectLatest { resultado ->
                        manejarEstadoUi(resultado)
                    }
                }

                // Observar estado del lector
                launch {
                    viewModel.lecturaActivada.collectLatest { activada ->
                        favoritosAdapter.actualizarPreferenciaLectura(activada)
                    }
                }
            }
        }
    }

    /**
     * Gestiona los cambios en el estado de la UI (Cargando, Éxito, Error).
     */
    private fun manejarEstadoUi(resultado: Resultado<List<ContactoTelefono>>) {
        when(resultado) {
            is Resultado.Cargando -> {
                // Opcional: Mostrar indicador de carga
            }
            is Resultado.Exito -> {
                favoritosAdapter.submitList(resultado.datos)
                if (resultado.datos.isEmpty()) {
                    // Opcional: Mostrar indicación de lista vacía
                }
            }
            is Resultado.Error -> {
                Toast.makeText(requireContext(), resultado.mensaje, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Configura el menú de opciones del fragmento.
     */
    private fun configurarMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_contactos, menu)

                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem?.actionView as? SearchView

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

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_search -> true
                    else -> {
                       NavigationUI.onNavDestinationSelected(menuItem, findNavController())
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
