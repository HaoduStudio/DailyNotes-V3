package com.haoduyoudu.DailyAccounts.helper

import android.widget.Toast
import com.haoduyoudu.DailyAccounts.BaseApplication

fun makeToast(msg: CharSequence) {
    Toast.makeText(BaseApplication.context, msg, Toast.LENGTH_SHORT).show()
}