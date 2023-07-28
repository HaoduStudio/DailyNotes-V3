package com.haoduyoudu.DailyAccounts.view.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import com.bumptech.glide.Glide
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.R
import com.haoduyoudu.DailyAccounts.databinding.ActivityCongratulationsBinding
import com.haoduyoudu.DailyAccounts.helper.makeToast
import com.haoduyoudu.DailyAccounts.utils.NetworkUtils
import com.haoduyoudu.DailyAccounts.utils.PlayMediaUtils
import com.haoduyoudu.DailyAccounts.view.activities.base.BaseActivity

class CongratulationsActivity : BaseActivity() {
    private val binding by lazy { ActivityCongratulationsBinding.inflate(layoutInflater) }
    private lateinit var ringReceiver: RINGReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Glide.with(this).load(R.drawable.ic_cute_loading).into(binding.progressBar)

        NetworkUtils.isNetworkOnline(object : Handler() {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.arg1 != 0) {
                    makeToast("当前无网络")
                    finish()
                }else {
                    loadContent()
                }
            }
        })

        val intentFilter = IntentFilter()
        intentFilter.addAction("com.xtc.alarmclock.action.ALARM_VIEW_SHOWING")
        intentFilter.addAction("com.xtc.videochat.start")
        intentFilter.addAction("android.intent.action.PHONE_STATE")
        ringReceiver = RINGReceiver()
        registerReceiver(ringReceiver,intentFilter)
    }

    private fun loadContent() {
        Glide.with(this).load(BaseApplication.BASE_SERVER_URI + "/static/congratulations_img.webp").into(binding.conImg)
        PlayMediaUtils.play(BaseApplication.BASE_SERVER_URI + "/static/congratulations_audio.mp3")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(ringReceiver)
        PlayMediaUtils.stop()
    }

    inner class RINGReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("PlayRecord","Play interrupted.")
            finish()
        }
    }
}