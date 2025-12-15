package com.SamiDev.agendasencilla.ui.listadocontactos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.SamiDev.agendasencilla.R
import com.SamiDev.agendasencilla.data.ContactoTelefono
import com.SamiDev.agendasencilla.databinding.FragmentListadocontactosBinding
import com.SamiDev.agendasencilla.util.LectorDeVoz
import com.SamiDev.agendasencilla.util.Resultado
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class listadocontactosFragment : Fragment() {


    private var _binding: FragmentListadocontactosBinding? = null
    private val binding get() = _binding!!

    // Usamos la Factory actualizada
    private val viewModel: ListadocontactosViewModel by viewModels {
        ListadocontactosViewModelFactory(requireActivity().application)
    }

    private lateinit var contactoAdapter: ContactoAdapter
    private lateinit var lectorDeVoz: LectorDeVoz

    // Lanzador para solicitar permisos
    private val solicitudPermisoLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { esConcedido: Boolean ->
        if (esConcedido) {
            // Si el usuario acepta, cargamos los contactos
            viewModel.cargarContactosIniciales()
        } else {
            mostrarError("El permiso es necesario para ver los contactos.")
        }
    }

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

        configurarMenu()
        inicializarComponentes()
        configurarRecyclerView()
        verificarPermisosYCargar()
        configurarObservadores()
    }

    private fun inicializarComponentes() {
        lectorDeVoz = LectorDeVoz.obtenerInstancia()
        lectorDeVoz.inicializar(requireContext())
    }

    private fun verificarPermisosYCargar() {
        val permisoEstado = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_CONTACTS
        )

        if (permisoEstado == PackageManager.PERMISSION_GRANTED) {
            viewModel.cargarContactosIniciales()
        } else {
            solicitudPermisoLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun configurarRecyclerView() {
        contactoAdapter = ContactoAdapter(
            alHacerClicEnItem = { contacto ->
                manejarClicEnContacto(contacto)
            },
            alHacerClicEnFavorito = { contacto, esFavorito ->
                // Aquí llamamos al ViewModel
                viewModel.actualizarEstadoFavorito(contacto, esFavorito)
            }
        )

        binding.rvContactos.apply {
            adapter = contactoAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun configurarObservadores() {
        // Observamos el estado de la UI (Cargando, Éxito, Error)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Observar lista de contactos y estado
                launch {
                    viewModel.estadoUi.collectLatest { resultado ->
                        manejarEstadoUi(resultado)
                    }
                }

                // Observar preferencia de lectura de voz
                launch {
                    viewModel.lecturaActivada.collectLatest { activada ->
                        contactoAdapter.actualizarPreferenciaLectura(activada)
                    }
                }
            }
        }
    }

    private fun manejarEstadoUi(resultado: Resultado<List<ContactoTelefono>>) {
        when (resultado) {
            is Resultado.Cargando -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.rvContactos.visibility = View.GONE
            }
            is Resultado.Exito -> {
                binding.progressBar.visibility = View.GONE
                binding.rvContactos.visibility = View.VISIBLE
                contactoAdapter.submitList(resultado.datos)

                if (resultado.datos.isEmpty()) {
                    // Opcional: Mostrar vista de "lista vacía"
                }
            }
            is Resultado.Error -> {
                binding.progressBar.visibility = View.GONE
                mostrarError(resultado.mensaje)
            }
        }
    }

    private fun manejarClicEnContacto(contacto: ContactoTelefono) {
        if (viewModel.lecturaActivada.value) {
            lectorDeVoz.leerEnVozAlta("${contacto.nombreCompleto}")
        }
        // Aquí puedes navegar al detalle si lo deseas
    }

    private fun mostrarError(mensaje: String) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
    }

    // Configuración moderna del menú (sustituye a setHasOptionsMenu)
    private fun configurarMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_contactos, menu) // Asegúrate que este recurso exista

                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as? SearchView

                searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean = true

                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.actualizarTerminoBusqueda(newText.orEmpty())
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}