package com.haoduyoudu.DailyAccounts.view.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.R
import com.haoduyoudu.DailyAccounts.databinding.ActivityNoteViewerBinding
import com.haoduyoudu.DailyAccounts.helper.*
import com.haoduyoudu.DailyAccounts.model.listener.ChangeNoteDataCallBack
import com.haoduyoudu.DailyAccounts.model.models.Note
import com.haoduyoudu.DailyAccounts.utils.BitmapUtils
import com.haoduyoudu.DailyAccounts.utils.DateUtils
import com.haoduyoudu.DailyAccounts.utils.FileUtils
import com.haoduyoudu.DailyAccounts.utils.NoteUtils
import com.haoduyoudu.DailyAccounts.utils.ninePatch.NinePatchChunk
import com.haoduyoudu.DailyAccounts.view.activities.base.BaseActivity
import com.haoduyoudu.DailyAccounts.view.adapters.FixLinearLayoutManager
import com.haoduyoudu.DailyAccounts.view.adapters.ImageAdapter
import com.haoduyoudu.DailyAccounts.view.adapters.RecordAdapter
import com.haoduyoudu.DailyAccounts.viewModel.viewModels.GlobalViewModel
import com.xtc.shareapi.share.communication.SendMessageToXTC
import com.xtc.shareapi.share.manager.ShareMessageManager
import com.xtc.shareapi.share.shareobject.XTCImageObject
import com.xtc.shareapi.share.shareobject.XTCShareMessage
import rx_activity_result2.RxActivityResult
import java.io.File
import kotlin.concurrent.thread


class NoteViewer : BaseActivity() {

    private val binding by lazy { ActivityNoteViewerBinding.inflate(layoutInflater) }
    private val appViewModel by lazy { BaseApplication.viewModel as GlobalViewModel }
    private lateinit var noteData: Note

    private lateinit var imageAdapter: ImageAdapter
    private val imageList = ArrayList<ImageAdapter.ImageItem>()
    private lateinit var recordAdapter: RecordAdapter
    private val recordList = ArrayList<RecordAdapter.RecordItem>()

    private var _editing = false
    private var isEditing: Boolean
        get() = _editing
        set(value) {
            _editing = value
            val viewToChange = arrayListOf(
                binding.tmpLine1,
                binding.tmpLine2,
                binding.complete,
                binding.editTemplate,
                binding.editMood,
                binding.editBodyText,
                binding.mediaBar,
                binding.addMedia,
            )
            if (value) {
                viewToChange.forEach {
                    it.visibility = View.VISIBLE
                }
            }else {
                viewToChange.forEach {
                    it.visibility = View.GONE
                }
            }
            if (value) {
                binding.stickerLayout.visibility = View.GONE
            }else {
                binding.stickerLayout.visibility = View.VISIBLE
            }
        }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.stickerLayout.removeAllSticker()
        binding.stickerLayout.setCanEdit(false)

        val idData = intent.getLongExtra("noteId", -1L)
        val shareMode = intent.getBooleanExtra("share", false)
        isEditing = intent.getBooleanExtra("editMode", false)
        if (idData == -1L) {
            makeToast("Empty id")
            finish()
        }else {
            appViewModel.getNoteFromIdLiveData(idData).observe(this) {
                Log.d("NoteViewer", it.toString())
                if (!::noteData.isInitialized) {
                    try {
                        updateMood(it.mood)
                        updateBackground(it.data["template"]!!.toPair())
                        updateTextColor(it.data["textColor"]!!)
                        updateBodyText(it.data["body"]!!)
                        updateTittle(it.yy, it.mm, it.dd)
                        updateImage(it.data["imagePaths"]!!.toArray(), it.data["videoPaths"]!!.toArray())
                        updateRecord(it.data["recordPaths"]!!.toArray())
                        loadSticker(it)

                        if (shareMode) {
                            imageAdapter.setOnAllDoneListener {
                                try {
                                    shotToShare()
                                }catch (e: Exception) {
                                    runOnUiThread { makeToast("Shared failure") }
                                    e.printStackTrace()
                                }
                            }
                        }
                    }catch (e: Exception) {
                        makeToast("First Load Failure")
                        e.printStackTrace()
                    }
                }else {
                    try {
                        if (noteData.mood != it.mood) {
                            updateMood(it.mood)
                        }
                        if (noteData.data["template"]!! != it.data["template"]!!) {
                            updateBackground(it.data["template"]!!.toPair())
                        }
                        if (noteData.data["textColor"]!! != it.data["textColor"]) {
                            updateTextColor(it.data["textColor"]!!)
                        }
                        if (noteData.data["body"]!! != it.data["body"]!!) {
                            updateBodyText(it.data["body"]!!)
                        }
                        if (noteData.data["recordPaths"]!! != it.data["recordPaths"]!!) {
                            updateRecord(it.data["recordPaths"]!!.toArray())
                        }
                        if (noteData.data["imagePaths"]!! != it.data["imagePaths"]!! ||
                            noteData.data["videoPaths"]!! != it.data["videoPaths"]!!) {
                            updateImage(it.data["imagePaths"]!!.toArray(), it.data["videoPaths"]!!.toArray())
                        }
                    }catch (e: Exception) {
                        makeToast("Update Failure")
                        e.printStackTrace()
                    }
                }
                noteData = it
            }
        }

