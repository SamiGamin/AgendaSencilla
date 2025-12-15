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
import com.SamiDev.agendasencilla.data.ContactoTelefono
import com.SamiDev.agendasencilla.databinding.FragmentGestionContactoBinding
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GestionContactoFragment : Fragment() {

    private var _binding: FragmentGestionContactoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GestionContactoViewModel by viewModels {
        GestionContactoViewModelFactory(requireActivity().application)
    }

    private var contactoId: String? = null // El ID ahora es String
    private var contactoActual: ContactoTelefono? = null

    // Launcher para permiso de escritura (necesario para guardar cambios)
    private val permisoEscrituraLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { esConcedido ->
        if (esConcedido) {
            intentarGuardar()
        } else {
            Toast.makeText(requireContext(), "Se necesita permiso para editar contactos", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Recuperamos el ID. Ahora los IDs de Android son Strings
        arguments?.let {
            // Intenta obtener como String, si falla intenta como Int y convierte
            contactoId = it.getString("contactId") ?: it.getInt("contactId", -1).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGestionContactoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()
        configurarUI()
        cargarDatos()
        observarViewModel()
    }

    private fun configurarUI() {
        // Ocultamos el botón importar porque ya no existe esa función
        binding.btnImportarContactos.visibility = View.GONE

        // Ajustamos textos
        if (contactoId != null && contactoId != "-1") {
            binding.btnGuardarContacto.text = "Guardar Cambios"
        } else {
            // Si es nuevo, sugerimos usar la app nativa (opcional)
            binding.btnGuardarContacto.text = "Crear Contacto"
        }

        binding.btnGuardarContacto.setOnClickListener {
            verificarPermisoYGuardar()
        }

        // La edición de foto localmente es muy compleja (gestión de archivos raw).
        // Por ahora deshabilitamos el clic en la foto o lanzamos Intent nativo.
        binding.ivFotoContactoDetalle.setOnClickListener {
            Toast.makeText(requireContext(), "La foto se gestiona desde la app Contactos", Toast.LENGTH_SHORT).show()
        }
        binding.btnSeleccionarFoto.visibility = View.GONE // Ocultamos botón de cambiar foto
    }

    private fun cargarDatos() {
        contactoId?.let { id ->
            if (id != "-1") {
                viewModel.cargarContacto(id)
            }
        }
    }

    private fun observarViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Observar carga del contacto
                launch {
                    viewModel.contactoCargado.collectLatest { contacto ->
                        contacto?.let {
                            contactoActual = it
                            poblarDatos(it)
                        }
                    }
                }

                // Observar resultado de guardado
                launch {
                    viewModel.estadoGuardado.collectLatest { guardado ->
                        if (guardado == true) {
                            Toast.makeText(requireContext(), "Contacto actualizado correctamente", Toast.LENGTH_SHORT).show()
                            viewModel.reiniciarEstadoGuardado()
                            findNavController().popBackStack()
                        } else if (guardado == false) {
                            Toast.makeText(requireContext(), "Error al guardar cambios", Toast.LENGTH_SHORT).show()
                            viewModel.reiniciarEstadoGuardado()
                        }
                    }
                }
            }
        }
    }

    private fun poblarDatos(contacto: ContactoTelefono) {
        binding.etNombreCompleto.setText(contacto.nombreCompleto)
        binding.etNumeroTelefono.setText(contacto.numeroTelefono)
        binding.switchFavorito.isChecked = contacto.esFavorito

        // Notas: Android ContactsContract tiene notas, pero requiere permisos complejos.
        // Por simplicidad, ocultamos o dejamos el campo notas inactivo si no queremos lidiar con ello.
        binding.etNotasContacto.isEnabled = false
        binding.etNotasContacto.hint = "Notas (Solo lectura)"

        if (contacto.fotoUri != null) {
            Glide.with(this)
                .load(contacto.fotoUri)
                .circleCrop()
                .into(binding.ivFotoContactoDetalle)
        } else {
            binding.ivFotoContactoDetalle.setImageResource(R.drawable.ic_perm_identity)
        }
    }

    private fun verificarPermisoYGuardar() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_CONTACTS)
            == PackageManager.PERMISSION_GRANTED) {
            intentarGuardar()
        } else {
            permisoEscrituraLauncher.launch(Manifest.permission.WRITE_CONTACTS)
        }
    }

    private fun intentarGuardar() {
        val nuevoNombre = binding.etNombreCompleto.text.toString().trim()
        val nuevoTelefono = binding.etNumeroTelefono.text.toString().trim()
        val esFavorito = binding.switchFavorito.isChecked

        if (nuevoNombre.isEmpty() || nuevoTelefono.isEmpty()) {
            Toast.makeText(requireContext(), "Nombre y teléfono requeridos", Toast.LENGTH_SHORT).show()
            return
        }

        if (contactoActual != null) {
            // Edición
            viewModel.guardarCambios(contactoActual!!, nuevoNombre, nuevoTelefono, esFavorito)
        } else {
            // Creación (Si llegaste aquí sin ID)
            // Aquí podrías implementar la lógica de insertar un nuevo RawContact
            Toast.makeText(requireContext(), "Usa el botón + en la lista principal para crear", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear() // Limpiamos menú si no queremos opciones extra aquí
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}