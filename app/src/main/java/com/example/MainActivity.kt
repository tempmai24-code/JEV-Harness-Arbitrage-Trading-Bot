package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppTab
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = JevDarkBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = JevDarkSurface,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.FLASH_LOAN,
                    onClick = { viewModel.selectTab(AppTab.FLASH_LOAN) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Flash Loan"
                        )
                    },
                    label = {
                        Text(
                            text = "Flash Loan",
                            fontSize = 10.sp,
                            fontWeight = if (currentTab == AppTab.FLASH_LOAN) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JevGoldAccent,
                        selectedTextColor = JevGoldAccent,
                        unselectedIconColor = JevTextMuted,
                        unselectedTextColor = JevTextMuted,
                        indicatorColor = JevGoldAccent.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("tab_flash_loan")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.TRADING_DESK,
                    onClick = { viewModel.selectTab(AppTab.TRADING_DESK) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Trading Desk"
                        )
                    },
                    label = {
                        Text(
                            text = "Trading",
                            fontSize = 10.sp,
                            fontWeight = if (currentTab == AppTab.TRADING_DESK) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JevCyanPrimary,
                        selectedTextColor = JevCyanPrimary,
                        unselectedIconColor = JevTextMuted,
                        unselectedTextColor = JevTextMuted,
                        indicatorColor = JevCyanPrimary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("tab_trading_desk")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.AGENT_GATING,
                    onClick = { viewModel.selectTab(AppTab.AGENT_GATING) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Agent Gating"
                        )
                    },
                    label = {
                        Text(
                            text = "Gating",
                            fontSize = 10.sp,
                            fontWeight = if (currentTab == AppTab.AGENT_GATING) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JevCyanPrimary,
                        selectedTextColor = JevCyanPrimary,
                        unselectedIconColor = JevTextMuted,
                        unselectedTextColor = JevTextMuted,
                        indicatorColor = JevCyanPrimary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("tab_agent_gating")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.PLAYGROUND,
                    onClick = { viewModel.selectTab(AppTab.PLAYGROUND) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Playground"
                        )
                    },
                    label = {
                        Text(
                            text = "Jev API",
                            fontSize = 10.sp,
                            fontWeight = if (currentTab == AppTab.PLAYGROUND) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JevCyanPrimary,
                        selectedTextColor = JevCyanPrimary,
                        unselectedIconColor = JevTextMuted,
                        unselectedTextColor = JevTextMuted,
                        indicatorColor = JevCyanPrimary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("tab_playground")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.BLUEPRINTS,
                    onClick = { viewModel.selectTab(AppTab.BLUEPRINTS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = "Blueprints"
                        )
                    },
                    label = {
                        Text(
                            text = "Docs/Code",
                            fontSize = 10.sp,
                            fontWeight = if (currentTab == AppTab.BLUEPRINTS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = JevCyanPrimary,
                        selectedTextColor = JevCyanPrimary,
                        unselectedIconColor = JevTextMuted,
                        unselectedTextColor = JevTextMuted,
                        indicatorColor = JevCyanPrimary.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.testTag("tab_blueprints")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.FLASH_LOAN -> FlashLoanScreen(viewModel = viewModel)
                AppTab.TRADING_DESK -> TradingDeskScreen(viewModel = viewModel)
                AppTab.AGENT_GATING -> AgentGatingScreen(viewModel = viewModel)
                AppTab.PLAYGROUND -> PlaygroundScreen(viewModel = viewModel)
                AppTab.BLUEPRINTS -> BlueprintsScreen(viewModel = viewModel)
            }
        }
    }
}
