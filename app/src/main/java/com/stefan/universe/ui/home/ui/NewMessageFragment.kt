package com.stefan.universe.ui.home.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.stefan.universe.common.EventObserver
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.base.BaseFragment
import com.stefan.universe.databinding.FragmentNewMessageBinding
import com.stefan.universe.ui.home.data.adapter.UserListAdapter
import com.stefan.universe.ui.main.ui.UniApplication
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewMessageFragment : BaseFragment<FragmentNewMessageBinding, NewMessageViewModel>() {

    override val viewModel: NewMessageViewModel by viewModels()
    private var adapter: UserListAdapter? = null

    override fun getViewBinding(container: ViewGroup?): FragmentNewMessageBinding {
        return FragmentNewMessageBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRecyclerView()

        (activity as UniApplication).toggleBottomNavigationVisibility(View.GONE)
    }

    private fun showRecyclerView() {
        binding.shimmerLayout.apply {
            stopShimmer()
            visibility = View.GONE
        }
        binding.userListRecyclerview.visibility = View.VISIBLE
    }

    private fun initRecyclerView() {
        adapter = UserListAdapter(
            requireContext(),
            emptyList()
        )
        binding.userListRecyclerview.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        binding.userListRecyclerview.adapter = adapter
        binding.userListRecyclerview.setHasFixedSize(true)
    }

    override fun observeViewModel() {
        val sideEffectsObserver = EventObserver<SideEffect> {
            handleSideEffects(it)
        }
        val uiStateObserver = Observer<NewMessageUiState> {
            if (it.loading) {
                binding.shimmerLayout.startShimmer()
            } else {
                adapter?.swapData(it.users )
                showRecyclerView()
            }
        }
        viewModel.sideEffect.observe(this, sideEffectsObserver)
        viewModel.uiState.observe(this, uiStateObserver)
    }

    override fun handleSideEffects(sideEffect: SideEffect) {
        when (sideEffect) {
            NewMessageSideEffects.NavigateToHome -> {
                findNavController().popBackStack()
            }

            NewMessageSideEffects.StartChat -> {
                // Start chat
            }
        }
    }
}