package com.transportpay

import androidx.compose.ui.window.CanvasBasedWindow
import androidx.compose.ui.ExperimentalComposeUiApi
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        CanvasBasedWindow("Transport Pay", canvasElementId = "ComposeTarget") {
            App()
        }
    }
}