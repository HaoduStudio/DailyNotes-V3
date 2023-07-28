package com.haoduyoudu.DailyAccounts.view.activities

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.databinding.ActivityNoteChangeMoodBinding
import com.haoduyoudu.DailyAccounts.helper.makeToast
import com.haoduyoudu.DailyAccounts.helper.toGson
import com.haoduyoudu.DailyAccounts.model.database.NOTE_TYPE_V1
import com.haoduyoudu.DailyAccounts.model.database.NOTE_TYPE_V2
import com.haoduyoudu.DailyAccounts.model.listener.AddNoteCallBack
import com.haoduyoudu.DailyAccounts.model.listener.ChangeNoteDataCallBack
import com.haoduyoudu.DailyAccounts.model.models.Note
import com.haoduyoudu.DailyAccounts.utils.DateUtils
import com.haoduyoudu.DailyAccounts.utils.FileUtils
import com.haoduyoudu.DailyAccounts.utils.NoteUtils
import com.haoduyoudu.DailyAccounts.view.activities.base.BaseActivity
import com.haoduyoudu.DailyAccounts.view.adapters.MoodAdapter
import com.haoduyoudu.DailyAccounts.viewModel.viewModels.GlobalViewModel
import java.io.File
import kotlin.concurrent.thread

class NoteChangeMood : BaseActivity() {

    private val binding by lazy { ActivityNoteChangeMoodBinding.inflate(layoutInflater) }
    private val appViewModel by lazy { BaseApplication.viewModel as GlobalViewModel }
    private var isFirstWrite = false
    private lateinit var note: Note
    private lateinit var moodAdapter: MoodAdapter
    private val moodList = ArrayList<MoodAdapter.MoodItem>()
    private var chooseMoodId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        isFirstWrite = intent.getBooleanExtra("firstWrite", false)
        if (!isFirstWrite) {
            val noteId = intent.getLongExtra("noteId", -1L)
            if (noteId == -1L) {
                makeToast("Empty id")
                finish()
            }else {
                appViewModel.getNoteFromIdLiveData(noteId).observe(this) {
                    note = it
                }
            }
        }

        try {
            val allMoodNames = FileUtils.getAllFileNameInAssets(FileUtils.removePathSlashAtLast(BaseApplication.ASSETS_MOOD_PATH))
            allMoodNames.sort()
            for (i in allMoodNames) {
                moodList.add(MoodAdapter.MoodItem(FileUtils.getFileNameWithoutSuffix(i).toInt()))
                Log.d("NoteChangeMood", "load mood id ${FileUtils.getFileNameWithoutSuffix(i).toInt()}, file name is $i")
            }
            moodAdapter = MoodAdapter(this, moodList)

            binding.recyclerView.apply {
                layoutManager = StaggeredGridLayoutManager(4,
                    StaggeredGridLayoutManager.VERTICAL)
                adapter = moodAdapter
            }
        }catch (e: Exception) {
            e.printStackTrace()
            makeToast("情绪载入失败")
            finish()
        }

        if (::moodAdapter.isInitialized) {
            moodAdapter.setOnItemClickListener { view, i ->
                val mMood = moodList[i]
                chooseMoodId = mMood.moodId
                binding.bottomView.visibility = View.VISIBLE
                Glide.with(this).load("file:///android_asset/${BaseApplication.ASSETS_MOOD_PATH}${moodList[i].moodId}.png")
                    .into(binding.moodImage)
                if (!isFirstWrite) {
                    if (::note.isInitialized) {
                        try {
                            binding.moodText.setText(note.mood.second)
                        }catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }else if (!isFirstWrite){
                        makeToast("手帐损坏")
                        finish()
                    }
                }

                scrollToPosition(0,360) {
                    binding.topView.visibility = View.GONE
                }
            }
        }

        binding.cancel.setOnClickListener {
            finish()
        }

