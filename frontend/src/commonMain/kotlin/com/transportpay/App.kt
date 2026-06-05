package com.transportpay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.transportpay.data.api.ApiClient
import com.transportpay.data.storage.TokenStorage
import com.transportpay.ui.components.rememberToastState
import com.transportpay.ui.components.ToastDisplay
import com.transportpay.ui.navigation.Screen
import com.transportpay.ui.screens.cards.CardsScreen
import com.transportpay.ui.screens.dashboard.DashboardScreen
import com.transportpay.ui.screens.keys.KeysScreen
import com.transportpay.ui.screens.login.LoginScreen
import com.transportpay.ui.screens.settings.SettingsScreen
import com.transportpay.ui.screens.simulator.SimulatorScreen
import com.transportpay.ui.screens.terminals.TerminalsScreen
import com.transportpay.ui.screens.transactions.TransactionsScreen
import com.transportpay.ui.screens.users.UsersScreen
import com.transportpay.ui.theme.TransportPayTheme

@Composable
fun App(nfcService: NfcService? = null) {
    val apiClient = remember { ApiClient() }
    val isLoggedIn = TokenStorage.isLoggedIn()
    val isAdmin = TokenStorage.userIsAdmin
    var currentScreen by remember { mutableStateOf<Screen>(if (isLoggedIn) Screen.Dashboard else Screen.Login) }
    var darkTheme by remember { mutableStateOf(TokenStorage.theme != "light") }
    var sidebarExpanded by remember { mutableStateOf(true) }
    val toastState = rememberToastState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            apiClient.setToken(TokenStorage.token)
        }
    }

    TransportPayTheme(darkTheme = darkTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (!isLoggedIn && currentScreen == Screen.Login) {
                LoginScreen(apiClient = apiClient) {
                    currentScreen = Screen.Dashboard
                }
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Sidebar
                    AnimatedVisibility(
                        visible = sidebarExpanded,
                        enter = slideInHorizontally(initialOffsetX = { -250 }) + fadeIn(),
                        exit = slideOutHorizontally(targetOffsetX = { -250 }) + fadeOut()
                    ) {
                        Sidebar(
                            currentScreen = currentScreen,
                            onNavigate = { currentScreen = it },
                            isAdmin = isAdmin,
                            modifier = Modifier.width(260.dp)
                        )
                    }

                    // Main content with Scaffold
                    Scaffold(
                        modifier = Modifier.weight(1f),
                        topBar = {
                            TopBar(
                                title = getScreenTitle(currentScreen),
                                onMenuClick = { sidebarExpanded = !sidebarExpanded },
                                darkTheme = darkTheme,
                                onThemeToggle = {
                                    darkTheme = !darkTheme
                                    TokenStorage.theme = if (!darkTheme) "dark" else "light"
                                }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(horizontal = 24.dp, vertical = 16.dp)
                        ) {
                            when (currentScreen) {
                                Screen.Dashboard -> DashboardScreen(apiClient)
                                Screen.Cards -> CardsScreen(apiClient, isAdmin)
                                Screen.Terminals -> TerminalsScreen(apiClient, isAdmin)
                                Screen.Transactions -> TransactionsScreen(apiClient)
                                Screen.Keys -> KeysScreen(apiClient, isAdmin)
                                Screen.Users -> if (isAdmin) UsersScreen(apiClient) else AccessDeniedScreen()
                                Screen.Simulator -> SimulatorScreen(apiClient, nfcService)
                                Screen.Settings -> SettingsScreen(darkTheme, { d -> darkTheme = d }) { currentScreen = Screen.Login }
                                else -> {}
                            }
                        }
                    }
                }
            }

            ToastDisplay(
                toastState = toastState,
                onDismiss = { toastState.dismiss() }
            )
        }
    }
}

private fun getScreenTitle(screen: Screen): String = when (screen) {
    Screen.Dashboard -> "Dashboard"
    Screen.Cards -> "Cards Management"
    Screen.Terminals -> "Terminals"
    Screen.Transactions -> "Transaction History"
    Screen.Keys -> "Security Keys"
    Screen.Users -> "User Management"
    Screen.Simulator -> "Terminal Simulator"
    Screen.Settings -> "System Settings"
    else -> "Transport Pay"
}

@Composable
private fun Sidebar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    isAdmin: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp, start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Transport Pay",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(bottom = 8.dp))

            // Main navigation
            SidebarItem(Icons.Default.Home, "Dashboard", currentScreen == Screen.Dashboard) { onNavigate(Screen.Dashboard) }
            SidebarItem(Icons.Default.AccountBox, "Cards", currentScreen == Screen.Cards) { onNavigate(Screen.Cards) }
            SidebarItem(Icons.Default.Place, "Terminals", currentScreen == Screen.Terminals) { onNavigate(Screen.Terminals) }
            SidebarItem(Icons.Default.List, "Transactions", currentScreen == Screen.Transactions) { onNavigate(Screen.Transactions) }

            if (isAdmin) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "ADMINISTRATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
                SidebarItem(Icons.Default.Lock, "Security Keys", currentScreen == Screen.Keys) { onNavigate(Screen.Keys) }
                SidebarItem(Icons.Default.Person, "Users", currentScreen == Screen.Users) { onNavigate(Screen.Users) }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 8.dp))

            // Tools
            SidebarItem(Icons.Default.PlayArrow, "Simulator", currentScreen == Screen.Simulator) { onNavigate(Screen.Simulator) }
            SidebarItem(Icons.Default.Settings, "Settings", currentScreen == Screen.Settings) { onNavigate(Screen.Settings) }
        }
    }
}

@Composable
private fun SidebarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = contentColor,
            fontWeight = fontWeight
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    title: String,
    onMenuClick: () -> Unit,
    darkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Toggle Menu"
                )
            }
        },
        actions = {
            IconButton(onClick = onThemeToggle) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Toggle Theme"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "User",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = TokenStorage.userLogin ?: "User",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}

@Composable
private fun AccessDeniedScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Access Denied",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Access Denied",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = "You don't have permission to view this page.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
