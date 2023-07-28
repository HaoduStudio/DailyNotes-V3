package com.haoduyoudu.DailyAccounts.view.adapters

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class FixLinearLayoutManager(context: Context) : LinearLayoutManager(context){
    init {
        this.orientation = LinearLayoutManager.VERTICAL
    }
    override fun canScrollVertically() = false
}