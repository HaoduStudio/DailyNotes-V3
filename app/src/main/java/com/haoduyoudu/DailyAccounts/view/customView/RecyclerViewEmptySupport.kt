package com.haoduyoudu.DailyAccounts.view.customView

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.haoduyoudu.DailyAccounts.R


class RecyclerViewEmptySupport : RecyclerView {
    private var mEmptyView: View? = null

    private val emptyObserver: AdapterDataObserver = object : AdapterDataObserver() {
        @SuppressLint("LongLogTag")
        override fun onChanged() {
            try {
                Log.i(TAG, "onChanged: 000")
                if (mEmptyView != null && adapter != null) {
                    if (adapter!!.itemCount == 0) {
                        mEmptyView?.visibility = View.VISIBLE
                        this@RecyclerViewEmptySupport.visibility = View.GONE
                    } else {
                        mEmptyView?.visibility = View.GONE
                        this@RecyclerViewEmptySupport.visibility = View.VISIBLE
                    }
                }
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    fun setEmptyView(emptyView: View) {
        mEmptyView = emptyView
        (parent as ViewGroup).addView(emptyView)
    }


    @SuppressLint("LongLogTag")
    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        try {
            val ev = LayoutInflater.from(context).inflate(R.layout.layout_no_note_data, (parent as ViewGroup), false)
            setEmptyView(ev)

            Log.i(TAG, "setAdapter: adapter::$adapter")
            adapter?.registerAdapterDataObserver(emptyObserver)
            emptyObserver.onChanged()
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "RecyclerViewEmptySupport"
    }
}