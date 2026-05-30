package com.transportpay.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: Int,
    val amount: Double,
    @SerialName("card_id") val cardId: Int,
    @SerialName("terminal_id") val terminalId: Int,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class CreateTransactionRequest(
    val amount: Double,
    @SerialName("card_id") val cardId: Int,
    @SerialName("terminal_id") val terminalId: Int
)

@Serializable
data class AuthTransactionRequest(
    @SerialName("card_number") val cardNumber: String,
    val amount: Double,
    @SerialName("terminal_id") val terminalId: Int
)

@Serializable
data class AuthTransactionResponse(
    val authorized: Boolean,
    val message: String
)
