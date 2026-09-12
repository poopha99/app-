package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TranslationRepository
import com.example.ui.screens.DictionaryScreen
import com.example.ui.screens.GameSimulatorScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

sealed class NavTab(val title: String, val icon: ImageVector) {
    object Home : NavTab("ปุ่มลอย & แปล", Icons.Default.ElectricBolt)
    object Simulator : NavTab("จำลองแปลเกม", Icons.Default.Gamepad)
    object Dictionary : NavTab("คลังศัพท์", Icons.Default.MenuBook)
    object History : NavTab("ประวัติ", Icons.Default.History)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTranslatorApp() {
    val context = LocalContext.current
    val repository = remember { TranslationRepository.getInstance(context) }
    var currentTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        NavTab.Home,
        NavTab.Simulator,
        NavTab.Dictionary,
        NavTab.History
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🎮 แปลเกมไต้หวันสด (TW ➔ TH)",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberBlack,
                    titleContentColor = CyberCyan
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CyberDarkSurface,
                modifier = Modifier
                    .border(width = 0.5.dp, color = CyberCardBorder)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = currentTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) CyberCyan else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CyberCyan else TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberCyan,
                            unselectedIconColor = TextMuted,
                            indicatorColor = CyberCyan.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CyberBlack)
                .padding(innerPadding)
        ) {
            when (currentTabIndex) {
                0 -> HomeScreen(
                    repository = repository,
                    onNavigateToSimulator = { currentTabIndex = 1 }
                )
                1 -> GameSimulatorScreen(repository = repository)
                2 -> DictionaryScreen()
                3 -> HistoryScreen(repository = repository)
            }
        }
    }
}
