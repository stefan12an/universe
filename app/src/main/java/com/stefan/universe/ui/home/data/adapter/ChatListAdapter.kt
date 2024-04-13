package com.stefan.universe.ui.home.data.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.stefan.universe.databinding.RowChatListBinding
import com.stefan.universe.ui.home.data.model.Chat

class ChatListAdapter(
    private val context: Context,
    private var chatList: List<Chat>
) : RecyclerView.Adapter<ChatListAdapter.UserListViewHolder>() {

    inner class UserListViewHolder(
        val binding: RowChatListBinding
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun swapData(newList: List<Chat>) {
        this.chatList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
        val binding = RowChatListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserListViewHolder(binding)
    }

    override fun getItemCount(): Int = chatList.size

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
        with(holder) {
            with(chatList[position]) {
                binding.userName.text = this.name
                binding.latestMessage.text = this.latestMessage
            }
        }
    }
}