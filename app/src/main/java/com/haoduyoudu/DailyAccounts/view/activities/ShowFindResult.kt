package com.haoduyoudu.DailyAccounts.view.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.R
import com.haoduyoudu.DailyAccounts.databinding.ActivityShowFindResultBinding
import com.haoduyoudu.DailyAccounts.helper.makeToast
import com.haoduyoudu.DailyAccounts.model.database.NOTE_TYPE_V1
import com.haoduyoudu.DailyAccounts.model.models.Note
import com.haoduyoudu.DailyAccounts.utils.DateUtils
import com.haoduyoudu.DailyAccounts.utils.ViewUtils
import com.haoduyoudu.DailyAccounts.view.activities.base.BaseActivity
import com.haoduyoudu.DailyAccounts.view.adapters.FindNoteAdapter
import com.haoduyoudu.DailyAccounts.viewModel.viewModels.GlobalViewModel
import kotlin.concurrent.thread

class ShowFindResult : BaseActivity() {
    private val binding by lazy { ActivityShowFindResultBinding.inflate(layoutInflater) }
    private val appViewModel by lazy { BaseApplication.viewModel as GlobalViewModel }
    private var findingRunning = true
    private val noteList = ArrayList<FindNoteAdapter.NoteItem>()
    private lateinit var adapter: FindNoteAdapter
    private var tempBackgroundPath = ""

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val keywordList = intent.getStringArrayExtra("keyword")
        if (keywordList == null) {
            makeToast("关键词错误")
            finish()
            return
        }

        val sb = StringBuilder()
        keywordList.forEach {
            sb.append(it)
            sb.append(" ")
        }
        ViewUtils.ellipsizeEnd(binding.title, 1, "查找: $sb")

        appViewModel.notesList.observe(this) { oList ->
            val it = oList.sortedBy { it2 ->
                DateUtils.formatYYMMDD(it2.yy, it2.mm, it2.dd, DateUtils.FORMAT_YYYY_MM_DD)
            }.reversed()

            val isFirstLoad = !::adapter.isInitialized
            if (isFirstLoad) {
                adapter = FindNoteAdapter(this@ShowFindResult, noteList)
                adapter.setKeyword(keywordList)
                binding.listView.apply {
                    layoutManager = LinearLayoutManager(this@ShowFindResult).apply {
                        orientation = LinearLayoutManager.VERTICAL
                    }
                    adapter = this@ShowFindResult.adapter
                }

                adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onChanged() {
                        super.onChanged()
                        refreshBackground()
                    }
                })

                adapter.setOnItemClickListener { view, i ->
                    try {
                        val note = noteList[i]
                        val mIntent = Intent(this, NoteViewer::class.java)
                        mIntent.putExtra("noteId", note.data.id)
                        startActivity(mIntent)
                    }catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                adapter.setOnItemLongClickListener { view, i ->
                    try {
                        val note = noteList[i]
                        val mIntent = Intent(this, NoteOption::class.java)
                        mIntent.putExtra("noteId", note.data.id)
                        mIntent.putExtra("type", note.data.type)
                        startActivity(mIntent)
                    }catch (e: Exception) {
                        e.printStackTrace()
                    }
                    null
                }
                makeToast("查找中...")
            }

            noteList.clear()
            adapter.notifyDataSetChanged()

            thread {
                try {
                    var i = 0
                    while (findingRunning && i < it.size) {
                        if (it[i].type == NOTE_TYPE_V1) {
                            for (aKeyword in keywordList) {
                                if (it[i].data["body"]!!.lowercase().contains(aKeyword.lowercase())
                                    || it[i].mood.second.lowercase().contains(aKeyword.lowercase())) {
                                    noteList.add(FindNoteAdapter.NoteItem(it[i]))
                                    Log.d("ShowFindResult", "在手帐中找到关键词: $it")
                                    break
                                }
                            }
                        }
                        i++
                    }
                    runOnUiThread {
                        adapter.notifyDataSetChanged()
                        if (isFirstLoad) {
                            makeToast("找到 ${adapter.itemCount} 个手帐")
                        }
                    }
                }catch (e: Exception) {
                    e.printStackTrace()
                    runOnUiThread {
                        makeToast("查找失败")
                    }
                }
            }
        }

        appViewModel.appBackgroundPath.observe(this) {
            tempBackgroundPath = it
            refreshBackground()
        }
    }

    override fun onStop() {
        super.onStop()
        findingRunning = false
    }

    private fun refreshBackground() {
        if (adapter.itemCount > 0) {
            if (tempBackgroundPath != "") {
                Glide.with(this@ShowFindResult).load(
                    tempBackgroundPath
                ).into(binding.backgroundImg)
            }
        }else {
            binding.backgroundImg.setImageResource(R.color.black)
        }
    }
}