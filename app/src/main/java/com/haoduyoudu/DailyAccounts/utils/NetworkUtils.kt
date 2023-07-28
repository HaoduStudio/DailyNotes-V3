package com.haoduyoudu.DailyAccounts.utils

import android.os.Handler
import android.os.Message
import com.haoduyoudu.DailyAccounts.BaseApplication

object NetworkUtils {
    fun isNetworkOnline(myHandler: Handler) {
        //Process ipProcess = runtime.exec("ping -c 1 114.114.114.114");
        Thread {
            try {
                val ip = "baidu.com"
                var ipProcess: Process? = null // -c 次数 -w 超时
                ipProcess = Runtime.getRuntime().exec("ping -c 1 $ip")
                //exitValue==0网络可用，否则网络不可用
                val exitValue = ipProcess.waitFor()
                val message = Message()
                message.arg1 = exitValue
                myHandler.sendMessage(message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}