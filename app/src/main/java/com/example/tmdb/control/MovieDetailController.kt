package com.example.tmdb.control

import android.util.Log
import com.example.tmdb.api.TmdbApiService
import com.example.tmdb.model.MovieDetail

object MovieDetailController {
    private val detailCache: MutableMap<Int, MovieDetail> = hashMapOf()

    fun getDetail(movieId: Int, receiver: (MovieDetail) -> Unit) {
        // Try to load from cache
        detailCache[movieId]?.let {
            receiver.invoke(it)
            return
        }

        Log.d(this::class.simpleName, "GET detail : MovieId $movieId")

        // Load from API
        TmdbApiService.actionGetMovieDetail(movieId) {
            detailCache[movieId] = it
            receiver.invoke(it)
        }
    }

    fun clearCache() = detailCache.clear()
}