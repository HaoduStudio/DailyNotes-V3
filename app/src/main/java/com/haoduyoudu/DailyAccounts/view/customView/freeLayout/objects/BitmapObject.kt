package com.haoduyoudu.DailyAccounts.view.customView.freeLayout.objects

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log

class BitmapObject(private val mBitmap: Bitmap) : FreeObject() {

    init {
        initFreeObject(mBitmap)
        val scaleVal = (270 * 0.5f + 50f) / mBitmap.width.toFloat()
        scale(scaleVal, scaleVal)
    }

    override fun onDraw(canvas: Canvas?, paint: Paint) {
        Log.d("BitmapObject","on Draw")
        canvas?.drawBitmap(mBitmap, mMatrix, paint)
    }

    override fun getBounds(): Rect {
        Log.d("BitmapObj", "getBounds")
        return Rect(0, 0, mBitmap.width, mBitmap.height)
    }
}