package com.haoduyoudu.DailyAccounts.utils

import android.app.Activity
import android.content.res.AssetManager
import android.graphics.*
import android.graphics.Bitmap.CompressFormat
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Base64
import android.util.Log
import android.view.View
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.BaseApplication.Companion.context
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.InputStream


object BitmapUtils {
    fun getImageFromAssetsFile(filePath: String): Bitmap {
        val am: AssetManager = BaseApplication.context.resources.assets
        val mIs: InputStream = am.open(filePath)
        val image = BitmapFactory.decodeStream(mIs)
        mIs.close()
        return image
    }

    fun getImageFromPath(path: String): Bitmap {
        val fis = FileInputStream(path)
        return BitmapFactory.decodeStream(fis)
    }

    fun getActivityShotBitmap(activity: Activity): Bitmap {
        val view: View = activity.window.decorView
        return viewConversionBitmap(view)
    }

    // 高斯模糊
    fun rsBlur(source: Bitmap, radius: Int = 0): Bitmap {
        val renderScript = RenderScript.create(context)
        Log.i("BitmapUtils", "scale size:" + source.width + "*" + source.height)
        val input = Allocation.createFromBitmap(renderScript, source)
        val output = Allocation.createTyped(renderScript, input.type)
        val scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        scriptIntrinsicBlur.setInput(input)
        if (radius != 0) {
            scriptIntrinsicBlur.setRadius(radius.toFloat())
        }
        scriptIntrinsicBlur.forEach(output)
        output.copyTo(source)
        renderScript.destroy()
        return source
    }

    fun viewConversionBitmap(v: View, config:Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
        val w = v.width
        val h = v.height
        val bmp = Bitmap.createBitmap(w, h, config)
        val c = Canvas(bmp)
        /** 如果不设置canvas画布为白色，则生成透明  */
        v.layout(0, 0, w, h)
        v.draw(c)
        return bmp
    }

    fun stringToBitmap(string: String): Bitmap {
        // 将字符串转换成Bitmap类型
        val bitmap: Bitmap
        val bitmapArray: ByteArray = Base64.decode(string, Base64.DEFAULT)
        bitmap = BitmapFactory.decodeByteArray(
            bitmapArray, 0,
            bitmapArray.size
        )
        return bitmap
    }

    fun bitmapToString(bitmap: Bitmap): String {
        //将Bitmap转换成字符串
        var string: String? = null
        val bStream = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.PNG, 100, bStream)
        val bytes: ByteArray = bStream.toByteArray()
        string = Base64.encodeToString(bytes, Base64.DEFAULT)
        return string
    }
}