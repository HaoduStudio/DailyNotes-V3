package com.haoduyoudu.DailyAccounts.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.gson.Gson
import com.haoduyoudu.DailyAccounts.BaseApplication.Companion.ASSETS_MOOD_PATH
import com.haoduyoudu.DailyAccounts.BaseApplication.Companion.OLD_DATA_PATH
import com.haoduyoudu.DailyAccounts.helper.toGson
import com.haoduyoudu.DailyAccounts.model.models.Note
import com.haoduyoudu.DailyAccounts.view.customView.sticker.Sticker
import com.haoduyoudu.DailyAccounts.view.customView.sticker.StickerLayout
import com.haoduyoudu.DailyAccounts.view.customView.sticker.StickerSaveModel
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object NoteUtils {
    fun getMoodBitmapFromId(id: Int): Bitmap {
        return BitmapUtils.getImageFromAssetsFile("$ASSETS_MOOD_PATH$id.png")
    }

    fun getDayOfWeekFromNoteName(name: String): String {
        val mSDF = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        val mDate = mSDF.parse(name)!!
        return DateUtils.getDayOfWeek(mDate)
    }

    fun getOldNotesFileList(): MutableList<String> {
        val fileNames: MutableList<String> = mutableListOf()
        //在该目录下走一圈，得到文件目录树结构
        try {
            val fileTree: FileTreeWalk = File(OLD_DATA_PATH).walk()
            val rootName = File(OLD_DATA_PATH).name
            fileTree.maxDepth(1) //需遍历的目录层次为1，即无须检查子目录
                .filter { it.isDirectory && it.name.length == 8 && it.name != rootName} //只挑选文件，不处理文件夹
                //.filter { it.extension in listOf("m4a","mp3") }
                .forEach { fileNames.add(it.name) }//循环 处理符合条件的文件
        }catch (e: Exception) {
            Log.d("NoteUtils", "getOldNotesFileList fail")
            e.printStackTrace()
        }
        return fileNames
    }

    private fun getStickerFilePath(note: Note): String {
        return try {
            note.data["noteFolder"] + "sticker.json"
        }catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun loadSticker(view: StickerLayout, note: Note) {
        val path = getStickerFilePath(note)
        if (FileUtils.exists(path)){
//            val fileIn = FileInputStream(path)
//            val fin = ObjectInputStream(fileIn)
//            val allPosData = (fin.readObject() as ArrayList<FloatArray>)
//            val allBitmapData = (fin.readObject() as ArrayList<ByteArray>)
//            fin.close()
//            fileIn.close()
//            view.removeAllSticker()
//            for (i in 0 until allPosData.size) {
//                val bitmap = BitmapFactory.decodeByteArray(allBitmapData[i], 0, allBitmapData[i].size)
//                val sticker = Sticker(bitmap)
//                sticker.matrix.setValues(allPosData[i])
//                view.addSticker(sticker)
//            }
//            view.update()
            view.removeAllSticker()
            val stringCan = FileUtils.readTxtFile(path)
            Log.d("NoteUtils", "Read sticker data: $stringCan")
            val model = Gson().fromJson(stringCan, StickerSaveModel::class.java)
            model.stickerList.forEach { sticker ->
                val bitmap = BitmapUtils.stringToBitmap(sticker.first)
                val matrix = sticker.second
                val mSticker = Sticker(bitmap)
                mSticker.matrix.setValues(matrix)
                view.addSticker(mSticker)
            }
            view.update()
        }
    }

    fun saveSticker(view: StickerLayout, note: Note) {
        val path = getStickerFilePath(note)
        val allSticker = view.returnAllSticker()
//        val allPosData = ArrayList<FloatArray>()
//        val allBitmapData = ArrayList<ByteArray>()
        FileUtils.delete(path)
        if(allSticker.size != 0) {
//            for(i in allSticker){
//                val data = FloatArray(9)
//                i.matrix.getValues(data)
//                allPosData.add(data)
//
//                val mBitmap = i.bitmap
//                val bao = ByteArrayOutputStream()
//                mBitmap.compress(Bitmap.CompressFormat.PNG, 0, bao) //压缩位图
//                allBitmapData.add(bao.toByteArray())
//            }
//            val fileOut = FileOutputStream(path)
//            val out = ObjectOutputStream(fileOut)
//            out.writeObject(allPosData)
//            out.writeObject(allBitmapData)
//            out.close()
//            fileOut.close()
            val pairs = ArrayList<Pair<String, FloatArray>>()
            allSticker.forEach { sticker ->
                val bitmap = sticker.bitmap
                val bitmapString = BitmapUtils.bitmapToString(bitmap)
                val pd = FloatArray(10)
                sticker.matrix.getValues(pd)
                val mp = Pair(bitmapString, pd)
                pairs.add(mp)
            }
            val finallyData = StickerSaveModel(pairs)
            FileUtils.writeTxtToFile(finallyData.toGson(), note.data["noteFolder"]!!, File(path).name)
        }
    }
}