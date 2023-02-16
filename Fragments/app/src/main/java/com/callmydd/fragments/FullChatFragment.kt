package com.callmydd.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.android.synthetic.main.chat_list_item.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class FullChatFragment : Fragment() {
    private lateinit var chatPath: String
    lateinit var viewModel: MainViewModel
    private lateinit var adapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatPath = arguments?.getString(KEY_CHAT_PATH) ?: ""
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        var str = "database_saved_text_for_channel_3_$chatPath"
        if (chatPath.startsWith("Private chat with: ")) {
            str = "database_saved_text_for_private_chat7"
        }
        viewModel.db = Room.databaseBuilder(
            requireContext().applicationContext,
            AppDatabase::class.java, str
        ).build()
        viewModel.chatPath = chatPath
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.chat_list_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        swpr.setOnRefreshListener {
            viewModel.getMessages(true)
            swpr.isRefreshing = false
        }
        btn.setOnClickListener {
            viewModel.postMessageText(edittext.text.toString())
            edittext.text.clear()
        }
        btn2.setOnClickListener {
            sendImageFromGallery()
        }
        viewModel.adapter = adapter
        viewModel.getFromDB()
    }

    private fun sendImageFromGallery() = sendImageFromGalleryResult.launch("image/*")

    private val sendImageFromGalleryResult = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            sendImage(uri)
        }
    }

    private fun sendImage(uri: Uri) {
        val service = RetrofitBuilder.apiService
        val `in`: InputStream = requireContext().contentResolver.openInputStream(uri)!!
        val file = File("${requireContext().filesDir}/${System.currentTimeMillis()}.jpg")
        val out: OutputStream = FileOutputStream(file)
        val buf = ByteArray(1024)
        var len: Int
        while (`in`.read(buf).also { len = it } > 0) {
            out.write(buf, 0, len)
        }
        out.close()
        `in`.close()

        var str = chatPath
        if (chatPath.startsWith("Private chat with: ")) {
            str = str.substring(19)
        }

        val description: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            "{\"from\":\"l\"," +
                    "\"to\":\"$str\"," +
                    "\"data\":{\"Image\":{\"link\":\"${file.absolutePath}\"}}}"
        )

        val requestFile: RequestBody = RequestBody.create(
            MediaType.parse(requireContext().contentResolver.getType(uri)!!),
            file
        )

        val body = MultipartBody.Part.createFormData(
            "picture",
            file.name,
            requestFile
        )

        val call: Call<ResponseBody> = service.sendImage(description, body)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(
                call: Call<ResponseBody?>,
                response: Response<ResponseBody?>
            ) {
                if (response.code() >= 500) {
                    System.err.println("Response code " + response.code().toString() + " The error was caused by the server")
                } else if (response.code() == 413) {
                    System.err.println("Response code " + response.code().toString() + " Image too big for sending")
                } else if (response.code() == 409) {
                    sendImage(uri)
                } else if (response.code() == 404) {
                    System.err.println("Response code " + response.code().toString() + " The server cannot find the requested resource")
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("Upload error:", t.message!!)
            }
        })
    }

    private fun setupUI() {
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = MainAdapter(viewModel.list, chatPath) {
            if (it.data.Image != null) {
                val intent = Intent(context, ImageActivity::class.java)
                intent.putExtra("url", "http://213.189.221.170:8008/thumb/" + it.data.Image.link)
                startActivity(intent)
            }
        }
        viewModel.adapter = adapter
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                (recyclerView.layoutManager as LinearLayoutManager).orientation
            )
        )
        recyclerView.adapter = adapter
    }

    companion object {
        private const val KEY_CHAT_PATH = "chatPath"

        fun create(imagePath: String): FullChatFragment {
            return FullChatFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_CHAT_PATH, imagePath)
                }
            }
        }
    }
}