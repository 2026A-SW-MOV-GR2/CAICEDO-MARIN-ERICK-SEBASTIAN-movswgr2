package com.example.redyseguridad.ui.rest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.redyseguridad.databinding.FragmentRestBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Fragment que muestra la pantalla de conectividad REST.
 * Permite obtener un post desde JSONPlaceholder y actualizarlo.
 * Usa ViewBinding y recolecta el StateFlow del ViewModel de forma reactiva.
 */
class RestFragment : Fragment() {

    // ViewBinding para acceder a los elementos de la UI
    private lateinit var binding: FragmentRestBinding

    // ViewModel específico de este fragment
    private val viewModel: RestViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inicializar binding
        binding = FragmentRestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupListeners()
        observeStateFlow()
    }

    /**
     * Configura los listeners de botones.
     */
    private fun setupListeners() {
        // Botón Consultar: obtiene el ID y llama a fetchPost
        binding.btnFetch.setOnClickListener {
            val idStr = binding.etPostId.text?.toString()?.trim()
            
            // Validar que el ID no esté vacío
            if (idStr.isNullOrEmpty()) {
                showErrorSnackbar("Por favor ingresa un ID válido")
                return@setOnClickListener
            }

            try {
                val id = idStr.toInt()
                // Llamar al ViewModel para obtener el post
                viewModel.fetchPost(id)
            } catch (e: NumberFormatException) {
                showErrorSnackbar("El ID debe ser un número")
            }
        }

        // Botón Actualizar: actualiza el post con los nuevos datos
        binding.btnUpdate.setOnClickListener {
            val idStr = binding.etPostId.text?.toString()?.trim()
            val title = binding.etTitle.text?.toString()?.trim()
            val body = binding.etBody.text?.toString()?.trim()

            // Validar que los campos no estén vacíos
            if (idStr.isNullOrEmpty() || title.isNullOrEmpty() || body.isNullOrEmpty()) {
                showErrorSnackbar("Todos los campos son requeridos")
                return@setOnClickListener
            }

            try {
                val id = idStr.toInt()
                // Llamar al ViewModel para actualizar el post
                viewModel.updatePost(id, title, body)
            } catch (e: NumberFormatException) {
                showErrorSnackbar("El ID debe ser un número")
            }
        }
    }

    /**
     * Recolecta el StateFlow del ViewModel y responde a los cambios de estado.
     * Usa lifecycleScope.launch con repeatOnLifecycle para evitar memory leaks.
     */
    private fun observeStateFlow() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Recopilar el StateFlow solo cuando el fragment está en estado STARTED
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        // Estado Idle: UI limpia, sin datos
                        is UiState.Idle -> {
                            clearUI()
                            enableAllUI()
                        }

                        // Estado Loading: mostrar indicador de progreso, deshabilitar UI
                        is UiState.Loading -> {
                            disableAllUI()
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        // Estado Success: llenar los campos con los datos obtenidos
                        is UiState.Success -> {
                            enableAllUI()
                            binding.progressBar.visibility = View.GONE
                            binding.etTitle.setText(state.post.title)
                            binding.etTitle.isEnabled = true
                            binding.etBody.setText(state.post.body)
                            binding.etBody.isEnabled = true
                            binding.btnUpdate.isEnabled = true
                        }

                        // Estado Updated: mostrar mensaje de éxito
                        is UiState.Updated -> {
                            enableAllUI()
                            binding.progressBar.visibility = View.GONE
                            showSuccessSnackbar("✓ Post actualizado correctamente")
                            // Opcionalmente, resetear el estado después de un tiempo
                            viewModel.resetState()
                        }

                        // Estado Error: mostrar mensaje de error
                        is UiState.Error -> {
                            enableAllUI()
                            binding.progressBar.visibility = View.GONE
                            showErrorSnackbar("Error: ${state.message}")
                        }
                    }
                }
            }
        }
    }

    /**
     * Limpia todos los campos de entrada.
     */
    private fun clearUI() {
        binding.etPostId.text?.clear()
        binding.etTitle.text?.clear()
        binding.etBody.text?.clear()
        binding.btnUpdate.isEnabled = false
        binding.etTitle.isEnabled = false
        binding.etBody.isEnabled = false
    }

    /**
     * Habilita todos los controles de la UI.
     */
    private fun enableAllUI() {
        binding.etPostId.isEnabled = true
        binding.btnFetch.isEnabled = true
    }

    /**
     * Deshabilita todos los controles de la UI (durante una carga).
     */
    private fun disableAllUI() {
        binding.etPostId.isEnabled = false
        binding.btnFetch.isEnabled = false
        binding.btnUpdate.isEnabled = false
    }

    /**
     * Muestra un Snackbar con un mensaje de éxito.
     */
    private fun showSuccessSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    /**
     * Muestra un Snackbar con un mensaje de error.
     */
    private fun showErrorSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}

