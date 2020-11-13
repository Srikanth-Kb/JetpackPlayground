package com.playground.jetpackplayground.repository.main

import android.util.Log
import android.widget.AbsListView
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.playground.jetpackplayground.api.GenericResponse
import com.playground.jetpackplayground.api.main.OpenApiMainService
import com.playground.jetpackplayground.models.AccountProperties
import com.playground.jetpackplayground.models.AuthToken
import com.playground.jetpackplayground.persistence.AccountPropertiesDao
import com.playground.jetpackplayground.repository.JobManager
import com.playground.jetpackplayground.repository.NetworkBoundResource
import com.playground.jetpackplayground.session.SessionManager
import com.playground.jetpackplayground.ui.DataState
import com.playground.jetpackplayground.ui.Response
import com.playground.jetpackplayground.ui.ResponseType
import com.playground.jetpackplayground.ui.main.account.state.AccountViewState
import com.playground.jetpackplayground.util.AbsentLiveData
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
): JobManager("AccountRepository") {

    private val TAG = "AppDebug"
    private var repositoryJob: Job? = null

    fun getAccountProperties(authToken: AuthToken) : LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
            sessionManager.isConnectedToTheInternet(),
            true,
            false,
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
                addJob("getAccountProperties", job)
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

    fun saveAccountProperties(authToken: AuthToken,
                              accountProperties: AccountProperties
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            isNetworkAvailable = sessionManager.isConnectedToTheInternet(),
            isNetworkRequest = true,
            shouldCancelIfNoInternet = true,
            shouldLoadFromCache = false
        ) {
            override suspend fun createCacheRequestAndReturn() {
                // Not applicable to this function
            }

            override suspend fun handleApiSuccessResponse(response: GenericApiResponse.ApiSuccessResponse<GenericResponse>) {
                updateLocalDb(null)
                withContext(Main) {
                    // finish with success response
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(response.body.response, ResponseType.Toast())
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.saveAccountProperties(
                    "Token ${authToken.token!!}",
                    accountProperties.email,
                    accountProperties.username
                )
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                return accountPropertiesDao.updateAccountProperties(
                    accountProperties.pk,
                    accountProperties.email,
                    accountProperties.username
                )
            }

            override fun setJob(job: Job) {
                addJob("saveAccountProperties", job)
            }

        }.asLiveData()
    }

    fun updatePassword(
        authToken: AuthToken,
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState> (
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {
            override suspend fun createCacheRequestAndReturn() {
                // Not applicable in this case
            }

            override suspend fun handleApiSuccessResponse(response: GenericApiResponse.ApiSuccessResponse<GenericResponse>) {
                withContext(Main) {
                    // finish with a success response
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(
                                response.body.response,
                                ResponseType.Toast()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return openApiMainService.updatePassword(
                    "Token ${authToken.token!!}",
                    currentPassword,
                    newPassword,
                    confirmNewPassword
                )
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                // Not applicable in this case
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                // Not applicable in this case
            }

            override fun setJob(job: Job) {
                addJob("updatePassword", job)
            }

        }.asLiveData()
    }
}