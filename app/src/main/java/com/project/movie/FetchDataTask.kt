package com.project.movie

import android.os.AsyncTask
import com.google.gson.Gson
import com.project.movie.data.api.repsonse.MovieResponse

class FetchDataTask(private val listener: OnFetchDataListener) : AsyncTask<Any, Any, MovieResponse>() {

    override fun doInBackground(vararg params: Any?): MovieResponse {
        return Gson().fromJson(FAKE_RESPONSE_DATA, MovieResponse::class.java)
    }

    override fun onPostExecute(movieResponse: MovieResponse) {
        super.onPostExecute(movieResponse)
        listener.onResponse(movieResponse)
    }
}