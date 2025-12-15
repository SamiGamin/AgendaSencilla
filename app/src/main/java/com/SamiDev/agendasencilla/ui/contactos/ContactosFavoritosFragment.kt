package com.SamiDev.agendasencilla.ui.contactos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
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
        // IMPORTANTE: Recargamos los favoritos cada vez que la vista se hace visible
        // por si el usuario agregó uno nuevo en la otra pestaña.
        viewModel.cargarFavoritos()
    }

    private fun configurarRecyclerView() {
        favoritosAdapter = FavoritosAdapter() // Ya no necesita lambda de navegación, el adapter maneja los Intents

        binding.rvContactos.apply {
            adapter = favoritosAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun configurarObservadores() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Observar lista y estado
                launch {
                    viewModel.estadoUi.collectLatest { resultado ->
                        manejarEstadoUi(resultado)
                    }
                }

                // Observar lector
                launch {
                    viewModel.lecturaActivada.collectLatest { activada ->
                        favoritosAdapter.actualizarPreferenciaLectura(activada)
                    }
                }
            }
        }
    }

    private fun manejarEstadoUi(resultado: Resultado<List<ContactoTelefono>>) {
        when(resultado) {
            is Resultado.Cargando -> {
                // Puedes mostrar un ProgressBar si tienes uno en el XML
                // binding.progressBar.visibility = View.VISIBLE
            }
            is Resultado.Exito -> {
                // binding.progressBar.visibility = View.GONE
                favoritosAdapter.submitList(resultado.datos)
                if (resultado.datos.isEmpty()) {
                    // Opcional: Mostrar texto "No hay favoritos aún"
                }
            }
            is Resultado.Error -> {
                // binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), resultado.mensaje, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarMenu() {
        val menuHost: MenuHost = requireActivity()

        // Añadimos el MenuProvider asociado al ciclo de vida de la vista (viewLifecycleOwner)
        menuHost.addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // 1. Inflar el menú
                menuInflater.inflate(R.menu.menu_contactos, menu)

                // 2. Configurar el SearchView
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
                // 3. Manejar clics en opciones del menú
                return when (menuItem.itemId) {
                    R.id.action_search -> {
                        // El SearchView ya se maneja solo, pero retornamos true para indicar consumo
                        true
                    }
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
