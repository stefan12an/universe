package com.stefan.universe.ui.notifications.ui

import android.app.Notification
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.UiState
import com.stefan.universe.common.UserIntent
import com.stefan.universe.common.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor():
    BaseViewModel<NotificationsSideEffects, NotificationsState, NotificationsUserIntent>() {

    init {
        setUiState(NotificationsState())
    }

    override fun action(intent: NotificationsUserIntent) {
        // no-op
    }
}

sealed class NotificationsUserIntent : UserIntent {
}

data class NotificationsState(
    val loading: Boolean = false,
    val notifications: List<Notification> = emptyList()
) : UiState

sealed class NotificationsSideEffects : SideEffect {
    data class Feedback(val message: String) : NotificationsSideEffects()
}