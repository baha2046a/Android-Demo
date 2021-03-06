package com.example.tmdb.api

import android.util.Log
import com.example.tmdb.Common
import com.example.tmdb.io.NetworkConnectionInterceptor
import com.example.tmdb.model.MovieDetail
import com.example.tmdb.model.MovieSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*


object TmdbApiService {
    private val api = create()

    fun actionSearchMovie(
        keyForValidCheck: String,
        param: Map<String, String>,
        receiver: (String, Optional<MovieSearchResult>) -> Unit
    ) {
        GlobalScope.launch {
            val response = api.searchMovie(param)
            Log.d("API Search Result", "Response Code : ${response.code()}")
            withContext(Dispatchers.Main) {
                receiver.invoke(keyForValidCheck, Optional.ofNullable(response.body()))
            }
        }
    }

    fun actionGetMovieDetail(movieId: Int, receiver: (MovieDetail) -> Unit) {
        val params: MutableMap<String, String> = HashMap()
        params["api_key"] = Common.API_KEY
        params["language"] = Common.PREFER_LANG

        GlobalScope.launch {
            val response = api.movieDetail(movieId.toString(), params)
            Log.d("API Detail Result", "Response Code : ${response.code()}")
            if (response.isSuccessful) {
                response.body()?.let {
                    withContext(Dispatchers.Main) {
                        receiver.invoke(it)
                    }
                }
            }
        }
    }

    fun getImageUrl(imgString: String, preferWidth: Int = 0): String {
        val apiPath = when (preferWidth) {
            0 -> Common.MOVIE_IMAGE_API.replace("{width}", "original")
            else -> Common.MOVIE_IMAGE_API.replace("{width}", "w$preferWidth")
        }
        return "${Common.BASE_IMAGE_URL}$apiPath$imgString"
    }

    private fun create(): TmdbApi {
        val httpClientBuilder: OkHttpClient.Builder = OkHttpClient.Builder()
            .addInterceptor(NetworkConnectionInterceptor())
        val retrofit: Retrofit = Retrofit.Builder().baseUrl(Common.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(httpClientBuilder.build())
            .build()
        return retrofit.create(TmdbApi::class.java)
    }

    // For API Benchmark Test Only
    suspend fun searchBenchmark(param: Map<String, String>): Response<MovieSearchResult> {
        return api.searchMovie(param)
    }
}

fun String.toTmdbImg(width: Int = 0) = TmdbApiService.getImageUrl(this, width)
