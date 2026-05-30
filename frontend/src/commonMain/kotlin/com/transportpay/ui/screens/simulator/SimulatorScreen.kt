package com.transportpay.ui.screens.simulator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.transportpay.data.api.ApiClient
import com.transportpay.data.models.AuthTransactionRequest
import com.transportpay.ui.components.getToastState
import kotlinx.coroutines.launch

@Composable
fun SimulatorScreen(apiClient: ApiClient) {
    var cardNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var terminalId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    val scope = rememberCoroutineScope()
    val toastState = getToastState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Column {
            Text(
                text = "Terminal Simulator",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Test payment authorization endpoint",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = { Text("Card Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., 1234567890123456") }
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., 50.00") }
                )
                OutlinedTextField(
                    value = terminalId,
                    onValueChange = { terminalId = it },
                    label = { Text("Terminal ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., 1") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (cardNumber.isNotBlank() && amount.isNotBlank() && terminalId.isNotBlank()) {
                            scope.launch {
                                isLoading = true
                                lastResult = null
                                val amountVal = amount.toDoubleOrNull() ?: 0.0
                                val terminalIdVal = terminalId.toIntOrNull() ?: 0
                                val request = AuthTransactionRequest(cardNumber, amountVal, terminalIdVal)
                                val result = apiClient.authTransaction(request)
                                isLoading = false
                                result.fold(
                                    onSuccess = { resp ->
                                        lastResult = resp.authorized to resp.message
                                        if (resp.authorized) {
                                            toastState.success("Transaction authorized!")
                                        } else {
                                            toastState.warning("Transaction declined: ${resp.message}")
                                        }
                                    },
                                    onFailure = { err ->
                                        lastResult = false to (err.message ?: "Unknown error")
                                        toastState.error("Error: ${err.message}")
                                    }
                                )
                            }
                        } else {
                            toastState.warning("Please fill all fields")
                        }
                    },
                    enabled = !isLoading && cardNumber.isNotBlank() && amount.isNotBlank() && terminalId.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Processing...", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Authorize Transaction", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Result Card
        AnimatedVisibility(
            visible = lastResult != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -20 })
        ) {
            lastResult?.let { (authorized, message) ->
                ResultCard(authorized = authorized, message = message)
            }
        }

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "How to use",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Enter card number, amount, and terminal ID to simulate a payment transaction. The simulator will call the /auth-transaction endpoint and display the result.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ResultCard(
    authorized: Boolean,
    message: String
) {
    val successColor = Color(0xFF22C55E)
    val errorColor = Color(0xFFEF4444)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (authorized) successColor.copy(alpha = 0.15f) else errorColor.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (authorized) successColor.copy(alpha = 0.2f) else errorColor.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (authorized) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (authorized) successColor else errorColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Message
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (authorized) "Transaction Authorized" else "Transaction Declined",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (authorized) successColor else errorColor
                )
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = if (authorized) successColor else errorColor,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
