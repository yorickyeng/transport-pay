package com.transportpay.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Card(
    val id: Int,
    @SerialName("card_number") val cardNumber: String,
    val balance: Double,
    @SerialName("is_blocked") val isBlocked: Boolean,
    @SerialName("owner_name") val ownerName: String?,
    @SerialName("key_id") val keyId: Int?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class CreateCardRequest(
    @SerialName("card_number") val cardNumber: String,
    val balance: Double = 0.0,
    @SerialName("owner_name") val ownerName: String? = null,
    @SerialName("key_id") val keyId: Int? = null
)

@Serializable
data class UpdateCardRequest(
    val balance: Double? = null,
    @SerialName("is_blocked") val isBlocked: Boolean? = null,
    @SerialName("owner_name") val ownerName: String? = null,
    @SerialName("key_id") val keyId: Int? = null
)
