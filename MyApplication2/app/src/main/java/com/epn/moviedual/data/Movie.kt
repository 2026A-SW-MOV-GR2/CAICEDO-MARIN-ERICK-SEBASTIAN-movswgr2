package com.epn.moviedual.data

/**
 * Movie entity for the dual persistence system.
 *
 * @property id Unique identifier (auto-increment in SQLite, index in NoSQL)
 * @property title Movie title
 * @property year Release year
 * @property genre Movie genre
 * @property synopsis Movie description
 * @property imagePath Local URI or file path from gallery/camera
 */
data class Movie(
    val id: Int = 0,
    val title: String,
    val year: Int,
    val genre: String,
    val synopsis: String,
    val imagePath: String? = null
)

