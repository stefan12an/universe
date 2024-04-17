package com.stefan.universe.ui.chat.ui

import android.annotation.SuppressLint
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.PopupWindowCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.stefan.universe.R
import com.stefan.universe.common.EventObserver
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.base.BaseFragment
import com.stefan.universe.databinding.FragmentChatBinding
import com.stefan.universe.ui.chat.data.adapter.ChatAdapter
import com.stefan.universe.ui.chat.data.adapter.ChatAdapterListener
import com.stefan.universe.ui.chat.data.adapter.UserType
import com.stefan.universe.ui.chat.data.model.ChatWrapper
import com.stefan.universe.ui.chat.data.model.FirebaseChatModelWrapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ChatFragment : BaseFragment<FragmentChatBinding, ChatViewModel>() {
    override val viewModel: ChatViewModel by viewModels()
    private val adapter by lazy {
        ChatAdapter(FirebaseChatModelWrapper(), object : ChatAdapterListener {
            override fun onMessageLongClicked(
                view: View,
                position: Int,
                messageId: String,
                gravity: Int,
                userType: UserType
            ) {
                binding.messageListRecyclerview.smoothScrollToPosition(position)
                showPopup(view, messageId, gravity, userType)
            }
        })
    }

    override fun getViewBinding(container: ViewGroup?) =
        FragmentChatBinding.inflate(layoutInflater, container, false)

    override fun bottomNavigationVisiblity(): Int {
        return View.GONE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setChat(ChatFragmentArgs.fromBundle(requireArguments()).chat)
        initRecyclerView()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun setupListeners() {
        binding.messageListRecyclerview.setOnTouchListener { _, event ->
            if ((event.x < binding.emojiPickerLayout.left ||
                        event.x > binding.emojiPickerLayout.right ||
                        event.y > binding.emojiPickerLayout.bottom ||
                        event.y < binding.emojiPickerLayout.top) && binding.emojiPickerLayout.visibility == View.VISIBLE
            ) {
                hideEmojiPickerWithAnimation()
            }
            false
        }
        binding.buttonSend.setOnClickListener {
            viewModel.action(ChatUserIntent.SendMessage(binding.messageInput.text.toString()))
        }
        binding.backButton.setOnClickListener { handleBackButton() }
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    handleBackButton()
                }
            })
    }

    private fun handleBackButton() {
        if (binding.emojiPickerLayout.visibility == View.VISIBLE) {
            hideEmojiPickerWithAnimation()
        } else {
            viewModel.action(ChatUserIntent.GoBack)
        }
    }

    private fun hideEmojiPickerWithAnimation() {
        val upBottom: Animation = AnimationUtils.loadAnimation(context, R.anim.up_bottom)
        binding.emojiPickerLayout.animation = upBottom
        binding.emojiPickerLayout.animate().start()
        binding.emojiPickerLayout.visibility = View.GONE
    }

    private fun initRecyclerView() {
        binding.messageListRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ChatFragment.adapter
            setHasFixedSize(true)
        }
    }

    override fun observeViewModel() {
        viewModel.sideEffect.observe(viewLifecycleOwner, EventObserver { handleSideEffects(it) })
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            if (uiState.loading) {
                binding.shimmerLayout.startShimmer()
            } else updateUi(uiState)
        }
    }

    private fun updateUi(uiState: ChatUiState) {
        with(uiState.chatWrapper) {
            adapter.swapData(this)
            binding.shimmerLayout.apply { visibility = View.GONE; stopShimmer() }
            binding.messageListRecyclerview.visibility = View.VISIBLE
            binding.messageListRecyclerview.scrollToPosition(adapter.itemCount - 1)
            CoroutineScope(Main).launch {
                Glide.with(requireContext())
                    .load(recieverPhotoUri())
                    .placeholder(R.drawable.app_logo)
                    .fitCenter()
                    .into(binding.profilePicture)
                binding.recieverName.text = recieverDisplayName()
            }
        }
    }

    override fun handleSideEffects(sideEffect: SideEffect) {
        when (sideEffect) {
            is ChatSideEffects.MessageSent -> updateChat(sideEffect.chat, UpdateType.SENT)
            is ChatSideEffects.MessageDeleted -> updateChat(sideEffect.chat, UpdateType.DELETED)
            is ChatSideEffects.GoBack -> findNavController().popBackStack()
            is ChatSideEffects.Feedback -> Toast.makeText(
                requireContext(),
                sideEffect.msg,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateChat(chat: ChatWrapper, type: UpdateType) {
        adapter.swapData(chat)
        binding.messageInput.text.clear()
        if (type == UpdateType.SENT) {
            binding.messageListRecyclerview.smoothScrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun showPopup(view: View, messageId: String, gravity: Int, userType: UserType) {
        val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(
            if (userType == UserType.SENDER) R.layout.sender_popup_chat_options else R.layout.reciever_popup_chat_options,
            null
        )
        val spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        popupView.measure(spec, spec)
        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        ).apply { height = popupView.measuredHeight; elevation = 10f }

        val actions = mapOf(
            R.id.delete_action to { viewModel.action(ChatUserIntent.DeleteMessage(messageId)) },
            R.id.like_action to { /* Perform your action for the "Like" option here */ },
            R.id.reaction_1 to {
                viewModel.action(
                    ChatUserIntent.ReactToMessage(
                        messageId,
                        popupView.findViewById<TextView>(R.id.reaction_1).text.toString()
                    )
                )
            },
            R.id.reaction_2 to {
                viewModel.action(
                    ChatUserIntent.ReactToMessage(
                        messageId,
                        popupView.findViewById<TextView>(R.id.reaction_2).text.toString()
                    )
                )
            },
            R.id.reaction_3 to {
                viewModel.action(
                    ChatUserIntent.ReactToMessage(
                        messageId,
                        popupView.findViewById<TextView>(R.id.reaction_3).text.toString()
                    )
                )
            },
            R.id.reaction_4 to { showEmojiPicker() }
        )

        actions.forEach { (id, action) ->
            popupView.findViewById<View>(id)?.let {
                it.setOnClickListener {
                    action()
                    popupWindow.dismiss()
                }
            }
        }

        binding.emojiPickerLayout.setOnEmojiPickedListener {
            viewModel.action(ChatUserIntent.ReactToMessage(messageId, it.emoji))
            hideEmojiPickerWithAnimation()
        }

        PopupWindowCompat.showAsDropDown(popupWindow, view, 0, 0, gravity)
    }

    private fun showEmojiPicker() {
        val bottomUp: Animation = AnimationUtils.loadAnimation(context, R.anim.bottom_up)
        binding.emojiPickerLayout.animation = bottomUp
        binding.emojiPickerLayout.animate().start()
        binding.emojiPickerLayout.visibility = View.VISIBLE
    }

}

enum class UpdateType {
    SENT, DELETED
}