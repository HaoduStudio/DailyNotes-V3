package com.haoduyoudu.DailyAccounts.view.activities

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.animation.addListener
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.drawerlayout.widget.DrawerLayout
import com.google.gson.Gson
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.R
import com.haoduyoudu.DailyAccounts.databinding.ActivityFreeMakeNoteBinding
import com.haoduyoudu.DailyAccounts.helper.makeToast
import com.haoduyoudu.DailyAccounts.helper.toGson
import com.haoduyoudu.DailyAccounts.model.listener.ChangeNoteDataCallBack
import com.haoduyoudu.DailyAccounts.model.models.Note
import com.haoduyoudu.DailyAccounts.utils.BitmapFilletUtils
import com.haoduyoudu.DailyAccounts.utils.BitmapUtils
import com.haoduyoudu.DailyAccounts.utils.DisplayUtil
import com.haoduyoudu.DailyAccounts.utils.FileUtils
import com.haoduyoudu.DailyAccounts.view.activities.base.BaseActivity
import com.haoduyoudu.DailyAccounts.view.activities.base.DialogActivity
import com.haoduyoudu.DailyAccounts.view.activities.base.NoRightSlideActivity
import com.haoduyoudu.DailyAccounts.view.customView.freeLayout.ObjectSaveModel
import com.haoduyoudu.DailyAccounts.view.customView.freeLayout.objects.BitmapObject
import com.haoduyoudu.DailyAccounts.view.customView.freeLayout.objects.TextObject
import com.haoduyoudu.DailyAccounts.viewModel.viewModels.GlobalViewModel
import com.permissionx.guolindev.PermissionX
import com.xtc.shareapi.share.communication.SendMessageToXTC
import com.xtc.shareapi.share.manager.ShareMessageManager
import com.xtc.shareapi.share.shareobject.XTCImageObject
import com.xtc.shareapi.share.shareobject.XTCShareMessage
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import rx_activity_result2.Result
import rx_activity_result2.RxActivityResult
import kotlin.concurrent.thread
import kotlin.math.abs

class FreeMakeNote: DialogActivity(noShot = true, canDis = true) {
    private val binding by lazy { ActivityFreeMakeNoteBinding.inflate(layoutInflater) }
    private val appViewModel by lazy { BaseApplication.viewModel as GlobalViewModel }
    private lateinit var note: Note
    private var isSupportSwipeBack = true
    private var _editMode = false
    private var editMode: Boolean
    set(value) {
        _editMode = value
        binding.freeLayout.editMode = value
        binding.addPaper.visibility = if (value) View.VISIBLE else View.GONE
        binding.removePaper.visibility = if (value) View.VISIBLE else View.GONE
        binding.showRight.visibility = if (value) View.VISIBLE else View.GONE
        isSupportSwipeBack = true
    }
    get() = _editMode
    private var backgroundColorIdNow = 12

    @SuppressLint("SetTextI18n", "CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(
            if (intent.getBooleanExtra("editMode", false)) {
                R.style.DialogActivityTheme
            }else {
                R.style.DialogActivityTheme2
            }
        )
        setContentView(binding.root)

        binding.drawerLayout.setDrawerLockMode(
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        )

        val noteId = intent.getLongExtra("noteId", -1L)
        editMode = intent.getBooleanExtra("editMode", false)
        val shareMode = intent.getBooleanExtra("share", false)
        if (noteId == -1L) {
            makeToast("Empty id")
            finish()
            return
        }

        appViewModel.getNoteFromIdLiveData(noteId).observe(this) {
            try {
                val isFirstLoad = !::note.isInitialized
                if (isFirstLoad) {
                    note = it
                    loadFreeObj()
                    binding.freeLayout.addPaper(it.data["pageSize"]!!.toInt())
                    backgroundColorIdNow = it.data["backgroundColor"]!!.toInt()
                    binding.backgroundColorRoot.setBackgroundColor(resources.getColor(BaseApplication.idToTextColor[backgroundColorIdNow]!!))
                }else {
                    note = it
                }

                if (shareMode && isFirstLoad) {
                    shotToShare()
                }
            }catch (e: Exception) {
                e.printStackTrace()
                makeToast("加载失败")
            }
        }

        binding.backToEdit.setOnClickListener {
            finish()
        }

