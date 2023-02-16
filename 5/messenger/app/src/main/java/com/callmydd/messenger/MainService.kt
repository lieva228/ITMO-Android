package com.callmydd.messenger

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import androidx.room.Room
import com.google.gson.JsonParser
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class MainService : Service() {
    private var serviceLooper: Looper? = null
    private var serviceHandler: ServiceHandler? = null

    var idx = 1
    var lastId = 1
    var end = false
    val listOfMessages: MutableList<MainActivity.Message> = Collections.synchronizedList(mutableListOf())
    private val binder = MyBinder()
    var getNew = false
    var lastFromDB = 0
    lateinit var db : MainActivity.AppDatabase

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            if (getNew) {
                parsePartFromHttp(idx)
                val responseIntent = Intent("MessageService")
                responseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                sendBroadcast(responseIntent)
                Thread {
                    try {
                        for (i in (listOfMessages.size - 100) until listOfMessages.size) {
                            if (listOfMessages[i].text == null) {
                                try {
                                    listOfMessages[i].img =
                                        HttpURLConnectionToHost().sendGETImageThumb(
                                            listOfMessages[i].url.toString()
                                        )
                                    val myDir = File(Environment.getExternalStorageDirectory().toString() + "/saved1")
                                    if (!myDir.exists()) {
                                        myDir.mkdirs()
                                    }
                                    val fname = listOfMessages[i].id.toString() + ".jpg"
                                    val file = File(myDir, fname)
                                    if (file.exists()) {
                                        file.delete()
                                    }
                                    try {
                                        file.createNewFile()
                                        val out = FileOutputStream(file)
                                        val bm = listOfMessages[i].img
                                        bm?.compress(Bitmap.CompressFormat.JPEG, 10, out)
                                        out.flush()
                                        out.close()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        println("some do not saved ")
                                        print(listOfMessages[i].id.toString())
                                    }
                                } catch (ex: Exception) {

                                }
                            }
                            if (end) {
                                val responseLastIntent = Intent("Messages")
                                responseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                sendBroadcast(responseLastIntent)
                            }
                        }
                    } catch (ex: Exception) {

                    }
                }.start()
            } else {
                db = Room.databaseBuilder(
                    applicationContext,
                    MainActivity.AppDatabase::class.java, "database-name"
                ).build()
                val listOfDB = db.messageDao?.allPeople!!
                for (i in lastFromDB until listOfDB.size) {
                    var img : Bitmap? = null
                    if (listOfDB[i]?.url != null) {
                        val imgPath = Environment.getExternalStorageDirectory().path + "/saved1" + "/${listOfDB[i]!!.uid}.jpg"
                        val options = BitmapFactory.Options()
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888
                        img = BitmapFactory.decodeFile(imgPath, options)
                    }
                    listOfMessages.add(
                        MainActivity.Message(
                            listOfDB[i]!!.uid,
                            listOfDB[i]!!.time,
                            listOfDB[i]!!.name,
                            listOfDB[i]!!.text,
                            img,
                            listOfDB[i]!!.url
                        )
                    )
                    lastId = listOfDB[i]!!.uid
                    idx = lastId + 1
                    lastFromDB = i + 1
                    if (idx % 100 == 0) {
                        val responseIntent = Intent("MessageService")
                        responseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        sendBroadcast(responseIntent)
                    }
                }
                getNew = true
                val responseIntent = Intent("MessageService")
                responseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                sendBroadcast(responseIntent)
            }
        }
    }

    override fun onCreate() {
        HandlerThread("ServiceStartArguments").apply {
            start()
            serviceLooper = looper
            serviceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        idx = intent.getIntExtra("idx", 1)
        serviceHandler?.obtainMessage()?.also { msg ->
            serviceHandler?.sendMessage(msg)
        }
        return START_NOT_STICKY
    }

    private fun parsePartFromHttp(idx : Int?) {
        try {
            db = Room.databaseBuilder(
                applicationContext,
                MainActivity.AppDatabase::class.java, "database-name"
            ).build()

            val a = JsonParser().parse(HttpURLConnectionToHost().sendGET(idx!!)).asJsonArray
            val tmpMessageList = mutableListOf<MainActivity.Message>()
            val tmpMessageForDBList = mutableListOf<MainActivity.MessageForDB>()
            end = a.size() == 0
            for (i in 0 until a.size()) {
                if (a[i].asJsonObject["data"].asJsonObject.toString().substring(2, 6) == "Text") {
                    tmpMessageList.add(
                        MainActivity.Message(
                            a[i].asJsonObject["id"].asInt,
                            a[i].asJsonObject["time"].asString.toString(),
                            a[i].asJsonObject["from"].asString.toString(),
                            a[i].asJsonObject["data"].asJsonObject["Text"].asJsonObject["text"].asString.toString(),
                            null
                        )
                    )
                    tmpMessageForDBList.add(MainActivity.MessageForDB(
                        a[i].asJsonObject["id"].asInt,
                        a[i].asJsonObject["time"].asString.toString(),
                        a[i].asJsonObject["from"].asString.toString(),
                        a[i].asJsonObject["data"].asJsonObject["Text"].asJsonObject["text"].asString.toString(),
                        null
                    ))
                } else {
                    tmpMessageList.add(
                        MainActivity.Message(
                            a[i].asJsonObject["id"].asInt,
                            a[i].asJsonObject["time"].asString.toString(),
                            a[i].asJsonObject["from"].asString.toString(),
                            null,
                            null,
                            a[i].asJsonObject["data"].asJsonObject["Image"].asJsonObject["link"].asString.toString()
                        )
                    )
                    tmpMessageForDBList.add(MainActivity.MessageForDB(
                        a[i].asJsonObject["id"].asInt,
                        a[i].asJsonObject["time"].asString.toString(),
                        a[i].asJsonObject["from"].asString.toString(),
                        null,
                        a[i].asJsonObject["data"].asJsonObject["Image"].asJsonObject["link"].asString.toString()
                    ))
                }
            }
            listOfMessages.addAll(tmpMessageList)
            for (i in 0 until tmpMessageForDBList.size) {
                db.messageDao?.insert(tmpMessageForDBList[i])
            }
            if (!end) {
                lastId = a[a.size() - 1].asJsonObject["id"].asInt
                this.idx = lastId + 1
            }
        } catch (ex : IOException) {
            end = true
        }
    }

    inner class MyBinder : Binder() {
        fun getService() = this@MainService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }
}