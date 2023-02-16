package com.callmydd.messenger

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class HttpURLConnectionToHost {

    @Throws(IOException::class)
    fun sendGET(lastKnownId : Int) : String {
        var response = ""
            val url = URL("http://213.189.221.170:8008/1ch?lastKnownId=$lastKnownId&limit=100")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val `in` = BufferedReader(
                    InputStreamReader(
                        connection.inputStream
                    )
                )
                response = `in`.readText()
                `in`.close()
            }
        return response
    }

    @Throws(IOException::class)
    fun sendGETImageThumb(pic : String) : Bitmap? {
        val urlstr = "http://213.189.221.170:8008/thumb/$pic"
        val url = URL(urlstr)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val `in` = connection.inputStream
            val bimap = BitmapFactory.decodeStream(`in`)
            `in`.close()
            return bimap
        } else {
            return null
        }
    }

    @Throws(IOException::class)
    fun sendGETImageImg(pic : String) : Bitmap? {
        val urlstr = "http://213.189.221.170:8008/img/$pic"
        val url = URL(urlstr)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val `in` = connection.inputStream
            val bimap = BitmapFactory.decodeStream(`in`)
            `in`.close()
            return bimap
        } else {
            return null
        }
    }
}