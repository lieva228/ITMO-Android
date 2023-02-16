package com.callmydd.mvvm.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.callmydd.mvvm.R
import com.callmydd.mvvm.model.Message
import com.callmydd.mvvm.utils.BASE_URL
import com.callmydd.mvvm.viewModel.MainViewModel
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: MainAdapter
    private lateinit var swipeContainer: SwipeRefreshLayout
    private lateinit var send: TextView
    private lateinit var getImage: TextView
    private var lst : MutableList<Message> = Collections.synchronizedList(mutableListOf())

    @set:Inject
    var viewModelFactory: ViewModelProvider.Factory? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AndroidInjection.inject(this)
        checkAndRequestPermissions()
        viewModel = ViewModelProvider(this, viewModelFactory!!)[MainViewModel::class.java]
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MainAdapter(lst) {
            if (it.data.Image != null) {
                val intent = Intent(this@MainActivity, FullScreenActivity::class.java)
                intent.putExtra("url", BASE_URL + "thumb/" + it.data.Image.link)
                startActivity(intent)
            }
        }
        recyclerView.adapter = adapter
        observeViewModel(viewModel)
        swipeContainer = findViewById(R.id.swpr)
        swipeContainer.setOnRefreshListener {
            viewModel.updateData()
            swipeContainer.isRefreshing = false
        }

        send = findViewById(R.id.btn)
        send.setOnClickListener {
            val editText = findViewById<EditText>(R.id.edittext)
            viewModel.postMessageText(editText.text.toString())
            editText.text.clear()
        }
        getImage = findViewById(R.id.btn2)
        getImage.setOnClickListener {
            if (checkAndRequestPermissions()) {
                sendImageFromGallery()
            }
        }
    }

    private fun sendImageFromGallery() = sendImageFromGalleryResult.launch("image/*")

    private val sendImageFromGalleryResult = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
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
            viewModel.postImage(file, contentResolver.getType(uri)!!)
        }
    }

    private fun observeViewModel(viewModel: MainViewModel) {
        viewModel.liveDataMessageList.observe(this
        ) { projects ->
            if (projects != null) {
                lst.clear()
                lst.addAll(projects)
                adapter.notifyDataSetChanged()
            }
            println("observed")
        }
    }

    private fun checkAndRequestPermissions() : Boolean {
        val wExtortPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (wExtortPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1)
        }
        if (Build.VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                val getPermission = Intent()
                getPermission.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivity(getPermission)
            }
        }
        if (wExtortPermission != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

}