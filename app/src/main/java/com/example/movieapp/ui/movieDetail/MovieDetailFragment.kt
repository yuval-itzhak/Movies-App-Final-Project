package com.example.movieapp.ui.movieDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.movieapp.data.local.Movie
import com.example.movieapp.databinding.FragmentMovieDetailBinding
import com.example.movieapp.ui.MovieViewModel
import com.example.movieapp.utils.Resource
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieDetailFragment : Fragment() {

    private var _binding: FragmentMovieDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MovieDetailViewModel by viewModels()
    private val args: MovieDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val movieId = args.movie.id

        // Use the new DetailViewModel
        viewModel.getMovieById(movieId).observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.titleTextView.text = "Loading..."
                    binding.overviewTextView.text = "Loading..."
                }
                is Resource.Success -> {
                    resource.data?.let { updateUI(it) }
                }
                is Resource.Error -> {
                    Toast.makeText(context, "Failed to load movie details", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Fetch trailer using the new ViewModel
        viewModel.fetchTrailer(movieId) { videoId ->
            if (videoId != null) {
                binding.youtubePlayerView.visibility = View.VISIBLE
                loadYouTubeVideo(videoId)
            } else {
                binding.youtubePlayerView.visibility = View.GONE
                Toast.makeText(requireContext(), "Trailer not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(movie: Movie) {
        binding.titleTextView.text = movie.title
        binding.overviewTextView.text = movie.overview
        binding.releaseDateTextView.text = movie.release_date
        binding.ratingTextView.text = "★ ${movie.vote_average}"

        // Load poster image
        Glide.with(this)
            .load("https://image.tmdb.org/t/p/original${movie.posterPath}")
            .into(binding.backdropImageView)
    }

    private fun loadYouTubeVideo(videoId: String) {
        val youTubePlayerView: YouTubePlayerView = binding.youtubePlayerView
        lifecycle.addObserver(youTubePlayerView)

        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(videoId, 0f)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