        binding.complete.setOnClickListener {
            try {
                saveFreeObj()
                thread {
                    appViewModel.changeNoteDataFromId(noteId, object : ChangeNoteDataCallBack {
                        override fun doChange(it: Note) {
                            it.data["pageSize"] = binding.freeLayout.getPaperSize().toString()
                            it.data["backgroundColor"] = backgroundColorIdNow.toString()
                        }

                        override fun onChangeSuccessful() {
//                            editMode = false
//                            binding.drawerLayout.closeDrawer(GravityCompat.END)
//                            finish()
//                            startActivity(intent.apply {
//                                putExtra("noteId", noteId)
//                                putExtra("editMode", false)
//                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                            })
                            intent.putExtra("editMode", false)
                            recreate()
                        }

                        override fun onChangeFailure(e: Exception) {
                            e.printStackTrace()
                            makeToast("保存失败")
                        }

                    })
                }
            }catch (e: Exception) {
                e.printStackTrace()
                makeToast("保存失败")
            }
        }

        binding.freeLayout.setOnObjFocusListener { freeObject, isFocus ->
            binding.objPopView.visibility = if (isFocus) View.VISIBLE else View.GONE
        }

        binding.objSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    try {
                        val focusObj = binding.freeLayout.getFocusObj()
                        if (focusObj != null) {
                            val imageTemp = focusObj.srcBitmap.width
                            val values = FloatArray(9)
                            focusObj.mMatrix.getValues(values)
                            val nowWidth = imageTemp * values[0]
                            val scaleValue = (progress.toFloat() + 50) / nowWidth
                            focusObj.scale(scaleValue, scaleValue)
                            binding.freeLayout.invalidate()
                        }
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.objAngleLeft.setOnClickListener {
            try{
                val focusObj = binding.freeLayout.getFocusObj()
                focusObj?.rotate(-10f)
                binding.freeLayout.invalidate()
            }catch (_:Exception){}
        }

        binding.objAngleRight.setOnClickListener {
            try{
                val focusObj = binding.freeLayout.getFocusObj()
                focusObj?.rotate(10f)
                binding.freeLayout.invalidate()
            }catch (_:Exception){}
        }

        binding.delObj.setOnClickListener {
            val focus = binding.freeLayout.getFocusObj()
            focus?.let {
                binding.freeLayout.deleteFreeObj(it)
            }
            binding.objPopView.visibility = View.GONE
        }

        binding.showRight.setOnClickListener {
            onRightViewPop()
        }

        binding.addObj.setOnClickListener {
            binding.addSomethingRoot.visibility = View.VISIBLE
            binding.drawerLayout.closeDrawer(GravityCompat.END)
        }

        binding.addBack.setOnClickListener {
            binding.addSomethingRoot.visibility = View.GONE
        }

        binding.addText.setOnClickListener {
            binding.addSomethingRoot.visibility = View.GONE
            binding.addTextAction.visibility = View.VISIBLE
        }

        binding.addImage.setOnClickListener {
            requestPermission(object : PermissionRequestCallBack {
                override fun onAllGranted() {
                    val intent = Intent()
                    intent.action = Intent.ACTION_GET_CONTENT
                    intent.type = "image/*"
                    intent.putExtra("com.xtc.camera.LEFT_BUTTON_TEXT", "关闭")
                    intent.putExtra("com.xtc.camera.RIGHT_BUTTON_TEXT", "选择")
                    RxActivityResult.on(this@FreeMakeNote).startIntent(intent).subscribe(object :
                        Observer<Result<FreeMakeNote>> {
                        override fun onSubscribe(d: Disposable) {}
                        override fun onNext(data: Result<FreeMakeNote>) {
                            if (data.resultCode() == Activity.RESULT_OK) {
                                val bundle: Bundle? = data.data().extras
                                if (bundle != null) {
                                    val photoPath: String = bundle.getString(MediaStore.EXTRA_OUTPUT, null)
                                    binding.freeLayout.addFreeObj(
                                        BitmapObject(
                                            BitmapUtils.getImageFromPath(photoPath)
                                        )
                                    )
                                    binding.addSomethingRoot.visibility = View.GONE
                                }
                            }
                        }
                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                            makeToast("当前机型不支持")
                        }

                        override fun onComplete() {}
                    })
                }
                override fun onCancel() {

                }
            })
        }

