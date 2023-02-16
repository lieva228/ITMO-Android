package com.callmydd.fragments

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.util.*

class ChannelsViewModel : ViewModel() {
    private var lastInd = 100000
    private var maxID = 100000
    var list : MutableList<Message> = Collections.synchronizedList(mutableListOf())
    lateinit var db : AppDatabase
    var getNew = false
    private var lastFromDB = 0
    lateinit var adapter: ChannelsAdapter
    var listOfChats : MutableList<String> = Collections.synchronizedList(mutableListOf())

    fun getChannels() {
        val service = RetrofitBuilder.apiService
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.getChannels()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        listOfChats.addAll(response.body()!!)
                        retrieverList()
                    }
                }
            } catch (e : ConnectException) {}
            getFromDBPrivateChat()
        }
    }

    fun getFromDBPrivateChat() {
        CoroutineScope(Dispatchers.IO).launch {
            val listOfDB = db.messageDao?.allPeople!!
            getNew = false
            if (listOfDB.isNotEmpty()) {
                for (i in listOfDB.size - 1 downTo 0) {
                    println("from : " + listOfDB[i]!!.name)
                    if (!listOfChats.contains(listOfDB[i]!!.name) && !listOfChats.contains("Private chat with: " + listOfDB[i]!!.name) && listOfDB[i]!!.name != "l") {
                        listOfChats.add("Private chat with: " + listOfDB[i]!!.name)
                    } else if (!listOfChats.contains(listOfDB[i]!!.time) && !listOfChats.contains("Private chat with: " + listOfDB[i]!!.time) && listOfDB[i]!!.name != "l") {
                        listOfChats.add("Private chat with: " + listOfDB[i]!!.time)
                    }
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
                }
                withContext(Dispatchers.Main) {
                    retrieverList()
                }
                maxID = listOfDB[listOfDB.size - 1]!!.uid
                getMessagesForPrivateChat(true)
            } else {
                getMessagesForPrivateChat()
            }
            db.close()
        }
    }

    fun getMessagesForPrivateChat(new : Boolean = false) {
        if (new) {
            lastInd = 10000
        } else {
            maxID = 2
        }
        val service = RetrofitBuilder.apiService
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.getMessagesPrivateChats(lastInd, 100)
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
                        lastInd = list[list.size - 1].id!!.toInt()
                    }
                }
                for (i in tmpList) {
                    println("online from : " + i.from)
                    if (!listOfChats.contains(i.from) && !listOfChats.contains("Private chat with: " + i.from) && i.from != "l") {
                        listOfChats.add("Private chat with: " + i.from.toString())
                    } else if (!listOfChats.contains(i.to) && !listOfChats.contains("Private chat with: " + i.to) && i.from != "l") {
                        listOfChats.add("Private chat with: " + i.to.toString())
                    }
                    val msgDB = MessageForDB(
                        i.id!!.toInt(),
                        i.to.toString(),
                        i.from.toString(),
                        i.data.Text?.text,
                        i.data.Image?.link
                    )
                    db.messageDao?.insert(msgDB)
                }
                withContext(Dispatchers.Main) {
                    retrieverList()
                }
                if (lastInd > maxID) {
                    getMessagesForPrivateChat(new)
                } else {
                    maxID = list[0].id!!.toInt()
                }
            } catch (e : ConnectException) {}
        }
    }

    private fun retrieverList() {
        adapter.apply {
            notifyDataSetChanged()
        }
    }

}