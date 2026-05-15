package com.transportpay.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val login: String,
    @SerialName("is_admin") val isAdmin: Boolean,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)

@Serializable
data class CreateUserRequest(
    val login: String,
    val password: String,
    @SerialName("is_admin") val isAdmin: Boolean = false
)

@Serializable
data class LoginRequest(
    val login: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val user: User
)
