package com.transportpay

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

fun main() = application {
    val scanner = NfcScanner()
    val port = scanner.findAndOpenPort()
    
    var nfcService: NfcService? = null

    if (port != null) {
        nfcService = NfcService(PN532Driver(port))
        // Запускаем фоновый опрос модуля
        nfcService.startListening(CoroutineScope(Dispatchers.IO))
    } else {
        println("⚠️ NFC-модуль не подключен. Приложение работает в ручном режиме.")
    }

    Window(
        onCloseRequest = {
            scanner.closePort(port)
            exitApplication()
        },
        title = "Transport Pay (NFC Enabled)",
        state = rememberWindowState(width = 1200.dp, height = 800.dp),
    ) {
        // Передаем сервис в основное приложение
        App(nfcService)
    }
}
