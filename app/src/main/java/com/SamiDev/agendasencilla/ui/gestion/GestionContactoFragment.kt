package com.SamiDev.agendasencilla.ui.gestion

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.SamiDev.agendasencilla.R
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.databinding.FragmentGestionContactoBinding
import com.SamiDev.agendasencilla.util.ContactosImporter
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class GestionContactoFragment : Fragment() {

    private var _binding: FragmentGestionContactoBinding? = null
    private val binding get() = _binding!!
    private lateinit var contactosImporter: ContactosImporter

    private val viewModel: GestionContactoViewModel by viewModels {
        GestionContactoViewModelFactory(requireActivity().application)
    }

    private lateinit var requestPermisoLauncher: ActivityResultLauncher<String>
    private lateinit var selectorImagenLauncher: ActivityResultLauncher<String>
    private var fotoSeleccionadaUri: Uri? = null

    private var contactoId: Int = -1
    private var esModoEdicion = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Recupera el ID del contacto desde los argumentos de navegación.
        arguments?.let {
            contactoId = it.getInt("contactId", -1)
            esModoEdicion = contactoId != -1
        }
        contactosImporter = ContactosImporter(
            context = requireContext(),
            repository = viewModel.repository, // Asegúrate de que tu ViewModel exponga el repository
            onEstadoChanged = { mensaje ->
                Snackbar.make(binding.root, mensaje, Snackbar.LENGTH_LONG).show()
            },
            onImportacionEnCurso = { enCurso ->
                binding.btnImportarContactos.isEnabled = !enCurso
            }
        )

        requestPermisoLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    viewModel.importarContactosDelDispositivo()
                } else {
                    Snackbar.make(
                        binding.root,
                        "El permiso para leer contactos es necesario para la importación.",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }

        selectorImagenLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                uri?.let {
                    fotoSeleccionadaUri = it
                    cargarImagenConGlide(it)
                }
            }
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
        setupMenu()


        if (esModoEdicion) {
            prepararUIModoEdicion()
            viewModel.cargarContacto(contactoId)
            observarContactoCargado()
        } else {
            cargarImagenPorDefecto()
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

    }

    private fun configurarListeners() {
        binding.btnGuardarContacto.setOnClickListener {
            guardarContacto()
        }

        binding.btnImportarContactos.setOnClickListener {
            if (contactosImporter.intentarImportar(requestPermisoLauncher)) {
                viewLifecycleOwner.lifecycleScope.launch {
                    contactosImporter.importarContactosDelDispositivo()
                }
            }
        }

        binding.ivFotoContactoDetalle.setOnClickListener {
            selectorImagenLauncher.launch("image/*")
        }

        binding.btnSeleccionarFoto.setOnClickListener {
            selectorImagenLauncher.launch("image/*")
        }
    }

    private fun prepararUIModoEdicion() {
        binding.btnGuardarContacto.text = "Actualizar Contacto"
        binding.btnImportarContactos.visibility = View.GONE
    }

    private fun observarContactoCargado() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.contactoCargado.collect { contacto ->
                    contacto?.let { poblarDatosEnUI(it) }
                }
            }
        }
    }

    private fun poblarDatosEnUI(contacto: Contacto) {
        binding.etNombreCompleto.setText(contacto.nombreCompleto)
        binding.etNumeroTelefono.setText(contacto.numeroTelefono)
        binding.etNotasContacto.setText(contacto.notas)
        binding.switchFavorito.isChecked = contacto.esFavorito

        if (contacto.fotoUri != null) {
            fotoSeleccionadaUri = Uri.parse(contacto.fotoUri)
            cargarImagenConGlide(fotoSeleccionadaUri)
        } else {
            cargarImagenPorDefecto()
        }
    }

//    private fun solicitarPermisoYImportarContactos() {
//        when {
//            ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.READ_CONTACTS
//            ) == PackageManager.PERMISSION_GRANTED -> {
//                viewModel.importarContactosDelDispositivo()
//            }
//
//            shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
//                Snackbar.make(
//                    binding.root,
//                    "Se necesita acceso a tus contactos para importarlos a la agenda.",
//                    Snackbar.LENGTH_INDEFINITE
//                )
//                    .setAction("ENTENDIDO") { requestPermisoLauncher.launch(Manifest.permission.READ_CONTACTS) }
//                    .show()
//            }
//
//            else -> {
//                requestPermisoLauncher.launch(Manifest.permission.READ_CONTACTS)
//            }
//        }
//    }

    /*    private fun configurarObservadoresDeImportacion() {
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        viewModel.importacionEnCurso.collect { enCurso ->
                            binding.btnImportarContactos.isEnabled = !enCurso
                        }
                    }
                    launch {
                        viewModel.estadoImportacion.collect { mensaje ->
                            mensaje?.let {
                                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                                viewModel.limpiarEstadoImportacion()
                            }
                        }
                    }
                }
            }
        }*/

    private fun guardarContacto() {
        val nombre = binding.etNombreCompleto.text.toString().trim()
        val telefono = binding.etNumeroTelefono.text.toString().trim()
        val notas = binding.etNotasContacto.text.toString().trim()
        val esFavorito = binding.switchFavorito.isChecked

        if (validarEntradas(nombre, telefono)) {
            val contactoParaGuardar = Contacto(
                id = if (esModoEdicion) contactoId else 0,
                nombreCompleto = nombre,
                numeroTelefono = telefono,
                fotoUri = fotoSeleccionadaUri?.toString(),
                esFavorito = esFavorito,
                notas = notas
            )

            viewModel.guardarContacto(contactoParaGuardar)

            val mensaje = if (esModoEdicion) "Contacto actualizado" else "Contacto guardado"
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()

        } else {
            Toast.makeText(
                requireContext(),
                "Por favor, complete el nombre y el teléfono",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun validarEntradas(nombre: String, telefono: String): Boolean {
        return nombre.isNotEmpty() && telefono.isNotEmpty()
    }

    private fun cargarImagenConGlide(uri: Uri?) {
        Glide.with(this)
            .load(uri)
            .placeholder(R.drawable.ic_perm_identity)
            .error(R.drawable.ic_perm_identity)
            .circleCrop()
            .into(binding.ivFotoContactoDetalle)
    }

    private fun cargarImagenPorDefecto() {
        cargarImagenConGlide(null)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}