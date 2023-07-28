package com.haoduyoudu.DailyAccounts.view.activities

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.R
import com.haoduyoudu.DailyAccounts.databinding.ActivityBackgroundChooserBinding
import com.haoduyoudu.DailyAccounts.helper.makeToast
import com.haoduyoudu.DailyAccounts.helper.toBoolean
import com.haoduyoudu.DailyAccounts.model.models.BackgroundList
import com.haoduyoudu.DailyAccounts.utils.FileUtils
import com.haoduyoudu.DailyAccounts.viewModel.repositories.NetworkRepository
import com.haoduyoudu.DailyAccounts.viewModel.viewModels.GlobalViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BackgroundChooser : AppCompatActivity() {
    private val binding by lazy { ActivityBackgroundChooserBinding.inflate(layoutInflater) }
    private val appViewModel by lazy { BaseApplication.viewModel as GlobalViewModel }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        val backgrounds = ArrayList<String>()
        try {
            backgrounds.add(
                BaseApplication.ASSETS_WEATHER_BACKGROUND_PATH + assets.list(
                    FileUtils.removePathSlashAtLast(BaseApplication.ASSETS_WEATHER_BACKGROUND_PATH)
                )!![0]
            )
            backgrounds.addAll(
                assets.list(
                    FileUtils.removePathSlashAtLast(BaseApplication.ASSETS_DEFAULT_BACKGROUND_PATH)
                )!!.toMutableList().map {
                    BaseApplication.ASSETS_DEFAULT_BACKGROUND_PATH + it
                }
            )

            for (i in 0 until backgrounds.size) {
                val it = backgrounds[i]
                val view = LayoutInflater.from(this).inflate(R.layout.app_background_item, binding.listFather, false)
                Glide.with(this).load("file:///android_asset/$it").into(
                    view.findViewById(R.id.background_image)
                )
                val mTv = view.findViewById<TextView>(R.id.background_name)
                if (i == 0) {
                    mTv.text = "随天气变化"
                }else {
                    mTv.text = "背景 $i"
                }

                val isWeather = !i.toBoolean()
                view.setOnClickListener { _ ->
                    Log.d("BackgroundChooser", "Choose background $it")
                    try {
                        appViewModel.setAppBackground(isWeather, "file:///android_asset/$it")
                    }catch (e: Exception) {
                        e.printStackTrace()
                        makeToast("Change failure")
                    }finally {
                        finish()
                    }
                }
                binding.listFather.addView(view)
            }

            makeToast("加载网络背景")
            NetworkRepository.getBackgroundListCall().enqueue(object : Callback<BackgroundList> {
                override fun onResponse(
                    call: Call<BackgroundList>,
                    response: Response<BackgroundList>
                ) {
                    try {
                        val obj = response.body()!!
                        val realUriList = obj.getList().map { BaseApplication.BASE_SERVER_URI + it }
                        if (!FileUtils.exists(BaseApplication.BACKGROUND_DOWNLOAD_FROM_URI_PATH)) {
                            FileUtils.makeRootDirectory(BaseApplication.BACKGROUND_DOWNLOAD_FROM_URI_PATH)
                        }
                        makeToast("获取成功")
                        realUriList.forEach {
                            Glide.with(this@BackgroundChooser).asBitmap().load(it).into(object : SimpleTarget<Bitmap>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                                ) {
                                    try {
                                        val fileName = it.substring(it.lastIndexOf("/") + 1, it.length)
                                        val cpPath = cacheDir.absolutePath + '/' + fileName
                                        FileUtils.saveBitmap(cpPath, resource)
                                        val view = LayoutInflater.from(this@BackgroundChooser).inflate(R.layout.app_background_item, binding.listFather, false)
                                        Glide.with(this@BackgroundChooser).load(cpPath).into(view.findViewById(R.id.background_image))
                                        val mTv = view.findViewById<TextView>(R.id.background_name)
                                        mTv.text = "在线背景"
                                        view.setOnClickListener {
                                            try {
                                                val tgPath = BaseApplication.BACKGROUND_DOWNLOAD_FROM_URI_PATH + fileName
                                                FileUtils.copyFile(cpPath, tgPath)
                                                appViewModel.setAppBackground(false, tgPath)
                                            }catch (e: Exception) {
                                                e.printStackTrace()
                                                makeToast("Setting error")
                                            }finally {
                                                finish()
                                            }
                                        }
                                        binding.listFather.addView(view)
                                    }catch (e: Exception) {
                                        e.printStackTrace()
                                        makeToast("Save bitmap failure")
                                    }
                                }
                            } )
                        }
                    }catch (e: Exception) {
                        onFailure(call, e)
                    }
                }

                override fun onFailure(call: Call<BackgroundList>, t: Throwable) {
                    t.printStackTrace()
                    if (t !is IllegalArgumentException) {
                        makeToast("加载网络背景失败")
                    }
                }
            }
            )
        }catch (e: Exception) {
            e.printStackTrace()
            makeToast("Load background failure")
            finish()
        }
    }
}