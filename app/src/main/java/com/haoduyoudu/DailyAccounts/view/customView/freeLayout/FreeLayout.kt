package com.haoduyoudu.DailyAccounts.view.customView.freeLayout

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.Display
import android.view.MotionEvent
import android.view.View
import com.haoduyoudu.DailyAccounts.utils.DisplayUtil
import com.haoduyoudu.DailyAccounts.view.customView.freeLayout.objects.FreeObject
import com.haoduyoudu.DailyAccounts.view.customView.sticker.StickerScrollView
import kotlin.math.ceil

class FreeLayout : View, View.OnTouchListener {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var focusFreeObj: FreeObject? = null
    private var mPaint: Paint? = null
    private val mFM = FreeManager.getInstance()
    var editMode = false
    private lateinit var onObjFocusChangeListener: (FreeObject, Boolean) -> Unit

    init {
        setOnTouchListener(this)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event == null) {
            return false
        }
        if (!editMode) {
            return false
        }
        Log.d("FreeLayout", "onTouch")
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                requestFocus()
                val before = focusFreeObj
                focusFreeObj = mFM.getFreeObj(event.x, event.y)

                if (focusFreeObj != null) {
                    mFM.setFocusFreeObj(focusFreeObj!!)
                    if (::onObjFocusChangeListener.isInitialized) {
                        onObjFocusChangeListener(focusFreeObj!!, true)
                    }
                }else {
                    before?.isFocus = false
                    if (::onObjFocusChangeListener.isInitialized && before != null) {
                        onObjFocusChangeListener(before, false)
                    }
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (focusFreeObj != null) { // draging
                    val scrollView = parent.parent as StickerScrollView
                    scrollView.isGetFocus = false
                    onObjFocusChangeListener(focusFreeObj!!, false)

                    if(event.y < scrollView.scrollY + 50){
                        Log.d("Sticker","MoveUp")
                        scrollView.post {
                            try {
                                scrollView.smoothScrollTo(0, scrollView.scrollY - 10)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }else if(event.y > scrollView.scrollY + 360-50){
                        Log.d("Sticker","MoveDown")
                        scrollView.post {
                            try {
                                scrollView.smoothScrollTo(0, scrollView.scrollY + 10)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                (parent.parent as StickerScrollView).isGetFocus = true
            }
        }

        if (focusFreeObj != null) {
            focusFreeObj?.onTouch(event)
        }
        invalidate()
        return true
    }

    fun addFreeObj(obj: FreeObject) {
        mFM.addFreeObj(obj)
        mFM.setFocusFreeObj(obj)
        invalidate()
    }

    fun deleteFreeObj(obj: FreeObject) {
        mFM.deleteFreeObj(obj)
        invalidate()
    }

    fun getFocusObj() = focusFreeObj

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val objList = mFM.getFreeObjList()
        objList.forEach { obj ->
            obj.onDraw(canvas, getPaint())
        }
    }

    private fun getPaint(): Paint {
        if (mPaint == null) {
            mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mPaint!!.color = Color.BLACK
            mPaint!!.strokeWidth = 2f
        }
        return mPaint!!
    }


    fun setOnObjFocusListener(func: (FreeObject, Boolean) -> Unit) {
        onObjFocusChangeListener = func
    }

    fun addPaper(pageSize: Int = ceil(layoutParams.height/360f).toInt() + 1) {
        val lp = layoutParams
        lp.height = 360 * pageSize
        layoutParams = lp
        invalidate()
    }

    fun deletePaper(pageSize: Int = ceil(layoutParams.height/360f).toInt() - 1) {
        val lp = layoutParams
        if (lp.height > 360) {
            lp.height = 360 * pageSize
            layoutParams = lp
            invalidate()
        }
    }

    fun getPaperSize() = layoutParams.height / 360

    fun getAllObj() = mFM.getFreeObjList()

    fun clear() {
        mFM.clear()
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mFM.clear()
    }
}