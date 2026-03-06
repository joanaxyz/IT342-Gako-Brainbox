package com.it342.brainbox.network.models

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String
)
