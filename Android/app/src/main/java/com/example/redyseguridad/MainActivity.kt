package com.example.redyseguridad

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.redyseguridad.databinding.ActivityMainBinding

/**
 * Activity principal que actúa como contenedor de los fragments.
 * Integra Navigation Component con BottomNavigationView para navegar entre pantallas.
 * Utiliza ViewBinding para acceso seguro a los elementos de la UI.
 */
class MainActivity : AppCompatActivity() {

    // ViewBinding para acceso seguro a elementos de activity_main.xml
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding en lugar de setContentView()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar Navigation Component con BottomNavigationView
        setupNavigation()
    }

    /**
     * Configura la integración entre Navigation Component y BottomNavigationView.
     * Esto permite que los items del menú navegen automáticamente a los fragments correspondientes.
     */
    private fun setupNavigation() {
        // Obtener el NavHostFragment que contiene los fragments
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                as NavHostFragment

        // Obtener el NavController del NavHostFragment
        val navController = navHostFragment.navController

        // Conectar el BottomNavigationView con el NavController
        // Esto vincula los items del menú con los IDs de los fragmentos en el nav_graph
        binding.bottomNavigation.setupWithNavController(navController)
    }
}

