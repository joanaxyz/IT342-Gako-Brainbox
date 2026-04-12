package edu.cit.gako.brainbox.network.models

import com.google.gson.annotations.SerializedName

data class FlashcardDeckSummary(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("notebookUuid") val notebookUuid: String? = null,
    @SerializedName("notebookTitle") val notebookTitle: String? = null,
    @SerializedName("cardCount") val cardCount: Int = 0,
    @SerializedName("bestMastery") val bestMastery: Int? = null,
    @SerializedName("attempts") val attempts: Long = 0,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class FlashcardCard(
    @SerializedName("front") val front: String,
    @SerializedName("back") val back: String
)

data class FlashcardDeckCreateRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("notebookUuid") val notebookUuid: String? = null,
    @SerializedName("cards") val cards: List<FlashcardCard> = emptyList()
)

data class FlashcardDeckDetail(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String? = null,
    @SerializedName("notebookUuid") val notebookUuid: String? = null,
    @SerializedName("notebookTitle") val notebookTitle: String? = null,
    @SerializedName("cardCount") val cardCount: Int = 0,
    @SerializedName("bestMastery") val bestMastery: Int? = null,
    @SerializedName("attempts") val attempts: Long = 0,
    @SerializedName("cards") val cards: List<FlashcardCard> = emptyList(),
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class FlashcardAttemptRequest(
    @SerializedName("mastery") val mastery: Int,
    @SerializedName("clientMutationId") val clientMutationId: String? = null
)

