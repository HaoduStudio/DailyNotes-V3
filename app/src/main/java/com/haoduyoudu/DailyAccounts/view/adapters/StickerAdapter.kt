package com.haoduyoudu.DailyAccounts.view.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haoduyoudu.DailyAccounts.BaseApplication
import com.haoduyoudu.DailyAccounts.R

class     StickerAdapter (private val activity: AppCompatActivity, private val StickerList: List<StickerItem>):
        RecyclerView.Adapter<StickerAdapter.ViewHolder>() {

    private lateinit var itemClickListener: (View, Int) -> Unit
    private lateinit var itemLongClickListener: (View, Int) -> Boolean?

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val stickerImg: ImageView = view.findViewById(R.id.stickerImage)
    }

    fun setOnItemClickListener(function: (View, Int) -> Unit) {
        itemClickListener = function
    }

    fun setOnItemLongClickListener(function: (View, Int) -> Boolean?) {
        itemLongClickListener = function
    }

    data class StickerItem(val path: String, val uriBitmapPath: String = "")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_sticker_item, parent, false)
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

    override fun getItemCount() = StickerList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sticker = StickerList[position]
        if (sticker.uriBitmapPath.isNotEmpty()) {
            Glide.with(activity).load(sticker.uriBitmapPath).into(holder.stickerImg)
        }else {
            Glide.with(activity).load("file:///android_asset/${sticker.path}")
                .into(holder.stickerImg)
        }
    }
}