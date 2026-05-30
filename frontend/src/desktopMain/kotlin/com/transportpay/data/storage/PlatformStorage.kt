package com.transportpay.data.storage

import java.util.prefs.Preferences

actual object PlatformStorage {
    private val prefs: Preferences = Preferences.userNodeForPackage(PlatformStorage::class.java)

    actual fun getItem(key: String): String? = prefs.get(key, null)
    actual fun setItem(key: String, value: String) = prefs.put(key, value)
    actual fun removeItem(key: String) = prefs.remove(key)
}
