package com.transportpay.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Key(
    val id: Int,
    @SerialName("key_value") val keyValue: String,
    val description: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class CreateKeyRequest(
    @SerialName("key_value") val keyValue: String,
    val description: String? = null
)

@Serializable
data class UpdateKeyRequest(
    @SerialName("key_value") val keyValue: String? = null,
    val description: String? = null
)
