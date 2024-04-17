package com.stefan.universe.ui.settings.ui

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.stefan.universe.R
import com.stefan.universe.common.EventObserver
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.base.BaseFragment
import com.stefan.universe.common.utils.DateUtils
import com.stefan.universe.databinding.FragmentSettingsBinding
import com.stefan.universe.ui.main.data.model.FirebaseUserModel
import com.stefan.universe.ui.main.ui.UniApplication
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding, SettingsViewModel>() {
    override val viewModel: SettingsViewModel by viewModels()

    override fun getViewBinding(container: ViewGroup?) =
        FragmentSettingsBinding.inflate(layoutInflater, container, false)

    override fun bottomNavigationVisiblity(): Int {
        return View.VISIBLE
    }

    override fun observeViewModel() {
        viewModel.sideEffect.observe(viewLifecycleOwner, EventObserver { handleSideEffects(it) })
        viewModel.uiState.observe(viewLifecycleOwner) { handleUiState(it) }
    }

    private fun handleUiState(uiState: SettingsUiState?) {
        uiState?.let { state ->
            with(binding) {
                setViewVisibility(state.loading)
                if (!state.loading) {
                    state.user?.let { user ->
                        setUserDetails(user)
                        setSettingsItems()
                    }
                }
            }
        }
    }

    private fun FragmentSettingsBinding.setViewVisibility(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        settingsLinearLayout.visibility = if (!isLoading) View.VISIBLE else View.GONE
        generalDetailsCardView.visibility = if (!isLoading) View.VISIBLE else View.GONE
    }

    private fun FragmentSettingsBinding.setUserDetails(user: FirebaseUserModel) {
        Glide.with(this@SettingsFragment)
            .load(user.photoUri)
            .placeholder(R.drawable.app_logo)
            .into(profilePicture)
        fullNameTextView.text = user.displayName

        setViewDetails(universityFacultyTextView, user.faculty)
        setViewDetails(descriptionTextView, user.description)
        setViewDetails(universityTextView, user.university)
        genderTextView.apply {
            visibility = if (user.gender.isEmpty()) View.GONE else View.VISIBLE
            this.text = getString(R.string.gender, user.gender)
        }

        additionalDetailsCardView.visibility =
            if (user.university.isEmpty() && user.gender.isEmpty() && !DateUtils.isUserAtLeast18(
                    user.birthDate
                )
            ) View.GONE else View.VISIBLE

        dateOfBirthTextView.apply {
            visibility = if (!DateUtils.isUserAtLeast18(user.birthDate)) View.GONE else View.VISIBLE
            text = getString(R.string.date_of_birth, DateUtils.getDate(user.birthDate.time))
        }
    }

    private fun setViewDetails(view: TextView, text: String) {
        view.apply {
            visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
            this.text = text
        }
    }

    private fun FragmentSettingsBinding.setSettingsItems() {
        setItem(emailSettings.root, R.string.edit_email)
        setItem(passwordSettings.root, R.string.edit_password)
        setItem(
            themeSettings.root.apply { setOnClickListener { viewModel.action(SettingsUserIntent.ChangeTheme) } },
            R.string.change_theme
        )
    }

    override fun handleSideEffects(sideEffect: SideEffect) {
        when (sideEffect) {
            is SettingsSideEffects.NavigateToAuth -> {
                (activity as UniApplication).selectBottomNavigationElement(R.id.chat_page)
                navigate(SettingsFragmentDirections.actionSettingsFragmentToAuthFragment())
            }

            is SettingsSideEffects.NavigateToChangeTheme -> navigate(
                SettingsFragmentDirections.actionSettingsFragmentToThemeSettingsFragment()
            )

            is SettingsSideEffects.Feedback -> Toast.makeText(
                requireContext(),
                sideEffect.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun setupListeners() {
        binding.logoutButton.setOnClickListener { viewModel.action(SettingsUserIntent.Logout) }
    }

    private fun setItem(view: View, string: Int) {
        view.findViewById<TextView>(R.id.buttonTextView).setText(string)
    }

    private fun navigate(action: NavDirections) {
        findNavController().navigate(action)
    }
}