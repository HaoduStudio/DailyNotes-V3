package com.haoduyoudu.DailyAccounts.view.customView.freeLayout.objects

import android.graphics.*
import android.text.TextPaint
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max


class TextObject(mText: String, private val textPaint: TextPaint): FreeObject() {
    private var mSrcBitmap: Bitmap

    init {
        mSrcBitmap = generateBitmap(mText)
        initFreeObject(
            mSrcBitmap
        )
    }

    override fun onDraw(canvas: Canvas?, paint: Paint) {
        canvas?.drawBitmap(mSrcBitmap, mMatrix, paint)
    }

    override fun getBounds(): Rect {
        return Rect(0, 0, mSrcBitmap.width, mSrcBitmap.height)
    }

    private fun generateBitmap(text: String): Bitmap {
        val fontMetrics: Paint.FontMetrics = textPaint.fontMetrics
        val height = ceil(abs(fontMetrics.bottom) + abs(fontMetrics.top)).toInt()
        val lines = text.split("\n")
        var width = 0
        lines.forEach { line -> width = max(width, textPaint.measureText(line).toInt()) }
        val bitmap = Bitmap.createBitmap(width, lines.size * height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        for (i in lines.indices) {
            canvas.drawText(lines[i], 0F, (height * i).toFloat() + abs(fontMetrics.ascent), textPaint)
        }
        return bitmap
    }
}