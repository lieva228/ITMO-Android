package com.callmydd.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room


class ChatsListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChannelsAdapter
    private lateinit var viewModel: ChannelsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[ChannelsViewModel::class.java]
        viewModel.db = Room.databaseBuilder(
            requireContext().applicationContext,
            AppDatabase::class.java, "database_saved_text_for_private_chat7"
        ).build()
        adapter = ChannelsAdapter(viewModel.listOfChats) { path, root ->
            (context as ChatPathClickListener).onChatClick(path, root)}
        viewModel.adapter = adapter
        if (viewModel.list.isEmpty()) {
            viewModel.getChannels()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chats_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context

        recyclerView = view.findViewById(R.id.recycler)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

}