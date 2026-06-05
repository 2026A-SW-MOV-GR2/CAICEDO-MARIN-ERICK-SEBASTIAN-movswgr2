package com.epn.moviedual.ui

import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.epn.moviedual.data.Movie
import com.epn.moviedual.databinding.ItemMovieBinding

/**
 * RecyclerView adapter for displaying movies.
 */
class MovieAdapter(
    private var movies: List<Movie> = emptyList(),
    private val onItemClick: (Movie) -> Unit,
    private val onItemLongClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    fun setMovies(newMovies: List<Movie>) {
        this.movies = newMovies
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding, onItemClick, onItemLongClick)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position])
    }

    override fun getItemCount(): Int = movies.size

    class MovieViewHolder(
        private val binding: ItemMovieBinding,
        private val onItemClick: (Movie) -> Unit,
        private val onItemLongClick: (Movie) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movie: Movie) {
            binding.apply {
                tvTitle.text = movie.title
                tvYearGenre.text = "${movie.year} • ${movie.genre}"
                tvSynopsis.text = movie.synopsis

                // Load image if available
                if (!movie.imagePath.isNullOrEmpty()) {
                    try {
                        val uri = Uri.parse(movie.imagePath)
                        val inputStream = itemView.context.contentResolver.openInputStream(uri)
                        if (inputStream != null) {
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            ivImage.setImageBitmap(bitmap)
                            inputStream.close()
                        }
                    } catch (e: Exception) {
                        // Fallback if image cannot be loaded
                        ivImage.setImageDrawable(null)
                    }
                }

                root.setOnClickListener { onItemClick(movie) }
                root.setOnLongClickListener {
                    onItemLongClick(movie)
                    true
                }
            }
        }
    }
}

