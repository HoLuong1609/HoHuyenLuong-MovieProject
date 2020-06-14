package com.project.movie

import com.project.movie.data.api.repsonse.MovieResponse

interface OnFetchDataListener {

    fun onResponse(response: MovieResponse)
}