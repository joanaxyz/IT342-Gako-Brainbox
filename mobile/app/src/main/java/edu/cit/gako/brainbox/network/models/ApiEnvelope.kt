package edu.cit.gako.brainbox.network.models

import com.google.gson.annotations.SerializedName

data class ApiEnvelope<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: T? = null,
    @SerializedName("error") val error: ApiErrorPayload? = null,
    @SerializedName("timestamp") val timestamp: String? = null
)

data class ApiErrorPayload(
    @SerializedName("code") val code: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("details") val details: String? = null
)

