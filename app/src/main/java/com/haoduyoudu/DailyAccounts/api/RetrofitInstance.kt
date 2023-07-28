package com.haoduyoudu.DailyAccounts.api

import android.util.Log
import android.webkit.WebSettings
import com.xtc.shareapi.share.constant.OpenApiConstant.App
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitInstance {

    companion object {
        private var INSTANCE: Retrofit? = null

        fun getInstance(baseUri: String): Retrofit = INSTANCE ?: kotlin.run {
            Retrofit.Builder()
                .client(getOkHttpClient())
                .baseUrl(baseUri)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        private fun getOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request()
                        .newBuilder()
                        .removeHeader("User-Agent") //移除旧的
                        .addHeader("User-Agent", getUserAgent()) //添加真正的头部
                        .build()
                    chain.proceed(request)
                }.build()
        }

        private fun getUserAgent(): String {
            var userAgent = ""
            val sb = StringBuffer()
            userAgent = System.getProperty("http.agent") as String
            var i = 0
            val length = userAgent.length
            while (i < length) {
                val c = userAgent[i]
                if (c <= '\u001f' || c >= '\u007f') {
                    sb.append(String.format("\\u%04x", c.code))
                } else {
                    sb.append(c)
                }
                i++
            }
            Log.v("User-Agent", "User-Agent: $sb")
            return sb.toString()
        }
    }
}
