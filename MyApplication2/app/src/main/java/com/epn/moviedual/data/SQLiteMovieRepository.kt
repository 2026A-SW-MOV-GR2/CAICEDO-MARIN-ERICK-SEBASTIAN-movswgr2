package com.epn.moviedual.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * SQLite-based implementation of MovieRepository.
 * Uses native Android SQLiteOpenHelper (no Room, no ORM).
 */
class SQLiteMovieRepository(context: Context) : MovieRepository {
    private val dbHelper = MovieDatabaseHelper(context)

    override fun getAll(): List<Movie> {
        return try {
            val db = dbHelper.readableDatabase
            val cursor = db.query(
                MovieDatabaseHelper.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
            )

            val movies = mutableListOf<Movie>()
            with(cursor) {
                while (moveToNext()) {
                    val idIndex = getColumnIndexOrThrow(MovieDatabaseHelper.COL_ID)
                    val titleIndex = getColumnIndexOrThrow(MovieDatabaseHelper.COL_TITLE)
                    val yearIndex = getColumnIndexOrThrow(MovieDatabaseHelper.COL_YEAR)
                    val genreIndex = getColumnIndexOrThrow(MovieDatabaseHelper.COL_GENRE)
                    val synopsisIndex = getColumnIndexOrThrow(MovieDatabaseHelper.COL_SYNOPSIS)
                    val imagePathIndex = getColumnIndexOrThrow(MovieDatabaseHelper.COL_IMAGE_PATH)

                    movies.add(
                        Movie(
                            id = getInt(idIndex),
                            title = getString(titleIndex),
                            year = getInt(yearIndex),
                            genre = getString(genreIndex),
                            synopsis = getString(synopsisIndex),
                            imagePath = getString(imagePathIndex)
                        )
                    )
                }
            }
            cursor.close()

            Log.d("MovieRepo", "[SQLite] Loaded ${movies.size} movies from database")
            movies
        } catch (e: Exception) {
            Log.e("MovieRepo", "[SQLite] Error on getAll: ${e.message}", e)
            emptyList()
        }
    }

    override fun insert(movie: Movie): Long {
        return try {
            val db = dbHelper.writableDatabase
            val contentValues = android.content.ContentValues().apply {
                put(MovieDatabaseHelper.COL_TITLE, movie.title)
                put(MovieDatabaseHelper.COL_YEAR, movie.year)
                put(MovieDatabaseHelper.COL_GENRE, movie.genre)
                put(MovieDatabaseHelper.COL_SYNOPSIS, movie.synopsis)
                put(MovieDatabaseHelper.COL_IMAGE_PATH, movie.imagePath)
            }

            val id = db.insert(MovieDatabaseHelper.TABLE_NAME, null, contentValues)
            Log.i("MovieRepo", "[SQLite] Inserted movie id=$id title=${movie.title}")
            id
        } catch (e: Exception) {
            Log.e("MovieRepo", "[SQLite] Error on insert: ${e.message}", e)
            -1L
        }
    }

    override fun update(movie: Movie): Boolean {
        return try {
            val db = dbHelper.writableDatabase
            val contentValues = android.content.ContentValues().apply {
                put(MovieDatabaseHelper.COL_TITLE, movie.title)
                put(MovieDatabaseHelper.COL_YEAR, movie.year)
                put(MovieDatabaseHelper.COL_GENRE, movie.genre)
                put(MovieDatabaseHelper.COL_SYNOPSIS, movie.synopsis)
                put(MovieDatabaseHelper.COL_IMAGE_PATH, movie.imagePath)
            }

            val rowsUpdated = db.update(
                MovieDatabaseHelper.TABLE_NAME,
                contentValues,
                "${MovieDatabaseHelper.COL_ID} = ?",
                arrayOf(movie.id.toString())
            )

            if (rowsUpdated > 0) {
                Log.i("MovieRepo", "[SQLite] Updated movie id=${movie.id} title=${movie.title}")
                true
            } else {
                Log.d("MovieRepo", "[SQLite] No rows updated for id=${movie.id}")
                false
            }
        } catch (e: Exception) {
            Log.e("MovieRepo", "[SQLite] Error on update: ${e.message}", e)
            false
        }
    }

    override fun delete(id: Int): Boolean {
        return try {
            val db = dbHelper.writableDatabase
            val rowsDeleted = db.delete(
                MovieDatabaseHelper.TABLE_NAME,
                "${MovieDatabaseHelper.COL_ID} = ?",
                arrayOf(id.toString())
            )

            if (rowsDeleted > 0) {
                Log.i("MovieRepo", "[SQLite] Deleted movie id=$id")
                true
            } else {
                Log.d("MovieRepo", "[SQLite] No rows deleted for id=$id")
                false
            }
        } catch (e: Exception) {
            Log.e("MovieRepo", "[SQLite] Error on delete: ${e.message}", e)
            false
        }
    }
}

/**
 * Database helper for SQLite-based Movie storage.
 */
class MovieDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSQL = """
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_YEAR INTEGER NOT NULL,
                $COL_GENRE TEXT NOT NULL,
                $COL_SYNOPSIS TEXT NOT NULL,
                $COL_IMAGE_PATH TEXT
            )
        """.trimIndent()

        db.execSQL(createTableSQL)
        Log.d("MovieRepo", "[SQLite] Database created")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
        Log.d("MovieRepo", "[SQLite] Database upgraded from $oldVersion to $newVersion")
    }

    companion object {
        private const val DATABASE_NAME = "movie_database.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "movies"
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_YEAR = "year"
        const val COL_GENRE = "genre"
        const val COL_SYNOPSIS = "synopsis"
        const val COL_IMAGE_PATH = "image_path"
    }
}

