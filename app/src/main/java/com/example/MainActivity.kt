package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.NodeJsViewModel
import com.example.ui.components.GuidesView
import com.example.ui.components.RequestClientView
import com.example.ui.components.RouteEditorView
import com.example.ui.components.TerminalView
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: NodeJsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) { // Force developer dark visual theme
                Scaffold(
                    modifier = Modifier.fillMaxSize().background(Color(0xFF070B11)),
                    bottomBar = {
                        BottomNavigationBar()
                    }
                ) { innerPadding ->
                    DashboardScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private var activeTabState = mutableStateOf(DashboardTab.TERMINAL)

    enum class DashboardTab(val title: String, val icon: ImageVector) {
        TERMINAL("Terminal", Icons.Default.PlayArrow),
        ROUTER("Router Map", Icons.Default.List),
        CLIENT("REST Client", Icons.Default.Send),
        GUIDES("Guides", Icons.Default.Info)
    }

    @Composable
    fun BottomNavigationBar() {
        var activeTab by activeTabState
        
        Surface(
            color = Color(0xFF131A26),
            border = BorderStroke(1.dp, Color(0xFF232D3F)),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // Safe margin protection on system gesture block
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DashboardTab.entries.forEach { tab ->
                    val isSelected = activeTab == tab
                    val tintColor = if (isSelected) Color(0xFF2ECC71) else Color(0xFF64748B)
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { activeTab = tab }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = tintColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.title,
                            color = tintColor,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun DashboardScreen(viewModel: NodeJsViewModel, modifier: Modifier = Modifier) {
        val activeTab by activeTabState
        val routes by viewModel.routes.collectAsStateWithLifecycle()
        val stats by viewModel.stats.collectAsStateWithLifecycle()
        val logs by viewModel.logs.collectAsStateWithLifecycle()
        val uptimeSeconds by viewModel.uptimeSeconds.collectAsStateWithLifecycle()

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF070B11))
                .statusBarsPadding() // Safe notch margins padding
        ) {
            // Elegant dev header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131A26))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(if (stats.isRunning) Color(0xFF2ECC71) else Color(0xFF64748B))
                    )
                    Text(
                        text = "NODEJS SERVER SIMULATOR",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF0B1015))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Express Node_v${stats.nodeVersion}",
                        color = Color(0xFF2ECC71),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Panel router
            when (activeTab) {
                DashboardTab.TERMINAL -> {
                    TerminalView(
                        stats = stats,
                        logs = logs,
                        uptimeSeconds = uptimeSeconds,
                        onStart = { viewModel.startServer() },
                        onStop = { viewModel.stopServer() },
                        onRestart = { viewModel.restartServer() },
                        onClearLogs = { viewModel.clearLogs() },
                        onInputLine = { strLine ->
                            viewModel.addLog(strLine, com.example.server.LogType.STDOUT)
                        }
                    )
                }
                DashboardTab.ROUTER -> {
                    RouteEditorView(
                        routes = routes,
                        onAddRoute = { method, path, code, body, desc, onFin ->
                            viewModel.addNewRoute(method, path, code, body, desc, onFin)
                        },
                        onDeleteRoute = { route ->
                            viewModel.deleteRoute(route)
                        }
                    )
                }
                DashboardTab.CLIENT -> {
                    RequestClientView(
                        routes = routes,
                        isServerRunning = stats.isRunning,
                        onSendRequest = { method, urlPath, bStr, hStr, onRes ->
                            viewModel.simulateClientRequest(method, urlPath, bStr, hStr, onRes)
                        }
                    )
                }
                DashboardTab.GUIDES -> {
                    GuidesView()
                }
            }
        }
    }
}
