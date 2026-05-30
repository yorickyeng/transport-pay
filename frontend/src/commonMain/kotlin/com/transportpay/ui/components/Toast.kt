package com.transportpay.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class ToastMessage(
    val message: String,
    val type: ToastType = ToastType.INFO
)

enum class ToastType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO
}

class ToastState {
    var currentToast by mutableStateOf<ToastMessage?>(null)
        private set

    fun success(message: String) {
        currentToast = ToastMessage(message, ToastType.SUCCESS)
    }

    fun error(message: String) {
        currentToast = ToastMessage(message, ToastType.ERROR)
    }

    fun warning(message: String) {
        currentToast = ToastMessage(message, ToastType.WARNING)
    }

    fun info(message: String) {
        currentToast = ToastMessage(message, ToastType.INFO)
    }

    fun dismiss() {
        currentToast = null
    }
}

@Composable
fun rememberToastState(): ToastState {
    return remember { ToastState() }
}

@Composable
fun getToastState(): ToastState {
    return rememberToastState()
}

@Composable
fun ToastDisplay(toastState: ToastState, onDismiss: () -> Unit) {
    AnimatedVisibility(
        visible = toastState.currentToast != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { _ -> -20 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { _ -> -20 })
    ) {
        toastState.currentToast?.let { toast ->
            ToastItem(
                message = toast.message,
                type = toast.type,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun ToastItem(
    message: String,
    type: ToastType,
    onDismiss: () -> Unit
) {
    // Auto-dismiss after 3 seconds
    LaunchedEffect(Unit) {
        delay(3000)
        onDismiss()
    }

    val (icon, backgroundColor) = when (type) {
        ToastType.SUCCESS -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        ToastType.ERROR -> Icons.Default.Error to Color(0xFFF44336)
        ToastType.WARNING -> Icons.Default.Warning to Color(0xFFFF9800)
        ToastType.INFO -> Icons.Default.Info to MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
