package com.stefan.universe.ui.chat.data.adapter

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.stefan.universe.common.utils.DateUtils
import com.stefan.universe.databinding.DayBannerRowChatBinding
import com.stefan.universe.databinding.ReceiverRowChatBinding
import com.stefan.universe.databinding.SenderRowChatBinding
import com.stefan.universe.ui.chat.data.model.ChatWrapper
import com.stefan.universe.ui.chat.data.model.FirebaseMessageModel
import com.stefan.universe.ui.chat.data.model.Message

class ChatAdapter(
    private var chatWrapper: ChatWrapper,
    private val listener: ChatAdapterListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENDER = 1
        private const val VIEW_TYPE_RECEIVER = 2
        private const val VIEW_TYPE_DAY_BANNER = 3
    }

    // Add a new ViewHolder for the day banner
    inner class DayBannerViewHolder(val binding: DayBannerRowChatBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class SenderViewHolder(val binding: SenderRowChatBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class ReceiverViewHolder(val binding: ReceiverRowChatBinding) :
        RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun swapData(chatWrapper: ChatWrapper) {
        val newMessages = mutableListOf<Message>()
        val oldMessages = chatWrapper.chat.messages

        oldMessages.forEachIndexed { index, message ->
            if (index == 0 || DateUtils.isNewDay(
                    oldMessages[index - 1].timestamp,
                    message.timestamp
                )
            ) {
                newMessages.add(FirebaseMessageModel(timestamp = message.timestamp)) // Add banner for each new day
            }
            newMessages.add(message)
        }

        this.chatWrapper = chatWrapper.copyWith(chatWrapper.chat.copyWith(messages = newMessages))
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SENDER -> {
                val binding = SenderRowChatBinding.inflate(inflater, parent, false)
                SenderViewHolder(binding)
            }

            VIEW_TYPE_RECEIVER -> {
                val binding = ReceiverRowChatBinding.inflate(inflater, parent, false)
                ReceiverViewHolder(binding)
            }

            VIEW_TYPE_DAY_BANNER -> {
                val binding = DayBannerRowChatBinding.inflate(inflater, parent, false)
                DayBannerViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = chatWrapper.chat.messages.size

    override fun getItemViewType(position: Int): Int {
        val message = chatWrapper.chat.messages[position]

        return when {
            message.isBanner -> VIEW_TYPE_DAY_BANNER
            chatWrapper.isSenderCurrentUser(message) -> VIEW_TYPE_SENDER
            else -> VIEW_TYPE_RECEIVER
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = chatWrapper.chat.messages[position]
        when (holder) {
            is SenderViewHolder -> bindSenderViewHolder(holder, message, position)
            is ReceiverViewHolder -> bindReceiverViewHolder(holder, message, position)
            is DayBannerViewHolder -> {
                holder.binding.date.text =
                    DateUtils.getDayOrDate(message.timestamp)
            }
        }
    }

    private fun bindSenderViewHolder(holder: SenderViewHolder, message: Message, position: Int) {
        with(holder.binding) {
            cardGchatMessageMe.setOnLongClickListener {
                listener.onMessageLongClicked(
                    it,
                    position,
                    message.id,
                    Gravity.END,
                    UserType.SENDER
                )
                false
            }
            reactionContainer.visibility =
                if (message.reactions.isNotEmpty()) View.VISIBLE else View.GONE
            reactions.text = message.reactions.map { it.data }.toSet()
                .joinToString(" ")
            this.message.text = message.text
            timestamp.text = DateUtils.getTime(message.timestamp)
        }
    }

    private fun bindReceiverViewHolder(
        holder: ReceiverViewHolder,
        message: Message,
        position: Int
    ) {
        with(holder.binding) {
            cardGchatMessageOther.setOnLongClickListener {
                listener.onMessageLongClicked(
                    it,
                    position,
                    message.id,
                    Gravity.START,
                    UserType.RECEIVER
                )
                false
            }
            reactionContainer.visibility =
                if (message.reactions.isNotEmpty()) View.VISIBLE else View.GONE
            reactions.text = message.reactions.map { it.data }.toSet().joinToString(" ")
            this.message.text = message.text
            timestamp.text = DateUtils.getTime(message.timestamp)
        }
    }
}

interface ChatAdapterListener {
    fun onMessageLongClicked(
        view: View,
        position: Int,
        messageId: String,
        gravity: Int,
        userType: UserType
    )
}

enum class UserType {
    SENDER, RECEIVER
}