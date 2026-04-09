package com.it342.brainbox.network.models

import com.google.gson.annotations.SerializedName

data class NotebookSummary(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("title") val title: String,
    @SerializedName("wordCount") val wordCount: Int? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("lastReviewedAt") val lastReviewedAt: String? = null,
    @SerializedName("version") val version: Long? = null,
    @SerializedName("categoryId") val categoryId: Long? = null,
    @SerializedName("categoryName") val categoryName: String? = null
)

data class PlaylistSummary(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("title") val title: String,
    @SerializedName("currentIndex") val currentIndex: Int = 0,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("queue") val queue: List<NotebookSummary> = emptyList()
)
