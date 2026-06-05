package com.transportpay

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class NfcService(private val driver: PN532Driver) {

    // Создаем "горячий" поток событий. UI сможет подписываться на него.
    private val _nfcEvents = MutableSharedFlow<String>()
    val nfcEvents = _nfcEvents.asSharedFlow()

    fun startListening(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            if (!driver.initialize()) {
                println("[NfcService] Ошибка инициализации PN532.")
                return@launch
            }
            
            println("[NfcService] Модуль готов. Ожидание карты...")
            
            while (true) {
                val uid = driver.pollForCard()
                if (uid != null) {
                    println("[NfcService] Карта обнаружена: $uid")
                    _nfcEvents.emit(uid)
                    // Пауза 3 секунды, чтобы не спамить событиями, пока карта лежит на модуле
                    delay(3000) 
                } else {
                    delay(500)
                }
            }
        }
    }
}
