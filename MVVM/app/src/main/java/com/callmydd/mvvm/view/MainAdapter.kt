package com.callmydd.mvvm.view

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.callmydd.mvvm.R
import com.callmydd.mvvm.model.Message
import com.callmydd.mvvm.utils.BASE_URL
import com.callmydd.mvvm.utils.PATH_TO_SAVED_IMAGES
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.item_layout.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


class MainAdapter(private val messages: MutableList<Message>, private val onClick: (Message) -> Unit) : RecyclerView.Adapter<MainAdapter.DataViewHolder>() {

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
                    val myDir = File(Environment.getExternalStorageDirectory().toString() + PATH_TO_SAVED_IMAGES)
                    if (!myDir.exists()) {
                        myDir.mkdirs()
                    }
                    val fName = msg.id.toString() + ".jpg"
                    val file = File(myDir, fName)
                    println(file.absoluteFile)
                    if (file.exists()) {
                        Picasso.get()
                            .load(file)
                            .into(img)
                        println("file exist")
                    } else {
                        Picasso.get()
                            .load(BASE_URL + "thumb/" + msg.data.Image.link)
                            .into(img)
                        Picasso.get()
                            .load(BASE_URL + "thumb/" + msg.data.Image.link)
                            .into(getTarget("/$fName"))
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
                                File(Environment.getExternalStorageDirectory().path + PATH_TO_SAVED_IMAGES + path)
                            println("save to " + file.absoluteFile)
                            if (!File(file.parent!!).exists()) {
                                File(file.parent!!).createNewFile()
                            }
                            file.createNewFile()
                            val outStream = FileOutputStream(file)
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outStream)
                            outStream.flush()
                            outStream.close()
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
            onClick(messages[holder.adapterPosition])
        }
        return holder
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        holder.bind(messages[position])
    }
}