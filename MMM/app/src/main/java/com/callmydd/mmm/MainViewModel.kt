package com.callmydd.mmm

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.ConnectException
import java.util.*

class MainViewModel : ViewModel() {
    lateinit var adapter: MainAdapter
    private var lastInd = 100000
    private var maxID = 100000
    var list : MutableList<Message> = Collections.synchronizedList(mutableListOf())
    lateinit var db : AppDatabase
    var getNew = false
    private var lastFromDB = 0

    fun getMessages(new : Boolean = false) {
        if (new) {
            lastInd = 10000
        } else {
            maxID = 2
        }
        val service = RetrofitBuilder.apiService
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.getMessages(lastInd, 100)
                val tmpList = mutableListOf<Message>()
                for (i in response.body()!!) {
                    if (i.id?.toInt()!! > maxID) {
                        tmpList.add(i)
                    } else {
                        break
                    }
                }
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        if (new) {
                            list.addAll(0, tmpList)
                        } else {
                            list.addAll(response.body()!!)
                        }
                        retrieverList()
                        lastInd = list[list.size - 1].id!!.toInt()
                    }
                }
                //add to database
                for (i in tmpList) {
                    val msgDB = MessageForDB(
                        i.id!!.toInt(),
                        i.time.toString(),
                        i.from.toString(),
                        i.data.Text?.text,
                        i.data.Image?.link
                    )
                    db.messageDao?.insert(msgDB)
                }
                //add end
                if (lastInd > maxID) {
                    getMessages(new)
                } else {
                    maxID = list[0].id!!.toInt()
                    println("stop")
                }
            } catch (e : ConnectException) {}
        }
    }

    fun postMessageText(text: String) {
        val service = RetrofitBuilder.apiService
        service.sendMessage(Message(null, "l", null, Data(SomeData(text, null), null), null)).enqueue(
            object : Callback<Message> {
                override fun onResponse(call: Call<Message>?, response: Response<Message>) {
                    if (response.isSuccessful) {
                        println(response.body().toString())
                    } else {
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<Message>, t: Throwable) {
                    Log.e("Upload error:", t.message!!)
                }
            }
        )
    }

    fun getFromDB() {
        CoroutineScope(Dispatchers.IO).launch {
            val listOfDB = db.messageDao?.allPeople!!
            getNew = false
            if (listOfDB.isNotEmpty()) {
                for (i in listOfDB.size - 1 downTo 0) {
                    if (listOfDB[i]!!.url == null) {
                        list.add(
                            Message(
                                listOfDB[i]!!.uid.toString(),
                                listOfDB[i]!!.name,
                                listOfDB[i]!!.time,
                                Data(SomeData(listOfDB[i]!!.text, null), null),
                                listOfDB[i]!!.url
                            )
                        )
                    } else {
                        list.add(
                            Message(
                                listOfDB[i]!!.uid.toString(),
                                listOfDB[i]!!.name,
                                listOfDB[i]!!.time,
                                Data(null, SomeData(null, listOfDB[i]!!.url)),
                                listOfDB[i]!!.url
                            )
                        )
                    }
                    lastFromDB = i + 1
                    if (i % 100 == 0) {
                        withContext(Dispatchers.Main) {
                            retrieverList()
                        }
                    }
                }
                maxID = listOfDB[listOfDB.size - 1]!!.uid
//                getUsers(true)
            } else {
                getMessages()
            }
        }
    }

    private fun retrieverList() {
        adapter.apply {
            notifyDataSetChanged()
        }
    }

}