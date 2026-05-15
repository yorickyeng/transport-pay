package com.transportpay.ui.screens.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.transportpay.data.api.ApiClient
import com.transportpay.data.models.Card
import com.transportpay.ui.components.EmptyState
import com.transportpay.ui.components.getToastState
import kotlinx.coroutines.launch

@Composable
fun CardsScreen(
    apiClient: ApiClient,
    isAdmin: Boolean
) {
    var cards by remember { mutableStateOf<List<Card>?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var cardToTopUp by remember { mutableStateOf<Card?>(null) }
    val scope = rememberCoroutineScope()
    val toastState = getToastState()

    fun loadCards() {
        scope.launch {
            isLoading = true
            apiClient.getCards()
                .onSuccess { cards = it }
                .onFailure { 
                    // Игнорируем ошибки при первоначальной загрузке
                    cards = emptyList()
                }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadCards()
    }

    fun refreshCards() {
        scope.launch {
            apiClient.getCards()
                .onSuccess { cards = it }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Cards",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Manage payment cards and balances",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (isAdmin) {
                Button(onClick = { showCreateDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Create Card")
                }
            }
        }

        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            cards.isNullOrEmpty() -> {
                EmptyState(
                    message = "No cards found",
                    subMessage = if (isAdmin) "Create a new card to get started" else "No cards available in the system"
                )
            }
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    cards!!.forEach { card ->
                        CardItem(
                            card = card,
                            isAdmin = isAdmin,
                            apiClient = apiClient,
                            toastState = toastState,
                            onRefresh = ::refreshCards,
                            onTopUp = { cardToTopUp = card }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCardDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { cardNumber, ownerName, initialBalance ->
                scope.launch {
                    apiClient.createCard(cardNumber, initialBalance, ownerName)
                        .onSuccess {
                            toastState.success("Card created successfully")
                            refreshCards()
                        }
                        .onFailure { toastState.error("Failed to create card: ${it.message}") }
                }
                showCreateDialog = false
            }
        )
    }

    if (cardToTopUp != null) {
        TopUpDialog(
            card = cardToTopUp!!,
            onDismiss = { cardToTopUp = null },
            onTopUp = { cardId, amount ->
                scope.launch {
                    apiClient.updateCard(cardId, balance = amount)
                        .onSuccess {
                            toastState.success("Balance updated: $$amount")
                            refreshCards()
                        }
                        .onFailure { toastState.error("Failed to update balance: ${it.message}") }
                }
                cardToTopUp = null
            }
        )
    }
}

@Composable
private fun CardItem(
    card: Card,
    isAdmin: Boolean,
    apiClient: ApiClient,
    toastState: dynamic,
    onRefresh: () -> Unit,
    onTopUp: () -> Unit
) {
    var isBlocked by remember { mutableStateOf(card.isBlocked) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Text(
                        text = "Card: ${card.cardNumber}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Owner: ${card.ownerName ?: "N/A"}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Balance: $${card.balance}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top Up кнопка для всех
                IconButton(onClick = onTopUp) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Top Up",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = if (isBlocked) "Blocked" else "Active",
                        color = if (isBlocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    if (isAdmin) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = !isBlocked,
                            onCheckedChange = { active ->
                                isBlocked = !active
                                scope.launch {
                                    apiClient.updateCard(card.id, isBlocked = isBlocked)
                                        .onSuccess { toastState.success("Card updated") }
                                        .onFailure {
                                            isBlocked = !isBlocked
                                            toastState.error("Failed to update card: ${it.message}")
                                            onRefresh()
                                        }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateCardDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String?, Double) -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var initialBalance by remember { mutableStateOf("0.00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Create New Card") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it },
                    label = { Text("Card Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter card number") }
                )
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Owner Name (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter owner name") }
                )
                OutlinedTextField(
                    value = initialBalance,
                    onValueChange = { initialBalance = it },
                    label = { Text("Initial Balance") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.00") },
                    prefix = { Text("$") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val balance = initialBalance.toDoubleOrNull() ?: 0.0
                    onCreate(cardNumber, ownerName.ifBlank { null }, balance)
                },
                enabled = cardNumber.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TopUpDialog(
    card: Card,
    onDismiss: () -> Unit,
    onTopUp: (Int, Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.MonetizationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
        },
        title = { Text("Top Up Balance") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Card: ${card.cardNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Current Balance: $${card.balance}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("New Balance") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter new balance") },
                    prefix = { Text("$") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newBalance = amount.toDoubleOrNull() ?: card.balance
                    onTopUp(card.id, newBalance)
                },
                enabled = amount.isNotBlank()
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
