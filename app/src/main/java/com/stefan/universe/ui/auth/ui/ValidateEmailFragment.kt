package com.stefan.universe.ui.auth.ui

import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.stefan.universe.R
import com.stefan.universe.common.EventObserver
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.base.BaseFragment
import com.stefan.universe.databinding.FragmentValidateEmailBinding
import com.stefan.universe.ui.main.ui.UniApplication
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ValidateEmailFragment : BaseFragment<FragmentValidateEmailBinding, ValidateEmailViewModel>() {
    override val viewModel: ValidateEmailViewModel by viewModels()

    override fun getViewBinding(container: ViewGroup?): FragmentValidateEmailBinding {
        return FragmentValidateEmailBinding.inflate(layoutInflater, container, false)
    }

    override fun bottomNavigationVisiblity(): Int {
        return View.GONE
    }

    override fun onResume() {
        super.onResume()
        viewModel.action(ValidateEmailUserIntent.CheckEmailVerification)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as UniApplication).updateStatusBarColor(reset = true)
    }

    override fun observeViewModel() {
        val sideEffectsObserver = EventObserver<SideEffect> {
            handleSideEffects(it)
        }
        val uiStateObserver = Observer<ValidateEmailUiState> {
        }
        viewModel.sideEffect.observe(this, sideEffectsObserver)
        viewModel.uiState.observe(this, uiStateObserver)
    }

    override fun setupListeners() {
        binding.resendEmailButton.setOnClickListener { viewModel.action(ValidateEmailUserIntent.SendEmailVerification) }
        binding.closeButton.setOnClickListener { viewModel.action(ValidateEmailUserIntent.GiveUpEmailVerification) }
        (activity as UniApplication).updateStatusBarColor(color = R.color.primary)
        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.action(ValidateEmailUserIntent.GiveUpEmailVerification)
                }
            })
    }

    override fun handleSideEffects(sideEffect: SideEffect) {
        when (sideEffect) {
            is ValidateEmailSideEffect.NavigateToAdditionalDetails -> {
                val action =
                    ValidateEmailFragmentDirections.actionValidateEmailFragmentToAdditionalDetailsFragment()
                findNavController().navigate(action)
            }

            is ValidateEmailSideEffect.NavigateToAuth -> {
                val action =
                    ValidateEmailFragmentDirections.actionValidateEmailFragmentToAuthFragment()
                findNavController().navigate(action)
            }

            is ValidateEmailSideEffect.Feedback -> {
                Toast.makeText(requireContext(), sideEffect.msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}