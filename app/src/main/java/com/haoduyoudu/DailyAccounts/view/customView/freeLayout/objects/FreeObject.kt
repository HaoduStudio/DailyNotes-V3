package com.haoduyoudu.DailyAccounts.view.customView.freeLayout.objects

import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import com.haoduyoudu.DailyAccounts.view.customView.freeLayout.IFreeOperation

open class FreeObject : IFreeOperation {
    val mMatrix = Matrix()
    lateinit var mSrcPoints: FloatArray
    lateinit var mDstPoints: FloatArray
    private val mLastSinglePoint = PointF()
    lateinit var srcBitmap: Bitmap
    var isFocus = false

    fun initFreeObject(srcBitmap: Bitmap) {
         mSrcPoints = floatArrayOf(
             0f, 0f,
             srcBitmap.width.toFloat(), 0f,
             srcBitmap.width.toFloat(), srcBitmap.height.toFloat(),
             0f, srcBitmap.height.toFloat(),
             srcBitmap.width / 2f, srcBitmap.height / 2f
         )
        mDstPoints = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
        this.srcBitmap = srcBitmap
    }

    fun updatePoints() {
        //更新贴纸点坐标
        mMatrix.mapPoints(mDstPoints, mSrcPoints)
    }

    // Over write
    override fun translate(dx: Float, dy: Float) {
        mMatrix.postTranslate(dx, dy)
        updatePoints()
    }

    override fun scale(sx: Float, sy: Float) {
        mMatrix.postScale(sx, sy, mDstPoints[8], mDstPoints[9])
        updatePoints()
    }

    override fun rotate(degrees: Float) {
        mMatrix.postRotate(degrees, mDstPoints[8], mDstPoints[9])
        updatePoints()
    }

    override fun onDraw(canvas: Canvas?, paint: Paint) {
//        canvas.drawBitmap(srcBitmap, mMatrix, paint)
//        TODO("Not yet implemented")
    }

    override fun getBounds(): Rect {
        TODO("Not yet implemented")
    }

    override fun onTouch(event: MotionEvent?) {
        if (event != null) {
            Log.d("FreeObject", "onTouch")
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mLastSinglePoint.set(event.x, event.y)
                }
                MotionEvent.ACTION_MOVE -> {
                    translate(event.x - mLastSinglePoint.x, event.y - mLastSinglePoint.y)
                    Log.d("FreeObject", "Change x: ${event.x - mLastSinglePoint.x}, y: ${event.y - mLastSinglePoint.y}")
                    mLastSinglePoint.set(event.x, event.y)
                }
                MotionEvent.ACTION_UP -> {
                    mLastSinglePoint.set(0f, 0f)
                }
            }
        }
    }
    // End Over write
}