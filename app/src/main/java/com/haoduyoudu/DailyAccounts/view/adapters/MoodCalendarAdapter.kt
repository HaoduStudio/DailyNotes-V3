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
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.R
import com.haoduyoudu.DailyAccounts.model.database.NOTE_TYPE_V1

class MoodCalendarAdapter (private val activity: AppCompatActivity, private val MoodList: List<MoodItem>):
        RecyclerView.Adapter<MoodCalendarAdapter.ViewHolder>(){

    private lateinit var itemClickListener: (View, Int) -> Unit
    private lateinit var itemLongClickListener: (View, Int) -> Boolean?

    data class MoodItem(val moodId: Int = -1, val noteId: Long = -1L, val type: Int = NOTE_TYPE_V1)

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val moodImage: ImageView = view.findViewById(R.id.moodImage)
        val maskText: TextView = view.findViewById(R.id.maskText)
        val bigDateText: TextView = view.findViewById(R.id.bigDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.mood_calendar_item, parent, false)
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

    override fun getItemCount() = MoodList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mood = MoodList[position]
        if (mood.moodId != -1) {
            holder.bigDateText.visibility = View.GONE
            holder.maskText.visibility = View.VISIBLE
            Glide.with(activity).load("file:///android_asset/${BaseApplication.ASSETS_MOOD_PATH}${mood.moodId}.png")
                .into(holder.moodImage)
            holder.maskText.text = (position + 1).toString()
        }else {
            holder.bigDateText.visibility = View.VISIBLE
            holder.maskText.visibility = View.GONE
            holder.bigDateText.text = (position + 1).toString()
        }
    }

    fun setOnItemClickListener(function: (View, Int) -> Unit) {
        itemClickListener = function
    }

    fun setOnItemLongClickListener(function: (View, Int) -> Boolean?) {
        itemLongClickListener = function
    }
}