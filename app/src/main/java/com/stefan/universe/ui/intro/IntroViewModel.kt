package com.stefan.universe.ui.intro

import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.UiState
import com.stefan.universe.common.UserIntent
import com.stefan.universe.common.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IntroViewModel @Inject constructor() : BaseViewModel<IntroSideEffect, IntroUiState, IntroUserIntent>() {
}


data class IntroUiState(
    val loading: Boolean = false
) : UiState

sealed class IntroUserIntent : UserIntent {
}

sealed class IntroSideEffect: SideEffect {
    data class Feedback(val msg: String) : IntroSideEffect()
}