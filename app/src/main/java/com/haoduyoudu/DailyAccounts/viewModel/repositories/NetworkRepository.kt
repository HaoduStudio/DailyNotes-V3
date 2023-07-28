package com.haoduyoudu.DailyAccounts.viewModel.repositories

import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.api.RetrofitInstance
import com.haoduyoudu.DailyAccounts.api.DailyServer

object NetworkRepository {
    private val mengCalls = RetrofitInstance.getInstance(BaseApplication.BASE_SERVER_URI).create(DailyServer::class.java)

    fun getWeatherCall() = mengCalls.getWeather()
    fun getStickerListCall() = mengCalls.getStickerList()
    fun getTemplateListCall() = mengCalls.getTemplateList()
    fun getBackgroundListCall() = mengCalls.getBackgroundList()
    fun getGuideImgListCall() = mengCalls.getGuideImgList()
}