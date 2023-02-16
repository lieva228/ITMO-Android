package com.callmydd.mvvm.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.callmydd.mvvm.model.Message
import com.callmydd.mvvm.repository.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class MainViewModel @Inject constructor(private val repository: Repository) : ViewModel() {

    var liveDataMessageList: LiveData<List<Message>?> = repository.data.also {
        CoroutineScope(Dispatchers.IO).launch {
            repository.getMessages()
        }
    }

    fun updateData() {
        CoroutineScope(Dispatchers.IO).launch {
            repository.getNewMessages()
        }
    }

    fun postImage(file: File, type: String) {
        repository.postImage(file, type)
    }

    fun postMessageText(text : String) {
        repository.postMessageText(text)
    }

}
