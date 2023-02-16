package com.callmydd.messenger

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.*
import androidx.room.Entity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


class MainActivity : AppCompatActivity() {

    data class Message(
        val id : Int,
        val time: String,
        val name: String,
        val text: String?= null,
        var img: Bitmap?= null,
        val url: String?= null
    )

    @Entity
    data class MessageForDB(
        @PrimaryKey val uid : Int,
        @ColumnInfo val time: String,
        @ColumnInfo val name: String,
        @ColumnInfo val text: String?= null,
        @ColumnInfo val url: String? = null
    )

    lateinit var swipeContainer: SwipeRefreshLayout
    private var idx = 1
    private var end = false
    private lateinit var mService: MainService
    private var mBound: Boolean = false
    var mMyServiceIntent : Intent? = null
    private var sendIntent : Intent? = null

    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            end = mService.end
            idx = mService.lastId
            updateRecycler()
            if (mService.getNew) {
                if (!end) {
                    startService(mMyServiceIntent?.putExtra("idx", idx))
                } else {
                    swipeContainer.isRefreshing = false
                }
            }
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName, p1: IBinder) {
            val binder: MainService.MyBinder =
                p1 as MainService.MyBinder
            mService = binder.getService()
            mBound = true
            startChat()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkAndRequestPermissions(this)

        mMyServiceIntent = Intent(this@MainActivity, MainService::class.java)
        sendIntent = Intent(this@MainActivity, SendService::class.java)

        swipeContainer = findViewById(R.id.swpr)
        swipeContainer.setOnRefreshListener {
            getPartOfMessage()

            swipeContainer.isRefreshing = false
        }

        val edittext = findViewById<EditText>(R.id.edittext)
        val btn = findViewById<Button>(R.id.btn)
        btn.setOnClickListener {
            //here send using service
            startService(sendIntent?.putExtra("type", "text")
                ?.putExtra("text", edittext.text.toString())
                ?.putExtra("path", ""))
            edittext.text.clear()
        }

        val imgSend = findViewById<Button>(R.id.imgsend)
        imgSend.setOnClickListener {
            if (checkAndRequestPermissions(this)) {
                selectImageFromGallery()
            }
        }

        if (savedInstanceState != null) {
            idx = savedInstanceState.getInt("idx")
            end = savedInstanceState.getBoolean("end")
            mBound = savedInstanceState.getBoolean("mBound")
            updateRecycler()
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

    private fun selectImageFromGallery() = selectImageFromGalleryResult.launch("image/*")

    private val selectImageFromGalleryResult = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val `in`: InputStream? = contentResolver.openInputStream(uri)
            val tmpFile = File("${filesDir}/${System.currentTimeMillis()}.jpg")
            val out: OutputStream = FileOutputStream(tmpFile)
            val buf = ByteArray(1024)
            var len: Int
            while (`in`?.read(buf).also { len = it!! }!! > 0) {
                out.write(buf, 0, len)
            }
            out.close()
            `in`?.close()
            startService(sendIntent?.putExtra("type", "img")
                ?.putExtra("text", "")
                ?.putExtra("path", tmpFile.absolutePath))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("Save", idx.toString())
        outState.putInt("idx", idx)
        outState.putBoolean("end", end)
        outState.putBoolean("mBound", mBound)
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(this, MainService::class.java)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        mBound = false
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateRecycler() {
        val myRecyclerView : RecyclerView = findViewById(R.id.recyler)
        myRecyclerView.adapter?.notifyDataSetChanged()
    }

    private fun startChat() {
        swipeContainer.isRefreshing = true
        val viewManager = LinearLayoutManager(this)
        val myRecyclerView : RecyclerView = findViewById(R.id.recyler)
        myRecyclerView.apply {
            layoutManager = viewManager
            adapter = MessageAdapter(mService.listOfMessages) {
                if (it.img != null) {
                    val intent = Intent(this@MainActivity, ImageActivity::class.java)
                    intent.putExtra("url", it.url)
                    startActivity(intent)
                }
            }
        }
        getPartOfMessage()
    }

    private fun getPartOfMessage() {
        Log.d("index Start Change config", idx.toString())
        startService(mMyServiceIntent?.putExtra("idx", idx))

        val intentFilter = IntentFilter("MessageService")
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT)
        registerReceiver(broadcastReceiver, intentFilter)
    }

    class MessageViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        private val nameView: TextView = root.findViewById(R.id.name)
        private val textView: TextView = root.findViewById(R.id.text)
        private val timeView: TextView = root.findViewById(R.id.time)
        val imgView: ImageView = root.findViewById(R.id.img)

        fun bind(message: Message) {
            if(message.text != null) {
                nameView.text = message.name
                textView.text = message.text
                timeView.text = message.time
                imgView.setImageBitmap(null)
            } else if (message.img != null) {
                nameView.text = message.name
                timeView.text = message.time
                imgView.setImageBitmap(message.img)
                textView.text = null
            }
        }
    }

    class MessageAdapter(
        private val messages: MutableList<Message>,
        private val onClick: (Message) -> Unit
    ): RecyclerView.Adapter<MessageViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val holder = MessageViewHolder(
                LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.list_item, parent, false)
            )

            holder.imgView.setOnClickListener {
                onClick(messages[holder.absoluteAdapterPosition])
            }
            return holder
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) = holder.bind(messages[position])

        override fun getItemCount() = messages.size
    }

    @Dao
    interface MessageDao {
        @Insert
        fun insert(vararg message: MessageForDB?)

        @get:Query("SELECT * FROM MessageForDB")
        val allPeople: List<MessageForDB?>?
    }

    @Database(
        entities = [MessageForDB::class],
        version = 1
    )
    abstract class AppDatabase : RoomDatabase() {
        abstract val messageDao: MessageDao?
    }

}