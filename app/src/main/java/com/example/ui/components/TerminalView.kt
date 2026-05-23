package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.server.LogType
import com.example.server.ServerStats
import com.example.server.TerminalLog
import java.util.Locale

@Composable
fun TerminalView(
    stats: ServerStats,
    logs: List<TerminalLog>,
    uptimeSeconds: Int,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRestart: () -> Unit,
    onClearLogs: () -> Unit,
    onInputLine: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val terminalBg = Color(0xFF0F141C)
    val textStyle = MaterialTheme.typography.bodyMedium.copy(
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium
    )

    var consoleInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val listState = rememberLazyListState()
    // Auto Scroll to bottom when logs change
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    // Format uptime
    val hours = uptimeSeconds / 3600
    val minutes = (uptimeSeconds % 3600) / 60
    val seconds = uptimeSeconds % 60
    val uptimeText = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B11))
    ) {
        // Upper Quick Stats Console Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Server Power Widget
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF131A26))
                    .border(1.dp, Color(0xFF232D3F), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "PROCESS STATUS",
                            color = Color(0xFF64748B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        // Pulsing online dot
                        if (stats.isRunning) {
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 0.4f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "alpha"
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF2ECC71).copy(alpha = alpha))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE74C3C))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (stats.isRunning) "ONLINE" else "OFFLINE",
                        color = if (stats.isRunning) Color(0xFF2ECC71) else Color(0xFFE74C3C),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Uptime: $uptimeText",
                        color = Color(0xFF94A3B8),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }

            // Hits Metrics Widget
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF131A26))
                    .border(1.dp, Color(0xFF232D3F), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "TOTAL REQS",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${stats.totalRequests}",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Latency: ${stats.avgLatencyMs}ms",
                        color = Color(0xFF00FFCC),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Health Status Badge Details
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF131A26))
                    .border(1.dp, Color(0xFF232D3F), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "HEALTH",
                        color = Color(0xFF64748B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${stats.successfulRequests} OK",
                        color = Color(0xFF2ECC71),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${stats.failedRequests} FAIL",
                        color = Color(0xFFE74C3C),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Terminal Panel Title with Server Controllers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Terminal",
                    tint = Color(0xFF2ECC71),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "LOCAL DEV TERMINAL",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }

            // Power Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (stats.isRunning) {
                    IconButton(
                        onClick = onRestart,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart",
                            tint = Color(0xFF00FFCC),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Button(
                        onClick = onStop,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC0392B)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("STOP", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27AE60)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("START SERVER", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(
                    onClick = onClearLogs,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear logs",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Interactive Console Shell Box
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(terminalBg)
                .border(1.dp, Color(0xFF232D3F), RoundedCornerShape(8.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Outer Console Logs
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    items(logs, key = { it.id }) { log ->
                        val logColor = when (log.type) {
                            LogType.SYSTEM -> Color(0xFF9B59B6) // Node/Nodemon/Admin purple
                            LogType.STDOUT -> Color(0xFFBDC3C7) // Standard echo text
                            LogType.HTTP_IN -> Color(0xFF3498DB) // Outbound direction blue
                            LogType.SUCCESS -> Color(0xFF2ECC71) // Ok status green
                            LogType.ERROR -> Color(0xFFE74C3C) // Error/404 red
                            LogType.INFO -> Color(0xFFF1C40F) // Dev highlights
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp)
                        ) {
                            Text(
                                text = "[${log.getFormattedTime()}] ",
                                color = Color(0xFF475569),
                                style = textStyle,
                                modifier = Modifier.width(68.dp)
                            )
                            Text(
                                text = log.message,
                                color = logColor,
                                style = textStyle,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Interactive script injector
                Divider(color = Color(0xFF1E293B), thickness = 1.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF090D14))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "console.log( ",
                        color = Color(0xFFF1C40F),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextField(
                        value = consoleInput,
                        onValueChange = { textVal -> consoleInput = textVal },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color(0xFF00FFCC),
                            unfocusedTextColor = Color(0xFF00FFCC)
                        ),
                        placeholder = {
                            Text(
                                text = "'Hello from server!'",
                                color = Color(0xFF475569),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (consoleInput.isNotBlank()) {
                                    onInputLine(consoleInput)
                                    consoleInput = ""
                                    keyboardController?.hide()
                                }
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp), // Increase height slightly as contentPadding is removed
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        ),
                        singleLine = true
                    )
                    Text(
                        text = " );",
                        color = Color(0xFFF1C40F),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    IconButton(
                        onClick = {
                            if (consoleInput.isNotBlank()) {
                                onInputLine(consoleInput)
                                consoleInput = ""
                                keyboardController?.hide()
                            }
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Inject Log",
                            tint = Color(0xFF2ECC71),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
