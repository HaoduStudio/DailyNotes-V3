package com.haoduyoudu.DailyAccounts.view.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haoduyoudu.DailyAccounts.BaseApplication.Companion.ASSETS_MOOD_PATH
import com.haoduyoudu.DailyAccounts.R
import com.haoduyoudu.DailyAccounts.model.models.Note
import com.haoduyoudu.DailyAccounts.utils.DateUtils
import com.haoduyoudu.DailyAccounts.utils.ViewUtils
import java.util.*
import kotlin.collections.ArrayList


class FindNoteAdapter (private val activity: AppCompatActivity, private val NotesList: List<NoteItem>) :
    RecyclerView.Adapter<FindNoteAdapter.ViewHolder>() {

    private val tag = "FindNoteAdapter"
    lateinit var itemClickListener: (View, Int) -> Unit
    lateinit var itemLongClickListener: (View, Int) -> Boolean?
    private val mKeyword = ArrayList<String>()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val moodImg: ImageView = view.findViewById(R.id.mood_img)
        val dayOfWeekText: TextView = view.findViewById(R.id.day_of_week_text)
        val dateText: TextView = view.findViewById(R.id.note_date_text)
        val bodyText: TextView = view.findViewById(R.id.body_text)
        val moodText: TextView = view.findViewById(R.id.mood_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.find_note_item, parent, false)
        val viewHolder = ViewHolder(view)
        view.setOnClickListener {
            if (::itemClickListener.isInitialized) {
                itemClickListener(it, viewHolder.adapterPosition)
            }
        }
        view.setOnLongClickListener {
            if (::itemLongClickListener.isInitialized) {
                itemLongClickListener(it, viewHolder.adapterPosition) ?: true
            }else {
                true
            }
        }
        return viewHolder
    }

    override fun getItemCount() = NotesList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = NotesList[position]
        try {
            holder.dateText.text = "${note.data.yy}-${note.data.mm}-${note.data.dd}"
            Glide.with(activity).load("file:///android_asset/$ASSETS_MOOD_PATH${note.data.mood.first}.png")
                .into(holder.moodImg)
            holder.dayOfWeekText.text = DateUtils.getDayOfWeek(note.data.yy, note.data.mm, note.data.dd)
            ViewUtils.ellipsizeEnd(holder.bodyText, 3, note.data.data["body"]!!) {
                Log.d("FindNoteAdapter", holder.bodyText.text.toString())
                setHighLightText(holder.bodyText.text.toString(), mKeyword, holder.bodyText)
            }
            setHighLightText(note.data.mood.second, mKeyword, holder.moodText)
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }


    data class NoteItem(var data: Note)

    fun setOnItemClickListener(function: (View, Int) -> Unit) {
        itemClickListener = function
    }

    fun setOnItemLongClickListener(function: (View, Int) -> Boolean?) {
        itemLongClickListener = function
    }

    fun setKeyword(kw: Array<String>) {
        mKeyword.clear()
        mKeyword.addAll(kw)
    }

    private fun setHighLightText(text: String, keywords: ArrayList<String>, textView: TextView) {
        val styled = SpannableStringBuilder(text)
        for (keyword in keywords) {
            if (keyword.isNotEmpty() && text.isNotEmpty()) {
                val mKeyword = keyword.lowercase()
                val mText = text.lowercase()
                val allStartIndex = searchAllIndex(mText, mKeyword)
                allStartIndex.forEach { start ->
                    Log.d("FindNoteAdapter", "Start index at $start")
                    styled.setSpan(
                        ForegroundColorSpan(Color.parseColor("#2196F3")),
                        start,
                        start + keyword.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
        textView.text = styled
    }

    private fun searchAllIndex(str: String, key: String): ArrayList<Int> {
        val result = ArrayList<Int>()
        var a: Int = str.indexOf(key) //*第一个出现的索引位置
        while (a != -1) {
            result.add(a)
            a = str.indexOf(key, a + 1) //*从这个索引往后开始第一个出现的位置
        }
        return result
    }
}