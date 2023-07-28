package com.haoduyoudu.DailyAccounts.view.customView.freeLayout

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent

interface IFreeOperation {
    fun translate(dx: Float, dy: Float)
    fun scale(sx: Float, sy: Float)
    fun rotate(degrees: Float)
    fun onDraw(canvas: Canvas?, paint: Paint)
    fun getBounds(): Rect
    fun onTouch(event: MotionEvent?)
}