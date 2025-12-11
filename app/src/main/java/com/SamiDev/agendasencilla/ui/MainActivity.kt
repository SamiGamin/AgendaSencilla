package com.SamiDev.agendasencilla.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.SamiDev.agendasencilla.R
import com.SamiDev.agendasencilla.data.preferencias.OpcionTamanoFuente // Importar enum
import com.SamiDev.agendasencilla.data.preferencias.PreferenciasManager
import com.SamiDev.agendasencilla.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var preferenciasManager: PreferenciasManager

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

        // Inicializar PreferenciasManager para el resto de la actividad (ej. tema)
        // Esto ocurre DESPUÉS de attachBaseContext.
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
                R.id.listadocontactosFragment
            )
        )

        // Conectar NavController con la Toolbar (ActionBar)
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration)

        // Conectar NavController con el BottomNavigationView
        NavigationUI.setupWithNavController(binding.navegacionInferiorView, navController)

        // Ajustar el padding para el edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun solicitarPermisoDeLlamada() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // El permiso no ha sido concedido, así que lo solicitamos.
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CODE_CALL_PHONE)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_contactos, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_search -> {
                Toast.makeText(this, "Acción de búsqueda seleccionada", Toast.LENGTH_SHORT).show()
                true
            }
            else -> NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }
}