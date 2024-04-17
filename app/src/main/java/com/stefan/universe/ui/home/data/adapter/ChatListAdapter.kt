package com.stefan.universe.ui.home.data.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.stefan.universe.R
import com.stefan.universe.common.utils.DateUtils
import com.stefan.universe.databinding.RowChatListBinding
import com.stefan.universe.ui.chat.data.model.ChatWrapper
import com.stefan.universe.ui.main.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class ChatListAdapter(
    private val context: Context,
    private var currentUser: User,
    private val chatClickListener: ChatClickListener
) : ListAdapter<ChatWrapper, ChatListAdapter.UserListViewHolder>(DiffCallback()) {

    inner class UserListViewHolder(
        val binding: RowChatListBinding
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun swapData(currentUser: User) {
        this.currentUser = currentUser
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
        val binding = RowChatListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
        with(holder) {
            with(getItem(position)) {
                binding.textViewDelete.setOnClickListener {
                    chatClickListener.onDeleteChatClicked(this.chat.id)
                }
                binding.root.setOnClickListener { chatClickListener.onChatClicked(this.chat.id) }
                CoroutineScope(Main).launch {
                    Glide.with(context)
                        .load(this@with.recieverPhotoUri())
                        .placeholder(R.drawable.app_logo)
                        .fitCenter()
                        .into(binding.userIcon)
                    binding.userName.text = this@with.recieverDisplayName()
                }
                binding.latestMessage.text = this.chat.latestMessage.text.ifEmpty { "Nothing yet" }
                binding.timestamp.text =
                    if (this.chat.latestMessage.text.isNotEmpty()) DateUtils.getTime(this.chat.latestMessage.timestamp) else ""
            }
        }
    }
}

class DiffCallback : DiffUtil.ItemCallback<ChatWrapper>() {
    override fun areItemsTheSame(oldItem: ChatWrapper, newItem: ChatWrapper): Boolean {
        return oldItem.chat.id == newItem.chat.id
    }

    override fun areContentsTheSame(oldItem: ChatWrapper, newItem: ChatWrapper): Boolean {
        return oldItem == newItem
    }
}

interface ChatClickListener {
    fun onChatClicked(chatId: String)
    fun onDeleteChatClicked(chatId: String)
}