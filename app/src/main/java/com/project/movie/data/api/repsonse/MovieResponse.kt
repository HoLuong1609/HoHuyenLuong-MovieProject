package com.project.movie.data.api.repsonse

import com.google.gson.annotations.SerializedName

class MovieResponse {
    @SerializedName("title")
    var title: String? = null

    @SerializedName("image")
    var image: List<String>? = null
}