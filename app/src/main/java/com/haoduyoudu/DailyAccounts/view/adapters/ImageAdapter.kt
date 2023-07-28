package com.haoduyoudu.DailyAccounts.view.adapters

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.haoduyoudu.DailyAccounts.R
import com.haoduyoudu.DailyAccounts.helper.makeToast

class ImageAdapter (private val activity: AppCompatActivity, private val ImageList: List<ImageItem>) :
        RecyclerView.Adapter<ImageAdapter.ViewHolder>(){

    private lateinit var itemClickListener: (View, Int) -> Unit
    private lateinit var itemLongClickListener: (View, Int) -> Boolean?
    private lateinit var allDoneListener: () -> Unit
    private var loadCompleteCount = 0


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.listImage)
        val videoForeground: ImageView = view.findViewById(R.id.videoForeground)
    }

    data class ImageItem(val path: String, val isVideo: Boolean)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_image_item, parent, false)
        val viewHolder = ViewHolder(view)
        // listener
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

    override fun getItemCount() = ImageList.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        if (position == 0) loadCompleteCount = 0
        val image = ImageList[position]
        Glide.with(activity).load(image.path).listener(
            object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    activity.runOnUiThread {
                        makeToast("Load photo failure")
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    loadCompleteCount++
                    if (loadCompleteCount == itemCount) {
                        if (::allDoneListener.isInitialized) {
                            allDoneListener()
                        }
                    }
                    return false
                }

            }
        ).into(holder.imageView)
        if (image.isVideo) {
            holder.videoForeground.visibility = View.VISIBLE
        }else {
            holder.videoForeground.visibility = View.GONE
        }
    }

    fun setOnItemClickListener(function: (View, Int) -> Unit) {
        itemClickListener = function
    }

    fun setOnItemLongClickListener(function: (View, Int) -> Boolean?) {
        itemLongClickListener = function
    }

    fun setOnAllDoneListener(function: () -> Unit) {
        allDoneListener = function
    }
}