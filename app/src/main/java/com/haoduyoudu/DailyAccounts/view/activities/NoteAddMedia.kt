package com.haoduyoudu.DailyAccounts.view.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.R
import com.haoduyoudu.DailyAccounts.databinding.ActivityNoteAddMediaBinding
import com.haoduyoudu.DailyAccounts.helper.makeToast
import com.haoduyoudu.DailyAccounts.helper.toArray
import com.haoduyoudu.DailyAccounts.helper.toGson
import com.haoduyoudu.DailyAccounts.model.listener.ChangeNoteDataCallBack
import com.haoduyoudu.DailyAccounts.model.models.Note
import com.haoduyoudu.DailyAccounts.utils.FileUtils
import com.haoduyoudu.DailyAccounts.view.activities.base.BaseActivity
import com.haoduyoudu.DailyAccounts.viewModel.viewModels.GlobalViewModel
import com.hw.videoprocessor.VideoProcessor
import com.hw.videoprocessor.util.VideoProgressListener
import com.permissionx.guolindev.PermissionX
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import rx_activity_result2.Result
import rx_activity_result2.RxActivityResult
import java.io.File
import kotlin.concurrent.thread

class NoteAddMedia : BaseActivity() {

    private val binding by lazy { ActivityNoteAddMediaBinding.inflate(layoutInflater) }
    private val appViewModel by lazy { BaseApplication.viewModel as GlobalViewModel }
    private lateinit var note: Note

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val nodeId = intent.getLongExtra("noteId", -1L)

        if (nodeId == -1L) {
            emptyNote()
        }else {
            appViewModel.getNoteFromIdLiveData(nodeId).observe(this) {
                note = it
            }

            Glide.with(this).load(R.drawable.ic_cute_loading).into(binding.progressBar)
        }

        val getDirToSave = fun (type: Int) = when (type) {
            0 -> "image/"
            1 -> "video/"
            else -> ""
        }
        val fileNameNow = System.currentTimeMillis().toString()

        binding.fromAlbum.setOnClickListener {
            if (::note.isInitialized) {
                requestPermission(object : PermissionRequestCallBack {
                    override fun onAllGranted() {
                        val intent = Intent()
                        intent.action = Intent.ACTION_GET_CONTENT
                        intent.type = "file/*"
                        intent.putExtra("com.xtc.camera.LEFT_BUTTON_TEXT", "关闭")
                        intent.putExtra("com.xtc.camera.RIGHT_BUTTON_TEXT", "选择")
                        RxActivityResult.on(this@NoteAddMedia).startIntent(intent).subscribe(object : Observer<Result<NoteAddMedia>> {
                            override fun onSubscribe(d: Disposable) {}

                            override fun onNext(data: Result<NoteAddMedia>) {
                                if (data.resultCode() == Activity.RESULT_OK) {
                                    val bundle: Bundle? = data.data().extras
                                    if (bundle != null) {
                                        val photoPath: String = bundle.getString(MediaStore.EXTRA_OUTPUT, null)
                                        //获取文件类型，0代表图片，1代表视频
                                        val type: Int = bundle.getInt("com.xtc.camera.EXTRA_PHOTO_TYPE")
                                        val saveDir = getDirToSave(type)
                                        val newPathInNote = note.data["noteFolder"]!! + saveDir + fileNameNow + FileUtils.getFileSuffix(photoPath)
                                        binding.lodingView.visibility = View.VISIBLE
                                        if (type == 0) {
                                            addPhoto(photoPath, newPathInNote)
                                        }else if (type == 1){
                                            addVideo(photoPath, newPathInNote)
                                        }
                                    }else {
                                        returnCanceled()
                                    }
                                }
                            }

                            override fun onError(e: Throwable) {
                                e.printStackTrace()
                                noSupport()
                            }

                            override fun onComplete() {}

                        })
                    }
                    override fun onCancel() {

                    }
                })
            }else {
                emptyNote()
            }
        }

        binding.takePhoto.setOnClickListener {
            if (::note.isInitialized) {
                requestPermission(object : PermissionRequestCallBack {
                    override fun onAllGranted() {
                        val intent = Intent()
                        intent.action = MediaStore.ACTION_IMAGE_CAPTURE
                        intent.putExtra("com.xtc.camera.LEFT_BUTTON_TEXT", "关闭")
                        intent.putExtra("com.xtc.camera.RIGHT_BUTTON_TEXT", "选择")
                        RxActivityResult.on(this@NoteAddMedia).startIntent(intent).subscribe(object : Observer<Result<NoteAddMedia>> {
                            override fun onSubscribe(d: Disposable) {}

                            override fun onError(e: Throwable) {
                                e.printStackTrace()
                                noSupport()
                            }

                            override fun onComplete() {}

                            override fun onNext(data: Result<NoteAddMedia>) {
                                if (data.resultCode() == Activity.RESULT_OK) {
                                    val bundle: Bundle? = data.data().extras
                                    if (bundle != null) {
                                        val photoPath: String = bundle.getString(MediaStore.EXTRA_OUTPUT, null)
                                        val saveDir = getDirToSave(0)
                                        val newPathInNote = note.data["noteFolder"]!! + saveDir + fileNameNow + FileUtils.getFileSuffix(photoPath)
                                        binding.lodingView.visibility = View.VISIBLE
                                        addPhoto(photoPath, newPathInNote)
                                    }
                                }
                            }
                        })
                    }

                    override fun onCancel() {

                    }
                })
            }else {
                emptyNote()
            }
        }

