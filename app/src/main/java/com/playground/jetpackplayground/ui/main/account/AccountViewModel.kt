package com.playground.jetpackplayground.ui.main.account

import androidx.lifecycle.LiveData
import com.playground.jetpackplayground.models.AccountProperties
import com.playground.jetpackplayground.repository.main.AccountRepository
import com.playground.jetpackplayground.session.SessionManager
import com.playground.jetpackplayground.ui.DataState
import com.playground.jetpackplayground.ui.main.account.state.AccountStateEvent
import com.playground.jetpackplayground.ui.main.account.state.AccountStateEvent.*
import com.playground.jetpackplayground.ui.main.account.state.AccountViewState
import com.playground.jetpackplayground.util.AbsentLiveData
import com.playground.jetpackplayground.util.BaseViewModel
import javax.inject.Inject

class AccountViewModel
@Inject
constructor(
    val sessionManager: SessionManager,
    val accountRepository: AccountRepository
): BaseViewModel<AccountStateEvent, AccountViewState> ()
{
    override fun handleStateEvent(stateEvent: AccountStateEvent): LiveData<DataState<AccountViewState>> {
        when(stateEvent) {
            is GetAccountPropertiesEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    accountRepository.getAccountProperties(authToken)
                }?: AbsentLiveData.create()
            }
            is UpdateAccountPropertiesEvent -> {
                return AbsentLiveData.create()
            }
            is ChangePasswordEvent -> {
                return AbsentLiveData.create()
            }
            is None -> {
                return AbsentLiveData.create()
            }
        }
    }

    override fun initNewViewState(): AccountViewState {
        return AccountViewState()
    }

    fun setAccountPropertiesData(accountProperties: AccountProperties) {
        val update = getCurrentViewStateOrNew()
        if(update.accountProperties == accountProperties)
            return
        update.accountProperties = accountProperties
        _viewState.value = update
    }

    fun logout() {
        sessionManager.logOut()
    }
}