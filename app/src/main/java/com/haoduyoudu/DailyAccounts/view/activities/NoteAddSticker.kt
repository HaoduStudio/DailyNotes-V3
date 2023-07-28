package com.haoduyoudu.DailyAccounts.view.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.databinding.ActivityNoteAddStickerBinding
import com.haoduyoudu.DailyAccounts.helper.makeToast
import com.haoduyoudu.DailyAccounts.model.models.Note
import com.haoduyoudu.DailyAccounts.utils.*
import com.haoduyoudu.DailyAccounts.view.customView.sticker.Sticker
import com.haoduyoudu.DailyAccounts.view.activities.base.NoRightSlideActivity
import com.haoduyoudu.DailyAccounts.viewModel.viewModels.GlobalViewModel
import rx_activity_result2.RxActivityResult
import java.io.*
import kotlin.math.ceil

class NoteAddSticker : NoRightSlideActivity() {

    private val binding by lazy { ActivityNoteAddStickerBinding.inflate(layoutInflater) }
    private val appViewModel by lazy { BaseApplication.viewModel as GlobalViewModel }
    private lateinit var noteData: Note

    private var _isEditingSticker = true
    private var isEditingSticker: Boolean
        get() {
            return _isEditingSticker
        }
        set(yes) {
            binding.mDrawerLayout.closeDrawer(GravityCompat.END)
            if(yes){
                loadSticker()
                binding.mStickerLayout.setCanEdit(true)
            }else{
                saveSticker()
                binding.mStickerLayout.cleanAllFocus()
                binding.mStickerLayout.setCanEdit(false)
                binding.skPopview.visibility = View.GONE
            }
            _isEditingSticker = yes
        }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val noteId = intent.getLongExtra("noteId", -1L)
        if (noteId == -1L) {
            makeToast("Empty id")
            setResult(RESULT_CANCELED, Intent())
            finish()
        }else {
            appViewModel.getNoteFromIdLiveData(noteId).observe(this) {
                if (!::noteData.isInitialized) {
                    noteData = it
                    loadSticker()
                }else {
                    noteData = it
                }
            }
        }

        binding.backtoedit.setOnClickListener {
            try {
                binding.mDrawerLayout.closeDrawer(GravityCompat.END)
                setResult(RESULT_CANCELED, Intent())
                finish()
            }catch (e:Exception){
                e.printStackTrace()
                makeToast("存储错误")
            }
        }

        binding.showright.setOnClickListener {
            try {
                onPopWindowsShow()
                binding.showright.visibility = View.GONE
            }catch (e:Exception){
                e.printStackTrace()
                makeToast("弹出错误")
            }
        }

