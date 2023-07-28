package com.haoduyoudu.DailyAccounts.view.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.haoduyoudu.DailyAccounts.R
import com.haoduyoudu.DailyAccounts.databinding.ActivityPlayRecordBinding
import com.haoduyoudu.DailyAccounts.helper.makeToast
import com.haoduyoudu.DailyAccounts.utils.PlayMediaUtils
import com.haoduyoudu.DailyAccounts.utils.VolumeUtil
import com.haoduyoudu.DailyAccounts.view.activities.base.DialogActivity
import java.util.*
import kotlin.concurrent.timerTask

class PlayRecord : DialogActivity() {

    private val binding by lazy { ActivityPlayRecordBinding.inflate(layoutInflater) }
    private val tag = "PlayRecord"
    private val mTimer = Timer()
    private lateinit var ringReceiver: RINGReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.playProgress.progress = 0f
        Log.d("PlayRecord", "Init val progress to ${VolumeUtil.mediaVolume}")
        binding.volBar.progress = 0
        binding.volBar.max = VolumeUtil.mediaMaxVolume
        binding.volBar.post {
            binding.volBar.progress = VolumeUtil.mediaVolume
        }

        binding.volBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    try {
                        VolumeUtil.mediaVolume = binding.volBar.progress
                        Log.d("PlayRecord", "Now change vol to $p1")
                    }catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}

        })

        Glide.with(this).load(R.drawable.ic_playing_sound).into(binding.playGif)

        try {
            val path = intent.getStringExtra("path")
            if (path == null) {
                makeToast("系统错误")
                finish()
            }else {
                Log.d("PlayRecord", "Record time: ${PlayMediaUtils.getTime()}")
                PlayMediaUtils.play(path)
            }
        }catch (e: Exception) {
            e.printStackTrace()
            makeToast("系统错误")
            finish()
        }

        PlayMediaUtils.setOnMediaBeReadyListener() {
            binding.playProgress.maxProgress = PlayMediaUtils.getTime().toFloat()
            binding.volBar.progress = 0

            mTimer.schedule(timerTask {
                if (PlayMediaUtils.isPlaying()) {
                    runOnUiThread {
                        Log.d("PlayRecord", "Pos now: ${PlayMediaUtils.getPos()}")
                        binding.playProgress.progress = PlayMediaUtils.getPos().toFloat()
                    }
                }else {
                    runOnUiThread {
                        binding.playProgress.progress = binding.playProgress.maxProgress
                    }
                    mTimer.cancel()
                }
            }, 500, 500)
        }

        PlayMediaUtils.setOnCompleteListener {
            try {
                binding.tittle.text = "完成！"
                (binding.playGif.drawable as GifDrawable).stop()
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.close.setOnClickListener {
            finish()
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction("com.xtc.alarmclock.action.ALARM_VIEW_SHOWING")
        intentFilter.addAction("com.xtc.videochat.start")
        intentFilter.addAction("android.intent.action.PHONE_STATE")
        ringReceiver = RINGReceiver()
        registerReceiver(ringReceiver,intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        mTimer.cancel()
        PlayMediaUtils.stop()
        unregisterReceiver(ringReceiver)
    }

    inner class RINGReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("PlayRecord","Play interrupted.")
            finish()
        }
    }
}