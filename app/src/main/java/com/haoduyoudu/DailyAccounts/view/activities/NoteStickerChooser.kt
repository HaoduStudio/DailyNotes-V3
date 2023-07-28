package com.haoduyoudu.DailyAccounts.view.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.databinding.ActivityNoteStickerChooserBinding
import com.haoduyoudu.DailyAccounts.helper.makeToast
import com.haoduyoudu.DailyAccounts.model.models.StickerList
import com.haoduyoudu.DailyAccounts.utils.FileUtils
import com.haoduyoudu.DailyAccounts.view.activities.base.BaseActivity
import com.haoduyoudu.DailyAccounts.view.adapters.StickerAdapter
import com.haoduyoudu.DailyAccounts.viewModel.repositories.NetworkRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoteStickerChooser : BaseActivity(noShot = true) {

    private val binding by lazy { ActivityNoteStickerChooserBinding.inflate(layoutInflater) }
    private lateinit var stickerAdapter: StickerAdapter
    private val stickerList = ArrayList<StickerAdapter.StickerItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        try {
            val allStickerPaths = FileUtils.getAllFileNameInAssets(
                FileUtils.removePathSlashAtLast(BaseApplication.ASSETS_STICKER_PATH)
            ).map { BaseApplication.ASSETS_STICKER_PATH + it } + FileUtils.getAllFileNameInAssets(
                FileUtils.removePathSlashAtLast(BaseApplication.ASSETS_MOOD_PATH)
            ).map { BaseApplication.ASSETS_MOOD_PATH + it }

            for (i in allStickerPaths) {
                stickerList.add(StickerAdapter.StickerItem(i))
            }
            stickerAdapter = StickerAdapter(this, stickerList)

            binding.recyclerView.apply {
                layoutManager = StaggeredGridLayoutManager(3,
                    StaggeredGridLayoutManager.VERTICAL)
                adapter = stickerAdapter
            }

            makeToast("加载在线贴纸")
            NetworkRepository.getStickerListCall().enqueue(object : Callback<StickerList> {
                override fun onResponse(call: Call<StickerList>, response: Response<StickerList>) {
                    try {
                        response.body().let {
                            makeToast("成功加载")
                            it!!.stickerList.map { short -> BaseApplication.BASE_SERVER_URI + short }.forEach { item ->
                                // download only
                                Glide.with(this@NoteStickerChooser).asBitmap().load(item).into(object : SimpleTarget<Bitmap>() {
                                    override fun onResourceReady(
                                        resource: Bitmap,
                                        transition: Transition<in Bitmap>?
                                    ) {
                                        val targetPath = cacheDir.absolutePath + "/sticker_temp_${System.currentTimeMillis()}.png"
                                        FileUtils.saveBitmap(targetPath, resource, Bitmap.CompressFormat.PNG)
                                        stickerList.add(StickerAdapter.StickerItem("", targetPath))
                                        stickerAdapter.notifyDataSetChanged() // to dataInterst??
                                    }

                                })
                            }
                        }
                    }catch (e: Exception) {
                        onFailure(call, e)
                    }
                }

                override fun onFailure(call: Call<StickerList>, t: Throwable) {
                    t.printStackTrace()
                    if (t !is IllegalArgumentException) {
                        makeToast("获取在线贴纸失败，请检查网络")
                    }
                }

            })
        }catch (e: Exception) {
            e.printStackTrace()
            makeToast("Load stickers failure")
            finish()
        }

        setResult(RESULT_CANCELED, Intent())

        stickerAdapter.setOnItemClickListener { _, i ->
            val mSticker = stickerList[i]
            val mIntent = Intent()
            mIntent.putExtra("stickerPath", mSticker.path)
            mIntent.putExtra("bitmapPath", mSticker.uriBitmapPath)
            setResult(RESULT_OK, mIntent)
            finish()
        }
    }
}