        binding.editsk.setOnClickListener {
            try {
                binding.mDrawerLayout.closeDrawer(GravityCompat.END)
                if(!isEditingSticker) {
                    isEditingSticker = true
                }
                RxActivityResult.on(this).startIntent(Intent(this, NoteStickerChooser::class.java)).subscribe {
                    if(it.resultCode() == RESULT_OK){
                        try{
                            val urlBitmap = it.data().getStringExtra("bitmapPath") ?: ""
                            val bitmap = if (urlBitmap.isNotEmpty())  {
                                BitmapUtils.getImageFromPath(urlBitmap)
                            }else {
                                BitmapUtils.getImageFromAssetsFile(it.data().getStringExtra("stickerPath")!!)
                            }
                            val sticker = Sticker(bitmap)
                            sticker.translate(0f, binding.mScrollView.scrollY.toFloat())
                            binding.mStickerLayout.addSticker(sticker)
                            binding.mStickerLayout.focusSticker = sticker
                        }catch (e:Exception){
                            makeToast("添加失败")
                            e.printStackTrace()
                        }
                    }else if (binding.mStickerLayout.returnAllSticker().size == 0) {
                        isEditingSticker = false
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
                makeToast("编辑错误")
            }
        }

        binding.ok.setOnClickListener {
            try {
                binding.mDrawerLayout.closeDrawer(GravityCompat.END)
                setResult(RESULT_OK, Intent())
                finish()
            }catch (e:Exception){
                e.printStackTrace()
                makeToast("出现致命错误")
            }
        }

        binding.mDrawerLayout.setDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(arg0: Int) {}

            override fun onDrawerSlide(arg0: View, arg1: Float) {}

            override fun onDrawerOpened(arg0: View) {
                Log.e("mDrawerLayout", "open")
                try {
                    val vibrator = BaseApplication.context.getSystemService(VIBRATOR_SERVICE) as Vibrator
                    vibrator.vibrate(34)
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

            override fun onDrawerClosed(arg0: View) {
                Log.e("mDrawerLayout", "colse")
                try {
                    binding.showright.visibility= View.VISIBLE
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        })

        if (FileUtils.exists(File(cacheDir.absolutePath, "tempNoteShot.jpg"))){
            try {
                val imgPath = File(cacheDir,"tempNoteShot.jpg").absolutePath
                val lp = binding.images.layoutParams
                val options = BitmapFactory.Options()
                BitmapFactory.decodeFile(imgPath,options)
                lp.height = options.outHeight
                binding.images.layoutParams = lp
                val onceImgHeight = 4000
                val `is` = FileInputStream(imgPath)
                val bmp = BitmapFactory.decodeStream(`is`)
                for (i in 0..(ceil(options.outHeight / onceImgHeight.toDouble()) - 1).toInt()){
                    val img = ImageView(this)
                    img.scaleType = ImageView.ScaleType.FIT_XY
                    val startHeight = if(i==0) 0 else i * onceImgHeight + 1;
                    val height = if((startHeight+onceImgHeight) > options.outHeight) {
                        if (options.outHeight % onceImgHeight == 1) {
                            options.outHeight - startHeight + 1
                        }else{
                            options.outHeight - startHeight
                        }
                    }else{
                        onceImgHeight
                    }
                    val lp2 = LinearLayout.LayoutParams(options.outWidth, height)
                    img.layoutParams = lp2
                    img.setImageBitmap(Bitmap.createBitmap(bmp,0, startHeight, options.outWidth, height))
                    binding.images.addView(img)
                }
            }catch (e: Exception){
                e.printStackTrace()
                makeToast("Load background err")
                setResult(RESULT_CANCELED, Intent())
                finish()
            }
        }else{
            makeToast("背景损坏")
            setResult(RESULT_CANCELED, Intent())
            finish()
        }

        binding.delsk.setOnClickListener {
            try{
                onClosePopEdit()
                binding.mStickerLayout.removeSticker(binding.mStickerLayout.focusSticker)
                if(binding.mStickerLayout.returnAllSticker().size == 1){
                    isEditingSticker = false
                    saveSticker()
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        binding.mStickerLayout.setOnEditPopChangeListener { onFocus, sticker ->
            if (onFocus) {
                onPopEdit()
            }else {
                onClosePopEdit()
            }
        }

        binding.skAngleLeft.setOnClickListener {
            try{
                val focusSk: Sticker = binding.mStickerLayout.focusSticker
                binding.mStickerLayout.rotateSticker(focusSk,-10f)
            }catch (_:Exception){}
        }
        binding.skAngleRight.setOnClickListener {
            try{
                val focusSk:Sticker = binding.mStickerLayout.focusSticker
                binding.mStickerLayout.rotateSticker(focusSk,10f)
            }catch (_:Exception){}
        }

        binding.skSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    try {
                        val focusSk:Sticker = binding.mStickerLayout.focusSticker

                        val imageTemp = focusSk.bitmap.width
                        val values = FloatArray(9)
                        focusSk.matrix.getValues(values)
                        val nowWidth = imageTemp*values[0]
                        val scaleValue = (progress.toFloat()+50)/nowWidth
                        binding.mStickerLayout.scaleSticker(focusSk,scaleValue,scaleValue)
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun onPopEdit(){
        if (!binding.skPopview.isVisible) {
            try{
                val bitmap: Bitmap = BitmapUtils.rsBlur(BitmapUtils.viewConversionBitmap(binding.mDrawerLayout),8)
                val bitmap1 = Bitmap.createBitmap(bitmap, 0, 360 - DisplayUtil.dip2px(75f), 320, DisplayUtil.dip2px(75f))
                binding.skPopview.background = BitmapDrawable(resources, BitmapFilletUtils.fillet(bitmap1,DisplayUtil.dip2px(8f), BitmapFilletUtils.CORNER_TOP))
                bitmap.recycle()
                bitmap1.recycle()
                binding.skPopview.visibility = View.VISIBLE
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        try {
            val focusSk = binding.mStickerLayout.focusSticker
            val imageTemp = focusSk.bitmap
            val values = FloatArray(10)
            focusSk.matrix.getValues(values)
            val nowWidth = imageTemp.width * values[0]

            binding.skSize.progress = nowWidth.toInt() - 50
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun onClosePopEdit(){
        try {
            binding.skPopview.visibility = View.GONE
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun loadSticker(){
        if (::noteData.isInitialized) {
            try {
                NoteUtils.loadSticker(binding.mStickerLayout, noteData)
            }catch (e: Exception) {
                e.printStackTrace()
                makeToast("Load sk failure")
            }
        }else {
            makeToast("Empty note")
            setResult(RESULT_CANCELED, Intent())
            finish()
        }
    }

    private fun saveSticker(){
        Log.i("info","savesk")
        if (::noteData.isInitialized) {
            try {
                NoteUtils.saveSticker(binding.mStickerLayout, noteData)
            }catch (e: Exception) {
                e.printStackTrace()
                makeToast("Save sk failure")
            }
        }else {
            makeToast("Empty note")
            setResult(RESULT_CANCELED, Intent())
            finish()
        }
    }

    private fun onPopWindowsShow(){
        try{
            val bitmap: Bitmap = BitmapUtils.rsBlur(BitmapUtils.viewConversionBitmap(binding.mDrawerLayout), 8)
            val bitmap1 = Bitmap.createBitmap(bitmap, 320-DisplayUtil.dip2px(55f), 0, DisplayUtil.dip2px(55f), 360)
            binding.popbk.setImageBitmap(BitmapFilletUtils.fillet(bitmap1,DisplayUtil.dip2px(15f), BitmapFilletUtils.CORNER_LEFT))
            bitmap.recycle()
            bitmap1.recycle()

            binding.mDrawerLayout.openDrawer(GravityCompat.END)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}