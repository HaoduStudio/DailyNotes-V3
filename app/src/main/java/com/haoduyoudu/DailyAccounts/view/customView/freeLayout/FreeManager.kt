package com.haoduyoudu.DailyAccounts.view.customView.freeLayout

import android.graphics.Matrix
import android.util.Log
import com.haoduyoudu.DailyAccounts.view.customView.freeLayout.objects.FreeObject

class FreeManager {
    private val mFreeList = ArrayList<FreeObject>()

    companion object {
        private lateinit var mInstance: FreeManager

        @Synchronized
        fun getInstance(): FreeManager {
            if (!::mInstance.isInitialized) {
                mInstance = FreeManager()
            }
            return mInstance
        }
    }

    fun addFreeObj(obj: FreeObject) {
        mFreeList.add(obj)
    }

    fun deleteFreeObj(obj: FreeObject) {
        mFreeList.remove(obj)
    }

    fun getFreeObjList(): List<FreeObject> {
        return mFreeList
    }

    fun getFreeObj(x: Float, y: Float): FreeObject? {
        val dstPoints = FloatArray(2)
        val srcPoints = floatArrayOf(x, y)
        mFreeList.reversed().forEach { freeObj ->
            val matrix = Matrix()
            freeObj.mMatrix.invert(matrix)
            matrix.mapPoints(dstPoints, srcPoints)
            if (freeObj.getBounds().contains(dstPoints[0].toInt(), dstPoints[1].toInt())) {
                return freeObj
            }
        }
        return null
    }

    fun setFocusFreeObj(obj: FreeObject) {
        Log.d("FreeManger", "setFocus, $obj")
        mFreeList.remove(obj)
        mFreeList.add(obj)
        for (a in mFreeList) {
            a.isFocus = a == obj
        }
    }

    fun clear() {
        mFreeList.forEach {
            try {
                if (!it.srcBitmap.isRecycled) {
                    it.srcBitmap.recycle()
                    Log.d("FreeManager", "recycle bitmap ${it.srcBitmap}")
                }
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mFreeList.clear()
    }
}