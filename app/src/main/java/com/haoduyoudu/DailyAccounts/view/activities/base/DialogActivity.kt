package com.haoduyoudu.DailyAccounts.view.activities.base

import android.annotation.SuppressLint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.R
import com.haoduyoudu.DailyAccounts.utils.ViewUtils


open class DialogActivity(private val canDis: Boolean = false, noShot: Boolean = false) : BaseActivity(noShot = noShot) {

    private val rootView by lazy { findViewById<ViewGroup>(android.R.id.content).getChildAt(0) }

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (canDis) {
            setTheme(R.style.DialogActivityTheme2)
        }else {
            setTheme(R.style.DialogActivityTheme)
        }
        setWindowAlpha(0f)
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        try {
            window.setBackgroundDrawable(BitmapDrawable(BaseApplication.activityBitmap))
            Log.d("DialogActivity", "Setting window background")
        }catch (e: Exception) {
            e.printStackTrace()
        }
        rootView.visibility = View.INVISIBLE
        setWindowAlpha(1f)
        super.onStart()
        ViewUtils.fadeIn(rootView)
    }

    override fun finish() {
        ViewUtils.fadeOut(rootView)

        rootView.postDelayed({
            super.finish()
            overridePendingTransition(0, 0)
        }, 100)
    }


    private fun setWindowAlpha(data: Float) {
        window.attributes = window.attributes.apply {
            alpha = data
        }
    }
}