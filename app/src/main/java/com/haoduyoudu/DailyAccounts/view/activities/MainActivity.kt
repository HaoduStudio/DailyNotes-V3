package com.haoduyoudu.DailyAccounts.view.activities

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.backup.BackupManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.R
import com.haoduyoudu.DailyAccounts.databinding.ActivityMainBinding
import com.haoduyoudu.DailyAccounts.helper.makeToast
import com.haoduyoudu.DailyAccounts.helper.toGson
import com.haoduyoudu.DailyAccounts.model.database.NOTE_TYPE_V1
import com.haoduyoudu.DailyAccounts.model.database.NOTE_TYPE_V2
import com.haoduyoudu.DailyAccounts.model.models.Note
import com.haoduyoudu.DailyAccounts.utils.BitmapUtils
import com.haoduyoudu.DailyAccounts.utils.DateUtils
import com.haoduyoudu.DailyAccounts.utils.FileUtils
import com.haoduyoudu.DailyAccounts.utils.NoteUtils
import com.haoduyoudu.DailyAccounts.view.activities.base.BaseActivity
import com.haoduyoudu.DailyAccounts.view.adapters.BaseNoteAdapter
import com.haoduyoudu.DailyAccounts.view.customView.sticker.StickerSaveModel
import com.haoduyoudu.DailyAccounts.viewModel.viewModels.GlobalViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import java.io.FileInputStream
import java.io.ObjectInputStream


