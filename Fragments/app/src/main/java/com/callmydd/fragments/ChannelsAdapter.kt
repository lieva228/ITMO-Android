package com.callmydd.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChannelsAdapter(var channels : MutableList<String>, private val clickListener: (String, View) -> Unit) : RecyclerView.Adapter<ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ItemViewHolder(parent, clickListener)
    override fun getItemCount() = channels.size
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) = holder.bind(channels[position])

}

class ItemViewHolder(parent: ViewGroup, private val clickListener: (String, View) -> Unit) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.images_list_item, parent, false)
) {
    private val title = itemView.findViewById<TextView>(R.id.title)
    private var channelName: String = ""

    init {
        itemView.setOnClickListener {
            clickListener(channelName, itemView)
        }
    }

    fun bind(channel: String) {
        channelName = channel
        if (!channel.startsWith("Private chat with: ")) {
            title.text = channel.substring(0, channel.length - 8)
        } else {
            title.text = channel
        }
    }
}