package com.SamiDev.agendasencilla.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
import com.SamiDev.agendasencilla.data.database.AppDatabase
import com.SamiDev.agendasencilla.data.preferencias.OpcionTamanoFuente
import com.SamiDev.agendasencilla.data.preferencias.PreferenciasManager
import com.SamiDev.agendasencilla.data.repository.ContactoTelefonoRepositorio
import com.SamiDev.agendasencilla.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

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
            OpcionTamanoFuente.GRANDE -> 1.7f // Valor de ejemplo, ajustar si es necesario
            OpcionTamanoFuente.MAS_GRANDE -> 2.2f // Valor de ejemplo, ajustar si es necesario
        }
        // Aplicar la configuración al contexto base con el que la actividad será creada.
        super.attachBaseContext(newBase.createConfigurationContext(nuevaConfiguracion))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permisoLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                lifecycleScope.launch {

                }
            }
        }
        preferenciasManager = PreferenciasManager.getInstance(applicationContext)

        // Aplicar el tema antes de inflar la vista y setContentView
        aplicarTemaGuardado()

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Solicitar permiso de llamada al iniciar
        solicitarPermisoDeLlamada()

        // Configurar la Toolbar como ActionBar
        setSupportActionBar(binding.toolbar)

        // Configurar NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Configurar AppBarConfiguration para los destinos de nivel superior
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.contactosFavoritosFragment,
                R.id.listadocontactosFragment,
                R.id.marcarFragment,

            )
        )

        // Conectar NavController con la Toolbar (ActionBar)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        // Conectar NavController con el BottomNavigationView
        NavigationUI.setupWithNavController(binding.navegacionInferiorView, navController)

        // Ajustar el padding para el edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.main.setPadding(0, 0, 0, 0)
            binding.main.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                0  // Esto empuja el contenido hacia arriba para no tapar con la barra de gestos
            )
            binding.navHostFragment.setPadding(
                systemBars.left,
                0,  // o systemBars.top si quieres que respete la status bar (raro en bottom nav)
                systemBars.right,
                0   // ← Aquí está la clave: padding bottom = 0
            )

            insets
        }
        setupFab()
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.contactosFavoritosFragment->{
                    binding.fabAnadirContacto.setImageResource(R.drawable.person_add)
                    binding.fabAnadirContacto.contentDescription = "Añadir nuevo contacto"
                    binding.fabAnadirContacto.show()
                }
                R.id.listadocontactosFragment -> {
                    binding.fabAnadirContacto.setImageResource(R.drawable.person_add)
                    binding.fabAnadirContacto.contentDescription = "Añadir nuevo contacto"
                    binding.fabAnadirContacto.show()
                }
                R.id.marcarFragment -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.fabAnadirContacto.hide()
                }
                R.id.configuracionFragment -> {
                    binding.fabAnadirContacto.hide()
                }
                R.id.gestionContactoFragment -> {
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
            when (navController.currentDestination?.id) {
                R.id.listadocontactosFragment -> {
                    navController.navigate(R.id.action_listadocontactosFragment_to_gestionContactoFragment)
                }
                R.id.contactosFavoritosFragment -> {
                    navController.navigate(R.id.action_contactosFavoritosFragment_to_gestionContactoFragment)
                }
                // Puedes agregar más si es necesario
            }
        }
    }


    private fun solicitarPermisoDeLlamada() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // El permiso no ha sido concedido, así que lo solicitamos.
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                REQUEST_CODE_CALL_PHONE
            )
        }
        // Si el permiso ya está concedido, no hacemos nada.
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