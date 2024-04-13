package com.stefan.universe.ui.auth.ui

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.UiState
import com.stefan.universe.common.UserIntent
import com.stefan.universe.common.base.BaseViewModel
import com.stefan.universe.common.succeeded
import com.stefan.universe.ui.main.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdditionalDetailsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepository) :
    BaseViewModel<AdditionalDetailsSideEffect, AdditionalDetailsUiState, AdditionalDetailsUserIntent>() {
    init {
        setUiState(AdditionalDetailsUiState())
    }

    override fun action(intent: AdditionalDetailsUserIntent) {
        when (intent) {
            is AdditionalDetailsUserIntent.Skip -> {
                pushSideEffect(AdditionalDetailsSideEffect.NavigateToMain)
            }

            is AdditionalDetailsUserIntent.SaveAdditionalInformation -> {
                if (intent.displayName.isNullOrEmpty()) {
                    pushSideEffect(AdditionalDetailsSideEffect.Feedback("Please enter display name"))
                    return
                }
                saveAdditionalInformation(intent.displayName, intent.uri)
            }
        }
    }

    private fun saveAdditionalInformation(displayName: String, uri: Uri?) {
        _uiState.value = AdditionalDetailsUiState(loading = true)
        viewModelScope.launch {
            val result = firebaseRepository.editProfile(displayName, uri)
            if (result.succeeded) {
                pushSideEffect(AdditionalDetailsSideEffect.NavigateToMain)
            } else {
                pushSideEffect(AdditionalDetailsSideEffect.Feedback("Failed to save additional information"))
            }
            _uiState.value = AdditionalDetailsUiState(loading = false)
        }
    }
}

data class AdditionalDetailsUiState(
    val loading: Boolean = false,
) : UiState

sealed class AdditionalDetailsUserIntent : UserIntent {

    class SaveAdditionalInformation(val displayName: String?, val uri: Uri?) :
        AdditionalDetailsUserIntent()

    data object Skip : AdditionalDetailsUserIntent()

}

sealed class AdditionalDetailsSideEffect : SideEffect {
    data class Feedback(val msg: String) : AdditionalDetailsSideEffect()
    data object NavigateToMain : AdditionalDetailsSideEffect()
}