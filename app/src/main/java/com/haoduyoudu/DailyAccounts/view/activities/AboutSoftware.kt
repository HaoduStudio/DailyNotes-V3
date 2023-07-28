package com.haoduyoudu.DailyAccounts.view.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.R
import com.haoduyoudu.DailyAccounts.databinding.ActivityAboutSoftwareBinding
import com.haoduyoudu.DailyAccounts.helper.makeToast
import com.haoduyoudu.DailyAccounts.utils.ViewUtils
import com.haoduyoudu.DailyAccounts.view.activities.base.BaseActivity
import com.haoduyoudu.DailyAccounts.viewModel.viewModels.GlobalViewModel


class AboutSoftware : BaseActivity() {
    private val binding by lazy { ActivityAboutSoftwareBinding.inflate(layoutInflater) }
    private val appViewModel by lazy { BaseApplication.viewModel as GlobalViewModel }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.openSourceText.setOnClickListener {
            makeToast("视频加载中\n改编自：@我家邻居全是猫")
            val mIntent = Intent(this, ViewImage::class.java)
            mIntent.putExtra("isVideo", true)
            mIntent.putExtra("path", BaseApplication.BASE_SERVER_URI + "/static/about_eggshell_video.mp4")
            startActivity(mIntent)
        }

        binding.artPeopleInfo.setOnClickListener {
            val mIntent = Intent(this, ViewImage::class.java)
            mIntent.putExtra("path", BaseApplication.BASE_SERVER_URI + "/static/about_thanks_img.png")
            mIntent.putExtra("zoomEnabled", false)
            startActivity(mIntent)
        }

        binding.haodusWindow.setOnClickListener {
            makeToast("诶哟你干嘛 (")
        }

        binding.mengxisWindow.setOnClickListener {
            makeToast("WebStorm，启动！")
        }

        val verName = packageManager.
            getPackageInfo(packageName, 0).versionName
        binding.versionInfo.text = "Version $verName"

        // tot = 2.5, ge ge ge (?
        ViewUtils.fadeIn(binding.aboutForeground, 1000) {
            binding.aboutForeground.postDelayed({
                ViewUtils.fadeOut(binding.aboutForeground, 500) {
                    startMainPage()
                }
            }, 1000)
        }
    }

    private fun startMainPage() {
        binding.videoView.visibility = View.GONE
        binding.aboutForeground.visibility = View.GONE
        binding.aboutRoot.visibility = View.VISIBLE
//        Glide.with(this).asGif().load(R.drawable.ic_cute_loading).into(binding.userPpImg)
        Glide
            .with(this)
            .load(BaseApplication.BASE_SERVER_URI + "/static/about_user_privacy_policy.png")
            .placeholder(R.drawable.ic_cute_loading)
            .skipMemoryCache(true) // 不使用内存缓存
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(binding.userPpImg)
    }
}