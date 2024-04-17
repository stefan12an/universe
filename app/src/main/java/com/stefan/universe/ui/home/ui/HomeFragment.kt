package com.stefan.universe.ui.home.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.stefan.universe.R
import com.stefan.universe.common.EventObserver
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.base.BaseFragment
import com.stefan.universe.common.views.setItemTouchHelper
import com.stefan.universe.databinding.FragmentHomeBinding
import com.stefan.universe.ui.home.data.adapter.ChatClickListener
import com.stefan.universe.ui.home.data.adapter.ChatListAdapter
import com.stefan.universe.ui.main.data.model.FirebaseUserModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {

    override val viewModel: HomeViewModel by viewModels()
    private val adapter by lazy {
        ChatListAdapter(
            requireContext(),
            FirebaseUserModel(),
            object : ChatClickListener {
                override fun onChatClicked(chatId: String) {
                    viewModel.action(HomeUserIntent.OpenChat(chatId))
                }

                override fun onDeleteChatClicked(chatId: String) {
                    viewModel.action(HomeUserIntent.DeleteChat(chatId))
                }
            })
    }

    override fun getViewBinding(container: ViewGroup?) =
        FragmentHomeBinding.inflate(layoutInflater, container, false)

    override fun bottomNavigationVisiblity(): Int {
        return View.VISIBLE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        setItemTouchHelper(requireContext(), binding.userListRecyclerview, adapter)
    }

    override fun setupListeners() {
        binding.newMessageIndicator.setOnClickListener { viewModel.action(HomeUserIntent.NewMessage) }
    }

    private fun initRecyclerView() {
        binding.userListRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            val dividerItemDecoration = DividerItemDecoration(
                context,
                LinearLayoutManager(requireContext()).orientation
            )
            addItemDecoration(dividerItemDecoration)
            adapter = this@HomeFragment.adapter
            setHasFixedSize(true)
        }
    }

    override fun observeViewModel() {
        viewModel.sideEffect.observe(viewLifecycleOwner, EventObserver { handleSideEffects(it) })
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            if (uiState.loading) {
                binding.shimmerLayout.apply {
                    visibility = View.VISIBLE; startShimmer()
                }
                binding.userListRecyclerview.visibility = View.GONE
            } else updateUi(uiState)
        }
    }

    private fun updateUi(uiState: HomeUiState) {
        with(binding) {
            Glide.with(requireContext()).load(uiState.user?.photoUri).placeholder(R.drawable.app_logo).into(profilePicture)
            welcomeMessageTextView.text = if (uiState.user?.displayName?.isEmpty() == true)
                "Welcome back!" else "Welcome back, ${uiState.user?.displayName}!"
            shimmerLayout.apply { visibility = View.GONE; stopShimmer() }
            adapter.submitList(uiState.chats)
            adapter.swapData(uiState.user ?: FirebaseUserModel())
            userListRecyclerview.visibility = View.VISIBLE
        }
    }

    override fun handleSideEffects(sideEffect: SideEffect) {
        when (sideEffect) {
            is HomeSideEffect.NavigateToNewMessage -> navigate(HomeFragmentDirections.actionHomeFragmentToNewMessageFragment())
            is HomeSideEffect.NavigateToChat -> navigate(
                HomeFragmentDirections.actionHomeFragmentToChatFragment(
                    chat = sideEffect.chat
                )
            )

            is HomeSideEffect.Feedback -> Toast.makeText(
                requireContext(),
                sideEffect.msg,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun navigate(action: NavDirections) {
        findNavController().navigate(action)
    }
}