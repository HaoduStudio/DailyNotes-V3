package com.haoduyoudu.DailyAccounts.view.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.haoduyoudu.DailyAccounts.R

class RecordAdapter (private val activity: AppCompatActivity, private val RecordList: List<RecordItem>) :
        RecyclerView.Adapter<RecordAdapter.ViewHolder>() {

    private lateinit var itemClickListener: (View, Int) -> Unit
    private lateinit var itemLongClickListener: (View, Int) -> Boolean?

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recordText: TextView = view.findViewById(R.id.recordName)
    }

    data class RecordItem(val path: String)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_record_item, parent, false)
        val viewHolder = ViewHolder(view)

        // Listener
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

    override fun getItemCount(): Int {
        return RecordList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = RecordList[position]
        holder.recordText.text = "录音 ${position+1}"
    }

    fun setOnItemClickListener(function: (View, Int) -> Unit) {
        itemClickListener = function
    }

    fun setOnItemLongClickListener(function: (View, Int) -> Boolean?) {
        itemLongClickListener = function
    }
}