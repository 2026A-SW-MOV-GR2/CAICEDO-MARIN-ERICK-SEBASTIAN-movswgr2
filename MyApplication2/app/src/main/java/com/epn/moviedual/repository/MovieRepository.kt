package com.epn.moviedual.repository

import com.epn.moviedual.model.Movie

interface MovieRepository {
    fun getAll(): List<Movie>
    fun insert(movie: Movie): Long
    fun update(movie: Movie): Boolean
    fun delete(id: Int): Boolean
}

