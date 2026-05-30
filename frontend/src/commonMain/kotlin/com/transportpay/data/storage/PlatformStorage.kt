package com.transportpay.data.storage

expect object PlatformStorage {
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
    fun removeItem(key: String)
}