        binding.takeVideo.setOnClickListener {
            if (::note.isInitialized) {
                requestPermission(object : PermissionRequestCallBack {
                    override fun onAllGranted() {
                        val intent: Intent = Intent()
                        intent.action = MediaStore.ACTION_VIDEO_CAPTURE
                        intent.putExtra("com.xtc.camera.LEFT_BUTTON_TEXT", "关闭")
                        intent.putExtra("com.xtc.camera.RIGHT_BUTTON_TEXT", "选择")
                        RxActivityResult.on(this@NoteAddMedia).startIntent(intent).subscribe(object : Observer<Result<NoteAddMedia>> {
                            override fun onSubscribe(d: Disposable) {}

                            override fun onError(e: Throwable) {
                                e.printStackTrace()
                                noSupport()
                            }

                            override fun onComplete() {}

                            override fun onNext(data: Result<NoteAddMedia>) {
                                if (data.resultCode() == Activity.RESULT_OK) {
                                    val bundle: Bundle? = data.data().extras
                                    if (bundle != null) {
                                        val photoPath: String = bundle.getString(MediaStore.EXTRA_OUTPUT, null)
                                        val saveDir = getDirToSave(1)
                                        val newPathInNote = note.data["noteFolder"]!! + saveDir + fileNameNow + FileUtils.getFileSuffix(photoPath)
                                        binding.lodingView.visibility = View.VISIBLE
                                        addVideo(photoPath, newPathInNote)
                                    }
                                }
                            }
                        })
                    }

                    override fun onCancel() {

                    }

                })
            }else {
                emptyNote()
            }
        }

        binding.makeRecord.setOnClickListener {
            if (::note.isInitialized) {
                val intent = Intent(this, NoteRecording::class.java)
                intent.putExtra("noteId", note.id)
                RxActivityResult.on(this).startIntent(intent).subscribe {
                    finish()
                }
            }else {
                emptyNote()
            }
        }
    }

    private fun addPhoto(photoPath: String, newPathInNote: String) {
        thread {
            if (!FileUtils.copyFile(photoPath, newPathInNote)) {
                runOnUiThread {
                    makeToast("Copy failure")
                }
                returnCanceled()
                return@thread
            }

            appViewModel.changeNoteDataFromId(note.id, object : ChangeNoteDataCallBack {
                override fun doChange(it: Note) {
                    it.data["imagePaths"] = it.data["imagePaths"]!!.toArray<String>().let { arr ->
                        arr.add(photoPath)
                        arr.toGson()
                    }
                }

                override fun onChangeSuccessful() {
                    returnOk()
                }

                override fun onChangeFailure(e: Exception) {
                    e.printStackTrace()
                    addFailure()
                }

            })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addVideo(photoPath: String, newPathInNote: String) {
        thread {
            try {
                compressVideo(photoPath, newPathInNote) {
                    runOnUiThread {
                        binding.loadText.text = "Loading ${it * 100}%"
                    }
                    if (it == 1f) {
                        returnOk()
                    }
                }
            }catch (e: Exception) {
                runOnUiThread {
                    makeToast("Compress failure")
                }
                e.printStackTrace()
                returnCanceled()
            }

            appViewModel.changeNoteDataFromId(note.id, object : ChangeNoteDataCallBack {
                override fun doChange(it: Note) {
                    it.data["videoPaths"] = it.data["videoPaths"]!!.toArray<String>().let { arr ->
                        arr.add(photoPath)
                        arr.toGson()
                    }
                }

                override fun onChangeSuccessful() {
                    returnOk()
                }

                override fun onChangeFailure(e: Exception) {
                    e.printStackTrace()
                    addFailure()
                }

            })
        }
    }

    private fun compressVideo(filepath: String, desPath: String, mListener: VideoProgressListener){
        FileUtils.makeRootDirectory(File(desPath).parent.ifEmpty { "" })
        VideoProcessor.processor(this)
            .input(filepath)
            .output(desPath)
            .bitrate(250*250)  //输出视频比特率
            .frameRate(30)   //帧率
            .iFrameInterval(20)  //关键帧距，为0时可输出全关键帧视频（部分机器上需为-1）
            .progressListener(mListener)
            .process()
    }

    private fun returnOk() {
        val intent = Intent()
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun returnCanceled() {
        val intent = Intent()
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    private fun noSupport() {
        runOnUiThread {
            makeToast("Not supported on your device")
        }
        returnCanceled()
    }

    private fun emptyNote() {
        runOnUiThread {
            makeToast("Empty note")
        }
        returnCanceled()
    }

    private fun addFailure() {
        runOnUiThread {
            makeToast("Add failure")
        }
        returnCanceled()
    }

    private fun requestPermission(func: PermissionRequestCallBack) {
        PermissionX.init(this)
            .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            .request { allGranted, _, _ ->
                if (allGranted) {
                    Log.d("NoteAddMedia", "All permissions Granted")
                    func.onAllGranted()
                }else {
                    func.onCancel()
                }
            }
    }

    interface PermissionRequestCallBack {
        fun onAllGranted()
        fun onCancel()
    }
}