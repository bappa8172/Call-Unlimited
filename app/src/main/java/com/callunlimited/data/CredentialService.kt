package com.callunlimited.data

import com.callunlimited.BuildConfig
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Url

data class SipCredentials(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("server") val server: String,
    @SerializedName("port") val port: Int,
    @SerializedName("transport") val transport: String
)

interface CredentialApi {
    @GET
    suspend fun getCredentials(@Url url: String = BuildConfig.CRED_FILE): SipCredentials
}
