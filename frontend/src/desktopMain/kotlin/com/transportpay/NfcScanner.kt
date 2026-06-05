package com.transportpay

import com.fazecast.jSerialComm.SerialPort

class NfcScanner {
    fun findAndOpenPort(): SerialPort? {
        val availablePorts = SerialPort.getCommPorts()
        
        // Ищем адаптер Silicon Labs (CP2102/CP2103)
        val port = availablePorts.firstOrNull { 
            it.systemPortName.contains("SLAB", ignoreCase = true) || 
            it.systemPortName.contains("usbserial", ignoreCase = true) 
        } ?: return null

        port.baudRate = 115200
        port.numDataBits = 8
        port.numStopBits = SerialPort.ONE_STOP_BIT
        port.parity = SerialPort.NO_PARITY

        // Критично для обхода блокировок ядра macOS
        port.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED)
        port.clearDTR()
        port.clearRTS()

        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 0)

        return if (port.openPort()) port else null
    }

    fun closePort(port: SerialPort?) {
        port?.closePort()
    }
}
