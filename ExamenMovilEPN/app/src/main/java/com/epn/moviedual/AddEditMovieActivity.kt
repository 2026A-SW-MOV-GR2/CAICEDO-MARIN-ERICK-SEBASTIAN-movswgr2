package com.epn.moviedual

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.epn.moviedual.data.Movie
import com.epn.moviedual.data.MovieRepository
import com.epn.moviedual.data.RepositoryFactory
import com.epn.moviedual.databinding.ActivityAddEditMovieBinding

/**
 * Activity for adding or editing a movie.
 */
class AddEditMovieActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditMovieBinding
    private lateinit var movieRepository: MovieRepository
    private var currentMovie: Movie? = null
    private var useSQL = true
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddEditMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)

        useSQL = intent.getBooleanExtra("use_sql", true)
        movieRepository = RepositoryFactory.getRepository(this, useSQL)

        setupToolbar()
        setupImagePicker()
        setupSaveButton()
        setupCancelButton()

        // Load movie data if editing
        val movieId = intent.getIntExtra("movie_id", -1)
        if (movieId != -1) {
            loadMovie(movieId)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = if (currentMovie == null) {
                getString(R.string.add_movie)
            } else {
                getString(R.string.edit_movie)
            }
            setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun loadMovie(movieId: Int) {
        // Find the movie in the repository
        val movies = movieRepository.getAll()
        currentMovie = movies.find { it.id == movieId }

        currentMovie?.let { movie ->
            binding.etTitle.setText(movie.title)
            binding.etYear.setText(movie.year.toString())
            binding.etGenre.setText(movie.genre)
            binding.etSynopsis.setText(movie.synopsis)

            // Load image
            if (!movie.imagePath.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(movie.imagePath)
                    selectedImageUri = uri
                    val inputStream = contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.ivMovieImage.setImageBitmap(bitmap)
                        inputStream.close()
                    }
                } catch (e: Exception) {
                    Log.e("MovieRepo", "Error loading image: ${e.message}", e)
                }
            }

            supportActionBar?.title = getString(R.string.edit_movie)
        }
    }

    private fun setupImagePicker() {
        binding.btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_PICKER_REQUEST)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            val selectedUri = data?.data
            if (selectedUri != null) {
                selectedImageUri = selectedUri
                try {
                    val inputStream = contentResolver.openInputStream(selectedUri)
                    if (inputStream != null) {
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.ivMovieImage.setImageBitmap(bitmap)
                        inputStream.close()
                    }
                } catch (e: Exception) {
                    Log.e("MovieRepo", "Error loading selected image: ${e.message}", e)
                }
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            saveMovie()
        }
    }

    private fun setupCancelButton() {
        binding.btnCancel.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun saveMovie() {
        val title = binding.etTitle.text.toString().trim()
        val yearStr = binding.etYear.text.toString().trim()
        val genre = binding.etGenre.text.toString().trim()
        val synopsis = binding.etSynopsis.text.toString().trim()

        // Validation
        if (title.isEmpty() || yearStr.isEmpty() || genre.isEmpty() || synopsis.isEmpty()) {
            Log.e("MovieRepo", "Validation failed: Empty fields")
            return
        }

        val year = yearStr.toIntOrNull()
        if (year == null) {
            Log.e("MovieRepo", "Validation failed: Invalid year")
            return
        }

        val movie = Movie(
            id = currentMovie?.id ?: 0,
            title = title,
            year = year,
            genre = genre,
            synopsis = synopsis,
            imagePath = selectedImageUri?.toString() ?: currentMovie?.imagePath
        )

        val success = if (currentMovie == null) {
            // Insert new movie
            val result = movieRepository.insert(movie)
            result > 0
        } else {
            // Update existing movie
            movieRepository.update(movie)
        }

        if (success) {
            Log.i("MovieRepo", "Movie saved successfully: ${movie.title}")
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            Log.e("MovieRepo", "Failed to save movie: ${movie.title}")
        }
    }

    companion object {
        private const val IMAGE_PICKER_REQUEST = 2001
    }
}

