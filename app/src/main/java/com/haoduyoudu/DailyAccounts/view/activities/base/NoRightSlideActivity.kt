package com.haoduyoudu.DailyAccounts.view.activities.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.haoduyoudu.DailyAccounts.R

open class NoRightSlideActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.NoRightSlideTheme)
        super.onCreate(savedInstanceState)
    }
}