        binding.textStyleReturn.setOnClickListener {
            try {
                val lastSel = binding.addTextEdit.selectionStart
                if (binding.addTextEdit.length() != lastSel) binding.addTextEdit.setText(
                    binding.addTextEdit.text.subSequence(0, lastSel).toString()
                            + "\n"
                            + binding.addTextEdit.text.subSequence(lastSel, binding.addTextEdit.length()).toString()
                )else binding.addTextEdit.setText(binding.addTextEdit.text.toString() + "\n")
                binding.addTextEdit.setSelection(lastSel+1)
            }catch (e: Exception) {
                e.printStackTrace()
            }
        }

        var isBold = false
        var isItalic = false
        var isUnderLine = false
        var isStrikethrough = false

        binding.textStyleBold.setOnClickListener {
            isBold = !isBold
            binding.addTextEdit.paint.isFakeBoldText = isBold
            binding.addTextEdit.invalidate()
        }

        binding.textStyleItalic.setOnClickListener {
            isItalic = !isItalic
            binding.addTextEdit.paint.textSkewX = if (isItalic) -0.2f else 0f
            binding.addTextEdit.invalidate()
        }

        binding.textStyleUnderline.setOnClickListener {
            isUnderLine = !isUnderLine
            binding.addTextEdit.paint.isUnderlineText = isUnderLine
            binding.addTextEdit.invalidate()
        }

        binding.textStyleStrikethrough.setOnClickListener {
            isStrikethrough = !isStrikethrough
            binding.addTextEdit.paint.isStrikeThruText = isStrikethrough
            binding.addTextEdit.invalidate()
        }

        binding.textStyleChangeColor.setOnClickListener {
            binding.colorsBoard.visibility = View.VISIBLE
            val allChild = getAllChildViews(binding.colorsBoard)
            for (i in 1..allChild.size) {
                allChild[i - 1].setOnClickListener {
                    val mColor = ContextCompat.getColor(this, BaseApplication.idToTextColor[i]!!)
                    binding.addTextEdit.setTextColor(mColor)
                    binding.addTextEdit.invalidate()
                    binding.addTextEdit.paint.color = mColor
                    binding.colorsBoard.visibility = View.GONE
                }
            }
        }

        binding.completeAddText.setOnClickListener {
            if (binding.addTextEdit.text.isNotEmpty()) {
                binding.addTextAction.visibility = View.GONE
                Log.d("FreeMakeNote", "Text = ${binding.addTextEdit.text}")
                binding.freeLayout.addFreeObj(
                    TextObject(binding.addTextEdit.text.toString(), binding.addTextEdit.paint)
                )
                binding.addTextEdit.paint.isFakeBoldText = false
                binding.addTextEdit.paint.textSkewX = 0f
                binding.addTextEdit.paint.isUnderlineText = false
                binding.addTextEdit.paint.isStrikeThruText = false
                binding.addTextEdit.paint.color = resources.getColor(R.color.black)
                binding.addTextEdit.setText("")
            }else {
                makeToast("文字不能为空")
            }
        }

        binding.cancelAddText.setOnClickListener {
            binding.addTextAction.visibility = View.GONE
        }

        binding.addSticker.setOnClickListener {
            RxActivityResult.on(this).startIntent(Intent(this, NoteStickerChooser::class.java)).subscribe {
                if(it.resultCode() == RESULT_OK){
                    try{
                        val urlBitmap = it.data().getStringExtra("bitmapPath") ?: ""
                        val bitmap = if (urlBitmap.isNotEmpty())  {
                            BitmapUtils.getImageFromPath(urlBitmap)
                        }else {
                            BitmapUtils.getImageFromAssetsFile(it.data().getStringExtra("stickerPath")!!)
                        }
                        val sticker = BitmapObject(bitmap)
                        binding.freeLayout.addFreeObj(sticker)
                        binding.addSomethingRoot.visibility = View.GONE
                    }catch (e:Exception){
                        makeToast("添加失败")
                        e.printStackTrace()
                    }
                }
            }
        }

        binding.addPaper.setOnClickListener {
            binding.freeLayout.addPaper()
        }

