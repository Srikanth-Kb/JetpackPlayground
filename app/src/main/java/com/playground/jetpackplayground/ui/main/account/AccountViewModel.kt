package com.playground.jetpackplayground.ui.main.account

import androidx.lifecycle.LiveData
import com.playground.jetpackplayground.models.AccountProperties
import com.playground.jetpackplayground.repository.main.AccountRepository
import com.playground.jetpackplayground.session.SessionManager
import com.playground.jetpackplayground.ui.BaseViewModel
import com.playground.jetpackplayground.ui.DataState
import com.playground.jetpackplayground.ui.Loading
import com.playground.jetpackplayground.ui.auth.state.AuthStateEvent
import com.playground.jetpackplayground.ui.main.account.state.AccountStateEvent
import com.playground.jetpackplayground.ui.main.account.state.AccountStateEvent.*
import com.playground.jetpackplayground.ui.main.account.state.AccountViewState
import com.playground.jetpackplayground.ui.main.blog.state.BlogViewState
import com.playground.jetpackplayground.util.AbsentLiveData
import javax.inject.Inject

class AccountViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val accountRepository: AccountRepository
)
    : BaseViewModel<AccountStateEvent, AccountViewState>()
{
    override fun handleStateEvent(stateEvent: AccountStateEvent): LiveData<DataState<AccountViewState>> {
        when(stateEvent){

            is GetAccountPropertiesEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    accountRepository.getAccountProperties(authToken)
                }?: AbsentLiveData.create()
            }

            is UpdateAccountPropertiesEvent ->{
                return sessionManager.cachedToken.value?.let { authToken ->
                    authToken.account_pk?.let { pk ->
                        val newAccountProperties = AccountProperties(
                            pk,
                            stateEvent.email,
                            stateEvent.username
                        )
                        accountRepository.saveAccountProperties(
                            authToken,
                            newAccountProperties
                        )
                    }
                }?: AbsentLiveData.create()
            }

            is ChangePasswordEvent ->{
                return sessionManager.cachedToken.value?.let {
                    accountRepository.updatePassword(
                        it,
                        stateEvent.currentPassword,
                        stateEvent.newPassword,
                        stateEvent.confirmNewPassword
                    )
                }?:AbsentLiveData.create()
            }

            is None ->{
                return object : LiveData<DataState<AccountViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        value = DataState(
                            null,
                            Loading(false),
                            null
                        )
                    }
                }
            }
        }
    }

    fun setAccountPropertiesData(accountProperties: AccountProperties){
        val update = getCurrentViewStateOrNew()
        if(update.accountProperties == accountProperties){
            return
        }
        update.accountProperties = accountProperties
        _viewState.value = update
    }

    override fun initNewViewState(): AccountViewState {
        return AccountViewState()
    }

    fun logout(){
        sessionManager.logOut()
    }

    fun cancelActiveJobs(){
        handlePendingEvent()
        accountRepository.cancelActiveJobs()
    }

    fun handlePendingEvent() {
        setStateEvent(AccountStateEvent.None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }
}














