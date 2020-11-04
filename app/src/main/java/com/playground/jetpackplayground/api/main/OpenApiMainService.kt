package com.playground.jetpackplayground.api.main

import androidx.lifecycle.LiveData
import com.playground.jetpackplayground.models.AccountProperties
import com.playground.jetpackplayground.util.GenericApiResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface OpenApiMainService {

    @GET("account/properties")
    fun getAccountProperties(
        @Header("Authorization") authorization: String
    ): LiveData<GenericApiResponse<AccountProperties>>

}