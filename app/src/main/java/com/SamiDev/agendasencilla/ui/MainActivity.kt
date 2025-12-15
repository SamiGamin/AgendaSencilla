package com.SamiDev.agendasencilla.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.SamiDev.agendasencilla.R
import com.SamiDev.agendasencilla.data.preferencias.OpcionTamanoFuente
import com.SamiDev.agendasencilla.data.preferencias.PreferenciasManager
import com.SamiDev.agendasencilla.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

/**
 * Actividad principal de la aplicación.
 * Gestiona la navegación entre fragmentos, la configuración global (tema, fuente) y los permisos iniciales.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var preferenciasManager: PreferenciasManager

    private lateinit var permisoLauncher: ActivityResultLauncher<String>

    companion object {
        private const val REQUEST_CODE_CALL_PHONE = 101
    }

    override fun attachBaseContext(newBase: Context) {
        val localPreferenciasManager = PreferenciasManager.getInstance(newBase.applicationContext)
        val opcionTamanoFuente = localPreferenciasManager.obtenerOpcionTamanoFuente()
        val nuevaConfiguracion = Configuration(newBase.resources.configuration)

        nuevaConfiguracion.fontScale = when (opcionTamanoFuente) {
            OpcionTamanoFuente.NORMAL -> 1.0f
            OpcionTamanoFuente.GRANDE -> 1.15f
            OpcionTamanoFuente.MAS_GRANDE -> 1.30f
        }
        super.attachBaseContext(newBase.createConfigurationContext(nuevaConfiguracion))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permisoLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                lifecycleScope.launch {
                    // Acciones post-concesión si fueran necesarias
                }
            }
        }
        preferenciasManager = PreferenciasManager.getInstance(applicationContext)

        aplicarTemaGuardado()

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        solicitarPermisoDeLlamada()

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.contactosFavoritosFragment,
                R.id.listadocontactosFragment,
                R.id.marcarFragment,
            )
        )

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)
        NavigationUI.setupWithNavController(binding.navegacionInferiorView, navController)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.main.setPadding(0, 0, 0, 0) // Reset padding
            binding.main.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                0
            )
            binding.navHostFragment.setPadding(
                systemBars.left,
                0,
                systemBars.right,
                0
            )
            insets
        }

        setupFab()
        configurarNavegacionFab()
    }

    /**
     * Configura el comportamiento y visibilidad del FAB (Floating Action Button) según el destino.
     */
    private fun configurarNavegacionFab() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.contactosFavoritosFragment, R.id.listadocontactosFragment -> {
                    binding.fabAnadirContacto.setImageResource(R.drawable.person_add)
                    binding.fabAnadirContacto.contentDescription = getString(R.string.anadir_nuevo_contacto)
                    binding.fabAnadirContacto.show()
                }
                R.id.marcarFragment -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.fabAnadirContacto.hide()
                }
                else -> {
                    binding.fabAnadirContacto.hide()
                }
            }
        }
    }

    private fun setupFab() {
        binding.fabAnadirContacto.setOnClickListener {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                type = ContactsContract.Contacts.CONTENT_TYPE
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "No se encontró app de contactos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun solicitarPermisoDeLlamada() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                REQUEST_CODE_CALL_PHONE
            )
        }
    }

    private fun aplicarTemaGuardado() {
        if (preferenciasManager.tienePreferenciaTemaGuardada()) {
            if (preferenciasManager.obtenerTemaOscuro()) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(
            navController,
            appBarConfiguration
        ) || super.onSupportNavigateUp()
    }
}