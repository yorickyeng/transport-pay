package com.transportpay.data.api

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import com.transportpay.data.models.*

class ApiClient(
    private val baseUrl: String = "http://localhost:8080/api/v1",
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 30000
        }
    }

    private var authToken: String? = null

    fun setToken(token: String?) {
        authToken = token
    }

    fun getToken(): String? = authToken

    private fun HttpRequestBuilder.auth() {
        authToken?.let { token ->
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    // ===== AUTH =====
    suspend fun login(login: String, password: String): Result<LoginResponse> {
        return try {
            val response = client.post("$baseUrl/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(login, password))
            }

            if (response.status.isSuccess()) {
                val loginResponse = response.bodyAsText()
                val parsed = Json.decodeFromString<LoginResponse>(loginResponse)
                setToken(parsed.token)
                Result.success(parsed)
            } else {
                Result.failure(Exception("Login failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== FETCH KEYS (public) =====
    suspend fun fetchKeys(): Result<List<Key>> {
        return try {
            val response = client.get("$baseUrl/fetch-keys")
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to fetch keys: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== AUTH TRANSACTION (public) =====
    suspend fun authTransaction(request: AuthTransactionRequest): Result<AuthTransactionResponse> {
        return try {
            val response = client.post("$baseUrl/auth-transaction") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            when {
                response.status.isSuccess() -> {
                    val text = response.bodyAsText()
                    Result.success(Json.decodeFromString(text))
                }
                response.status.value == 403 -> Result.failure(Exception("Card not authorized"))
                else -> Result.failure(Exception("Auth failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== USERS =====
    suspend fun getUser(id: Int): Result<User> {
        return try {
            val response = client.get("$baseUrl/users/$id") {
                auth()
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to get user: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(id: Int, isAdmin: Boolean? = null): Result<User> {
        return try {
            val response = client.put("$baseUrl/users/$id") {
                auth()
                contentType(ContentType.Application.Json)
                setBody(UpdateUserRequest(isAdmin))
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to update user: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUser(login: String, password: String, isAdmin: Boolean): Result<User> {
        return try {
            val response = client.post("$baseUrl/admin/users") {
                auth()
                contentType(ContentType.Application.Json)
                setBody(CreateUserRequest(login, password, isAdmin))
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to create user: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsers(): Result<List<User>> {
        return try {
            val response = client.get("$baseUrl/admin/users") {
                auth()
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to get users: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(id: Int): Result<Unit> {
        return try {
            val response = client.delete("$baseUrl/admin/users/$id") {
                auth()
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete user: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== TERMINALS =====
    suspend fun getTerminals(): Result<List<Terminal>> {
        return try {
            val response = client.get("$baseUrl/terminals") {
                auth()
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to get terminals: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTerminal(id: Int): Result<Terminal> {
        return try {
            val response = client.get("$baseUrl/terminals/$id") {
                auth()
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to get terminal: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTerminal(serialNumber: String, installationAddress: String, name: String? = null): Result<Terminal> {
        return try {
            val response = client.post("$baseUrl/admin/terminals") {
                auth()
                contentType(ContentType.Application.Json)
                setBody(CreateTerminalRequest(serialNumber, installationAddress, name))
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to create terminal: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTerminal(id: Int, serialNumber: String? = null, installationAddress: String? = null, name: String? = null): Result<Terminal> {
        return try {
            val response = client.put("$baseUrl/admin/terminals/$id") {
                auth()
                contentType(ContentType.Application.Json)
                setBody(UpdateTerminalRequest(serialNumber, installationAddress, name))
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to update terminal: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTerminal(id: Int): Result<Unit> {
        return try {
            val response = client.delete("$baseUrl/admin/terminals/$id") {
                auth()
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete terminal: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== CARDS =====
    suspend fun getCards(): Result<List<Card>> {
        return try {
            val response = client.get("$baseUrl/cards") {
                auth()
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to get cards: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCard(id: Int): Result<Card> {
        return try {
            val response = client.get("$baseUrl/cards/$id") {
                auth()
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to get card: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCard(cardNumber: String, balance: Double = 0.0, ownerName: String? = null, keyId: Int? = null): Result<Card> {
        return try {
            val response = client.post("$baseUrl/admin/cards") {
                auth()
                contentType(ContentType.Application.Json)
                setBody(CreateCardRequest(cardNumber, balance, ownerName, keyId))
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to create card: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCard(id: Int, balance: Double? = null, isBlocked: Boolean? = null, ownerName: String? = null, keyId: Int? = null): Result<Card> {
        return try {
            val response = client.put("$baseUrl/admin/cards/$id") {
                auth()
                contentType(ContentType.Application.Json)
                setBody(UpdateCardRequest(balance, isBlocked, ownerName, keyId))
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to update card: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCard(id: Int): Result<Unit> {
        return try {
            val response = client.delete("$baseUrl/admin/cards/$id") {
                auth()
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete card: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== KEYS =====
    suspend fun getKeys(): Result<List<Key>> {
        return try {
            val response = client.get("$baseUrl/admin/keys") {
                auth()
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to get keys: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getKey(id: Int): Result<Key> {
        return try {
            val response = client.get("$baseUrl/admin/keys/$id") {
                auth()
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to get key: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createKey(keyValue: String, description: String? = null): Result<Key> {
        return try {
            val response = client.post("$baseUrl/admin/keys") {
                auth()
                contentType(ContentType.Application.Json)
                setBody(CreateKeyRequest(keyValue, description))
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to create key: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateKey(id: Int, keyValue: String? = null, description: String? = null): Result<Key> {
        return try {
            val response = client.put("$baseUrl/admin/keys/$id") {
                auth()
                contentType(ContentType.Application.Json)
                setBody(UpdateKeyRequest(keyValue, description))
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to update key: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteKey(id: Int): Result<Unit> {
        return try {
            val response = client.delete("$baseUrl/admin/keys/$id") {
                auth()
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete key: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===== TRANSACTIONS =====
    suspend fun getTransactions(): Result<List<Transaction>> {
        return try {
            val response = client.get("$baseUrl/transactions") {
                auth()
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to get transactions: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTransaction(id: Int): Result<Transaction> {
        return try {
            val response = client.get("$baseUrl/transactions/$id") {
                auth()
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to get transaction: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createTransaction(amount: Double, cardId: Int, terminalId: Int): Result<Transaction> {
        return try {
            val response = client.post("$baseUrl/admin/transactions") {
                auth()
                contentType(ContentType.Application.Json)
                setBody(CreateTransactionRequest(amount, cardId, terminalId))
            }
            if (response.status.isSuccess()) {
                val text = response.bodyAsText()
                Result.success(Json.decodeFromString(text))
            } else {
                Result.failure(Exception("Failed to create transaction: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(id: Int): Result<Unit> {
        return try {
            val response = client.delete("$baseUrl/admin/transactions/$id") {
                auth()
            }
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete transaction: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun close() {
        client.close()
    }
}

@Serializable
data class LoginRequest(
    val login: String,
    val password: String
)

@Serializable
data class UpdateUserRequest(
    @SerialName("is_admin") val isAdmin: Boolean? = null
)

@Serializable
data class UpdateTerminalRequest(
    @SerialName("serial_number") val serialNumber: String? = null,
    @SerialName("installation_address") val installationAddress: String? = null,
    val name: String? = null
)

@Serializable
data class UpdateCardRequest(
    val balance: Double? = null,
    @SerialName("is_blocked") val isBlocked: Boolean? = null,
    @SerialName("owner_name") val ownerName: String? = null,
    @SerialName("key_id") val keyId: Int? = null
)

@Serializable
data class UpdateKeyRequest(
    @SerialName("key_value") val keyValue: String? = null,
    val description: String? = null
)
