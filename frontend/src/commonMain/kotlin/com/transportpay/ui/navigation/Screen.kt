package com.transportpay.ui.navigation

sealed class Screen {
    object Login : Screen()
    object Dashboard : Screen()
    object Cards : Screen()
    object Terminals : Screen()
    object Transactions : Screen()
    object Keys : Screen()
    object Users : Screen()
    object Simulator : Screen()
    object Settings : Screen()
}
