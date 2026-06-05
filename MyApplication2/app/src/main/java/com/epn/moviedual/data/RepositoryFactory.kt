package com.epn.moviedual.data

import android.content.Context

/**
 * Factory object for creating MovieRepository instances.
 * Provides a single point to switch between SQLite and NoSQL implementations.
 */
object RepositoryFactory {
    /**
     * Get a MovieRepository instance based on the specified engine.
     *
     * @param context Android context
     * @param useSQL true for SQLiteMovieRepository, false for NoSQLMovieRepository
     * @return MovieRepository instance
     */
    fun getRepository(context: Context, useSQL: Boolean): MovieRepository {
        return if (useSQL) {
            SQLiteMovieRepository(context)
        } else {
            NoSQLMovieRepository(context)
        }
    }
}

