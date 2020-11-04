package com.playground.jetpackplayground.repository.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.playground.jetpackplayground.api.main.OpenApiMainService
import com.playground.jetpackplayground.models.AccountProperties
import com.playground.jetpackplayground.models.AuthToken
import com.playground.jetpackplayground.persistence.AccountPropertiesDao
import com.playground.jetpackplayground.repository.NetworkBoundResource
import com.playground.jetpackplayground.session.SessionManager
import com.playground.jetpackplayground.ui.DataState
import com.playground.jetpackplayground.ui.main.account.state.AccountViewState
import com.playground.jetpackplayground.util.GenericApiResponse
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.security.interfaces.DSAKey
import javax.inject.Inject

class AccountRepository
@Inject
constructor(
    val openApiMainService: OpenApiMainService,
    val accountPropertiesDao: AccountPropertiesDao,
    val sessionManager: SessionManager
) {

    private val TAG = "AppDebug"

    private var repositoryJob: Job? = null

    fun getAccountProperties(authToken: AuthToken) : LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            true
        ){
            override suspend fun createCacheRequestAndReturn() {
                withContext(Main){
                    // finish by viewing the db source
                    result.addSource(loadFromCache()) {
                        onCompleteJob(DataState.data(
                            data = it,
                            response = null
                        ))
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(response: GenericApiResponse.ApiSuccessResponse<AccountProperties>) {
                updateLocalDb(response.body)
                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {
                return openApiMainService.getAccountProperties(
                    "Token ${authToken.token}"
                )
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return accountPropertiesDao.searchByPk(authToken.account_pk!!)
                    .switchMap {
                        object : LiveData<AccountViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = AccountViewState(it)
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: AccountProperties?) {
                cacheObject?.let {
                    accountPropertiesDao.updateAccountProperties(
                        cacheObject.pk,
                        cacheObject.email,
                        cacheObject.username
                    )
                }
            }

        }.asLiveData()
    }

    fun cancelActiveJobs() {
        Log.d(TAG, "cancelActiveJobs: in Account repository...")
        repositoryJob?.cancel()
    }
}