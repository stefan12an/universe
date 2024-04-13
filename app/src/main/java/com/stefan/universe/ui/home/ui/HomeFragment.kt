package com.stefan.universe.ui.home.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.stefan.universe.common.EventObserver
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.base.BaseFragment
import com.stefan.universe.databinding.FragmentHomeBinding
import com.stefan.universe.ui.home.data.adapter.ChatListAdapter
import com.stefan.universe.ui.main.ui.UniApplication
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {

    override val viewModel: HomeViewModel by viewModels()
    private var adapter: ChatListAdapter? = null

    override fun getViewBinding(container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        (activity as UniApplication).toggleBottomNavigationVisibility(View.VISIBLE)
        binding.newMessageIndicator.setOnClickListener { viewModel.action(HomeUserIntent.NewMessage) }
        binding.logoutButton.setOnClickListener {
            (activity as UniApplication).toggleBottomNavigationVisibility(View.GONE)
            viewModel.action(HomeUserIntent.Logout)
        }
    }

    override fun observeViewModel() {
        val sideEffectsObserver = EventObserver<SideEffect> {
            handleSideEffects(it)
        }
        val uiStateObserver = Observer<HomeUiState> {
            if (it.loading) {
                binding.welcomeMessageTextView.text = "Loading..."
                binding.shimmerLayout.startShimmer()
            } else {
                Glide.with(requireContext()).load(it.user?.photoUri).into(binding.profilePicture)
                binding.welcomeMessageTextView.text = if (it.user?.displayName?.isEmpty() == true)
                    "Welcome back!" else "Welcome back, ${it.user?.displayName}!"
                adapter?.swapData(it.chats ?: emptyList())
                showRecyclerView()
            }
        }
        viewModel.sideEffect.observe(this, sideEffectsObserver)
        viewModel.uiState.observe(this, uiStateObserver)
    }

    private fun showRecyclerView() {
        binding.shimmerLayout.apply {
            stopShimmer()
            visibility = View.GONE
        }
        binding.userListRecyclerview.visibility = View.VISIBLE
    }

    private fun initRecyclerView() {
        adapter = ChatListAdapter(
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

    override fun handleSideEffects(sideEffect: SideEffect) {
        when (sideEffect) {
            is HomeSideEffect.NavigateToAuth -> {
                val action = HomeFragmentDirections.actionHomeFragmentToAuthFragment()
                findNavController().navigate(action)
            }

            is HomeSideEffect.NavigateToNewMessage -> {
                val action = HomeFragmentDirections.actionHomeFragmentToNewMessageFragment()
                findNavController().navigate(action)
            }

            is HomeSideEffect.Feedback -> {
                Toast.makeText(requireContext(), sideEffect.msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}