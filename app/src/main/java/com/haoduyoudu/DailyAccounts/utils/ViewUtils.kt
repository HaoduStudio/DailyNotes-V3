package com.haoduyoudu.DailyAccounts.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Build
import android.text.Layout
import android.text.TextUtils
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.TextView


object ViewUtils {
    fun ellipsizeEnd(tv: TextView, showLines: Int, originText: String, onComplete: (() -> Unit)? = null) {
        var mShowLine: Int
        var showEllipsis: Boolean

        if (TextUtils.isEmpty(originText)) {
            return
        }
        mShowLine = showLines
        tv.text = originText
        tv.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                try {
                    val lineCount = tv.lineCount
                    println(":> ### lineCount = $lineCount")

                    // 如果设置了 maxLine 属性的情况
                    if (tv.maxLines in 1 until mShowLine) {
                        mShowLine = tv.maxLines
                    }

                    // lineCount（实际展示行数） 、mShowLine（指定行数）
                    if (lineCount <= mShowLine) {
                        mShowLine = lineCount
                        showEllipsis = false
                    } else {
                        showEllipsis = true
                    }

                    // 需要在结尾添加省略号的情况
                    if (showEllipsis) {
                        val layout: Layout = tv.layout
                        var sum = 0 // 需要展示多少个字
                        for (i in 0 until mShowLine) {
                            sum += layout.getLineEnd(i) - layout.getLineStart(i)
                        }
                        println(":> ### sum = $sum")
                        val showStr = originText.substring(0, sum - 3) + "..."
                        tv.text = showStr
                    }

                    // 及时的清除 OnGlobalLayoutListener 监听
                    tv.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    if (onComplete != null) {
                        onComplete()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        })
    }

    fun fadeIn(view: View, time: Long = 200, onComplete: (() -> Unit)? = null) {
        view.post {
            view.alpha = 0f
            view.visibility = View.VISIBLE
            view.animate()
                .alpha(1f)
                .setDuration(time)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (onComplete != null) onComplete()
                    }
                })
        }
    }

    fun fadeOut(view: View, time: Long = 100, onComplete: (() -> Unit)? = null) {
        view.post {
            view.animate()
                .alpha(0f)
                .setDuration(time)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = View.GONE
                        if (onComplete != null) onComplete()
                    }
                })
        }
    }
}