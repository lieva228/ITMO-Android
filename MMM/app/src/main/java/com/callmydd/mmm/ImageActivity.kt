package com.callmydd.mmm

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso

class ImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        Picasso.get()
            .load(intent.getStringExtra("url"))
            .into(findViewById<ImageView>(R.id.imgFull))
    }
}