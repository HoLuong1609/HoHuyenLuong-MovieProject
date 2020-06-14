package com.project.movie

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.project.movie.Utils.bytes2String
import com.project.movie.data.api.repsonse.MovieResponse
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.Executors
import kotlin.math.roundToInt


@Suppress("SameParameterValue")
class MainActivity : AppCompatActivity(), OnDownloadListener, View.OnClickListener,
    OnFetchDataListener {

    private var mDownloadedBytes = 0
    private val mPathList = arrayListOf<String>()
    private var mPosition = 0
    private var mFetchDataTask: FetchDataTask? = null
    private var mResponse: MovieResponse? = null

    @SuppressLint("NewApi")
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
        ivMovie.setOnClickListener(this)
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
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mFetchDataTask?.status != AsyncTask.Status.FINISHED) {
            mFetchDataTask?.cancel(true)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ivMovie -> {
                if (mPathList.size > mPosition + 1) {
                    mPosition++
                    loadImage(mPathList[mPosition])
                }
            }
        }
    }

    override fun onResponse(response: MovieResponse) {
        mResponse = response
        tvTitle.text = response.title
        val numberOfThread = 10
        val executor = Executors.newFixedThreadPool(numberOfThread)
        val imageUrlList = response.image ?: listOf()
        for (index in imageUrlList.indices) {
            val downloadImageRunnable = DownloadImageRunnable(imageUrlList[index], this)
            executor.execute(downloadImageRunnable)
        }
        executor.shutdown()
    }

    private fun fetchData() {
        mFetchDataTask = FetchDataTask(this)
        mFetchDataTask?.execute()
    }

    override fun onStartDownload() {
        tvStatus.text = getString(R.string.downloading)
    }

    override fun onProgress(downloadedBytes: Int) {
        mDownloadedBytes += downloadedBytes
        runOnUiThread {
            tvStatus.text = String.format(
                Locale.getDefault(),
                getString(R.string.msg_downloading),
                bytes2String(mDownloadedBytes.toLong())
            )
        }
    }

    override fun onFailed(e: Exception?) {
        runOnUiThread {
            tvStatus.text = e?.message
        }
    }

    override fun onDownloadCompleted(path: String) {
        mPathList.add(path)
        if (mPathList.size == mResponse?.image?.size) {
            runOnUiThread {
                loadImage(mPathList[0])
            }
        }
    }

    private fun loadImage(path: String) {
        val width = resources.displayMetrics.widthPixels - Utils.dpToPx(this, 30f * 2)
        ivMovie.setImageBitmap(
            decodeSampledBitmapFromFile(
                path,
                width.toInt(),
                (width * 310 / 443).toInt()
            )
        )
    }

    private fun decodeSampledBitmapFromFile(
        path: String?,
        reqWidth: Int, reqHeight: Int
    ): Bitmap? { // BEST QUALITY MATCH

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)

        // Calculate inSampleSize
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        options.inPreferredConfig = Bitmap.Config.RGB_565
        var inSampleSize = 1
        if (height > reqHeight) {
            inSampleSize = (height.toFloat() / reqHeight.toFloat()).roundToInt()
        }
        val expectedWidth = width / inSampleSize
        if (expectedWidth > reqWidth) {
            //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
            inSampleSize = (width.toFloat() / reqWidth.toFloat()).roundToInt()
        }
        options.inSampleSize = inSampleSize

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(path, options)
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
