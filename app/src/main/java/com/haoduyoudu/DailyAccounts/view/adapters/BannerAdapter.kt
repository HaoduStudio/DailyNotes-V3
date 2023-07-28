package com.haoduyoudu.DailyAccounts.view.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.haoduyoudu.DailyAccounts.R

class BannerAdapter (private val activity: AppCompatActivity, private val ImgList: List<String>) :
    RecyclerView.Adapter<BannerAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bannerImg: ImageView = view.findViewById(R.id.banner_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.guide_banner_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = ImgList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(activity).load(ImgList[position]).into(holder.bannerImg)
    }
}