class MainActivity : BaseActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val appViewModel by lazy { BaseApplication.viewModel as GlobalViewModel }
    private lateinit var notesAdapter: BaseNoteAdapter
    private val mNoteList = ArrayList<BaseNoteAdapter.NoteItem>()
    private lateinit var mWakeLock: PowerManager.WakeLock
    private var _allDone = false
    private var allDone: Boolean
        get() = _allDone
        set(value) {
            _allDone = value
            if (value) {
                try {
                    mWakeLock.release()
                }catch (_: Exception) { }
            }else {
                try {
                    val powerManager = getSystemService(POWER_SERVICE) as PowerManager
                    mWakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "DailyNote:WakeWoke")
                    mWakeLock.acquire(2*60*1000L)
                }catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

    private var backgroundPath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        allDone = false

        lifecycleScope.launch {
            beforeStartupCheck().flowOn(Dispatchers.IO)
                .onCompletion {
                    startMainPage()
                }.flowOn(Dispatchers.Main)
                .collect {
                    withContext(Dispatchers.Main) {
                        binding.startTips.text = it
                        if (it == "Done") {
                            makeToast("更新完成")
                            startActivity(
                                Intent(
                                this@MainActivity, GuideActivity::class.java
                            )
                            )
                            startActivity(Intent(
                                this@MainActivity, CongratulationsActivity::class.java
                            ))
                        }
                    }
                }
        }

        binding.menu.setOnClickListener {
            binding.mDrawerLayout.openDrawer(GravityCompat.END)
        }

        binding.write.setOnClickListener {
            val mIntent = Intent(this, NoteChangeMood::class.java)
            mIntent.putExtra("firstWrite", true)
            startActivity(mIntent)
            binding.mDrawerLayout.closeDrawer(GravityCompat.END)
        }

        binding.moodCalendar.setOnClickListener {
            if (mNoteList.isEmpty()) {
                makeToast("请至少写一篇手帐")
            }else {
                val mIntent = Intent(this, MoodCalendar::class.java)
                startActivity(mIntent)
            }
            binding.mDrawerLayout.closeDrawer(GravityCompat.END)
        }

        binding.personalize.setOnClickListener {
            if (mNoteList.isEmpty()) {
                makeToast("请至少写一篇手帐")
            }else {
                val mIntent = Intent(this, BackgroundChooser::class.java)
                startActivity(mIntent)
            }
            binding.mDrawerLayout.closeDrawer(GravityCompat.END)
        }

        binding.find.setOnClickListener {
            if (mNoteList.isEmpty()) {
                makeToast("请至少写一篇手帐")
            }else {
                val mIntent = Intent(this, SearchNote::class.java)
                startActivity(mIntent)
            }
            binding.mDrawerLayout.closeDrawer(GravityCompat.END)
        }

        binding.guide.setOnClickListener {
            val mIntent = Intent(this, GuideActivity::class.java)
            startActivity(mIntent)
            binding.mDrawerLayout.closeDrawer(GravityCompat.END)
        }

        binding.about.setOnClickListener {
            val mIntent = Intent(this, AboutSoftware::class.java)
            startActivity(mIntent)
            binding.mDrawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun startMainPage() {
        allDone = true

        binding.startTips.visibility = View.GONE
        binding.startBg.visibility = View.GONE

        appViewModel.notesList.observe(this) {
            mNoteList.clear()
            it.forEach { j -> mNoteList.add(BaseNoteAdapter.NoteItem(j)) }
            mNoteList.sortBy { it2 ->
                DateUtils.formatYYMMDD(it2.data.yy, it2.data.mm, it2.data.dd, DateUtils.FORMAT_YYYY_MM_DD)
            }
            mNoteList.reverse()

            if (binding.listView.adapter == null || !::notesAdapter.isInitialized) {
                Log.d("MainActivity", "设置转换器")
                initAdapter()

                appViewModel.appBackgroundPath.observe(this) { it2 ->
                    backgroundPath = it2
                    refreshBackground()
                }
            }else {
                notesAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun initAdapter() {
        notesAdapter = BaseNoteAdapter(this, mNoteList)
        binding.listView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            adapter = notesAdapter
        }

        notesAdapter.setOnItemClickListener { view, pos ->
            val targetNote = mNoteList[pos]
            if (targetNote.data.type == NOTE_TYPE_V1) {
                startActivity(Intent(this, NoteViewer::class.java).apply {
                    putExtra("noteId", targetNote.data.id)
                })
            }else if (targetNote.data.type == NOTE_TYPE_V2) {
                startActivity(Intent(this, FreeMakeNote::class.java).apply {
                    putExtra("noteId", targetNote.data.id)
                })
            }
        }

        notesAdapter.setOnItemLongClickListener { _, pos ->
            val targetNote = mNoteList[pos]
            val mIntent = Intent(this, NoteOption::class.java)
            mIntent.putExtra("noteId", targetNote.data.id)
            mIntent.putExtra("type", targetNote.data.type)
            startActivity(mIntent)
            null
        }

        notesAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                refreshBackground()
            }
        })
    }

    private suspend fun beforeStartupCheck() = flow {
        val sharedPreferences = getSharedPreferences(BaseApplication.APP_SHARED_PREFERENCES_NAME, 0)
        val load = sharedPreferences.getBoolean("transportSuccessful", false)
        if (!load) {
            emit("正在更新...\n请不要在更新期间关闭应用！")
            delay(1500)
            FileUtils.delete(BaseApplication.OLD_ASSETS_PATH)
            FileUtils.makeRootDirectory(BaseApplication.NOTES_PATH)
            val dirsName = NoteUtils.getOldNotesFileList()
            for (aNote in dirsName) {
                val targetDir = BaseApplication.OLD_DATA_PATH + aNote + '/'
                val newDirInNote = BaseApplication.NOTES_PATH + aNote + '/'
                try {
                    val date = DateUtils.getCalendarFromFormat(aNote, DateUtils.FORMAT_YYYY_MM_DD)
                    val mood = FileUtils.readTxtFile(targetDir + "mood.txt").split("$[%|!|%]$")
                    val moodText = when(mood.size) {
                        1 -> BaseApplication.code2MoodText_old[mood[0].toInt()]!!
                        else -> mood[1]
                    }

                    val recordList = ArrayList(FileUtils.getFilesList(targetDir + "record").map { newDirInNote + "record/" + it })
                    val imageList = ArrayList(FileUtils.getFilesList(targetDir + "image").map { newDirInNote + "image/" + it})
                    val videoList = ArrayList(FileUtils.getFilesList(targetDir + "video").map { newDirInNote + "video/" + it})

                    val bean = Note(
                        DateUtils.getYYYYFromCalendar(date),
                        DateUtils.getMMFromCalendar(date),
                        DateUtils.getDDFromCalendar(date),
                        Pair(mood[0].toInt(), moodText),
                        NOTE_TYPE_V1,
                        hashMapOf(
                            "template" to Pair(false, FileUtils.readTxtFile(targetDir + "template.data").ifEmpty { "1" }).toGson(),
                            "textColor" to FileUtils.readTxtFile(targetDir + "textcolor.data").ifEmpty { "1" },
                            "noteFolder" to newDirInNote,
                            "body" to FileUtils.readTxtFile(targetDir + "text.txt"),
                            "recordPaths" to recordList.toGson(),
                            "imagePaths" to imageList.toGson(),
                            "videoPaths" to videoList.toGson(),
                        )
                    )

                    appViewModel.addNote(bean)

                    if (FileUtils.exists(targetDir + "record")) {
                        FileUtils.copyFolder(targetDir + "record", newDirInNote + "record")
                    }

                    if (FileUtils.exists(targetDir + "image")) {
                        FileUtils.copyFolder(targetDir + "image", newDirInNote + "image")
                    }

                    if (FileUtils.exists(targetDir + "video")) {
                        FileUtils.copyFolder(targetDir + "video", newDirInNote + "video")
                    }

                    if (FileUtils.exists(targetDir + "sitcker.sk")) {
                        val path = targetDir + "sitcker.sk"
                        val fileIn = FileInputStream(path)
                        val fin = ObjectInputStream(fileIn)
                        val allPosData = (fin.readObject() as ArrayList<FloatArray>)
                        val allBitmapData = (fin.readObject() as ArrayList<ByteArray>)
                        fin.close()
                        fileIn.close()
                        val pairs = ArrayList<Pair<String, FloatArray>>()
                        for (i in 0 until allPosData.size) {
                            val bitmap = BitmapFactory.decodeByteArray(allBitmapData[i], 0, allBitmapData[i].size)
                            val bitmapString = BitmapUtils.bitmapToString(bitmap)
                            bitmap.recycle()
                            val mp = Pair(bitmapString, allPosData[i])
                            pairs.add(mp)
                        }
                        val finallyData = StickerSaveModel(pairs)
                        FileUtils.writeTxtToFile(finallyData.toGson(), newDirInNote, "sticker.json")
                    }

                    emit("Moved $aNote Successful")
                    FileUtils.delete(targetDir)
                }catch (e: Exception) {
                    e.printStackTrace()
                    emit("Moved $aNote Failure")
                }
            }
            sharedPreferences.edit().putBoolean("transportSuccessful", true).apply()

            delay(200)
            emit("Done")
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (allDone) super.dispatchTouchEvent(ev) else false
    }

    private fun refreshBackground() {
        if (backgroundPath.isNotEmpty() && mNoteList.isNotEmpty()) {
            Glide.with(this@MainActivity).load(backgroundPath).into(
                binding.backgroundImg
            )
        }else {
            binding.backgroundImg.setImageResource(R.color.black)
        }
    }
}