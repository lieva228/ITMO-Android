package com.callmydd.mmm

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.callmydd.mmm.R.*
import kotlinx.android.synthetic.main.activity_main.*
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

class MainActivity : AppCompatActivity() {

    lateinit var viewModel: MainViewModel
    private lateinit var adapter: MainAdapter
    lateinit var swipeContainer: SwipeRefreshLayout
    private lateinit var send: TextView
    private lateinit var getImage: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)
        checkAndRequestPermissions(this)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database_saved_text_MMM4"
        ).build()
        setupUI()
        if (viewModel.list.size == 0) {
            if (!viewModel.getNew) {
                viewModel.getFromDB()
            } else {
                viewModel.getMessages()
            }
        }

        swipeContainer = findViewById(id.swpr)
        swipeContainer. setColorSchemeResources(color.colorPrimary)
        swipeContainer.setOnRefreshListener {
            viewModel.getMessages(true)
            swipeContainer.isRefreshing = false
        }
        send = findViewById(id.btn)
        send.setOnClickListener {
            val editText = findViewById<EditText>(id.edittext)
            viewModel.postMessageText(editText.text.toString())
            editText.text.clear()
        }
        getImage = findViewById(id.btn2)
        getImage.setOnClickListener {
            if (checkAndRequestPermissions(this)) {
                sendImageFromGallery()
            }
        }
    }

    private fun checkAndRequestPermissions(context: Activity?): Boolean {
        val wExtortPermission = ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (wExtortPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1)
        }
        if (wExtortPermission != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    private fun sendImageFromGallery() = sendImageFromGalleryResult.launch("image/*")

    private val sendImageFromGalleryResult = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            sendImage(uri)
        }
    }

    private fun sendImage(uri: Uri) {
        val service = RetrofitBuilder.apiService
        val `in`: InputStream? = contentResolver.openInputStream(uri)
        val file = File("${filesDir}/${System.currentTimeMillis()}.jpg")
        val out: OutputStream = FileOutputStream(file)
        val buf = ByteArray(1024)
        var len: Int
        while (`in`?.read(buf).also { len = it!! }!! > 0) {
            out.write(buf, 0, len)
        }
        out.close()
        `in`?.close()

        val description: RequestBody = RequestBody.create(
            MediaType.parse("application/json"),
            "{\"from\":\"l\"," +
                    "\"to\":\"1@channel\"," +
                    "\"data\":{\"Image\":{\"link\":\"${file.absolutePath}\"}}}"
        )

        val requestFile: RequestBody = RequestBody.create(
            MediaType.parse(contentResolver.getType(uri)!!),
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
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MainAdapter(viewModel.list) {
            if (it.data.Image != null) {
                val intent = Intent(this@MainActivity, ImageActivity::class.java)
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
}
