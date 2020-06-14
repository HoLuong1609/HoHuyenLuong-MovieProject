package com.project.movie

import android.content.Context
import android.util.DisplayMetrics
import java.text.DecimalFormat
import java.text.NumberFormat

object Utils {

    private const val SPACE_KB = 1024.0
    private const val SPACE_MB = 1024 * SPACE_KB
    private const val SPACE_GB = 1024 * SPACE_MB
    private const val SPACE_TB = 1024 * SPACE_GB

    fun bytes2String(sizeInBytes: Long): String? {
        val nf: NumberFormat = DecimalFormat()
        nf.maximumFractionDigits = 2
        return try {
            when {
                sizeInBytes < SPACE_KB -> {
                    nf.format(sizeInBytes).toString() + " Byte(s)"
                }
                sizeInBytes < SPACE_MB -> {
                    nf.format(sizeInBytes / SPACE_KB).toString() + " KB"
                }
                sizeInBytes < SPACE_GB -> {
                    nf.format(sizeInBytes / SPACE_MB).toString() + " MB"
                }
                sizeInBytes < SPACE_TB -> {
                    nf.format(sizeInBytes / SPACE_GB).toString() + " GB"
                }
                else -> {
                    nf.format(sizeInBytes / SPACE_TB).toString() + " TB"
                }
            }
        } catch (e: Exception) {
            "$sizeInBytes Byte(s)"
        }
    }

    fun dpToPx(context: Context, dp: Float): Float {
        return dp * (context.resources
            .displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
}