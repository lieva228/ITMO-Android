package com.callmydd.messenger

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class ImageActivity : AppCompatActivity() {

    private var broadcastReceiver: BroadcastReceiver? = null
    private lateinit var mService: ImageService
    private var mBound: Boolean = false
    private lateinit var intentImage : Intent

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName, p1: IBinder) {
            val binder: ImageService.MyBinder =
                p1 as ImageService.MyBinder
            mService = binder.getService()
            mBound = true
            val img = findViewById<ImageView>(R.id.imgBig)

            if (mService.image != null && mService.url == intent.getStringExtra("url")) {
                img.setImageBitmap(mService.image)
            } else {
                startService(intentImage)
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activy_image)

        val img = findViewById<ImageView>(R.id.imgBig)

        intentImage = Intent(this, ImageService::class.java).apply {
            putExtra("url", intent.getStringExtra("url"))}

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                img.setImageBitmap(mService.image)
            }
        }

        val intentFilter = IntentFilter("ImageService")
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, ImageService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        mBound = false
    }
}