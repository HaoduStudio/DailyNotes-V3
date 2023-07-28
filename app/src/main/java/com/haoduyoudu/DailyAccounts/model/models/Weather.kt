package com.haoduyoudu.DailyAccounts.model.models

import android.util.Log
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.regex.Pattern

data class Weather (
    @SerializedName("weather")
    val mWeather: String,

    @SerializedName("result_code")
    val code: Int?
) {
    fun getWeather(): String {
        if (code == null || code != 0) {
            throw RuntimeException("Result code is 1 or null")
        }
        return mWeather
    }
}