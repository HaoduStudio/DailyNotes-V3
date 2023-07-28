package com.haoduyoudu.DailyAccounts.view.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.databinding.ActivityMoodCalendarBinding
import com.haoduyoudu.DailyAccounts.helper.makeToast
import com.haoduyoudu.DailyAccounts.model.database.NOTE_TYPE_V1
import com.haoduyoudu.DailyAccounts.model.database.NOTE_TYPE_V2
import com.haoduyoudu.DailyAccounts.model.models.Note
import com.haoduyoudu.DailyAccounts.utils.DateUtils
import com.haoduyoudu.DailyAccounts.view.activities.base.BaseActivity
import com.haoduyoudu.DailyAccounts.view.adapters.MoodCalendarAdapter
import com.haoduyoudu.DailyAccounts.viewModel.viewModels.GlobalViewModel

class MoodCalendar : BaseActivity() {

    private val binding by lazy { ActivityMoodCalendarBinding.inflate(layoutInflater) }
    private val appViewModel by lazy { BaseApplication.viewModel as GlobalViewModel }
    private lateinit var adapter: MoodCalendarAdapter
    private val moodList = ArrayList<MoodCalendarAdapter.MoodItem>()
    private var yyNow = DateUtils.getYYYYFromCalendar()
    private var mmNow = DateUtils.getMMFromCalendar()
    private val tempNotes = ArrayList<Note>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initAdapter()
        appViewModel.notesList.observe(this) {
            tempNotes.clear()
            tempNotes.addAll(it)
            updateTimeOrMood(yyNow, mmNow)
        }

        binding.leftButton.setOnClickListener {
            if (mmNow == 1) {
                mmNow = 12
                yyNow -= 1
            }else {
                mmNow -= 1
            }
            updateTimeOrMood()
        }

        binding.rightButton.setOnClickListener {
            if (mmNow == 12) {
                mmNow = 1
                yyNow += 1
            }else {
                mmNow += 1
            }
            updateTimeOrMood()
        }

        adapter.setOnItemClickListener { view, i ->
            val mNote = moodList[i]
            if (mNote.noteId != -1L) {
                if (mNote.type == NOTE_TYPE_V1) {
                    val mIntent = Intent(this, NoteViewer::class.java)
                    mIntent.putExtra("noteId", mNote.noteId)
                    startActivity(mIntent)
                }else if (mNote.type == NOTE_TYPE_V2) {
                    val mIntent = Intent(this, FreeMakeNote::class.java)
                    mIntent.putExtra("noteId", mNote.noteId)
                    startActivity(mIntent)
                }
            }else {
                if (yyNow <= DateUtils.getYYYYFromCalendar() &&
                    (mmNow < DateUtils.getMMFromCalendar() || (mmNow == DateUtils.getMMFromCalendar() && (i + 1) <= DateUtils.getDDFromCalendar()))
                ) {
                    val mIntent = Intent(this, NoteChangeMood::class.java)
                    mIntent.putExtra("firstWrite", true)
                    mIntent.putExtra("yy", yyNow)
                    mIntent.putExtra("mm", mmNow)
                    mIntent.putExtra("dd", (i + 1))
                    startActivity(mIntent)
                }
            }
        }

        adapter.setOnItemLongClickListener { view, i ->
            val mNote = moodList[i]
            if (mNote.noteId != -1L) {
                val mIntent = Intent(this, NoteOption::class.java)
                mIntent.putExtra("noteId", mNote.noteId)
                mIntent.putExtra("type", mNote.type)
                startActivity(mIntent)
            }
            null
        }
    }

    private fun initAdapter() {
        adapter = MoodCalendarAdapter(this, moodList)
        binding.calendar.apply {
            layoutManager = GridLayoutManager(this@MoodCalendar,5)
            adapter = this@MoodCalendar.adapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateTimeOrMood(yy: Int = yyNow, mm: Int = mmNow) {
        yyNow = yy
        mmNow = mm
        binding.mouth.text = DateUtils.formatYYMMDD(yy, mm, 1, "yy/MM")
        val days = DateUtils.getMonthDays(yy, mm)
        moodList.clear()
        val targetMoodList = tempNotes.filter {
            it.yy == yy && it.mm == mm
        }

        try {
            val noChangeList = Array(days) {
                MoodCalendarAdapter.MoodItem()
            }
            targetMoodList.forEach {
                noChangeList[it.dd - 1] = MoodCalendarAdapter.MoodItem(it.mood.first, it.id, it.type)
            }

            moodList.addAll(noChangeList)
            adapter.notifyDataSetChanged()
        }catch (e: Exception) {
            e.printStackTrace()
            makeToast("更新失败")
        }
    }
}