        binding.addMedia.setOnClickListener {
            val mIntent = Intent(this, NoteAddMedia::class.java)
            mIntent.putExtra("noteId", noteData.id)
            startActivity(mIntent)
        }

        binding.editBodyText.setOnClickListener {
            val mIntent = Intent(this, NoteEditBody::class.java)
            mIntent.putExtra("noteId", noteData.id)
            startActivity(mIntent)
        }

        binding.editMood.setOnClickListener {
            val mIntent = Intent(this, NoteChangeMood::class.java)
            mIntent.putExtra("noteId", noteData.id)
            startActivity(mIntent)
        }

        binding.editTemplate.setOnClickListener {
            val mIntent = Intent(this, NoteChangeTemplate::class.java)
            mIntent.putExtra("noteId", noteData.id)
            startActivity(mIntent)
        }

        binding.complete.setOnClickListener {
            Log.d("NoteViewer", "Complete note")
            isEditing = false
            binding.stickerLayout.visibility = View.GONE
            thread {
                Thread.sleep(500)
                val bitmap = BitmapUtils.viewConversionBitmap(binding.viewForShot)
                FileUtils.saveBitmap(File(cacheDir, "tempNoteShot.jpg").absolutePath, bitmap)
                val mIntent = Intent(this, NoteAddSticker::class.java)
                mIntent.putExtra("noteId", noteData.id)
                RxActivityResult.on(this).startIntent(mIntent).subscribe {
                    if (it.resultCode() == RESULT_OK) {
                        Log.d("NoteViewer", "Reload Sticker")
                        binding.stickerLayout.visibility = View.VISIBLE
                        loadSticker()
                        binding.stickerLayout.cleanAllFocus()
                    }else {
                        isEditing = true
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateRecord(pathList: ArrayList<String>) {
        if (!::recordAdapter.isInitialized) {
            recordAdapter = RecordAdapter(this, recordList)
            binding.recordList.apply {
                layoutManager = FixLinearLayoutManager(this@NoteViewer)
                adapter = recordAdapter
            }
            recordAdapter.setOnItemClickListener { view, i ->
                val mRecord = recordList[i]
                val mIntent = Intent(this, PlayRecord::class.java)
                mIntent.putExtra("path", mRecord.path)
                startActivity(mIntent)
            }
            recordAdapter.setOnItemLongClickListener { view, i ->
                if (::noteData.isInitialized && isEditing) {
                    val mRecord = recordList[i]
                    DeleteSafeCheck.check(this) { del ->
                        if (del) {
                            removeRecord(mRecord.path)
                        }
                    }
                }
                null
            }
        }

        recordList.clear()
        pathList.forEach {
            recordList.add(RecordAdapter.RecordItem(it))
        }
        recordList.sortBy {
            File(it.path).nameWithoutExtension.safeToLong()
        }
        recordAdapter.notifyDataSetChanged()
    }

    // "imagePaths" (Gson p)
    // "videoPaths" (Gson p)
    @SuppressLint("NotifyDataSetChanged")
    private fun updateImage(imagePath: ArrayList<String>, videoPath: ArrayList<String>) {
        if (!::imageAdapter.isInitialized) {
            imageAdapter = ImageAdapter(this@NoteViewer, imageList)
            binding.imageList.apply {
                layoutManager = FixLinearLayoutManager(this@NoteViewer)
                adapter = imageAdapter
            }

            imageAdapter.setOnItemClickListener { view, i ->
                val image = imageList[i]
                val mIntent = Intent(this, ViewImage::class.java)
                mIntent.putExtra("isVideo", image.isVideo)
                mIntent.putExtra("path", image.path)
                startActivity(mIntent)
            }

            imageAdapter.setOnItemLongClickListener { view, i ->
                if (::noteData.isInitialized && isEditing) {
                    val mPhoto = imageList[i]
                    DeleteSafeCheck.check(this) { del ->
                        if (del) {
                            if (mPhoto.isVideo) {
                                removeVideo(mPhoto.path)
                            }else {
                                removeImage(mPhoto.path)
                            }
                        }
                    }
                }
                null
            }
        }

        imageList.clear()
        imageList.addAll(
            imagePath.sortedBy {
                it.safeToLong()
            }.toMutableList().map {
                ImageAdapter.ImageItem(it, false)
            }
        )
        imageList.addAll(
            videoPath.sortedBy {
                it.safeToLong()
            }.toMutableList().map {
                ImageAdapter.ImageItem(it, true)
            }
        )
        imageAdapter.notifyDataSetChanged()
    }

    private fun removeRecord(path: String) {
        thread {
            appViewModel.changeNoteDataFromId(noteData.id, object : ChangeNoteDataCallBack {
                override fun doChange(it: Note) {
                    it.data["recordPaths"] = it.data["recordPaths"]!!.toArray<String>().let {
                        it.remove(path)
                        it.toGson()
                    }
                }

                override fun onChangeSuccessful() {}
                override fun onChangeFailure(e: Exception) {
                    e.printStackTrace()
                    makeToast("Deleted failure")
                }

            })
            FileUtils.delete(path)
        }
    }

    private fun removeVideo(path: String) {
        thread {
            appViewModel.changeNoteDataFromId(noteData.id, object : ChangeNoteDataCallBack {
                override fun doChange(it: Note) {
                    it.data["videoPaths"] = it.data["videoPaths"]!!.toArray<String>().let {
                        it.remove(path)
                        it.toGson()
                    }
                }

                override fun onChangeSuccessful() {}
                override fun onChangeFailure(e: Exception) {
                    e.printStackTrace()
                    makeToast("Deleted failure")
                }

            })
            FileUtils.delete(path)
        }
    }

    private fun removeImage(path: String) {
        thread {
            appViewModel.changeNoteDataFromId(noteData.id, object : ChangeNoteDataCallBack {
                override fun doChange(it: Note) {
                    it.data["imagePaths"] = it.data["imagePaths"]!!.toArray<String>().let {
                        it.remove(path)
                        it.toGson()
                    }
                }

                override fun onChangeSuccessful() {}
                override fun onChangeFailure(e: Exception) {
                    e.printStackTrace()
                    makeToast("Deleted failure")
                }

            })
            FileUtils.delete(path)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateTittle(yy: Int, mm: Int, dd: Int) {
        binding.date.text = "$yy-$mm-$dd"
        binding.dayOfWeek.text = DateUtils.getDayOfWeek(yy, mm, dd)
    }

    private fun updateMood(mood: Pair<Int, String>) {
        Glide.with(this)
            .load(BitmapUtils.getImageFromAssetsFile(BaseApplication.ASSETS_MOOD_PATH + mood.first + ".png"))
            .into(binding.moodImage)
        binding.moodText.text = mood.second
    }

    private fun updateBackground(tmp: Pair<Boolean, String>) {
        val bitmap: Bitmap = if (tmp.first) {
            BitmapUtils.getImageFromPath(tmp.second)
        }else {
            BitmapUtils.getImageFromAssetsFile(BaseApplication.ASSETS_TEMPLATE_PATH + "${tmp.second}.9.png")
        }
        val ninePatch = NinePatchChunk.create9PatchDrawable(this, bitmap, "background")
        binding.templateBackground.background = ninePatch
    }

    private fun updateTextColor(colorId: String) {
        BaseApplication.idToTextColor[colorId.toInt()]?.let {
            binding.bodyText.setTextColor(resources.getColor(it))
        }
    }

    private fun updateBodyText(str: String) {
        binding.bodyText.setText(str)
    }

    private fun loadSticker(note: Note = noteData) {
        try {
            NoteUtils.loadSticker(binding.stickerLayout, note)
        }catch (e: Exception) {
            e.printStackTrace()
            makeToast("View sticker failure")
        }
    }

    private fun shotToShare() {
        binding.viewForShot.postDelayed({
            try{
                val shareImage = BitmapUtils.viewConversionBitmap(binding.viewForShot)
                //第一步：创建XTCImageObject 对象，并设置bitmap属性为要分享的图片
                val xtcImageObject = XTCImageObject()
                xtcImageObject.setBitmap(shareImage);
                //如果图片在公共目录，可以直接设置图片路径即可
                //第二步：创建XTCShareMessage对象，并将shareObject属性设置为xtcTextObject对象
                val xtcShareMessage = XTCShareMessage();
                xtcShareMessage.shareObject = xtcImageObject;
                //第三步：创建SendMessageToXTC.Request对象，并设置message属性为xtcShareMessage
                val request = SendMessageToXTC.Request();
                request.message = xtcShareMessage;
                request.flag = 1
                //第四步：创建ShareMessageManager对象，调用sendRequestToXTC方法，传入SendMessageToXTC.Request对象和AppKey
                ShareMessageManager(this).sendRequestToXTC(request, BaseApplication.APP_ID);
            }catch (e:Exception){
                e.printStackTrace()
            }
        }, 800)
    }
}