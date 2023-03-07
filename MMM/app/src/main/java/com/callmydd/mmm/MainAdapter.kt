package com.callmydd.mmm

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.item_layout.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


class MainAdapter(private val users: MutableList<Message>, private val onClick: (Message) -> Unit) : RecyclerView.Adapter<MainAdapter.DataViewHolder>() {

    class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(msg: Message) {
            itemView.apply {
                println(msg.toString())
                textViewUserName.text = msg.from
                if (msg.data.Text != null) {
                    textViewUserEmail.text = msg.data.Text.text
                    img.setImageBitmap(null)
                    Picasso.get().cancelRequest(img)
                } else if (msg.data.Image != null) {
                    val myDir = File(Environment.getExternalStorageDirectory().toString() + "/saveImageForMMM4")
                    if (!myDir.exists()) {
                        myDir.mkdirs()
                    }
                    val fname = msg.id.toString() + ".jpg"
                    val file = File(myDir, fname)
                    println(file.absoluteFile)
                    if (file.exists()) {
                        Picasso.get()
                            .load(file)
                            .into(img)
                        println("file exist")
                    } else {
                        Picasso.get()
                            .load("http://213.189.221.170:8008/thumb/" + msg.data.Image.link)
                            .into(img)
                        Picasso.get()
                            .load("http://213.189.221.170:8008/thumb/" + msg.data.Image.link)
                            .into(getTarget(msg.id.toString() +  ".jpg"))
                    }
                    textViewUserEmail.text = ""
                }
            }
        }

        private fun getTarget(path: String): Target {
            return object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom?) {
                    CoroutineScope(Dispatchers.IO).launch {
                        kotlin.runCatching {
                            val file =
                                File(Environment.getExternalStorageDirectory().path + "/saveImageForMMM4/" + path)
                            println("save to " + file.absoluteFile)
                            if (!File(file.parent!!).exists()) {
                                File(file.parent!!).createNewFile()
                            }
                            file.createNewFile()
                            val ostream = FileOutputStream(file)
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream)
                            ostream.flush()
                            ostream.close()
                        }
                    }
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val holder = DataViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false))
        holder.itemView.img.setOnClickListener {
            onClick(users[holder.adapterPosition])
        }
        return holder
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(users[position])
    }
}