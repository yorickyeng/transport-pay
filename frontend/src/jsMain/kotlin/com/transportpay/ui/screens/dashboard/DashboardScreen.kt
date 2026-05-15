package com.transportpay.ui.screens.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.transportpay.data.models.Transaction
import com.transportpay.ui.components.EmptyState
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(apiClient: ApiClient) {
    var cardsCount by remember { mutableStateOf<Int?>(null) }
    var terminalsCount by remember { mutableStateOf<Int?>(null) }
    var transactionsCount by remember { mutableStateOf<Int?>(null) }
    var totalAmount by remember { mutableStateOf<Double?>(null) }
    var recentTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Animated numbers
    val animatedCardsCount by animateFloatAsState(
        targetValue = cardsCount?.toFloat() ?: 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing)
    )
    val animatedTerminalsCount by animateFloatAsState(
        targetValue = terminalsCount?.toFloat() ?: 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing)
    )
    val animatedTransactionsCount by animateFloatAsState(
        targetValue = transactionsCount?.toFloat() ?: 0f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing)
    )
    val animatedTotalAmount by animateFloatAsState(
        targetValue = (totalAmount ?: 0.0).toFloat(),
        animationSpec = tween(1000, easing = FastOutSlowInEasing)
    )

    LaunchedEffect(Unit) {
        isLoading = true
        scope.launch {
            try {
                val cardsResult = apiClient.getCards()
                val terminalsResult = apiClient.getTerminals()
                val transactionsResult = apiClient.getTransactions()

                cardsResult.onSuccess { cardsCount = it.size }
                terminalsResult.onSuccess { terminalsCount = it.size }
                transactionsResult.onSuccess { txns ->
                    transactionsCount = txns.size
                    totalAmount = txns.sumOf { it.amount }
                    recentTransactions = txns.take(5)
                }

                // Игнорируем ошибки при первоначальной загрузке — показываем пустое состояние
                // Ошибки показываются только после действий пользователя
            } catch (e: Exception) {
                // Игнорируем ошибки при первоначальной загрузке
            }
        }
        isLoading = false
    }

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
                text = "Dashboard",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Overview of your payment system",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(400.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Stats cards
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    title = "Total Cards",
                    value = animatedCardsCount.toInt().toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Terminals",
                    value = animatedTerminalsCount.toInt().toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Transactions",
                    value = animatedTransactionsCount.toInt().toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Total Amount",
                    value = "$${animatedTotalAmount.toInt()}",
                    modifier = Modifier.weight(1f)
                )
            }

            // Recent transactions
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (recentTransactions.isEmpty()) {
                    EmptyState(
                        message = "No transactions yet",
                        subMessage = "Transactions will appear here once payments are processed"
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        recentTransactions.forEach { transaction ->
                            TransactionItem(transaction)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(140.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Transaction #${transaction.id}",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Card: ${transaction.cardId} • Terminal: ${transaction.terminalId}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$${transaction.amount}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
