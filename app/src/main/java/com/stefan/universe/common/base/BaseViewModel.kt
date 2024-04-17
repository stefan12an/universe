package com.stefan.universe.common.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stefan.universe.common.Event
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.UiState
import com.stefan.universe.common.UserIntent

abstract class BaseViewModel<SE : SideEffect, US: UiState, UI: UserIntent> : ViewModel() {

    protected val _sideEffect = MutableLiveData<Event<SE>>()
    val sideEffect: LiveData<Event<SE>> = _sideEffect

    protected val _uiState = MutableLiveData<US>()
    val uiState: LiveData<US> = _uiState

    protected fun pushSideEffect(navigateTo: SE) {
        _sideEffect.value = Event(navigateTo)
    }

    open fun action(intent: UI) {
        // Implement in subclass
    }

    protected fun setUiState(uiState: US) {
        _uiState.value = uiState
    }
}