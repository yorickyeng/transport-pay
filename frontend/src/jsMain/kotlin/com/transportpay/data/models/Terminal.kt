package com.transportpay.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Terminal(
    val id: Int,
    @SerialName("serial_number") val serialNumber: String,
    @SerialName("installation_address") val installationAddress: String,
    val name: String?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class CreateTerminalRequest(
    @SerialName("serial_number") val serialNumber: String,
    @SerialName("installation_address") val installationAddress: String,
    val name: String? = null
)

@Serializable
data class UpdateTerminalRequest(
    @SerialName("serial_number") val serialNumber: String? = null,
    @SerialName("installation_address") val installationAddress: String? = null,
    val name: String? = null
)
