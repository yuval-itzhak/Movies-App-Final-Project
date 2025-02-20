package com.example.movieapp.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.movieapp.data.MovieRepository
import com.example.movieapp.data.local.Movie
import com.example.movieapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieViewModel @Inject constructor(private val repository: MovieRepository) : ViewModel() {

    val popularMovies: LiveData<Resource<List<Movie>>> = repository.getPopularMovies()
    val upcomingMovies: LiveData<Resource<List<Movie>>> = repository.getUpcomingMovies()
    val favoriteMovies: LiveData<List<Movie>> = repository.getFavoriteMovies()

    fun updateFavorite(movie: Movie) = viewModelScope.launch {
        val updatedMovie = movie.copy(favorite = !movie.favorite)
        repository.updateFavorite(updatedMovie)
    }

    fun updateMovie(movie: Movie) = viewModelScope.launch {
        if (movie.title.isBlank()) {
            Log.e("VALIDATION", "Movie title cannot be empty")
            return@launch
        }
        if (movie.release_date.isBlank()) {
            Log.e("VALIDATION", "Release date cannot be empty")
            return@launch
        }
        if (movie.vote_average < 0 || movie.vote_average > 10) {
            Log.e("VALIDATION", "Invalid rating value")
            return@launch
        }

        repository.updateMovie(movie)
    }

    fun fetchPopularMovies() = viewModelScope.launch {
        repository.refreshPopularMovies()
    }

    fun fetchUpcomingMovies() = viewModelScope.launch {
        repository.refreshUpcomingMovies()
    }
}

