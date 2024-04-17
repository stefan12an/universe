package com.stefan.universe.common.base

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.viewbinding.ViewBinding
import com.stefan.universe.common.SideEffect
import javax.inject.Inject

// Define a base activity class parameterized by ViewBinding
abstract class BaseActivity<VB : ViewBinding, VM : ViewModel> : AppCompatActivity() {

    // Inject ViewModel using Dagger/Hilt
    abstract val viewModel: VM

    // ViewBinding instance for the activity
    protected lateinit var binding: VB

    protected lateinit var navController: NavController

    // Abstract method to initialize ViewBinding
    abstract fun getViewBinding(): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize ViewBinding
        binding = getViewBinding()
        setContentView(binding.root)

        // Observe ViewModel
        observeViewModel()
    }

    // Method to observe ViewModel events
    abstract fun observeViewModel()

    abstract fun handleSideEffects(sideEffect: SideEffect, navigateTo: NavController)
}