        binding.complete.setOnClickListener { _ ->
            val moodText = binding.moodText.text.toString()
            if (moodText.length <= 7) {
                if (moodText.isEmpty()) {
                    makeToast("输入名字")
                }else {
                    binding.bottomAndBottomView.visibility = View.VISIBLE
                    scrollToPosition(0,360*2) {
                        binding.bottomView.visibility = View.GONE
                        if (isFirstWrite) {
                            var yy = DateUtils.getYYYYFromCalendar()
                            var mm = DateUtils.getMMFromCalendar()
                            var dd = DateUtils.getDDFromCalendar()
                            intent.getIntExtra("yy", -1).let {
                                if (it != -1) yy = it
                            }
                            intent.getIntExtra("mm", -1).let {
                                if (it != -1) mm = it
                            }
                            intent.getIntExtra("dd", -1).let {
                                if (it != -1) dd = it
                            }
                            binding.oldWay.setOnClickListener {
                                thread {
                                    appViewModel.addNote(
                                        Note(
                                            yy, mm, dd,
                                            Pair(chooseMoodId, moodText),
                                            NOTE_TYPE_V1,
                                            hashMapOf(
                                                "template" to Pair(false, "1").toGson(),
                                                "textColor" to "1",
                                                "noteFolder" to BaseApplication.NOTES_PATH + DateUtils.formatYYMMDD(yy, mm, dd, DateUtils.FORMAT_YYYY_MM_DD) + '/',
                                                "body" to "",
                                                "recordPaths" to arrayListOf<String>().toGson(),
                                                "imagePaths" to arrayListOf<String>().toGson(),
                                                "videoPaths" to arrayListOf<String>().toGson(),
                                            )
                                        ),
                                        object : AddNoteCallBack {
                                            override fun onSuccessful(id: Long) {
                                                val mIntent = Intent(this@NoteChangeMood, NoteViewer::class.java)
                                                mIntent.putExtra("noteId", id)
                                                mIntent.putExtra("editMode", true)
                                                startActivity(mIntent)
                                                finish()
                                            }

                                            override fun onFailure(e: Exception) {
                                                e.printStackTrace()
                                                makeToast("创建失败")
                                                finish()
                                            }

                                            override fun hasExist() {
                                                makeToast("您已经写过了")
                                                finish()
                                            }
                                        }
                                    )
                                }
                            }
                            binding.newWay.setOnClickListener {
                                thread {
                                    appViewModel.addNote(
                                        Note(
                                            yy, mm, dd,
                                            Pair(chooseMoodId, moodText),
                                            NOTE_TYPE_V2,
                                            hashMapOf(
                                                "noteFolder" to BaseApplication.NOTES_PATH + DateUtils.formatYYMMDD(yy, mm, dd, DateUtils.FORMAT_YYYY_MM_DD) + '/',
                                                "backgroundColor" to "12",
                                                "pageSize" to "1",
                                            )
                                        ),
                                        object : AddNoteCallBack {
                                            override fun onSuccessful(id: Long) {
                                                val mIntent = Intent(this@NoteChangeMood, FreeMakeNote::class.java)
                                                mIntent.putExtra("noteId", id)
                                                mIntent.putExtra("editMode", true)
                                                startActivity(mIntent)
                                                finish()
                                            }

                                            override fun onFailure(e: Exception) {
                                                e.printStackTrace()
                                                makeToast("创建失败")
                                                finish()
                                            }

                                            override fun hasExist() {
                                                makeToast("您已经写过了")
                                                finish()
                                            }

                                        }
                                    )
                                }
                            }
                        }else {
                            saveMoodToDataBase(chooseMoodId, moodText)
                            finish()
                        }
                    }
                }
            }else {
                makeToast("名字太长了！")
            }
        }
    }

    private fun scrollToPosition(x:Int, y:Int, func: () -> Unit) {

        val xTranslate: ObjectAnimator = ObjectAnimator.ofInt(binding.scrollview, "scrollX", x)
        val yTranslate: ObjectAnimator = ObjectAnimator.ofInt(binding.scrollview, "scrollY", y)

        val animators = AnimatorSet();
        animators.duration = 1000
        animators.playTogether(xTranslate, yTranslate);
        animators.addListener(object : Animator.AnimatorListener{

            override fun onAnimationStart(arg0: Animator) {
                // TODO Auto-generated method stub
            }

            override fun onAnimationRepeat(arg0: Animator) {
                // TODO Auto-generated method stub

            }

            override fun onAnimationEnd(arg0: Animator) {
                // TODO Auto-generated method stub
                runOnUiThread {
                    func()
                }
            }

            override fun onAnimationCancel(arg0: Animator) {
                // TODO Auto-generated method stub
            }
        });
        animators.start();
    }

    private fun saveMoodToDataBase(moodId: Int, moodText: String) {
        thread {
            appViewModel.changeNoteDataFromId(note.id, object : ChangeNoteDataCallBack {
                override fun doChange(it: Note) {
                    it.mood = Pair(moodId, moodText)
                }

                override fun onChangeSuccessful() {}

                override fun onChangeFailure(e: Exception) {
                    makeToast("Change Failure")
                    e.printStackTrace()
                    finish()
                }
            })
        }
    }
}