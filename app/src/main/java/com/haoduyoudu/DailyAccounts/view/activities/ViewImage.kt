package com.haoduyoudu.DailyAccounts.view.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaPlayer.OnSeekCompleteListener
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.haoduyoudu.DailyAccounts.R
import com.haoduyoudu.DailyAccounts.databinding.ActivityViewImageBinding
import com.haoduyoudu.DailyAccounts.helper.makeToast
import com.haoduyoudu.DailyAccounts.utils.BitmapUtils
import com.haoduyoudu.DailyAccounts.view.activities.base.BaseActivity
import com.xtc.shareapi.share.utils.BitmapUtil.getScreenWidth


class ViewImage : BaseActivity() {

    private val binding by lazy { ActivityViewImageBinding.inflate(layoutInflater) }
    private var playCount = 0
    lateinit var ringReceiver: RINGReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val path = intent.getStringExtra("path")
        if (path == null) {
            Log.e("ViewImage", "Path is empty")
            finish()
        }else {
            Log.d("ViewImage", "Path is $path")
            val isVideo = intent.getBooleanExtra("isVideo", false)
            if (isVideo) {
                binding.videoView.visibility = View.VISIBLE
                try {
                    if (path.startsWith("http")) {
                        binding.videoView.setVideoURI(Uri.parse(path))
                    }else {
                        binding.videoView.setVideoPath(path)
                    }
                    binding.videoView.start()
                }catch (e: Exception) {
                    Log.e("ViewIMage", "Play video failure")
                    e.printStackTrace()
                }

                binding.videoView.setOnCompletionListener {
                    playCount += 1
                    if (playCount <= 2) {
                        Handler().postDelayed({
                            binding.videoView.start()
                        }, 1000)

                    }
                }

            }else {
                binding.photoView.visibility = View.VISIBLE
                val isHttp = path.startsWith("http")
                try {
                    binding.photoView.setImage(ImageSource.resource(R.drawable.ic_image_loading_pic))
                    binding.photoView.isZoomEnabled = intent.getBooleanExtra("zoomEnabled", true)
                    if (isHttp) {
                        makeToast("加载中")
                    }

                    Glide.with(this)
                        .asBitmap()
                        .load(path)
                        .skipMemoryCache(isHttp) // 不使用内存缓存
                        .diskCacheStrategy(if (isHttp) DiskCacheStrategy.NONE else DiskCacheStrategy.ALL)
                        .into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                binding.photoView.setImage(
                                    ImageSource.cachedBitmap(resource)
                                )
                                binding.photoView.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP)
                            }

                            override fun onLoadFailed(errorDrawable: Drawable?) {
                                super.onLoadFailed(errorDrawable)
                                makeToast("加载失败")
                                finish()
                            }
                    })
                }catch (e: Exception) {
                    makeToast("Load failure")
                    finish()
                    e.printStackTrace()
                }
            }
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
        unregisterReceiver(ringReceiver)
    }

    inner class RINGReceiver : BroadcastReceiver(){

        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("ViewImage","哥哥(姐姐) 电话来啦！")
            finish()
        }
    }
}