package com.playground.jetpackplayground.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.playground.jetpackplayground.api.auth.network_responses.LoginResponse
import com.playground.jetpackplayground.api.auth.network_responses.RegistrationResponse
import com.playground.jetpackplayground.models.AuthToken
import com.playground.jetpackplayground.repository.auth.AuthRepository
import com.playground.jetpackplayground.ui.DataState
import com.playground.jetpackplayground.ui.auth.state.AuthStateEvent
import com.playground.jetpackplayground.ui.auth.state.AuthStateEvent.*
import com.playground.jetpackplayground.ui.auth.state.AuthViewState
import com.playground.jetpackplayground.ui.auth.state.LoginFields
import com.playground.jetpackplayground.ui.auth.state.RegistrationFields
import com.playground.jetpackplayground.util.AbsentLiveData
import com.playground.jetpackplayground.util.BaseViewModel
import com.playground.jetpackplayground.util.GenericApiResponse
import javax.inject.Inject

class AuthViewModel
@Inject
constructor(
    val authRepository: AuthRepository
): BaseViewModel<AuthStateEvent, AuthViewState>()
{
    override fun initNewViewState(): AuthViewState {
        return AuthViewState()
    }

    fun setRegistrationFields(registrationFields: RegistrationFields) {
        val update = getCurrentViewStateOrNew()
        if (update.registration_fields == registrationFields) {
            return
        }
        update.registration_fields = registrationFields
        _viewState.value = update
    }

    fun setLoginFields(loginFields: LoginFields) {
        val update = getCurrentViewStateOrNew()
        if (update.login_fields == loginFields) {
            return
        }
        update.login_fields = loginFields
        _viewState.value = update
    }

    fun setAuthToken(authToken: AuthToken) {
        val update = getCurrentViewStateOrNew()
        if (update.authToken == authToken) {
            return
        }
        update.authToken = authToken
        _viewState.value = update
    }

    fun cancelActiveJobs() {
        authRepository.cancelActiveJobs()
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

    override fun handleStateEvent(stateEvent: AuthStateEvent): LiveData<DataState<AuthViewState>> {
        when (stateEvent) {
            is LoginAttemptEvent -> {
                return authRepository.attemptLogin(
                    stateEvent.email,
                    stateEvent.password
                )
            }
            is RegisterAttemptEvent -> {
                return authRepository.attemptRegistration(
                    stateEvent.email,
                    stateEvent.username,
                    stateEvent.password,
                    stateEvent.confirm_password
                )
            }
            is CheckPreviousAuthEvent -> {
                return authRepository.checkPreviousAuthUser()
            }
        }
    }
}