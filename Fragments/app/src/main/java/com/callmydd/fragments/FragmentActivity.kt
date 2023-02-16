package com.callmydd.fragments

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.transition.Fade
import android.transition.TransitionInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager

class FragmentActivity : AppCompatActivity(), ChatPathClickListener {
    private var fragmentExtraContainer: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.fragment_activity)
        checkAndRequestPermissions(this)

        fragmentExtraContainer = findViewById(R.id.fragment_extra_container)

        val upEnabled = fragmentExtraContainer == null
        supportActionBar?.setDisplayShowHomeEnabled(upEnabled)
        supportActionBar?.setDisplayHomeAsUpEnabled(upEnabled)

        if (savedInstanceState == null) { // First launch
            supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, ChatsListFragment(), TAG_LIST)
                .commit()
        } else if (fragmentExtraContainer != null) { // Portrait -> Landscape
            val imageFragment = supportFragmentManager.findFragmentByTag(TAG_FULLSCREEN)

            if (imageFragment != null) { // Мы открывали фулскрин и у нас есть транзакция в бэкстеке
                supportFragmentManager.popBackStack(
                    FULLSCREEN_BACK_STACK,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                supportFragmentManager.executePendingTransactions()

                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_extra_container, imageFragment, TAG_FULLSCREEN)
                    .commit()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (supportFragmentManager.backStackEntryCount > 0) {
                    supportFragmentManager.popBackStack()
                } else {
                    finish()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onChatClick(channelName: String, root: View) {
        val chatFragment = FullChatFragment.create(channelName)

        if (fragmentExtraContainer != null) {
            chatFragment.enterTransition = Fade().apply { duration = 500L }
            chatFragment.exitTransition = Fade().apply { duration = 500L }

            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragment_extra_container,
                    chatFragment,
                    TAG_FULLSCREEN
                )
                .commit()
        } else {
            val translation = TransitionInflater
                .from(this)
                .inflateTransition(R.transition.shared_image_transition)

            supportFragmentManager.findFragmentByTag(TAG_LIST)?.let {
                it.exitTransition = Fade().apply {
                    duration = translation.duration
                    interpolator = translation.interpolator
                    addTarget(CardView::class.java)
                    excludeTarget(root, true)
                    addTarget(root.findViewById<View>(R.id.title))
                }
            }

            chatFragment.sharedElementEnterTransition = translation

            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragment_container,
                    chatFragment,
                    TAG_FULLSCREEN
                )
                .addToBackStack(FULLSCREEN_BACK_STACK)
                .commit()
        }
    }

    fun checkAndRequestPermissions(context: Activity?): Boolean {
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

    companion object {
        private const val TAG_LIST = "list"
        private const val TAG_FULLSCREEN = "fullscreen"
        private const val FULLSCREEN_BACK_STACK = "fullscreen_back_stack"
    }
}