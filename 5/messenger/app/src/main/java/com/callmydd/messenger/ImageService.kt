package com.callmydd.messenger

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.*
import java.net.ConnectException

class ImageService : Service() {

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    var image: Bitmap? = null
    var url: String? = null
    private val binder = MyBinder()

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {

            getImage()
            val responseIntent = Intent("ImageService")
            responseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            sendBroadcast(responseIntent)
        }
    }

    override fun onCreate() {
        HandlerThread("Start").apply {
            start()
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        url = intent.getStringExtra("url")
        serviceHandler?.obtainMessage()?.also { msg ->
            serviceHandler?.sendMessage(msg)
        }
        return START_NOT_STICKY
    }

    private fun getImage() {
        try {
            image = HttpURLConnectionToHost().sendGETImageImg(url.toString())
        } catch (ex : ConnectException) {

        }
    }

    inner class MyBinder : Binder() {
        fun getService() = this@ImageService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }
}