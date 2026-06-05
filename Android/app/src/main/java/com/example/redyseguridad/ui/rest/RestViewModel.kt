package com.example.redyseguridad.ui.rest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.redyseguridad.model.Post
import com.example.redyseguridad.network.HttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Sealed class que representa los diferentes estados de la UI para la pantalla REST.
 * Esto es parte del patrón MVVM con StateFlow para reactividad.
 */
sealed class UiState {
    /** Estado inicial: sin datos ni operación en curso */
    data object Idle : UiState()

    /** Estado mientras se realiza una operación HTTP */
    data object Loading : UiState()

    /** Estado tras obtener un post exitosamente */
    data class Success(val post: Post) : UiState()

    /** Estado tras actualizar un post exitosamente */
    data object Updated : UiState()

    /** Estado cuando ocurre un error */
    data class Error(val message: String) : UiState()
}

/**
 * ViewModel para la pantalla REST.
 * Maneja la lógica de obtener y actualizar posts desde JSONPlaceholder.
 * Usa Coroutines con viewModelScope y Dispatchers.IO para operaciones de red.
 */
class RestViewModel : ViewModel() {

    // StateFlow expuesto a la UI para que se actualice reactivamente
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    /**
     * Obtiene un post por su ID desde JSONPlaceholder.
     * Emite Loading → Success/Error dependiendo del resultado.
     *
     * @param id El ID del post a obtener
     */
    fun fetchPost(id: Int) {
        // Validar que el ID sea válido
        if (id <= 0) {
            _uiState.value = UiState.Error("El ID debe ser mayor a 0")
            return
        }

        // Lanzar coroutine en viewModelScope para que se cancele si el ViewModel es destruido
        viewModelScope.launch {
            // Emitir estado Loading
            _uiState.value = UiState.Loading

            try {
                // Llamar a HttpClient.getPost que corre en Dispatchers.IO
                val post = HttpClient.getPost(id)
                
                // Emitir estado Success con el post obtenido
                _uiState.value = UiState.Success(post)
            } catch (e: Exception) {
                // En caso de error, emitir el mensaje de error
                val errorMessage = e.message ?: "Error desconocido al obtener el post"
                _uiState.value = UiState.Error(errorMessage)
            }
        }
    }

    /**
     * Actualiza un post existente mediante una solicitud PUT.
     * Emite Loading → Updated/Error dependiendo del resultado.
     *
     * @param id El ID del post a actualizar
     * @param title El nuevo título
     * @param body El nuevo cuerpo del post
     */
    fun updatePost(id: Int, title: String, body: String) {
        // Validar que los campos no estén vacíos
        if (title.isBlank() || body.isBlank()) {
            _uiState.value = UiState.Error("Título y cuerpo no pueden estar vacíos")
            return
        }

        // Lanzar coroutine en viewModelScope
        viewModelScope.launch {
            // Emitir estado Loading
            _uiState.value = UiState.Loading

            try {
                // Llamar a HttpClient.putPost que corre en Dispatchers.IO
                val responseCode = HttpClient.putPost(id, title, body)

                // Verificar que la solicitud fue exitosa (HTTP 200)
                if (responseCode == 200) {
                    _uiState.value = UiState.Updated
                } else {
                    _uiState.value = UiState.Error("Error: código HTTP $responseCode")
                }
            } catch (e: Exception) {
                // En caso de error, emitir el mensaje de error
                val errorMessage = e.message ?: "Error desconocido al actualizar el post"
                _uiState.value = UiState.Error(errorMessage)
            }
        }
    }

    /**
     * Reinicia el estado a Idle. Útil cuando se descarta o resetea la pantalla.
     */
    fun resetState() {
        _uiState.value = UiState.Idle
    }
}

