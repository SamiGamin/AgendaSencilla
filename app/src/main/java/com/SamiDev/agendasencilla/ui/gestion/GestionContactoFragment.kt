package com.SamiDev.agendasencilla.ui.gestion

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.SamiDev.agendasencilla.R // Necesario para placeholder/error de Glide si se usan.
import com.SamiDev.agendasencilla.data.database.Contacto
import com.SamiDev.agendasencilla.databinding.FragmentGestionContactoBinding
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class GestionContactoFragment : Fragment() {

    private var _binding: FragmentGestionContactoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GestionContactoViewModel by viewModels {
        GestionContactoViewModelFactory(requireActivity().application)
    }

    private lateinit var requestPermisoLauncher: ActivityResultLauncher<String>
    private lateinit var selectorImagenLauncher: ActivityResultLauncher<String>
    private var fotoSeleccionadaUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launcher para solicitar permiso de lectura de contactos
        requestPermisoLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                viewModel.importarContactosDelDispositivo()
            } else {
                Snackbar.make(binding.root, "El permiso para leer contactos es necesario para la importación.", Snackbar.LENGTH_LONG).show()
            }
        }

        // Launcher para seleccionar imagen de la galería
        selectorImagenLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                fotoSeleccionadaUri = it
                Glide.with(this)
                    .load(it)
                    .placeholder(R.drawable.ic_perm_identity) // Asume que tienes este drawable
                    .error(R.drawable.ic_perm_identity) // Asume que tienes este drawable
                    .circleCrop() // Para hacerlo circular si la imagen original no lo es
                    .into(binding.ivFotoContactoDetalle)
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
        configurarObservadoresDeImportacion()
        // Cargar imagen por defecto si no hay ninguna seleccionada aún
        Glide.with(this)
            .load(fotoSeleccionadaUri) // Si es null, cargará el error/placeholder
            .placeholder(R.drawable.ic_perm_identity)
            .error(R.drawable.ic_perm_identity)
            .circleCrop()
            .into(binding.ivFotoContactoDetalle)
    }

    private fun configurarListeners() {
        binding.btnGuardarContacto.setOnClickListener {
            guardarNuevoContacto()
        }

        binding.btnImportarContactos.setOnClickListener {
            solicitarPermisoYImportarContactos()
        }

        binding.btnSeleccionarFoto.setOnClickListener {
            selectorImagenLauncher.launch("image/*")
        }
    }

    private fun solicitarPermisoYImportarContactos() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.importarContactosDelDispositivo()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
                Snackbar.make(binding.root, "Se necesita acceso a tus contactos para importarlos a la agenda.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("ENTENDIDO") { requestPermisoLauncher.launch(Manifest.permission.READ_CONTACTS) }
                    .show()
            }
            else -> {
                requestPermisoLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun configurarObservadoresDeImportacion() {
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
    }

    private fun guardarNuevoContacto() {
        val nombre = binding.etNombreCompleto.text.toString().trim()
        val telefono = binding.etNumeroTelefono.text.toString().trim()
        val notas = binding.etNotasContacto.text.toString().trim()
        val esFavorito = binding.switchFavorito.isChecked

        if (validarEntradas(nombre, telefono)) {
            val nuevoContacto = Contacto(
                nombreCompleto = nombre,
                numeroTelefono = telefono,
                fotoUri = fotoSeleccionadaUri?.toString(), // Guardar la URI de la imagen como String
                esFavorito = esFavorito,
                notas = notas
            )
            viewModel.guardarContacto(nuevoContacto)
            Toast.makeText(requireContext(), "Contacto guardado exitosamente", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        } else {
            Toast.makeText(requireContext(), "Por favor, complete el nombre y el teléfono", Toast.LENGTH_LONG).show()
        }
    }

    private fun validarEntradas(nombre: String, telefono: String): Boolean {
        return nombre.isNotEmpty() && telefono.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}