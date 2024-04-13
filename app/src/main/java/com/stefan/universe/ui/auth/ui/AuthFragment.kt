package com.stefan.universe.ui.auth.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.stefan.universe.R
import com.stefan.universe.common.EventObserver
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.base.BaseFragment
import com.stefan.universe.databinding.FragmentAuthBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthFragment : BaseFragment<FragmentAuthBinding, AuthViewModel>() {

    override val viewModel: AuthViewModel by viewModels()

    override fun getViewBinding(container: ViewGroup?): FragmentAuthBinding {
        return FragmentAuthBinding.inflate(layoutInflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.loginButton.setOnClickListener {
            val email = binding.textInputEmail.text?.toString()
            val password = binding.textInputPassword.text?.toString()
            val authType = viewModel.uiState.value?.type ?: AuthType.LOGIN
            val authIntent = if (authType == AuthType.LOGIN) AuthUserIntent.Login(
                email,
                password
            ) else AuthUserIntent.Register(email, password)
            viewModel.action(authIntent)
        }
        binding.registerButton.setOnClickListener {
            viewModel.action(AuthUserIntent.ChangeToRegister)
        }
    }

    override fun observeViewModel() {
        val sideEffectsObserver = EventObserver<SideEffect> {
            handleSideEffects(it)
        }
        val uiStateObserver = Observer<AuthUiState> {
            binding.loadingLayout.visibility = if (it.loading) View.VISIBLE else View.GONE
        }
        viewModel.sideEffect.observe(this, sideEffectsObserver)
        viewModel.uiState.observe(this, uiStateObserver)
    }

    override fun handleSideEffects(sideEffect: SideEffect) {
        when (sideEffect) {
            is AuthSideEffect.Feedback -> {
                Toast.makeText(requireContext(), sideEffect.msg, Toast.LENGTH_SHORT).show()
            }

            is AuthSideEffect.NavigateToMain -> {
                val action = AuthFragmentDirections.actionAuthFragmentToHomeFragment()
                findNavController().navigate(action)
            }

            is AuthSideEffect.NavigateToValidateEmail -> {
                val action = AuthFragmentDirections.actionAuthFragmentToValidateEmailFragment()
                findNavController().navigate(action)
            }

            is AuthSideEffect.ChangeToLogin -> {
                binding.loginButton.text = getString(R.string.login)
                binding.registerButton.text = getString(R.string.sign_up)
                binding.registerButton.setOnClickListener {
                    viewModel.action(AuthUserIntent.ChangeToRegister)
                }
            }

            is AuthSideEffect.ChangeToRegister -> {
                binding.loginButton.text = getString(R.string.sign_up)
                binding.registerButton.text = getString(R.string.login)
                binding.registerButton.setOnClickListener {
                    viewModel.action(AuthUserIntent.ChangeToLogin)
                }
            }
        }
    }
}