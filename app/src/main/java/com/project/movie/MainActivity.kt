package com.project.movie

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.project.movie.data.api.repsonse.MovieResponse
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.*

class MainActivity : AppCompatActivity(), OnDownloadListener {

    private var mDownloadedKb = 0
    private val mPathList = arrayListOf<String>()
    private var mPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            fetchData()
        } else {
            //Register permission
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE_SAVE_IMAGE
            )
        }
        ivMovie.setOnClickListener {
            if (mPathList.size > mPosition + 1) {
                mPosition++
                loadImage(mPathList[mPosition])
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE_SAVE_IMAGE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            fetchData()
            return
        }
    }

    private fun fetchData() {
        val dataStr =
            "{'title':'Civil War','image':['http://movie.phinf.naver.net/20151127_272/1448585271749MCMVs_JPEG/movie_image.jpg?type=m665_443_2','http://movie.phinf.naver.net/20151127_84/1448585272016tiBsF_JPEG/movie_image.jpg?type=m665_443_2','http://movie.phinf.naver.net/20151125_36/1448434523214fPmj0_JPEG/movie_image.jpg?type=m665_443_2']}"
        val response = Gson().fromJson(dataStr, MovieResponse::class.java)
        onHandleResponse(response)
    }

    private fun onHandleResponse(response: MovieResponse) {
        tvTitle.text = response.title
        val numberOfThread = 10
        val executor = Executors.newFixedThreadPool(numberOfThread)
        val service = ExecutorCompletionService<String>(executor)
        val callables = arrayListOf<Callable<String>>()
        val imageUrlList = response.image ?: listOf()
        for (index in imageUrlList.indices) {
            callables.add(DownloadImageCallable(imageUrlList[index], this))
        }
        for (callable in callables) {
            service.submit(callable)
        }
        var future = service.take()
        mPathList.add(future.get())
        future = service.take()
        mPathList.add(future.get())
        future = service.take()
        mPathList.add(future.get())
        onAllImageDownloaded()
    }

    override fun onStartDownload() {
        tvStatus.text = getString(R.string.downloading)
    }

    override fun onProgress(downloadedKb: Int) {
        mDownloadedKb += downloadedKb
        tvStatus.text = "$mDownloadedKb Kb downloading"
    }

    override fun onFailed(e: Exception?) {
        tvStatus.text = e?.message
    }

    private fun onAllImageDownloaded() {
        loadImage(mPathList[0])
    }

    private fun loadImage(path: String) {
        val myBitmap = BitmapFactory.decodeFile(path)
        ivMovie.setImageBitmap(myBitmap)
    }

    private fun checkPermission(permission: String?): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission!!
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                //Permission don't granted
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    // Permission isn't granted
                    false
                } else {
                    // Permission don't granted and don't show dialog again.
                    false
                }
            } else true
        } else {
            true
        }
    }

    companion object {
        const val REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE_SAVE_IMAGE = 100
    }
}
