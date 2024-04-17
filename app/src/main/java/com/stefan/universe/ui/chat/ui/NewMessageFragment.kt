package com.stefan.universe.ui.chat.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.stefan.universe.common.EventObserver
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.base.BaseFragment
import com.stefan.universe.databinding.FragmentNewMessageBinding
import com.stefan.universe.ui.chat.data.adapter.UserListAdapter
import com.stefan.universe.ui.chat.data.adapter.UserListListener
import com.stefan.universe.ui.main.data.model.User
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewMessageFragment : BaseFragment<FragmentNewMessageBinding, NewMessageViewModel>() {

    override val viewModel: NewMessageViewModel by viewModels()
    private val adapter by lazy {
        UserListAdapter(requireContext(), emptyList(), object : UserListListener {
            override fun onUserClicked(user: User) {
                viewModel.action(NewMessageUserIntent.StartChat(user))
            }
        })
    }

    override fun getViewBinding(container: ViewGroup?) =
        FragmentNewMessageBinding.inflate(layoutInflater, container, false)

    override fun bottomNavigationVisiblity(): Int {
        return View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }

    override fun setupListeners() {
        binding.backButton.setOnClickListener { viewModel.action(NewMessageUserIntent.NavigateHome) }
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.action(NewMessageUserIntent.NavigateHome)
                }
            })
    }

    private fun initRecyclerView() {
        binding.userListRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            val dividerItemDecoration = DividerItemDecoration(
                context,
                LinearLayoutManager(requireContext()).orientation
            )
            addItemDecoration(dividerItemDecoration)
            adapter = this@NewMessageFragment.adapter
            setHasFixedSize(true)
        }
    }

    override fun observeViewModel() {
        viewModel.sideEffect.observe(viewLifecycleOwner, EventObserver { handleSideEffects(it) })
        viewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            if (uiState.loading) binding.shimmerLayout.startShimmer() else updateUi(uiState)
        }
    }

    private fun updateUi(uiState: NewMessageUiState) {
        adapter.swapData(uiState.users)
        binding.shimmerLayout.apply { visibility = View.GONE; stopShimmer() }
        binding.userListRecyclerview.visibility = View.VISIBLE
    }

    override fun handleSideEffects(sideEffect: SideEffect) {
        when (sideEffect) {
            NewMessageSideEffects.NavigateToHome -> findNavController().popBackStack()
            is NewMessageSideEffects.NavigateToChat -> findNavController().navigate(
                NewMessageFragmentDirections.actionNewMessageFragmentToChatFragment(chat = sideEffect.chat)
            )

            is NewMessageSideEffects.Feedback -> Toast.makeText(
                requireContext(),
                sideEffect.msg,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}