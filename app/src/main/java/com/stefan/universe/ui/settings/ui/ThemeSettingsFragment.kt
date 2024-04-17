package com.stefan.universe.ui.settings.ui

import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.stefan.universe.R
import com.stefan.universe.common.EventObserver
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.base.BaseFragment
import com.stefan.universe.databinding.FragmentThemeSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThemeSettingsFragment : BaseFragment<FragmentThemeSettingsBinding, ThemeSettingsViewModel>() {

    override val viewModel: ThemeSettingsViewModel by viewModels()

    override fun getViewBinding(container: ViewGroup?): FragmentThemeSettingsBinding {
        return FragmentThemeSettingsBinding.inflate(layoutInflater, container, false)
    }

    override fun bottomNavigationVisiblity(): Int {
        return View.GONE
    }

    override fun setupListeners() {
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.action(ThemeSettingsIntent.GoBack)
                }
            })
        with(binding) {
            backButton.setOnClickListener { viewModel.action(ThemeSettingsIntent.GoBack) }
            themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.lightRadioButton -> {
                        viewModel.action(ThemeSettingsIntent.SelectTheme(Theme.LIGHT))
                    }

                    R.id.darkRadioButton -> {
                        viewModel.action(ThemeSettingsIntent.SelectTheme(Theme.DARK))
                    }

                    R.id.systemRadioButton -> {
                        viewModel.action(ThemeSettingsIntent.SelectTheme(Theme.SYSTEM))
                    }
                }
            }
        }
    }

    override fun observeViewModel() {
        viewModel.sideEffect.observe(viewLifecycleOwner, EventObserver { handleSideEffects(it) })
        viewModel.uiState.observe(viewLifecycleOwner) { state -> updateUi(state) }
    }

    private fun updateUi(state: ThemeSettingsState) {
        with(binding) {
            when (state.theme) {
                Theme.LIGHT -> lightRadioButton.isChecked = true
                Theme.DARK -> darkRadioButton.isChecked = true
                Theme.SYSTEM -> systemRadioButton.isChecked = true
                null -> {}
            }
        }
    }

    override fun handleSideEffects(sideEffect: SideEffect) {
        when (sideEffect) {
            is ThemeSettingsSideEffect.Feedback -> {
                Toast.makeText(requireContext(), sideEffect.message, Toast.LENGTH_SHORT).show()
            }

            is ThemeSettingsSideEffect.GoBack -> {
                findNavController().popBackStack()
            }
        }
    }
}