package com.callmydd.fragments

import android.view.View

interface ChatPathClickListener {
    fun onChatClick(channelName: String, root: View)
}