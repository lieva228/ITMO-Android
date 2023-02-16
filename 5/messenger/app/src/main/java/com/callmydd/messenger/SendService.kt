package com.callmydd.messenger

import android.app.Service
import android.content.Intent
import android.os.*
import java.io.IOException
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class SendService : Service() {

    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    var path: String? = null
    var text: String = ""
    var type: String? = null
    private val binder = MyBinder()

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            if (type == "img") {
                sendImg()
            } else {
                sendText()
            }
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
        type = intent.getStringExtra("type")
        text = intent.getStringExtra("text").toString()
        println(text)
        path = intent.getStringExtra("path").toString()
        serviceHandler?.obtainMessage()?.also { msg ->
            serviceHandler?.sendMessage(msg)
        }
        return START_NOT_STICKY
    }

    private fun sendText() {
        try {
            val url = URL("http://213.189.221.170:8008/1ch")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            val os = OutputStreamWriter(connection.outputStream)
            os.append("{\"from\":\"l\",\"to\":\"1@channel\",\"data\":{\"Text\":{\"text\":\"$text\"}}}")
                .flush()
            os.close()
            println(connection.responseCode)
        } catch (ex : IOException) {

        }
    }

    private fun sendImg() {
        try {
            MultipartExampleClient.f(
                arrayOf(
                    "http://213.189.221.170:8008/1ch",
                    "msg%{\"from\":\"l\"," +
                            "\"to\":\"1@channel\"," +
                            "\"data\":{\"Image\":{\"link\":\"${path}\"}}}",
                    "pic=${path}"
                )
            )
        } catch (ex : IOException) {

        }
    }


    inner class MyBinder : Binder() {
        fun getService() = this@SendService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }
}