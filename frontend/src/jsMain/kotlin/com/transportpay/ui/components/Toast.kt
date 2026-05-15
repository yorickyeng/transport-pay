package com.transportpay.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ToastMessage(
    val id: Int,
    val message: String,
    val type: ToastType = ToastType.INFO,
    val duration: Long = 3000
)

enum class ToastType {
    SUCCESS, ERROR, INFO, WARNING
}

class ToastState {
    private val _toasts = mutableStateListOf<ToastMessage>()
    val toasts: List<ToastMessage> get() = _toasts

    private var nextId = 0

    fun showToast(message: String, type: ToastType = ToastType.INFO, duration: Long = 3000) {
        val toast = ToastMessage(nextId++, message, type, duration)
        _toasts.add(toast)

        // Auto-dismiss using GlobalScope for JS
        GlobalScope.launch {
            delay(duration)
            dismissToast(toast.id)
        }
    }

    fun dismissToast(id: Int) {
        _toasts.removeAll { it.id == id }
    }

    fun success(message: String) = showToast(message, ToastType.SUCCESS)
    fun error(message: String) = showToast(message, ToastType.ERROR)
    fun info(message: String) = showToast(message, ToastType.INFO)
    fun warning(message: String) = showToast(message, ToastType.WARNING)
}

private val toastState = ToastState()

fun getToastState(): ToastState = toastState

@Composable
fun ToastContainer() {
    val toasts = toastState.toasts.toList()
    
    // Позиционируем toast'ы вверху экрана по центру
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            toasts.forEach { toast ->
                ToastItem(
                    toast = toast,
                    onDismiss = { toastState.dismissToast(toast.id) }
                )
            }
        }
    }
}

@Composable
private fun ToastItem(
    toast: ToastMessage,
    onDismiss: () -> Unit
) {
    val backgroundColor = when (toast.type) {
        ToastType.SUCCESS -> Color(0xFF22C55E)
        ToastType.ERROR -> Color(0xFFEF4444)
        ToastType.INFO -> Color(0xFF3B82F6)
        ToastType.WARNING -> Color(0xFFF59E0B)
    }

    val icon = when (toast.type) {
        ToastType.SUCCESS -> Icons.Default.CheckCircle
        ToastType.ERROR -> Icons.Default.Warning
        ToastType.INFO -> Icons.Default.Info
        ToastType.WARNING -> Icons.Default.Warning
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it -> -20 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it -> -20 })
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .shadow(4.dp, RoundedCornerShape(8.dp))
                .background(backgroundColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = toast.message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
