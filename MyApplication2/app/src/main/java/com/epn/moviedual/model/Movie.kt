package com.epn.moviedual.model

data class Movie(
    val id: Int = 0,
    val title: String,
    val year: Int,
    val genre: String,
    val synopsis: String,
    val imagePath: String? = null
)

