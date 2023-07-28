package com.haoduyoudu.DailyAccounts.view.adapters

import android.annotation.SuppressLint
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
import com.haoduyoudu.DailyAccounts.utils.NoteUtils

open class BaseNoteAdapter (private val activity: AppCompatActivity, private val NotesList: List<NoteItem>) :
        RecyclerView.Adapter<BaseNoteAdapter.ViewHolder>() {

    private val tag = "BaseNoteAdapter"
    lateinit var itemClickListener: (View, Int) -> Unit
    lateinit var itemLongClickListener: (View, Int) -> Boolean?

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val moodImg: ImageView = view.findViewById(R.id.mood_img)
        val dayOfWeekText: TextView = view.findViewById(R.id.day_of_week_text)
        val dateText: TextView = view.findViewById(R.id.note_date_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.base_note_item, parent, false)
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

}