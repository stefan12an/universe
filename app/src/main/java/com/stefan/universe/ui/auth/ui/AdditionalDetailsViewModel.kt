package com.stefan.universe.ui.auth.ui

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.stefan.universe.common.SideEffect
import com.stefan.universe.common.UiState
import com.stefan.universe.common.UserIntent
import com.stefan.universe.common.base.BaseViewModel
import com.stefan.universe.common.succeeded
import com.stefan.universe.common.utils.RemoteConfigHelper
import com.stefan.universe.ui.auth.data.model.University
import com.stefan.universe.ui.main.data.repository.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AdditionalDetailsViewModel @Inject constructor(private val firebaseRepository: FirebaseRepository) :
    BaseViewModel<AdditionalDetailsSideEffect, AdditionalDetailsUiState, AdditionalDetailsUserIntent>() {
    init {
        viewModelScope.launch {
            val user = firebaseRepository.getUser()
            setUiState(
                if (user.succeeded) AdditionalDetailsUiState(
                    university = RemoteConfigHelper.getUniversitiesList()
                        .firstOrNull {
                            it.emailIdentifier.contains(
                                user.data?.email?.split("@")?.last()
                            )
                        }) else AdditionalDetailsUiState()
            )
        }
    }

    override fun action(intent: AdditionalDetailsUserIntent) {
        when (intent) {

            is AdditionalDetailsUserIntent.SelectBirthDate -> {
                _uiState.value = uiState.value?.copy(birthDate = intent.date)
            }

            is AdditionalDetailsUserIntent.SaveAdditionalInformation -> {
                saveAdditionalInformation(
                    intent.displayName,
                    intent.uri,
                    intent.faculty,
                    intent.gender
                )
            }
        }
    }

    private fun validateMandatoryFields(
        displayName: String?,
        faculty: String?
    ): List<MandatoryFields> {
        val mandatoryFields = mutableListOf<MandatoryFields>()
        if (displayName?.isEmpty() == true) {
            mandatoryFields.add(MandatoryFields.DISPLAY_NAME)
        }
        if (faculty.isNullOrEmpty()) {
            mandatoryFields.add(MandatoryFields.FACULTY)
        }
        return mandatoryFields
    }

    private fun saveAdditionalInformation(
        displayName: String?,
        uri: Uri?,
        faculty: String?,
        gender: String?
    ) {
        _uiState.value = uiState.value?.copy(loading = true)
        val errors = validateMandatoryFields(displayName, faculty)
        if (errors.isNotEmpty()) {
            pushSideEffect(AdditionalDetailsSideEffect.NotifyErrors(errors))
            _uiState.value = uiState.value?.copy(loading = false)
            return
        }
        viewModelScope.launch {
            val result = firebaseRepository.editProfile(
                displayName,
                uri,
                uiState.value?.university?.name,
                faculty,
                gender,
                uiState.value?.birthDate
            )
            if (result.succeeded) {
                pushSideEffect(AdditionalDetailsSideEffect.NavigateToMain)
            } else {
                pushSideEffect(AdditionalDetailsSideEffect.Feedback("Failed to save additional information"))
            }
            _uiState.value = uiState.value?.copy(loading = false)
        }
    }
}

data class AdditionalDetailsUiState(
    val loading: Boolean = false,
    val university: University? = null,
    val birthDate: Date? = null
) : UiState

sealed class AdditionalDetailsUserIntent : UserIntent {

    class SaveAdditionalInformation(
        val displayName: String?,
        val uri: Uri?,
        val faculty: String?,
        val gender: String?,
    ) :
        AdditionalDetailsUserIntent()

    class SelectBirthDate(val date: Date) : AdditionalDetailsUserIntent()
}

sealed class AdditionalDetailsSideEffect : SideEffect {
    data class Feedback(val msg: String) : AdditionalDetailsSideEffect()
    data class NotifyErrors(val mandatoryFields: List<MandatoryFields>) : AdditionalDetailsSideEffect()
    data object NavigateToMain : AdditionalDetailsSideEffect()
}

enum class MandatoryFields {
    DISPLAY_NAME,
    FACULTY,
}