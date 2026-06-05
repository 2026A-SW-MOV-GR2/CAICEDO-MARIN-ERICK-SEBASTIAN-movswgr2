package com.example.redyseguridad.model

/**
 * Data class que representa un Post de JSONPlaceholder.
 * Se parsea manualmente desde JSONObject sin usar librerías como Gson o Moshi.
 *
 * @param id Identificador único del post
 * @param userId ID del usuario que creó el post
 * @param title Título del post
 * @param body Contenido/cuerpo del post
 */
data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String
)

