package com.haoduyoudu.DailyAccounts.api

import com.haoduyoudu.DailyAccounts.model.models.*
import retrofit2.Call
import retrofit2.http.GET

interface DailyServer {

    @GET("/api/get_weather/")
    fun getWeather(): Call<Weather>

    @GET("/api/get_sticker_list/")
    fun getStickerList(): Call<StickerList>

    @GET("/api/get_template_list/")
    fun getTemplateList(): Call<TemplateList>

    @GET("/api/get_background_list/")
    fun getBackgroundList(): Call<BackgroundList>

    @GET("/api/get_guide_img_list/")
    fun getGuideImgList(): Call<GuideImgList>
}