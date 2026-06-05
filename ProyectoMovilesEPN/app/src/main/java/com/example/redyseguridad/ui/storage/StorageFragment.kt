package com.example.redyseguridad.ui.storage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.redyseguridad.databinding.FragmentStorageBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * Fragment que muestra la pantalla de Almacenamiento Seguro.
 * Permite guardar y recuperar valores usando tres tipos de almacenamiento:
 * - SharedPreferences
 * - Jetpack DataStore
 * - EncryptedSharedPreferences
 *
 * Usa ViewBinding y recolecta el StateFlow del ViewModel de forma reactiva.
 */
class StorageFragment : Fragment() {

    // ViewBinding para acceder a los elementos de la UI
    private lateinit var binding: FragmentStorageBinding

    // ViewModel específico de este fragment
    private val viewModel: StorageViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inicializar binding
        binding = FragmentStorageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupListeners()
        observeRetrievedValue()
    }

    /**
     * Configura los listeners de botones del fragment.
     */
    private fun setupListeners() {
        // Botón Guardar: guarda la clave-valor en el almacenamiento seleccionado
        binding.btnSave.setOnClickListener {
            val key = binding.etKey.text?.toString()?.trim()
            val value = binding.etValue.text?.toString()?.trim()

            // Validar que la clave y el valor no estén vacíos
            if (key.isNullOrEmpty() || value.isNullOrEmpty()) {
                showErrorSnackbar("Por favor completa clave y valor")
                return@setOnClickListener
            }

            // Obtener el tipo de almacenamiento seleccionado
            val selectedStorageId = binding.rgStorageType.checkedRadioButtonId
            
            when (selectedStorageId) {
                binding.rbSharedPrefs.id -> {
                    // Guardar en SharedPreferences
                    viewModel.saveToSharedPrefs(requireContext(), key, value)
                    showSuccessSnackbar("✓ Valor guardado en SharedPreferences")
                }
                binding.rbDataStore.id -> {
                    // Guardar en DataStore
                    viewModel.saveToDataStore(requireContext(), key, value)
                    showSuccessSnackbar("✓ Valor guardado en DataStore")
                }
                binding.rbEncrypted.id -> {
                    // Guardar en EncryptedSharedPreferences
                    viewModel.saveToEncrypted(requireContext(), key, value)
                    showSuccessSnackbar("✓ Valor guardado cifrado")
                }
            }
            
            // Limpiar campo de valor después de guardar
            binding.etValue.text?.clear()
        }

        // Botón Recuperar: recupera el valor del almacenamiento seleccionado
        binding.btnRetrieve.setOnClickListener {
            val key = binding.etKey.text?.toString()?.trim()

            // Validar que la clave no esté vacía
            if (key.isNullOrEmpty()) {
                showErrorSnackbar("Por favor ingresa una clave")
                return@setOnClickListener
            }

            // Obtener el tipo de almacenamiento seleccionado
            val selectedStorageId = binding.rgStorageType.checkedRadioButtonId
            
            when (selectedStorageId) {
                binding.rbSharedPrefs.id -> {
                    // Recuperar de SharedPreferences
                    val retrievedValue = viewModel.getFromSharedPrefs(requireContext(), key)
                    handleRetrievedValue(retrievedValue)
                }
                binding.rbDataStore.id -> {
                    // Recuperar de DataStore (asincrónico)
                    viewModel.getFromDataStore(requireContext(), key)
                }
                binding.rbEncrypted.id -> {
                    // Recuperar de EncryptedSharedPreferences
                    val retrievedValue = viewModel.getFromEncrypted(requireContext(), key)
                    handleRetrievedValue(retrievedValue)
                }
            }
        }

        // Cambio en RadioGroup: limpiar resultado y habilitar campo de valor
        binding.rgStorageType.setOnCheckedChangeListener { _, _ ->
            binding.tvResult.text = ""
            binding.etValue.text?.clear()
            binding.etValue.isEnabled = true
        }
    }

    /**
     * Recolecta el StateFlow del ViewModel para valores recuperados.
     * Se usa especialmente para DataStore que retorna un Flow.
     */
    private fun observeRetrievedValue() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.retrievedValue.collect { value ->
                    handleRetrievedValue(value)
                }
            }
        }
    }

    /**
     * Maneja el valor recuperado. Si es null, muestra un mensaje de error.
     * Si existe, lo muestra en el TextView de resultados.
     *
     * @param value El valor recuperado o null
     */
    private fun handleRetrievedValue(value: String?) {
        if (value == null) {
            showErrorSnackbar("Secreto no encontrado")
            binding.tvResult.text = ""
        } else {
            binding.tvResult.text = value
            showSuccessSnackbar("✓ Valor recuperado correctamente")
        }
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

