package com.transportpay.data.storage

object TokenStorage {
    private const val TOKEN_KEY = "transport_pay_token"
    private const val USER_ID_KEY = "transport_pay_user_id"
    private const val USER_LOGIN_KEY = "transport_pay_user_login"
    private const val USER_IS_ADMIN_KEY = "transport_pay_user_is_admin"
    private const val THEME_KEY = "transport_pay_theme"

    private val storage get() = PlatformStorage

    var token: String?
        get() = storage.getItem(TOKEN_KEY)
        set(value) {
            if (value != null) storage.setItem(TOKEN_KEY, value)
            else storage.removeItem(TOKEN_KEY)
        }

    var userId: Int?
        get() = storage.getItem(USER_ID_KEY)?.toIntOrNull()
        set(value) {
            if (value != null) storage.setItem(USER_ID_KEY, value.toString())
            else storage.removeItem(USER_ID_KEY)
        }

    var userLogin: String?
        get() = storage.getItem(USER_LOGIN_KEY)
        set(value) {
            if (value != null) storage.setItem(USER_LOGIN_KEY, value)
            else storage.removeItem(USER_LOGIN_KEY)
        }

    var userIsAdmin: Boolean
        get() = storage.getItem(USER_IS_ADMIN_KEY) == "true"
        set(value) {
            storage.setItem(USER_IS_ADMIN_KEY, value.toString())
        }

    var theme: String?
        get() = storage.getItem(THEME_KEY)
        set(value) {
            if (value != null) storage.setItem(THEME_KEY, value)
            else storage.removeItem(THEME_KEY)
        }

    fun isLoggedIn(): Boolean = token != null

    fun clear() {
        storage.removeItem(TOKEN_KEY)
        storage.removeItem(USER_ID_KEY)
        storage.removeItem(USER_LOGIN_KEY)
        storage.removeItem(USER_IS_ADMIN_KEY)
    }
}
