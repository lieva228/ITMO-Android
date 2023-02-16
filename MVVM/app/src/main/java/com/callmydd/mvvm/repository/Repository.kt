package com.callmydd.mvvm.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.callmydd.mvvm.api.ApiService
import com.callmydd.mvvm.model.*
import com.callmydd.mvvm.utils.CHANNEL_NAME
import com.callmydd.mvvm.utils.LIMIT
import com.callmydd.mvvm.utils.USER_NAME
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.net.ConnectException
import javax.inject.Inject

class Repository @Inject constructor(private val api: ApiService, private val db: MessageDao) {

    private var dbShown : Boolean = false
    private var maxIdRead : Int = 0
    val data: MutableLiveData<List<Message>?> = MutableLiveData<List<Message>?>()
    private val localData = ArrayList<Message>()

    suspend fun getMessages() {
        if (db.allMessage.isEmpty()) {
            getAllMessageListApi()
        } else {
            getAllMessageListDB()
        }
    }

    private suspend fun getAllMessageListApi(lastIdRead: Int = Int.MAX_VALUE) {
        try {
            val response = api.getMessages(lastIdRead, LIMIT)
            localData.addAll(response)
            data.postValue(localData)
            insertInDB(response)
            if (response.isNotEmpty()) {
                println(localData[localData.lastIndex].id!!.toInt())
                getAllMessageListApi(localData[localData.lastIndex].id!!.toInt())
            } else {
                maxIdRead = localData[0].id!!.toInt()
            }
        } catch (ex : ConnectException) {}
    }

    suspend fun getNewMessages(actualId : Int = Int.MAX_VALUE) {
        try {
            val tmpList = mutableListOf<Message>()
            val response = api.getMessages(actualId, LIMIT)
            for (i in response) {
                if (i.id?.toInt()!! > maxIdRead) {
                    tmpList.add(i)
                } else {
                    break
                }
            }
            if (dbShown) {
                localData.addAll(0, tmpList)
                data.postValue(localData)
            }
            insertInDB(tmpList)
            if (tmpList.size == LIMIT) {
                getNewMessages(tmpList[tmpList.lastIndex].id!!.toInt())
            } else {
                if (localData.isNotEmpty()) {
                    maxIdRead = localData[0].id?.toInt()!!
                }
                if (!dbShown) {
                    showDB()
                }
            }
        } catch (ex : ConnectException) {
            if (!dbShown) {
                showDB()
            }
        }
    }

    private suspend fun getAllMessageListDB() {
        maxIdRead = db.allMessage[db.allMessage.lastIndex]!!.uid
        dbShown = false
        getNewMessages()
    }

    private fun showDB() {
        for (i in db.allMessage) {
            val tmpData : Data = if (i!!.url == null) {
                Data(SomeData(i.text, null), null)
            } else {
                Data(null, SomeData(null, i.url))
            }
            localData.add(0, Message(
                i.uid.toString(),
                i.from,
                i.to,
                tmpData,
                "i forgot about time"
            ))
            if (localData.size % 100 == 0) {
                data.postValue(localData)
            }
        }
        dbShown = true
    }

    private fun insertInDB(lst : List<Message>) {
        for (i in lst) {
            db.insert(MessageDB(
                i.id!!.toInt(),
                i.from.toString(),
                i.to.toString(),
                i.data.Text?.text,
                i.data.Image?.link
            ))
        }
    }

    fun postMessageText(text : String) {
        api.sendMessage(Message(
            null,
            USER_NAME,
            CHANNEL_NAME,
            Data(SomeData(text, null), null),
            null
        )).enqueue(
            object : Callback<ResponseBody?> {
                override fun onResponse(
                    call: Call<ResponseBody?>,
                    response: Response<ResponseBody?>
                )  {
                    errorHandler(response)
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    Log.e("Upload error:", t.message!!)
                }
            }
        )
    }

    fun postImage(file: File, type: String) {
        val description: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            "{\"from\":\"l\"," +
                    "\"to\":\"1@channel\"," +
                    "\"data\":{\"Image\":{\"link\":\"${file.absolutePath}\"}}}"
        )

        val requestFile: RequestBody = RequestBody.create(
            MediaType.parse(type),
            file
        )

        val body = MultipartBody.Part.createFormData(
            "picture",
            file.name,
            requestFile
        )

        val call: Call<ResponseBody> = api.sendImage(description, body)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                response: Response<ResponseBody?>
            ) {
                errorHandler(response)
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("Upload error:", t.message!!)
            }
        })
    }

    fun errorHandler(response: Response<ResponseBody?>) {
        if (response.code() >= 500) {
            System.err.println("Response code " + response.code().toString() + " The error was caused by the server")
        } else if (response.code() == 413) {
            System.err.println("Response code " + response.code().toString() + " Message too big for sending")
        } else if (response.code() == 409) {
            System.err.println("Response code " + response.code().toString() + " Try one more time")
        } else if (response.code() == 404) {
            System.err.println("Response code " + response.code().toString() + " The server cannot find the requested resource")
        }
    }
}