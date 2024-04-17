package com.stefan.universe.common.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.stefan.universe.common.SideEffect
import com.stefan.universe.ui.main.ui.UniApplication

// Define a base fragment class parameterized by ViewBinding
abstract class BaseFragment<VB : ViewBinding, VM : ViewModel> : Fragment() {

    // Inject ViewModel using Dagger/Hilt
    abstract val viewModel: VM

    // ViewBinding instance for the fragment
    protected lateinit var binding: VB

    // Abstract method to initialize ViewBinding
    abstract fun getViewBinding(container: ViewGroup?): VB

    abstract fun bottomNavigationVisiblity(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = getViewBinding(container)
        (activity as UniApplication).toggleBottomNavigationVisibility(bottomNavigationVisiblity())

        setupListeners()

        observeViewModel()

        return binding.root
    }

    abstract fun observeViewModel()
    abstract fun setupListeners()

    abstract fun handleSideEffects(sideEffect: SideEffect)
}