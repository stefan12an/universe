package com.stefan.universe.ui.main.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.stefan.universe.NavDirections
import com.stefan.universe.R
import com.stefan.universe.common.EventObserver
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.base.BaseActivity
import com.stefan.universe.common.utils.ViewUtils.setLightStatusBars
import com.stefan.universe.databinding.ActivityMainBinding
import com.stefan.universe.ui.settings.ui.Theme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class UniApplication : BaseActivity<ActivityMainBinding, UniApplicationViewModel>() {

    override val viewModel: UniApplicationViewModel by viewModels()

    lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun getViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.apply {
            setOnApplyWindowInsetsListener(null)
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.chat_page -> {
                        viewModel.action(UniApplicationUserIntent.NavigateToChats)
                        true
                    }

                    R.id.notifications_page -> {
                        viewModel.action(UniApplicationUserIntent.NavigateToNotifications)
                        true
                    }

                    R.id.settings_page -> {
                        viewModel.action(UniApplicationUserIntent.NavigateToSettings)
                        true
                    }

                    else -> false
                }
            }
        }
    }

    override fun observeViewModel() {
        val sideEffectsObserver = EventObserver<SideEffect> {
            handleSideEffects(it, navController)
        }
        viewModel.sideEffect.observe(this, sideEffectsObserver)
    }

    override fun handleSideEffects(sideEffect: SideEffect, navigateTo: NavController) {
        when (sideEffect) {
            is UniApplicationSideEffects.UpdateTheme -> {
                when (sideEffect.theme) {
                    Theme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    Theme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    Theme.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }

            is UniApplicationSideEffects.NavigateToMain -> {
                selectBottomNavigationElement(R.id.chat_page)
                val action = NavDirections.actionGlobalHomeFragment()
                navigateTo.navigate(action)
            }

            is UniApplicationSideEffects.NavigateToAuth -> {
                val action = NavDirections.actionGlobalAuthFragment()
                navigateTo.navigate(action)
            }

            is UniApplicationSideEffects.NavigateToValidateEmail -> {
                val action = NavDirections.actionGlobalEmailValidationFragment()
                navigateTo.navigate(action)
            }

            is UniApplicationSideEffects.NavigateToNotifications -> {
                selectBottomNavigationElement(R.id.notifications_page)
                val action = NavDirections.actionGlobalNotificationsFragment()
                navigateTo.navigate(action)
            }

            is UniApplicationSideEffects.NavigateToSettings -> {
                selectBottomNavigationElement(R.id.settings_page)
                val action = NavDirections.actionGlobalSettingsFragment()
                navigateTo.navigate(action)
            }

            is UniApplicationSideEffects.Feedback ->
                Toast.makeText(this, sideEffect.msg, Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleBottomNavigationVisibility(visibility: Int) {
        binding.bottomNavigation.visibility = visibility
    }

    fun selectBottomNavigationElement(item: Int) {
        binding.bottomNavigation.setOnItemSelectedListener(null)
        binding.bottomNavigation.selectedItemId = item
        setupBottomNavigation()
    }

    fun updateStatusBarColor(color: Int = R.color.background, reset: Boolean = false) {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                if (reset) systemBars.top else 0,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }
        window.setLightStatusBars(reset)
        window.statusBarColor = getColor(color)
    }
}