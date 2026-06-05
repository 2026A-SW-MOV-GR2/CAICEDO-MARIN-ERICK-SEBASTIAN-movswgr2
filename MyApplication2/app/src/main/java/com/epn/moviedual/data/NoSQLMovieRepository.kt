package com.epn.moviedual.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * NoSQL-based implementation of MovieRepository using SharedPreferences.
 * Stores all movies as a JSON array in a single SharedPreferences key.
 * Manual serialization/deserialization using org.json (no Gson, no Moshi).
 */
class NoSQLMovieRepository(context: Context) : MovieRepository {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getAll(): List<Movie> {
        return try {
            val jsonString = sharedPreferences.getString(MOVIES_KEY, "[]")
            val jsonArray = JSONArray(jsonString ?: "[]")

            val movies = mutableListOf<Movie>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                movies.add(movieFromJson(jsonObject))
            }

            Log.d("MovieRepo", "[NoSQL] Loaded ${movies.size} movies from SharedPreferences")
            movies
        } catch (e: Exception) {
            Log.e("MovieRepo", "[NoSQL] Error on getAll: ${e.message}", e)
            emptyList()
        }
    }

    override fun insert(movie: Movie): Long {
        return try {
            val movies = getAll().toMutableList()

            // Find the max ID and increment it
            val maxId = movies.maxOfOrNull { it.id } ?: 0
            val newId = maxId + 1

            val newMovie = movie.copy(id = newId)
            movies.add(newMovie)

            saveMovies(movies)
            Log.i("MovieRepo", "[NoSQL] Inserted movie id=$newId title=${movie.title}")
            newId.toLong()
        } catch (e: Exception) {
            Log.e("MovieRepo", "[NoSQL] Error on insert: ${e.message}", e)
            -1L
        }
    }

    override fun update(movie: Movie): Boolean {
        return try {
            val movies = getAll().toMutableList()
            val index = movies.indexOfFirst { it.id == movie.id }

            if (index >= 0) {
                movies[index] = movie
                saveMovies(movies)
                Log.i("MovieRepo", "[NoSQL] Updated movie id=${movie.id} title=${movie.title}")
                true
            } else {
                Log.d("MovieRepo", "[NoSQL] No movie found with id=${movie.id}")
                false
            }
        } catch (e: Exception) {
            Log.e("MovieRepo", "[NoSQL] Error on update: ${e.message}", e)
            false
        }
    }

    override fun delete(id: Int): Boolean {
        return try {
            val movies = getAll().toMutableList()
            val initialSize = movies.size
            movies.removeAll { it.id == id }

            if (movies.size < initialSize) {
                saveMovies(movies)
                Log.i("MovieRepo", "[NoSQL] Deleted movie id=$id")
                true
            } else {
                Log.d("MovieRepo", "[NoSQL] No movie found with id=$id")
                false
            }
        } catch (e: Exception) {
            Log.e("MovieRepo", "[NoSQL] Error on delete: ${e.message}", e)
            false
        }
    }

    /**
     * Convert a Movie object to JSONObject.
     */
    private fun movieToJson(movie: Movie): JSONObject {
        return JSONObject().apply {
            put("id", movie.id)
            put("title", movie.title)
            put("year", movie.year)
            put("genre", movie.genre)
            put("synopsis", movie.synopsis)
            put("imagePath", movie.imagePath ?: "")
        }
    }

    /**
     * Convert a JSONObject to Movie object.
     */
    private fun movieFromJson(jsonObject: JSONObject): Movie {
        return Movie(
            id = jsonObject.getInt("id"),
            title = jsonObject.getString("title"),
            year = jsonObject.getInt("year"),
            genre = jsonObject.getString("genre"),
            synopsis = jsonObject.getString("synopsis"),
            imagePath = jsonObject.optString("imagePath", "").takeIf { it.isNotEmpty() }
        )
    }

    /**
     * Save all movies to SharedPreferences as a JSON array.
     */
    private fun saveMovies(movies: List<Movie>) {
        try {
            val jsonArray = JSONArray()
            for (movie in movies) {
                jsonArray.put(movieToJson(movie))
            }

            sharedPreferences.edit().apply {
                putString(MOVIES_KEY, jsonArray.toString())
                apply()
            }
            Log.d("MovieRepo", "[NoSQL] Saved ${movies.size} movies to SharedPreferences")
        } catch (e: Exception) {
            Log.e("MovieRepo", "[NoSQL] Error saving movies: ${e.message}", e)
        }
    }

    companion object {
        private const val PREFS_NAME = "movie_nosql_prefs"
        private const val MOVIES_KEY = "movies_nosql"
    }
}


