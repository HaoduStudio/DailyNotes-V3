package com.haoduyoudu.DailyAccounts.view.activities

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.databinding.ActivityNoteChangeTemplateBinding
import com.haoduyoudu.DailyAccounts.helper.makeToast
import com.haoduyoudu.DailyAccounts.helper.toGson
import com.haoduyoudu.DailyAccounts.model.listener.ChangeNoteDataCallBack
import com.haoduyoudu.DailyAccounts.model.models.Note
import com.haoduyoudu.DailyAccounts.model.models.TemplateList
import com.haoduyoudu.DailyAccounts.utils.FileUtils
import com.haoduyoudu.DailyAccounts.view.activities.base.BaseActivity
import com.haoduyoudu.DailyAccounts.viewModel.repositories.NetworkRepository
import com.haoduyoudu.DailyAccounts.viewModel.viewModels.GlobalViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.concurrent.thread

class NoteChangeTemplate : BaseActivity(), View.OnClickListener {

    private val binding by lazy { ActivityNoteChangeTemplateBinding.inflate(layoutInflater) }
    private val appViewModel by lazy { BaseApplication.viewModel as GlobalViewModel }
    private var noteId = -1L
    private val viewToTmp = HashMap<View, Pair<Boolean, String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        noteId = intent.getLongExtra("noteId", -1L)
        if (noteId == -1L) {
            makeToast("Empty id")
            finish()
        }

        val allTemplateName = FileUtils.getAllFileNameInAssets(
            FileUtils.removePathSlashAtLast(BaseApplication.ASSETS_TEMPLATE_PATH)
        )

        allTemplateName.map { it.replace(".9.png", "") }.forEach { id ->
            addTmpView(false, id)
        }

        makeToast("获取在线模版")
        NetworkRepository.getTemplateListCall().enqueue(object: Callback<TemplateList> {
            override fun onResponse(call: Call<TemplateList>, response: Response<TemplateList>) {
                try {
                    FileUtils.makeRootDirectory(BaseApplication.TEMPLATE_DOWNLOAD_FROM_URI_PATH)
                    val uriList = response.body()!!.getList().map { BaseApplication.BASE_SERVER_URI + it }
                    makeToast("获取成功")
                    uriList.forEach {
                        Glide.with(this@NoteChangeTemplate).asBitmap().load(it).into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                try {
                                    val localPath = cacheDir.absolutePath + '/' + it.substring(it.lastIndexOf("/") + 1, it.length)
                                    if (!FileUtils.exists(localPath)) {
                                        FileUtils.saveBitmap(localPath, resource, Bitmap.CompressFormat.PNG)
                                    }
                                    addTmpView(true, localPath)
                                }catch (e: Exception) {
                                    e.printStackTrace()
                                    makeToast("Add online tmp failure")
                                }
                            }

                        })
                    }
                }catch (e: Exception) {
                    onFailure(call, e)
                }
            }

            override fun onFailure(call: Call<TemplateList>, t: Throwable) {
                t.printStackTrace()
                if (t !is IllegalArgumentException) {
                    makeToast("获取失败，请检查网络")
                }
            }

        })
    }

    private fun addTmpView(isUri: Boolean, path: String) {
        val button = ImageView(this)
        val lp = LinearLayout.LayoutParams(240, 280)
        lp.gravity = Gravity.CENTER_HORIZONTAL
        lp.bottomMargin = 20
        button.layoutParams = lp
        button.scaleType = ImageView.ScaleType.FIT_XY

        binding.templateList.addView(button)

        button.setOnClickListener(this)
        if (isUri) {
            Glide.with(this).load(path).into(button)
            viewToTmp[button] = Pair(true, path)
        }else {
            Glide.with(this).load("file:///android_asset/${BaseApplication.ASSETS_TEMPLATE_PATH}${path}.9.png").into(button)
            viewToTmp[button] = Pair(false, path)
        }
    }

    override fun onClick(view: View?) {
        if (view != null) {
            try {
                if (viewToTmp[view]!!.first) {
                    val it = viewToTmp[view]!!.second
                    val target = BaseApplication.TEMPLATE_DOWNLOAD_FROM_URI_PATH + '/' + it.substring(it.lastIndexOf("/") + 1, it.length)
                    FileUtils.copyFile(it, target)
                    changeTemplate(Pair(true, target))
                }else {
                    changeTemplate(viewToTmp[view]!!)
                }
            }catch (e: Exception) {
                makeToast("Binding Err")
                finish()
            }
        }
    }

    private fun changeTemplate(tmp: Pair<Boolean, String>) {
        thread {
            appViewModel.changeNoteDataFromId(noteId, object : ChangeNoteDataCallBack {
                override fun doChange(it: Note) {
                    it.data["template"] = tmp.toGson()
                }

                override fun onChangeSuccessful() {
                    finish()
                }
                override fun onChangeFailure(e: Exception) {
                    makeToast("Change failure")
                    e.printStackTrace()
                    finish()
                }
            })
        }
    }

}