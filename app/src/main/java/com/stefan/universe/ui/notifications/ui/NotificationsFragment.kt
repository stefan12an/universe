package com.stefan.universe.ui.notifications.ui

import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.stefan.universe.common.EventObserver
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.base.BaseFragment
import com.stefan.universe.databinding.FragmentNotificationsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsFragment : BaseFragment<FragmentNotificationsBinding, NotificationsViewModel>() {
    override val viewModel: NotificationsViewModel by viewModels()

    override fun getViewBinding(container: ViewGroup?): FragmentNotificationsBinding {
        return FragmentNotificationsBinding.inflate(layoutInflater, container, false)
    }

    override fun bottomNavigationVisiblity(): Int {
        return View.VISIBLE
    }

    override fun observeViewModel() {
        viewModel.sideEffect.observe(viewLifecycleOwner, EventObserver { handleSideEffects(it) })
    }

    override fun setupListeners() {
        //no-op
    }

    override fun handleSideEffects(sideEffect: SideEffect) {
        when (sideEffect) {
            is NotificationsSideEffects.Feedback -> Toast.makeText(
                requireContext(),
                sideEffect.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}