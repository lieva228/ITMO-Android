package com.callmydd.fragments

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
import java.util.Collections.max

class MainViewModel : ViewModel() {
    lateinit var adapter: MainAdapter
    private var lastInd = 100000
    private var maxID = 100000
    var list : MutableList<Message> = Collections.synchronizedList(mutableListOf())
    lateinit var db : AppDatabase
    var getNew = false
    var chatPath : String = ""
    private var lastFromDB = 0

    fun getMessages(new : Boolean = false) {
        if (chatPath.startsWith("Private chat with: ")) {
            getForPrivateChat(new)
        } else {
            if (new) {
                lastInd = 10000
            } else {
                maxID = 2
            }
            val service = RetrofitBuilder.apiService
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (maxID == 2) {
                        maxID = service.getMessagesFromChannel(chatPath, 0, 1, false).body()!![0].id!!.toInt()
                    }
                    val response = service.getMessagesFromChannel(chatPath, lastInd, 100)
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
                    if (lastInd > maxID) {
                        getMessages(new)
                    } else {
                        maxID = list[0].id!!.toInt()
                        println("stop")
                    }
                } catch (e : ConnectException) {}
            }
        }
    }

    fun postMessageText(text: String) {
        val service = RetrofitBuilder.apiService
        var str = chatPath
        if (str.startsWith("Private chat with: ")) {
            str = str.substring(19)
        }
        service.sendMessage(Message(null, "l", str, Data(SomeData(text, null), null), null)).enqueue(
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

    private fun getForPrivateChat(new : Boolean = false) {
        if (new) {
            lastInd = 10000
        } else {
            maxID = 2
        }
        val service = RetrofitBuilder.apiService
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (maxID == 2) {
                    maxID = service.getMessagesPrivateChats( 0, 1, false).body()!![0].id!!.toInt()
                }
                val response = service.getMessagesPrivateChats(lastInd, 100)
                val tmpList = mutableListOf<Message>()
                for (i in response.body()!!) {
                    lastInd = kotlin.math.min(lastInd, i.id!!.toInt())
                    if (i.id.toInt() > maxID) {
                        if ((i.from == "l" && i.to == chatPath.substring(19)) ||
                            (i.to == "l" && i.from == chatPath.substring(19))
                        ) {
                            tmpList.add(i)
                        }
                    } else {
                        break
                    }
                }
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        if (new) {
                            list.addAll(0, tmpList)
                        } else {
                            for (i in response.body()!!) {
                                if ((i.from == "l" && i.to == chatPath.substring(19)) ||
                                    (i.to == "l" && i.from == chatPath.substring(19))
                                ) {
                                    list.add(i)
                                }
                            }
                        }
                        retrieverList()
//                        lastInd = list[list.size - 1].id!!.toInt()
                    }
                }
                if (lastInd > maxID) {
                    getMessages(new)
                } else {
                    maxID = list[0].id!!.toInt()
                    println("stop")
                }
            } catch (e : ConnectException) {}
        }
    }

    fun getFromDB() {
        if (chatPath.startsWith("Private chat with: ")) {
            getForPrivateChat()
        } else {
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
                    maxID = listOfDB[listOfDB.size - 1]!!.uid + 1
                } else {
                    getMessages()
                }
                db.close()
            }
        }
    }

    private fun retrieverList() {
        adapter.apply {
            notifyDataSetChanged()
        }
    }

}