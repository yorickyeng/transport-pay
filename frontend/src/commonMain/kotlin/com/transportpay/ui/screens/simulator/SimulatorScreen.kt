package com.transportpay.ui.screens.simulator

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.transportpay.NfcService
import com.transportpay.data.api.ApiClient
import com.transportpay.data.models.AuthTransactionRequest
import com.transportpay.ui.components.getToastState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SimulatorScreen(apiClient: ApiClient, nfcService: NfcService? = null) {
    var cardNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var terminalId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    
    // Состояние для диалога NFC
    var showNfcDialog by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val toastState = getToastState()

    // Слушатель событий NFC. Активируется только когда открыт диалог.
    LaunchedEffect(showNfcDialog) {
        if (showNfcDialog && nfcService != null) {
            try {
                // Приостанавливаем выполнение до появления ПЕРВОЙ карты в потоке
                val uid = nfcService.nfcEvents.first()
                
                cardNumber = uid
                showNfcDialog = false // Скрываем UI
                
                // Делегируем сетевой запрос в независимый scope экрана
                scope.launch {
                    val amountVal = amount.toDoubleOrNull() ?: 0.0
                    val terminalIdVal = terminalId.toIntOrNull() ?: 0
                    
                    isLoading = true
                    lastResult = null
                    
                    val request = AuthTransactionRequest(uid, amountVal, terminalIdVal)
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
            } catch (e: Exception) {
                // Игнорируем CancellationException, если пользователь нажал "Отмена" на диалоге
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column {
            Text(
                text = "Terminal Simulator",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Test payment authorization via NFC or manually",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount (₽)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("50.00") }
                    )
                    OutlinedTextField(
                        value = terminalId,
                        onValueChange = { terminalId = it },
                        label = { Text("Terminal ID") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("1") }
                    )
                }

                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = { Text("Card UID (Manual entry)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., 89FC2006") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Кнопка оплаты через железо (NFC)
                    Button(
                        onClick = {
                            if (amount.isNotBlank() && terminalId.isNotBlank()) {
                                if (nfcService != null) {
                                    showNfcDialog = true // Открываем диалог ожидания карты
                                } else {
                                    toastState.error("NFC модуль не подключен")
                                }
                            } else {
                                toastState.warning("Введите Amount и Terminal ID")
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Icon(Icons.Default.Contactless, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Оплатить через NFC", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Старая кнопка ручной отправки
                    OutlinedButton(
                        onClick = {
                            if (cardNumber.isNotBlank() && amount.isNotBlank() && terminalId.isNotBlank()) {
                                scope.launch {
                                    isLoading = true
                                    lastResult = null
                                    val request = AuthTransactionRequest(
                                        cardNumber, 
                                        amount.toDoubleOrNull() ?: 0.0, 
                                        terminalId.toIntOrNull() ?: 0
                                    )
                                    val result = apiClient.authTransaction(request)
                                    isLoading = false
                                    result.fold(
                                        onSuccess = { resp -> lastResult = resp.authorized to resp.message },
                                        onFailure = { err -> lastResult = false to (err.message ?: "Error") }
                                    )
                                }
                            }
                        },
                        enabled = !isLoading && cardNumber.isNotBlank(),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Ручная авторизация", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Диалог ожидания прикладывания карты
        if (showNfcDialog) {
            AlertDialog(
                onDismissRequest = { showNfcDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Nfc,
                        contentDescription = "NFC",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                title = { Text("Ожидание карты") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("Приложите пластиковую карту к считывателю PN532.", fontSize = 16.sp)
                        CircularProgressIndicator()
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showNfcDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }

        AnimatedVisibility(
            visible = lastResult != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            lastResult?.let { (authorized, message) ->
                ResultCard(authorized = authorized, message = message)
            }
        }
    }
}

@Composable
private fun ResultCard(authorized: Boolean, message: String) {
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
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (authorized) successColor.copy(alpha = 0.2f) else errorColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (authorized) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (authorized) successColor else errorColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (authorized) "Transaction Authorized" else "Transaction Declined",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (authorized) successColor else errorColor
                )
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = if (authorized) successColor else errorColor
                )
            }
        }
    }
}
