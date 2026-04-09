package com.it342.brainbox.network.models

import com.google.gson.annotations.SerializedName

data class QuizSummary(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("difficulty") val difficulty: String? = null,
    @SerializedName("notebookUuid") val notebookUuid: String? = null,
    @SerializedName("notebookTitle") val notebookTitle: String? = null,
    @SerializedName("questionCount") val questionCount: Int = 0,
    @SerializedName("estimatedTime") val estimatedTime: String? = null,
    @SerializedName("bestScore") val bestScore: Int? = null,
    @SerializedName("attempts") val attempts: Long = 0,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class QuizQuestion(
    @SerializedName("type") val type: String? = null,
    @SerializedName("text") val text: String,
    @SerializedName("options") val options: List<String> = emptyList(),
    @SerializedName("correctIndex") val correctIndex: Int
)

data class QuizDetail(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("difficulty") val difficulty: String? = null,
    @SerializedName("notebookUuid") val notebookUuid: String? = null,
    @SerializedName("notebookTitle") val notebookTitle: String? = null,
    @SerializedName("questionCount") val questionCount: Int = 0,
    @SerializedName("estimatedTime") val estimatedTime: String? = null,
    @SerializedName("bestScore") val bestScore: Int? = null,
    @SerializedName("attempts") val attempts: Long = 0,
    @SerializedName("questions") val questions: List<QuizQuestion> = emptyList(),
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class QuizAttemptRequest(
    @SerializedName("score") val score: Int
)
