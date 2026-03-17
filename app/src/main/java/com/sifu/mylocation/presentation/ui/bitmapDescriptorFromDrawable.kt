package com.sifu.mylocation.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun Context.bitmapDescriptorFromDrawable(
    @DrawableRes resId: Int,
    sizeDp: Int = 48
): BitmapDescriptor {
    // Decode bitmap on IO thread
    val bitmap = withContext(Dispatchers.IO) {
        val px       = (sizeDp * resources.displayMetrics.density).toInt()
        val drawable = ContextCompat.getDrawable(
            this@bitmapDescriptorFromDrawable, resId
        ) ?: error("Drawable $resId not found")
        drawable.setBounds(0, 0, px, px)
        val bmp = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        drawable.draw(Canvas(bmp))
        bmp
    }
    // BitmapDescriptorFactory MUST run on Main thread
    return withContext(Dispatchers.Main) {
        BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}