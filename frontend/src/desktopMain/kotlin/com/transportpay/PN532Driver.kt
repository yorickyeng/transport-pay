package com.transportpay

import com.fazecast.jSerialComm.SerialPort

class PN532Driver(private val port: SerialPort) {

    private val WAKE_UP = byteArrayOf(
        0x55, 0x55, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    )

    private val SAM_CONFIG = byteArrayOf(
        0x00, 0x00, 0xFF.toByte(), 0x03, 0xFD.toByte(), 
        0xD4.toByte(), 0x14, 0x01, 0x17, 0x00
    )

    private val READ_CARD = byteArrayOf(
        0x00, 0x00, 0xFF.toByte(), 0x04, 0xFC.toByte(), 
        0xD4.toByte(), 0x4A, 0x01, 0x00, 0xE1.toByte(), 0x00
    )

    fun initialize(): Boolean {
        port.writeBytes(WAKE_UP, WAKE_UP.size)
        Thread.sleep(200)

        flushBuffer()

        port.writeBytes(SAM_CONFIG, SAM_CONFIG.size)
        val response = readBytesWithTimeout(500)
        
        return response.size >= 6
    }

    fun pollForCard(): String? {
        flushBuffer()
        port.writeBytes(READ_CARD, READ_CARD.size)

        var totalResponse = byteArrayOf()
        var attempts = 0
        
        // Опрашиваем порт короткими интервалами, формируя пакет
        while (attempts < 20) {
            Thread.sleep(100)
            val available = port.bytesAvailable()
            
            if (available > 0) {
                val buffer = ByteArray(available)
                val readCount = port.readBytes(buffer, buffer.size)
                if (readCount > 0) {
                    totalResponse += buffer.copyOf(readCount)
                }
            }

            // Поиск маркера D5 4B 01 01 (InListPassiveTarget Response)
            val targetIndex = totalResponse.indexOf(byteArrayOf(0xD5.toByte(), 0x4B, 0x01, 0x01))
            if (targetIndex != -1) {
                val uidLengthIndex = targetIndex + 7
                if (uidLengthIndex < totalResponse.size) {
                    val uidLength = totalResponse[uidLengthIndex].toInt()
                    if (uidLengthIndex + 1 + uidLength <= totalResponse.size) {
                        val uidBytes = totalResponse.copyOfRange(uidLengthIndex + 1, uidLengthIndex + 1 + uidLength)
                        return uidBytes.joinToString("") { "%02X".format(it) }
                    }
                }
            }
            attempts++
        }
        return null
    }

    private fun readBytesWithTimeout(timeoutMs: Int): ByteArray {
        Thread.sleep(timeoutMs.toLong())
        val available = port.bytesAvailable()
        if (available <= 0) return byteArrayOf()
        
        val buffer = ByteArray(available)
        port.readBytes(buffer, available)
        return buffer
    }

    private fun flushBuffer() {
        while (port.bytesAvailable() > 0) {
            val flush = ByteArray(port.bytesAvailable())
            port.readBytes(flush, flush.size)
        }
    }

    private fun ByteArray.indexOf(subArray: ByteArray): Int {
        if (subArray.isEmpty() || this.size < subArray.size) return -1
        for (i in 0..this.size - subArray.size) {
            var match = true
            for (j in subArray.indices) {
                if (this[i + j] != subArray[j]) {
                    match = false
                    break
                }
            }
            if (match) return i
        }
        return -1
    }
}
