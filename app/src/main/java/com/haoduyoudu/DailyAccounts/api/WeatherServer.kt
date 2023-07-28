package com.haoduyoudu.DailyAccounts.api

import com.haoduyoudu.DailyAccounts.model.models.Weather
import retrofit2.Call
import retrofit2.http.GET

interface WeatherServer {

    @GET("/")
    fun getWeather(): Call<Weather>
}