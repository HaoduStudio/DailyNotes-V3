package com.haoduyoudu.DailyAccounts.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haoduyoudu.DailyAccounts.BaseApplication.Companion.ASSETS_MOOD_PATH
import com.haoduyoudu.DailyAccounts.R

class MoodAdapter (private val activity: AppCompatActivity, private val MoodList: List<MoodItem>) :
        RecyclerView.Adapter<MoodAdapter.ViewHolder>(){

    private lateinit var itemClickListener: (View, Int) -> Unit
    private lateinit var itemLongClickListener: (View, Int) -> Boolean?

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val moodImage: ImageView = view.findViewById(R.id.moodImage)
        val background: View = view.findViewById(R.id.moreMoodBackground)
    }

    data class MoodItem(val moodId: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_mood_item, parent, false)
        val viewHolder = ViewHolder(view)
        view.setOnClickListener {
            viewHolder.background.visibility = View.VISIBLE
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mood = MoodList[position]
        Glide.with(activity).load("file:///android_asset/$ASSETS_MOOD_PATH${mood.moodId}.png")
            .into(holder.moodImage)
    }

    fun setOnItemClickListener(function: (View, Int) -> Unit) {
        itemClickListener = function
    }

    fun setOnItemLongClickListener(function: (View, Int) -> Boolean?) {
        itemLongClickListener = function
    }
}