        binding.removePaper.setOnClickListener {
            binding.freeLayout.deletePaper()
        }

        binding.addBackground.setOnClickListener {
            binding.colorsBoard.visibility = View.VISIBLE
            val allChild = getAllChildViews(binding.colorsBoard)
            for (i in 1..allChild.size) {
                allChild[i - 1].setOnClickListener {
                    val mColor = ContextCompat.getColor(this, BaseApplication.idToTextColor[i]!!)
                    binding.backgroundColorRoot.setBackgroundColor(mColor)
                    binding.colorsBoard.visibility = View.GONE
                    binding.addSomethingRoot.visibility = View.GONE
                    backgroundColorIdNow = i
                }
            }
        }
    }

    private fun getAllChildViews(view: View): List<View> {
        val allChildViews: MutableList<View> = ArrayList()
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val viewChild = view.getChildAt(i) as ViewGroup
                allChildViews.add(viewChild.getChildAt(0))
                allChildViews.add(viewChild.getChildAt(1))
                allChildViews.add(viewChild.getChildAt(2))
            }
        }
        return allChildViews
    }

    private fun saveFreeObj() {
        if (!::note.isInitialized) throw RuntimeException("Note is empty")
        val dirPath = note.data["noteFolder"]!!
        val fileName = "free_obj.json"
        val allObj = binding.freeLayout.getAllObj()
        val objSaveList = ArrayList<Pair<String, FloatArray>>()
        allObj.forEach { obj ->
            val fl = FloatArray(10)
            obj.mMatrix.getValues(fl)
            val mp = Pair(
                BitmapUtils.bitmapToString(obj.srcBitmap),
                fl
            )
            objSaveList.add(mp)
        }
        val saveModel = ObjectSaveModel(objSaveList)
        val stringCen = saveModel.toGson()
        FileUtils.deleteFile(dirPath + fileName)
        FileUtils.writeTxtToFile(stringCen, dirPath, fileName)
    }

    private fun loadFreeObj() {
        if (!::note.isInitialized) throw RuntimeException("Note is empty")
        val dirPath = note.data["noteFolder"]!!
        val fileName = "free_obj.json"
        val emptyNote = fun() {
            if (!editMode) {
                makeToast("这篇手帐是空的")
            }
        }
        if (FileUtils.exists(dirPath + fileName)) {
            binding.freeLayout.clear()
            val stringCen = FileUtils.readTxtFile(dirPath + fileName)
            val model = Gson().fromJson(stringCen, ObjectSaveModel::class.java)
            model.objList.forEach { obj ->
                val bitmap = BitmapUtils.stringToBitmap(obj.first)
                val matrix = obj.second
                val newObj = BitmapObject(bitmap)
                newObj.mMatrix.setValues(matrix)
                newObj.updatePoints()
                binding.freeLayout.addFreeObj(newObj)
            }
            if (model.objList.isEmpty()) {
                emptyNote()
            }
        }else {
            emptyNote()
        }
    }

    private fun requestPermission(func: PermissionRequestCallBack) {
        PermissionX.init(this)
            .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            .request { allGranted, _, _ ->
                if (allGranted) {
                    Log.d("FreeMakeNote", "All permissions Granted")
                    func.onAllGranted()
                }else {
                    func.onCancel()
                }
            }
    }

    private fun onRightViewPop() {
        try{
            val bitmap: Bitmap = BitmapUtils.rsBlur(BitmapUtils.viewConversionBitmap(binding.drawerLayout), 8)
            val bitmap1 = Bitmap.createBitmap(bitmap, 320 - DisplayUtil.dip2px(55f), 0, DisplayUtil.dip2px(55f), 360)
            binding.popbk.setImageBitmap(
                BitmapFilletUtils.fillet(bitmap1,
                    DisplayUtil.dip2px(15f), BitmapFilletUtils.CORNER_LEFT))
            bitmap.recycle()
            bitmap1.recycle()

            binding.drawerLayout.openDrawer(GravityCompat.END)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun shotToShare() {
        binding.viewToShot.postDelayed({
            try{
                val shareImage = BitmapUtils.viewConversionBitmap(binding.viewToShot)
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

    interface PermissionRequestCallBack {
        fun onAllGranted()
        fun onCancel()
    }
}