package com.playground.jetpackplayground.repository.auth

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.playground.jetpackplayground.api.auth.OpenApiAuthService
import com.playground.jetpackplayground.api.auth.network_responses.LoginResponse
import com.playground.jetpackplayground.api.auth.network_responses.RegistrationResponse
import com.playground.jetpackplayground.models.AccountProperties
import com.playground.jetpackplayground.models.AuthToken
import com.playground.jetpackplayground.persistence.AccountPropertiesDao
import com.playground.jetpackplayground.persistence.AuthTokenDao
import com.playground.jetpackplayground.repository.JobManager
import com.playground.jetpackplayground.repository.NetworkBoundResource
import com.playground.jetpackplayground.session.SessionManager
import com.playground.jetpackplayground.ui.DataState
import com.playground.jetpackplayground.ui.Response
import com.playground.jetpackplayground.ui.ResponseType
import com.playground.jetpackplayground.ui.auth.state.AuthViewState
import com.playground.jetpackplayground.ui.auth.state.LoginFields
import com.playground.jetpackplayground.ui.auth.state.RegistrationFields
import com.playground.jetpackplayground.util.AbsentLiveData
import com.playground.jetpackplayground.util.ErrorHandling.Companion.ERROR_SAVE_AUTH_TOKEN
import com.playground.jetpackplayground.util.ErrorHandling.Companion.ERROR_UNKNOWN
import com.playground.jetpackplayground.util.ErrorHandling.Companion.GENERIC_AUTH_ERROR
import com.playground.jetpackplayground.util.GenericApiResponse
import com.playground.jetpackplayground.util.GenericApiResponse.*
import com.playground.jetpackplayground.util.PreferenceKeys
import com.playground.jetpackplayground.util.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
import kotlinx.coroutines.Job
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val openApiAuthService: OpenApiAuthService,
    val sessionManager: SessionManager,
    val sharedPreferences: SharedPreferences,
    val sharedPrefsEditor: SharedPreferences.Editor
): JobManager("AuthRepository"){
    private val TAG : String = "AppDebug"

    fun attemptLogin(email: String, password: String) : LiveData<DataState<AuthViewState>> {
        val loginFieldErrors = LoginFields(email, password).isValidForLogin()
        if (!loginFieldErrors.equals(LoginFields.LoginError.none()))
            return returnErrorResponse(loginFieldErrors, ResponseType.Dialog())

        return object : NetworkBoundResource<LoginResponse, Any, AuthViewState> (
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<LoginResponse>) {
                Log.d(TAG, "handleApiSuccessResponse: ${response}")

                // Incorrect login credentials counts as 200 from server, so need to handle that
                if (response.body.response.equals(GENERIC_AUTH_ERROR)) {
                    return onErrorReturn(response.body.errorMessage, true, false)
                }

                // Don't care about result, just insert if it doesn't exist because of foreign key relationship
                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        response.body.pk,
                        response.body.email,
                        ""
                    )
                )

                // will return -1 if failure
                val result = authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )

                if (result < 0) {
                    return onCompleteJob(
                        DataState.error(
                            Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                        )
                    )
                }

                saveAuthenticatedUserToPrefs(email)

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )

            }

            override fun createCall(): LiveData<GenericApiResponse<LoginResponse>> {
                return openApiAuthService.login(email, password)
            }

            override fun setJob(job: Job) {
                addJob("attemptLogin", job)
            }

            // not used in this case
            override suspend fun createCacheRequestAndReturn() {
            }

            override fun loadFromCache(): LiveData<AuthViewState> {
                // Not used in this case
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                // Not used in this case
            }

        }.asLiveData()
    }

    fun attemptRegistration(
        email : String,
        username :String,
        password : String,
        confirm_password : String) : LiveData<DataState<AuthViewState>> {
        val registrationFieldErrors = RegistrationFields(email, username, password, confirm_password).isValidForRegistration()
        if (!registrationFieldErrors.equals(RegistrationFields.RegistrationError.none())) {
            return returnErrorResponse(registrationFieldErrors, ResponseType.Dialog())
        }
        return object : NetworkBoundResource<RegistrationResponse, Any, AuthViewState> (
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {
                Log.d(TAG, "handleApiSuccessResponse: reponse : ${response}")

                if (response.body.response.equals(GENERIC_AUTH_ERROR)) {
                    return onErrorReturn(response.body.errorMessage, true, false)
                }

                // Don't care about result, just insert if it doesn't exist because of foreign key relationship
                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        response.body.pk,
                        response.body.email,
                        ""
                    )
                )

                // will return -1 if failure
                val result = authTokenDao.insert(
                    AuthToken(
                        response.body.pk,
                        response.body.token
                    )
                )

                if (result < 0) {
                    return onCompleteJob(
                        DataState.error(
                            Response(ERROR_SAVE_AUTH_TOKEN, ResponseType.Dialog())
                        )
                    )
                }

                saveAuthenticatedUserToPrefs(email)

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.pk, response.body.token)
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<RegistrationResponse>> {
                return openApiAuthService.register(email, username, password, confirm_password)
            }

            override fun setJob(job: Job) {
                addJob("attemptRegistration", job)
            }

            override suspend fun createCacheRequestAndReturn() {
                // not used in this case
            }

            override fun loadFromCache(): LiveData<AuthViewState> {
                // not used in this case
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                // not used in this case
            }

        }.asLiveData()
    }

    fun checkPreviousAuthUser() : LiveData<DataState<AuthViewState>> {
        val previousAuthUserEmail: String? = sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)
        if (previousAuthUserEmail.isNullOrBlank()) {
            Log.d(TAG, "checkPreviousAuthUser: No previously authenticated user found...")
            return returnNoTokenFound()
        }

        return object : NetworkBoundResource<Void, Any, AuthViewState> (
            sessionManager.isConnectedToTheInternet(),
            false,
            false,
            false
        ) {
            override suspend fun createCacheRequestAndReturn() {
                accountPropertiesDao.searchByEmail(previousAuthUserEmail).let { accountProperties ->
                    Log.d(TAG, "createCacheRequestAndReturn: Searching for token: $accountProperties")
                    accountProperties?.let {
                        if(accountProperties.pk > -1) {
                            authTokenDao.searchByPk(accountProperties.pk).let {
                                if (it != null) {
                                    onCompleteJob(
                                        DataState.data(
                                            data = AuthViewState(
                                                authToken = it
                                            )
                                        )
                                    )
                                    return
                                }
                            }
                        }
                    }
                }
                Log.d(TAG, "createCacheRequestAndReturn: Authtoken not found...")
                onCompleteJob(
                    DataState.data(
                        data = null,
                        response = Response(
                            RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                            ResponseType.None()
                        )
                    )
                )
            }

            // not used in this case
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<Void>) {
            }

            // not used in this case
            override fun createCall(): LiveData<GenericApiResponse<Void>> {
                return AbsentLiveData.create()
            }

            override fun setJob(job: Job) {
                addJob("checkPreviousAuthUser", job)
            }

            override fun loadFromCache(): LiveData<AuthViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cacheObject: Any?) {
                // not used in this case
            }

        }.asLiveData()
    }

    private fun returnNoTokenFound() : LiveData<DataState<AuthViewState>> {
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.data(
                    data = null,
                    response = Response(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE, ResponseType.None())
                )
            }
        }
    }

    private fun saveAuthenticatedUserToPrefs(email: String) {
        sharedPrefsEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPrefsEditor.apply()
    }

    private fun returnErrorResponse(errorMessage:String, responseType: ResponseType) : LiveData<DataState<AuthViewState>> {
        return object:LiveData<DataState<AuthViewState>> () {
            override fun onActive() {
                super.onActive()
                value = DataState.error(
                    Response(
                        errorMessage,
                        responseType
                    )
                )
            }
        }
    }
}