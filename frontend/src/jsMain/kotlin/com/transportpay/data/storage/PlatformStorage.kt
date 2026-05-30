package com.transportpay.data.storage

import kotlinx.browser.localStorage

actual object PlatformStorage {
    actual fun getItem(key: String): String? = localStorage.getItem(key)
    actual fun setItem(key: String, value: String) = localStorage.setItem(key, value)
    actual fun removeItem(key: String) = localStorage.removeItem(key)
}
