package edu.cit.gako.brainbox.network.models

import com.google.gson.annotations.SerializedName

data class AiConversationMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class AiSelectionTarget(
    @SerializedName("id") val id: String,
    @SerializedName("text") val text: String
)

data class AiSelectionEdit(
    @SerializedName("id") val id: String,
    @SerializedName("content") val content: String
)

data class AiQueryRequest(
    @SerializedName("query") val query: String,
    @SerializedName("notebookUuid") val notebookUuid: String,
    @SerializedName("conversationHistory") val conversationHistory: List<AiConversationMessage> = emptyList(),
    @SerializedName("selectedText") val selectedText: String = "",
    @SerializedName("aiSelections") val aiSelections: List<AiSelectionTarget> = emptyList(),
    @SerializedName("selectionMode") val selectionMode: String = "",
    @SerializedName("mode") val mode: String = "editor"
)

data class AiQueryResponse(
    @SerializedName("response") val response: String? = null,
    @SerializedName("action") val action: String? = null,
    @SerializedName("editorContent") val editorContent: String? = null,
    @SerializedName("conversationTitle") val conversationTitle: String? = null,
    @SerializedName("selectionEdits") val selectionEdits: List<AiSelectionEdit> = emptyList(),
    @SerializedName("quizData") val quizData: QuizCreateRequest? = null,
    @SerializedName("flashcardData") val flashcardData: FlashcardDeckCreateRequest? = null
)

data class AiConfigResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("model") val model: String,
    @SerializedName("proxyUrl") val proxyUrl: String,
    @SerializedName("hasApiKey") val hasApiKey: Boolean,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class AiConfigListResponse(
    @SerializedName("configs") val configs: List<AiConfigResponse> = emptyList(),
    @SerializedName("selectedConfigId") val selectedConfigId: Long? = null
)

data class AiConfigRequest(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("name") val name: String,
    @SerializedName("model") val model: String,
    @SerializedName("proxyUrl") val proxyUrl: String,
    @SerializedName("apiKey") val apiKey: String? = null
)
