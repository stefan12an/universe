package com.stefan.universe.ui.chat.data.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.stefan.universe.R
import com.stefan.universe.databinding.RowUserListBinding
import com.stefan.universe.ui.main.data.model.User

class UserListAdapter(
    private val context: Context,
    private var userList: List<User>,
    private val listener: UserListListener
) : RecyclerView.Adapter<UserListAdapter.UserListViewHolder>() {

    inner class UserListViewHolder(
        val binding: RowUserListBinding
    ) : RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun swapData(newList: List<User>) {
        this.userList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
        val binding = RowUserListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserListViewHolder(binding)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
        with(holder) {
            with(userList[position]) {
                binding.root.setOnClickListener { listener.onUserClicked(this) }
                binding.userName.text = this.displayName
                Glide.with(context).load(this.photoUri).placeholder(
                    AppCompatResources.getDrawable(context, R.drawable.app_logo)
                ).into(binding.userIcon)
                binding.accountStatus.setImageDrawable(
                    if (this.emailVerified) {
                        AppCompatResources.getDrawable(context, R.drawable.ic_check)
                    } else {
                        AppCompatResources.getDrawable(context, R.drawable.ic_close)
                    }
                )
            }
        }
    }
}

interface UserListListener {
    fun onUserClicked(user: User)
}