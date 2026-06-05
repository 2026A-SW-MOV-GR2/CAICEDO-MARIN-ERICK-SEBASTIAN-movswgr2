package com.epn.moviedual

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.epn.moviedual.data.MovieRepository
import com.epn.moviedual.data.RepositoryFactory
import com.epn.moviedual.databinding.ActivityMainBinding
import com.epn.moviedual.ui.MovieAdapter
import com.google.android.material.chip.Chip

/**
 * Main activity displaying the movie list with dual persistence engine toggle.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var movieRepository: MovieRepository
    private lateinit var movieAdapter: MovieAdapter
    private var useSQL = true // Default to SQLite

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupRepositoryToggle()
        setupFAB()

        // Load movies from SQLite by default
        loadMovies()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)
    }

    private fun setupRecyclerView() {
        movieAdapter = MovieAdapter(
            onItemClick = { movie ->
                // Open edit movie activity
                val intent = Intent(this, AddEditMovieActivity::class.java)
                intent.putExtra("movie_id", movie.id)
                intent.putExtra("use_sql", useSQL)
                startActivityForResult(intent, EDIT_MOVIE_REQUEST)
            },
            onItemLongClick = { movie ->
                // Show delete confirmation dialog
                showDeleteConfirmation(movie.id)
            }
        )

        binding.rvMovies.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = movieAdapter
        }
    }

    private fun setupRepositoryToggle() {
        binding.switchEngine.apply {
            isChecked = useSQL
            setOnCheckedChangeListener { _, isChecked ->
                useSQL = isChecked
                loadMovies()
                updateEngineChip()
                Log.d("MovieRepo", "Switched to ${if (useSQL) "SQLite" else "NoSQL"}")
            }
        }
        updateEngineChip()
    }

    private fun updateEngineChip() {
        val chip: Chip = binding.chipEngine
        if (useSQL) {
            chip.text = "SQLite"
            chip.setChipBackgroundColorResource(android.R.color.holo_green_light)
        } else {
            chip.text = "NoSQL"
            chip.setChipBackgroundColorResource(android.R.color.holo_orange_light)
        }
    }

    private fun setupFAB() {
        binding.fabAddMovie.setOnClickListener {
            val intent = Intent(this, AddEditMovieActivity::class.java)
            intent.putExtra("use_sql", useSQL)
            startActivityForResult(intent, ADD_MOVIE_REQUEST)
        }
    }

    private fun loadMovies() {
        movieRepository = RepositoryFactory.getRepository(this, useSQL)
        val movies = movieRepository.getAll()
        movieAdapter.setMovies(movies)

        Log.d("MovieRepo", "Loaded ${movies.size} movies using ${if (useSQL) "SQLite" else "NoSQL"}")
    }

    private fun showDeleteConfirmation(movieId: Int) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete))
            .setMessage(getString(R.string.delete_confirmation))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteMovie(movieId)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun deleteMovie(movieId: Int) {
        val success = movieRepository.delete(movieId)
        if (success) {
            loadMovies()
            Log.i("MovieRepo", "Movie deleted successfully (ID: $movieId)")
        } else {
            Log.e("MovieRepo", "Failed to delete movie (ID: $movieId)")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                ADD_MOVIE_REQUEST, EDIT_MOVIE_REQUEST -> {
                    // Refresh the list
                    loadMovies()
                }
            }
        }
    }

    companion object {
        private const val ADD_MOVIE_REQUEST = 1001
        private const val EDIT_MOVIE_REQUEST = 1002
    }
}

