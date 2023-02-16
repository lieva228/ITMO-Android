package com.callmydd.mvvm.view

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.callmydd.mvvm.R
import com.squareup.picasso.Picasso

class FullScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen)
        Picasso.get()
            .load(intent.getStringExtra("url"))
            .into(findViewById<ImageView>(R.id.imgFull))
    }
}