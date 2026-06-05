package com.epn.moviedual.data

/**
 * Repository interface for Movie CRUD operations.
 * Two implementations: SQLiteMovieRepository and NoSQLMovieRepository
 */
interface MovieRepository {
    /**
     * Get all movies from the current storage engine.
     * @return List of all movies
     */
    fun getAll(): List<Movie>

    /**
     * Insert a new movie into the current storage engine.
     * @param movie Movie to insert
     * @return ID of the inserted movie
     */
    fun insert(movie: Movie): Long

    /**
     * Update an existing movie in the current storage engine.
     * @param movie Movie to update
     * @return true if successful, false otherwise
     */
    fun update(movie: Movie): Boolean

    /**
     * Delete a movie by ID from the current storage engine.
     * @param id Movie ID to delete
     * @return true if successful, false otherwise
     */
    fun delete(id: Int): Boolean
}

