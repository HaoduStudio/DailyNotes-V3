package com.haoduyoudu.DailyAccounts.view.activities.base

import android.content.Intent
import android.os.Bundle
import android.transition.Explode
import android.util.Log
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.utils.BitmapUtils

open class BaseActivity(private val noShot: Boolean = false): AppCompatActivity() {
    private val rootView by lazy { window.decorView }

    override fun startActivity(intent: Intent?) {
        shot()
        super.startActivity(intent)
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        shot()
        super.startActivityForResult(intent, requestCode)
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        shot()
        super.startActivityForResult(intent, requestCode, options)
    }

    private fun shot() {
        if (!noShot) {
            Log.d("BaseActivity", "shot")
            rootView.post {
                try {
                    BaseApplication.activityBitmap = BitmapUtils.viewConversionBitmap(rootView)
                }catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}